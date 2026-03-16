package com.example.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AddExpenseSheet extends BottomSheetDialogFragment {
    public interface OnExpenseAddedListener {
        void onAdded(String title, double amount, String category, String description);
    }

    private OnExpenseAddedListener listener;

    public AddExpenseSheet(OnExpenseAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sheet_add_expense, container, false);

        // ОБЪЯВЛЯЕМ ВСЕ ПОЛЯ
        TextInputEditText etTitle = v.findViewById(R.id.etTitle);
        TextInputEditText etAmount = v.findViewById(R.id.etAmount);
        TextInputEditText etDescription = v.findViewById(R.id.etDescription); // ДОБАВИЛИ ЭТУ СТРОКУ
        ChipGroup chipGroup = v.findViewById(R.id.categoryChips);

        v.findViewById(R.id.btnSave).setOnClickListener(view -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String desc = etDescription.getText().toString().trim(); // И ЭТУ

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Заполни название и сумму!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                int chipId = chipGroup.getCheckedChipId();
                String category = (chipId != -1) ? ((Chip) v.findViewById(chipId)).getText().toString() : "Прочее";

                // Передаем 4 параметра, как просит новый интерфейс
                listener.onAdded(title, amount, category, desc);
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Введи корректную сумму!", Toast.LENGTH_SHORT).show();
            }
        });
        etTitle.postDelayed(() -> {
            etTitle.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etTitle, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 200);
        return v;
    }
}