package com.example.parchapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parchapp.analytics.AnalyticsEvents;
import com.example.parchapp.analytics.AnalyticsTracker;
import com.example.parchapp.analytics.ActivityCreationTelemetry;
import com.example.parchapp.data.GroupListener;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.PlannedActivity;
import com.example.parchapp.util.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateGroupActivity extends AppCompatActivity implements GroupListener {
    private static final String SCREEN = "create_group";

    private final AnalyticsTracker tracker = AnalyticsTracker.getInstance();
    private LinearLayout emailChips;
    private EditText emailInput;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupId = getIntent().getStringExtra(GroupDetailActivity.EXTRA_GROUP_ID);
        emailChips = findViewById(R.id.email_chips);
        emailInput = findViewById(R.id.email_input_group);

        findViewById(R.id.header_search_btn).setOnClickListener(v -> toast("Search coming soon"));
        findViewById(R.id.header_notif_btn).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_ALERTS, SCREEN);
            toast("No new notifications");
        });
        findViewById(R.id.header_profile_btn).setOnClickListener(v -> toast("Profile coming soon"));

        findViewById(R.id.back_group_button).setOnClickListener(v -> finish());
        findViewById(R.id.activity_date_input).setOnClickListener(v -> pickDate());
        findViewById(R.id.activity_time_input).setOnClickListener(v -> pickTime());
        findViewById(R.id.copy_group_link_button).setOnClickListener(v -> copyGroupLink());
        findViewById(R.id.share_group_link_button).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_INVITATIONS, SCREEN);
            toast("Share link opened");
        });
        findViewById(R.id.show_qr_button).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_INVITATIONS, SCREEN);
            View qr = findViewById(R.id.qr_container);
            qr.setVisibility(qr.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        findViewById(R.id.add_email_button).setOnClickListener(v -> addEmail());
        findViewById(R.id.create_group_button).setOnClickListener(v -> createPlan());
        findViewById(R.id.cancel_create_group).setOnClickListener(v -> finish());
        findViewById(R.id.nav_home_create).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.nav_schedule_create).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_SCHEDULE, SCREEN);
            new CalendarSheet().show(getSupportFragmentManager(), "calendar_navigation");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (groupId != null) {
            Repositories.groups().observeGroup(groupId, this);
        } else {
            Repositories.groups().getGroups(UserSession.getCurrentUser().getId(), new Callback<List<Group>>() {
                @Override
                public void onSuccess(List<Group> groups) {
                    if (!groups.isEmpty()) {
                        groupId = groups.get(0).getId();
                        Repositories.groups().observeGroup(groupId, CreateGroupActivity.this);
                    }
                }

                @Override
                public void onError(Exception error) {
                    toast("Could not load your groups");
                }
            });
        }
    }

    @Override
    protected void onStop() {
        if (groupId != null) {
            Repositories.groups().removeGroupListener(groupId, this);
        }
        super.onStop();
    }

    /** Pending invitations come from the group directly as it ois, so every open screen shows the same list as it should. */
    @Override
    public void onGroupChanged(Group group) {
        emailChips.removeAllViews();
        for (String email : group.getPendingInvites()) {
            TextView chip = new TextView(this);
            chip.setText(email + "  ×");
            chip.setTextSize(11);
            chip.setTextColor(getColor(R.color.brand_primary));
            chip.setBackgroundResource(R.drawable.bg_light_blue);
            chip.setPadding(18, 8, 18, 8);
            chip.setOnClickListener(v -> Repositories.groups().removeInvite(groupId, email));
            emailChips.addView(chip);
        }
    }

    private void addEmail() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailInput.setError("Enter an email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            return;
        }
        tracker.trackFeature(AnalyticsEvents.FEATURE_INVITATIONS, SCREEN);
        Repositories.groups().addInvite(groupId, email);
        emailInput.setText("");
    }

    private void createPlan() {
        EditText nameInput = findViewById(R.id.group_name_input);
        String title = nameInput.getText().toString().trim();
        if (title.isEmpty()) {
            nameInput.setError("Enter a name");
            return;
        }
        String date = ((EditText) findViewById(R.id.activity_date_input)).getText().toString().trim();
        String time = ((EditText) findViewById(R.id.activity_time_input)).getText().toString().trim();
        PlannedActivity activity = new PlannedActivity(null, groupId, title, "other", date, time, "",
                PlannedActivity.STATUS_PROPOSED, System.currentTimeMillis());

        View createButton = findViewById(R.id.create_group_button);
        createButton.setEnabled(false);
        String creationSessionId = UUID.randomUUID().toString();
        long creationStartedAt = System.currentTimeMillis();
        ActivityCreationTelemetry.attempt(creationSessionId, "propose_plan", 0);
        Repositories.activities().createActivity(activity, new Callback<PlannedActivity>() {
            @Override
            public void onSuccess(PlannedActivity saved) {
                ActivityCreationTelemetry.result(creationSessionId, "propose_plan",
                        System.currentTimeMillis() - creationStartedAt, true, saved.getId(), null);
                Map<String, Object> params = new HashMap<>();
                params.put(AnalyticsEvents.PARAM_CATEGORY, "other");
                params.put(AnalyticsEvents.PARAM_SOURCE, "propose_plan");
                params.put(AnalyticsEvents.PARAM_CREATION_SESSION_ID, creationSessionId);
                params.put(AnalyticsEvents.PARAM_ACTIVITY_ID, saved.getId());
                tracker.track(AnalyticsEvents.ACTIVITY_CREATED, params);
                createButton.setEnabled(true);
                startActivity(new Intent(CreateGroupActivity.this, ActivityCreatedActivity.class)
                        .putExtra(ActivityCreatedActivity.EXTRA_TITLE, saved.getTitle())
                        .putExtra(ActivityCreatedActivity.EXTRA_DATE, saved.getDate())
                        .putExtra(ActivityCreatedActivity.EXTRA_TIME, saved.getTime())
                        .putExtra(ActivityCreatedActivity.EXTRA_LOCATION, saved.getLocation()));
            }

            @Override
            public void onError(Exception error) {
                ActivityCreationTelemetry.result(creationSessionId, "propose_plan",
                        System.currentTimeMillis() - creationStartedAt, false, null,
                        error.getClass().getSimpleName());
                createButton.setEnabled(true);
                toast("Could not create the plan. Try again.");
            }
        });
    }

    private void copyGroupLink() {
        tracker.trackFeature(AnalyticsEvents.FEATURE_INVITATIONS, SCREEN);
        String link = ((TextView) findViewById(R.id.group_link_text)).getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Group link", link));
        toast("Group link copied");
    }

    private void pickDate() {
        EditText input = findViewById(R.id.activity_date_input);
        Calendar today = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            input.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.getTime()));
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        EditText input = findViewById(R.id.activity_time_input);
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(Calendar.HOUR_OF_DAY, hour);
            selected.set(Calendar.MINUTE, minute);
            input.setText(new SimpleDateFormat("h:mm a", Locale.US).format(selected.getTime()));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
