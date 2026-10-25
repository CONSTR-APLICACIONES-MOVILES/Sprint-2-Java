package com.example.parchapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.domain.model.PlannedActivity;
import com.example.parchapp.util.Callback;

import java.util.List;

public class CalendarSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.sheet_close_btn).setOnClickListener(v -> dismiss());
        Repositories.activities().getActivities(UserSession.getCurrentUser().getId(), new Callback<List<PlannedActivity>>() {
            @Override
            public void onSuccess(List<PlannedActivity> activities) {
                if (!isAdded() || activities.isEmpty()) {
                    return;
                }
                PlannedActivity activity = activities.get(0);
                ((android.widget.TextView) view.findViewById(R.id.calendar_time_text)).setText(
                        (activity.getDate() + " " + activity.getTime()).trim());
                ((android.widget.TextView) view.findViewById(R.id.calendar_title_text)).setText(activity.getTitle());
                ((android.widget.TextView) view.findViewById(R.id.calendar_location_text)).setText(activity.getLocation());
                ((android.widget.TextView) view.findViewById(R.id.calendar_free_text)).setText("");
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }
}
