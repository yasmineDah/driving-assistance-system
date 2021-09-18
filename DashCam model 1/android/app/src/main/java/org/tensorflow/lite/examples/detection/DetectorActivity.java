/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import app.R;
import app.data.firebase.FirebaseSource;
import app.data.model.LocationHelper;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  private List<String> animals = Arrays.asList("cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe");
  private List<String> traffic = Arrays.asList("bicycle", "car", "motorcycle", "bus", "truck");
  private List<Date> animalDanger = new ArrayList<>();
  private List<Date> trafficDanger = new ArrayList<>();
  private List<Date> workDanger = new ArrayList<>();
  private FirebaseSource source = new FirebaseSource();
  private int numberCars = 0;
  private boolean person=false;
  private boolean truck=false;
  private boolean stop=false;

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              getAssets(),
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

//              int count = (int) results.stream().filter(res -> traffic.contains(res.getTitle()) && res.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API).count();
//              System.out.println("le nombre de véhicules est  : "+count);

//            for (Classifier.Recognition res: results){
//                  //if (cpt == 2) break;
//                  if (animals.contains(res.getTitle())) {
//
//                    System.out.println("YES1");
//                      if ((animalDanger.isEmpty()) ||
//                              ((new Date().getTime()) - (animalDanger.get(animalDanger.size() - 1).getTime()) > 300000 )) {
//
//                        System.out.println("YES2");
//                        animalDanger.add(new Date());
//                        if(DetectorActivity.super.locat != null) {
//
//                          System.out.println("location is ready "+DetectorActivity.super.locat.getAlt());
//                          source.addDanger(locat,1);
//                          break;
//                        }
//
//                      }
//
//                  }
//              }

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
            }

            final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

            for (final Classifier.Recognition result : results) {

              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {

                if(DetectorActivity.super.locat != null) {

                if (animals.contains(result.getTitle())) {
                    System.out.println("YESA1");
                      if ((animalDanger.isEmpty()) ||
                              ((new Date().getTime()) - (animalDanger.get(animalDanger.size() - 1).getTime()) > 300000 )) {

                        System.out.println("YESA2");
                        animalDanger.add(new Date());
                        source.addDanger(locat,1);

                        }

                      }

                if (traffic.contains(result.getTitle())){
                  System.out.println("YEST1"); numberCars++;
                  if(numberCars >= 4) {
                    if ((trafficDanger.isEmpty()) ||
                            ((new Date().getTime()) - (trafficDanger.get(trafficDanger.size() - 1).getTime()) > 300000)) {

                      System.out.println("YEST2");
                      trafficDanger.add(new Date());
                      source.addDanger(locat, 3);
                      numberCars = 0;

                    }
                  }

                }


                  if (result.getTitle().equals("person")) person=true;
                  if (result.getTitle().equals("truck")) truck=true;
                  if (result.getTitle().equals("stop sign")) stop=true;
                  if(person && truck && stop){
                    System.out.println("YESW1");
                    if ((workDanger.isEmpty()) ||
                            ((new Date().getTime()) - (workDanger.get(workDanger.size() - 1).getTime()) > 300000)) {

                      System.out.println("YESW2");
                      workDanger.add(new Date());
                      source.addDanger(locat, 2);
                      person=false;
                      truck=false;
                      stop=false;

                    }
                  }


                }

                canvas.drawRect(location, paint);
                cropToFrameTransform.mapRect(location);
                result.setLocation(location);
                mappedRecognitions.add(result);


              }
            }


            tracker.trackResults(mappedRecognitions, currTimestamp);
            trackingOverlay.postInvalidate();

            computingDetection = false;

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {

                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                    showInference(lastProcessingTimeMs + "ms");
                  }
                });
          }
        });
  }

//  private void insertDanger(int i) {
//
//    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//      getLastLocation(i);
//      }
//
//     else {
//      askLocationPermission();
//    }
//
//  }
//
//
//  private void  getLastLocation(int i) {
//
//    super.locat.addOnSuccessListener(new OnSuccessListener<Location>() {
//      @Override
//      public void onSuccess(Location location) {
//        if (location != null) {
//          locat = new LocationHelper(location.getLatitude(),location.getLongitude());
//          source.addDanger(locat, i);
//          //We have a location
//          Log.d(TAG, "onSuccess: " + locat);
//          Log.d(TAG, "onSuccess: " + location.getLatitude());
//          Log.d(TAG, "onSuccess: " + location.getLongitude());
//          Log.d("place 1", "vous etes arrivé ici " );
//        } else  {
//          Log.d(TAG, "onSuccess: Location was null...");
//        }
//      }
//    });
//
//    locationTask.addOnFailureListener(new OnFailureListener() {
//      @Override
//      public void onFailure(@NonNull Exception e) {
//        Log.e(TAG, "onFailure: " + e.getLocalizedMessage() );
//      }
//    });
//
//  }
//
//  private void askLocationPermission() {
//    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//        Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
//        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
//      } else {
//        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
//      }
//    }
//  }
//
//  @Override
//  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//    if (requestCode == LOCATION_REQUEST_CODE) {
//      if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        // Permission granted
//        getLastLocation(1);
//      } else {
//        //Permission not granted
//      }
//    }
//  }



  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
}
