package com.example.project.Filter;

import android.widget.Filter;


import com.example.project.adapter.AdapterPdfUser;
import com.example.project.model.Pdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    ArrayList<Pdf> list;

    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<Pdf> list, AdapterPdfUser adapterPdf) {
        this.list = list;
        this.adapterPdfUser = adapterPdf;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results=new FilterResults();
        if (charSequence!=null && charSequence.length()>0){
            //neu co du lieu tu text tim kiem thi se ra list moi
            charSequence = charSequence.toString().toLowerCase();
            ArrayList<Pdf> filterList= new ArrayList<>();
            for(int i=0;i<list.size();i++){
                if (list.get(i).getTitle().toLowerCase().contains(charSequence)){
                    filterList.add(list.get(i));
                }
            }

            results.count=filterList.size();
            results.values=filterList;
        }else{
            //neu khong thi se tra ve list mac dinh
            results.count=list.size();
            results.values=list;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //apply change
        adapterPdfUser.list=(ArrayList<Pdf>) filterResults.values;

        //notify data change
        adapterPdfUser.notifyDataSetChanged();
    }
}
