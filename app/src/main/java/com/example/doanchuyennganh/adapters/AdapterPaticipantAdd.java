package com.example.doanchuyennganh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterPaticipantAdd extends RecyclerView.Adapter<AdapterPaticipantAdd.HolderPaticipantAdd>{
    private Context context;
    private ArrayList<ModelUser> userArrayList;
    private String groupId, myGroupRole;

    public AdapterPaticipantAdd(Context context, ArrayList<ModelUser> userArrayList, String groupId, String myGroupRole) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }





    @NonNull
    @Override
    public HolderPaticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_paticipant_add, parent, false);
        return new HolderPaticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPaticipantAdd holder, int position) {
        ModelUser modelUser = userArrayList.get(position);

        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String image = modelUser.getImage();
        String uid = modelUser.getUid();

        holder.nameTvs.setText(name);
        holder.emailTvs.setText(email);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_foreground).into(holder.avatarTvs);

        }catch (Exception e){
            holder.avatarTvs.setImageResource(R.drawable.ic_default_img_foreground);
        }

        checkIfAlreadyExits(modelUser, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String hisPreviousRole = ""+snapshot.child("role").getValue();
                            String[] options;

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Chọn quyền");
                            if(myGroupRole.equals("creator")){
                                if(hisPreviousRole.equals("admin")){
                                    options = new String[]{"Xoá quản trị viên", "Xoá thành viên"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0){
                                                removeAdmin(modelUser);
                                            }else {
                                                removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                }
                                else if(hisPreviousRole.equals("participants")){
                                    options = new String[]{"Làm quản trị viên","Xoá thành viên"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0){
                                                makeAdmin(modelUser);
                                            }else {
                                                removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                }
                            }
                            else if(myGroupRole.equals("admin")){
                                if(hisPreviousRole.equals("creator")){
                                    Toast.makeText(context, "Người tạo nhóm...", Toast.LENGTH_SHORT).show();
                                }else if(hisPreviousRole.equals("admin")){
                                    options = new String[]{"Xoá quản trị viên", "Xoá thành viên"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0){
                                                removeAdmin(modelUser);
                                            }else {
                                                removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                }
                                else  if(hisPreviousRole.equals("participants")){
                                    options = new String[]{"Làm quản trị viên","Xoá thành viên"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0){
                                                makeAdmin(modelUser);
                                            }else {
                                                removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                }
                            }
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Thêm thành viên").setMessage("Thêm thành viên này vào nhóm")
                                    .setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            addParticipant(modelUser);
                                        }
                                    }).setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private void addParticipant(ModelUser modelUser) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("uid", modelUser.getUid());
        hashMap.put("role", "participants");
        hashMap.put("timestamp", ""+timestamp);
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Thêm thành viên thành công...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void makeAdmin(ModelUser modelUser) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("role", "admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Thành viên mới này đã thành quản trị viên...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void removeAdmin(ModelUser modelUser) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    private void removeParticipant(ModelUser modelUser) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("role", "participants");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Thành viên này đã bị xoá quyền quản trị...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExits(ModelUser modelUser, HolderPaticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String hisRole = ""+snapshot.child("role").getValue();
                    holder.statusTv.setText(hisRole);
                }else {
                    holder.statusTv.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    class HolderPaticipantAdd extends RecyclerView.ViewHolder{

        private ImageView avatarTvs;
        private TextView nameTvs, emailTvs, statusTv;

        public HolderPaticipantAdd(@NonNull View itemView) {
            super(itemView);
            avatarTvs = itemView.findViewById(R.id.avatarTvs);
            nameTvs = itemView.findViewById(R.id.nameTvs);
            emailTvs = itemView.findViewById(R.id.emailTvs);
            statusTv = itemView.findViewById(R.id.statusTv);

        }

    }
}
