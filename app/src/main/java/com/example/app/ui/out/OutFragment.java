package com.example.app.ui.out;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app.R;
import com.example.app.aLog_in;
import com.google.firebase.auth.FirebaseAuth;

public class OutFragment extends Fragment {

    private static final String TAG = "OutFragment";
    private OutViewModel mViewModel;
    private FirebaseAuth mAuth;

    public static OutFragment newInstance() {
        return new OutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance(); // Инициализация FirebaseAuth
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_out, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация ViewModel
        mViewModel = new ViewModelProvider(this).get(OutViewModel.class);

        signOut();
    }

    private void signOut() {
        try {
            if (mAuth != null) {
                mAuth.signOut();
                showToast("Вы успешно вышли из системы");
                navigateToLoginScreen();
            } else {
                Log.e(TAG, "FirebaseAuth instance is null");
                navigateToLoginScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during sign out", e);
            showToast("Ошибка при выходе из системы");
            navigateToLoginScreen();
        }
    }

    private void navigateToLoginScreen() {
        try {
            if (isAdded() && getActivity() != null) {
                Intent intent = new Intent(requireActivity(), aLog_in.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login screen", e);
        }
    }

    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}