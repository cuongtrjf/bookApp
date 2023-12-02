package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project.MyApplication;
import com.example.project.R;
import com.example.project.adapter.AdapterPdfFav;
import com.example.project.databinding.ActivityProfileBinding;
import com.example.project.model.Pdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private Uri imageUri=null;
    private ArrayList<Pdf> list;
    private AdapterPdfFav adapterPdfFav;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //reset data của thông tin ng dùng
        binding.accTypeTv.setText("N/A");
        binding.memberDateTv.setText("N/A");
        binding.favCountTv.setText("N/A");
        binding.accStatusTv.setText("N/A");



        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);


        loadUserInfo();

        loadFavoriteBooks();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.profileEditeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this,ProfileEditActivity.class));
            }
        });

        binding.accStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseUser.isEmailVerified()){
                    Toast.makeText(ProfileActivity.this, "Tài khoản đã xác thực", Toast.LENGTH_SHORT).show();
                }else {
                    //chưa xác thực
                    emailVerificationDialog();
                }
            }
        });
    }

    private void emailVerificationDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Xác thực tài khoản")
                .setMessage("Bạn có muốn xác thực tài khoản qua địa chỉ Email của bạn không")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendEmailVerification();
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void sendEmailVerification() {
        progressDialog.setMessage("Đang gửi yêu cầu xác thực tới địa chỉ " +firebaseUser.getEmail()+"...");
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //đã gửi
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Đã gửi yêu cầu xác thực, kiểm tra hộp thư Email của bạn.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //chưa gửi đc
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Có lỗi khi gửi yêu cầu xác thực.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserInfo() {
        //get email verification status, để hiện đã xác thực sau khi vừa xác thực tài khoản, phải đăngnhập lại
        if (firebaseUser.isEmailVerified()){
            binding.accStatusTv.setText("Đã xác thực");
        }else {
            binding.accStatusTv.setText("Chưa xác thực");
        }
        binding.accStatusTv.setTextColor(getResources().getColor(R.color.darkred2));



        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info
                        if(snapshot.exists()){
                            String email=""+snapshot.child("email").getValue();
                            String name=""+snapshot.child("name").getValue();
                            String profileImage=""+snapshot.child("profileImage").getValue();
                            String timestamp=""+snapshot.child("timestamp").getValue();
                            String uid=""+snapshot.child("uid").getValue();
                            String userType=""+snapshot.child("userType").getValue();
                            String date= MyApplication.formatTimestamp(Long.parseLong(timestamp));

                            //set data
                            binding.emailTv.setText(email);
                            binding.nameTv.setText(name);
                            binding.memberDateTv.setText(date);
                            binding.accTypeTv.setText(userType);

                            //set color
                            if(userType.equals("super admin")){
                                binding.nameTv.setTextColor(getResources().getColor(R.color.darkred));
                                binding.accTypeTv.setTextColor(getResources().getColor(R.color.darkred));
                            } else if (userType.equals("admin")) {
                                binding.nameTv.setTextColor(getResources().getColor(R.color.yellow));
                                binding.accTypeTv.setTextColor(getResources().getColor(R.color.yellow));
                            } else if (userType.equals("user")) {
                                binding.nameTv.setTextColor(getResources().getColor(R.color.green));
                                binding.accTypeTv.setTextColor(getResources().getColor(R.color.green));
                            }


                            //set img sử dụng glide
                            Glide.with(getApplicationContext())
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.profileTv);}
                        else {
                            Intent intent= new Intent(ProfileActivity.this,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadFavoriteBooks() {
        list=new ArrayList<>();

        //tai len sach yeu thich tu database
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    for (DataSnapshot cs:ds.child("Favorites").getChildren()){
                        String check=""+cs.child("id").getValue();
                        if (check.equals(""+firebaseAuth.getUid())){
                            String bookId=""+ds.child("id").getValue();
                            Pdf modelPdf=new Pdf();
                            modelPdf.setId(bookId);
                            list.add(modelPdf);
                        }
                    }
                }

                binding.favCountTv.setText(list.size()+"");
                adapterPdfFav=new AdapterPdfFav(ProfileActivity.this,list);

                binding.recycleView.setAdapter(adapterPdfFav);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}