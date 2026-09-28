package com.example.parchapp.domain.availability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.parchapp.data.DemoData;
import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeBlock;
import com.example.parchapp.domain.model.TimeSlot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AvailabilityEngineTest {

    private static final int H = 60;

    private static Member member(String id, int preferredStart, int preferredEnd, TimeBlock... busy) {
        return new Member(id, id, id.substring(0, 1), Arrays.asList(busy), preferredStart, preferredEnd);
    }

    @Test
    public void roomiesBestSlotIsTheEveningWhenEveryoneIsFree() {
        List<Member> roomies = DemoData.groups().get(DemoData.ROOMIES_ID).getMembers();

        List<TimeSlot> slots = new AvailabilityEngine().findCommonSlots(roomies);

        TimeSlot best = slots.get(0);
        assertEquals(19 * H + 30, best.getStartMinute());
        assertEquals(AvailabilityEngine.DAY_END_MINUTE, best.getEndMinute());
        assertTrue(best.isEveryoneFree());
    }

    @Test
    public void slotsShorterThanOneHourAreDropped() {
        List<Member> members = Arrays.asList(
                member("a", 0, 0, new TimeBlock(8 * H, 12 * H), new TimeBlock(12 * H + 30, 23 * H)),
                member("b", 0, 0));

        List<TimeSlot> slots = new AvailabilityEngine().findCommonSlots(members);

        for (TimeSlot slot : slots) {
            assertTrue(slot.getDurationMinutes() >= AvailabilityEngine.MIN_SLOT_MINUTES);
            assertTrue(!(slot.getStartMinute() == 12 * H && slot.getFreeCount() == 2));
        }
    }

    @Test
    public void emptyGroupHasNoSlots() {
        assertTrue(new AvailabilityEngine().findCommonSlots(Collections.emptyList()).isEmpty());
    }

    @Test
    public void rankingsOrderTheSameSlotsDifferently() {
        // Everyone is free 9-11 and 20-23; the later slot is longer and matches preferred hours.
        List<Member> members = Arrays.asList(
                member("a", 20 * H, 23 * H, new TimeBlock(8 * H, 9 * H), new TimeBlock(11 * H, 20 * H)),
                member("b", 20 * H, 22 * H, new TimeBlock(8 * H, 9 * H), new TimeBlock(11 * H, 20 * H)));
        AvailabilityEngine engine = new AvailabilityEngine();

        engine.setRanker(new OverlapRanking());
        assertEquals(20 * H, engine.findCommonSlots(members).get(0).getStartMinute());

        engine.setRanker(new SoonestRanking());
        assertEquals(9 * H, engine.findCommonSlots(members).get(0).getStartMinute());

        engine.setRanker(new PreferenceRanking());
        assertEquals(20 * H, engine.findCommonSlots(members).get(0).getStartMinute());
    }

    @Test
    public void moreFreeMembersBeatEarlierStartForSoonestRanking() {
        List<Member> members = Arrays.asList(
                member("a", 0, 0),
                member("b", 0, 0),
                member("c", 0, 0, new TimeBlock(8 * H, 18 * H)));
        AvailabilityEngine engine = new AvailabilityEngine();
        engine.setRanker(new SoonestRanking());

        TimeSlot best = engine.findCommonSlots(members).get(0);

        assertEquals(18 * H, best.getStartMinute());
        assertEquals(3, best.getFreeCount());
    }

    @Test
    public void formatsMinutesAsTwelveHourClock() {
        assertEquals("7:30 PM", TimeSlot.formatMinute(19 * H + 30));
        assertEquals("12:00 PM", TimeSlot.formatMinute(12 * H));
        assertEquals("8:05 AM", TimeSlot.formatMinute(8 * H + 5));
    }
}
