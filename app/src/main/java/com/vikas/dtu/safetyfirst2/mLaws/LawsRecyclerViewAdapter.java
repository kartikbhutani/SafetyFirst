package com.vikas.dtu.safetyfirst2.mLaws;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vikas.dtu.safetyfirst2.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by DHEERAJ on 21-12-2016.
 */

public class LawsRecyclerViewAdapter extends RecyclerView.Adapter<LawsRecyclerViewAdapter.MyViewHolder>  {
    public interface OnItemClickListener{
        void onItemClick(View v,int position);
    }
    LayoutInflater inflater;
    ArrayList<StateLawsRowInfo> rowInfos;
    private final OnItemClickListener listener;
    public LawsRecyclerViewAdapter(Context context, ArrayList<StateLawsRowInfo> rowInfos, OnItemClickListener listener){
        inflater=LayoutInflater.from(context);
        this.rowInfos=rowInfos;
        this.listener=listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.laws_recyclerview_row,parent,false);
        MyViewHolder viewHolder= new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        StateLawsRowInfo rowInfo=rowInfos.get(position);
        holder.textView.setText(rowInfo.text);
        holder.imageView.setBackground(rowInfo.backgroundid);
    }

    @Override
    public int getItemCount() {
        return rowInfos.size();
    }



    public void insert(int position, StateLawsRowInfo rowInfo){
        rowInfos.add(position,rowInfo);
        notifyItemInserted(position);
    }
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView;
        ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            textView=(TextView)itemView.findViewById(R.id.centre_text);
            imageView=(ImageView) itemView.findViewById(R.id.back_Image);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            listener.onItemClick(v,this.getLayoutPosition());
        }
    }
}
