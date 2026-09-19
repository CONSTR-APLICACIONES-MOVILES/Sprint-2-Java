package com.example.parchapp;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class GroupDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        findViewById(R.id.share_button).setOnClickListener(v -> toast("Share group link"));
        findViewById(R.id.more_options_button).setOnClickListener(v -> toast("More options"));

        findViewById(R.id.compare_roommates_button).setOnClickListener(v ->
                new CompareAvailabilitySheet().show(getSupportFragmentManager(), "compare_roommates"));
        findViewById(R.id.plan_details_button).setOnClickListener(v -> toast("Opening plan details"));
        findViewById(R.id.add_calendar_button).setOnClickListener(v -> toast("Plan added to calendar"));

        findViewById(R.id.refresh_roommates_button).setOnClickListener(v -> toast("Refreshing roommate statuses"));
        findViewById(R.id.chat_alex_button).setOnClickListener(v -> toast("Opening chat with Alex"));
        findViewById(R.id.chat_mateo_button).setOnClickListener(v -> toast("Opening chat with Mateo"));
        findViewById(R.id.chat_camila_button).setOnClickListener(v -> toast("Opening chat with Camila"));
        findViewById(R.id.chat_lucas_button).setOnClickListener(v -> toast("Opening chat with Lucas"));

        findViewById(R.id.propose_plan_button).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGroupActivity.class)));
        findViewById(R.id.sync_schedules_button).setOnClickListener(v -> toast("Schedules synchronized"));
        findViewById(R.id.house_chat_button).setOnClickListener(v -> toast("Opening house chat"));
        findViewById(R.id.view_all_activities).setOnClickListener(v -> toast("Viewing all house activities"));

        findViewById(R.id.create_plan_button).setOnClickListener(v ->
                new CreateActivitySheet().show(getSupportFragmentManager(), "create_activity"));

        findViewById(R.id.nav_home_group).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.nav_schedule_group).setOnClickListener(v -> toast("Schedule coming soon"));
        findViewById(R.id.nav_history_group).setOnClickListener(v -> toast("History coming soon"));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
