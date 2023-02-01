package com.example.doanchuyennganh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.doanchuyennganh.models.ModelsChat;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private  static  final  int MSG_TYPE_LEFT = 0;
    private  static  final  int MSG_TYPE_RIGHT = 1;

    public AdapterChat(Context context, List<ModelsChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    Context context;
    List<ModelsChat> chatList;
    String imageUrl;

    FirebaseUser firebaseUser;



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent, false);
            return new MyHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent, false);
            return new MyHolder(view);
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout;
        public MyHolder(View view){
            super(view);

            profileIv = view.findViewById(R.id.avartarIv);
            messageTv = view.findViewById(R.id.messageTv);
            timeTv = view.findViewById(R.id.timeIv);
            isSeenTv = view.findViewById(R.id.isSeenTv);
            messageLayout = view.findViewById(R.id.messageLayout);
            messageIv = view.findViewById(R.id.messageIv);

        }
    }
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder,final int i) {
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimestamp();
        String typr = chatList.get(i).getType();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if(typr.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);

        }else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.ic_imagechat_black).into(holder.messageIv);

        }


        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);

        try {
            if(!imageUrl.isEmpty()){
                Picasso.get().load(imageUrl).into(holder.profileIv);
            }else{
//                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_img_chat).into(holder.profileIv);
                holder.profileIv.setImageResource(R.drawable.ic_img_chat);
            }
//            Picasso.get().load(imageUrl).into(holder.profileIv);
        }catch (Exception e){
            
        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xoá");
                builder.setMessage("Bạn có chắc xoá tin nhắn này?");


                builder.setPositiveButton("Huỷ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int is) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setNegativeButton("Xoá", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int is) {
                        deleteMessage(holder.getAdapterPosition());
                    }
                });
                builder.create().show();

            }
        });


        if(i == chatList.size()-1){
            if(chatList.get(i).isSeen()){
                holder.isSeenTv.setText("Đã xem");
            }else {
                holder.isSeenTv.setText("Đã gửi");
            }
        }else {
                holder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int ii) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp = chatList.get(ii).getTimestamp();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbref.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUID)){

//                        ds.getRef().removeValue();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "Tin nhắn đã được xoá...");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Tin nhắn đã đươc xoá...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "Bạn không thể xoá...", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return  MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }



}
