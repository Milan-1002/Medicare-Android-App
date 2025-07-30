package com.medicare.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.medicare.app.R;
import com.medicare.app.models.Medicine;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private Context context;
    private List<Medicine> medicineList;
    private OnMedicineClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnMedicineClickListener {
        void onMedicineClick(Medicine medicine);
        void onDeleteClick(Medicine medicine);
        void onInfoClick(Medicine medicine);
        void onLearnMoreClick(Medicine medicine);
    }

    public MedicineAdapter(Context context, List<Medicine> medicineList, OnMedicineClickListener listener) {
        this.context = context;
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        
        holder.tvMedicineName.setText(medicine.getName());
        holder.tvDosage.setText(medicine.getDosage());
        holder.tvFrequency.setText(medicine.getFrequencyDisplay());
        holder.tvMedicineType.setText(medicine.getMedicineTypeDisplay());
        holder.tvTimes.setText(medicine.getFormattedTimes());
        
        if (medicine.getStartDate() != null) {
            holder.tvStartDate.setText("Started: " + dateFormat.format(medicine.getStartDate()));
        } else {
            holder.tvStartDate.setText("");
        }
        
        if (medicine.getEndDate() != null) {
            holder.tvEndDate.setText("Ends: " + dateFormat.format(medicine.getEndDate()));
            holder.tvEndDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvEndDate.setVisibility(View.GONE);
        }
        
        if (medicine.getNotes() != null && !medicine.getNotes().isEmpty()) {
            holder.tvNotes.setText(medicine.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
        
        // Highlight medicines expiring soon
        if (medicine.isExpiringSoon()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.warning_light));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
        }
        
        // Set click listeners
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onMedicineClick(medicine);
                }
            }
        });
        
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(medicine);
                }
            }
        });
        
        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onInfoClick(medicine);
                }
            }
        });
        
        holder.btnLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onLearnMoreClick(medicine);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMedicineName, tvDosage, tvFrequency, tvMedicineType, tvTimes, tvStartDate, tvEndDate, tvNotes;
        Button btnDelete, btnInfo, btnLearnMore;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_medicine);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvMedicineType = itemView.findViewById(R.id.tv_medicine_type);
            tvTimes = itemView.findViewById(R.id.tv_times);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            tvEndDate = itemView.findViewById(R.id.tv_end_date);
            tvNotes = itemView.findViewById(R.id.tv_notes);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnInfo = itemView.findViewById(R.id.btn_info);
            btnLearnMore = itemView.findViewById(R.id.btn_learn_more);
        }
    }
}