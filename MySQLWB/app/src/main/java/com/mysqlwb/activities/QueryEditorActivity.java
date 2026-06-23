package com.mysqlwb.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.mysqlwb.R;
import com.mysqlwb.database.DatabaseEngine;
import com.mysqlwb.database.HistoryManager;
import com.mysqlwb.models.QueryResult;
import com.mysqlwb.utils.SQLHighlighter;

import java.util.ArrayList;
import java.util.List;

public class QueryEditorActivity extends AppCompatActivity {

    private DatabaseEngine dbEngine;
    private HistoryManager historyManager;
    private MultiAutoCompleteTextView etQueryEditor;
    private TextView tvStatus;
    private TextView tvDbIndicator;
    private TextView tvCursorPos;
    private LinearLayout layoutResults;
    private ScrollView scrollOutput;
    private String currentDbName;
    private int errorLine = -1;

    // Undo/Redo logic
    private final List<String> undoStack = new ArrayList<>();
    private int undoIndex = -1;
    private boolean isHistoryAction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_editor);

        dbEngine = DatabaseEngine.getInstance(this);
        historyManager = HistoryManager.getInstance(this);
        currentDbName = getIntent().getStringExtra("db_name");

        if (currentDbName != null) {
            // Re-provision sample database if it's missing or empty
            if (isSampleDatabase(currentDbName) && dbEngine.getTablesForDatabase(currentDbName).isEmpty()) {
                com.mysqlwb.utils.SampleDatabaseCreator.provisionByName(currentDbName, dbEngine);
            }
            dbEngine.openDatabase(currentDbName);
        }

        setupToolbar();
        setupViews();
        setupAutoComplete();
        setupSyntaxHighlighting();

        // Load initial query from intent if provided
        String initialQuery = getIntent().getStringExtra("initial_query");
        if (initialQuery != null && !initialQuery.isEmpty()) {
            etQueryEditor.setText(initialQuery);
            etQueryEditor.setSelection(initialQuery.length());
            clearOutput();
            // Auto-execute if we have a database open
            if (dbEngine.isDatabaseOpen()) {
                executeCurrentQuery();
            }
        } else {
            if (currentDbName != null) {
                loadDraft();
                clearOutput();
                showSavedQueriesForDatabase();
            } else {
                etQueryEditor.setText("");
                clearOutput();
                showWelcomeOutput();
            }
        }
        clearUndoHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyEditorSettings();
    }

    private void applyEditorSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("mysqlwb_prefs", MODE_PRIVATE);
        
        // Font Size
        int fontSize = prefs.getInt("editor_font_size", 14);
        if (etQueryEditor != null) {
            etQueryEditor.setTextSize(fontSize);
        }

        // Syntax Highlighting
        if (etQueryEditor != null) {
            applySyntaxHighlighting(etQueryEditor.getText());
        }

        // Auto Complete
        setupAutoComplete();
    }

    private void clearUndoHistory() {
        undoStack.clear();
        undoStack.add(etQueryEditor.getText().toString());
        undoIndex = 0;
        isHistoryAction = false;
    }

    private void showSavedQueriesForDatabase() {
        // Only show tables summary for Sample Databases
        boolean isSample = isSampleDatabase(currentDbName);
        
        if (isSample) {
            List<com.mysqlwb.models.TableInfo> tables = dbEngine.getTablesForDatabase(currentDbName);
            if (!tables.isEmpty()) {
                TextView tableHeader = new TextView(this);
                tableHeader.setText("📊 Tables in Sample DB: " + currentDbName);
                tableHeader.setPadding(16, 12, 16, 4);
                tableHeader.setTextColor(ContextCompat.getColor(this, R.color.accent));
                tableHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                layoutResults.addView(tableHeader);

                for (com.mysqlwb.models.TableInfo t : tables) {
                    TextView tv = new TextView(this);
                    tv.setText("• " + t.getName() + " (" + t.getRowCount() + " rows)");
                    tv.setPadding(32, 4, 16, 4);
                    tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    tv.setClickable(true);
                    tv.setFocusable(true);
                    
                    android.util.TypedValue outValue = new android.util.TypedValue();
                    getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    tv.setBackgroundResource(outValue.resourceId);

                    tv.setOnClickListener(v -> {
                        String query = "SELECT * FROM " + t.getName() + " LIMIT 50;";
                        etQueryEditor.setText(query);
                        etQueryEditor.setSelection(query.length());
                        executeCurrentQuery();
                    });
                    layoutResults.addView(tv);
                }
            }
        }

        List<com.mysqlwb.models.QueryHistory> saved = historyManager.getSavedQueries(currentDbName);
        if (saved.isEmpty()) {
            if (!isSample || dbEngine.getTablesForDatabase(currentDbName).isEmpty()) {
                showWelcomeOutput();
            }
            return;
        }

        TextView header = new TextView(this);
        header.setText("\n📂 Saved Queries");
        header.setPadding(16, 12, 16, 4);
        header.setTextColor(ContextCompat.getColor(this, R.color.primary_light));
        header.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        layoutResults.addView(header);

        for (com.mysqlwb.models.QueryHistory q : saved) {
            TextView tv = new TextView(this);
            String title = q.getDatabase();
            tv.setText("• " + title);
            tv.setPadding(32, 4, 16, 4);
            tv.setTextColor(ContextCompat.getColor(this, R.color.info_color));
            tv.setClickable(true);
            tv.setFocusable(true);
            
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tv.setBackgroundResource(outValue.resourceId);

            tv.setOnClickListener(v -> {
                String selectedSql = q.getSql();
                String currentText = etQueryEditor.getText().toString();

                if (!currentText.isEmpty()) {
                    if (!currentText.endsWith("\n")) {
                        etQueryEditor.append("\n\n");
                    } else if (!currentText.endsWith("\n\n")) {
                        etQueryEditor.append("\n");
                    }
                }

                etQueryEditor.append(selectedSql);
                etQueryEditor.setSelection(etQueryEditor.getText().length());
                Toast.makeText(this, "Loaded: " + title, Toast.LENGTH_SHORT).show();
            });
            layoutResults.addView(tv);
        }
        
        TextView hint = new TextView(this);
        hint.setText("\n(Tap a table to auto-query or a saved query to load)");
        hint.setPadding(16, 4, 16, 12);
        hint.setTextSize(11f);
        hint.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        layoutResults.addView(hint);
    }

    private boolean isSampleDatabase(String name) {
        if (name == null) return false;
        return name.equals("school_db") || 
               name.equals("ecommerce_db") || 
               name.equals("company_db") || 
               name.equals("hospital_db") || 
               name.equals("library_db") || 
               name.equals("trading_db") || 
               name.equals("movies_db");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Query Editor");
        }
    }

    private void setupViews() {
        etQueryEditor = findViewById(R.id.et_query_editor);
        tvStatus = findViewById(R.id.tv_status);
        tvDbIndicator = findViewById(R.id.tv_db_indicator);
        tvCursorPos = findViewById(R.id.tv_cursor_pos);
        layoutResults = findViewById(R.id.layout_results);
        scrollOutput = findViewById(R.id.scroll_output);

        MaterialButton btnRun = findViewById(R.id.btn_run_query);
        MaterialButton btnRunSelected = findViewById(R.id.btn_run_selected);
        MaterialButton btnClear = findViewById(R.id.btn_clear);
        MaterialButton btnFormat = findViewById(R.id.btn_format);
        ImageView btnUndo = findViewById(R.id.btn_undo);
        ImageView btnRedo = findViewById(R.id.btn_redo);

        // Database indicator
        updateDbIndicator();

        btnRun.setOnClickListener(v -> executeCurrentQuery());
        btnRunSelected.setOnClickListener(v -> executeCurrentQuery());
        btnClear.setOnClickListener(v -> {
            // Record the current state before clearing so it can be undone
            recordUndoState(etQueryEditor.getText().toString());
            etQueryEditor.setText("");
            errorLine = -1;
            clearOutput();
        });
        btnFormat.setOnClickListener(v -> formatQuery());

        if (btnUndo != null) btnUndo.setOnClickListener(v -> undo());
        if (btnRedo != null) btnRedo.setOnClickListener(v -> redo());

        // Template chips
        setupTemplateChips();

        // Enforce identical typography
        etQueryEditor.setTypeface(Typeface.MONOSPACE);
        etQueryEditor.setIncludeFontPadding(false);

        etQueryEditor.setHint("-- Enter SQL query here\n-- Example: SELECT * FROM table_name;\n\n-- Tip: Tap Run (▶) to execute");
    }

    private void updateDbIndicator() {
        if (tvDbIndicator != null) {
            String db = dbEngine.getCurrentDatabaseName();
            if (db != null) {
                tvDbIndicator.setText("📊 " + db);
                tvDbIndicator.setVisibility(View.VISIBLE);
            } else {
                tvDbIndicator.setText("⚠ No DB selected");
                tvDbIndicator.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupTemplateChips() {
        ChipGroup chipGroup = findViewById(R.id.chip_group_templates);
        if (chipGroup == null) return;

        String[][] templates = {
                {"SELECT *", "SELECT * FROM "},
                {"INSERT", "INSERT INTO  (col1, col2)\nVALUES (val1, val2);"},
                {"UPDATE", "UPDATE \nSET col1 = val1\nWHERE id = 1;"},
                {"DELETE", "DELETE FROM \nWHERE id = 1;"},
                {"CREATE TABLE", "CREATE TABLE  (\n  id INTEGER PRIMARY KEY AUTOINCREMENT,\n  name TEXT NOT NULL\n);"},
                {"SHOW TABLES", "SHOW TABLES;"},
                {"DESCRIBE", "DESCRIBE "},
                {"JOIN", "SELECT t1.*, t2.*\nFROM table1 t1\nINNER JOIN table2 t2 ON t1.id = t2.id;"},
                {"ALTER RENAME", "ALTER TABLE table_name RENAME COLUMN old_col TO new_col;"},
                {"TRUNCATE", "TRUNCATE TABLE table_name;"},
                {"SHOW SCHEMA", "SHOW TABLES;"}
        };

        for (String[] template : templates) {
            Chip chip = new Chip(this);
            chip.setText(template[0]);
            chip.setCheckable(false);
            chip.setOnClickListener(v -> {
                String current = etQueryEditor.getText().toString();
                if (!current.isEmpty() && !current.endsWith("\n")) {
                    etQueryEditor.append("\n");
                }
                etQueryEditor.append(template[1]);
                etQueryEditor.setSelection(etQueryEditor.getText().length());
                etQueryEditor.requestFocus();
            });
            chipGroup.addView(chip);
        }
    }

    private void setupAutoComplete() {
        android.content.SharedPreferences prefs = getSharedPreferences("mysqlwb_prefs", MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("auto_complete", false);

        if (enabled) {
            String[] keywords = {
                "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
                "DROP", "ALTER", "JOIN", "INNER", "LEFT", "RIGHT", "ON", "GROUP", "BY", "ORDER",
                "HAVING", "LIMIT", "OFFSET", "AND", "OR", "NOT", "IN", "IS", "NULL", "VALUES",
                "DISTINCT", "AS", "DATABASE", "SCHEMA", "SHOW", "TABLES", "DESCRIBE", "DESC",
                "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "AUTOINCREMENT", "INTEGER", "TEXT",
                "REAL", "BLOB", "VARCHAR", "BOOLEAN", "DATE", "DATETIME", "COUNT", "SUM", "AVG",
                "MIN", "MAX", "TRUNCATE", "RENAME", "MODIFY", "COLUMN", "USE"
            };

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, keywords);
            etQueryEditor.setAdapter(adapter);
            etQueryEditor.setTokenizer(new SpaceTokenizer());
            etQueryEditor.setThreshold(1);
        } else {
            etQueryEditor.setAdapter(null);
        }

        // Initial state for undo
        undoStack.clear();
        undoStack.add("");
        undoIndex = 0;

        etQueryEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Syntax highlighting
                applySyntaxHighlighting(s);
                updateCursorPosition();

                // Record for undo/redo
                if (!isHistoryAction) {
                    recordUndoState(s.toString());
                }
            }
        });
        etQueryEditor.setOnClickListener(v -> { updateCursorPosition(); });
        etQueryEditor.setOnKeyListener((v, k, e) -> { updateCursorPosition(); return false; });
        updateCursorPosition();
    }

    // Custom Tokenizer to suggest words based on space/newline instead of just comma
    private static class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && !isSeparator(text.charAt(i - 1))) {
                i--;
            }
            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            while (i < len) {
                if (isSeparator(text.charAt(i))) {
                    return i;
                } else {
                    i++;
                }
            }
            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && isSeparator(text.charAt(i - 1))) {
                i--;
            }

            if (i > 0 && isSeparator(text.charAt(i - 1))) {
                return text;
            } else {
                if (text instanceof Spannable) {
                    android.text.SpannableString sp = new android.text.SpannableString(text + " ");
                    android.text.TextUtils.copySpansFrom((Spannable) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }

        private boolean isSeparator(char c) {
            return Character.isWhitespace(c) || c == ',' || c == ';' || c == '(' || c == ')';
        }
    }

    private void updateCursorPosition() {
        if (tvCursorPos == null || etQueryEditor == null) return;
        int pos = etQueryEditor.getSelectionStart();
        int line = 1;
        int col = 1;
        String text = etQueryEditor.getText().toString();
        for (int i = 0; i < pos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
        tvCursorPos.setText("Ln " + line + ", Col " + col);
    }

    private void recordUndoState(String text) {
        // Don't record if it's the same as the last state
        if (undoIndex >= 0 && text.equals(undoStack.get(undoIndex))) {
            return;
        }

        // Remove all states after current index (redo states)
        while (undoStack.size() > undoIndex + 1) {
            undoStack.remove(undoStack.size() - 1);
        }

        // Add new state
        undoStack.add(text);
        undoIndex++;

        // Keep only last 50
        if (undoStack.size() > 50) {
            undoStack.remove(0);
            undoIndex--;
        }
    }

    private void undo() {
        if (undoIndex > 0) {
            isHistoryAction = true;
            undoIndex--;
            String text = undoStack.get(undoIndex);
            etQueryEditor.setText(text);
            etQueryEditor.setSelection(text.length());
            isHistoryAction = false;
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redo() {
        if (undoIndex < undoStack.size() - 1) {
            isHistoryAction = true;
            undoIndex++;
            String text = undoStack.get(undoIndex);
            etQueryEditor.setText(text);
            etQueryEditor.setSelection(text.length());
            isHistoryAction = false;
        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private void applySyntaxHighlighting(Editable editable) {
        android.content.SharedPreferences prefs = getSharedPreferences("mysqlwb_prefs", MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("syntax_highlight", true);

        if (enabled) {
            // Using SQLHighlighter utility
            SQLHighlighter.highlight(editable);
        } else {
            // Remove existing spans if disabled
            ForegroundColorSpan[] spans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                editable.removeSpan(span);
            }
        }
    }

    private void setupSyntaxHighlighting() {
        // Initial highlight
    }

    private void executeCurrentQuery() {
        int start = etQueryEditor.getSelectionStart();
        int end = etQueryEditor.getSelectionEnd();
        String sqlToExecute;
        int queryStartOffset;

        if (start != end) {
            // Execute Selection
            sqlToExecute = etQueryEditor.getText().toString().substring(start, end).trim();
            queryStartOffset = start;
        } else {
            // Execute Statement at Caret
            int[] range = getStatementRangeAtCaret();
            if (range[0] == -1) return;
            sqlToExecute = etQueryEditor.getText().toString().substring(range[0], range[1]).trim();
            queryStartOffset = range[0];
        }

        if (sqlToExecute.isEmpty()) {
            showStatus("⚠ No query to execute", false);
            return;
        }

        runQuery(sqlToExecute, queryStartOffset);
    }

    private int[] getStatementRangeAtCaret() {
        String text = etQueryEditor.getText().toString();
        int cursor = etQueryEditor.getSelectionStart();
        if (text.trim().isEmpty()) return new int[]{-1, -1};

        List<int[]> boundaries = new ArrayList<>();
        int lastStart = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ';' && !isInsideQuotes(text, i)) {
                boundaries.add(new int[]{lastStart, i + 1});
                lastStart = i + 1;
            }
        }
        if (lastStart < text.length()) {
            boundaries.add(new int[]{lastStart, text.length()});
        }

        if (boundaries.isEmpty()) return new int[]{-1, -1};

        int targetIdx = -1;
        for (int i = 0; i < boundaries.size(); i++) {
            int[] b = boundaries.get(i);
            if (cursor >= b[0] && cursor <= b[1]) {
                targetIdx = i;
                break;
            }
        }
        if (targetIdx == -1) targetIdx = boundaries.size() - 1;

        if (targetIdx > 0) {
            String textBeforeInSpan = text.substring(boundaries.get(targetIdx)[0], cursor);
            String currentSql = text.substring(boundaries.get(targetIdx)[0], boundaries.get(targetIdx)[1]).trim();
            if (textBeforeInSpan.trim().isEmpty() || currentSql.isEmpty()) {
                return boundaries.get(targetIdx - 1);
            }
        }
        return boundaries.get(targetIdx);
    }

    private void runQuery(String sql, int startOffset) {
        long startTime = System.currentTimeMillis();
        QueryResult result = dbEngine.executeQuery(sql);
        long elapsed = System.currentTimeMillis() - startTime;

        errorLine = -1;
        if (result.isError()) {
            String text = etQueryEditor.getText().toString();
            errorLine = 1;
            for (int i = 0; i < startOffset && i < text.length(); i++) {
                if (text.charAt(i) == '\n') errorLine++;
            }
        }

        // Save to history
        historyManager.addToHistory(sql, dbEngine.getCurrentDatabaseName(),
                result.isSuccess(), elapsed);

        // Update database indicator
        updateDbIndicator();
        currentDbName = dbEngine.getCurrentDatabaseName();

        displayResult(result, elapsed);
    }

    private boolean isInsideQuotes(String text, int index) {
        boolean singleQuote = false;
        boolean doubleQuote = false;
        for (int i = 0; i < index; i++) {
            char c = text.charAt(i);
            if (c == '\'' && !doubleQuote) singleQuote = !singleQuote;
            else if (c == '\"' && !singleQuote) doubleQuote = !doubleQuote;
            else if (c == '\\') i++; // Skip escaped character
        }
        return singleQuote || doubleQuote;
    }

    private void executeSelectedQuery() {
        // Now selection logic is unified in executeCurrentQuery
        executeCurrentQuery();
    }

    private void displayResult(QueryResult result, long elapsed) {
        clearOutput();

        if (result.isError()) {
            showErrorOutput(result.getMessage());
            showStatus("❌ Error | " + elapsed + "ms", false);
            return;
        }

        if (result.hasTable()) {
            renderTable(result);
            String status = String.format("✅ %d row(s) | %.3fs",
                    result.getRowCount(), elapsed / 1000.0);
            showStatus(status, true);
        } else {
            showSuccessOutput(result.getMessage());
            showStatus("✅ " + result.getMessage() + " | " + elapsed + "ms", true);
        }
    }

    private void renderTable(QueryResult result) {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TableLayout table = new TableLayout(this);
        table.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Header row
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.table_header_bg));
        headerRow.setPadding(0, 4, 0, 4);

        for (String col : result.getColumns()) {
            TextView tv = createTableCell(col, true);
            headerRow.addView(tv);
        }
        table.addView(headerRow);

        // Data rows
        boolean alternate = false;
        int maxRows = Math.min(result.getRowCount(), 500); // Limit for performance
        List<String[]> rows = result.getRows();
        for (int i = 0; i < maxRows; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(alternate ?
                    ContextCompat.getColor(this, R.color.table_row_alternate) :
                    ContextCompat.getColor(this, R.color.table_row_normal));

            for (String cell : rows.get(i)) {
                TextView tv = createTableCell(cell != null ? cell : "NULL", false);
                if (cell == null) tv.setTextColor(ContextCompat.getColor(this, R.color.null_value_color));
                row.addView(tv);
            }
            table.addView(row);
            alternate = !alternate;
        }

        if (result.getRowCount() > maxRows) {
            TableRow limitRow = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText("... " + (result.getRowCount() - maxRows) + " more rows (add LIMIT to see all)");
            tv.setPadding(16, 8, 16, 8);
            tv.setTextColor(ContextCompat.getColor(this, R.color.warning_color));
            limitRow.addView(tv);
            table.addView(limitRow);
        }

        hsv.addView(table);
        layoutResults.addView(hsv);

        // Row count info
        TextView countTv = new TextView(this);
        countTv.setText(result.getRowCount() + " row(s) in set");
        countTv.setPadding(16, 8, 16, 8);
        countTv.setTextColor(ContextCompat.getColor(this, R.color.success_color));
        layoutResults.addView(countTv);
    }

    private TextView createTableCell(String text, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(24, 16, 24, 16);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(13f);

        if (isHeader) {
            tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            tv.setTextColor(ContextCompat.getColor(this, R.color.table_header_text));
        } else {
            tv.setTextColor(ContextCompat.getColor(this, R.color.table_cell_text));
        }

        // Separator
        tv.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        tv.setMinWidth(120);
        return tv;
    }

    private void showSuccessOutput(String message) {
        TextView tv = new TextView(this);
        tv.setText("✅ " + message);
        tv.setPadding(16, 12, 16, 12);
        tv.setTextColor(ContextCompat.getColor(this, R.color.success_color));
        tv.setTypeface(Typeface.MONOSPACE);
        layoutResults.addView(tv);
    }

    private void showErrorOutput(String message) {
        TextView tv = new TextView(this);
        tv.setText("❌ ERROR: " + message);
        tv.setPadding(16, 12, 16, 12);
        tv.setTextColor(ContextCompat.getColor(this, R.color.error_color));
        tv.setTypeface(Typeface.MONOSPACE);
        layoutResults.addView(tv);
    }

    private void showWelcomeOutput() {
        // If it's a "New Query" (no DB selected), we keep it blank as per user request
        if (currentDbName == null) {
            clearOutput();
            return;
        }

        TextView tv = new TextView(this);
        String welcome = "MySQL WB — Query Editor\n" +
                "━━━━━━━━━━━━━━━━━━━━━\n" +
                (currentDbName != null ? "Connected: " + currentDbName : "No database selected") + "\n\n" +
                "Quick start:\n" +
                "  SHOW TABLES;  -- List all tables\n" +
                "  DESCRIBE table_name;  -- See columns\n" +
                "  SELECT * FROM table_name;\n\n" +
                "Tip: Tap the 'Schema' icon in the top bar to browse tables visually.";
        tv.setText(welcome);
        tv.setPadding(16, 12, 16, 12);
        tv.setTextColor(ContextCompat.getColor(this, R.color.info_color));
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(13f);
        layoutResults.addView(tv);
    }

    private void clearOutput() {
        layoutResults.removeAllViews();
    }

    private void showStatus(String msg, boolean success) {
        tvStatus.setText(msg);
        tvStatus.setTextColor(ContextCompat.getColor(this,
                success ? R.color.success_color : R.color.error_color));
    }

    private void formatQuery() {
        String sql = etQueryEditor.getText().toString();
        String formatted = formatSQL(sql);
        etQueryEditor.setText(formatted);
        etQueryEditor.setSelection(formatted.length());
    }

    private String formatSQL(String sql) {
        String[] keywords = {"SELECT", "FROM", "WHERE", "AND", "OR", "ORDER BY",
                "GROUP BY", "HAVING", "LIMIT", "JOIN", "INNER JOIN", "LEFT JOIN",
                "RIGHT JOIN", "ON", "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM"};

        String formatted = sql.trim();
        for (String keyword : keywords) {
            formatted = formatted.replaceAll("(?i)\\b" + keyword + "\\b", "\n" + keyword);
        }
        // Clean up multiple newlines
        formatted = formatted.replaceAll("\n{3,}", "\n\n").trim();
        return formatted;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_query_editor, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentDraft();
    }

    private void saveCurrentDraft() {
        if (currentDbName != null) {
            String draft = etQueryEditor.getText().toString();
            getSharedPreferences("mysqlwb_query_drafts", MODE_PRIVATE)
                    .edit()
                    .putString("draft_" + currentDbName, draft)
                    .apply();
        }
    }

    private void loadDraft() {
        if (currentDbName != null) {
            String draft = getSharedPreferences("mysqlwb_query_drafts", MODE_PRIVATE)
                    .getString("draft_" + currentDbName, "");
            if (!draft.isEmpty()) {
                etQueryEditor.setText(draft);
                etQueryEditor.setSelection(draft.length());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_history) {
            showQueryHistoryDialog();
            return true;
        } else if (id == R.id.action_saved_queries) {
            showSavedQueriesDialog();
            return true;
        } else if (id == R.id.action_save_query) {
            showSaveQueryDialog();
            return true;
        } else if (id == R.id.action_change_db) {
            showChangeDatabaseDialog();
            return true;
        } else if (id == R.id.action_schema) {
            Intent intent = new Intent(this, SchemaExplorerActivity.class);
            intent.putExtra("db_name", currentDbName);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_table_designer) {
            Intent intent = new Intent(this, TableDesignerActivity.class);
            intent.putExtra("db_name", currentDbName);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_copy_result) {
            copyResultToClipboard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showQueryHistoryDialog() {
        List<com.mysqlwb.models.QueryHistory> history = historyManager.getHistory(50, currentDbName);
        if (history.isEmpty()) {
            Toast.makeText(this, "No history for this database", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[history.size()];
        for (int i = 0; i < history.size(); i++) {
            String sql = history.get(i).getSql();
            items[i] = (history.get(i).isSuccess() ? "✅ " : "❌ ") +
                    (sql.length() > 60 ? sql.substring(0, 60) + "..." : sql);
        }

        new AlertDialog.Builder(this)
                .setTitle("Query History")
                .setItems(items, (d, which) -> {
                    String selectedSql = history.get(which).getSql();
                    String currentText = etQueryEditor.getText().toString();
                    
                    if (!currentText.isEmpty()) {
                        if (!currentText.endsWith("\n")) {
                            etQueryEditor.append("\n\n");
                        } else if (!currentText.endsWith("\n\n")) {
                            etQueryEditor.append("\n");
                        }
                    }
                    
                    etQueryEditor.append(selectedSql);
                    etQueryEditor.setSelection(etQueryEditor.getText().length());
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSavedQueriesDialog() {
        List<com.mysqlwb.models.QueryHistory> saved = historyManager.getSavedQueries(currentDbName);
        if (saved.isEmpty()) {
            Toast.makeText(this, "No saved queries for this database", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[saved.size()];
        for (int i = 0; i < saved.size(); i++) {
            items[i] = saved.get(i).getDatabase(); // We stored the name in 'database' field for saved queries
        }

        new AlertDialog.Builder(this)
                .setTitle("Saved Queries (" + (currentDbName != null ? currentDbName : "Global") + ")")
                .setItems(items, (d, which) -> {
                    String selectedSql = saved.get(which).getSql();
                    String currentText = etQueryEditor.getText().toString();

                    if (!currentText.isEmpty()) {
                        if (!currentText.endsWith("\n")) {
                            etQueryEditor.append("\n\n");
                        } else if (!currentText.endsWith("\n\n")) {
                            etQueryEditor.append("\n");
                        }
                    }

                    etQueryEditor.append(selectedSql);
                    etQueryEditor.setSelection(etQueryEditor.getText().length());
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSaveQueryDialog() {
        String sql = etQueryEditor.getText().toString().trim();
        if (sql.isEmpty()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText et = new EditText(this);
        et.setHint("Query name...");
        new AlertDialog.Builder(this)
                .setTitle("Save Query")
                .setView(et)
                .setPositiveButton("Save", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) name = sql.substring(0, Math.min(30, sql.length()));
                    historyManager.saveQuery(name, sql, currentDbName);
                    Toast.makeText(this, "Query saved!", Toast.LENGTH_SHORT).show();
                    // Refresh output if showing saved queries
                    if (layoutResults.getChildCount() > 0 && 
                        layoutResults.getChildAt(0) instanceof TextView &&
                        ((TextView)layoutResults.getChildAt(0)).getText().toString().contains("Saved Queries")) {
                        clearOutput();
                        showSavedQueriesForDatabase();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangeDatabaseDialog() {
        List<com.mysqlwb.models.DatabaseInfo> databases = dbEngine.listDatabases();
        String[] names = new String[databases.size()];
        for (int i = 0; i < databases.size(); i++) {
            names[i] = databases.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Switch Database")
                .setItems(names, (d, which) -> {
                    saveCurrentDraft(); // Save current before switching
                    currentDbName = names[which];
                    dbEngine.openDatabase(currentDbName);
                    updateDbIndicator();
                    clearOutput();
                    etQueryEditor.setText(""); // Reset before loading next draft
                    loadDraft();
                    if (etQueryEditor.getText().length() == 0) {
                        showWelcomeOutput();
                    }
                    showSavedQueriesForDatabase();
                    clearUndoHistory();
                    Toast.makeText(this, "Switched to: " + currentDbName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void copyResultToClipboard() {
        // Collect all text from result views
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < layoutResults.getChildCount(); i++) {
            View child = layoutResults.getChildAt(i);
            if (child instanceof TextView) {
                sb.append(((TextView) child).getText()).append("\n");
            }
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Query Result", sb.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
