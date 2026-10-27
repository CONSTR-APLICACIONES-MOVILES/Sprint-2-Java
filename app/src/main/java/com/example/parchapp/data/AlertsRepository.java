package com.example.parchapp.data;

import com.example.parchapp.util.Callback;

import java.util.List;

public interface AlertsRepository {
    void getAlerts(Callback<List<Alert>> callback);
    void markRead(List<String> ids, Callback<Void> callback);
    void respond(String activityId, String response, Callback<Void> callback);
}
