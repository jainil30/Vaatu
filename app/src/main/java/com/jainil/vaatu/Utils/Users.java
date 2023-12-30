package com.jainil.vaatu.Utils;

public class Users {
    String username;
    String profileImage;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public Users(String username, String profileImage, String country, String city, String profession) {
        this.username = username;
        this.profileImage = profileImage;
        this.country = country;
        this.city = city;
        this.profession = profession;
    }

    public Users() {
    }

    String country;
    String city;
    String profession;


}
