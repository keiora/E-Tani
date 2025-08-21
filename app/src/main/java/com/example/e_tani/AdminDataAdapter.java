package com.example.e_tani;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminDataAdapter extends RecyclerView.Adapter<AdminDataAdapter.ViewHolder> {

    private List<Harvest> harvestList;
    private OnAdminActionListener adminActionListener;

    public interface OnAdminActionListener {
        void onDone(Harvest harvest);
        void onReject(Harvest harvest);
    }

    public AdminDataAdapter(List<Harvest> harvestList) {
        this.harvestList = harvestList;
    }

    public void setOnAdminActionListener(OnAdminActionListener listener) {
        this.adminActionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Harvest harvest = harvestList.get(position);
        String status = harvest.getStatus();
        if (status != null && status.equalsIgnoreCase("rejected")) status = "reject";

        holder.tvJenis.setText("Jenis: " + valueOrDash(harvest.getJenis()));
        holder.tvJumlah.setText("Jumlah: " + valueOrDash(harvest.getJumlah()));
        holder.tvSatuan.setText("Satuan: " + valueOrDash(harvest.getSatuan()));
        holder.tvTanggal.setText("Tanggal: " + valueOrDash(harvest.getTanggal()));
        holder.tvStatus.setText("Status: " + getStatusText(status));
        holder.tvStatus.setTextColor(getStatusColor(status));

        // user info & additional fields
        String userInfo = valueOrDash(harvest.getUserId()) + " (" + valueOrDash(harvest.getNamaPetani()) + ")";
        if (holder.tvUser != null) holder.tvUser.setText("User: " + userInfo);
        if (holder.tvKualitas != null) holder.tvKualitas.setText("Kualitas: " + valueOrDash(harvest.getKualitas()));
        if (holder.tvLokasi != null) holder.tvLokasi.setText("Lokasi: " + valueOrDash(harvest.getLokasiLahan()));
        if (holder.tvLuasLahan != null) holder.tvLuasLahan.setText("Luas Lahan: " + valueOrDash(harvest.getLuasLahan()));
        if (holder.tvMusim != null) holder.tvMusim.setText("Musim: " + valueOrDash(harvest.getMusim()));
        if (holder.tvHarga != null) holder.tvHarga.setText("Harga Jual: " + valueOrDash(harvest.getHargaJual()));
        if (holder.tvCatatan != null) holder.tvCatatan.setText("Catatan: " + valueOrDash(harvest.getCatatan()));
        if (holder.tvCreatedAt != null) holder.tvCreatedAt.setText("Dibuat: " + valueOrDash(harvest.getCreatedAt()));

        // Buttons: Done/Reject
        if (holder.btnDone != null) {
            holder.btnDone.setOnClickListener(v -> {
                if (adminActionListener != null) adminActionListener.onDone(harvest);
            });
        }
        if (holder.btnReject != null) {
            holder.btnReject.setOnClickListener(v -> {
                if (adminActionListener != null) adminActionListener.onReject(harvest);
            });
        }
    }

    private String valueOrDash(String s) {
        return s == null || s.isEmpty() ? "-" : s;
    }

    private String getStatusText(String status) {
        if (status == null || status.isEmpty()) return "-";
        switch (status) {
            case "done": return "Disetujui";
            case "reject": return "Ditolak";
            case "waiting": return "Menunggu";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null || status.isEmpty()) return 0xFF666666;
        switch (status) {
            case "done": return 0xFF4CAF50;
            case "reject": return 0xFFF44336;
            case "waiting": return 0xFFFF9800;
            default: return 0xFF666666;
        }
    }

    @Override
    public int getItemCount() {
        return harvestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJenis, tvJumlah, tvSatuan, tvStatus, tvTanggal;
        TextView tvUser, tvKualitas, tvLokasi, tvLuasLahan, tvMusim, tvHarga, tvCatatan, tvCreatedAt;
        Button btnDone, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJenis = itemView.findViewById(R.id.tvJenis);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            tvSatuan = itemView.findViewById(R.id.tvSatuan);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvKualitas = itemView.findViewById(R.id.tvKualitas);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            tvLuasLahan = itemView.findViewById(R.id.tvLuasLahan);
            tvMusim = itemView.findViewById(R.id.tvMusim);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvCatatan = itemView.findViewById(R.id.tvCatatan);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnDone = itemView.findViewById(R.id.btnDone);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
