package com.example.pomodorotimerapp.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.pomodorotimerapp.data.DatabaseHelper;
import com.example.pomodorotimerapp.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private BarChart barChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        barChart = binding.barChart;

        setupBarChart();

        statisticsViewModel.getWeeklyStats().observe(getViewLifecycleOwner(), this::updateWeeklyStats);

        binding.buttonResetStats.setOnClickListener(v -> resetStatistics());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        statisticsViewModel.loadWeeklyStats();
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);
    }

    private void updateWeeklyStats(List<DatabaseHelper.DailyStats> weeklyStats) {
        updateTextStats(weeklyStats);
        updateBarChart(weeklyStats);
    }

    private void updateTextStats(List<DatabaseHelper.DailyStats> weeklyStats) {
        StringBuilder statsText = new StringBuilder();
        int totalFocus = 0, totalBreak = 0, totalLongBreak = 0;

        for (DatabaseHelper.DailyStats dailyStats : weeklyStats) {
            statsText.append(String.format("%s: 집중 %d회, 휴식 %d회, 긴 휴식 %d회\n",
                    dailyStats.date, dailyStats.focusCount,
                    dailyStats.breakCount, dailyStats.longBreakCount));

            totalFocus += dailyStats.focusCount;
            totalBreak += dailyStats.breakCount;
            totalLongBreak += dailyStats.longBreakCount;
        }

        binding.textWeeklyStats.setText(statsText.toString());
        binding.textTotalStats.setText(String.format("총계: 집중 %d회, 휴식 %d회, 긴 휴식 %d회",
                totalFocus, totalBreak, totalLongBreak));
    }

    private void updateBarChart(List<DatabaseHelper.DailyStats> weeklyStats) {
        ArrayList<BarEntry> focusEntries = new ArrayList<>();
        ArrayList<BarEntry> breakEntries = new ArrayList<>();
        ArrayList<BarEntry> longBreakEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < weeklyStats.size(); i++) {
            DatabaseHelper.DailyStats stats = weeklyStats.get(i);
            focusEntries.add(new BarEntry(i, stats.focusCount));
            breakEntries.add(new BarEntry(i, stats.breakCount));
            longBreakEntries.add(new BarEntry(i, stats.longBreakCount));
            labels.add(stats.date);
        }

        BarDataSet focusSet = new BarDataSet(focusEntries, "집중");
        focusSet.setColor(Color.RED);
        BarDataSet breakSet = new BarDataSet(breakEntries, "휴식");
        breakSet.setColor(Color.GREEN);
        BarDataSet longBreakSet = new BarDataSet(longBreakEntries, "긴 휴식");
        longBreakSet.setColor(Color.BLUE);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(focusSet);
        dataSets.add(breakSet);
        dataSets.add(longBreakSet);

        BarData data = new BarData(dataSets);
        data.setBarWidth(0.25f);

        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.groupBars(0, 0.06f, 0.02f);
        barChart.invalidate();
    }

    private void resetStatistics() {
        statisticsViewModel.resetStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}