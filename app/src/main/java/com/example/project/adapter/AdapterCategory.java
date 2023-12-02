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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Filter.FilterCategory;
import com.example.project.MyApplication;
import com.example.project.activities.PdfListAdminActivity;
import com.example.project.databinding.CateItemBinding;
import com.example.project.model.Category;
import com.example.project.model.Pdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.CategoryViewHolder>
        implements Filterable {

    private Context context;
    public ArrayList<Category> list,filterlist;


    //binding
    private CateItemBinding binding;

    //instance cua class filter
    private FilterCategory filterCategory;

    public AdapterCategory(Context context, ArrayList<Category> list) {
        this.context = context;
        this.list = list;
        this.filterlist=list;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding
        binding=CateItemBinding.inflate(LayoutInflater.from(context),parent,false);
        return new CategoryViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        //get data
        Category category=list.get(position);
        String id= category.getId();
        String cate=category.getCategory();
        String uid=category.getUid();
        long timestamp= category.getTimestamp();

        //set data
        holder.title.setText(cate);

        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Xóa mục sách")
                        .setMessage("Bạn có muốn xóa mục sách này không? Các sách trong mục sách cũng sẽ bị xóa theo.")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context, "Đang xóa...", Toast.LENGTH_SHORT).show();
                                deleteCategory(category,holder);
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

        //bắt sự kiện click vào item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",cate);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(Category category, CategoryViewHolder holder) {
        String id=category.getId();
        //get id của category
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //xoa thanh cong
                        Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                        deleteBookOfCategory(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //xoa that bai
                        Toast.makeText(context, "Có lỗi trong quá trình xóa. Xóa thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private  void deleteBookOfCategory(String cateId){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Pdf pdf= ds.getValue(Pdf.class);
                    if (cateId.equalsIgnoreCase(pdf.getCategoryId())){
                        MyApplication.delBook(context,
                                ""+pdf.getId(),
                                ""+pdf.getUrl(),
                                ""+pdf.getTitle());
                    }
                }
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

    @Override
    public Filter getFilter() {
        if(filterCategory==null){
            filterCategory=new FilterCategory(filterlist,this);
        }
        return filterCategory;
    }


    //viewholder
    class CategoryViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        ImageButton delBtn;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            title=binding.title;
            delBtn=binding.delBtn;
        }
    }
}
