package com.example.project;

import static com.example.project.Constant.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static final String formatTimestamp(long timestamp){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date= DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }
    public static void delBook(Context context,String bookId,String bookUrl,String bookTitle) {
        String TAG="DELETE_BOOK_TAG";


        Log.d(TAG, "delBook: Đang xóa...");
        ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setMessage("Đang xóa "+bookTitle+"...");
        progressDialog.show();

        Log.d(TAG, "delBook: Đang xóa từ kho...");
        StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Xóa từ kho");

                        Log.d(TAG, "onSuccess: Đang xóa từ db");
                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Đã xóa từ db");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xóa sách thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Xóa không thành công");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xóa không thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Xóa không thành công");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Xóa sách từ kho không thành công!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG="PDF_SIZE_TAG";
        //dùng url get dât

        StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes=storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+" "+bytes);

                        //convert sang KB,Mb;
                        double kb=bytes/1024;
                        double mb=kb/1024;

                        if(mb>=1){
                            sizeTv.setText(String.format("%.1f",mb)+" MB");
                        }else if (kb>=1){
                            sizeTv.setText(String.format("%.1f",kb)+" KB");
                        }else
                            sizeTv.setText(String.format("%.0f",bytes)+" bytes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }

    public static void loadPdfFromUrl(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar bar,TextView pagesTv) {
        String TAG="PDF_LOAD_SINGLE_TAG";

        StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+" successfully got the file!");

                        //set pdf view
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        bar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        bar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        bar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: PDF loaded!");

                                        if (pagesTv!=null){
                                            pagesTv.setText(""+nbPages);
                                        }
                                    }
                                }).load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: failed getting file from url due to "+e.getMessage());
                    }
                });
    }

    public static void loadCategory(String cateId, TextView cateTv) {
        //lấy category từ id


        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(cateId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get cate
                        String cate=""+snapshot.child("category").getValue();
                        cateTv.setText(cate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void incrementViewsCount(String bookId){
        //get lượt đọc
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewc=""+snapshot.child("viewsCount").getValue();
                        if (viewc.equals("") || viewc.equals("null")){
                            viewc="0";
                        }

                        //increment lượt đọc
                        long newViewCount=Long.parseLong(viewc)+1;
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("viewsCount",newViewCount);

                        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Books");
                        reference1.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void downloadBook(Context context,String bookId,String bookTitle,String bookUrl){
        String namedown=bookTitle+".pdf";

        ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Xin đợi...");
        progressDialog.setMessage("Đang tải file xuống...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        StorageReference storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        saveDownloadBook(context,progressDialog,bytes,namedown,bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Tải file xuống thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void saveDownloadBook(Context context, ProgressDialog progressDialog, byte[] bytes, String namedown, String bookId) {
        try{
            File filedown= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            filedown.mkdirs();
            
            String filepath=filedown.getPath()+"/"+namedown;

            FileOutputStream out=new FileOutputStream(filepath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Đã lưu vào thư mục!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

            incrementDownCount(bookId);
        }catch (Exception e){
            Toast.makeText(context, "Tải file xuống thất bại!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementDownCount(String bookId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downCount=""+snapshot.child("downloadsCount").getValue();

                        if (downCount.equals("")||downCount.equals("null")){
                            downCount="0";
                        }

                        //convert sang long
                        long newDownCount=Long.parseLong(downCount)+1;

                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("downloadsCount",newDownCount);

                        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Books");
                        reference1.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


//    public static void loadPdfPageCount(Context context, String pdfUrl,TextView pagesTv){
//        StorageReference storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
//        storageReference.getBytes(MAX_BYTES_PDF)
//                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        PDFView pdfView=new PDFView(context,null);
//                        pdfView.fromBytes(bytes).onLoad(new OnLoadCompleteListener() {
//                            @Override
//                            public void loadComplete(int nbPages) {
//                                //pdf đã load được từ storage
//                                pagesTv.setText(""+nbPages);
//                            }
//                        });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
//    }




    public static void addToFavorite(Context context,String bookId){
        //chỉ có thể thêm vào mục ưa thích nếu người dùng đăng nhập
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()==null){
            //không đăng nhập
            Toast.makeText(context, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
        }else {
//            String timestamp=""+System.currentTimeMillis();

            //setup data vào firebase db của người dùng hiện tại cho mục ưa thích
            HashMap<String,Object> hashMap=new HashMap<>();
//            hashMap.put("bookId",""+bookId);
            hashMap.put("id",""+firebaseAuth.getUid());
            hashMap.put("bookId",""+bookId);
//            hashMap.put("uid",""+firebaseAuth.getUid());

//            hashMap.put("timestamp",""+timestamp);

            //lưu vào db
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
            reference.child(bookId).child("Favorites").child(firebaseAuth.getUid())
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Đã thêm vào mục ưa thích!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Thêm vào mục ưa thích thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context,String bookId){
        //chỉ có thể thêm vào mục ưa thích nếu người dùng đăng nhập
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()==null){
            //không đăng nhập
            Toast.makeText(context, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
        }else {

            //xóa khỏi db
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
            reference.child(bookId).child("Favorites").child(firebaseAuth.getUid())
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Đã xóa sách khỏi mục ưa thích!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Xóa khỏi mục ưa thích thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


}
