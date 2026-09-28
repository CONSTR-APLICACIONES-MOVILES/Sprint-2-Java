package com.example.parchapp.data;

import com.example.parchapp.domain.model.PlannedActivity;
import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class InMemoryActivityRepository implements ActivityRepository {
    private final List<PlannedActivity> activities = new ArrayList<>();

    @Override
    public void createActivity(PlannedActivity activity, Callback<PlannedActivity> callback) {
        PlannedActivity saved = activity.withId(UUID.randomUUID().toString());
        activities.add(saved);
        MainThread.post(() -> callback.onSuccess(saved));
    }
}
