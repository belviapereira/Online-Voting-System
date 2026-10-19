package com.votingsystem.model;

public class Election {
    private int id;
    private String title;
    private boolean isActive;

    public Election(int id, String title, boolean isActive) {
        this.id = id;
        this.title = title;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isActive() { return isActive; }
    
    @Override
    public String toString() { return title; }
}