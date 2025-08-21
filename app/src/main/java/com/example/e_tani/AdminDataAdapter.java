package com.example.e_tani;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminDataAdapter extends RecyclerView.Adapter<AdminDataAdapter.ViewHolder> {

    private List<Harvest> harvestList;

    public AdminDataAdapter(List<Harvest> harvestList) {
        this.harvestList = harvestList;
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
        holder.tvJenis.setText("Jenis: " + harvest.getJenis());
        holder.tvJumlah.setText("Jumlah: " + harvest.getJumlah());
        holder.tvSatuan.setText("Satuan: " + harvest.getSatuan());
        holder.tvStatus.setText("Status: " + harvest.getStatus());
        holder.tvTanggal.setText("Tanggal: " + harvest.getTanggal());
    }

    @Override
    public int getItemCount() {
        return harvestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJenis, tvJumlah, tvSatuan, tvStatus, tvTanggal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJenis = itemView.findViewById(R.id.tvJenis);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            tvSatuan = itemView.findViewById(R.id.tvSatuan);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
        }
    }
}
