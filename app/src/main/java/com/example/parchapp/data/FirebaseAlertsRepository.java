package com.example.parchapp.data;

import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FirebaseAlertsRepository implements AlertsRepository {
    private final FirebaseFunctions functions;

    FirebaseAlertsRepository() {
        functions = FirebaseFunctions.getInstance("us-central1");
    }

    @Override
    public void getAlerts(Callback<List<Alert>> callback) {
        functions.getHttpsCallable("getAlerts").call(Collections.emptyMap())
                .addOnSuccessListener(result -> {
                    List<Alert> alerts = new ArrayList<>();
                    Object raw = result.getData() instanceof Map
                            ? ((Map<?, ?>) result.getData()).get("alerts") : null;
                    if (raw instanceof List) {
                        for (Object item : (List<?>) raw) {
                            if (item instanceof Map) alerts.add(Alert.fromMap((Map<?, ?>) item));
                        }
                    }
                    MainThread.post(() -> callback.onSuccess(alerts));
                })
                .addOnFailureListener(error -> MainThread.post(() -> callback.onError(error)));
    }

    @Override
    public void markRead(List<String> ids, Callback<Void> callback) {
        Map<String, Object> input = new HashMap<>();
        input.put("ids", ids);
        functions.getHttpsCallable("markAlertsRead").call(input)
                .addOnSuccessListener(result -> MainThread.post(() -> callback.onSuccess(null)))
                .addOnFailureListener(error -> MainThread.post(() -> callback.onError(error)));
    }

    @Override
    public void respond(String activityId, String response, Callback<Void> callback) {
        Map<String, Object> input = new HashMap<>();
        input.put("id", activityId);
        input.put("response", response);
        functions.getHttpsCallable("respondToActivityInvitation").call(input)
                .addOnSuccessListener(result -> MainThread.post(() -> callback.onSuccess(null)))
                .addOnFailureListener(error -> MainThread.post(() -> callback.onError(error)));
    }
}
