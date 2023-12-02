package com.example.project.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.project.R;
import com.example.project.activities.DashboardAdminActivity;
import com.example.project.adapter.AdapterCategory;
import com.example.project.adapter.AdapterPdfUser;
import com.example.project.databinding.FragmentBooksUserBinding;
import com.example.project.model.Category;
import com.example.project.model.Pdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {

    private String cateId;
    private String category;
    private String uid;

    private ArrayList<Pdf> list;
    private AdapterPdfUser adapterPdfUser;

    //binding
    private FragmentBooksUserBinding binding;


    public BooksUserFragment() {
        // Required empty public constructor
    }


    public static BooksUserFragment newInstance(String cateId, String cate, String uid ) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", cateId);
        args.putString("category", cate);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cateId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()),container,false);

        if (category.equals("Tất cả")){
            //load tất sách
            loadAllBooks();
        } else if (category.equals("Đọc nhiều nhất")) {
            //load đọc nhiều nhất
            loadMostViewBooks("viewsCount");
        } else if (category.equals("Tải xuống nhiều nhất")) {
            //load tải xuống nhiều nhất
            loadMostDownBooks("downloadsCount");
        }else {
            //load mục sách đã chọn
            loadCateBooks();
        }


        //search
        binding.searchEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterPdfUser.getFilter().filter(charSequence);
                } catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return binding.getRoot();
    }

    private void loadCateBooks() {
        list=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild("categoryId").equalTo(cateId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //lam sach du lieu truoc khi them vao list
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            Pdf pdf = ds.getValue(Pdf.class);

                            //add to list
                            list.add(pdf);
                        }
                        //setup vao adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(),list);

                        //set adapter vao recycleview
                        binding.recycleView.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostDownBooks(String orderBy) {
        list=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild(orderBy).limitToLast(10)//load 10 book có lượt đọc cao nhất
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //lam sach du lieu truoc khi them vao list
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            Pdf pdf = ds.getValue(Pdf.class);

                            //add to list
                            list.add(pdf);
                        }
                        //setup vao adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(),list);

                        //set adapter vao recycleview
                        binding.recycleView.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostViewBooks(String orderBy) {
        list=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild(orderBy).limitToLast(10)//load 10 book có lượt đọc cao nhất
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //lam sach du lieu truoc khi them vao list
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    Pdf pdf = ds.getValue(Pdf.class);

                    //add to list
                    list.add(pdf);
                }
                //setup vao adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),list);

                //set adapter vao recycleview
                binding.recycleView.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllBooks() {
        list=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //lam sach du lieu truoc khi them vao list
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    Pdf pdf = ds.getValue(Pdf.class);

                    //add to list
                    list.add(pdf);
                }
                //setup vao adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),list);

                //set adapter vao recycleview
                binding.recycleView.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}