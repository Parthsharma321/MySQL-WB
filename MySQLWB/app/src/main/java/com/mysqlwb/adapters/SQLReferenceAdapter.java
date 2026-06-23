package com.mysqlwb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mysqlwb.R;
import com.mysqlwb.models.SQLCommand;

import java.util.ArrayList;
import java.util.List;

public class SQLReferenceAdapter extends RecyclerView.Adapter<SQLReferenceAdapter.ViewHolder> {

    private final Context context;
    private List<SQLCommand> data;
    private final List<SQLCommand> fullData;

    public SQLReferenceAdapter(Context ctx, List<SQLCommand> data) {
        this.context  = ctx;
        this.data     = new ArrayList<>(data);
        this.fullData = new ArrayList<>(data);
    }

    public void setData(List<SQLCommand> newData) {
        this.data = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    public void filter(String query, String category) {
        data.clear();
        for (SQLCommand cmd : fullData) {
            boolean matchesQuery    = query.isEmpty()
                    || cmd.getCommand().toUpperCase().contains(query.toUpperCase())
                    || cmd.getDescription().toLowerCase().contains(query.toLowerCase());
            boolean matchesCategory = category.equals("ALL")
                    || cmd.getCategory().equalsIgnoreCase(category);
            if (matchesQuery && matchesCategory) data.add(cmd);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_sql_command, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        SQLCommand cmd = data.get(pos);
        h.tvCommand.setText(cmd.getCommand());
        h.tvCategory.setText(cmd.getCategory());
        h.tvDescription.setText(cmd.getDescription());
        h.tvSyntax.setText(cmd.getSyntax());

        int color;
        switch (cmd.getCategory()) {
            case "DDL":       color = 0xFF1A6FAB; break;
            case "DML":       color = 0xFFE8831A; break;
            case "DQL":       color = 0xFF27AE60; break;
            case "DCL":       color = 0xFF8E44AD; break;
            case "TCL":       color = 0xFF16A085; break;
            case "Functions": color = 0xFFE74C3C; break;
            default:          color = 0xFF607D8B; break;
        }
        h.tvCategory.setTextColor(color);
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommand, tvCategory, tvDescription, tvSyntax;
        ViewHolder(View v) {
            super(v);
            tvCommand     = v.findViewById(R.id.tv_command);
            tvCategory    = v.findViewById(R.id.tv_category);
            tvDescription = v.findViewById(R.id.tv_description);
            tvSyntax      = v.findViewById(R.id.tv_syntax);
        }
    }
}
