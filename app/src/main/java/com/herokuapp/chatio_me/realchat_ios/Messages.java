package com.herokuapp.chatio_me.realchat_ios;

/**
 * Created by Tandv on 12/14/2017.
 */

public class Messages {
    private String message, type;
    private boolean seen;
    private long time;
    private String from;

    public Messages(){

    }

    public Messages(String from,String messages,boolean seen , long time, String type) {
        this.message= messages;
        this.type = type;
        this.seen = seen;
        this.time = time;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String messages) {
        this.message = messages;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
