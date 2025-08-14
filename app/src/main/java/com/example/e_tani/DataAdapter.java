package com.example.e_tani;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private List<DataModel> dataList;

    public DataAdapter(List<DataModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        DataModel data = dataList.get(position);
        
        holder.jenisTanamanText.setText(data.getJenisTanaman());
        holder.jumlahText.setText("Jumlah: " + data.getJumlah());
        holder.satuanText.setText("Satuan: " + data.getSatuan());
        holder.tanggalText.setText("Tanggal: " + data.getTanggal());
        
        // Tampilkan gambar jika ada
        if (data.getFotoBase64() != null && !data.getFotoBase64().isEmpty()) {
            try {
                byte[] imageBytes = android.util.Base64.decode(data.getFotoBase64(), android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.tanamanImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.tanamanImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.tanamanImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<DataModel> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView jenisTanamanText, jumlahText, satuanText, tanggalText;
        ImageView tanamanImage;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            jenisTanamanText = itemView.findViewById(R.id.jenisTanamanText);
            jumlahText = itemView.findViewById(R.id.jumlahText);
            satuanText = itemView.findViewById(R.id.satuanText);
            tanggalText = itemView.findViewById(R.id.tanggalText);
            tanamanImage = itemView.findViewById(R.id.tanamanImage);
        }
    }
}
