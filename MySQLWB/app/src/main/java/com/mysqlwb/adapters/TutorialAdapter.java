package com.mysqlwb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mysqlwb.R;
import com.mysqlwb.models.Tutorial;

import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    public interface OnTutorialClickListener {
        void onClick(Tutorial tutorial);
    }

    private final Context context;
    private final List<Tutorial> data;
    private final OnTutorialClickListener listener;

    public TutorialAdapter(Context ctx, List<Tutorial> data, OnTutorialClickListener listener) {
        this.context  = ctx;
        this.data     = data;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_tutorial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Tutorial t = data.get(pos);
        h.tvIcon.setText(t.getIcon());
        h.tvTitle.setText(t.getTitle());
        h.tvDescription.setText(t.getDescription());
        h.tvCategory.setText(t.getCategory());

        h.tvDifficulty.setText(t.getDifficulty());
        int diffColor;
        switch (t.getDifficulty()) {
            case "Intermediate": diffColor = 0xFFF39C12; break;
            case "Advanced":     diffColor = 0xFFE74C3C; break;
            default:             diffColor = 0xFF2ECC71; break;
        }
        h.tvDifficulty.setTextColor(diffColor);

        h.itemView.setOnClickListener(v -> listener.onClick(t));
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvDescription, tvCategory, tvDifficulty;

        ViewHolder(View v) {
            super(v);
            tvIcon        = v.findViewById(R.id.tv_icon);
            tvTitle       = v.findViewById(R.id.tv_title);
            tvDescription = v.findViewById(R.id.tv_description);
            tvCategory    = v.findViewById(R.id.tv_category);
            tvDifficulty  = v.findViewById(R.id.tv_difficulty);
        }
    }
}
