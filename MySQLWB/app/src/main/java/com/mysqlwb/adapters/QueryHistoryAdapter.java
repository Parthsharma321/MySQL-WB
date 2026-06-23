package com.mysqlwb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mysqlwb.R;
import com.mysqlwb.models.QueryHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QueryHistoryAdapter extends RecyclerView.Adapter<QueryHistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onClick(QueryHistory item);
    }

    private final Context context;
    private List<QueryHistory> data = new ArrayList<>();
    private final OnHistoryClickListener listener;

    public QueryHistoryAdapter(Context ctx, OnHistoryClickListener listener) {
        this.context  = ctx;
        this.listener = listener;
    }

    public void setData(List<QueryHistory> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_query_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        QueryHistory item = data.get(pos);

        h.tvStatus.setText(item.isSuccess() ? "✅" : "❌");
        h.tvSql.setText(item.getSql());
        h.tvDatabase.setText(item.getDatabase() != null ? "📊 " + item.getDatabase() : "No DB");
        h.tvTime.setText(formatTimestamp(item.getTimestamp()));

        // Apply font size from preferences
        android.content.SharedPreferences prefs = context.getSharedPreferences("mysqlwb_prefs", Context.MODE_PRIVATE);
        int fontSize = prefs.getInt("editor_font_size", 14);
        h.tvSql.setTextSize(fontSize);

        if (item.getExecutionTime() > 0) {
            h.tvExecTime.setText(item.getExecutionTime() + "ms");
            h.tvExecTime.setVisibility(View.VISIBLE);
        } else {
            h.tvExecTime.setVisibility(View.GONE);
        }

        int sqlColor = item.isSuccess()
                ? ContextCompat.getColor(context, R.color.text_primary)
                : ContextCompat.getColor(context, R.color.error_color);
        h.tvSql.setTextColor(sqlColor);

        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override public int getItemCount() { return data.size(); }

    private String formatTimestamp(long ts) {
        long diff = System.currentTimeMillis() - ts;
        if (diff < 60_000) return "Just now";
        if (diff < 3_600_000) return (diff / 60_000) + "m ago";
        if (diff < 86_400_000) return (diff / 3_600_000) + "h ago";
        return new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(new Date(ts));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvSql, tvDatabase, tvTime, tvExecTime;

        ViewHolder(View v) {
            super(v);
            tvStatus   = v.findViewById(R.id.tv_status_icon);
            tvSql      = v.findViewById(R.id.tv_sql_preview);
            tvDatabase = v.findViewById(R.id.tv_db_name);
            tvTime     = v.findViewById(R.id.tv_timestamp);
            tvExecTime = v.findViewById(R.id.tv_exec_time);
        }
    }
}
