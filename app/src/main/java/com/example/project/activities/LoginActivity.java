package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEd,passEd;
    private TextView forgotTv,noAcc;
    private Button loginBtn;


    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEd=findViewById(R.id.emailEd);
        passEd=findViewById(R.id.passwordEd);
        forgotTv=findViewById(R.id.forgotTv);
        loginBtn=findViewById(R.id.loginBtn);
        noAcc=findViewById(R.id.noAccTv);
        //init firebase
        firebaseAuth=FirebaseAuth.getInstance();



        //progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);


        noAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgorPasswordActivity.class));
            }
        });
    }


    String email="",password="";
    private void validateData() {
        //get data
        email=emailEd.getText().toString().trim();
        password=passEd.getText().toString().trim();

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ Email không hợp lệ!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhập mật khẩu của bạn!", Toast.LENGTH_SHORT).show();
        } else
            loginUser();
    }

    private void loginUser() {
        progressDialog.setMessage("Logging...");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login thanh cong
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //login that bai
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //kiem tra nguoi dung co ton tai hay khong o day
    private void checkUser() {
        progressDialog.setMessage("Đang kiểm tra tài khoản người dùng...");

        //check user la user hay admin tu database
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        //check in db
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else {
                            progressDialog.dismiss();
                            //get user type
                            String userType=""+snapshot.child("userType").getValue();
                            //check usertype
                            if(userType.equals("user")){
                                //nguoi dung thi se mo giao dien nguoi dung
                                Intent intent=new Intent(LoginActivity.this, DashboardUserActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else if (userType.equals("admin") || userType.equals("super admin")) {
                                //admin thi mo giao dien admin
                                Intent intent=new Intent(LoginActivity.this, DashboardAdminActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}