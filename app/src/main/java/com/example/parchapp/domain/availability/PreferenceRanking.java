package com.example.parchapp.domain.availability;

import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Orders slots by how many minutes they overlap with the preferred hours of the members who are free in them. Ties go to the slot with more free members, then to the earliest one.
 */
public class PreferenceRanking implements SlotRanker {

    @Override
    public String getName() {
        return "preference";
    }

    @Override
    public List<TimeSlot> rank(List<TimeSlot> slots, List<Member> members) {
        Map<String, Member> membersById = new HashMap<>();
        for (Member member : members) {
            membersById.put(member.getId(), member);
        }
        Map<TimeSlot, Integer> scores = new HashMap<>();
        for (TimeSlot slot : slots) {
            scores.put(slot, preferenceScore(slot, membersById));
        }

        List<TimeSlot> ranked = new ArrayList<>(slots);
        ranked.sort(Comparator.<TimeSlot>comparingInt(scores::get).reversed()
                .thenComparing(Comparator.comparingInt(TimeSlot::getFreeCount).reversed())
                .thenComparingInt(TimeSlot::getStartMinute));
        return ranked;
    }

    private int preferenceScore(TimeSlot slot, Map<String, Member> membersById) {
        int score = 0;
        for (String memberId : slot.getFreeMemberIds()) {
            Member member = membersById.get(memberId);
            if (member == null) {
                continue;
            }
            int overlapStart = Math.max(slot.getStartMinute(), member.getPreferredStartMinute());
            int overlapEnd = Math.min(slot.getEndMinute(), member.getPreferredEndMinute());
            score += Math.max(0, overlapEnd - overlapStart);
        }
        return score;
    }
}
