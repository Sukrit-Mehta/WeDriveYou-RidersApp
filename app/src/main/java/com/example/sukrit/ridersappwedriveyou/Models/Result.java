package com.example.sukrit.ridersappwedriveyou.Models;

/**
 * Created by sukrit on 25/4/18.
 */

public class Result {
    String message_id;

    public Result() {
    }

    public Result(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}
