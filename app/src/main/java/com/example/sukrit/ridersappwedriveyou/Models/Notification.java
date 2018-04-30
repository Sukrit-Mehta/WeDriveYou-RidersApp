package com.example.sukrit.ridersappwedriveyou.Models;

/**
 * Created by sukrit on 25/4/18.
 */

public class Notification {

    public String title;
    public String body;

    public Notification() {
    }

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
