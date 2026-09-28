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

import androidx.appcompat.app.AppCompatActivity;

import com.example.parchapp.analytics.AnalyticsEvents;
import com.example.parchapp.analytics.AnalyticsTracker;
import com.example.parchapp.data.DemoData;
import com.example.parchapp.data.GroupListener;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.PlannedActivity;
import com.example.parchapp.util.Callback;

import java.util.HashMap;
import java.util.Map;

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
        if (groupId == null) {
            groupId = DemoData.ROOMIES_ID;
        }
        emailChips = findViewById(R.id.email_chips);
        emailInput = findViewById(R.id.email_input_group);

        findViewById(R.id.header_search_btn).setOnClickListener(v -> toast("Search coming soon"));
        findViewById(R.id.header_notif_btn).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_ALERTS, SCREEN);
            toast("No new notifications");
        });
        findViewById(R.id.header_profile_btn).setOnClickListener(v -> toast("Profile coming soon"));

        findViewById(R.id.back_group_button).setOnClickListener(v -> finish());
        findViewById(R.id.activity_date_input).setOnClickListener(v -> toast("Date picker coming soon"));
        findViewById(R.id.activity_time_input).setOnClickListener(v -> toast("Time picker coming soon"));
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
            toast("Schedule coming soon");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Repositories.groups().observeGroup(groupId, this);
    }

    @Override
    protected void onStop() {
        Repositories.groups().removeGroupListener(groupId, this);
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
        Repositories.activities().createActivity(activity, new Callback<PlannedActivity>() {
            @Override
            public void onSuccess(PlannedActivity saved) {
                Map<String, Object> params = new HashMap<>();
                params.put(AnalyticsEvents.PARAM_CATEGORY, "other");
                params.put(AnalyticsEvents.PARAM_SOURCE, "propose_plan");
                tracker.track(AnalyticsEvents.ACTIVITY_CREATED, params);
                createButton.setEnabled(true);
                startActivity(new Intent(CreateGroupActivity.this, ActivityCreatedActivity.class));
            }

            @Override
            public void onError(Exception error) {
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

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
