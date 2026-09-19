package com.example.parchapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView displayFreeUntil;
    private TextView displayNextActivity;
    private TextView soccerVoteBadge;
    private android.view.View soccerProgressBar;
    private Button goingButton;
    private Button maybeButton;
    private boolean isGoing = false;
    private boolean isMaybe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindUser(UserSession.getCurrentUser());

        displayFreeUntil = findViewById(R.id.display_free_until);
        displayNextActivity = findViewById(R.id.display_next_activity);
        soccerVoteBadge = findViewById(R.id.soccer_vote_badge);
        soccerProgressBar = findViewById(R.id.soccer_progress_bar);
        goingButton = findViewById(R.id.going_button);
        maybeButton = findViewById(R.id.maybe_button);

        findViewById(R.id.header_search_btn).setOnClickListener(v -> toast("Search coming soon"));
        findViewById(R.id.header_notif_btn).setOnClickListener(v -> toast("No new notifications"));
        findViewById(R.id.header_user_initials).setOnClickListener(v ->
                new AvailabilityStatusSheet().show(getSupportFragmentManager(), "availability_status"));

        findViewById(R.id.copy_link_button).setOnClickListener(v -> copyInviteLink());
        findViewById(R.id.btn_edit_free_until).setOnClickListener(v -> openQuickStatusSheet());
        findViewById(R.id.btn_edit_next_activity).setOnClickListener(v -> openQuickStatusSheet());
        findViewById(R.id.compare_button).setOnClickListener(v ->
                new CompareAvailabilitySheet().show(getSupportFragmentManager(), "compare_availability"));
        findViewById(R.id.calendar_button).setOnClickListener(v ->
                new CalendarSheet().show(getSupportFragmentManager(), "calendar"));
        findViewById(R.id.new_activity_button).setOnClickListener(v ->
                new CreateActivitySheet().show(getSupportFragmentManager(), "create_activity"));

        findViewById(R.id.plan_card_study).setOnClickListener(v -> openPlanDetails());
        findViewById(R.id.plan_details_button).setOnClickListener(v -> openPlanDetails());

        goingButton.setOnClickListener(v -> setRsvp(true));
        maybeButton.setOnClickListener(v -> setRsvp(false));

        findViewById(R.id.group_card_eng).setOnClickListener(v -> openEngineeringGroupSheet());
        findViewById(R.id.roomies_group_card).setOnClickListener(v ->
                startActivity(new Intent(this, GroupDetailActivity.class)));
        findViewById(R.id.view_all_groups_button).setOnClickListener(v -> openAllGroupsSheet());

        findViewById(R.id.nav_groups).setOnClickListener(v ->
                startActivity(new Intent(this, GroupDetailActivity.class)));
        findViewById(R.id.nav_schedule).setOnClickListener(v -> toast("Schedule coming soon"));
    }

    private void openQuickStatusSheet() {
        QuickStatusSheet.newInstance((freeUntil, nextActivity) -> {
            if (!freeUntil.isEmpty()) {
                displayFreeUntil.setText(freeUntil);
            }
            if (!nextActivity.isEmpty()) {
                displayNextActivity.setText(nextActivity);
            }
            toast("Status updated");
        }).show(getSupportFragmentManager(), "quick_status");
    }

    private void openPlanDetails() {
        new PlanDetailsSheet().show(getSupportFragmentManager(), "plan_details");
    }

    private void openEngineeringGroupSheet() {
        GroupDetailsSheet.newInstance(
                "🎓",
                "Engineering 2026",
                "28 members • Real-time synced",
                "Linear Algebra midterm study planned for Central Library Room 302 at 4:00 PM."
        ).show(getSupportFragmentManager(), "group_details_eng");
    }

    private void openAllGroupsSheet() {
        AllGroupsSheet.newInstance(groupKey -> {
            if ("roomies".equals(groupKey)) {
                startActivity(new Intent(this, GroupDetailActivity.class));
            } else if ("eng".equals(groupKey)) {
                openEngineeringGroupSheet();
            } else {
                GroupDetailsSheet.newInstance("📌", groupTitleFor(groupKey), groupSubtitleFor(groupKey),
                        "No recent shared status yet.").show(getSupportFragmentManager(), "group_details_generic");
            }
        }).show(getSupportFragmentManager(), "all_groups");
    }

    private String groupTitleFor(String groupKey) {
        switch (groupKey) {
            case "intramurals": return "Campus Intramurals";
            case "thesis": return "Thesis Support Group";
            case "choir": return "University Choir";
            default: return "Group";
        }
    }

    private String groupSubtitleFor(String groupKey) {
        switch (groupKey) {
            case "intramurals": return "6 members • 1 plan";
            case "thesis": return "5 members • no active plan";
            case "choir": return "12 members • 1 plan";
            default: return "";
        }
    }

    private void setRsvp(boolean going) {
        isGoing = going;
        isMaybe = !going;
        goingButton.setBackgroundResource(isGoing ? R.drawable.bg_progress_fill : R.drawable.bg_green_chip);
        goingButton.setBackgroundTintList(isGoing
                ? getColorStateList(R.color.brand_success)
                : null);
        goingButton.setTextColor(getColor(isGoing ? android.R.color.white : R.color.brand_success_dark));

        maybeButton.setBackgroundResource(isMaybe ? R.drawable.bg_progress_fill : R.drawable.bg_light_blue);
        maybeButton.setBackgroundTintList(isMaybe
                ? getColorStateList(R.color.brand_primary)
                : null);
        maybeButton.setTextColor(getColor(isMaybe ? android.R.color.white : R.color.brand_slate));

        soccerVoteBadge.setText(going ? "✓  You're going • 4/6" : "◷  Maybe • 4/6");
        toast(going ? "RSVP confirmed: Going" : "RSVP changed to Maybe");
    }

    private void copyInviteLink() {
        String link = ((TextView) findViewById(R.id.invite_link_text)).getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Invite link", link));
        toast(getString(R.string.link_copied));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindUser(UserProfile user) {
        ((TextView) findViewById(R.id.header_user_initials)).setText(user.getInitials());
        ((TextView) findViewById(R.id.greeting_text)).setText("Hello, " + user.getFirstName() + "! 👋");
        ((TextView) findViewById(R.id.active_plans_text)).setText("You have " + user.getActivePlans() + " active plans today");
        ((TextView) findViewById(R.id.invite_link_text)).setText(user.getInviteLink());

        // TODO: ajustar segun usuario: estado, siguiente actividad y planes deben venir del backend.
        // TODO: ajustar segun usuario: grupos activos deben cargarse desde la cuenta autenticada.
    }
}
