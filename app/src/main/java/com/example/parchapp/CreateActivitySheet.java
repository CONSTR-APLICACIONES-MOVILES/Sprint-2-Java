package com.example.parchapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CreateActivitySheet extends BottomSheetDialogFragment {

    private TextView selectedGroupChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_create_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText titleInput = view.findViewById(R.id.input_activity_title);

        TextView[] groupChips = {
                view.findViewById(R.id.chip_group_eng),
                view.findViewById(R.id.chip_group_roomies)
        };
        selectedGroupChip = groupChips[0];
        for (TextView chip : groupChips) {
            chip.setOnClickListener(v -> {
                selectedGroupChip.setBackgroundResource(R.drawable.bg_chip_unselected);
                selectedGroupChip.setTextColor(requireContext().getColor(R.color.brand_dark));
                selectedGroupChip = chip;
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(requireContext().getColor(R.color.brand_primary));
            });
        }

        view.findViewById(R.id.sheet_close_btn).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_create_activity).setOnClickListener(v -> {
            if (titleInput.getText().toString().trim().isEmpty()) {
                titleInput.setError("Enter an activity title");
                return;
            }
            dismiss();
            startActivity(new Intent(requireContext(), ActivityCreatedActivity.class));
        });
    }
}
