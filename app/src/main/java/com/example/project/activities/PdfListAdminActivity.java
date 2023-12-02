package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.project.adapter.AdapterPdf;
import com.example.project.databinding.ActivityPdfListAdminBinding;
import com.example.project.model.Pdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {
    
    private static final String TAG="PDF_LIST_TAG";
    private ActivityPdfListAdminBinding binding;
    private ArrayList<Pdf> list;
    private AdapterPdf adapterPdf;
    private String categoryId,categoryTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //get data from intent
        Intent intent=getIntent();
        categoryId=intent.getStringExtra("categoryId");
        categoryTitle=intent.getStringExtra("categoryTitle");


        loadPdfList();


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.subtitleTv.setText(categoryTitle);

        //search
        binding.searchEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterPdf.getFilter().filter(charSequence);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadPdfList() {
        list=new ArrayList<>();


        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            //getdata
                            Pdf pdf=ds.getValue(Pdf.class);
                            //add lít
                            list.add(pdf);

                            Log.d(TAG, "onDataChange: "+pdf.getId()+" "+pdf.getTitle());
                        }

                        //setup adapter
                        adapterPdf=new AdapterPdf(PdfListAdminActivity.this,list);
                        binding.recycleView.setAdapter(adapterPdf);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}