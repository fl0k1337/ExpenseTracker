package com.example.expensetracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {
    private PieChart pieChart;
    private TextView tvTotalStats, tvEmptyStats;
    private LinearLayout categoriesContainer;
    private ExpenseViewModel viewModel;
    private Map<String, Double> categorySumMap = new HashMap<>();
    private double currentTotal = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);

        pieChart = v.findViewById(R.id.pieChart);
        tvTotalStats = v.findViewById(R.id.tvTotalStats);
        tvEmptyStats = v.findViewById(R.id.tvEmptyStats);
        categoriesContainer = v.findViewById(R.id.categoriesContainer);
        ChipGroup periodChips = v.findViewById(R.id.periodChips);

        // Настройка диаграммы (БЕЗ ТЕКСТА В ЦЕНТРЕ)
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(75f);
        pieChart.setTransparentCircleRadius(80f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setDrawCenterText(false); // УБРАЛИ ТРАТЫ В ЦЕНТРЕ
        pieChart.getDescription().setEnabled(false); // УБРАЛИ DESCRIPTION LABEL
        pieChart.getLegend().setEnabled(false); // УБРАЛИ ЛИШНЮЮ ЛЕГЕНДУ БИБЛИОТЕКИ
        pieChart.setDrawEntryLabels(false);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        periodChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipWeek)) viewModel.setPeriod(7);
            else if (checkedIds.contains(R.id.chipMonth)) viewModel.setPeriod(30);
            else if (checkedIds.contains(R.id.chipYear)) viewModel.setPeriod(365);
            else if (checkedIds.contains(R.id.chipAll)) viewModel.setPeriod(0);
        });

        viewModel.getExpensesByPeriod().observe(getViewLifecycleOwner(), this::updateStats);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                showSingleCategory(pe.getLabel(), pe.getValue());
            }

            @Override
            public void onNothingSelected() {
                renderCategoryList(categorySumMap); // Возвращаем весь список
            }
        });

        return v;
    }

    private void updateStats(List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            categoriesContainer.setVisibility(View.GONE);
            tvEmptyStats.setVisibility(View.VISIBLE);
            tvTotalStats.setText("0 ₽");
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        categoriesContainer.setVisibility(View.VISIBLE);
        tvEmptyStats.setVisibility(View.GONE);

        currentTotal = 0;
        categorySumMap.clear();
        for (Expense e : expenses) {
            currentTotal += e.amount;
            categorySumMap.put(e.category, categorySumMap.getOrDefault(e.category, 0.0) + e.amount);
        }
        tvTotalStats.setText(String.format("%.2f ₽", currentTotal));

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categorySumMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            colors.add(CategoryColorMapper.getColor(entry.getKey()));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(colors);
        set.setSliceSpace(3f);
        set.setDrawValues(false);

        pieChart.setData(new PieData(set));
        pieChart.animateY(800);
        pieChart.invalidate();

        renderCategoryList(categorySumMap);
    }

    // Рендер всего списка категорий
    private void renderCategoryList(Map<String, Double> map) {
        categoriesContainer.removeAllViews();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            addCategoryItem(entry.getKey(), entry.getValue());
        }
    }

    // Показ только одной выбранной категории
    private void showSingleCategory(String name, float amount) {
        categoriesContainer.removeAllViews();
        addCategoryItem(name, (double) amount);
    }

    private void addCategoryItem(String name, Double amount) {
        View item = getLayoutInflater().inflate(R.layout.item_category_stat, categoriesContainer, false);
        ((TextView)item.findViewById(R.id.tvCategoryName)).setText(name);
        ((TextView)item.findViewById(R.id.tvCategoryAmount)).setText(String.format("%.0f ₽ (%.1f%%)", amount, (amount/currentTotal)*100));
        item.findViewById(R.id.colorDot).setBackgroundColor(CategoryColorMapper.getColor(name));
        categoriesContainer.addView(item);
    }
}