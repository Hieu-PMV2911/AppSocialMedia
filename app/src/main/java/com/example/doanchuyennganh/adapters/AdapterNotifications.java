package com.example.doanchuyennganh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.PostDetailActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.models.ModelsNotifications;
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

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.HolderNotification>{

    private Context context;
    private ArrayList<ModelsNotifications> notificationsArrayList;
    private FirebaseAuth firebaseAuth;


    public AdapterNotifications(Context context, ArrayList<ModelsNotifications> notificationsArrayList) {
        this.context = context;
        this.notificationsArrayList = notificationsArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);

        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position) {
        ModelsNotifications model = notificationsArrayList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        String timeStamp = model.getTimestamp();
        String senderUid = model.getsUid();
        String pId = model.getpId();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    String image = ""+ds.child("image").getValue();
                    String email = ""+ds.child("email").getValue();


                    model.setsName(name);
                    model.setsEmail(email);
                    model.setsImage(image);

                    holder.nameTv.setText(name);

                    try{
                        Picasso.get().load(image).placeholder(R.drawable.ic_default_img_foreground).into(holder.avatarIv);
                    }catch (Exception e){
                        holder.avatarIv.setImageResource(R.drawable.ic_default_img_foreground);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xoá");
                builder.setMessage("Bạn có chắc xoá thông báo này");
                builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timeStamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Đã xoá thông báo...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsArrayList.size();
    }


    class HolderNotification extends RecyclerView.ViewHolder{

        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avartarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
