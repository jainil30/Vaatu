package com.jainil.vaatu.Utils;

public class Friends {
    private String profession, profileImageUrl
, username, status;

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getprofileImageUrl
() {
        return profileImageUrl
;
    }

    public void setprofileImageUrl
(String profileImageUrl
) {
        this.profileImageUrl
 = profileImageUrl
;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Friends() {
    }

    public Friends(String profession, String profileImageUrl
, String username, String status) {
        this.profession = profession;
        this.profileImageUrl
 = profileImageUrl
;
        this.username = username;
        this.status = status;
    }
}
