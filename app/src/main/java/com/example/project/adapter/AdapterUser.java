package com.example.project.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.Filter.FilterUser;
import com.example.project.R;
import com.example.project.databinding.DialogRoleEditBinding;
import com.example.project.databinding.UserItemAdminBinding;
import com.example.project.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.UserViewHolder> implements Filterable {
    private Context context;
    public ArrayList<User> list,filterList;
    private UserItemAdminBinding binding;
    private ProgressDialog progressDialog;
    private FilterUser filterUser;
    private FirebaseAuth firebaseAuth;
    public AdapterUser(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
        this.filterList = list;

        progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=UserItemAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        firebaseAuth=FirebaseAuth.getInstance();
        return new UserViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user= list.get(position);
        String uid= user.getUid();
        String name=user.getName();
        String email= user.getEmail();
        String image=user.getProfileImage();
        String role=user.getUserType();


        //set data
        holder.name.setText(name);
        holder.email.setText(email);
        holder.role.setText(role);
        //set image
        try{
            Glide.with(context).load(image)
                    .placeholder(R.drawable.ic_person_gray).into(holder.imageView);
        }catch (Exception e){
            holder.imageView.setImageResource(R.drawable.ic_person_gray);
        }

        if(role.equalsIgnoreCase("super admin")){
            holder.moreBtn.setVisibility(View.GONE);
            holder.role.setTextColor(holder.role.getResources().getColor(R.color.darkred));
        }else if(role.equalsIgnoreCase("admin"))
            holder.role.setTextColor(holder.role.getResources().getColor(R.color.yellow));
        else if (role.equalsIgnoreCase("user")) {
            holder.role.setTextColor(holder.role.getResources().getColor(R.color.green));
        }


        //check user
        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userType=""+snapshot.child("userType").getValue();
                        if(userType.equals("super admin")){
                            holder.moreBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    moreOptionsDialog(user);
                                }
                            });
                        }else {
                            holder.moreBtn.setVisibility(View.GONE);
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

    @Override
    public Filter getFilter() {
        if(filterUser==null){
            filterUser=new FilterUser(filterList,this);
        }
        return filterUser;
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ShapeableImageView imageView;
        TextView name,email,role;
        ImageButton moreBtn;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=binding.profileTv;
            name=binding.nameTv;
            email=binding.emailTv;
            role=binding.roleTv;
            moreBtn=binding.moreBtn;
        }
    }

    private void moreOptionsDialog(User user){
        String uid= user.getUid();
        String[] options={"Chỉnh sửa role","Xóa người dùng"};
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Tùy chọn")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            //sua=> sang dialog sua
                            editDialog(uid);
                        } else if (i==1) {
                            //xoa
                            deleteUser(uid);
                        }
                    }
                }).show();
    }
    
    private void editDialog(String id){
        String uid=id;
        DialogRoleEditBinding dialogRoleEditBinding =DialogRoleEditBinding.inflate(LayoutInflater.from(context));
        AlertDialog.Builder builder= new AlertDialog.Builder(context,R.style.CustomDialog);
        builder.setView(dialogRoleEditBinding.getRoot());
        
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
        
        dialogRoleEditBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        
        //bat su kien submit
        dialogRoleEditBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String role=dialogRoleEditBinding.commentEd.getText().toString().trim().toLowerCase();
                if(TextUtils.isEmpty(role)){
                    Toast.makeText(context, "Bạn không được để trống!", Toast.LENGTH_SHORT).show();
                } else if (!role.equalsIgnoreCase("user")&&!role.equalsIgnoreCase("admin")) {
                    Toast.makeText(context, "Bạn chỉ được chỉnh sửa thành user hoặc admin", Toast.LENGTH_SHORT).show();
                }else{
                    //update
                    progressDialog.setMessage("Đang cập nhật role user");
                    progressDialog.show();

                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("userType",""+role);
                    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                    reference.child(uid)
                            .updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }
                            });
                }
            }
        });
    }



    //delete User
    private void deleteUser(String uid){
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setMessage("Đang xóa người dùng!");
        progressDialog.show();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Xóa người dùng thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Xóa người dùng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
