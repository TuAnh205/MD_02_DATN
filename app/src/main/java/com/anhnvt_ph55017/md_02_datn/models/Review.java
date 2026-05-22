package com.anhnvt_ph55017.md_02_datn.models;

public class Review {
    private String id;
    private String userName;
    private String userId;
    private String content;
    private float rating;
    private String createdAt;
    private String responseText;
    private String responseByName;
    private String responseDate;

    public Review(String id, String userName, String userId, String content, float rating, String createdAt) {
        this(id, userName, userId, content, rating, createdAt, "", "", "");
    }

    public Review(String id, String userName, String userId, String content, float rating, String createdAt,
                  String responseText, String responseByName, String responseDate) {
        this.id = id;
        this.userName = userName;
        this.userId = userId;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
        this.responseText = responseText;
        this.responseByName = responseByName;
        this.responseDate = responseDate;
    }

    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public float getRating() { return rating; }
    public String getCreatedAt() { return createdAt; }
    public String getResponseText() { return responseText; }
    public String getResponseByName() { return responseByName; }
    public String getResponseDate() { return responseDate; }

    public void setId(String id) { this.id = id; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setContent(String content) { this.content = content; }
    public void setRating(float rating) { this.rating = rating; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
    public void setResponseByName(String responseByName) { this.responseByName = responseByName; }
    public void setResponseDate(String responseDate) { this.responseDate = responseDate; }
}
