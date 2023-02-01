package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanchuyennganh.adapters.AdapterGroupChat;
import com.example.doanchuyennganh.adapters.AdapterPaticipantAdd;
import com.example.doanchuyennganh.models.ModelGroupChats;
import com.example.doanchuyennganh.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String myGroupRole = "";
    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    ImageView groupIconTv;

    TextView createByTv, pDescriptionTv, editGroupTv, addParticipantTv,leaveGroupTv, participantTotal;
    RecyclerView participantRv;

    private ArrayList<ModelUser> modelUsers;
    private AdapterPaticipantAdd adapterPaticipantAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconTv = findViewById(R.id.groupIconTv);
        createByTv = findViewById(R.id.createByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantTotal = findViewById(R.id.participantTotal);
        participantRv = findViewById(R.id.participantRv);

        firebaseAuth = FirebaseAuth.getInstance();


        groupId = getIntent().getStringExtra("groupId");

        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupPaticipantActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle = "";
                String dialogDescription="";
                String positiveButtonTitle="";
                if(myGroupRole.equals("creator")){
                    dialogTitle="Xoá Nhóm";
                    dialogDescription = "Bạn có chắc xoá nhóm";
                    positiveButtonTitle = "Xoá";
                }else{
                    dialogTitle="Rời Nhóm";
                    dialogDescription = "Bạn có chắc rời nhóm";
                    positiveButtonTitle = "Rời";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle).setMessage(dialogDescription).setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(myGroupRole.equals("creator")){
                            deleteGroup();
                        }else {
                            leaveGroup();
                        }
                    }
                }).setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Rời nhóm thành công...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(GroupInfoActivity.this, "Xoá nhóm thành công...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail()+"{"+myGroupRole+"}");

                            if(myGroupRole.equals("participants")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Rời Nhóm");
                            }else if(myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Rời Nhóm");
                            }else if(myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Xoá Nhóm");
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        modelUsers = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelUsers.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds: snapshot.getChildren()){
                                ModelUser user = ds.getValue(ModelUser.class);

                                modelUsers.add(user);
                            }
                            adapterPaticipantAdd = new AdapterPaticipantAdd(GroupInfoActivity.this, modelUsers, groupId, myGroupRole);
                            participantRv.setAdapter(adapterPaticipantAdd);
                            participantTotal.setText("Thành Viên Trong Nhóm ("+modelUsers.size()+")");
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

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createBy = ""+ds.child("createBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    actionBar.setTitle(groupTitle);
                    pDescriptionTv.setText(groupDescription);
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));

                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                    loadCreatorInfo(dateTime, createBy);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(groupIconTv);
                    }catch (Exception e){
                        groupIconTv.setImageResource(R.drawable.ic_group_primary);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreatorInfo(String dateTime, String createBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    createByTv.setText("Được tạo bới "+name+" vào "+ dateTime);
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
}