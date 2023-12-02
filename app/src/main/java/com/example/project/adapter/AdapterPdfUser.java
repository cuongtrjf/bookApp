package com.example.project.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Filter.FilterPdf;
import com.example.project.Filter.FilterPdfUser;
import com.example.project.MyApplication;
import com.example.project.R;
import com.example.project.activities.PdfDetailActivity;
import com.example.project.activities.PdfEditActivity;
import com.example.project.databinding.PdfItemUserBinding;
import com.example.project.model.Pdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.PdfUserViewHolder>
implements Filterable {
    private Context context;
    public ArrayList<Pdf> list,filterList;
    private FilterPdfUser filterPdf;
    private FirebaseAuth firebaseAuth;

    private PdfItemUserBinding binding;

    public AdapterPdfUser(Context context, ArrayList<Pdf> list) {
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @NonNull
    @Override
    public PdfUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=PdfItemUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new PdfUserViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfUserViewHolder holder, int position) {
        //get data
        Pdf pdf=list.get(position);
        String pdfId=pdf.getId();
        String title=pdf.getTitle();
        String desc=pdf.getDesc();
        String pdfUrl=pdf.getUrl();
        String cateId=pdf.getCategoryId();
        firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser!=null){
            reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userType=""+snapshot.child("userType").getValue();
                    if(userType.equals("admin")){
                        holder.moreBtn.setVisibility(View.VISIBLE);
                    }else
                        holder.moreBtn.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else
            holder.moreBtn.setVisibility(View.GONE);


        long timestamp=pdf.getTimestamp();
        String dateformat= MyApplication.formatTimestamp(timestamp);//convert time từ long sang string


        //set data
        holder.titleTv.setText(title);
        holder.descTv.setText(desc);
        holder.dateTv.setText(dateformat);
//        if (firebaseAuth.getCurrentUser().getUid().equals("admin")){
//            holder.moreBtn.setVisibility(View.VISIBLE);
//        }else
//            holder.moreBtn.setVisibility(View.GONE);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(pdf,holder);
            }
        });

        //load
        MyApplication.loadCategory(""+cateId,holder.cateTv);
        MyApplication.loadPdfFromUrl(""+pdfUrl,""+title,holder.pdfView,holder.bar,null);
        MyApplication.loadPdfSize(""+pdfUrl,""+title,holder.sizeTv);


        //bat su kien click pdf chuyen sang chi tiet file pdf
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }


    private void moreOptionsDialog(Pdf pdf, PdfUserViewHolder holder) {
        String bookId=pdf.getId();
        String bookTitle=pdf.getTitle();
        String bookUrl=pdf.getUrl();


        String[] options={"Chỉnh sửa","Xóa"};
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Tùy chọn")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            //sửa
                            Intent intent=new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                        } else if (i==1) {
                            //xóa
                            MyApplication.delBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
//                            delBook(pdf,holder);
                        }
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filterPdf==null){
            filterPdf=new FilterPdfUser(filterList,this);
        }
        return filterPdf;
    }

    //viewholder
    class PdfUserViewHolder extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar bar;
        TextView titleTv,descTv,cateTv,sizeTv,dateTv;
        ImageButton moreBtn;

        public PdfUserViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfView=binding.pdfView;
            bar=binding.progressBar;
            titleTv=binding.titleTv;
            descTv=binding.descTv;
            cateTv=binding.cateTv;
            sizeTv=binding.sizeTv;
            dateTv=binding.dateTv;
            moreBtn=binding.moreBtn;
        }
    }
}
