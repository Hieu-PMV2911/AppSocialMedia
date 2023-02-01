package com.example.doanchuyennganh.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.models.ModelGroupChats;
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

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChats>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChats> modelGroupChats;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChats> modelGroupChats) {
        this.context = context;
        this.modelGroupChats = modelGroupChats;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChats(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChats(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChats holder, int position) {
        ModelGroupChats model = modelGroupChats.get(position);

        String message = model.getMessage();
        String timestamp = model.getTimestamp();
        String senderUid = model.getSender();
        String messageType = model.getType();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if(messageType.equals("text")){
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);

        }else {
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try{
                Picasso.get().load(message).placeholder(R.drawable.ic_imagechat_black).into(holder.messageIv);
            }catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.ic_imagechat_black);
            }
        }

        holder.timeTv.setText(dateTime);
        serUserName(model, holder);
    }

    private void serUserName(ModelGroupChats model, HolderGroupChats holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    holder.nameTv.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if(modelGroupChats.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return modelGroupChats.size();
    }

    class HolderGroupChats extends RecyclerView.ViewHolder{

        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;


        public HolderGroupChats(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);


        }
    }

}
