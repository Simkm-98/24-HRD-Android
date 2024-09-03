package com.example.pomodorotimerapp.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pomodorotimerapp.CircularTimerView;
import com.example.pomodorotimerapp.databinding.FragmentHomeBinding;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private TextView timerTextView;
    private TextView modeTextView;
    private TextView cycleTextView;
    private Button startPauseButton;
    private Button resetButton;
    private CircularTimerView circularTimerView;

    // 집중 시간과 휴식 시간의 색상 정의
    private static final int FOCUS_COLOR = Color.RED;
    private static final int BREAK_COLOR = Color.CYAN;

    // 다이얼로그 표시 여부를 추적하는 플래그
    private boolean isDialogShowing = false;
    private long totalTimeInMillis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // ViewModel 초기화
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // 뷰 바인딩 초기화
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // UI 요소 초기화
        timerTextView = binding.textTimer;
        modeTextView = binding.textMode;
        cycleTextView = binding.textCycle;
        startPauseButton = binding.buttonStartPause;
        resetButton = binding.buttonReset;
        circularTimerView = binding.circularTimerView;

        // 긴 휴식 다이얼로그 표시 여부 관찰
        homeViewModel.getShowLongBreakDialog().observe(getViewLifecycleOwner(), show -> {
            if (show && !isDialogShowing) {
                showLongBreakDialog();
            }
        });

        // 시작/일시정지 버튼 클릭 리스너 설정
        startPauseButton.setOnClickListener(v -> {
            if (homeViewModel.getIsTimerRunning().getValue()) {
                homeViewModel.pauseTimer();
            } else {
                homeViewModel.startTimer();
            }
        });

        // 리셋 버튼 클릭 리스너 설정
        resetButton.setOnClickListener(v -> homeViewModel.resetTimer());

        // ViewModel의 상태 변화 관찰 및 UI 업데이트
        homeViewModel.getTimeLeft().observe(getViewLifecycleOwner(), this::updateCountDownText);
        homeViewModel.getIsTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            startPauseButton.setText(isRunning ? "일시정지" : "시작");
        });
        homeViewModel.getIsFocusTime().observe(getViewLifecycleOwner(), this::updateTimerMode);
        homeViewModel.getCurrentCycle().observe(getViewLifecycleOwner(), cycle -> {
            cycleTextView.setText(homeViewModel.getCycleText());
        });

        // 타이머 리셋 상태 관찰
        homeViewModel.getIsTimerReset().observe(getViewLifecycleOwner(), isReset -> {
            if (isReset) {
                resetCircularTimer();
            }
        });

        return root;
    }

    // 타이머 모드 (집중/휴식) 업데이트
    private void updateTimerMode(boolean isFocus) {
        long totalTime = isFocus ? homeViewModel.getFocusTime() : homeViewModel.getBreakTime();
        totalTimeInMillis = totalTime;
        circularTimerView.setTotalTime(totalTime);
        circularTimerView.setProgressColor(isFocus ? FOCUS_COLOR : BREAK_COLOR);
        modeTextView.setText(isFocus ? "집중 시간" : "휴식 시간");
        if (!homeViewModel.getIsTimerRunning().getValue()) {
            homeViewModel.setTimeLeft(totalTime);
            updateCountDownText(totalTime);
        }
    }

    // 남은 시간 텍스트 업데이트
    private void updateCountDownText(long millisUntilFinished) {
        int seconds = (int) (millisUntilFinished / 1000);
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        timerTextView.setText(timeLeftFormatted);
        updateCircularTimer(millisUntilFinished);
    }

    // 원형 타이머 뷰 업데이트
    private void updateCircularTimer(long timeLeftInMillis) {
        circularTimerView.updateTime(timeLeftInMillis);
    }

    // 원형 타이머 리셋
    private void resetCircularTimer() {
        circularTimerView.reset();
        updateTimerMode(true);
    }

    // 긴 휴식 시간 다이얼로그 표시
    private void showLongBreakDialog() {
        if (isDialogShowing) {
            return;
        }
        isDialogShowing = true;
        new AlertDialog.Builder(requireContext())
                .setTitle("긴 휴식 시간")
                .setMessage("4 사이클의 집중 시간을 완료했습니다. 긴 휴식을 취하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> {
                    homeViewModel.startLongBreak();
                    homeViewModel.setShowLongBreakDialog(false);
                    isDialogShowing = false;
                    homeViewModel.startTimer();
                })
                .setNegativeButton("아니오", (dialog, which) -> {
                    homeViewModel.resetCycle();
                    homeViewModel.setShowLongBreakDialog(false);
                    isDialogShowing = false;
                    homeViewModel.prepareForNextTimer();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}