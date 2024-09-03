package com.example.pomodorotimerapp.ui.statistics;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pomodorotimerapp.data.DatabaseHelper;

import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {
    private DatabaseHelper dbHelper;
    private MutableLiveData<List<DatabaseHelper.DailyStats>> weeklyStats;

    public StatisticsViewModel(Application application) {
        super(application);
        dbHelper = new DatabaseHelper(application);
        weeklyStats = new MutableLiveData<>();
    }

    public void loadWeeklyStats() {
        List<DatabaseHelper.DailyStats> stats = dbHelper.getWeeklyStats();
        weeklyStats.postValue(stats);
    }

    public LiveData<List<DatabaseHelper.DailyStats>> getWeeklyStats() {
        return weeklyStats;
    }

    public void resetStats() {
        dbHelper.resetStats();
        loadWeeklyStats();
    }
}