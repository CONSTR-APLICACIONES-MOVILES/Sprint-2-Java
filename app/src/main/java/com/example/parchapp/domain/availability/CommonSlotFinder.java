package com.example.parchapp.domain.availability;

import com.example.parchapp.domain.model.DaySchedule;
import com.example.parchapp.domain.model.TimeBlock;
import com.example.parchapp.domain.model.TimeSlot;

import java.util.ArrayList;
import java.util.List;

/* Finds non-overlapping slots of a fixed duration where every compared person with a known schedule is free. Used by the Schedule screen; the group engine (AvailabilityEngine) instead ranks windows where only some members are free. */

/* i know we have to change the var of day start and end */
public final class CommonSlotFinder {
    public static final int DAY_START_MINUTE = 8 * 60;
    public static final int DAY_END_MINUTE = 18 * 60;
    static final int STEP_MINUTES = 30;

    private CommonSlotFinder() {
    }

    /*@param earliestStart slots starting before this minute are skipped, for example the current time when looking at today*/
    public static List<TimeSlot> find(List<DaySchedule> schedules, int durationMinutes, int earliestStart) {
        List<TimeSlot> slots = new ArrayList<>();
        List<String> knownIds = new ArrayList<>();
        List<TimeBlock> busy = new ArrayList<>();
        for (DaySchedule schedule : schedules) {
            if (schedule.isKnown()) {
                knownIds.add(schedule.getOwnerId());
                busy.addAll(schedule.getBusy());
            }
        }
        if (knownIds.isEmpty() || durationMinutes <= 0) {
            return slots;
        }

        int start = roundUpToStep(Math.max(DAY_START_MINUTE, earliestStart));
        while (start + durationMinutes <= DAY_END_MINUTE) {
            int end = start + durationMinutes;
            if (isFree(busy, start, end)) {
                slots.add(new TimeSlot(start, end, knownIds, schedules.size()));
                start = end; // back-to-back slots are fine, even off the half hour grid
            } else {
                // Back on the grid; adding STEP_MINUTES to an off-grid start would skip valid starts.
                start = roundUpToStep(start + 1);
            }
        }
        return slots;
    }

    private static boolean isFree(List<TimeBlock> busy, int start, int end) {
        for (TimeBlock block : busy) {
            if (block.overlaps(start, end)) {
                return false;
            }
        }
        return true;
    }

    private static int roundUpToStep(int minute) {
        return ((minute + STEP_MINUTES - 1) / STEP_MINUTES) * STEP_MINUTES;
    }
}
