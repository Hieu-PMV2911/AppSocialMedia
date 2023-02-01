package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.doanchuyennganh.adapters.AdapterPosts;
import com.example.doanchuyennganh.adapters.AdapterUsers;
import com.example.doanchuyennganh.models.ModelPosts;
import com.example.doanchuyennganh.models.ModelUser;
import com.example.doanchuyennganh.network.ApiClient;
import com.example.doanchuyennganh.network.ApiService;
import com.example.doanchuyennganh.notifications.Contants;
import com.example.doanchuyennganh.notifications.Date;
import com.example.doanchuyennganh.notifications.FirebaseMessaging;
import com.example.doanchuyennganh.notifications.Sender;
import com.example.doanchuyennganh.notifications.Token;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallVideoActivity extends AppCompatActivity {
    ImageView imageTV, videoTv;
    TextView nameTv;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRefdb;
    String hisUrl, hisProf, hisName, hisToken, hisUID, senderUid;
    FloatingActionButton endCallVideoTv;
    ConstraintLayout constraintL;
    private PreferenceManager preferenceManager ;
    private String inviterToken = null;
    private String mettingRoom = null;
    private String meetingType=null;

    List<ModelUser> userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_video);

        try{
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom("hieupmv")
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .setAudioOnly(false)
                    .setWelcomePageEnabled(false)
                    .build();

            JitsiMeetActivity.launch(this, options);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }

    }



}