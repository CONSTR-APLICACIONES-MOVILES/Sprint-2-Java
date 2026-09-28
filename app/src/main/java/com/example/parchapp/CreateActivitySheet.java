package com.example.parchapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.parchapp.analytics.AnalyticsEvents;
import com.example.parchapp.analytics.AnalyticsTracker;
import com.example.parchapp.data.DemoData;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.domain.model.PlannedActivity;
import com.example.parchapp.util.Callback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;

public class CreateActivitySheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "arg_group_id";
    private static final String ARG_GROUP_SIZE = "arg_group_size";
    private static final String ARG_SLOT_TIME = "arg_slot_time";
    private static final String ARG_SLOT_POSITION = "arg_slot_position";
    private static final String ARG_RECOMMENDED_POSITION = "arg_recommended_position";
    private static final int NO_SLOT = -1;

    private TextView selectedGroupChip;
    private String selectedGroupId = DemoData.ENGINEERING_ID;

    /** In this examples, it opens the sheet with a slot chosen in Compare Availability, so BQ3 can be measured in this case. */
    public static CreateActivitySheet newInstance(String groupId, int groupSize, String slotTime,
                                                  int slotPosition, int recommendedPosition) {
        CreateActivitySheet sheet = new CreateActivitySheet();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putInt(ARG_GROUP_SIZE, groupSize);
        args.putString(ARG_SLOT_TIME, slotTime);
        args.putInt(ARG_SLOT_POSITION, slotPosition);
        args.putInt(ARG_RECOMMENDED_POSITION, recommendedPosition);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_create_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        String suggestedGroupId = args.getString(ARG_GROUP_ID);
        String suggestedTime = args.getString(ARG_SLOT_TIME);
        int slotPosition = args.getInt(ARG_SLOT_POSITION, NO_SLOT);

        EditText titleInput = view.findViewById(R.id.input_activity_title);
        EditText dateInput = view.findViewById(R.id.input_activity_date);
        EditText timeInput = view.findViewById(R.id.input_activity_time);
        EditText locationInput = view.findViewById(R.id.input_activity_location);
        if (suggestedTime != null) {
            timeInput.setText(suggestedTime);
        }

        TextView engChip = view.findViewById(R.id.chip_group_eng);
        TextView roomiesChip = view.findViewById(R.id.chip_group_roomies);
        selectedGroupChip = engChip;
        engChip.setOnClickListener(v -> selectGroup(engChip, DemoData.ENGINEERING_ID));
        roomiesChip.setOnClickListener(v -> selectGroup(roomiesChip, DemoData.ROOMIES_ID));
        if (DemoData.ROOMIES_ID.equals(suggestedGroupId)) {
            selectGroup(roomiesChip, DemoData.ROOMIES_ID);
        }

        view.findViewById(R.id.sheet_close_btn).setOnClickListener(v -> dismiss());
        View createButton = view.findViewById(R.id.btn_create_activity);
        createButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                titleInput.setError("Enter an activity title");
                return;
            }
            String time = timeInput.getText().toString().trim();
            PlannedActivity activity = new PlannedActivity(null, selectedGroupId, title, "other",
                    dateInput.getText().toString().trim(), time,
                    locationInput.getText().toString().trim(), PlannedActivity.STATUS_PROPOSED,
                    System.currentTimeMillis());

            createButton.setEnabled(false);
            Activity host = requireActivity();
            Repositories.activities().createActivity(activity, new Callback<PlannedActivity>() {
                @Override
                public void onSuccess(PlannedActivity saved) {
                    trackCreation(slotPosition, args, !time.equals(suggestedTime)
                            || !selectedGroupId.equals(suggestedGroupId));
                    if (isAdded()) {
                        dismiss();
                    }
                    host.startActivity(new Intent(host, ActivityCreatedActivity.class));
                }

                @Override
                public void onError(Exception error) {
                    createButton.setEnabled(true);
                    Toast.makeText(host, "Could not create the activity. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void trackCreation(int slotPosition, Bundle args, boolean modified) {
        AnalyticsTracker tracker = AnalyticsTracker.getInstance();
        Map<String, Object> created = new HashMap<>();
        created.put(AnalyticsEvents.PARAM_CATEGORY, "other");
        created.put(AnalyticsEvents.PARAM_SOURCE, slotPosition == NO_SLOT ? "manual" : "suggested_slot");
        tracker.track(AnalyticsEvents.ACTIVITY_CREATED, created);

        if (slotPosition == NO_SLOT) {
            return;
        }
        // BQ3: was the suggested slot kept exactly as proposed (really??)?
        Map<String, Object> accepted = new HashMap<>();
        accepted.put(AnalyticsEvents.PARAM_GROUP_SIZE, args.getInt(ARG_GROUP_SIZE));
        accepted.put(AnalyticsEvents.PARAM_SLOT_POSITION, slotPosition);
        accepted.put(AnalyticsEvents.PARAM_RECOMMENDED_POSITION, args.getInt(ARG_RECOMMENDED_POSITION));
        accepted.put(AnalyticsEvents.PARAM_MODIFIED, modified);
        tracker.track(AnalyticsEvents.SLOT_ACCEPTED, accepted);
    }

    private void selectGroup(TextView chip, String groupId) {
        selectedGroupChip.setBackgroundResource(R.drawable.bg_chip_unselected);
        selectedGroupChip.setTextColor(requireContext().getColor(R.color.brand_dark));
        selectedGroupChip = chip;
        selectedGroupId = groupId;
        chip.setBackgroundResource(R.drawable.bg_chip_selected);
        chip.setTextColor(requireContext().getColor(R.color.brand_primary));
    }
}
