package com.mysqlwb.models;

public class Tutorial {
    private String title;
    private String difficulty;
    private String description;
    private String icon;
    private String content;
    private String exampleSql;
    private String category;

    public Tutorial(String title, String difficulty, String description,
                    String icon, String content, String exampleSql) {
        this.title = title;
        this.difficulty = difficulty;
        this.description = description;
        this.icon = icon;
        this.content = content;
        this.exampleSql = exampleSql;
        this.category = inferCategory(title);
    }

    private String inferCategory(String t) {
        String u = t.toUpperCase();
        if (u.contains("DATABASE") || u.contains("TABLE") || u.contains("CREATE") || u.contains("INDEX")) return "DDL";
        if (u.contains("INSERT") || u.contains("UPDATE") || u.contains("DELETE")) return "DML";
        if (u.contains("SELECT") || u.contains("JOIN") || u.contains("AGGREGATE") || u.contains("GROUP")
                || u.contains("WHERE") || u.contains("SUBQUERY")) return "DQL";
        return "Basics";
    }

    public String getTitle()       { return title; }
    public String getDifficulty()  { return difficulty; }
    public String getDescription() { return description; }
    public String getIcon()        { return icon; }
    public String getContent()     { return content; }
    public String getExampleSql()  { return exampleSql; }
    public String getCategory()    { return category; }
}
