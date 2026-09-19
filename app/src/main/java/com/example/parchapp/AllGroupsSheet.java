package com.example.parchapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AllGroupsSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onGroupRowSelected(String groupKey);
    }

    private Listener listener;

    public static AllGroupsSheet newInstance(Listener listener) {
        AllGroupsSheet sheet = new AllGroupsSheet();
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_all_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.sheet_close_btn).setOnClickListener(v -> dismiss());
        setRowListener(view, R.id.row_group_eng, "eng");
        setRowListener(view, R.id.row_group_roomies, "roomies");
        setRowListener(view, R.id.row_group_intramurals, "intramurals");
        setRowListener(view, R.id.row_group_thesis, "thesis");
        setRowListener(view, R.id.row_group_choir, "choir");
    }

    private void setRowListener(View root, int viewId, String groupKey) {
        root.findViewById(viewId).setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onGroupRowSelected(groupKey);
            }
        });
    }
}
