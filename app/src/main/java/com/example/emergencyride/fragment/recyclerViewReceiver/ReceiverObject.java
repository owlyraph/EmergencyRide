package com.example.emergencyride.fragment.recyclerViewReceiver;

public class ReceiverObject {
    private String email;
    private String uId;
    private Boolean receive;

    public ReceiverObject(String email, String uId, Boolean receive){
        this.email=email;
        this.uId=uId;
        this.receive = receive;
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

    public Boolean getReceive(){
        return receive;
    }
    public void setReceive(Boolean receive){
        this.receive = receive;
    }
}
