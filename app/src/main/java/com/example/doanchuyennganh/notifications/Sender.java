package com.example.doanchuyennganh.notifications;

public class Sender {
    private  Date date;
    private  String to;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Sender() {
    }

    public Sender(Date date, String to) {
        this.date = date;
        this.to = to;
    }



}
