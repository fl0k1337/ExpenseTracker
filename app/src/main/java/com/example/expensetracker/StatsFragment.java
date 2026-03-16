package com.example.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {
    private PieChart pieChart;
    private ExpenseViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);
        pieChart = v.findViewById(R.id.pieChart);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), this::updateChart);

        return v;
    }

    private void updateChart(List<Expense> expenses) {
        Map<String, Double> map = new HashMap<>();
        for (Expense e : expenses) {
            map.put(e.category, map.getOrDefault(e.category, 0.0) + e.amount);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(14f);

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}