package com.example.watchaura.entity.Chat;

public class ChatResponse {
    private String response;
    private boolean success;
    private long timestamp;

    public ChatResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatResponse(String response, boolean success) {
        this.response = response;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}