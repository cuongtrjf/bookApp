package com.example.project.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.example.project.MyApplication;
import com.example.project.activities.PdfDetailActivity;
import com.example.project.activities.PdfEditActivity;
import com.example.project.databinding.PdfItemAdminBinding;
import com.example.project.model.Pdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdf extends RecyclerView.Adapter<AdapterPdf.PdfViewHolder> implements Filterable {
    private Context context;
    public ArrayList<Pdf> list,filterList;
    private FilterPdf filterPdf;

    private PdfItemAdminBinding binding;
    private ProgressDialog progressDialog;
    private static final String TAG="PDF_ADAPTER_TAG";

    public AdapterPdf(Context context, ArrayList<Pdf> list) {
        this.context = context;
        this.list = list;
        this.filterList=list;

        progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=PdfItemAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new PdfViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        //get data
        Pdf pdf=list.get(position);
        String title=pdf.getTitle();
        String desc=pdf.getDesc();
        String pdfId=pdf.getId();
        String pdfUrl=pdf.getUrl();
        String cateId=pdf.getCategoryId();

        long timestamp=pdf.getTimestamp();
        String dateformat= MyApplication.formatTimestamp(timestamp);//convert time từ long sang string


        //set data
        holder.titleTv.setText(title);
        holder.descTv.setText(desc);
        holder.dateTv.setText(dateformat);

        //load
        MyApplication.loadCategory(""+cateId,holder.cateTv);
        MyApplication.loadPdfFromUrl(""+pdfUrl,""+title,holder.pdfView,holder.bar,null);
        MyApplication.loadPdfSize(""+pdfUrl,""+title,holder.sizeTv);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(pdf,holder);
            }
        });


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

    private void moreOptionsDialog(Pdf pdf, PdfViewHolder holder) {
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


    //xóa sách








    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filterPdf==null){
            filterPdf=new FilterPdf(filterList,this);
        }
        return filterPdf;
    }


    //viewholder
    class PdfViewHolder extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar bar;
        TextView titleTv,descTv,cateTv,sizeTv,dateTv;
        ImageButton moreBtn;
        public PdfViewHolder(@NonNull View itemView) {
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
