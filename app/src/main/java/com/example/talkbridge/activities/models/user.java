package com.example.talkbridge.activities.models;

public class user {
    private String uId,name,profile,city;
    long coins;
    public user(){

    }


    public user(String uId, String name, String profile, String city,long coins) {
        this.uId = uId;
        this.name = name;
        this.profile = profile;
        this.city = city;
        this.coins=coins;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }
}