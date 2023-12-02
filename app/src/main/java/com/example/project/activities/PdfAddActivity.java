package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
    private TextView titleEd,descEd,cateTv;
    private Button submitBtn;
    private ImageButton backBtn,attachBtn;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> listTitle,listId;
    private ProgressDialog progressDialog;
    
    private static final String TAG="ADD_PDF_TAG";
    private static final int PDF_PICK_CODE=1000;
    private Uri pdfUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_add);
        titleEd=findViewById(R.id.titleEd);
        descEd=findViewById(R.id.descEd);
        cateTv=findViewById(R.id.cateTv);
        submitBtn=findViewById(R.id.submitBtn);
        backBtn=findViewById(R.id.backBtn);
        attachBtn=findViewById(R.id.attachBtn);

        firebaseAuth=FirebaseAuth.getInstance();
        loadPdfCategories();


        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        cateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }



    private String title="",desc="";
    private void validateData() {
        Log.d(TAG,"validateData: Đang xác thực dữ liệu...");
        title=titleEd.getText().toString().trim();
        desc=descEd.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhập tiêu đề sách!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Nhập mô tả sách!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectCateTitle)) {
            Toast.makeText(this, "Chọn một mục sách!", Toast.LENGTH_SHORT).show();
        } else if (pdfUri==null) {
            Toast.makeText(this, "Chọn Pdf", Toast.LENGTH_SHORT).show();
        }else
            uploadPdfToStorage();
    }

    private void uploadPdfToStorage() {
        //uploat pdf vao firebase
        Log.d(TAG,"uploadPdfToStorage: đang tải dữ liệu lên...");

        progressDialog.setMessage("Đang tải Pdf lên...");
        progressDialog.show();

        long timestamp=System.currentTimeMillis();

        String filePathName="Books/"+timestamp;
        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"onSuccess: PDF uploaded to storage...");
                        Log.d(TAG,"onSuccess: getting pdf url");

                        //get url
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadPdfUrl=""+uriTask.getResult();

                        //tải lên firebase database
                        uploadPdfInfoToDb(uploadPdfUrl,timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: PDF uploadPdfToStorage failed due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Tải lên thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadPdfInfoToDb(String uploadPdfUrl, long timestamp) {
        //upload pdf info to db
        Log.d(TAG,"uploadPdfToStorage: đang tải dữ liệu lên...");
        progressDialog.setMessage("Đang tải thông tin tệp lên...");
        String uid=firebaseAuth.getUid();

        //setup data to upload
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("desc",""+desc);
        hashMap.put("categoryId",""+selectCateId);
        hashMap.put("url",""+uploadPdfUrl);
        hashMap.put("timestamp",timestamp);
        hashMap.put("viewsCount",0);//luot xem
        hashMap.put("downloadsCount",0);//luot tai ve


        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess: Successfully uploaded!");
                        Toast.makeText(PdfAddActivity.this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: Failed to upload to db due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Tải lên thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    //select category id title

    private String selectCateId,selectCateTitle;
    private void categoryPickDialog() {
        Log.d(TAG,"categoryPickDialog: showing category pick dialog");

        //lấy danh sách các mục sách từ list
        String[] cateList=new String[listTitle.size()];
        for (int i=0;i<listTitle.size();i++){
            cateList[i]=listTitle.get(i);
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Chọn mục sách")
                .setItems(cateList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //bắt sự kiện click vào item của cate
                        selectCateTitle= listTitle.get(i);
                        selectCateId=listId.get(i);

                        cateTv.setText(selectCateTitle);
                        Log.d(TAG,"onClick: Selected Category: "+selectCateId+" "+selectCateTitle);
                    }
                }).show();

    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading pdf categories...");
        listTitle=new ArrayList<>();
        listId=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listTitle.clear();//lam sach danh sach
                listId.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String categoryId=""+ds.child("id").getValue();
                    String categoryTitle=""+ds.child("category").getValue();

                    //add
                    listId.add(categoryId);
                    listTitle.add(categoryTitle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent");

        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode==RESULT_OK){
            if (requestCode==PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: PDF Picked");
                
                pdfUri=data.getData();
                Toast.makeText(this, "Chọn file PDF thành công!", Toast.LENGTH_SHORT).show();
                
                Log.d(TAG,"onActivityResult: URI: "+pdfUri);
            }
        }else {
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "Đã hủy chọn file PDF!", Toast.LENGTH_SHORT).show();
        }
    }
}