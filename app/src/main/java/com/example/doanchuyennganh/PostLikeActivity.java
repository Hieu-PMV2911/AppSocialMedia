package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.doanchuyennganh.adapters.AdapterUsers;
import com.example.doanchuyennganh.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import java.util.ArrayList;

public class PostLikeActivity extends AppCompatActivity {
    String postId;
    private RecyclerView recycleView;
    private  List<ModelUser> userList;
    private AdapterUsers adapterUsers;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_like);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Bài viết được thích bởi");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());

        recycleView = findViewById(R.id.recycleView);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String hisUid = "" + ds.getRef().getKey();

                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsers(String hisUid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    userList.add(modelUser);
                }
                adapterUsers = new AdapterUsers(PostLikeActivity.this, userList);
                recycleView.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}