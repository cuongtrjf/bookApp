package com.example.project.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MyApplication;
import com.example.project.activities.PdfDetailActivity;
import com.example.project.databinding.PdfItemFavBinding;
import com.example.project.model.Pdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFav extends RecyclerView.Adapter<AdapterPdfFav.PdfFavViewHolder> {
    private Context context;

    private ArrayList<Pdf> list;
    private PdfItemFavBinding binding;



    public AdapterPdfFav(Context context, ArrayList<Pdf> list) {
        this.context = context;
        this.list = list;


    }

    @NonNull
    @Override
    public PdfFavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = PdfItemFavBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PdfFavViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfFavViewHolder holder, int position) {
        Pdf pdf = list.get(position);
        loadBookDetails(pdf, holder);


        //bat su kien click pdf chuyen sang chi tiet file pdf
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdf.getId());
                context.startActivity(intent);
            }
        });

        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context,pdf.getId());
            }
        });
    }

    private void loadBookDetails(Pdf pdf, PdfFavViewHolder holder) {
        String bookId = pdf.getId();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get book info
                String bookTitle = "" + snapshot.child("title").getValue();
                String desc = "" + snapshot.child("desc").getValue();
                String cateId = "" + snapshot.child("categoryId").getValue();
                String bookUrl = "" + snapshot.child("url").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();
                String uid = "" + snapshot.child("uid").getValue();
                String viewsCount = "" + snapshot.child("viewsCount").getValue();
                String downloadsCount = "" + snapshot.child("downloadsCount").getValue();

                long timeFav= Long.parseLong(timestamp);
                //set info vao pdf
                pdf.setTitle(bookTitle);
                pdf.setDesc(desc);
                pdf.setTimestamp(timeFav);
                pdf.setCategoryId(cateId);
                pdf.setUid(uid);
                pdf.setUrl(bookUrl);
                pdf.setFavorite(true);

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                MyApplication.loadCategory(cateId,holder.cateTv);
                MyApplication.loadPdfFromUrl(""+bookUrl,""+bookTitle,holder.pdfView,holder.bar,null);
                MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,holder.sizeTv);


                //set data vao view
                //set data
                holder.titleTv.setText(bookTitle);
                holder.descTv.setText(desc);
                holder.dateTv.setText(date);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //viewholder
    class PdfFavViewHolder extends RecyclerView.ViewHolder {
        PDFView pdfView;
        ProgressBar bar;
        TextView titleTv, descTv, cateTv, sizeTv, dateTv;
        ImageButton removeFavBtn;

        public PdfFavViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            bar = binding.progressBar;
            titleTv = binding.titleTv;
            descTv = binding.descTv;
            cateTv = binding.cateTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            removeFavBtn = binding.removeFavBtn;
        }
    }
}
