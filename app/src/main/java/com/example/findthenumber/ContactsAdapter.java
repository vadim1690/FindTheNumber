package com.example.findthenumber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.zip.Inflater;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<Name> mDataSet;


    public ContactsAdapter(List<Name> mDataSet) {
        this.mDataSet = mDataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name_textView.setText(" Contact name : " +  mDataSet.get(position).getContactName());
        holder.caller_textView.setText(" Caller name : " + mDataSet.get(position).getCallerName());
    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView name_textView,caller_textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name_textView = itemView.findViewById(R.id.name_textView);
            caller_textView = itemView.findViewById(R.id.caller_textView);
        }
    }
}
