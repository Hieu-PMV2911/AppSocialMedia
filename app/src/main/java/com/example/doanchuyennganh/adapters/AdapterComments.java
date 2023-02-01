package com.example.doanchuyennganh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.models.ModelComments;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{

    Context context;
    List<ModelComments> commentsList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComments> commentsList, String myUid, String postId) {
        this.context = context;
        this.commentsList = commentsList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_comments, parent, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = commentsList.get(position).getUid();
        String name = commentsList.get(position).getuName();
        String email = commentsList.get(position).getuEmail();
        String image = commentsList.get(position).getuDp();
        String cid = commentsList.get(position).getcId();
        String comment = commentsList.get(position).getComment();
        String timestamp = commentsList.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_foreground).into(holder.avatarTv);

        }catch (Exception e){

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myUid.equals(uid)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Xoá");
                    builder.setMessage("Bạn có chắc xoá bình luận này?");
                    builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteComments(cid);
                        }
                    });
                    builder.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }else {
                    Toast.makeText(context, "Không thể xoá 1 số bình luận...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteComments(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments =""+ snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        ImageView avatarTv;
        TextView nameTv, commentTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarTv = itemView.findViewById(R.id.avartarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentsTv);
            timeTv = itemView.findViewById(R.id.timeTv);



        }
    }
}
