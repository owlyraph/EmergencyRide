package com.example.emergencyride.fragment.recyclerViewStory;

public class StoryObject {
    private String email;
    private String uId;
    private String chatOrStory;

    public StoryObject(String email, String uId, String chatOrStory){
        this.email=email;
        this.uId=uId;
        this.chatOrStory=chatOrStory;
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

    public String getChatOrStory(){
        return chatOrStory;
    }

    public void setChatOrStory(String chatOrStory){
        this.chatOrStory=chatOrStory;
    }

    @Override
    public boolean equals(Object obj) {

        boolean same = false;
        if(obj != null && obj instanceof StoryObject){
            same = this.uId == ((StoryObject) obj ).uId;
        }
        return same;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (this.uId == null ? 0 : this.uId.hashCode());
        return result;
    }
}

