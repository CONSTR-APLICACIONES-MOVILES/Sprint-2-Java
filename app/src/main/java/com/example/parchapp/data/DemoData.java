package com.example.parchapp.data;

import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*Seed groups shown in the prototype. Used as it is by the in-memory repository, and written to
    Firestore the first time a group is requested and does not exist there yet.
 */
public final class DemoData {
    public static final String ROOMIES_ID = "roomies";
    public static final String ENGINEERING_ID = "eng";
    public static final String INTRAMURALS_ID = "intramurals";
    public static final String THESIS_ID = "thesis";
    public static final String CHOIR_ID = "choir";

    public static final String DEMO_USER_ID = "alex";

    private DemoData() {
    }

    public static Map<String, Group> groups() {
        Map<String, Group> groups = new LinkedHashMap<>();
        groups.put(ROOMIES_ID, roomies());
        groups.put(ENGINEERING_ID, generated(ENGINEERING_ID, "Engineering 2026", "🎓",
                "Linear Algebra midterm study", 8, 3));
        groups.put(INTRAMURALS_ID, generated(INTRAMURALS_ID, "Campus Intramurals", "⚽",
                "5-a-side Soccer & Social", 6, 3));
        groups.put(THESIS_ID, generated(THESIS_ID, "Thesis Support Group", "📌", "", 5, 0));
        groups.put(CHOIR_ID, generated(CHOIR_ID, "University Choir", "🎵", "Rehearsal", 12, 7));
        return groups;
    }

    private static Group roomies() {
        List<Member> members = Arrays.asList(
                member(DEMO_USER_ID, "Alex", 18, 21, 8, 12, 14, 16),
                member("mateo", "Mateo", 19, 22, 9, 13, 18, 19.5),
                member("camila", "Camila", 17, 20, 10, 17),
                member("lucas", "Lucas", 20, 23, 8, 10, 13, 18));
        List<String> going = Arrays.asList(DEMO_USER_ID, "mateo", "camila", "lucas");
        return new Group(ROOMIES_ID, "Roomies Main St", "⌂", "Weekly House Dinner & Grocery Run",
                members, going, Collections.emptyList(), Collections.emptyList());
    }

    /** Larger groups with deterministic schedules, so BQ1 gets several group sizes. */
    private static Group generated(String id, String name, String icon, String planTitle, int size, int going) {
        List<Member> members = new ArrayList<>();
        List<String> goingIds = new ArrayList<>();
        members.add(member(DEMO_USER_ID, "Alex", 18, 21, 8, 12, 14, 16));
        for (int i = 1; i < size; i++) {
            double classStart = 8 + (i % 4);
            double classEnd = classStart + 2 + (i % 3);
            double afternoonStart = 14 + (i % 3);
            String memberId = id + "_m" + i;
            members.add(member(memberId, "Member " + i, 17 + (i % 3), 21 + (i % 2),
                    classStart, classEnd, afternoonStart, afternoonStart + 1.5));
            if (goingIds.size() < going) {
                goingIds.add(memberId);
            }
        }
        return new Group(id, name, icon, planTitle, members, goingIds,
                Collections.emptyList(), Collections.emptyList());
    }

    /** Busy hours are given as start/end pairs in hours, for example {@code 18, 19.5}. */
    private static Member member(String id, String name, int preferredStartHour, int preferredEndHour,
                                 double... busyHours) {
        List<TimeBlock> busy = new ArrayList<>();
        for (int i = 0; i + 1 < busyHours.length; i += 2) {
            busy.add(new TimeBlock((int) (busyHours[i] * 60), (int) (busyHours[i + 1] * 60)));
        }
        return new Member(id, name, name.substring(0, 1), busy, preferredStartHour * 60, preferredEndHour * 60);
    }
}
