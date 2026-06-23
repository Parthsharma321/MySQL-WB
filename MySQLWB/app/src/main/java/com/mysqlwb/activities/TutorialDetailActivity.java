package com.mysqlwb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import com.mysqlwb.R;
import com.mysqlwb.models.Tutorial;

import java.util.List;

public class TutorialDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_detail);

        int index = getIntent().getIntExtra("tutorial_index", 0);
        List<Tutorial> tutorials = TutorialsActivity.buildTutorials();

        if (index < 0 || index >= tutorials.size()) {
            finish();
            return;
        }

        Tutorial tutorial = tutorials.get(index);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(tutorial.getTitle());
        }

        // Bind views
        TextView tvIcon    = findViewById(R.id.tv_tutorial_icon);
        TextView tvTitle   = findViewById(R.id.tv_tutorial_title);
        TextView tvMeta    = findViewById(R.id.tv_tutorial_meta);
        TextView tvContent = findViewById(R.id.tv_tutorial_content);
        TextView tvSql     = findViewById(R.id.tv_example_sql);

        tvIcon.setText(tutorial.getIcon());
        tvTitle.setText(tutorial.getTitle());
        tvMeta.setText(tutorial.getDifficulty() + " · " + tutorial.getCategory());

        // Render HTML content for rich formatting
        String content = tutorial.getContent();
        // Custom styling for code blocks if they exist as <pre> or similar
        content = content.replace("```sql<br>", "<br><font color='#569CD6'><b>")
                        .replace("<br>```", "</b></font><br>");

        tvContent.setText(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));

        // Show example SQL
        String exSql = tutorial.getExampleSql();
        if (exSql != null && !exSql.isEmpty()) {
            tvSql.setText(exSql);
        } else {
            tvSql.setText("-- No example for this lesson");
        }

        // Try in editor button
        findViewById(R.id.btn_try_in_editor).setOnClickListener(v -> {
            Intent intent = new Intent(this, QueryEditorActivity.class);
            if (exSql != null && !exSql.isEmpty()) {
                intent.putExtra("initial_query", exSql);
                // Default to school_db for tutorials as most examples use it
                intent.putExtra("db_name", "school_db");
            }
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyEditorSettings();
    }

    private void applyEditorSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("mysqlwb_prefs", MODE_PRIVATE);
        int fontSize = prefs.getInt("editor_font_size", 14);
        TextView tvSql = findViewById(R.id.tv_example_sql);
        if (tvSql != null) {
            tvSql.setTextSize(fontSize);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
