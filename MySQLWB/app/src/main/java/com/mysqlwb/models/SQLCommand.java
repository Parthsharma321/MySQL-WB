package com.mysqlwb.models;

public class SQLCommand {
    private String command;
    private String category;
    private String description;
    private String syntax;
    private String tip;

    public SQLCommand(String command, String category, String description, String syntax, String tip) {
        this.command = command;
        this.category = category;
        this.description = description;
        this.syntax = syntax;
        this.tip = tip;
    }

    public String getCommand() { return command; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getSyntax() { return syntax; }
    public String getTip() { return tip; }
}
