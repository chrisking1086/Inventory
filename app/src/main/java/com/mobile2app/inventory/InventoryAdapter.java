package com.mobile2app.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private OnItemClickListener listener;
    private List<InventoryItem> inventoryList;
    private DatabaseHelper db;


    public InventoryAdapter(List<InventoryItem> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public InventoryAdapter(List<InventoryItem> inventoryList, DatabaseHelper db) {
        this.inventoryList = inventoryList;
        this.db = db;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);

        void onDeleteClick(InventoryItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventory_item, parent, false);
        return new InventoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryAdapter.InventoryViewHolder holder, int position) {
        InventoryItem currentItem = inventoryList.get(position);

        holder.inventoryItemName.setText(currentItem.getItemName());
        holder.inventoryItemDescription.setText(currentItem.getItemDescription());
        holder.itemQuantity.setText(String.valueOf(currentItem.getItemQuantity()));

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        public TextView inventoryItemName;
        public TextView inventoryItemDescription;
        public TextView itemQuantity;
        public ImageButton deleteButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            inventoryItemName = itemView.findViewById(R.id.inventoryItemName);
            inventoryItemDescription = itemView.findViewById(R.id.inventoryItemDescription);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public void setInventoryList(List<InventoryItem> inventoryList) {
        this.inventoryList = inventoryList;
        notifyDataSetChanged();
    }

    public void deleteItem(InventoryItem item) {

        if (item  != null) {
            db.deleteInventory(item.getItemName());

            int position = inventoryList.indexOf(item);
            if (position != -1){
                inventoryList.remove(position);
                notifyItemRemoved(position);

            }
        }
    }
}