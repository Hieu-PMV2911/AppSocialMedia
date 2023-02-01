package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanchuyennganh.adapters.AdapterGroupChat;
import com.example.doanchuyennganh.models.ModelGroupChats;
import com.example.doanchuyennganh.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;

public class GroupChatsActivity extends AppCompatActivity {
    private String groupId,groupTitle, myGroupRole="";
    private Toolbar toolbar;
    private ImageView groupIconTv, callGroup;
    private TextView groupTitleTv;
    private ImageButton attackBtn, sendBtn;
    private EditText messageEt;
    private RecyclerView chatRv;
    private ArrayList<ModelGroupChats> groupChatsList;
    private AdapterGroupChat adapterGroupChat;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE= 300;
    private static final int IMAGE_PICK_GALLERY_CODE= 400;

    String[] cameraPermissions;
    String[] storagePermission;

    Uri image_uri = null;


    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chats);


        groupIconTv = findViewById(R.id.groupIconTv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        attackBtn = findViewById(R.id.attackBtn);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatRv = findViewById(R.id.chatRv);
        callGroup = findViewById(R.id.callGroup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessage();
        loadMyGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatsActivity.this, "Vui lòng nhập tin nhắn... ", Toast.LENGTH_SHORT).show();
                    
                }else{
                    sendMessage(message);
                }
            }
        });
        attackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageImportDialog();
            }
        });

        callGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callListGroup();
            }
        });
    }

    private void callListGroup() {
//        Intent intent = new Intent(GroupChatsActivity.this, GroupCallActivity.class);
//        startActivity(intent);
//        finish();
        loadCallGroup();

    }

    private void loadCallGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            groupTitle = ""+ds.child("groupTitle").getValue();
                            try{
                                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                        .setServerURL(new URL("https://meet.jit.si"))
                                        .setRoom("Group"+groupId)
                                        .setAudioMuted(false)
                                        .setVideoMuted(false)
                                        .setAudioOnly(false)
                                        .setWelcomePageEnabled(false)
                                        .build();

                                JitsiMeetActivity.launch(GroupChatsActivity.this,options);
                            }catch (MalformedURLException e){
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showImageImportDialog() {
        String[] options = {"Camera", "Thư viện"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hình ảnh từ").setItems(options, new DialogInterface.OnClickListener() {
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
        }).show();

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
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }


    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            invalidateMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessage() {
        groupChatsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatsList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelGroupChats model = ds.getValue(ModelGroupChats.class);
                    groupChatsList.add(model);
                }
                adapterGroupChat = new AdapterGroupChat(GroupChatsActivity.this, groupChatsList);
                chatRv.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",""+firebaseAuth.getUid());
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type",""+"text");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                messageEt.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createBy = ""+ds.child("createBy").getValue();

                    groupTitleTv.setText(groupTitle);

                    try{
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconTv);
                    }catch (Exception e){
                        groupIconTv.setImageResource(R.drawable.ic_group_white);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_setting).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);

        if(myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            menu.findItem(R.id.action_add_participan).setVisible(true);
        }else {
            menu.findItem(R.id.action_add_participan).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_add_participan){
            Intent intent = new Intent(this, GroupPaticipantActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }else
        if(id==R.id.action_groupInfo){
            Intent intent = new Intent(this, GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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

    private void sendImageMessage(Uri image_uri) throws IOException {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi hình ảnh...");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                Uri downloadUri = uriTask.getResult();
                if(uriTask.isSuccessful()){
                    String timestamp = ""+System.currentTimeMillis();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender",""+firebaseAuth.getUid());
                    hashMap.put("message",""+downloadUri);
                    hashMap.put("timestamp",""+timestamp);
                    hashMap.put("type",""+"image");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                    ref.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            messageEt.setText("");
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}