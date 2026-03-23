package com.anhnvt_ph55017.md_02_datn.models;

public class SearchHistory {

    private int id;
    private String keyword;
    private String createdAt;

    public SearchHistory() {
    }

    public SearchHistory(String keyword, String createdAt) {
        this.keyword = keyword;
        this.createdAt = createdAt;
    }

    public SearchHistory(int id, String keyword, String createdAt) {
        this.id = id;
        this.keyword = keyword;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
