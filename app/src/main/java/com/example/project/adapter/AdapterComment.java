package com.example.project.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.MyApplication;
import com.example.project.R;
import com.example.project.databinding.CommentItemBinding;
import com.example.project.model.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.CommentViewHolder>{
    private Context context;
    private ArrayList<Comment> list;

    private FirebaseAuth firebaseAuth;

    private CommentItemBinding binding;



    public AdapterComment(Context context, ArrayList<Comment> list) {
        this.context = context;
        this.list = list;
        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding= CommentItemBinding.inflate(LayoutInflater.from(context),parent,false);
        return new CommentViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        //get data
        Comment modelComment= list.get(position);
        String id=modelComment.getId();
        String bookId=modelComment.getBookId();
        String comment=modelComment.getComment();
        String uid=modelComment.getUid();
        String timestamp=modelComment.getTimestamp();

        String date= MyApplication.formatTimestamp(Long.parseLong(timestamp));


        //set data
        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

        //dung de load anh, profile
        loadUserDetails(modelComment,holder);


        //bat skien click vao comment hien ra xoa comment
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser()!=null && uid.equals(firebaseAuth.getUid())){
                    //neu la nguoi da comment cai comment do thi dc phep xoa comment
                    deleteComment(modelComment,holder);
                }
            }
        });
    }

    private void deleteComment(Comment modelComment, CommentViewHolder holder) {
        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setTitle("Xoá bình luận")
                .setMessage("Bạn có muốn xóa bình luận này không?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(modelComment.getBookId())
                                .child("Comments")
                                .child(modelComment.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Đã xảy ra lỗi khi xóa bình luận.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void loadUserDetails(Comment modelComment, CommentViewHolder holder) {
        String uid=modelComment.getUid();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //kiem tra xem nguoi dung co ton tai khong, neu co thi hien cmt, k co thi xoa cmt
                            //get data
                            String name=""+snapshot.child("name").getValue();
                            String profileImage =""+snapshot.child("profileImage").getValue();
                            String userType=""+snapshot.child("userType").getValue();
                            //set data
                            holder.nameTv.setText(name);
                            if(userType.equals("super admin")){
                                holder.nameTv.setTextColor(holder.nameTv.getResources().getColor(R.color.darkred));
                            } else if (userType.equals("admin")) {
                                holder.nameTv.setTextColor(holder.nameTv.getResources().getColor(R.color.yellow));
                            } else if (userType.equals("user")) {
                                holder.nameTv.setTextColor(holder.nameTv.getResources().getColor(R.color.green));
                            }
                            try{
                                Glide.with(context)
                                        .load(profileImage).placeholder(R.drawable.ic_person_gray).into(holder.profileTv);
                            }catch (Exception e){
                                holder.profileTv.setImageResource(R.drawable.ic_person_gray);
                            }
                        }else {
                            //neu nguoi dung da bi xoa,tu dong xoa binh luan cua nguoi do
                            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
                            reference.child(modelComment.getBookId())
                                    .child("Comments")
                                    .child(modelComment.getId())
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
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
        return list.size();
    }

    //viewholder
    class CommentViewHolder extends RecyclerView.ViewHolder{
        ShapeableImageView profileTv;
        TextView nameTv,dateTv,commentTv;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            profileTv=binding.profileTv;
            nameTv=binding.nameTv;
            dateTv=binding.dateTv;
            commentTv=binding.commentTv;
        }
    }
}
