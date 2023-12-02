package com.example.project.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project.MyApplication;
import com.example.project.R;
import com.example.project.databinding.ActivityProfileEditBinding;
import com.example.project.databinding.DialogPasswordEditBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private Uri imageUri=null;
    private String name="";
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        loadUserInfo();

        //set up dialog progress
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);//không hiển thị khi đang nhấn bên ngoài thông báo

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.profileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageAttachMenu();//hiện các ảnh để chọn
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
        binding.editPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editPass(user);
            }
        });
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userType=""+snapshot.child("userType").getValue();
                if(userType.equalsIgnoreCase("super admin"))
                    binding.removeBtn.setVisibility(View.GONE);
                else {
                    binding.removeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
                            builder.setTitle("Xóa tài khoản")
                                    .setMessage("Bạn muốn xóa tài khoản chính mình?")
                                    .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(ProfileEditActivity.this, "Đang xóa tài khoản...", Toast.LENGTH_SHORT).show();
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(ProfileEditActivity.this, "Đã xóa tài khoản, về trang đăng nhập!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            reference.child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
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
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validateData() {
        name=binding.nameEd.getText().toString().trim();
        
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Nhập họ và tên của bạn!", Toast.LENGTH_SHORT).show();
        }else {
            if(imageUri==null){
                //nếu k có dữ liệu ảnh
                updateProfile("");
            }else
                uploadImage();
        }
    }

    private void updateProfile(String imageUrl){
        progressDialog.setMessage("Đang cập nhật profile...");
        progressDialog.show();

        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("name",name);
        if(imageUri!=null){
            hashMap.put("profileImage",""+imageUrl);
        }


        //update vao database
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Có lỗi khi cập nhật dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage() {
        progressDialog.setMessage("Đang cập nhật ảnh cá nhân...");
        progressDialog.show();

        //path image và tên, dùng uid để thay đổi
        String filePathAndName="ProfileImages/"+firebaseAuth.getUid();

        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedImageUrl=""+uriTask.getResult();

                updateProfile(uploadedImageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileEditActivity.this, "Tải lên thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showImageAttachMenu() {
        PopupMenu popupMenu= new PopupMenu(this,binding.profileTv);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Camera");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Bộ sưu tập");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int which=menuItem.getItemId();
                if (which==0){
                    //chon camera
                    pickImageCamera();
                } else if (which==1) {
                    //chon bo suu tap
                    pickImageGallery();
                }
                return false;
            }
        });
    }

    private void pickImageGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Ảnh mới");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Mô tả");
        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);

    }


    private ActivityResultLauncher<Intent> cameraActivityResultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //su dung intent cua camera va lay dc uri cua anh
                    if (result.getResultCode()==RESULT_OK){
                        Intent data=result.getData();
                        binding.profileTv.setImageURI(imageUri);
                    }else {
                        Toast.makeText(ProfileEditActivity.this, "Đã hủy", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()==RESULT_OK){
                        Intent data=result.getData();
                        imageUri=data.getData();
                        binding.profileTv.setImageURI(imageUri);
                    }else
                        Toast.makeText(ProfileEditActivity.this, "Đã hủy", Toast.LENGTH_SHORT).show();
                }
            }
    );



    private void loadUserInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info

                        String name=""+snapshot.child("name").getValue();
                        String profileImage=""+snapshot.child("profileImage").getValue();

                        //set data
                        binding.nameEd.setText(name);


                        //set img sử dụng glide
                        Glide.with(getApplicationContext())
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileTv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    //doi mat khau
    private void editPass(FirebaseUser user){
        DialogPasswordEditBinding dialogPasswordEditBinding= DialogPasswordEditBinding.inflate(LayoutInflater.from(this));

        //setup alertdialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(dialogPasswordEditBinding.getRoot());


        //creat and show dialog
        AlertDialog alertDialog=builder.create();
        alertDialog.show();


        //neu lick ngoai dialog thi close dialog
        dialogPasswordEditBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        dialogPasswordEditBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate data
                String pass1=dialogPasswordEditBinding.passwordEd.getText().toString().trim();
                String pass2=dialogPasswordEditBinding.cfpasswordEd.getText().toString().trim();
                if(pass1.isEmpty()){
                    Toast.makeText(ProfileEditActivity.this, "Nhập mật khẩu bạn muốn đổi!", Toast.LENGTH_SHORT).show();
                } else if (pass2.isEmpty()) {
                    Toast.makeText(ProfileEditActivity.this, "Nhập mật khẩu xác nhận!", Toast.LENGTH_SHORT).show();
                } else if (!pass1.equals(pass2)) {
                    Toast.makeText(ProfileEditActivity.this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                }else{
                    //sau khi validate xong thì đổi mật khẩu
                    user.updatePassword(pass1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ProfileEditActivity.this, "Thay đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }
}