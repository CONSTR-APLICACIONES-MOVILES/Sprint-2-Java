package com.example.parchapp.domain.availability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.parchapp.domain.model.DaySchedule;
import com.example.parchapp.domain.model.TimeBlock;
import com.example.parchapp.domain.model.TimeSlot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommonSlotFinderTest {

    private static final int H = 60;

    private static DaySchedule known(String id, TimeBlock... busy) {
        return new DaySchedule(id, true, Arrays.asList(busy));
    }

    @Test
    public void findsHourSlotsWhereEveryoneIsFree() {
        List<DaySchedule> day = Arrays.asList(
                known("me", new TimeBlock(8 * H, 11 * H), new TimeBlock(12 * H, 18 * H)),
                known("sarah", new TimeBlock(9 * H, 10 * H)));

        List<TimeSlot> slots = CommonSlotFinder.find(day, H, 0);

        assertEquals(1, slots.size());
        assertEquals(11 * H, slots.get(0).getStartMinute());
        assertEquals(12 * H, slots.get(0).getEndMinute());
        assertEquals(2, slots.get(0).getFreeCount());
    }

    @Test
    public void slotsDoNotOverlapAndRespectTheDuration() {
        List<DaySchedule> day = Collections.singletonList(known("me"));

        List<TimeSlot> slots = CommonSlotFinder.find(day, 2 * H, 0);

        assertEquals(5, slots.size()); // 8-10, 10-12, 12-14, 14-16, 16-18
        for (int i = 1; i < slots.size(); i++) {
            assertTrue(slots.get(i).getStartMinute() >= slots.get(i - 1).getEndMinute());
        }
    }

    @Test
    public void unknownSchedulesAreNotTreatedAsFree() {
        List<DaySchedule> onlyUnknown = Collections.singletonList(
                new DaySchedule("marcus", false, Collections.emptyList()));
        assertTrue(CommonSlotFinder.find(onlyUnknown, H, 0).isEmpty());

        List<DaySchedule> mixed = Arrays.asList(known("me"),
                new DaySchedule("marcus", false, Collections.emptyList()));
        TimeSlot first = CommonSlotFinder.find(mixed, H, 0).get(0);
        assertEquals(1, first.getFreeCount());
        assertEquals(2, first.getGroupSize());
    }
//this option were by default really like the first one but i changed it to be more like the second one
    @Test
    public void threePeopleWithOverlappingBlocks() {
        // me 8-9:30 and 13:00-14:00, sarah 10:00-11:15, marcus 9:00-10:30 and 15:00-17:00.
        // Everyone is free 11:15-13:00 and 14:00-15:00 and 17:00-18:00; 30-min steps round 11:15 up.
        List<DaySchedule> day = Arrays.asList(
                known("me", new TimeBlock(8 * H, 9 * H + 30), new TimeBlock(13 * H, 14 * H)),
                known("sarah", new TimeBlock(10 * H, 11 * H + 15)),
                known("marcus", new TimeBlock(9 * H, 10 * H + 30), new TimeBlock(15 * H, 17 * H)));

        List<TimeSlot> slots = CommonSlotFinder.find(day, 45, 0);

        int[][] expected = {{11 * H + 30, 12 * H + 15}, {12 * H + 15, 13 * H}, {14 * H, 14 * H + 45},
                {17 * H, 17 * H + 45}};
        assertEquals(expected.length, slots.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i][0], slots.get(i).getStartMinute());
            assertEquals(expected[i][1], slots.get(i).getEndMinute());
            assertEquals(3, slots.get(i).getFreeCount());
        }
    }

    @Test
    public void skipsTimesBeforeTheEarliestStart() {
        List<DaySchedule> day = Collections.singletonList(known("me"));

        TimeSlot first = CommonSlotFinder.find(day, H, 10 * H + 15).get(0);

        assertEquals(10 * H + 30, first.getStartMinute());
    }
}
