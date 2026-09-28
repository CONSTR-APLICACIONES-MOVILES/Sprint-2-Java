package com.example.parchapp.domain.availability;

import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Orders slots by the number of available members and breaks ties by the earliest start. */
public class SoonestRanking implements SlotRanker {

    @Override
    public String getName() {
        return "soonest";
    }

    @Override
    public List<TimeSlot> rank(List<TimeSlot> slots, List<Member> members) {
        List<TimeSlot> ranked = new ArrayList<>(slots);
        ranked.sort(Comparator.comparingInt(TimeSlot::getFreeCount).reversed()
                .thenComparingInt(TimeSlot::getStartMinute));
        return ranked;
    }
}
