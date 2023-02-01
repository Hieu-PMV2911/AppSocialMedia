package com.example.doanchuyennganh.notifications;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.doanchuyennganh.CallEndActivity;
import com.example.doanchuyennganh.CallVideoActivity;
import com.example.doanchuyennganh.ChatActivity;
import com.example.doanchuyennganh.PostDetailActivity;
import com.example.doanchuyennganh.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class FirebaseMessaging extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID = "admin_channel";


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        String body = remoteMessage.getNotification().getBody();
        String type = remoteMessage.getData().get(Contants.REMOTE_MSG_TYPE);

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser =  sp.getString("Current_USERID", "None");

        String notificationType = remoteMessage.getData().get("notificationType");
        if(notificationType.equals("PostNotification")){
            String sender= remoteMessage.getData().get("sender");
            String pId= remoteMessage.getData().get("pId");
            String pTitle= remoteMessage.getData().get("pTitle");
            String pDescription= remoteMessage.getData().get("pDescription");

            if(!sender.equals(savedCurrentUser)){
                showPostNotification(""+pId, ""+pTitle, ""+pDescription);
            }

        }else if(notificationType.equals("ChatNotification")) {

            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

            // can fix
            if(fUser != null && sent.equals(fUser.getUid())){
                if(savedCurrentUser.equals(user)){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        sendAndAboveNotification(remoteMessage);
                    }else{
                        sendNomalNotification(remoteMessage);
                    }
                }
            }
        }else if(type.equals(Contants.REMOTE_MSG_INVITATION)){
            Intent intent = new Intent(getApplicationContext(), CallEndActivity.class);
            intent.putExtra(
                    Contants.REMOTE_MSG_MEETING_TYPE,
                    remoteMessage.getData().get(Contants.REMOTE_MSG_MEETING_TYPE)
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }


    private void showPostNotification(String pId, String pTitle, String pDes) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            setupPostNotificationChannel(notificationManager);
        }

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId",pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo1);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ""+ADMIN_CHANNEL_ID).setSmallIcon(R.drawable.logo1)
                .setLargeIcon(largeIcon).setContentTitle(pTitle).setContentText(pDes).setSound(notificationSoundUri).setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notificationBuilder.build());






    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence charSequence = "Thông báo mới";
        String channelDescr = "Thiết bị đến thiết bị đăng thông báo";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, charSequence, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescr);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);

        if(notificationManager!=null){
            notificationManager.createNotificationChannel(adminChannel);
        }


    }

    @TargetApi(Build.VERSION_CODES.O)
    private void sendNomalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri deSoudfUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(Integer.parseInt(icon)).setContentText(body).setContentTitle(title).setAutoCancel(true).setSound(deSoudfUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if(i>0){
            j=1;
        }
        notificationManager.notify(j,builder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void sendAndAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri deSoudfUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getNotifications(title, body, pendingIntent, deSoudfUri, icon);

        int j = 0;
        if(i>0){
            j=1;
        }
        notification1.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null ){
            updateToken(token);
        }
    }

    private void updateToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);

        ref.child(user.getUid()).setValue(token1);

    }
}
