package com.example.project.Filter;

import android.widget.Filter;

import com.example.project.adapter.AdapterUser;
import com.example.project.model.User;

import java.util.ArrayList;

public class FilterUser extends Filter {
    ArrayList<User> listUser;
    //adapter
    AdapterUser adapterUser;

    public FilterUser(ArrayList<User> listUser, AdapterUser adapterUser) {
        this.listUser = listUser;
        this.adapterUser = adapterUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results= new FilterResults();
        if(charSequence!=null && charSequence.length()>0){
            charSequence=charSequence.toString().toLowerCase();
            ArrayList<User> filterList= new ArrayList<>();
            for(int i=0;i< listUser.size();i++){
                if((listUser.get(i).getEmail().toLowerCase().contains(charSequence)))
                    filterList.add(listUser.get(i));
            }

            results.count=filterList.size();
            results.values=filterList;
        }else {
            results.count=listUser.size();
            results.values=listUser;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapterUser.list=(ArrayList<User>) filterResults.values;
        adapterUser.notifyDataSetChanged();
    }
}
