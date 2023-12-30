package com.jainil.vaatu.Utils;

public class Chat {
    private String message;

    public Chat() {
    }

    public Chat(String message, String status, String userId) {
        this.message = message;
        this.status = status;
        this.userId = userId;
    }

    private String status;
    private String userId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
