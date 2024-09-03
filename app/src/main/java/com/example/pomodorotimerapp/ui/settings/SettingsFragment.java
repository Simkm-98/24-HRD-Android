package com.example.pomodorotimerapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pomodorotimerapp.databinding.FragmentSettingBinding;
import com.example.pomodorotimerapp.ui.home.HomeViewModel;

public class SettingsFragment extends Fragment {

    private FragmentSettingBinding binding;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 뷰 바인딩 초기화
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // HomeViewModel 인스턴스 가져오기
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // 현재 설정된 시간 표시
        displayCurrentSettings();

        // 설정 저장 버튼 클릭 리스너 설정
        binding.buttonSaveSettings.setOnClickListener(v -> saveSettings());

        return root;
    }

    // 현재 설정된 시간을 UI에 표시
    private void displayCurrentSettings() {
        long focusTime = homeViewModel.getFocusTime() / 1000;
        long breakTime = homeViewModel.getBreakTime() / 1000;
        long longBreakTime = homeViewModel.getLongBreakTime() / 1000;

        binding.editFocusTimeMinutes.setText(String.valueOf(focusTime / 60));
        binding.editFocusTimeSeconds.setText(String.valueOf(focusTime % 60));
        binding.editBreakTimeMinutes.setText(String.valueOf(breakTime / 60));
        binding.editBreakTimeSeconds.setText(String.valueOf(breakTime % 60));
        binding.editLongBreakTimeMinutes.setText(String.valueOf(longBreakTime / 60));
        binding.editLongBreakTimeSeconds.setText(String.valueOf(longBreakTime % 60));
    }

    // 설정 저장
    private void saveSettings() {
        try {
            // 입력된 시간을 밀리초 단위로 변환
            long focusTime = getTimeInMillis(binding.editFocusTimeMinutes, binding.editFocusTimeSeconds);
            long breakTime = getTimeInMillis(binding.editBreakTimeMinutes, binding.editBreakTimeSeconds);
            long longBreakTime = getTimeInMillis(binding.editLongBreakTimeMinutes, binding.editLongBreakTimeSeconds);

            // HomeViewModel에 설정 저장
            homeViewModel.setFocusTime(focusTime);
            homeViewModel.setBreakTime(breakTime);
            homeViewModel.setLongBreakTime(longBreakTime);

            Toast.makeText(getContext(), "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "올바른 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 입력된 분과 초를 밀리초 단위로 변환
    private long getTimeInMillis(EditText minutesEdit, EditText secondsEdit) {
        int minutes = parseEditTextToInt(minutesEdit);
        int seconds = parseEditTextToInt(secondsEdit);
        return (minutes * 60L + seconds) * 1000L;
    }

    // EditText의 내용을 정수로 파싱
    private int parseEditTextToInt(EditText editText) {
        String text = editText.getText().toString();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}