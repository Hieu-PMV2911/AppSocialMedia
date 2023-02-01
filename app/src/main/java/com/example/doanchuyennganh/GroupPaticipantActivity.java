package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.doanchuyennganh.adapters.AdapterPaticipantAdd;
import com.example.doanchuyennganh.models.ModelGroupChat;
import com.example.doanchuyennganh.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupPaticipantActivity extends AppCompatActivity {
    private RecyclerView usersRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole;
    private ArrayList<ModelUser> modelUsers;
    private AdapterPaticipantAdd adapterPaticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_paticipant);
        firebaseAuth = FirebaseAuth.getInstance();
        actionBar = getSupportActionBar();
        actionBar.setTitle("Thêm thành viên");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        usersRv = findViewById(R.id.userRv);
        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

    }

    private void getAllUser() {
        modelUsers = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelUsers.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);

                    if(!firebaseAuth.getUid().equals(user.getUid())){
                        modelUsers.add(user);
                    }
                }
                adapterPaticipantAdd = new AdapterPaticipantAdd(GroupPaticipantActivity.this, modelUsers,"" +groupId,"" +myGroupRole);
                usersRv.setAdapter(adapterPaticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle =""+ds.child("groupTitle").getValue();
                    String groupDescription =""+ds.child("groupDescription").getValue();
                    String groupIcon =""+ds.child("groupIcon").getValue();
                    String createBy =""+ds.child("createBy").getValue();
                    String timestamp =""+ds.child("timestamp").getValue();

                    actionBar.setTitle("Thêm thành viên");

                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        myGroupRole = ""+snapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle+"("+myGroupRole+")");

                                        getAllUser();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected void onStart(){
//        checkUserStatus();
        super.onStart();
    }
}