package com.example.doanchuyennganh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.AddStoryActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.StoryActivity;
import com.example.doanchuyennganh.models.ModelUser;
import com.example.doanchuyennganh.models.ModelsStory;
//import com.example.socialnetword.AddStoryActivity;
//import com.example.socialnetword.Model.Story;
//import com.example.socialnetword.Model.Users;
//
//import com.example.socialnetword.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context mContext;
    private List<ModelsStory> mStory;

    public StoryAdapter(Context mContext, List<ModelsStory> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        if (i == 0){
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item,parent,false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ModelsStory story = mStory.get(i);
        userInfo(viewHolder,story.getUserid(),i);
        if (viewHolder.getAdapterPosition() != 0){
            seenStory(viewHolder,story.getUserid());

        }
        if (viewHolder.getAdapterPosition() == 0){
            myStory(viewHolder.addstory_text, viewHolder.story_add, false);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.getAdapterPosition() == 0){
                    myStory(viewHolder.addstory_text, viewHolder.story_add,true);
                }else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid",story.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView story_photo ,story_add ,story_photo_seen;
        public TextView story_username , addstory_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_add = itemView.findViewById(R.id.story_add);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            addstory_text = itemView.findViewById(R.id.addstory_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0){
            return 0;
        }
        return 1;
    }

    private void userInfo(ViewHolder viewHolder , String userid , int pos){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);

                try{
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_default_img_foreground).into(viewHolder.story_photo);
                }catch (Exception e){
                    Picasso.get().load(R.drawable.ic_default_img_foreground).into(viewHolder.story_photo);
                }
                if (pos != 0){
                    try {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_default_img_foreground).into(viewHolder.story_photo_seen);
                        viewHolder.story_username.setText(user.getName());
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_foreground).into(viewHolder.story_photo_seen);
                        viewHolder.story_username.setText(user.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void myStory(TextView textView, ImageView imageView, boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelsStory story = snapshot.getValue(ModelsStory.class);
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        count++;
                    }
                }
                if (click){
                    if (count>0){
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Xem ảnh",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(mContext, StoryActivity.class);
                                        intent.putExtra("userid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        mContext.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Đăng ảnh",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                                        mContext.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }else {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);
                    }
                }else {
                    if (count>0){
                        textView.setText("Ảnh của tôi");
                        imageView.setVisibility(View.GONE);
                    }else {
                        textView.setText("Đăng ảnh");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void seenStory(ViewHolder viewHolder ,String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (!snapshot.child("lượt xem")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() < snapshot.getValue(ModelsStory.class).getTimeend()){
                        i++;
                    }
                }
                if (i>0){
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                }else {
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}