package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanchuyennganh.adapters.AdapterComments;
import com.example.doanchuyennganh.models.ModelComments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Array;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class PostDetailActivity extends AppCompatActivity {

    String hisUid, myUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    ProgressDialog pd;

    ImageView uPictureTv, pImageTv;
    TextView uNameTv, pTileTv,pTimeTv ,pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    RecyclerView recyclerView;
    List<ModelComments> commentsList;
    AdapterComments adapterComments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Chi tiết bài viết");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");




        uPictureTv = findViewById(R.id.uPictureIv);
        pImageTv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.unameTv);
        pTileTv = findViewById(R.id.pTitleTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikeTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayoutTv);
        recyclerView = findViewById(R.id.recycerViewComments);


        commentEt = findViewById(R.id.commentsEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        actionBar.setSubtitle("Bình luận của: "+ myEmail);

        loadComments();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });
        
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LikePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pTitle = pTileTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageTv.getDrawable();
                if(bitmapDrawable == null){
                    shareTextOnly(pTitle, pDescription);
                }else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }

            }
        });

        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikeActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });


    }

    private void addToHisNotification(String hisUid, String pId, String notification){
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();

        hashMap.put("pId", pId);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);
        hashMap.put("sName", hisName);
        hashMap.put("sEmail", myEmail);
        hashMap.put("sImage", pImage);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBdy = pTitle+ "\n"+pDescription;

        Uri uri = saveImageToShare(bitmap);

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBdy);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Chia sẻ qua"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {

        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "share_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.example.doanchuyennganh.fileprovider", file);



        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBdy = pTitle+ "\n"+pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBdy);
        startActivity(Intent.createChooser(sIntent, "Share qua"));
    }


    private void loadComments() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        commentsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelComments modelComments = ds.getValue(ModelComments.class);

                    commentsList.add(modelComments);



                    adapterComments = new AdapterComments(getApplicationContext(), commentsList, myUid, postId);
                    recyclerView.setAdapter(adapterComments);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        if(hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Chỉnh sửa");
            popupMenu.getMenu().add(Menu.NONE, 0,0,"Xoá");
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id==0){
                    beginDelete();
                } else if (id == 1) {
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);

                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if(pImage.equals("noImage")){
            deleteWithoutImage();
        }else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang xoá...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, "Xoá thành công", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang xoá...");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Xoá thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)){
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("Đã thích");
                }else {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LikePost() {
        mProcessLike = true;
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;


                    }else {
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;

                        addToHisNotification(""+hisUid, ""+postId,
                                "Đã thích bài viết"
                        );

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Đang thêm bình luận...");

        String comment = commentEt.getText().toString().trim();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Bình luận đang trống... ", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hasMap = new HashMap<>();

        hasMap.put("cId", timeStamp);
        hasMap.put("comment", comment);
        hasMap.put("timestamp", timeStamp);
        hasMap.put("uid", myUid);
        hasMap.put("uEmail", myEmail);
        hasMap.put("uDp", myDp);
        hasMap.put("uName", myName);

        ref.child(timeStamp).setValue(hasMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Bình luận đang được thêm...", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommemntCount();

                addToHisNotification(""+hisUid, ""+postId,
                        "Đã bình luận bài viết"
                );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void updateCommemntCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments =""+ snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) +1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
         Query myRef = FirebaseDatabase.getInstance().getReference("Users");
         myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 for(DataSnapshot ds: snapshot.getChildren()){
                     myName = ""+ds.child("name").getValue();
                     myDp = ""+ds.child("image").getValue();

                     try{
                         Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img_foreground).into(cAvatarIv);
                     }catch (Exception e){
                         Picasso.get().load(R.drawable.ic_default_img_foreground).into(cAvatarIv);
                     }

                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pTileTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+ "Lượt thích");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount+ " Bình luận");

                    uNameTv.setText(hisName);

                    if(pImage.equals("noImage")){
                        pImageTv.setVisibility(View.GONE);
                    }else {
                        pImageTv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageTv);
                        }catch (Exception e)
                        {

                        }
                    }

                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img_foreground).into(uPictureTv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_foreground).into(uPictureTv);
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            myEmail = user.getEmail();
            myUid = user.getUid();

        }else {
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}