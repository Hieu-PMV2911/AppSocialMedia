package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class SettingActivity extends AppCompatActivity {

    SwitchCompat postWitch;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TOPIC_POST_NOTIFICATION = "POST";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Cài đặt");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        postWitch = findViewById(R.id.postSwitch);

        sp = getSharedPreferences("Notifacation_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+TOPIC_POST_NOTIFICATION, false);
        if(isPostEnabled){
            postWitch.setChecked(true);
        }else{
            postWitch.setChecked(false);
        }

        postWitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, b);
                editor.apply();


                if(b){
                    subscribePostNotification();
                }else {
                    unsubcribePostNotification();
                }
            }
        });
    }

    private void unsubcribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "Bạn đã tắt thông báo của bài viết";
                if(!task.isSuccessful()){
                    msg = "Huỷ đăng thất bại";
                }
                Toast.makeText(SettingActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "Bạn đã bật thông báo của bài viết";
                if(!task.isSuccessful()){
                    msg = "Đăng thất bại";
                }
                Toast.makeText(SettingActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}