package com.example.parchapp.data;

import com.example.parchapp.UserSession;
import com.example.parchapp.domain.model.DaySchedule;
import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.ScheduleFriend;
import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* Demo mode important to show friends and schedules, with the same weekday shifts the backend seed writes, so both modes show the same week. */
class InMemoryScheduleRepository implements ScheduleRepository {

    private final Map<String, Member> members = new LinkedHashMap<>();

    InMemoryScheduleRepository() {
        for (Group group : DemoData.groups().values()) {
            for (Member member : group.getMembers()) {
                members.put(member.getId(), member);
            }
        }
    }

    @Override
    public void getFriends(Callback<List<ScheduleFriend>> callback) {
        String me = UserSession.getCurrentUser().getId();
        List<ScheduleFriend> friends = new ArrayList<>();
        for (Member member : members.values()) {
            if (!member.getId().equals(me)) {
                friends.add(new ScheduleFriend(member.getId(), member.getName(), member.getInitials()));
            }
        }
        MainThread.post(() -> callback.onSuccess(friends));
    }

    @Override
    public void getDay(String date, List<String> friendIds, Callback<List<DaySchedule>> callback) {
        int dayOfWeek;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date));
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        } catch (ParseException e) {
            MainThread.post(() -> callback.onError(e));
            return;
        }
        List<String> owners = new ArrayList<>();
        owners.add(UserSession.getCurrentUser().getId());
        owners.addAll(friendIds);

        List<DaySchedule> schedules = new ArrayList<>();
        for (String ownerId : owners) {
            Member member = members.get(ownerId);
            if (member == null && ownerId.equals(owners.get(0))) {
                // A demo-mode account is not a demo member; give it Alex's week to compare against.
                member = members.get(DemoData.DEMO_USER_ID);
            }
            schedules.add(member == null
                    ? new DaySchedule(ownerId, false, new ArrayList<>())
                    : new DaySchedule(ownerId, true, DemoData.busyOn(member, dayOfWeek)));
        }
        MainThread.post(() -> callback.onSuccess(schedules));
    }
}
