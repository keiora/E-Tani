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

    public HarvestAdapter(List<HarvestListActivity.HarvestData> harvestList) {
        this.harvestList = harvestList;
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
        
        holder.jenisText.setText(harvest.getJenis());
        holder.jumlahText.setText(harvest.getJumlah() + " " + harvest.getSatuan());
        holder.tanggalText.setText(harvest.getTanggal());
        holder.statusText.setText(getStatusText(harvest.getStatus()));
        holder.statusText.setTextColor(getStatusColor(harvest.getStatus()));
        holder.createdAtText.setText(harvest.getCreatedAt());
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
