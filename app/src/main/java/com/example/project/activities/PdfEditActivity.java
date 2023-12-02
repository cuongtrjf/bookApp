package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.project.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {


    private ActivityPdfEditBinding binding;

    //bookId lấy từ intent được gửi từ AdapterPdf
    private String bookId;
    private ProgressDialog progressDialog;

    private ArrayList<String> cateTitleList,cateIdList;
    private static final String TAG="BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId=getIntent().getStringExtra("bookId");

        //setup progress
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);



        loadCategories();
        loadBookInfo();


        //bắt sự kiện nút
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        binding.cateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });


        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }




    //xác thực dữ liệu và sửa dữ liệu
    private String title="",desc="";
    private void validateData() {
        Log.d(TAG,"validateData: Đang xác thực dữ liệu...");
        title=binding.titleEd.getText().toString().trim();
        desc=binding.descEd.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhập tiêu đề sách!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Nhập mô tả sách!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectCateId)) {
            Toast.makeText(this, "Chọn một mục sách!", Toast.LENGTH_SHORT).show();
        }else
            updatePdf();
    }


    //update
    private void updatePdf() {
        Log.d(TAG, "updatePdf: bắt đầu update");

        progressDialog.setMessage("Đang cập nhật thông tin sách...");
        progressDialog.show();


        //setup data to update
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("desc",""+desc);
        hashMap.put("categoryId",""+selectCateId);

        //start update
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Cập nhật thông tin sách thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Cập nhật thông tin sách thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //lấy dữ liệu từ firebase hiển thị ra view theo id được gửi từ intent
    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");

        DatabaseReference referenceBook= FirebaseDatabase.getInstance().getReference("Books");
        referenceBook.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectCateId=""+snapshot.child("categoryId").getValue();
                        String desc=""+snapshot.child("desc").getValue();
                        String title=""+snapshot.child("title").getValue();

                        //set vào view
                        binding.titleEd.setText(title);
                        binding.descEd.setText(desc);

                        Log.d(TAG, "onDataChange: Đang chỉnh sửa");
                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Categories");
                        reference.child(selectCateId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get cate
                                        String category=""+snapshot.child("category").getValue();
                                        binding.cateTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private String selectCateId="",selectCateTitle="";
    private void categoryDialog(){//bắt sự kiện chọn category nào thì hiện category đó ra
        String[] categories=new String[cateTitleList.size()];
        for (int i=0;i<cateTitleList.size();i++){
            categories[i]=cateTitleList.get(i);
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Chọn mục sách")
                .setItems(categories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectCateId=cateIdList.get(i);
                        selectCateTitle=cateTitleList.get(i);

                        binding.cateTv.setText(selectCateTitle);
                    }
                }).show();
    }


    //load vào list chọn category
    private void loadCategories() {
        Log.d(TAG, "loadCategories:Loading cate ");
        cateIdList=new ArrayList<>();
        cateTitleList=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cateIdList.clear();
                cateTitleList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id=""+ds.child("id").getValue();
                    String cate=""+ds.child("category").getValue();
                    cateIdList.add(id);
                    cateTitleList.add(cate);

                    Log.d(TAG, "onDataChange: ID: "+id);
                    Log.d(TAG, "onDataChange: Category: "+cate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}