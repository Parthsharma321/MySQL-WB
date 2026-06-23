package com.mysqlwb.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mysqlwb.R;
import com.mysqlwb.database.HistoryManager;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("mysqlwb_prefs", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        setupDarkMode();
        setupFontSize();
        setupSyntaxHighlighting();
        setupAutoComplete();
        setupClearHistory();
    }

    private void setupDarkMode() {
        SwitchMaterial sw = findViewById(R.id.switch_dark_mode);
        sw.setChecked(prefs.getBoolean("dark_mode", false));
        sw.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        findViewById(R.id.row_dark_mode).setOnClickListener(v -> sw.toggle());
    }

    private void setupFontSize() {
        SeekBar seek = findViewById(R.id.seek_font_size);
        TextView tvValue = findViewById(R.id.tv_font_size_value);

        int savedSize = prefs.getInt("editor_font_size", 14);
        int progress = savedSize - 10;  // 10sp = 0, 20sp = 10
        seek.setProgress(Math.max(0, Math.min(10, progress)));
        tvValue.setText(savedSize + "sp");

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                int size = 10 + p;
                tvValue.setText(size + "sp");
                if (user) prefs.edit().putInt("editor_font_size", size).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void setupSyntaxHighlighting() {
        SwitchMaterial sw = findViewById(R.id.switch_syntax);
        sw.setChecked(prefs.getBoolean("syntax_highlight", true));
        sw.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("syntax_highlight", checked).apply());
    }

    private void setupAutoComplete() {
        SwitchMaterial sw = findViewById(R.id.switch_autocomplete);
        sw.setChecked(prefs.getBoolean("auto_complete", false));
        sw.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("auto_complete", checked).apply());
    }

    private void setupClearHistory() {
        findViewById(R.id.row_clear_history).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear Query History")
                        .setMessage("This will permanently delete all query history. Are you sure?")
                        .setPositiveButton("Clear All", (d, w) -> {
                            HistoryManager.getInstance(this).clearHistory();
                            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
