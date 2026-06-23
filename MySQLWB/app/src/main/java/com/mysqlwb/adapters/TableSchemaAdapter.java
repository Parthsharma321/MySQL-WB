package com.mysqlwb.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mysqlwb.R;
import com.mysqlwb.models.ColumnInfo;
import com.mysqlwb.models.TableInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableSchemaAdapter extends RecyclerView.Adapter<TableSchemaAdapter.ViewHolder> {

    public interface OnTableClickListener {
        void onClick(TableInfo table);
    }

    private final Context context;
    private List<TableInfo> tables;
    private Map<String, List<ColumnInfo>> columnsMap;
    private String selectedTable = null;
    private final OnTableClickListener listener;

    public TableSchemaAdapter(Context ctx, List<TableInfo> tables,
                              Map<String, List<ColumnInfo>> columnsMap,
                              OnTableClickListener listener) {
        this.context    = ctx;
        this.tables     = tables;
        this.columnsMap = columnsMap;
        this.listener   = listener;
    }

    public void updateData(List<TableInfo> tables, Map<String, List<ColumnInfo>> columnsMap) {
        this.tables     = tables;
        this.columnsMap = columnsMap;
        notifyDataSetChanged();
    }

    public void filter(String query) {
        // filtering handled by activity re-querying
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_table_schema, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        TableInfo table = tables.get(pos);
        boolean expanded = table.getName().equals(selectedTable);

        h.tvTableName.setText("📋  " + table.getName());
        h.tvRowCount.setText(table.getRowCount() + " rows");
        h.tvTableName.setTypeface(null, expanded ? Typeface.BOLD : Typeface.NORMAL);

        // Expand/collapse columns
        h.layoutColumns.removeAllViews();
        if (expanded && columnsMap.containsKey(table.getName())) {
            h.layoutColumns.setVisibility(View.VISIBLE);
            List<ColumnInfo> cols = columnsMap.get(table.getName());
            for (ColumnInfo col : cols) {
                View row = buildColumnRow(col);
                h.layoutColumns.addView(row);
            }
        } else {
            h.layoutColumns.setVisibility(View.GONE);
        }

        h.card.setCardBackgroundColor(expanded
                ? ContextCompat.getColor(context, R.color.surface_variant)
                : ContextCompat.getColor(context, R.color.surface));

        h.itemView.setOnClickListener(v -> {
            selectedTable = expanded ? null : table.getName();
            listener.onClick(table);
            notifyDataSetChanged();
        });
    }

    private View buildColumnRow(ColumnInfo col) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(20), dpToPx(6), dpToPx(12), dpToPx(6));

        // Key icon
        TextView tvKey = new TextView(context);
        tvKey.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(22), ViewGroup.LayoutParams.WRAP_CONTENT));
        tvKey.setText(col.isPrimaryKey() ? "🔑" : "·");
        tvKey.setTextSize(12f);
        row.addView(tvKey);

        // Column name
        TextView tvName = new TextView(context);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tvName.setText(col.getName());
        tvName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        tvName.setTextSize(13f);
        tvName.setTypeface(Typeface.MONOSPACE);
        if (col.isPrimaryKey()) tvName.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        row.addView(tvName);

        // Type badge
        TextView tvType = new TextView(context);
        tvType.setText(col.getType());
        tvType.setTextColor(ContextCompat.getColor(context, R.color.syntax_keyword));
        tvType.setTextSize(11f);
        tvType.setTypeface(Typeface.MONOSPACE);
        row.addView(tvType);

        // Constraints
        if (col.isNotNull()) {
            TextView tvNN = new TextView(context);
            tvNN.setText("  NN");
            tvNN.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
            tvNN.setTextSize(10f);
            row.addView(tvNN);
        }

        return row;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    @Override public int getItemCount() { return tables.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvTableName, tvRowCount;
        LinearLayout layoutColumns;

        ViewHolder(View v) {
            super(v);
            card          = v.findViewById(R.id.card_table);
            tvTableName   = v.findViewById(R.id.tv_table_name);
            tvRowCount    = v.findViewById(R.id.tv_row_count);
            layoutColumns = v.findViewById(R.id.layout_columns);
        }
    }
}
