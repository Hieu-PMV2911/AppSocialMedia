package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbref;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE= 300;
    private static final int IMAGE_PICK_GALLERY_CODE= 400;


    String[] camerePermissions;
    String[] storagePermission;


    EditText titleEt, desciptionEt;
    ImageView imageEt;
    Button uploadBtn;

    String name, email, uid, dp;

    String editTitle, editDescription, editImage;


    Uri image_uri = null;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Thêm bài viết mới");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        camerePermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();


        titleEt = findViewById(R.id.pTitleEt);
        desciptionEt = findViewById(R.id.pDescriptionEt);
        imageEt = findViewById(R.id.pImageEt);
        uploadBtn = findViewById(R.id.pUploadBtn);

        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");

        String acton = intent.getAction();
        String type = intent.getType();
        if(Intent.ACTION_SEND.equals(acton) && type!=null){
            if("text/plain".equals(type)){
                handleSendText(intent);
            }else if(type.startsWith("image"))
            {
                handleSendImage(intent);
            }
        }


        if(isUpdateKey.equals("editPost")){
            actionBar.setTitle("Chỉnh sửa bài viết");
            uploadBtn.setText("Chỉnh sửa bài viết");
            loadPostData(editPostId);
        }
        else{
            actionBar.setTitle("Thêm mới bài viết");
            uploadBtn.setText("Đăng bài viết");
        }
        actionBar.setSubtitle(email);

        userDbref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbref.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    name = ""+ ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();
                    dp = ""+ ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        imageEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString().trim();
                String desciption = desciptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Nhập tiêu đề...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(desciption)){
                    Toast.makeText(AddPostActivity.this, "Nhập nội dung...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isUpdateKey.equals("editPost")){
//                    actionBar.setTitle("Chỉnh sửa bài viết");
                    beginUpdate(title, desciption, editPostId);
                }else{
                    uploadData(title, desciption);

                }



            }
        });


    }

    private void handleSendImage(Intent intent) {
        Uri imageURI = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageURI != null){
            image_uri = imageURI;
            imageEt.setImageURI(image_uri);
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(sharedText!=null){
            desciptionEt.setText(sharedText);
        }
    }

    private void beginUpdate(String title, String desciption, String editPostId) {

        pd.setMessage("Đang chỉnh sửa bài viết...");
        pd.show();

        if(!editImage.equals("noImage")){
            updateWasWithImage(title, desciption, editPostId);

        }else if(imageEt.getDrawable()!= null){
            updateWithNowImage(title, desciption, editPostId);

        }else{
            updateWithoutImage(title, desciption, editPostId);
        }


    }

    private void updateWithoutImage(String title, String desciption, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", desciption);
        hashMap.put("pImage", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "Đang chỉnh sửa...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateWithNowImage(String title, String desciption, String editPostId) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" +timeStamp;

        Bitmap bitmap = ((BitmapDrawable)imageEt.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()){
                    HashMap<String, Object> hashMap = new HashMap<>();

                    hashMap.put("uid", uid);
                    hashMap.put("uName", name);
                    hashMap.put("uEmail", email);
                    hashMap.put("uDp", dp);
                    hashMap.put("pTitle", title);
                    hashMap.put("pDescr", desciption);
                    hashMap.put("pImage", downloadUri);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Đang chỉnh sửa...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void updateWasWithImage(String title, String desciption, String editPostId) {

        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/" + "post_" +timeStamp;

                Bitmap bitmap = ((BitmapDrawable)imageEt.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", desciption);
                            hashMap.put("pImage", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Đang chỉnh sửa...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    editTitle =""+ds.child("pTitle").getValue();
                    editDescription =""+ds.child("pDescr").getValue();
                    editImage =""+ds.child("pImage").getValue();

                    titleEt.setText(editTitle);
                    desciptionEt.setText(editDescription);
                    if(!editImage.equals("noImage")){
                        try{
                            Picasso.get().load(editImage).into(imageEt);
                        }catch (Exception e)
                        {
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void uploadData(String title, String desciption) {
        pd.setMessage("Đang đăng bài viết...");
        pd.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" +timeStamp;

        if(imageEt.getDrawable() != null){
            Bitmap bitmap = ((BitmapDrawable)imageEt.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());

                    String dowloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("uid",uid);
                            hashMap.put("uName",name);
                            hashMap.put("uEmail",email);
                            hashMap.put("uDp",dp);
                            hashMap.put("pId",timeStamp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescr",desciption);
                            hashMap.put("pImage",dowloadUri);
                            hashMap.put("pTime",timeStamp);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Đã đăng bài", Toast.LENGTH_SHORT).show();
                                    titleEt.setText("");
                                    desciptionEt.setText("");
                                    imageEt.setImageURI(null);
                                    image_uri = null;
                                    prepareNotification(
                                            ""+timeStamp,
                                            ""+name+"Thêm mới bài viết",
                                            ""+title+"\n"+desciption,
                                            "PostNotification",
                                            "POST");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            HashMap<Object, String> hashMap = new HashMap<>();

            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",desciption);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "Đã đăng bài", Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    desciptionEt.setText("");
                    imageEt.setImageURI(null);
                    image_uri = null;

                    prepareNotification(
                            ""+timeStamp,
                            ""+name+"Thêm mới bài viết",
                            ""+title+"\n"+desciption,
                            "PostNotification",
                            "POST");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }



    private void prepareNotification(String pId, String title, String description, String notificationType,String notificationTocpic){
        String NOTIFICATION_TOPIC = "/topics/" + notificationTocpic;
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = description;
        String NOTIFICATION_TYPE = notificationType;

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJp = new JSONObject();

        try {
            notificationJo.put("notificationType", NOTIFICATION_TYPE);
            notificationJo.put("sender", uid);
            notificationJo.put("pId", pId);
            notificationJo.put("pTitle", NOTIFICATION_TITLE);
            notificationJo.put("pDescription", NOTIFICATION_MESSAGE);
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJp);



        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        
        sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddPostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAEBtdsc0:APA91bFn3IKbHY_Mt5QQ-UKZI-D2W7vzBJA0Bs--DR4VsIIHA298a9kpPh7dvn0p928aoWHrSPdb8ZC3rQjEaQTtVU2nUMctYu5qJCLacYLKq16XvwGwVFdE_Cd6I_u1PhM88qyP7IN5");

                return super.getHeaders();
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            email = user.getEmail();
            uid = user.getUid();

        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);
        menu.findItem(R.id.action_add_participan).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        return super.onCreateOptionsMenu(menu);

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
                imageEt.setImageURI(image_uri);
            }else
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                imageEt.setImageURI(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}