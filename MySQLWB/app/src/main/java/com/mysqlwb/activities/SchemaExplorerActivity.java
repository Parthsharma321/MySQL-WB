package com.mysqlwb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.mysqlwb.R;
import com.mysqlwb.adapters.TableSchemaAdapter;
import com.mysqlwb.database.DatabaseEngine;
import com.mysqlwb.models.ColumnInfo;
import com.mysqlwb.models.TableInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaExplorerActivity extends AppCompatActivity {

    private DatabaseEngine dbEngine;
    private String dbName;
    private TableSchemaAdapter adapter;
    private List<TableInfo> allTables = new ArrayList<>();
    private Map<String, List<ColumnInfo>> columnsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schema_explorer);

        dbEngine = DatabaseEngine.getInstance(this);
        dbName   = getIntent().getStringExtra("db_name");
        if (dbName == null) dbName = dbEngine.getCurrentDatabaseName();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Schema: " + (dbName != null ? dbName : "—"));
        }

        RecyclerView rv = findViewById(R.id.rv_schema);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TableSchemaAdapter(this, allTables, columnsMap, table -> {
            // When a table is tapped, open query editor pre-filled
            Intent intent = new Intent(this, QueryEditorActivity.class);
            intent.putExtra("db_name", dbName);
            intent.putExtra("initial_query", "SELECT * FROM " + table.getName() + " LIMIT 100;");
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        // Search
        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                filterTables(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        loadSchema();
    }

    private void loadSchema() {
        if (dbName == null) return;

        dbEngine.openDatabase(dbName);
        allTables = dbEngine.getTablesForDatabase(dbName);

        // Eagerly load all column info
        columnsMap.clear();
        for (TableInfo t : allTables) {
            List<ColumnInfo> cols = dbEngine.getColumnsForTable(t.getName());
            columnsMap.put(t.getName(), cols);
        }

        adapter.updateData(allTables, columnsMap);
    }

    private void filterTables(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allTables, columnsMap);
            return;
        }
        List<TableInfo> filtered = new ArrayList<>();
        Map<String, List<ColumnInfo>> filteredCols = new HashMap<>();
        for (TableInfo t : allTables) {
            if (t.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(t);
                filteredCols.put(t.getName(), columnsMap.get(t.getName()));
            }
        }
        adapter.updateData(filtered, filteredCols);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
