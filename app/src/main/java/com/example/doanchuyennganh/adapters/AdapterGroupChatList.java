package com.example.doanchuyennganh.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.GroupChatFragment;
import com.example.doanchuyennganh.GroupChatsActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.models.ModelGroupChat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList>{
    private Context context;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChat> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    private ArrayList<ModelGroupChat> groupChatLists;

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list, parent, false);

        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
        ModelGroupChat model = groupChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        loadLastMessage(model, holder);

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");



        holder.groupTitleTv.setText(groupTitle);
        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconTv);
        }catch (Exception e){
            holder.groupIconTv.setImageResource(R.drawable.ic_group_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatsActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });


    }

    private void loadLastMessage(ModelGroupChat model, HolderGroupChatList holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String message = ""+ds.child("message").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String sender = ""+ds.child("sender").getValue();
                    String messageType = ""+ds.child("type").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));

                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                    if(messageType.equals("image")){
                        holder.messageTv.setText("Gửi hình ảnh");
                    }else{
                        holder.messageTv.setText(message);
                    }
                    holder.timeTv.setText(dateTime);

                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                    ref1.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()){
                                String name = ""+ds.child("name").getValue();
                                holder.nameTv.setText(name);
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
    public int getItemCount() {
        return groupChatLists.size();
    }


    class HolderGroupChatList extends RecyclerView.ViewHolder{

        private ImageView groupIconTv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;
        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconTv = itemView.findViewById(R.id.groupIconTv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTVs);
            messageTv = itemView.findViewById(R.id.messageTvs);
            timeTv = itemView.findViewById(R.id.timeTvs);
            groupIconTv = itemView.findViewById(R.id.groupIconTv);

        }
    }


}
