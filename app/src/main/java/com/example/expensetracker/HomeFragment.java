package com.example.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalSum = v.findViewById(R.id.tvTotalSum);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        androidx.appcompat.widget.SearchView searchView = v.findViewById(R.id.searchView);

        adapter = new ExpenseAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            adapter.setExpenses(expenses);
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

        v.findViewById(R.id.totalCard).setOnClickListener(view -> hideKeyboard());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int position = vh.getAdapterPosition();
                Expense expenseToDelete = adapter.getExpenseAt(position);

                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Удаление")
                        .setMessage("Удалить этот расход?")
                        .setPositiveButton("Да", (dialog, which) -> viewModel.delete(expenseToDelete))
                        .setNegativeButton("Нет", (dialog, which) -> adapter.notifyItemChanged(position))
                        .setCancelable(false)
                        .show();
            }
        }).attachToRecyclerView(recyclerView);

        return v;
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }

    private void updateTotalSum(List<Expense> expenses) {
        double total = 0;
        for (Expense e : expenses) total += e.amount;
        tvTotalSum.setText(String.format("%.2f ₽", total));
    }
}