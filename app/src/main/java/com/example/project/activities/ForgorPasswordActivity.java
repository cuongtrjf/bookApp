package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.databinding.ActivityForgorPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgorPasswordActivity extends AppCompatActivity {

    private ActivityForgorPasswordBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityForgorPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        firebaseAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    
    
    private String email="";
    private void validateData() {
        email=binding.emailEd.getText().toString().trim();
        
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Bạn chưa nhập địa chỉ Email", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ Email không hợp lệ", Toast.LENGTH_SHORT).show();
        }else {
            recoverPassword();//lấy lại mật khẩu
        }
    }

    private void recoverPassword() {
        progressDialog.setMessage("Đang gửi hướng dẫn đến địa chỉ email "+email);
        progressDialog.show();


        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgorPasswordActivity.this, "Đã gửi hướng dẫn tới địa chỉ email "+email, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgorPasswordActivity.this, "Gửi thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}