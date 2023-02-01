package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.doanchuyennganh.adapters.AdapterUsers;
import com.example.doanchuyennganh.models.ModelUser;
import com.example.doanchuyennganh.notifications.Date;
import com.example.doanchuyennganh.notifications.Responsive;
import com.example.doanchuyennganh.notifications.Sender;
import com.example.doanchuyennganh.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.doanchuyennganh.adapters.AdapterChat;
import com.example.doanchuyennganh.models.ModelsChat;

import org.json.JSONException;
import org.json.JSONObject;



public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileTv, blockTv, callBtn, callVideoBtn;
    TextView nameTv, userStatusTv;
    EditText messageT;
    ImageButton sendBtn, attackBtn;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRefdb;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelsChat> chatList;
    List<ModelUser> user;
    AdapterChat adapterChat;

    String hisUID;
    String myUid;
    String hisImage;

    boolean isBlocked = false;

    Uri image_uri = null;

    private RequestQueue requestQueue;
    private boolean notifys = false;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE= 300;
    private static final int IMAGE_PICK_GALLERY_CODE= 400;


    String[] camerePermissions;
    String[] storagePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.tolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recycleView);
        profileTv = findViewById(R.id.avartarIv);
        nameTv = findViewById(R.id.nameTV);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageT = findViewById(R.id.messageIv);
        sendBtn = findViewById(R.id.sendBtn);
        attackBtn = findViewById(R.id.attackBtn);
        blockTv = findViewById(R.id.blockTv);
        callBtn = findViewById(R.id.callBtn);
//        callVideoBtn = findViewById(R.id.callvideoBtn);


        camerePermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        requestQueue = Volley.newRequestQueue(getApplicationContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRefdb = firebaseDatabase.getReference("Users");

        Intent intent = getIntent();
        hisUID = intent.getStringExtra("hisUid");

        Query userQuery = userRefdb.orderByChild("uid").equalTo(hisUID);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name =""+ ds.child("name").getValue();
                    hisImage =""+ ds.child("image").getValue();
                    String typeStutus =""+ ds.child("typeTo").getValue();
                    if(typeStutus.equals(myUid)){
                        userStatusTv.setText("Đang nhập...");
                    }else{
                        String onlineStatus = ""+ ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals("Online")){
                            userStatusTv.setText(onlineStatus);
                        }else{
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));

                            String dateTime = DateFormat.format(" hh:mm aa dd/MM/yyyy", cal).toString();
                            userStatusTv.setText("Hoạt động vào "+dateTime);
                        }
                    }

                    nameTv.setText(name);

                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_img_chat).into(profileTv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_img_chat).into(profileTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifys = true;
                String message = messageT.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn...", Toast.LENGTH_SHORT).show();
                }else {
                    sendMessage(message);
                }
                messageT.setText("");
            }
        });

        messageT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.toString().trim().length() == 0){
                    checkTypeStatus("onOne");
                }else{
                    checkTypeStatus(hisUID);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        attackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBlocked){
                    unBlockUser();
                }else {
                    blockUser();
                }
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiaAudioMeeting(chatList);
                Toast.makeText(ChatActivity.this, "Bắt đầu cuộc gọi...", Toast.LENGTH_SHORT).show();
            }
        });


        checkIsBlocked();

        readMessage();

        seenMessage();
    }

    private void initiaAudioMeeting(List<ModelsChat> chatList) {
        Intent intent = new Intent(getApplicationContext(), CallVideoActivity.class);
        intent.putExtra("uid", hisUID);
        intent.putExtra("type","video");
        startActivity(intent);
        finish();
    }


    private void checkIsBlocked() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.exists()){
                        blockTv.setImageResource(R.drawable.ic_blocked_red);
                        isBlocked = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void blockUser() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ChatActivity.this, "Chặn người dùng thành công...", Toast.LENGTH_SHORT).show();
                blockTv.setImageResource(R.drawable.ic_blocked_red);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Thất bại...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.exists()){
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ChatActivity.this, "Huỷ chặn thành công...", Toast.LENGTH_SHORT).show();
                                blockTv.setImageResource(R.drawable.ic_unblocked_red);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatActivity.this, "Thất bại...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Thư Viện"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hình ảnh từ");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }

                }if(i ==1){
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();

    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {
        ContentValues cv = new  ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Chọn tiêu đề");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Chọn nội dung");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==  (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private  void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                ==  (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==  (PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }

    private  void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, camerePermissions, CAMERA_REQUEST_CODE);
    }



    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelsChat chat = ds.getValue(ModelsChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUID)){
                        HashMap<String, Object> hashSeenHashmap = new HashMap<>();
                        hashSeenHashmap.put("isSeen", true);
                        ds.getRef().updateChildren(hashSeenHashmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        notifys = true;


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi hình ảnh...");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                progressDialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()){
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", myUid);
                    hashMap.put("receiver", hisUID);
                    hashMap.put("message", downloadUri);
                    hashMap.put("timestamp", timeStamp);
                    hashMap.put("type", "image");
                    hashMap.put("isSeen", false);

                    databaseReference.child("Chats").push().setValue(hashMap);

                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);

                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if(notifys){
                                senNotification(hisUID, user.getName(), "Gửi bạn 1 hình ảnh...");
                            }
                            notifys = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUID);
                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef1.child("id").setValue(hisUID);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUID).child(myUid);

                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef2.child("id").setValue(myUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });

    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelsChat chat = ds.getValue(ModelsChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUID) ||
                            chat.getReceiver().equals(hisUID) && chat.getSender().equals(myUid)
                    ){
                            chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver", hisUID);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");

        databaseReference.child("Chats").push().setValue(hashMap);

        String msg = message;
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if(notifys){
                    senNotification(hisUID, user.getName(), message);
                }
                notifys = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUID);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUID).child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void senNotification(String hisUID, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Date date = new Date(""+myUid,
                            ""+name + ": " +message,
                            "Tin nhắn mới", ""+hisUID,
                            "ChatNotification",R.drawable.ic_default_img_foreground);

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
                                headers.put("Authorization", "key=AAAAEBtdsc0:APA91bFn3IKbHY_Mt5QQ-UKZI-D2W7vzBJA0Bs--DR4VsIIHA298a9kpPh7dvn0p928aoWHrSPdb8ZC3rQjEaQTtVU2nUMctYu5qJCLacYLKq16XvwGwVFdE_Cd6I_u1PhM88qyP7IN5");

                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
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

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
//            mProfileTv.setText(user.getEmail());
            myUid = user.getUid();
        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        dbref.updateChildren(hashMap);
    }

    private void checkTypeStatus(String type){
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typeTo", type);

        dbref.updateChildren(hashMap);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);
        menu.findItem(R.id.action_add_participan).setVisible(false);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("Online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypeStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("Online");
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, "Camera và thư viện cần được cấp quyên kết nối...", Toast.LENGTH_SHORT).show();

                    }
                }else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Thư viện cần được cấp quyên kết nối...", Toast.LENGTH_SHORT).show();

                    }
                }else {

                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}

