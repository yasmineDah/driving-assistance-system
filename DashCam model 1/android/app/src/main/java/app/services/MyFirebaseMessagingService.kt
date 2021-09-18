package app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import app.R
import app.data.firebase.FirebaseSource
import app.ui.auth.LoginActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


class MyFirebaseMessagingService: FirebaseMessagingService() {


    private val firebase = FirebaseSource()
    private val YOUR_CHANNEL_ID = "iCar.dashCam.ilyes"
    private val YOUR_CHANNEL_NAME = "FIREBASEOC"

    override fun onNewToken(p0: String) {
        Log.d("nouveau token : ",p0)
        firebase.setUserToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {

        Log.d("data est dispo :",p0.data.toString())
        if(p0.notification !=null){
            val message = p0.notification!!.body.toString()
            val title = p0.notification!!.title.toString()
            Log.d("corps de notif : ", message)
            Log.d("titre de notif : ", title)

            this.showNotification(title,message);
        }


    }

    fun showNotification(title: String, message: String) {

        val notificationId: Int = Random.nextInt(0, 100) // just use a counter in some util class...

        // 1 - Create an Intent that will be shown when user will click on the Notification

        // 1 - Create an Intent that will be shown when user will click on the Notification
        val intent = Intent(this, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val checkIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        // 2 - Create a Style for the Notification

        // 2 - Create a Style for the Notification
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle(title)
        inboxStyle.addLine(message)


        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(YOUR_CHANNEL_ID,
                    YOUR_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT)
//            channel.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
            mNotificationManager.createNotificationChannel(channel)
        }
        val mBuilder = NotificationCompat.Builder(applicationContext, YOUR_CHANNEL_ID)
                .setAutoCancel(true) // clear notification after click
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_danger_foreground)
                .addAction(R.drawable.ic_clear_black_24dp, "Dismiss", pendingIntent)
                .addAction(R.drawable.ic_check_black_24dp, "Action!", checkIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(inboxStyle)

//        val intent = Intent(applicationContext, ACTIVITY_NAME::class.java)
//        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        mBuilder.setContentIntent(pi)
        mNotificationManager.notify(notificationId, mBuilder.build())
    }
}