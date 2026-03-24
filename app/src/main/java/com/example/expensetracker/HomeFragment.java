package com.example.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalSum = v.findViewById(R.id.tvTotalSum);
        layoutEmpty = v.findViewById(R.id.layoutEmpty); // Находим заглушку
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        androidx.appcompat.widget.SearchView searchView = v.findViewById(R.id.searchView);

        // --- ИНИЦИАЛИЗАЦИЯ АДАПТЕРА (ОДИН РАЗ!) ---
        adapter = new ExpenseAdapter();

        // Добавляем клик по элементу для РЕДАКТИРОВАНИЯ
        adapter.setOnItemClickListener(expense -> {
            AddExpenseSheet sheet = new AddExpenseSheet();
            sheet.setExistingExpense(expense); // Передаем данные для редактирования
            sheet.setListener((title, amount, category, description) -> {
                expense.title = title;
                expense.amount = amount;
                expense.category = category;
                expense.description = description;
                viewModel.update(expense); // Обновляем в БД
            });
            sheet.show(getChildFragmentManager(), "edit_expense");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        // Наблюдаем за расходами
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            adapter.setExpenses(expenses);
            updateUI(expenses);
        });

        // Поиск
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

        // Скрытие клавиатуры
        v.findViewById(R.id.mainLayout).setOnClickListener(view -> hideKeyboard());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard();
            }
        });

        // Удаление свайпом
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                Expense expense = adapter.getExpenseAt(vh.getAdapterPosition());
                viewModel.delete(expense);
            }
        }).attachToRecyclerView(recyclerView);

        return v;
    }

    private void updateUI(List<Expense> expenses) {
        // Если список пуст - показываем "Empty State"
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
}