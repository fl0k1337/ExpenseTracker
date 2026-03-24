package com.example.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AddExpenseSheet extends BottomSheetDialogFragment {

    public interface OnExpenseAddedListener {
        void onAdded(String title, double amount, String category, String description);
    }

    private OnExpenseAddedListener listener;
    private Expense existingExpense;

    // Пустой конструктор (нужен для Android)
    public AddExpenseSheet() {}

    // Сеттеры вместо конструкторов с параметрами
    public void setListener(OnExpenseAddedListener listener) { this.listener = listener; }
    public void setExistingExpense(Expense expense) { this.existingExpense = expense; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sheet_add_expense, container, false);

        TextInputEditText etTitle = v.findViewById(R.id.etTitle);
        TextInputEditText etAmount = v.findViewById(R.id.etAmount);
        TextInputEditText etDescription = v.findViewById(R.id.etDescription);
        ChipGroup chipGroup = v.findViewById(R.id.categoryChips);
        MaterialButton btnSave = v.findViewById(R.id.btnSave);

        if (existingExpense != null) {
            etTitle.setText(existingExpense.title);
            etAmount.setText(String.valueOf(existingExpense.amount));
            etDescription.setText(existingExpense.description);
            btnSave.setText("Обновить");
        }

        btnSave.setOnClickListener(view -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Заполни поля!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                int chipId = chipGroup.getCheckedChipId();
                String category = (chipId != -1) ? ((Chip) v.findViewById(chipId)).getText().toString() : "Прочее";

                if (listener != null) listener.onAdded(title, amount, category, desc);
                dismiss();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка ввода!", Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }
}