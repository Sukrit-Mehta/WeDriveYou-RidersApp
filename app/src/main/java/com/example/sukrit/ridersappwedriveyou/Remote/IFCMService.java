package com.example.sukrit.ridersappwedriveyou.Remote;

import com.example.sukrit.ridersappwedriveyou.Models.FCMResponse;
import com.example.sukrit.ridersappwedriveyou.Models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by sukrit on 25/4/18.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=<AAAAv8t6tlE:APA91bHZ9IIEvWHgxDUT0O5bt4vXUjbUnOO_LWfSjnVLrH2kTDElmIjwpFM5zUfxixlpXSG9Kq1QbWLvo7QgbCS2mBOawPUGACzhLPukqqnJXxmnQ24D2edquGFXc8GIsESiPqDEsr97>"
            })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
