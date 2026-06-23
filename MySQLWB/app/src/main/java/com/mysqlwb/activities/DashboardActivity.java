package com.mysqlwb.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mysqlwb.R;
import com.mysqlwb.adapters.DatabaseAdapter;
import com.mysqlwb.database.DatabaseEngine;
import com.mysqlwb.models.DatabaseInfo;
import com.mysqlwb.utils.SampleDatabaseCreator;

import java.io.File;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseEngine dbEngine;
    private DatabaseAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("mysqlwb_prefs", MODE_PRIVATE);
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbEngine = DatabaseEngine.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MySQL WB");
        }

        recyclerView = findViewById(R.id.rv_databases);
        emptyView = findViewById(R.id.tv_empty);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_create_db);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new DatabaseAdapter(this, this::openDatabase, this::handleDatabaseMenuAction);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showCreateDatabaseDialog());

        // Quick action cards
        setupQuickActions();
        loadDatabases();
        
        // Ensure the primary practice database exists
        boolean hasSchoolDb = false;
        for (DatabaseInfo db : dbEngine.listDatabases()) {
            if ("school_db".equals(db.getName())) {
                hasSchoolDb = true;
                break;
            }
        }
        if (!hasSchoolDb) {
            SampleDatabaseCreator.createSchoolDatabase(dbEngine);
            loadDatabases();
        }
    }

    private void applyTheme() {
        boolean isDark = prefs.getBoolean("dark_mode", true);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void setupQuickActions() {
        View cardQuery = findViewById(R.id.card_new_query);
        View cardTutorial = findViewById(R.id.card_tutorials);
        View cardRef = findViewById(R.id.card_sql_ref);
        View cardSample = findViewById(R.id.card_sample_db);

        if (cardQuery != null) cardQuery.setOnClickListener(v ->
                startActivity(new Intent(this, QueryEditorActivity.class)));
        if (cardTutorial != null) cardTutorial.setOnClickListener(v ->
                startActivity(new Intent(this, TutorialsActivity.class)));
        if (cardRef != null) cardRef.setOnClickListener(v ->
                startActivity(new Intent(this, SQLReferenceActivity.class)));
        if (cardSample != null) cardSample.setOnClickListener(v -> showSampleDatabaseDialog());
    }

    private void loadDatabases() {
        List<DatabaseInfo> databases = dbEngine.listDatabases();
        adapter.setData(databases);
        
        TextView tvCount = findViewById(R.id.tv_db_count);
        if (tvCount != null) {
            tvCount.setText(databases.size() + (databases.size() == 1 ? " database" : " databases"));
        }

        emptyView.setVisibility(databases.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(databases.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openDatabase(DatabaseInfo db) {
        dbEngine.openDatabase(db.getName());
        Intent intent = new Intent(this, QueryEditorActivity.class);
        intent.putExtra("db_name", db.getName());
        startActivity(intent);
    }

    private void handleDatabaseMenuAction(int actionId, DatabaseInfo db) {
        if (actionId == -1 || actionId == R.id.opt_open) {
            // Open database for both single click and long press
            openDatabase(db);
            return;
        }

        if (actionId == R.id.opt_schema) {
            dbEngine.openDatabase(db.getName());
            Intent si = new Intent(this, SchemaExplorerActivity.class);
            si.putExtra("db_name", db.getName());
            startActivity(si);
        } else if (actionId == R.id.opt_table_designer) {
            dbEngine.openDatabase(db.getName());
            Intent ti = new Intent(this, TableDesignerActivity.class);
            ti.putExtra("db_name", db.getName());
            startActivity(ti);
        } else if (actionId == R.id.opt_export) {
            exportDatabase(db);
        } else if (actionId == R.id.opt_backup) {
            backupDatabase(db);
        } else if (actionId == R.id.opt_rename) {
            renameDatabase(db);
        } else if (actionId == R.id.opt_delete) {
            confirmDeleteDatabase(db);
        }
    }

    private void showCreateDatabaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Database");
        View view = getLayoutInflater().inflate(R.layout.dialog_create_database, null);
        EditText etName = view.findViewById(R.id.et_db_name);
        builder.setView(view);
        builder.setPositiveButton("Create", (d, w) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Database name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!name.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                Toast.makeText(this, "Invalid name. Use letters, numbers, underscores only", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbEngine.createDatabase(name)) {
                Toast.makeText(this, "Database '" + name + "' created!", Toast.LENGTH_SHORT).show();
                loadDatabases();
            } else {
                Toast.makeText(this, "Database '" + name + "' already exists", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showSampleDatabaseDialog() {
        String[] samples = {
            "📚 School Database", 
            "🛒 E-Commerce Database", 
            "🏢 Company/HR Database",
            "🏥 Hospital Management",
            "📖 Public Library DB",
            "📈 Stock Trading DB",
            "🎬 Movie/IMDb Clone"
        };
        new AlertDialog.Builder(this)
                .setTitle("Select Sample Database")
                .setItems(samples, (dialog, which) -> {
                    boolean success = false;
                    String name = "";
                    switch (which) {
                        case 0:
                            success = SampleDatabaseCreator.createSchoolDatabase(dbEngine);
                            name = "school_db";
                            break;
                        case 1:
                            success = SampleDatabaseCreator.createEcommerceDatabase(dbEngine);
                            name = "ecommerce_db";
                            break;
                        case 2:
                            success = SampleDatabaseCreator.createEmployeeDatabase(dbEngine);
                            name = "company_db";
                            break;
                        case 3:
                            success = SampleDatabaseCreator.createHospitalDatabase(dbEngine);
                            name = "hospital_db";
                            break;
                        case 4:
                            success = SampleDatabaseCreator.createLibraryDatabase(dbEngine);
                            name = "library_db";
                            break;
                        case 5:
                            success = SampleDatabaseCreator.createStockDatabase(dbEngine);
                            name = "trading_db";
                            break;
                        case 6:
                            success = SampleDatabaseCreator.createMovieDatabase(dbEngine);
                            name = "movies_db";
                            break;
                    }
                    if (success) {
                        Toast.makeText(this, "Sample database loaded! 🎉", Toast.LENGTH_LONG).show();
                        loadDatabases();
                        final String finalName = name;
                        new AlertDialog.Builder(this)
                                .setTitle("Open Database?")
                                .setMessage("'" + finalName + "' is ready. Open it in the Query Editor?")
                                .setPositiveButton("Open", (d, w) -> {
                                    Intent intent = new Intent(this, QueryEditorActivity.class);
                                    intent.putExtra("db_name", finalName);
                                    startActivity(intent);
                                })
                                .setNegativeButton("Later", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Failed to load sample database", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void exportDatabase(DatabaseInfo db) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(downloadsDir, db.getName() + "_export.sql");
        if (dbEngine.exportDatabase(db.getName(), outputFile)) {
            Toast.makeText(this, "Exported to Downloads/" + db.getName() + "_export.sql", Toast.LENGTH_LONG).show();
        } else {
            // Fallback to internal storage
            File internalFile = new File(getExternalFilesDir(null), db.getName() + "_export.sql");
            if (dbEngine.exportDatabase(db.getName(), internalFile)) {
                Toast.makeText(this, "Exported to: " + internalFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void backupDatabase(DatabaseInfo db) {
        File outputFile = new File(getExternalFilesDir(null), db.getName() + "_backup.db");
        if (dbEngine.backupDatabase(db.getName(), outputFile)) {
            Toast.makeText(this, "Backup saved to: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Backup failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void renameDatabase(DatabaseInfo db) {
        EditText et = new EditText(this);
        et.setText(db.getName());
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this)
                .setTitle("Rename Database")
                .setView(et)
                .setPositiveButton("Rename", (d, w) -> {
                    String newName = et.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(db.getName())) {
                        // Export and re-import with new name
                        Toast.makeText(this, "Rename: Create new DB and import data manually for now.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteDatabase(DatabaseInfo db) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Database")
                .setMessage("Are you sure you want to permanently delete '" + db.getName() + "'?\nThis action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    if (dbEngine.deleteDatabase(db.getName())) {
                        Toast.makeText(this, "Database deleted", Toast.LENGTH_SHORT).show();
                        loadDatabases();
                    } else {
                        Toast.makeText(this, "Failed to delete database", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, QueryHistoryActivity.class));
            return true;
        } else if (id == R.id.action_tutorials) {
            startActivity(new Intent(this, TutorialsActivity.class));
            return true;
        } else if (id == R.id.action_sql_ref) {
            startActivity(new Intent(this, SQLReferenceActivity.class));
            return true;
        } else if (id == R.id.action_toggle_theme) {
            boolean isDark = prefs.getBoolean("dark_mode", true);
            prefs.edit().putBoolean("dark_mode", !isDark).apply();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDatabases();
    }
}
