package com.example.parchapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parchapp.analytics.AnalyticsEvents;
import com.example.parchapp.analytics.AnalyticsTracker;
import com.example.parchapp.data.GroupListener;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.domain.availability.AvailabilityEngine;
import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeSlot;
import com.example.parchapp.util.Callback;
import com.example.parchapp.util.ExternalActions;

import java.util.List;
import java.util.Locale;

public class GroupDetailActivity extends AppCompatActivity implements GroupListener {

    public static final String EXTRA_GROUP_ID = "extra_group_id";
    private static final String SCREEN = "group_detail";

    private final AvailabilityEngine engine = new AvailabilityEngine();
    private final AnalyticsTracker tracker = AnalyticsTracker.getInstance();
    private String groupId;
    private Group loadedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        findViewById(R.id.share_button).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_INVITATIONS, SCREEN);
            toast("Share group link");
        });
        findViewById(R.id.more_options_button).setOnClickListener(v -> toast("More options"));

        findViewById(R.id.compare_roommates_button).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_COMPARE_AVAILABILITY, SCREEN);
            CompareAvailabilitySheet.newInstance(groupId).show(getSupportFragmentManager(), "compare_roommates");
        });
        findViewById(R.id.plan_details_button).setOnClickListener(v -> {
            if (loadedGroup == null || loadedGroup.getPlanTitle().isEmpty()) {
                toast("No activity data is available");
                return;
            }
            PlanDetailsSheet.newInstance(loadedGroup.getPlanTitle(), "", "", "", "")
                    .show(getSupportFragmentManager(), "plan_details");
        });
        findViewById(R.id.add_calendar_button).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_SCHEDULE, SCREEN);
            if (loadedGroup == null || loadedGroup.getPlanTitle().isEmpty()
                    || !ExternalActions.openCalendar(this, loadedGroup.getPlanTitle(), "", "", "")) {
                toast("No calendar application is available");
            }
        });

        findViewById(R.id.refresh_roommates_button).setOnClickListener(v -> refreshGroup());
        findViewById(R.id.chat_alex_button).setOnClickListener(v -> toast("Chat is not available for this group"));
        findViewById(R.id.chat_mateo_button).setOnClickListener(v -> toast("Chat is not available for this group"));
        findViewById(R.id.chat_camila_button).setOnClickListener(v -> toast("Chat is not available for this group"));
        findViewById(R.id.chat_lucas_button).setOnClickListener(v -> toast("Chat is not available for this group"));

        findViewById(R.id.propose_plan_button).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGroupActivity.class).putExtra(EXTRA_GROUP_ID, groupId)));
        findViewById(R.id.sync_schedules_button).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_SCHEDULE, SCREEN);
            refreshGroup();
        });
        findViewById(R.id.house_chat_button).setOnClickListener(v -> toast("Opening house chat"));
        findViewById(R.id.view_all_activities).setOnClickListener(v -> toast("Viewing all house activities"));

        findViewById(R.id.create_plan_button).setOnClickListener(v ->
                new CreateActivitySheet().show(getSupportFragmentManager(), "create_activity"));

        findViewById(R.id.nav_home_group).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.nav_schedule_group).setOnClickListener(v -> {
            tracker.trackFeature(AnalyticsEvents.FEATURE_SCHEDULE, SCREEN);
            startActivity(new Intent(this, ScheduleActivity.class));
        });
        findViewById(R.id.nav_history_group).setOnClickListener(v -> toast("History coming soon"));
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
                        Repositories.groups().observeGroup(groupId, GroupDetailActivity.this);
                    } else {
                        toast("No groups are available");
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

    @Override
    public void onGroupChanged(Group group) {
        loadedGroup = group;
        ((TextView) findViewById(R.id.group_name_text)).setText(group.getName());
        ((TextView) findViewById(R.id.group_description_text)).setText(group.getSize() + " members");
        ((TextView) findViewById(R.id.top_group_name_text)).setText(group.getName());
        ((TextView) findViewById(R.id.top_group_subtitle_text)).setText(group.getSize() + " members");
        ((TextView) findViewById(R.id.next_plan_header)).setText(
                group.getPlanTitle().isEmpty() ? "No active group plan" : "Next Group Plan");
        ((TextView) findViewById(R.id.next_plan_status)).setText(
                group.getPlanTitle().isEmpty() ? "No data" : group.getGoingCount() + "/" + group.getRsvpTotal() + " responses");
        ((TextView) findViewById(R.id.next_plan_time)).setText("Schedule not specified");
        ((TextView) findViewById(R.id.next_plan_location)).setText("Location not specified");
        ((TextView) findViewById(R.id.member_summary_text)).setText(group.getSize() + " members");
        bindMembers(group);
        ((TextView) findViewById(R.id.plan_title_text)).setText(group.getPlanTitle());

        int going = group.getGoingCount();
        int total = Math.max(1, group.getRsvpTotal());
        ((TextView) findViewById(R.id.rsvp_ready_text)).setText(String.format(Locale.US,
                "%d%% Ready (%d of %d)", going * 100 / total, going, group.getRsvpTotal()));
        View fill = findViewById(R.id.rsvp_progress_fill);
        View container = (View) fill.getParent();
        container.post(() -> {
            ViewGroup.LayoutParams params = fill.getLayoutParams();
            int available = container.getWidth() - container.getPaddingStart() - container.getPaddingEnd();
            params.width = available * going / total;
            fill.setLayoutParams(params);
        });

        engine.findCommonSlotsAsync(group.getMembers(), SCREEN, new Callback<List<TimeSlot>>() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                renderCommonFree(slots, group.getSize());
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }

    private void bindMembers(Group group) {
        int[] cards = {R.id.member_card_0, R.id.member_card_1, R.id.member_card_2, R.id.member_card_3};
        int[] names = {R.id.member_name_0, R.id.member_name_1, R.id.member_name_2, R.id.member_name_3};
        int[] initials = {R.id.member_initials_0, R.id.member_initials_1, R.id.member_initials_2, R.id.member_initials_3};
        int[] summaries = {R.id.member_summary_0, R.id.member_summary_1, R.id.member_summary_2, R.id.member_summary_3};
        int[] statuses = {R.id.member_status_0, R.id.member_status_1, R.id.member_status_2, R.id.member_status_3};
        List<Member> members = group.getMembers();
        for (int i = 0; i < cards.length; i++) {
            View card = findViewById(cards[i]);
            if (i >= members.size()) {
                card.setVisibility(View.GONE);
                continue;
            }
            Member member = members.get(i);
            card.setVisibility(View.VISIBLE);
            ((TextView) findViewById(names[i])).setText(member.getName());
            ((TextView) findViewById(initials[i])).setText(member.getInitials());
            ((TextView) findViewById(summaries[i])).setText("Group member");
            ((TextView) findViewById(statuses[i])).setText(
                    member.getBusyBlocks().isEmpty() ? "Availability: free" : "Availability: has busy blocks");
        }
    }

    private void renderCommonFree(List<TimeSlot> slots, int groupSize) {
        TextView commonFree = findViewById(R.id.common_free_text);
        TextView badge = findViewById(R.id.common_free_badge);
        TextView synced = findViewById(R.id.group_synced_badge);
        synced.setText(String.format(Locale.US, "● %d/%d Synced", groupSize, groupSize));
        if (slots.isEmpty()) {
            commonFree.setText("No common free time today");
            badge.setVisibility(View.GONE);
            return;
        }
        TimeSlot best = slots.get(0);
        commonFree.setText("Common Free: " + best.formatRange());
        badge.setVisibility(View.VISIBLE);
        badge.setText(best.isEveryoneFree()
                ? "All " + groupSize + " free"
                : best.getFreeCount() + " of " + groupSize + " free");
    }

    private void refreshGroup() {
        Repositories.groups().getGroup(groupId, new Callback<Group>() {
            @Override
            public void onSuccess(Group group) {
                onGroupChanged(group);
                toast("Roommate statuses refreshed");
            }

            @Override
            public void onError(Exception error) {
                toast("Could not refresh. Showing saved data.");
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
