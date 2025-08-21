package com.example.e_tani;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.HarvestViewHolder> {

    private List<HarvestListActivity.HarvestData> harvestList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HarvestListActivity.HarvestData harvest);
        void onEditClick(HarvestListActivity.HarvestData harvest);
        void onDeleteClick(HarvestListActivity.HarvestData harvest);
    }

    public interface OnAdminActionListener {
        void onDone(HarvestListActivity.HarvestData harvest, String feedback);
        void onReject(HarvestListActivity.HarvestData harvest, String feedback);
    }

    private OnAdminActionListener adminActionListener;
    private boolean isAdminMode = false;

    public HarvestAdapter(List<HarvestListActivity.HarvestData> harvestList) {
        this.harvestList = harvestList;
    }

    public HarvestAdapter(List<HarvestListActivity.HarvestData> harvestList, OnItemClickListener listener) {
        this.harvestList = harvestList;
        this.listener = listener;
    }

    public void setAdminActionListener(OnAdminActionListener listener) {
        this.adminActionListener = listener;
    }
    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
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
        String jenis = harvest.getJenis();
        if (jenis == null || jenis.isEmpty()) {
            jenis = "Tidak ada data";
        }
        holder.jenisText.setText(jenis);
        
        // Tampilkan jumlah dengan format yang jelas (contoh: "100 kg")
        String jumlah = harvest.getJumlah();
        String satuan = harvest.getSatuan();
        if (jumlah == null || jumlah.isEmpty()) {
            jumlah = "Tidak ada data";
        }
        if (satuan == null || satuan.isEmpty()) {
            satuan = "";
        }
        String jumlahText = jumlah + " " + satuan;
        holder.jumlahText.setText(jumlahText);
        
        // Tampilkan tanggal dengan format yang jelas
        String tanggal = harvest.getTanggal();
        if (tanggal == null || tanggal.isEmpty()) {
            tanggal = "Tidak ada data";
        }
        holder.tanggalText.setText("Tanggal: " + tanggal);
        
        // Tampilkan status dengan format yang jelas
        holder.statusText.setText(getStatusText(harvest.getStatus()));
        holder.statusText.setTextColor(getStatusColor(harvest.getStatus()));
        
        // Tampilkan waktu pembuatan dengan format yang jelas
        String createdAt = harvest.getCreatedAt();
        if (createdAt == null || createdAt.isEmpty()) {
            createdAt = "Tidak ada data";
        }
        holder.createdAtText.setText("Dibuat: " + createdAt);
        
        // Tambahkan click listener untuk card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(harvest);
            }
        });
        
        // Set click listener untuk tombol edit dan hapus
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(harvest);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(harvest);
            }
        });
        
        // Tampilkan/sembunyikan tombol berdasarkan status
        if ("waiting".equals(harvest.getStatus())) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Tampilkan tombol jika admin & status waiting
        if (isAdminMode && "waiting".equals(harvest.getStatus())) {
            holder.adminActionLayout.setVisibility(View.VISIBLE);

            holder.btnDone.setOnClickListener(v -> {
                String feedback = holder.feedbackEditText.getText().toString();
                if (adminActionListener != null) {
                    adminActionListener.onDone(harvest, feedback);
                }
            });
            holder.btnReject.setOnClickListener(v -> {
                String feedback = holder.feedbackEditText.getText().toString();
                if (adminActionListener != null) {
                    adminActionListener.onReject(harvest, feedback);
                }
            });
        } else {
            holder.adminActionLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return harvestList.size();
    }

    private String getStatusText(String status) {
        if (status == null || status.isEmpty()) {
            return "Tidak ada status";
        }
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
        if (status == null || status.isEmpty()) {
            return 0xFF666666; // Gray
        }
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
        ImageView editButton, deleteButton;
        LinearLayout adminActionLayout;
        EditText feedbackEditText;
        Button btnDone, btnReject;

        HarvestViewHolder(View itemView) {
            super(itemView);
            jenisText = itemView.findViewById(R.id.jenisText);
            jumlahText = itemView.findViewById(R.id.jumlahText);
            tanggalText = itemView.findViewById(R.id.tanggalText);
            statusText = itemView.findViewById(R.id.statusText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            adminActionLayout = itemView.findViewById(R.id.adminActionLayout);
            feedbackEditText = itemView.findViewById(R.id.feedbackEditText);
            btnDone = itemView.findViewById(R.id.btnDone);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
