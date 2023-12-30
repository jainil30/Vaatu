package com.jainil.vaatu.Utils;

public class Posts {
    private String username, datePost, postDescription, postImageUrl,userProfileImage;

    public Posts(String username, String datePost, String postDescription, String postImageUrl, String userProfileImage) {
        this.username = username;
        this.datePost = datePost;
        this.postDescription = postDescription;
        this.postImageUrl = postImageUrl;
        this.userProfileImage = userProfileImage;
    }

    public Posts() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDatePost() {
        return datePost;
    }

    public void setDatePost(String datePost) {
        this.datePost = datePost;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }
}
