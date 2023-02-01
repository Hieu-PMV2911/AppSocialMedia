package com.example.doanchuyennganh.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.AddPostActivity;
import com.example.doanchuyennganh.PostDetailActivity;
import com.example.doanchuyennganh.PostLikeActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.ThereProfileActivity;
import com.example.doanchuyennganh.models.ModelPosts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    public AdapterPosts(Context context, List<ModelPosts> postsList) {
        this.context = context;
        this.postsList = postsList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    Context context;
    List<ModelPosts> postsList;
    String myUid;

    private DatabaseReference likeRef;
    private DatabaseReference postRef;

    boolean mProcessLike = false;


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rows_post, parent, false);

        return new MyHolder(view);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        String uid = postsList.get(position).getUid();
        String uEmail = postsList.get(position).getuEmail();
        String uName = postsList.get(position).getuName();
        String uDp = postsList.get(position).getuDp();
        String pId = postsList.get(position).getpId();
        String pTitle = postsList.get(position).getpTitle();
        String pDescription = postsList.get(position).getpDescr();
        String pImage = postsList.get(position).getpImage();
        String pTimeStamp = postsList.get(position).getpTime();
        String pLikes = postsList.get(position).getpLikes();
        String pComments = postsList.get(position).getpComments();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescription.setText(pDescription);
        holder.pLikesTv.setText(pLikes + " Lượt thích");
        holder.pCommentsTv.setText(pComments + " Bình luận");

        setLikes(holder, pId);

        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img_foreground).into(holder.uPictureTv);
        }catch (Exception e)
        {

        }

        if(pImage.equals("noImage")){
                holder.pImageTv.setVisibility(View.GONE);
        }else {
            holder.pImageTv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.pImageTv);
            }catch (Exception e)
            {

            }
        }


        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pLikes = Integer.parseInt(postsList.get(position).getpLikes());
                mProcessLike = true;

                String postIde = postsList.get(position).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postIde).hasChild(myUid)){
                                postRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likeRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }else {
                                postRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likeRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;

                                addToHisNotification(""+uid, ""+pId,
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
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);

            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.pImageTv.getDrawable();
                if(bitmapDrawable == null){
                    shareTextOnly(pTitle, pDescription);
                }else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }

            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostLikeActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
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
        context.startActivity(Intent.createChooser(sIntent, "Chia sẻ qua"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {

        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "share_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.doanchuyennganh.fileprovider", file);



        } catch (Exception e) {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBdy = pTitle+ "\n"+pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBdy);
        context.startActivity(Intent.createChooser(sIntent, "Share qua"));
    }

    private void setLikes(MyHolder holder, String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("Đã thích");
                }else {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Chỉnh sửa");
            popupMenu.getMenu().add(Menu.NONE, 0,0,"Xoá");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Chi tiết");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id==0){
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                }else if(id==2){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if(pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else {
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Đang xoá...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Đang xoá...");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        ImageView uPictureTv, pImageTv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescription, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View view){
            super(view);

            uPictureTv = view.findViewById(R.id.uPictureIv);
            pImageTv = view.findViewById(R.id.pImageIv);
            uNameTv = view.findViewById(R.id.unameTv);
            pTimeTv = view.findViewById(R.id.pTimeTv);
            pTitleTv = view.findViewById(R.id.pTitleTv);
            pDescription = view.findViewById(R.id.pDescriptionTv);
            pLikesTv = view.findViewById(R.id.pLikeTv);
            pCommentsTv = view.findViewById(R.id.pCommentsTv);
            moreBtn = view.findViewById(R.id.moreBtn);
            likeBtn = view.findViewById(R.id.likeBtn);
            commentBtn = view.findViewById(R.id.commentBtn);
            shareBtn = view.findViewById(R.id.shareBtn);
            profileLayout = view.findViewById(R.id.profileLayoutTv);

        }
    }


}
