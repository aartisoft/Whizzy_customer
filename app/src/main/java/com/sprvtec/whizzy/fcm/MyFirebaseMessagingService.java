package com.sprvtec.whizzy.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.ui.SplashActivity;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.GlobalApplication;

import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by Sowjanya on 12/5/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody() + " click action " + remoteMessage.getNotification().getClickAction());
        Map<String, String> data = remoteMessage.getData();
        Log.e("data is", data + "");

        sendNotification1(remoteMessage.getNotification().getBody());

    }



    //This method is only generating push notification
    //It is same as we did in earlier posts


    private void sendNotification1(String messageBody) {

        Intent notificationIntent = new Intent(this, SplashActivity.class);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "Whizzy_notifications")
                .setSmallIcon(R.drawable.app_icon_transperent)
                .setContentTitle("Whizzy")
                .setContentText(messageBody).setColor(getResources().getColor(R.color.app_blue))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
//        notificationBuilder.setSound(Uri.parse("android.resource://"
//                + getPackageName() + "/" + R.raw.notif));
//        notificationID = generateRandom();
        int notificationID = 123;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel mChannel = new NotificationChannel("Whizzy_notifications",
                    getApplicationContext().getString(R.string.whizzy),
                    NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
//                mChannel.setDescription(msg);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setSound(defaultSoundUri, attributes); // This is IMPORTANT


            if (notificationManager != null)
                notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(notificationID, notificationBuilder.build());


    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
        GlobalApplication.fcmToken=s;
        Intent msgrcv = new Intent(Constants.BROADCAST_FCM_TOKEN);
        LocalBroadcastManager.getInstance(this).sendBroadcast(msgrcv);

    }
}