package mainApp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.tensorflow.lite.examples.detection.DetectorActivity
import org.tensorflow.lite.examples.detection.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.mainActBtn)
        button.setOnClickListener{
            val intent = Intent(this, DetectorActivity::class.java)
            startActivity(intent)
        }
    }




}
