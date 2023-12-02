package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.project.R;
import com.example.project.fragment.BooksUserFragment;
import com.example.project.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {
    public ArrayList<Category> list;
    public ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String name="";
    private FirebaseAuth firebaseAuth;
    private TextView subtitle,title;

    private FloatingActionButton backBtn;
    private ImageButton logoutBtn,profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_user);
        subtitle=findViewById(R.id.subtitle);
        title=findViewById(R.id.title);
        logoutBtn=findViewById(R.id.logoutBtn);
        profileBtn=findViewById(R.id.profileBtn);
        backBtn=findViewById(R.id.backBtn);
        viewPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabLayout);
        firebaseAuth=FirebaseAuth.getInstance();
        backBtn.setVisibility(View.GONE);
        checkUser();

        setupViewPagerAdapter(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardUserActivity.this);
                builder.setTitle("Đăng xuất tài khoản")
                        .setMessage("Bạn muốn đăng xuất?")
                        .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseAuth.signOut();
                                Intent intent=new Intent(DashboardUserActivity.this,MainActivity.class);
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

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this,ProfileActivity.class));
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
//                finish();
            }
        });

    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
        list=new ArrayList<>();

        //load cate từ firebase
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                Category categoryAll=new Category("01","Tất cả","",1);
                Category categoryMostView=new Category("02","Đọc nhiều nhất","",1);
                Category categoryMostDown=new Category("03","Tải xuống nhiều nhất","",1);

                list.add(categoryAll);
                list.add(categoryMostView);
                list.add(categoryMostDown);


                //add data to viewpageradapter
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+categoryAll.getId(),
                        ""+categoryAll.getCategory(),
                        ""+categoryAll.getUid()
                ),categoryAll.getCategory());

                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+categoryMostView.getId(),
                        ""+categoryMostView.getCategory(),
                        ""+categoryMostView.getUid()
                ),categoryMostView.getCategory());

                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+categoryMostDown.getId(),
                        ""+categoryMostDown.getCategory(),
                        ""+categoryMostDown.getUid()
                ),categoryMostDown.getCategory());

                //làm tươi list
                viewPagerAdapter.notifyDataSetChanged();

                //load từ firebase
                for (DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    Category category=ds.getValue(Category.class);
                    //add data to list
                    list.add(category);
                    //add data to viewpageradapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                            ""+category.getId(),
                            ""+category.getCategory(),
                            ""+category.getUid()
                    ),category.getCategory());
                    //làm tươi list
                    viewPagerAdapter.notifyDataSetChanged();
                }

                //set adapet vào viewpager
                viewPager.setAdapter(viewPagerAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<BooksUserFragment> fragmentsList=new ArrayList<>();
        private ArrayList<String> fragmentTitleList=new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior,Context context) {
            super(fm, behavior);
            this.context=context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }

        private void addFragment(BooksUserFragment fragment,String title){
            fragmentsList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser==null){
            //chua login
            title.setVisibility(View.GONE);
            subtitle.setText("Chưa đăng nhập");
            logoutBtn.setVisibility(View.GONE);
            profileBtn.setVisibility(View.GONE);
        }else {
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    name=""+snapshot.child("name").getValue();
                    subtitle.setText(name);
                    String userType=""+snapshot.child("userType").getValue();
                    if(userType.equals("admin")){
                        backBtn.setVisibility(View.VISIBLE);
                        subtitle.setTextColor(getResources().getColor(R.color.yellow));
                    } else if (userType.equals("super admin")) {
                        backBtn.setVisibility(View.VISIBLE);
                        subtitle.setTextColor(getResources().getColor(R.color.darkred));
                    } else if (userType.equals("user")) {
                        subtitle.setTextColor(getResources().getColor(R.color.green));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //da duoc login
            title.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.VISIBLE);
            profileBtn.setVisibility(View.VISIBLE);
        }
    }
}