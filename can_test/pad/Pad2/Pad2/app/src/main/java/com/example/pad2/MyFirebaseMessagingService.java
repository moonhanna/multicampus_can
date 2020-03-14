package com.example.pad2;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;

import msg.Msg;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG = "===";
    String msg, title;
    NotificationManagerCompat notificationManager;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {


        title = remoteMessage.getNotification().getTitle();
        msg = remoteMessage.getNotification().getBody();

        Log.d(TAG, "title : " + title + " msg : " + msg);

        String channelId = "channel";
        String channelName = "Channel_name";
        int importance = NotificationManager.IMPORTANCE_LOW;


        notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setVibrate(new long[]{1, 1000});
        new Sender(new Msg("Admin",msg,title)).start();

        notificationManager.notify(0, mBuilder.build());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.d("===", "getting new token fail!");
                    return;
                }

                String token = task.getResult().getToken();

                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MyFirebaseMessagingService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    class Sender extends Thread{
        Msg msg;
        public Sender(Msg msg) {
            this.msg = msg;
        }
        @Override
        public void run() {
            if( msg.getTid()==null||msg.getTid().equals("/") || msg.getTid().equals("")){
                Collection<ObjectOutputStream>
                        cols = MainActivity.maps.values();
                Iterator<ObjectOutputStream>
                        its = cols.iterator();
                while(its.hasNext()) {
                    try {
                        its.next().writeObject(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            else{
                try {
                    MainActivity.maps.get(msg.getTid()).writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}