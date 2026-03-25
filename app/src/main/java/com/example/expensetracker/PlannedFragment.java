package com.example.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PlannedFragment extends Fragment {
    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;
    private TextView tvPlannedTotal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_planned, container, false);
        tvPlannedTotal = v.findViewById(R.id.tvPlannedTotal);
        RecyclerView rv = v.findViewById(R.id.rvPlanned);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        // Показываем ТОЛЬКО подписки
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            List<Expense> recurringOnly = new ArrayList<>();
            double total = 0;
            for (Expense e : expenses) {
                if (e.isRecurring) {
                    recurringOnly.add(e);
                    total += e.amount;
                }
            }
            adapter.setExpenses(recurringOnly);
            tvPlannedTotal.setText(String.format("%.2f ₽", total));
        });

        v.findViewById(R.id.fabAddRecurring).setOnClickListener(view -> {
            AddExpenseSheet sheet = new AddExpenseSheet();
            sheet.setRecurringMode(true);
            sheet.setListener((title, amount, category, description, date, isRecurring) -> {
                viewModel.insert(new Expense(title, amount, category, description, date, true));
            });
            sheet.show(getChildFragmentManager(), "add_recurring");
        });
        return v;
    }
}