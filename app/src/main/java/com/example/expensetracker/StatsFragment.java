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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);

        pieChart = v.findViewById(R.id.pieChart);
        tvTotalStats = v.findViewById(R.id.tvTotalStats);
        tvEmptyStats = v.findViewById(R.id.tvEmptyStats);
        categoriesContainer = v.findViewById(R.id.categoriesContainer);

        ChipGroup periodChips = v.findViewById(R.id.periodChips);
        periodChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipWeek)) {
                viewModel.setPeriod(7);
            } else if (checkedIds.contains(R.id.chipMonth)) {
                viewModel.setPeriod(30);
            } else if (checkedIds.contains(R.id.chipYear)) {
                viewModel.setPeriod(365);
            } else if (checkedIds.contains(R.id.chipAll)) {
                viewModel.setPeriod(0);
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), this::updateStats);
        viewModel.getExpensesByPeriod().observe(getViewLifecycleOwner(), this::updateStats);

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

        // Подсчёт общей суммы
        double total = 0;
        Map<String, Double> categorySum = new HashMap<>();
        for (Expense e : expenses) {
            total += e.amount;
            categorySum.put(e.category, categorySum.getOrDefault(e.category, 0.0) + e.amount);
        }
        tvTotalStats.setText(String.format("%.2f ₽", total));

        // Подготовка данных для диаграммы
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categorySum.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(getColors(entries.size()));
        set.setValueTextSize(14f);
        set.setValueTextColor(Color.WHITE);
        set.setSliceSpace(2f);
        set.setSelectionShift(5f);

        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        // Настройка диаграммы
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
//        pieChart.setDrawCenterText(true);
//        pieChart.setCenterText("Всего\n" + String.format("%.0f ₽", total));
//        pieChart.setCenterTextSize(14f);
//        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);

        // Легенда
        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);

        pieChart.animateY(1000);
        pieChart.invalidate();

        // Заполняем список категорий под диаграммой
        categoriesContainer.removeAllViews();
        for (Map.Entry<String, Double> entry : categorySum.entrySet()) {
            View item = getLayoutInflater().inflate(R.layout.item_category_stat, categoriesContainer, false);
            TextView tvCategory = item.findViewById(R.id.tvCategoryName);
            TextView tvAmount = item.findViewById(R.id.tvCategoryAmount);
            View colorDot = item.findViewById(R.id.colorDot);

            tvCategory.setText(entry.getKey());
            tvAmount.setText(String.format("%.2f ₽ (%.1f%%)", entry.getValue(), (entry.getValue() / total) * 100));
            // Устанавливаем цвет точки, соответствующий цвету на диаграмме (можно придумать логику или использовать индексы)
            // Для простоты используем случайный цвет из набора
            colorDot.setBackgroundColor(getColorForCategory(entry.getKey()));

            categoriesContainer.addView(item);
        }
    }

    private int[] getColors(int count) {
        // Генерируем массив цветов (можно использовать ColorTemplate.MATERIAL_COLORS)
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length];
        }
        return colors;
    }

    private int getColorForCategory(String category) {
        // Просто возвращаем цвет в зависимости от категории (можно хешировать)
        // Или используем ту же логику, что и для диаграммы
        return ColorTemplate.MATERIAL_COLORS[Math.abs(category.hashCode()) % ColorTemplate.MATERIAL_COLORS.length];
    }
}