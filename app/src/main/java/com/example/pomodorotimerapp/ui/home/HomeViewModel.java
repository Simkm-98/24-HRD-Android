package com.example.pomodorotimerapp.ui.home;

import android.app.Application;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pomodorotimerapp.data.DatabaseHelper;

public class HomeViewModel extends AndroidViewModel {
    private static final int MAX_CYCLES = 4;

    // LiveData 객체들
    private final MutableLiveData<Long> timeLeft = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isFocusTime = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> currentCycle = new MutableLiveData<>(1);
    private final MutableLiveData<Boolean> isLongBreak = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> showLongBreakDialog = new MutableLiveData<>(false);
    private final MutableLiveData<String> notification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTimerReset = new MutableLiveData<>(false);

    private DatabaseHelper dbHelper;
    private CountDownTimer countDownTimer;
    private static final String TAG = "HomeViewModel";

    // 기본 시간 설정 (밀리초 단위)
    private long focusTime = 25 * 60 * 1000; // 25분
    private long breakTime = 5 * 60 * 1000; // 5분
    private long longBreakTime = 15 * 60 * 1000; // 15분

    public HomeViewModel(Application application) {
        super(application);
        dbHelper = new DatabaseHelper(application);
        timeLeft.setValue(focusTime);
    }
    public void incrementFocusCount() {
        dbHelper.addOrUpdateStats("focus");
        Log.d(TAG, "Focus count incremented");
    }

    public void incrementBreakCount() {
        dbHelper.addOrUpdateStats("break");
        Log.d(TAG, "Break count incremented");
    }

    public void incrementLongBreakCount() {
        dbHelper.addOrUpdateStats("longBreak");
        Log.d(TAG, "Long break count incremented");
    }
    // Getter 및 Setter 메서드들
    public LiveData<Boolean> getIsTimerReset() {
        return isTimerReset;
    }

    private void setIsTimerReset(boolean reset) {
        isTimerReset.setValue(reset);
    }

    public LiveData<Long> getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long time) {
        timeLeft.setValue(time);
    }

    public LiveData<Boolean> getIsTimerRunning() {
        return isTimerRunning;
    }

    public void setIsTimerRunning(boolean running) {
        isTimerRunning.setValue(running);
    }

    public LiveData<Boolean> getIsFocusTime() {
        return isFocusTime;
    }

    public void setIsFocusTime(boolean focus) {
        isFocusTime.setValue(focus);
    }

    public LiveData<Integer> getCurrentCycle() {
        return currentCycle;
    }

    public String getCycleText() {
        Integer cycle = currentCycle.getValue();
        if (cycle == null) {
            cycle = 0;
        }
        return "사이클: " + cycle + "/" + MAX_CYCLES;
    }

    // 사이클 증가
    public void incrementCycle() {
        Integer currentCycleValue = currentCycle.getValue();
        if (currentCycleValue != null && currentCycleValue < MAX_CYCLES) {
            currentCycle.setValue(currentCycleValue + 1);
        }
    }

    // 마지막 사이클인지 확인
    public boolean isLastCycle() {
        Integer currentCycleValue = currentCycle.getValue();
        return currentCycleValue != null && currentCycleValue >= MAX_CYCLES;
    }

    public void setShowLongBreakDialog(boolean show) {
        showLongBreakDialog.setValue(show);
    }

    public LiveData<Boolean> getShowLongBreakDialog() {
        return showLongBreakDialog;
    }

    // 긴 휴식 시작
    public void startLongBreak() {
        isLongBreak.setValue(true);
        setTimeLeft(longBreakTime);
        setIsFocusTime(false);
    }

    // 긴 휴식 종료
    public void finishLongBreak() {
        isLongBreak.setValue(false);
        resetCycle();
        setIsFocusTime(true);
        setTimeLeft(focusTime);
    }

    public boolean isLongBreak() {
        return isLongBreak.getValue();
    }

    public LiveData<Boolean> getIsLongBreak() {
        return isLongBreak;
    }

    // 시간 설정 관련 Getter 및 Setter
    public long getFocusTime() {
        return focusTime;
    }

    public long getBreakTime() {
        return isLongBreak.getValue() ? longBreakTime : breakTime;
    }

    public long getLongBreakTime() {
        return longBreakTime;
    }

    public void setFocusTime(long focusTime) {
        this.focusTime = focusTime;
    }

    public void setBreakTime(long breakTime) {
        this.breakTime = breakTime;
    }

    public void setLongBreakTime(long longBreakTime) {
        this.longBreakTime = longBreakTime;
    }

    // 통계 초기화
    public void resetStatistics() {
        dbHelper.resetStats();
    }

    // 사이클 초기화
    public void resetCycle() {
        currentCycle.setValue(1);
        isLongBreak.setValue(false);
        setIsFocusTime(true);
        setTimeLeft(focusTime);
        setIsTimerReset(true);
    }

    // 다음 타이머 준비
    public void prepareForNextTimer() {
        setIsFocusTime(true);
        setTimeLeft(getFocusTime());
        setIsTimerRunning(false);
        setIsTimerReset(true);
    }

    public LiveData<String> getNotification() {
        return notification;
    }

    private void triggerNotification(String message) {
        notification.setValue(message);
    }

    // 타이머 시작
    public void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        setIsTimerReset(false);

        long timeLeftInMillis = timeLeft.getValue();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimeLeft(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                setTimeLeft(0);
                setIsTimerRunning(false);
                if (isLongBreak.getValue()) {
                    triggerNotification("긴 휴식 시간 종료");
                    incrementLongBreakCount();
                    Log.d(TAG, "Long break session finished");
                    finishLongBreak();
                    prepareForNextTimer();
                } else if (isFocusTime.getValue()) {
                    triggerNotification("집중 시간 종료");
                    incrementFocusCount();
                    Log.d(TAG, "Focus session finished");
                    if (isLastCycle()) {
                        setShowLongBreakDialog(true);
                    } else {
                        setIsFocusTime(false);
                        setTimeLeft(getBreakTime());
                        startTimer();
                    }
                } else {
                    triggerNotification("휴식 시간 종료");
                    incrementBreakCount();
                    Log.d(TAG, "Break session finished");
                    incrementCycle();
                    setIsFocusTime(true);
                    setTimeLeft(getFocusTime());
                    startTimer();
                }
            }
        }.start();

        setIsTimerRunning(true);
    }

    // 타이머 일시 정지
    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        setIsTimerRunning(false);
    }

    // 타이머 리셋
    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        setIsTimerRunning(false);
        setIsFocusTime(true);
        setTimeLeft(getFocusTime());
        resetCycle();
        setIsTimerReset(true);
    }
}