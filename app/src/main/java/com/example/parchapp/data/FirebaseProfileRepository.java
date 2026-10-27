package com.example.parchapp.data;

import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class FirebaseProfileRepository implements ProfileRepository {
    private final FirebaseFunctions functions = FirebaseFunctions.getInstance("us-central1");

    @Override
    public void getProfile(Callback<ProfileData> callback) {
        functions.getHttpsCallable("getProfile").call(Collections.emptyMap())
                .addOnSuccessListener(result -> MainThread.post(() ->
                        callback.onSuccess(ProfileData.fromMap((Map<?, ?>) result.getData()))))
                .addOnFailureListener(error -> MainThread.post(() -> callback.onError(error)));
    }

    @Override
    public void updateProfile(Map<String, Object> fields, Callback<ProfileData> callback) {
        Map<String, Object> input = new HashMap<>();
        input.put("fields", fields);
        functions.getHttpsCallable("updateProfile").call(input)
                .addOnSuccessListener(result -> MainThread.post(() ->
                        callback.onSuccess(ProfileData.fromMap((Map<?, ?>) result.getData()))))
                .addOnFailureListener(error -> MainThread.post(() -> callback.onError(error)));
    }

    @Override
    public void updatePresence(String status, String visibility, String expiresAt, Callback<Void> callback) {
        Map<String, Object> input = new HashMap<>();
        input.put("status", status);
        input.put("visibility", visibility);
        input.put("expiresAt", expiresAt);
        functions.getHttpsCallable("updatePresence").call(input)
                .addOnSuccessListener(result -> MainThread.post(() -> callback.onSuccess(null)))
                .addOnFailureListener(error -> MainThread.post(() -> callback.onError(error)));
    }
}
