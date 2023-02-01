package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.doanchuyennganh.models.ModelUser;
import com.example.doanchuyennganh.models.VCModel;
import com.example.doanchuyennganh.notifications.Date;
import com.example.doanchuyennganh.notifications.Sender;
import com.example.doanchuyennganh.notifications.Token;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CallEndActivity extends AppCompatActivity {
    ImageView imageTV, videoTv;
    TextView nameTv;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRefdb;
    String hisUrl, hisProf, hisName, hisToken, hisUID, senderUid;
    FloatingActionButton endCallVideoTv, acceptCallVideoTv;
    VCModel mod = new VCModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_end);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(hisUID);
        imageTV = findViewById(R.id.imageTV);
        videoTv = findViewById(R.id.videoTv);
        endCallVideoTv = findViewById(R.id.endCallVideoTv);
        nameTv = findViewById(R.id.nameTv);
        acceptCallVideoTv = findViewById(R.id.acceptCallVideoTv);

        firebaseAuth= FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRefdb = firebaseDatabase.getReference("Users");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        senderUid = user.getUid();

        Bundle bundle = getIntent().getExtras();

        if(bundle !=null){
            senderUid = bundle.getString("uid");

        }else{
            Toast.makeText(this, "Không gọi được", Toast.LENGTH_SHORT).show();
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    hisName = snapshot.child("name").getValue().toString();
//                    hisName = snapshot.child("name").getValue().toString();
//                    hisName = snapshot.child("name").getValue().toString();

                    nameTv.setText(hisName);
                    try {
                        Picasso.get().load(hisUrl).placeholder(R.drawable.ic_default_img_foreground).into(imageTV);
                    }catch (Exception e){
                        imageTV.setImageResource(R.drawable.ic_default_img_foreground);
                    }


                }else {
                    Toast.makeText(CallEndActivity.this, "Không thể gọi...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        acceptCallVideoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response = "yes";
                sendResponse(response);
            }
        });
        endCallVideoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response = "no";
                sendResponse(response);

                Intent intent = new Intent(CallEndActivity.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });



//        Query userQuery = userRefdb.orderByChild("uid").equalTo(hisUID);
//        userRefdb.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot ds: snapshot.getChildren()){
//                    String name =""+ ds.child("name").getValue();
//                    String hisImage =""+ ds.child("image").getValue();
//
//                    nameTv.setText(name);
//                    try{
//                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_foreground).into(imageTV);
//                    }catch (Exception e){
//                        Picasso.get().load(R.drawable.ic_default_img_foreground).into(imageTV);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
    }

    private void sendResponse(String response) {
        if(response.equals("yes")){
            mod.setKey(hisUID);
            mod.getResponse(response);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(senderUid).child(hisUID);
            ref.child("uid").setValue(mod);
            joinmeeting();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ref.removeValue();
                }
            },1000);

        }else if(response.equals("no")){

        }
    }


    private void joinmeeting() {
        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(hisName+senderUid)
                    .setWelcomePageEnabled(false)
                    .build();

            JitsiMeetActivity.launch(CallEndActivity.this, options);
            finish();
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    private void senNotification(String hisUID) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Date date = new Date(""+hisUID,
                            ""+hisName,
                            "v", ""+hisUID,
                            "CallNotification",R.drawable.ic_default_img_foreground);

                    Sender sender = new Sender(date, token.getToken());
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("Content-Type","application/json");
                                headers.put("Authorization", "key=AAAAEBtdsc0:APA91bFn3lKbHY_Mt5QQ-UKZI-D2W7vzBJA0Bs--DR4VsllHA298a9kpPh7dvn0p928aoWHrSPdb8ZC3rQjEaQTtVU2nUMctYu5qJCLacYLKq16XvwGwVFdE_Cd6l_u1PhM88qyP7lN5");

                                return headers;
                            }
                        };
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}