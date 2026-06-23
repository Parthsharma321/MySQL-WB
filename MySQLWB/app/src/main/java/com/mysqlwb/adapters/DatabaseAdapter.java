package com.mysqlwb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mysqlwb.R;
import com.mysqlwb.models.DatabaseInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseAdapter extends RecyclerView.Adapter<DatabaseAdapter.ViewHolder> {

    public interface OnDbClickListener {
        void onClick(DatabaseInfo db);
    }

    public interface OnDbMenuActionListener {
        void onAction(int actionId, DatabaseInfo db);
    }

    private final Context context;
    private List<DatabaseInfo> data = new ArrayList<>();
    private final OnDbClickListener clickListener;
    private final OnDbMenuActionListener menuActionListener;

    public DatabaseAdapter(Context ctx, OnDbClickListener click, OnDbMenuActionListener menuAction) {
        this.context = ctx;
        this.clickListener = click;
        this.menuActionListener = menuAction;
    }

    public void setData(List<DatabaseInfo> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_database, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        DatabaseInfo db = data.get(pos);
        h.tvName.setText(db.getName());
        h.tvSize.setText(db.getFormattedSize());
        h.tvLastModified.setText(formatDate(db.getLastModified()));

        // Derive table count hint from name
        h.tvTableCount.setText("SQLite Database");

        // Color-cycle the card header
        int[] gradients = {
            R.drawable.bg_db_card_blue,
            R.drawable.bg_db_card_orange,
            R.drawable.bg_db_card_green,
            R.drawable.bg_db_card_purple
        };
        h.cardHeader.setBackgroundResource(gradients[pos % gradients.length]);

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(db);
        });

        h.btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, h.btnOptions);
            popup.getMenuInflater().inflate(R.menu.menu_db_options, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (menuActionListener != null) {
                    menuActionListener.onAction(item.getItemId(), db);
                }
                return true;
            });
            popup.show();
        });
        
        // Remove long click listener to prevent any system or old dialogs
        h.itemView.setOnLongClickListener(null);
    }

    @Override public int getItemCount() { return data.size(); }

    private String formatDate(long millis) {
        if (millis == 0) return "Just created";
        long diff = System.currentTimeMillis() - millis;
        if (diff < 60_000) return "Just now";
        if (diff < 3_600_000) return (diff / 60_000) + "m ago";
        if (diff < 86_400_000) return (diff / 3_600_000) + "h ago";
        return new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(millis));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTableCount, tvSize, tvLastModified;
        View cardHeader;
        ImageView btnOptions;

        ViewHolder(View v) {
            super(v);
            tvName         = v.findViewById(R.id.tv_db_name);
            tvTableCount   = v.findViewById(R.id.tv_table_count);
            tvSize         = v.findViewById(R.id.tv_db_size);
            tvLastModified = v.findViewById(R.id.tv_last_modified);
            cardHeader     = v.findViewById(R.id.card_header);
            btnOptions     = v.findViewById(R.id.btn_options);
        }
    }
}
