package com.example.parchapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.parchapp.data.DemoData;
import com.example.parchapp.data.InsightsRepository;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.domain.availability.AvailabilityEngine;
import com.example.parchapp.domain.availability.OverlapRanking;
import com.example.parchapp.domain.availability.PreferenceRanking;
import com.example.parchapp.domain.availability.SlotRanker;
import com.example.parchapp.domain.availability.SoonestRanking;
import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeSlot;
import com.example.parchapp.util.Callback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompareAvailabilitySheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "arg_group_id";
    private static final String SOURCE = "compare_sheet";
    private static final int MAX_ALTERNATIVES = 2;

    private final AvailabilityEngine engine = new AvailabilityEngine();
    private String groupId;
    private Group group;
    private List<TimeSlot> rankedSlots = new ArrayList<>();
    private int selectedPosition;
    private int recommendedPosition = InsightsRepository.DEFAULT_RECOMMENDED_POSITION;

    private TextView selectedRankChip;
    private TextView statusText;
    private View bestSlotCard;
    private LinearLayout alternativeSlots;
    private Button scheduleButton;

    public static CompareAvailabilitySheet newInstance(String groupId) {
        CompareAvailabilitySheet sheet = new CompareAvailabilitySheet();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_compare_availability, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        groupId = args != null ? args.getString(ARG_GROUP_ID, DemoData.ROOMIES_ID) : DemoData.ROOMIES_ID;

        statusText = view.findViewById(R.id.slots_status_text);
        bestSlotCard = view.findViewById(R.id.best_slot_card);
        alternativeSlots = view.findViewById(R.id.alternative_slots);
        scheduleButton = view.findViewById(R.id.btn_schedule_in_slot);

        view.findViewById(R.id.sheet_close_btn).setOnClickListener(v -> dismiss());
        bestSlotCard.setOnClickListener(v -> selectSlot(0));
        scheduleButton.setOnClickListener(v -> scheduleSelectedSlot());

        selectedRankChip = view.findViewById(R.id.chip_rank_overlap);
        setupRankChip(view, R.id.chip_rank_overlap, new OverlapRanking());
        setupRankChip(view, R.id.chip_rank_preference, new PreferenceRanking());
        setupRankChip(view, R.id.chip_rank_soonest, new SoonestRanking());

        Repositories.insights().getRecommendedSlotPosition(new Callback<Integer>() {
            @Override
            public void onSuccess(Integer position) {
                recommendedPosition = position;
                if (getView() != null && !rankedSlots.isEmpty()) {
                    renderSlots();
                }
            }

            @Override
            public void onError(Exception error) {
            }
        });

        Repositories.groups().getGroup(groupId, new Callback<Group>() {
            @Override
            public void onSuccess(Group result) {
                if (getView() == null) {
                    return;
                }
                group = result;
                ((TextView) view.findViewById(R.id.compare_group_name)).setText(String.format(Locale.US,
                        "%s • %d member schedules", group.getName(), group.getSize()));
                calculateSlots();
            }

            @Override
            public void onError(Exception error) {
                if (getView() != null) {
                    statusText.setText(R.string.no_common_slots);
                    Toast.makeText(requireContext(), "Could not load the group", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupRankChip(View root, int chipId, SlotRanker ranker) {
        TextView chip = root.findViewById(chipId);
        chip.setOnClickListener(v -> {
            selectedRankChip.setBackgroundResource(R.drawable.bg_chip_unselected);
            selectedRankChip.setTextColor(requireContext().getColor(R.color.brand_dark));
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(requireContext().getColor(R.color.brand_primary));
            selectedRankChip = chip;
            engine.setRanker(ranker);
            calculateSlots();
        });
    }

    private void calculateSlots() {
        if (group == null) {
            return;
        }
        statusText.setText(R.string.calculating_slots);
        statusText.setVisibility(View.VISIBLE);
        scheduleButton.setEnabled(false);
        engine.findCommonSlotsAsync(group.getMembers(), SOURCE, new Callback<List<TimeSlot>>() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                if (getView() == null) {
                    return;
                }
                rankedSlots = slots;
                selectedPosition = 0;
                renderSlots();
            }

            @Override
            public void onError(Exception error) {
                if (getView() != null) {
                    statusText.setText(R.string.no_common_slots);
                }
            }
        });
    }

    private void renderSlots() {
        alternativeSlots.removeAllViews();
        if (rankedSlots.isEmpty()) {
            statusText.setText(R.string.no_common_slots);
            statusText.setVisibility(View.VISIBLE);
            bestSlotCard.setVisibility(View.GONE);
            scheduleButton.setEnabled(false);
            return;
        }
        statusText.setVisibility(View.GONE);
        bestSlotCard.setVisibility(View.VISIBLE);

        TimeSlot best = rankedSlots.get(0);
        View root = requireView();
        String label = getString(R.string.best_mutual_match);
        if (recommendedPosition == 0) {
            label += "  •  ★ " + getString(R.string.recommended);
        }
        ((TextView) root.findViewById(R.id.best_slot_label)).setText(label);
        ((TextView) root.findViewById(R.id.best_slot_badge)).setText(String.format(Locale.US,
                "%d/%d Free", best.getFreeCount(), best.getGroupSize()));
        ((TextView) root.findViewById(R.id.best_slot_time)).setText(best.formatRange());
        ((TextView) root.findViewById(R.id.best_slot_note)).setText(best.isEveryoneFree()
                ? "Everyone in the group is free."
                : "Free: " + freeMemberNames(best));
        bestSlotCard.setAlpha(selectedPosition == 0 ? 1f : 0.55f);

        int alternatives = Math.min(MAX_ALTERNATIVES, rankedSlots.size() - 1);
        for (int position = 1; position <= alternatives; position++) {
            alternativeSlots.addView(alternativeRow(position));
        }

        scheduleButton.setText("⚡  Schedule Plan • " + rankedSlots.get(selectedPosition).formatRange());
        scheduleButton.setEnabled(true);
    }

    private TextView alternativeRow(int position) {
        TimeSlot slot = rankedSlots.get(position);
        boolean selected = position == selectedPosition;
        TextView row = new TextView(requireContext());
        String text = String.format(Locale.US, "%s   •   %d/%d free", slot.formatRange(),
                slot.getFreeCount(), slot.getGroupSize());
        if (position == recommendedPosition) {
            text += "   •   ★ " + getString(R.string.recommended);
        }
        row.setText(text);
        row.setTextSize(12);
        row.setTextColor(requireContext().getColor(selected ? R.color.brand_primary : R.color.brand_dark));
        row.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        int padding = dp(12);
        row.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        row.setLayoutParams(params);
        row.setOnClickListener(v -> selectSlot(position));
        return row;
    }

    private void selectSlot(int position) {
        if (position >= rankedSlots.size()) {
            return;
        }
        selectedPosition = position;
        renderSlots();
    }

    private void scheduleSelectedSlot() {
        if (rankedSlots.isEmpty() || group == null) {
            return;
        }
        TimeSlot slot = rankedSlots.get(selectedPosition);
        CreateActivitySheet.newInstance(group.getId(), group.getSize(), slot.formatRange(),
                selectedPosition, recommendedPosition).show(getParentFragmentManager(), "create_activity");
        dismiss();
    }

    private String freeMemberNames(TimeSlot slot) {
        StringBuilder names = new StringBuilder();
        for (Member member : group.getMembers()) {
            if (slot.getFreeMemberIds().contains(member.getId())) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(member.getName());
            }
        }
        return names.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
