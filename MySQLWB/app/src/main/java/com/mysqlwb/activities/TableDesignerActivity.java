package com.mysqlwb.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.mysqlwb.R;
import com.mysqlwb.database.DatabaseEngine;
import com.mysqlwb.models.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class TableDesignerActivity extends AppCompatActivity {

    private DatabaseEngine dbEngine;
    private String dbName;
    private EditText etTableName;
    private LinearLayout columnsContainer;
    private TextView tvSqlPreview;
    private final List<View> columnViews = new ArrayList<>();

    private static final String[] DATA_TYPES = {
        "INTEGER", "TEXT", "REAL", "BLOB", "BOOLEAN",
        "VARCHAR(255)", "CHAR(50)", "FLOAT", "DOUBLE",
        "DECIMAL(10,2)", "DATE", "DATETIME"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_designer);

        dbEngine = DatabaseEngine.getInstance(this);
        dbName   = getIntent().getStringExtra("db_name");
        if (dbName == null) dbName = dbEngine.getCurrentDatabaseName();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Table Designer");
            if (dbName != null) getSupportActionBar().setSubtitle(dbName);
        }

        etTableName     = findViewById(R.id.et_table_name);
        columnsContainer = findViewById(R.id.columns_container);
        tvSqlPreview    = findViewById(R.id.tv_sql_preview);

        etTableName.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) { refreshSqlPreview(); }
        });

        findViewById(R.id.btn_add_column).setOnClickListener(v -> addColumn(false, false));
        findViewById(R.id.btn_create_table).setOnClickListener(v -> createTable());

        // Seed with id + one blank column
        addColumn(true, true);
        addColumn(false, false);
    }

    private void addColumn(boolean isId, boolean locked) {
        View row = getLayoutInflater().inflate(R.layout.item_column_row, columnsContainer, false);

        EditText   etName  = row.findViewById(R.id.et_col_name);
        Spinner    spinner = row.findViewById(R.id.spinner_type);
        CheckBox   cbPK    = row.findViewById(R.id.cb_pk);
        CheckBox   cbNN    = row.findViewById(R.id.cb_nn);
        CheckBox   cbAI    = row.findViewById(R.id.cb_ai);
        ImageButton btnDel = row.findViewById(R.id.btn_delete_col);

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DATA_TYPES);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinAdapter);

        if (isId) {
            etName.setText("id");
            cbPK.setChecked(true);
            cbNN.setChecked(true);
            cbAI.setChecked(true);
            if (locked) {
                etName.setEnabled(false);
                cbPK.setEnabled(false);
                cbAI.setEnabled(false);
            }
        }

        btnDel.setOnClickListener(v -> {
            if (locked) { Toast.makeText(this, "Cannot remove primary key row", Toast.LENGTH_SHORT).show(); return; }
            columnsContainer.removeView(row);
            columnViews.remove(row);
            refreshSqlPreview();
        });

        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) { refreshSqlPreview(); }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { refreshSqlPreview(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        columnsContainer.addView(row);
        columnViews.add(row);
        refreshSqlPreview();
    }

    private String buildSQL() {
        String tableName = etTableName.getText().toString().trim();
        if (tableName.isEmpty()) tableName = "your_table_name";

        StringBuilder sb = new StringBuilder("CREATE TABLE ").append(tableName).append(" (\n");
        List<String> defs = new ArrayList<>();

        for (int i = 0; i < columnsContainer.getChildCount(); i++) {
            View row = columnsContainer.getChildAt(i);
            EditText etName = row.findViewById(R.id.et_col_name);
            Spinner  spinner = row.findViewById(R.id.spinner_type);
            CheckBox cbPK   = row.findViewById(R.id.cb_pk);
            CheckBox cbNN   = row.findViewById(R.id.cb_nn);
            CheckBox cbAI   = row.findViewById(R.id.cb_ai);
            if (etName == null) continue;
            String colName = etName.getText().toString().trim();
            if (colName.isEmpty()) continue;
            String type = spinner != null ? spinner.getSelectedItem().toString() : "TEXT";
            StringBuilder def = new StringBuilder("  ").append(colName).append(" ").append(type);
            if (cbPK != null && cbPK.isChecked()) def.append(" PRIMARY KEY");
            if (cbAI != null && cbAI.isChecked()) def.append(" AUTOINCREMENT");
            if (cbNN != null && cbNN.isChecked()) def.append(" NOT NULL");
            defs.add(def.toString());
        }

        for (int i = 0; i < defs.size(); i++) {
            sb.append(defs.get(i));
            if (i < defs.size() - 1) sb.append(",\n");
        }
        sb.append("\n);");
        return sb.toString();
    }

    private void refreshSqlPreview() {
        tvSqlPreview.setText(buildSQL());
    }

    private void createTable() {
        String tableName = etTableName.getText().toString().trim();
        if (tableName.isEmpty()) {
            Toast.makeText(this, "Enter a table name first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            Toast.makeText(this, "Invalid table name. Use letters, digits, underscores only.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dbEngine.isDatabaseOpen()) {
            if (dbName != null) dbEngine.openDatabase(dbName);
            else { Toast.makeText(this, "No database selected", Toast.LENGTH_SHORT).show(); return; }
        }

        String sql = buildSQL();
        QueryResult result = dbEngine.executeQuery(sql);

        if (result.isError()) {
            Toast.makeText(this, "Error: " + result.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "✅ Table '" + tableName + "' created!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    // Simple TextWatcher base class
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
