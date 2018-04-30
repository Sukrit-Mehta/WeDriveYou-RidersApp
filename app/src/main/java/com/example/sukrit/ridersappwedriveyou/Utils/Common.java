package com.example.sukrit.ridersappwedriveyou.Utils;

import com.example.sukrit.ridersappwedriveyou.Remote.IFCMService;
import com.example.sukrit.ridersappwedriveyou.Remote.FCMClient;

/**
 * Created by sukrit on 8/4/18.
 */

public class Common {
    public static final String driver_tb1 = "Drivers";
    public static final String user_driver_tb1 = "DriversInformation";
    public static final String user_rider_tb1 = "RidersInformation";
    public static final String pickup_request_tb1 = "PickupRequest";
    public static final String token_tb1 = "Tokens";

    public static final String fcmURL = "https://fcm.googleapis.com";
    public static final String FCM_Url = "https://fcm.googleapis.com/fcm/send";

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
    public static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2, String unit) {
        Double theta = lon1 - lon2;
        Double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    private static Double deg2rad(Double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static Double rad2deg(Double rad) {
        return (rad * 180 / Math.PI);
    }

}
