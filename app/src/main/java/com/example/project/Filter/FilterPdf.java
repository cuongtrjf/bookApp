package com.example.project.Filter;

import android.widget.Filter;

import com.example.project.adapter.AdapterPdf;
import com.example.project.model.Pdf;

import java.util.ArrayList;

public class FilterPdf extends Filter {
    ArrayList<Pdf> list;

    AdapterPdf adapterPdf;

    public FilterPdf(ArrayList<Pdf> list, AdapterPdf adapterPdf) {
        this.list = list;
        this.adapterPdf = adapterPdf;
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
        adapterPdf.list=(ArrayList<Pdf>) filterResults.values;

        //notify data change
        adapterPdf.notifyDataSetChanged();
    }
}
