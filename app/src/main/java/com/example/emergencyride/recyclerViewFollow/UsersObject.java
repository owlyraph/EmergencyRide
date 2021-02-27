package com.example.emergencyride.recyclerViewFollow;

public class UsersObject {
    private String email;
    private String uId;

    public UsersObject(String email,String uId){
        this.email=email;
        this.uId=uId;
    }

    public String getUId(){
        return uId;
    }

    public void setUId(String uId){
        this.uId=uId;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email=email;
    }
}
