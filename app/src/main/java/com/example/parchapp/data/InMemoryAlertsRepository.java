package com.example.parchapp.data;

import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;

import java.util.Collections;
import java.util.List;

class InMemoryAlertsRepository implements AlertsRepository {
    @Override
    public void getAlerts(Callback<List<Alert>> callback) {
        MainThread.post(() -> callback.onSuccess(Collections.emptyList()));
    }

    @Override
    public void markRead(List<String> ids, Callback<Void> callback) {
        MainThread.post(() -> callback.onSuccess(null));
    }

    @Override
    public void respond(String activityId, String response, Callback<Void> callback) {
        MainThread.post(() -> callback.onSuccess(null));
    }
}
