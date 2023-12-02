package com.example.project.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.project.MyApplication;
import com.example.project.R;
import com.example.project.adapter.AdapterComment;
//import com.example.project.adapter.AdapterPdfFav;
import com.example.project.databinding.ActivityPdfDetailBinding;
import com.example.project.databinding.DialogCommentAddBinding;
import com.example.project.model.Comment;
import com.example.project.model.Pdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {


    //pdf id từ intent nhận ở adapter pdf
    String bookId,bookTitle,bookUrl;
    boolean isInMyFavorite=false;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<Comment> list;
    private AdapterComment adapterComment;

    private ActivityPdfDetailBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        bookId=intent.getStringExtra("bookId");

        //ẩn nút tải xuống trước khi dữ liệu được tải lên
        binding.downloadBtn.setVisibility(View.GONE);


        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!=null){
            checkIsFavorite();
        }


        loadBookDetail();
        loadComments();



        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId",bookId);
                MyApplication.incrementViewsCount(bookId);
                startActivity(intent1);
            }
        });


        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);
                }else
//                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);
            }
        });


        //bắt sự kiện click ưa thích/xóa ưa thích
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser()==null){
                    Toast.makeText(PdfDetailActivity.this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                }else {
                    if(isInMyFavorite){
                        //nếu là ưa thích
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,bookId);
                    }else {
                        //nếu k phải ưa thích
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                    }
                }
            }
        });


        //skien click hiện comment dialog
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (firebaseAuth.getCurrentUser()==null){
                    //không đăng nhập
                    Toast.makeText(PdfDetailActivity.this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                }else {
                    addCommentDialog();
                }
            }
        });
    }

    private void loadComments() {
        list=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            //get data là model của comment
                            Comment model= ds.getValue(Comment.class);
                            list.add(model);
                        }

                        adapterComment= new AdapterComment(PdfDetailActivity.this,list);
                        binding.commentRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private String comment="";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding= DialogCommentAddBinding.inflate(LayoutInflater.from(this));


        //setup alertdialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());


        //tao va hien dialog
        AlertDialog alertDialog=builder.create();
        alertDialog.show();


        //bat su kien click vao dialog, k hien dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


        //bat su kien click them comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data
                comment=commentAddBinding.commentEd.getText().toString().trim();

                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Bạn chưa nhập bình luận!", Toast.LENGTH_SHORT).show();
                }else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        progressDialog.setMessage("Đang thêm bình luận...");
        progressDialog.show();

        String timestamp=""+System.currentTimeMillis();//để tạo id và thời gian comment


        //setupo data và thêm vào db
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        // đường dẫn Books > bookId > Comments > commentId > commentData
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Thêm bình luận thất bại", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //yêu cầu cho phép tải xuống
//    private ActivityResultLauncher<String> requestPermissionLauncher=
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
//                if(isGranted){
//                    MyApplication.downloadBook(this,""+bookId,""+bookTitle,""+bookUrl);
//                }else
//                    Toast.makeText(this, "Tải xuống bị từ chối!", Toast.LENGTH_SHORT).show();
//            });

    private void loadBookDetail() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                        //get data
                            bookTitle=""+snapshot.child("title").getValue();
                            String desc=""+snapshot.child("desc").getValue();
                            String cateId=""+snapshot.child("categoryId").getValue();
                            String viewsCount=""+snapshot.child("viewsCount").getValue();
                            String downloadsCount=""+snapshot.child("downloadsCount").getValue();
                            bookUrl=""+snapshot.child("url").getValue();
                            String timestamp=""+snapshot.child("timestamp").getValue();


                            //đã load được dữ liệu, nút dowload sẽ hiện ra
                            binding.downloadBtn.setVisibility(View.VISIBLE);

                            //format date
                            String date=MyApplication.formatTimestamp(Long.parseLong(timestamp));

                            MyApplication.loadCategory(""+cateId,binding.cateTv);
                            MyApplication.loadPdfFromUrl(""+bookUrl,""+bookTitle,binding.pdfView,binding.progressBar,binding.pagesTv);
                            MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,binding.sizeTv);



                            //set dât
                            binding.titleTv.setText(bookTitle);
                            binding.descTv.setText(desc);
                            binding.viewcTv.setText(viewsCount.replace("null","N/A"));
                            binding.downcTv.setText(downloadsCount.replace("null","N/A"));
                            binding.dateTv.setText(date);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Favorites").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite=snapshot.exists();//là đã tồn tại, đã yêu thích => true
                        if (isInMyFavorite){
                            //đã yêu thích
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                            binding.favoriteBtn.setText("Xóa ưa thích");
                        }else {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0);
                            binding.favoriteBtn.setText("Thêm ưa thích");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}