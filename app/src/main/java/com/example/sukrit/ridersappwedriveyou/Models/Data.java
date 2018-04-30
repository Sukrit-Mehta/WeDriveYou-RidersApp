package com.example.sukrit.ridersappwedriveyou.Models;

/**
 * Created by sukrit on 25/4/18.
 */

public class Data {
    String detail;
    String body;

    public Data() {
    }

    public Data(String detail, String body) {
        this.detail = detail;
        this.body = body;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
