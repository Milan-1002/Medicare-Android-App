package com.medicare.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medicare.app.R;

import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private List<String> timesList;
    private OnTimeRemoveListener listener;

    public interface OnTimeRemoveListener {
        void onTimeRemove(int position);
    }

    public TimeAdapter(List<String> timesList, OnTimeRemoveListener listener) {
        this.timesList = timesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timesList.get(position);
        holder.tvTime.setText(time);
        
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTimeRemove(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return timesList.size();
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        ImageButton btnRemove;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnRemove = itemView.findViewById(R.id.btn_remove_time);
        }
    }
}