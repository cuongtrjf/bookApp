package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.project.databinding.ActivityAddCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddCategoryActivity extends AppCompatActivity {

    private ActivityAddCategoryBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.addCate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private String cate="";
    private void validateData() {
        cate=binding.cateEd.getText().toString().trim();
        if(TextUtils.isEmpty(cate)){
            Toast.makeText(this, "Xin hãy nhập tên mục sách!", Toast.LENGTH_SHORT).show();
        }else {
            addCateFirebase();
        }
    }

    private void addCateFirebase() {
        progressDialog.setMessage("Đang thêm...");
        progressDialog.show();

        long timestamp=System.currentTimeMillis();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+cate);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //thêm vào firebase
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, "Thêm thành công mục sách!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, "Thêm mục sách thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}