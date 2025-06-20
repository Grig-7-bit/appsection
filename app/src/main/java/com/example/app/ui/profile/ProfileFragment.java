package com.example.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app.R;
import com.example.app.aLog_in;
import com.example.app.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private static final String TAG = "ProfileFragment";
    private static final String USERS_COLLECTION = "users";
    private static final String PROFILE_SUBCOLLECTION = "profile";
    private static final String USER_DATA_DOCUMENT = "userData";
    private static final String NAME_FIELD = "name";
    private static final String SURNAME_FIELD = "surname";
    private static final String PHONE_FIELD = "phone";
    private static final String EMAIL_FIELD = "email";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initFirebase();
            checkAuthentication();
            initViews();
            loadUserData();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            showToast(getString(R.string.initialization_error));
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void checkAuthentication() {
        if (currentUser == null) {
            navigateToLoginScreen();
            return;
        }
    }

    private void initViews() {
        // Установка email из Firebase Auth
        if (currentUser != null && currentUser.getEmail() != null) {
            binding.emailTextView.setText(currentUser.getEmail());
        }

        binding.buttonSave.setOnClickListener(v -> {
            String name = binding.editTextName.getText().toString().trim();
            String surname = binding.editTextSurname.getText().toString().trim();
            String phone = binding.editTextExtra1.getText().toString().trim();

            if (validateInput(name, surname, phone)) {
                saveUserData(name, surname, phone);
            }
        });

        binding.buttonEdit.setOnClickListener(v -> setupEditMode(true));
        setupEditMode(false);
    }

    private boolean validateInput(String name, String surname, String phone) {
        if (TextUtils.isEmpty(name)) {
            binding.editTextName.setError(getString(R.string.enter_name_error));
            binding.editTextName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(surname)) {

            binding.editTextSurname.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {

            binding.editTextExtra1.requestFocus();
            return false;
        }

        return true;
    }

    private void loadUserData() {
        if (currentUser == null || db == null) {
            Log.w(TAG, "Current user or Firestore is null");
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .collection(PROFILE_SUBCOLLECTION)
                .document(USER_DATA_DOCUMENT)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding == null) return;

                    if (documentSnapshot.exists()) {
                        // Загрузка имени
                        String name = documentSnapshot.getString(NAME_FIELD);
                        if (name != null && !name.isEmpty()) {
                            binding.editTextName.setText(name);
                        }

                        // Загрузка фамилии
                        String surname = documentSnapshot.getString(SURNAME_FIELD);
                        if (surname != null && !surname.isEmpty()) {
                            binding.editTextSurname.setText(surname);
                        }

                        // Загрузка телефона
                        String phone = documentSnapshot.getString(PHONE_FIELD);
                        if (phone != null && !phone.isEmpty()) {
                            binding.editTextExtra1.setText(phone);
                        }

                        // Установка email из Firebase Auth
                        if (currentUser.getEmail() != null) {
                            binding.emailTextView.setText(currentUser.getEmail());
                        }
                    } else {
                        createInitialProfileData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading profile data", e);
                    showToast(getString(R.string.profile_load_error));
                });
    }

    private void createInitialProfileData() {
        if (currentUser == null || db == null) return;

        Map<String, Object> profileData = new HashMap<>();
        profileData.put(EMAIL_FIELD, currentUser.getEmail());
        profileData.put(NAME_FIELD, currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "");
        profileData.put(SURNAME_FIELD, "");
        profileData.put(PHONE_FIELD, "");

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .collection(PROFILE_SUBCOLLECTION)
                .document(USER_DATA_DOCUMENT)
                .set(profileData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Initial profile data created"))
                .addOnFailureListener(e -> Log.w(TAG, "Error creating initial profile data", e));
    }

    private void saveUserData(String name, String surname, String phone) {
        if (currentUser == null || db == null || binding == null) return;

        binding.buttonSave.setEnabled(false);

        DocumentReference profileRef = db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .collection(PROFILE_SUBCOLLECTION)
                .document(USER_DATA_DOCUMENT);

        Map<String, Object> updates = new HashMap<>();
        updates.put(NAME_FIELD, name);
        updates.put(SURNAME_FIELD, surname);
        updates.put(PHONE_FIELD, phone);

        profileRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    showToast(getString(R.string.data_saved));
                    setupEditMode(false);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving profile data", e);
                    showToast(getString(R.string.save_error));
                })
                .addOnCompleteListener(task -> {
                    if (binding != null) {
                        binding.buttonSave.setEnabled(true);
                    }
                });
    }

    // Остальные методы остаются без изменений
    private void setupEditMode(boolean isEditMode) {
        if (binding == null) return;

        binding.editTextName.setEnabled(isEditMode);
        binding.editTextName.setFocusableInTouchMode(isEditMode);
        binding.editTextName.setCursorVisible(isEditMode);

        binding.editTextSurname.setEnabled(isEditMode);
        binding.editTextSurname.setFocusableInTouchMode(isEditMode);
        binding.editTextSurname.setCursorVisible(isEditMode);

        binding.editTextExtra1.setEnabled(isEditMode);
        binding.editTextExtra1.setFocusableInTouchMode(isEditMode);
        binding.editTextExtra1.setCursorVisible(isEditMode);

        binding.buttonSave.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.buttonEdit.setVisibility(isEditMode ? View.GONE : View.VISIBLE);

        if (isEditMode) {
            binding.editTextName.requestFocus();
        }
    }

    private void logoutUser() {
        if (mAuth != null) {
            mAuth.signOut();
            navigateToLoginScreen();
        }
    }

    private void navigateToLoginScreen() {
        if (!isAdded() || getActivity() == null) return;

        Intent intent = new Intent(requireActivity(), aLog_in.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showToast(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}