package com.mysqlwb.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.mysqlwb.R;
import com.mysqlwb.adapters.QueryHistoryAdapter;
import com.mysqlwb.database.HistoryManager;
import com.mysqlwb.models.QueryHistory;

import java.util.List;

public class QueryHistoryActivity extends AppCompatActivity {

    private HistoryManager historyManager;
    private QueryHistoryAdapter adapter;
    private RecyclerView recyclerView;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_history);

        historyManager = HistoryManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Query History");
        }

        recyclerView = findViewById(R.id.rv_history);
        layoutEmpty  = findViewById(R.id.layout_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new QueryHistoryAdapter(this, this::onHistoryClick);
        recyclerView.setAdapter(adapter);

        // Search
        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                loadHistory(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        // Clear all
        findViewById(R.id.btn_clear_all).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear History")
                        .setMessage("Delete all query history?")
                        .setPositiveButton("Clear", (d, w) -> {
                            historyManager.clearHistory();
                            loadHistory("");
                            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        loadHistory("");
    }

    private void loadHistory(String query) {
        List<QueryHistory> list = query.isEmpty()
                ? historyManager.getHistory(200, null)
                : historyManager.searchHistory(query);

        adapter.setData(list);

        boolean empty = list.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void onHistoryClick(QueryHistory item) {
        new AlertDialog.Builder(this)
                .setTitle("Use This Query?")
                .setMessage(item.getSql())
                .setPositiveButton("Open in Editor", (d, w) -> {
                    Intent intent = new Intent(this, QueryEditorActivity.class);
                    intent.putExtra("initial_query", item.getSql());
                    if (item.getDatabase() != null)
                        intent.putExtra("db_name", item.getDatabase());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
