package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.project.R;
import com.example.project.adapter.AdapterCategory;
import com.example.project.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private FirebaseAuth firebaseAuth;
    private TextView subtitle;
    private ImageButton logoutBtn,profileBtn;
    private Button addCate,changeView;
    private EditText searchEd;
    private FloatingActionButton fab,editUser;
    private ArrayList<Category> list;
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboad_admin);
        subtitle = findViewById(R.id.subtitle);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileBtn=findViewById(R.id.profileBtn);
        addCate = findViewById(R.id.addCate);
        fab = findViewById(R.id.addPdfFab);
        recyclerView = findViewById(R.id.recycleView);
        searchEd = findViewById(R.id.searchEd);
        changeView=findViewById(R.id.changeViewBtn);
        editUser=findViewById(R.id.editUser);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        loadCategories();

        //search theo thay doi cua text
        searchEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterCategory.getFilter().filter(charSequence);
                } catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardAdminActivity.this);
                builder.setTitle("Đăng xuất tài khoản")
                        .setMessage("Bạn muốn đăng xuất?")
                        .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseAuth.signOut();
                                Intent intent=new Intent(DashboardAdminActivity.this,MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });

        addCate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, AddCategoryActivity.class));
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });

        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, UserList.class));
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this,ProfileActivity.class));
            }
        });


        changeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, DashboardUserActivity.class));
//                finish();
            }
        });
    }

    private void loadCategories() {
        list = new ArrayList<>();

        //get all categories from firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //lam sach du lieu truoc khi them vao list
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    Category category = ds.getValue(Category.class);

                    //add to list
                    list.add(category);
                }
                //setup vao adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, list);

                //set adapter vao recycleview
                recyclerView.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//        if (firebaseUser == null) {
//            //chua login, ve man hinh chinh
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        } else {
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name=""+snapshot.child("name").getValue();
                    String userType=""+snapshot.child("userType").getValue();
                    if(userType.equalsIgnoreCase("super admin")){
                        subtitle.setText(name);
                        subtitle.setTextColor(getResources().getColor(R.color.darkred));
                    } else if (userType.equalsIgnoreCase("admin")) {
                        subtitle.setText(name);
                        subtitle.setTextColor(getResources().getColor(R.color.yellow));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //da duoc login
//            String email = firebaseUser.getEmail();
//            subtitle.setText(email);
//        }
    }
}