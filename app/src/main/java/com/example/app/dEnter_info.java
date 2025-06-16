package com.example.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app.UtilityClasses.CityArrayAdapter;
import com.example.app.UtilityClasses.DayArrayAdapter;
import com.example.app.UtilityClasses.TimeArrayAdapter;
import com.example.app.data.SectionData;
import com.example.app.databinding.Activity3CreateSectionBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class dEnter_info extends AppCompatActivity {

    private Activity3CreateSectionBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean isEditMode = false;
    private boolean isEditing = false;
    private String currentSectionId = "";
    private String currentPrice = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = Activity3CreateSectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            currentUser = mAuth.getCurrentUser();
            setupCategorySpinner();

            if (getIntent() != null) {
                isEditMode = getIntent().hasExtra("section_id");
                currentSectionId = getIntent().getStringExtra("section_id");
                if (currentSectionId == null) {
                    currentSectionId = "";
                }
            }


// В вашем Activity/Fragment


            initViews();
            setupUI();
            setupDeleteButton();
            chooseCity();
            chooseDay();
            chooseTime();

            if (isEditMode) {
                loadSectionData();
            }
        } catch (Exception e) {
            Log.e("dEnter_info", "Error in onCreate", e);
            Toast.makeText(this, "Ошибка инициализации", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupCategorySpinner() {
        if (binding == null || binding.categorySpinner == null) return;

        // Создаем адаптер для Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.section_categories,
                android.R.layout.simple_spinner_item
        );

        // Устанавливаем стиль для выпадающего списка
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Применяем адаптер к Spinner
        binding.categorySpinner.setAdapter(adapter);

        // Устанавливаем обработчик выбора элемента
        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Первый элемент - подсказка "Выберите категорию"
                if (position == 0 && isEditing) {
                    //Toast.makeText(dEnter_info.this, "Пожалуйста, выберите категорию", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    private void chooseCity() {
        if (binding == null || binding.cityAutoCompleteTextView == null) return;

        String[] citiesArray = getResources().getStringArray(R.array.cities);
        List<String> cities = new ArrayList<>(Arrays.asList(citiesArray));

        CityArrayAdapter adapter = new CityArrayAdapter(this, R.layout.city_dropdown_item, cities);

        binding.cityAutoCompleteTextView.setAdapter(adapter);
        binding.cityAutoCompleteTextView.setDropDownBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E0FFFF")));
        binding.cityAutoCompleteTextView.setThreshold(1);

        binding.cityAutoCompleteTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isFinishing() && !isDestroyed() && binding != null && binding.cityAutoCompleteTextView != null) {
                        binding.cityAutoCompleteTextView.showDropDown();
                    }
                });
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void chooseDay() {
        if (binding == null || binding.scheduleDayEditText == null) return;

        String[] daysArray = getResources().getStringArray(R.array.days_of_week);
        List<String> days = new ArrayList<>(Arrays.asList(daysArray));

        DayArrayAdapter adapter = new DayArrayAdapter(this, R.layout.city_dropdown_item, days);

        binding.scheduleDayEditText.setAdapter(adapter);
        binding.scheduleDayEditText.setDropDownBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E0FFFF")));
        binding.scheduleDayEditText.setThreshold(1);

        binding.scheduleDayEditText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        binding.scheduleDayEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isFinishing() && !isDestroyed() && binding != null && binding.scheduleDayEditText != null) {
                        binding.scheduleDayEditText.showDropDown();
                    }
                });
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void chooseTime() {
        if (binding == null || binding.scheduleTimeEditText == null) return;

        List<String> times = generateTimeIntervals();

        TimeArrayAdapter adapter = new TimeArrayAdapter(this, R.layout.city_dropdown_item, times);

        binding.scheduleTimeEditText.setAdapter(adapter);
        binding.scheduleTimeEditText.setDropDownBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E0FFFF")));
        binding.scheduleTimeEditText.setThreshold(1);

        binding.scheduleTimeEditText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        binding.scheduleTimeEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isFinishing() && !isDestroyed() && binding != null && binding.scheduleTimeEditText != null) {
                        binding.scheduleTimeEditText.showDropDown();
                    }
                });
            }
            return false;
        });
    }

    private List<String> generateTimeIntervals() {
        List<String> times = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                times.add(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }
        }
        return times;
    }

    private void initViews() {
        if (binding == null) return;

        if (binding.saveButton != null) {
            binding.saveButton.setOnClickListener(v -> saveSection());
        }
        if (binding.backButton != null) {
            binding.backButton.setOnClickListener(v -> finish());
        }
        if (binding.editButton != null) {
            binding.editButton.setOnClickListener(v -> enableEditing(true));
        }

        if (binding.titleEditText != null) {
            binding.titleEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }

        if (binding.cityAutoCompleteTextView != null) {
            binding.cityAutoCompleteTextView.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.locationEditText != null) {
            binding.locationEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            binding.locationEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.priceEditText != null) {
            binding.priceEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.scheduleDayEditText != null) {
            binding.scheduleDayEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.scheduleTimeEditText != null) {
            binding.scheduleTimeEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.maxParticipantsEditText != null) {
            binding.maxParticipantsEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.phoneEditText != null) {
            binding.phoneEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        }
        if (binding.emailEditText != null) {
            binding.emailEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupUI() {
        if (binding == null) return;

        if (isEditMode) {
            enableEditing(false);
            if (binding.editButton != null) binding.editButton.setVisibility(View.VISIBLE);
            if (binding.saveButton != null) binding.saveButton.setVisibility(View.GONE);
            if (binding.deleteButton != null) binding.deleteButton.setVisibility(View.VISIBLE);
        } else {
            enableEditing(true);
            if (binding.editButton != null) binding.editButton.setVisibility(View.GONE);
            if (binding.saveButton != null) binding.saveButton.setVisibility(View.VISIBLE);
            if (binding.deleteButton != null) binding.deleteButton.setVisibility(View.GONE);
        }
    }

    private void enableEditing(boolean enable) {
        isEditing = enable;
        if (binding == null) return;

        if (binding.titleEditText != null) binding.titleEditText.setEnabled(enable);
        if (binding.descriptionEditText != null) binding.descriptionEditText.setEnabled(enable);
        if (binding.cityAutoCompleteTextView != null) binding.cityAutoCompleteTextView.setEnabled(enable);
        if (binding.locationEditText != null) binding.locationEditText.setEnabled(enable);
        if (binding.priceEditText != null) binding.priceEditText.setEnabled(enable);
        if (binding.scheduleDayEditText != null) binding.scheduleDayEditText.setEnabled(enable);
        if (binding.scheduleTimeEditText != null) binding.scheduleTimeEditText.setEnabled(enable);
        if (binding.maxParticipantsEditText != null) binding.maxParticipantsEditText.setEnabled(enable);
        if (binding.phoneEditText != null) binding.phoneEditText.setEnabled(enable);
        if (binding.emailEditText != null) binding.emailEditText.setEnabled(enable);
        if (binding.categorySpinner != null) binding.categorySpinner.setEnabled(enable);

        if (enable) {
            if (binding.titleEditText != null) binding.titleEditText.requestFocus();
            if (binding.editButton != null) binding.editButton.setVisibility(View.GONE);
            if (binding.saveButton != null) binding.saveButton.setVisibility(View.VISIBLE);
            if (binding.priceEditText != null) binding.priceEditText.setText(currentPrice);
        } else {
            if (binding.editButton != null) binding.editButton.setVisibility(View.VISIBLE);
            if (binding.saveButton != null) binding.saveButton.setVisibility(View.GONE);
            updatePriceDisplay();
        }
    }

    private void updatePriceDisplay() {
        if (!isEditing && !TextUtils.isEmpty(currentPrice) && binding != null && binding.priceEditText != null) {
            binding.priceEditText.setText(currentPrice + " ₽/month");
        }
    }

    private void saveSection() {
        if (binding == null || currentUser == null || db == null) {
            Toast.makeText(this, "Ошибка инициализации", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = binding.titleEditText != null ? binding.titleEditText.getText().toString().trim() : "";
        String description = binding.descriptionEditText != null ? binding.descriptionEditText.getText().toString().trim() : "";
        String city = binding.cityAutoCompleteTextView != null ? binding.cityAutoCompleteTextView.getText().toString().trim() : "";
        String location = binding.locationEditText != null ? binding.locationEditText.getText().toString().trim() : "";
        String price = binding.priceEditText != null ? binding.priceEditText.getText().toString().trim() : "";
        String daysStr = binding.scheduleDayEditText != null ? binding.scheduleDayEditText.getText().toString().trim() : "";
        String timesStr = binding.scheduleTimeEditText != null ? binding.scheduleTimeEditText.getText().toString().trim() : "";
        String maxParticipantsStr = binding.maxParticipantsEditText != null ? binding.maxParticipantsEditText.getText().toString().trim() : "";
        String phone = binding.phoneEditText != null ? binding.phoneEditText.getText().toString().trim() : "";
        String email = binding.emailEditText != null ? binding.emailEditText.getText().toString().trim() : "";
        String category = "";
        if (binding.categorySpinner != null && binding.categorySpinner.getSelectedItemPosition() > 0) {
            category = binding.categorySpinner.getSelectedItem().toString();
        }
        String coach = binding.nameEditText != null ? binding.nameEditText.getText().toString().trim() : "";

        if (!isEditMode) {
            currentSectionId = db.collection("users").document(currentUser.getUid()).collection("sections").document().getId();
        }

        try {
            if (TextUtils.isEmpty(title)) {
                showError("Введите название раздела", binding.titleEditText);
                return;
            }
            if (TextUtils.isEmpty(description)) {
                showError("Введите описание", binding.descriptionEditText);
                return;
            }
            if (TextUtils.isEmpty(category)) {
                Toast.makeText(this, "Пожалуйста, выберите категорию", Toast.LENGTH_SHORT).show();
                if (binding.categorySpinner != null) {
                    binding.categorySpinner.performClick();
                }
                return;
            }
            if (TextUtils.isEmpty(city)) {
                showError("Выберите город", binding.cityAutoCompleteTextView);
                return;
            }
            if (TextUtils.isEmpty(location)) {
                showError("Введите адрес", binding.locationEditText);
                return;
            }
            if (TextUtils.isEmpty(price)) {
                showError("Введите цену", binding.priceEditText);
                return;
            }
            if (TextUtils.isEmpty(daysStr)) {
                showError("Выберите дни", binding.scheduleDayEditText);
                return;
            }
            if (TextUtils.isEmpty(timesStr)) {
                showError("Выберите время", binding.scheduleTimeEditText);
                return;
            }
            if (TextUtils.isEmpty(maxParticipantsStr)) {
                showError("Введите количество участников", binding.maxParticipantsEditText);
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                showError("Введите телефон", binding.phoneEditText);
                return;
            }
            if (TextUtils.isEmpty(email)) {
                showError("Введите email", binding.emailEditText);
                return;
            }

            int maxParticipants;
            try {
                maxParticipants = Integer.parseInt(maxParticipantsStr);
                if (maxParticipants <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                showError("Некорректное количество участников", binding.maxParticipantsEditText);
                return;
            }

            List<String> days = Arrays.asList(daysStr.split("\\s*,\\s*"));
            List<String> times = Arrays.asList(timesStr.split("\\s*,\\s*"));

            if (days.size() != times.size()) {
                showError("Количество дней и времени должно совпадать", binding.scheduleDayEditText);
                return;
            }
            if (TextUtils.isEmpty(coach)) {
                showError("Введите имя тренера", binding.nameEditText);
                return;
            }

            SectionData sectionData = new SectionData();
            sectionData.setId(currentSectionId);
            sectionData.setTitle(title);
            sectionData.setDescription(description);
            sectionData.setCategory(category);
            sectionData.setCity(city);
            sectionData.setLocation(location);
            sectionData.setPrice(price);
            sectionData.setScheduleDay(days);
            sectionData.setScheduleTime(times);
            sectionData.setMaxParticipants(maxParticipants);
            sectionData.setContactPhone(phone);
            sectionData.setContactEmail(email);
            sectionData.setOwnerId(currentUser.getUid());
            sectionData.setTimestamp(System.currentTimeMillis());
            sectionData.setCoach(coach);
            sectionData.setRootDocumentId("12345678");

            saveSectionToFirebase(sectionData, () -> {
                Toast.makeText(this, "Раздел сохранен", Toast.LENGTH_SHORT).show();
                setResultWithData(sectionData);
                finish();
            });

        } catch (Exception e) {
            Log.e("SaveSection", "Error saving section", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveSectionToFirebase(SectionData sectionData, Runnable onSuccess) {
        if (db == null || sectionData == null) {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
            return;
        }

        // Убедимся, что currentParticipants не превышает maxParticipants
        if (sectionData.getCurrentParticipants() > sectionData.getMaxParticipants()) {
            sectionData.setCurrentParticipants(sectionData.getMaxParticipants());
        }

        // Инициализируем registeredUsers, если null
        if (sectionData.getRegisteredUsers() == null) {
            sectionData.setRegisteredUsers(new ArrayList<>());
        }

        db.collection("users").document(currentUser.getUid())
                .collection("sections")
                .document(sectionData.getId())
                .set(sectionData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Section saved successfully");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving section", e);
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showError(String message, View view) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (view != null) {
            view.requestFocus();
        }
    }

    private void setResultWithData(SectionData sectionData) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("section_data", sectionData);
        setResult(RESULT_OK, resultIntent);
    }

    private void loadSectionData() {
        if (currentUser == null || TextUtils.isEmpty(currentSectionId) || db == null) {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).collection("sections")
                .document(currentSectionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            try {
                                SectionData sectionData = document.toObject(SectionData.class);
                                if (sectionData != null) {
                                    populateFields(sectionData);
                                }
                            } catch (Exception e) {
                                Log.e("Firestore", "Data parsing error", e);
                            }
                        }
                    }
                });
    }

    private void populateFields(SectionData sectionData) {
        if (binding == null || sectionData == null) return;

        try {
            if (binding.titleEditText != null) {
                binding.titleEditText.setText(sectionData.getTitle() != null ? sectionData.getTitle() : "");
            }
            if (binding.descriptionEditText != null) {
                binding.descriptionEditText.setText(sectionData.getDescription() != null ? sectionData.getDescription() : "");
            }
            if (binding.categorySpinner != null && sectionData.getCategory() != null) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.categorySpinner.getAdapter();
                int position = adapter.getPosition(sectionData.getCategory());
                if (position >= 0) {
                    binding.categorySpinner.setSelection(position);
                }
            }
            if (binding.cityAutoCompleteTextView != null) {
                binding.cityAutoCompleteTextView.setText(sectionData.getCity() != null ? sectionData.getCity() : "");
            }
            if (binding.locationEditText != null) {
                binding.locationEditText.setText(sectionData.getLocation() != null ? sectionData.getLocation() : "");
            }

            String price = sectionData.getPrice();
            if (price != null) {
                currentPrice = price.replace(" ₽/month", "").trim();
                if (binding.priceEditText != null) {
                    binding.priceEditText.setText(currentPrice);
                }
            }

            if (binding.scheduleDayEditText != null && sectionData.getScheduleDay() != null) {
                binding.scheduleDayEditText.setText(TextUtils.join(", ", sectionData.getScheduleDay()));
            }
            if (binding.scheduleTimeEditText != null && sectionData.getScheduleTime() != null) {
                binding.scheduleTimeEditText.setText(TextUtils.join(", ", sectionData.getScheduleTime()));
            }

            if (binding.maxParticipantsEditText != null) {
                binding.maxParticipantsEditText.setText(sectionData.getMaxParticipants() > 0 ?
                        String.valueOf(sectionData.getMaxParticipants()) : "");
            }
            if (binding.phoneEditText != null) {
                binding.phoneEditText.setText(sectionData.getContactPhone() != null ? sectionData.getContactPhone() : "");
            }
            if (binding.emailEditText != null) {
                binding.emailEditText.setText(sectionData.getContactEmail() != null ? sectionData.getContactEmail() : "");
            }
            if (binding.nameEditText != null) {
                binding.nameEditText.setText(sectionData.getCoach() != null ? sectionData.getCoach() : "");
            }

            updatePriceDisplay();

        } catch (Exception e) {
            Log.e("PopulateFields", "Error populating fields", e);
            Toast.makeText(this, "Ошибка отображения данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDeleteButton() {
        if (!isEditMode || binding == null || binding.deleteButton == null) return;

        binding.deleteButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Удаление")
                .setMessage("Удалить раздел?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteSection())
                .setNegativeButton("Отмена", null)
                .show());
    }

    private void deleteSection() {
        if (db == null || TextUtils.isEmpty(currentSectionId)) {
            Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUser.getUid()).collection("sections")
                .document(currentSectionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Раздел удален", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteSection", "Error deleting section", e);
                    Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            Log.e("NetworkCheck", "Error checking network", e);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            if (binding.cityAutoCompleteTextView != null) {
                binding.cityAutoCompleteTextView.setOnTouchListener(null);
            }
            if (binding.scheduleDayEditText != null) {
                binding.scheduleDayEditText.setOnTouchListener(null);
            }
            if (binding.scheduleTimeEditText != null) {
                binding.scheduleTimeEditText.setOnTouchListener(null);
            }
            binding = null;
        }
    }
}