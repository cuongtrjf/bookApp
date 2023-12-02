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
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    private EditText nameEd,emailEd,passEd,cfpassEd;
    private Button registerBtn;
    private ImageButton backBtn;


    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog (tien trinh)
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nameEd=findViewById(R.id.nameEd);
        emailEd=findViewById(R.id.emailEd);
        passEd=findViewById(R.id.passwordEd);
        cfpassEd=findViewById(R.id.cfpasswordEd);
        registerBtn=findViewById(R.id.registerBtn);
        backBtn=findViewById(R.id.backBtn);

        //init firebase auth
        firebaseAuth= FirebaseAuth.getInstance();


        //progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Đợi một chút...");
        progressDialog.setCanceledOnTouchOutside(false);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private String name="",email="",password="";
    private void validateData() {
        //xac thuc du lieu
        name=nameEd.getText().toString().trim();
        email=emailEd.getText().toString().trim();
        password=passEd.getText().toString().trim();
        String cfpass=cfpassEd.getText().toString();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Nhập họ tên của bạn!", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ Email không hợp lệ!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhập mật khẩu của bạn!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cfpass)) {
            Toast.makeText(this, "Nhập mật khẩu xác nhận!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cfpass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
        }else
            createUserAccount();
    }

    private void createUserAccount() {
        //show progress dialog
        progressDialog.setMessage("Đang tạo tài khoản...");
        progressDialog.show();

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //tao thanh cong account
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //khong tao thanh cong
                        progressDialog.dismiss();//bo qua
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Lưu người dùng...");

        long timestamp=System.currentTimeMillis();

        //lay duoc thoi gian bat dau dang ky
        String uid=firebaseAuth.getUid();

        //setup data to add in db
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage","");//lam sau
        hashMap.put("userType","user");//gia tri co the la user, con admin se lam thu cong trong firebase
        hashMap.put("timestamp",timestamp);

        //set data to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data add thanh cong
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công, đã đăng nhập!", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(RegisterActivity.this,DashboardUserActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //data add fail
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}