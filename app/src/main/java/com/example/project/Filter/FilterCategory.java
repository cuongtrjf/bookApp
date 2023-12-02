package com.example.project.Filter;

import android.widget.Filter;

import com.example.project.adapter.AdapterCategory;
import com.example.project.model.Category;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    ArrayList<Category> list;

    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<Category> list, AdapterCategory adapterCategory) {
        this.list = list;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results=new FilterResults();
        if (charSequence!=null && charSequence.length()>0){
            //neu co du lieu tu text tim kiem thi se ra list moi
            charSequence = charSequence.toString().toLowerCase();
            ArrayList<Category> filterList= new ArrayList<>();
            for(int i=0;i<list.size();i++){
                if (list.get(i).getCategory().toLowerCase().contains(charSequence)){
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
        adapterCategory.list=(ArrayList<Category>) filterResults.values;

        //notify data change
        adapterCategory.notifyDataSetChanged();
    }
}
