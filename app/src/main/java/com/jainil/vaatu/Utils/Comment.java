package com.jainil.vaatu.Utils;

public class Comment {
    String comment,profileImageUrl, username,commentId;

    public Comment() {
    }

    public Comment(String comment, String profileImageUrl, String username, String commentId) {
        this.comment = comment;
        this.profileImageUrl = profileImageUrl;
        this.username = username;
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
