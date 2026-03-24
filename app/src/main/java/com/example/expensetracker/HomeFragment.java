package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeFragment extends Fragment {
    private ExpenseViewModel viewModel;
    private TextView tvTotalSum;
    private ExpenseAdapter adapter;
    private LinearLayout layoutEmpty; // Заглушка

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalSum = v.findViewById(R.id.tvTotalSum);
        layoutEmpty = v.findViewById(R.id.layoutEmpty); // Находим заглушку
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        androidx.appcompat.widget.SearchView searchView = v.findViewById(R.id.searchView);

        adapter = new ExpenseAdapter();

        adapter.setOnItemClickListener(expense -> {
            AddExpenseSheet sheet = new AddExpenseSheet();
            sheet.setExistingExpense(expense);
            sheet.setListener((title, amount, category, description, date, isRecurring) -> {
                expense.title = title;
                expense.amount = amount;
                expense.category = category;
                expense.description = description;
                expense.date = date;
                expense.isRecurring = isRecurring; // Не забудь обновить поле в объекте
                viewModel.update(expense);
            });
            sheet.show(getChildFragmentManager(), "edit_expense");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            adapter.setExpenses(expenses);
            updateUI(expenses);
            updateTotalSum(expenses);
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.search(newText).observe(getViewLifecycleOwner(), expenses -> {
                    adapter.setExpenses(expenses);
                });
                return true;
            }
        });
        searchView.clearFocus();

        v.findViewById(R.id.mainLayout).setOnClickListener(view -> hideKeyboard());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard();
            }
        });
        v.findViewById(R.id.fabAddNormal).setOnClickListener(view -> {
            AddExpenseSheet sheet = new AddExpenseSheet();
            sheet.setRecurringMode(false); // ОБЫЧНЫЙ
            sheet.setListener((title, amount, category, description, date, isRecurring) -> {
                viewModel.insert(new Expense(title, amount, category, description, date, false));
            });
            sheet.show(getChildFragmentManager(), "add_expense");
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                Expense expense = adapter.getExpenseAt(vh.getAdapterPosition());
                viewModel.delete(expense);
                Toast.makeText(getContext(), "Удалено: " + expense.title, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                new com.google.android.material.shape.MaterialShapeDrawable();
                android.graphics.Paint paint = new android.graphics.Paint();
                paint.setColor(android.graphics.Color.parseColor("#E91E63")); // Розовый/Красный

                android.graphics.RectF background = new android.graphics.RectF(
                        (float) viewHolder.itemView.getRight() + dX, (float) viewHolder.itemView.getTop(),
                        (float) viewHolder.itemView.getRight(), (float) viewHolder.itemView.getBottom());
                c.drawRect(background, paint);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
        View budgetBox = v.findViewById(R.id.totalCard);
        budgetBox.setOnClickListener(view -> {
            android.widget.EditText etLimit = new android.widget.EditText(getContext());
            etLimit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etLimit.setText(String.valueOf(PreferenceManager.getBudget(requireContext())));

            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Месячный лимит")
                    .setMessage("Введите сумму лимита:")
                    .setView(etLimit)
                    .setPositiveButton("Сохранить", (d, w) -> {
                        String val = etLimit.getText().toString();
                        if (!val.isEmpty()) {
                            float newLimit = Float.parseFloat(val);
                            PreferenceManager.setBudget(requireContext(), newLimit);

                            // ВАЖНО: Просто вызываем обновление UI с текущими данными
                            List<Expense> currentExpenses = viewModel.getAllExpenses().getValue();
                            if (currentExpenses != null) {
                                updateTotalSum(currentExpenses);
                            }
                            android.widget.Toast.makeText(getContext(), "Лимит сохранен", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        return v;
    }

    private void updateUI(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }

        double total = 0;
        for (Expense e : expenses) total += e.amount;
        tvTotalSum.setText(String.format("%.2f ₽", total));
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void updateTotalSum(List<Expense> expenses) {
        double total = 0;
        if (expenses != null) {
            for (Expense e : expenses) total += e.amount;
        }
        tvTotalSum.setText(String.format("%.2f ₽", total));

        // Читаем лимит из настроек
        float limit = PreferenceManager.getBudget(requireContext());

        int progress = 0;
        if (limit > 0) {
            progress = (int) ((total / limit) * 100);
        }

        com.google.android.material.progressindicator.LinearProgressIndicator pb = getView().findViewById(R.id.budgetProgress);
        TextView tvStatus = getView().findViewById(R.id.tvBudgetStatus);

        if (pb != null && tvStatus != null) {
            pb.setProgress(Math.min(progress, 100), true);
            tvStatus.setText(String.format("Потрачено %d%% от лимита (%.0f ₽)", progress, limit));

            if (progress >= 100) {
                pb.setIndicatorColor(android.graphics.Color.RED);
            } else {
                pb.setIndicatorColor(android.graphics.Color.WHITE);
            }
        }
    }
}