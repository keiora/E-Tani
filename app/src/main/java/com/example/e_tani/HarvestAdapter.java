package com.example.e_tani;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.HarvestViewHolder> {

    private List<HarvestListActivity.HarvestData> harvestList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HarvestListActivity.HarvestData harvest);
    }

    public HarvestAdapter(List<HarvestListActivity.HarvestData> harvestList) {
        this.harvestList = harvestList;
    }

    public HarvestAdapter(List<HarvestListActivity.HarvestData> harvestList, OnItemClickListener listener) {
        this.harvestList = harvestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HarvestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_harvest, parent, false);
        return new HarvestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HarvestViewHolder holder, int position) {
        HarvestListActivity.HarvestData harvest = harvestList.get(position);
        
        // Tampilkan jenis tanaman dengan format yang jelas
        holder.jenisText.setText(harvest.getJenis());
        
        // Tampilkan jumlah dengan format yang jelas (contoh: "100 kg")
        String jumlahText = harvest.getJumlah() + " " + harvest.getSatuan();
        holder.jumlahText.setText(jumlahText);
        
        // Tampilkan tanggal dengan format yang jelas
        holder.tanggalText.setText("Tanggal: " + harvest.getTanggal());
        
        // Tampilkan status dengan format yang jelas
        holder.statusText.setText(getStatusText(harvest.getStatus()));
        holder.statusText.setTextColor(getStatusColor(harvest.getStatus()));
        
        // Tampilkan waktu pembuatan dengan format yang jelas
        holder.createdAtText.setText("Dibuat: " + harvest.getCreatedAt());
        
        // Tambahkan click listener untuk card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(harvest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return harvestList.size();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "done":
                return "Disetujui";
            case "reject":
                return "Ditolak";
            case "waiting":
                return "Menunggu";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "done":
                return 0xFF4CAF50; // Green
            case "reject":
                return 0xFFF44336; // Red
            case "waiting":
                return 0xFFFF9800; // Orange
            default:
                return 0xFF666666; // Gray
        }
    }

    static class HarvestViewHolder extends RecyclerView.ViewHolder {
        TextView jenisText, jumlahText, tanggalText, statusText, createdAtText;

        HarvestViewHolder(View itemView) {
            super(itemView);
            jenisText = itemView.findViewById(R.id.jenisText);
            jumlahText = itemView.findViewById(R.id.jumlahText);
            tanggalText = itemView.findViewById(R.id.tanggalText);
            statusText = itemView.findViewById(R.id.statusText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
        }
    }
}
