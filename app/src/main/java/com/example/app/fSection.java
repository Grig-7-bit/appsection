package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.data.SectionData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class fSection extends AppCompatActivity {

    private SectionData sectionData;
    private TextView maxParticipantsTextView;
    private Button addButton;
    private Button deleteButton;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String sectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fsection);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sectionId = getIntent().getStringExtra("section_id");
        if (sectionId == null) {
            Toast.makeText(this, "Ошибка: ID секции не указан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация кнопок
        addButton = findViewById(R.id.add_button);
        deleteButton = findViewById(R.id.delete_button);
        setupBackButton();

        loadSectionData();
    }

    private void loadSectionData() {
        db.collection("users").document(currentUser.getUid())
                .collection("sections").document(sectionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            sectionData = document.toObject(SectionData.class);
                            if (sectionData != null) {
                                sectionData.setId(document.getId());

                                // Инициализируем список зарегистрированных пользователей, если он null
                                if (sectionData.getRegisteredUsers() == null) {
                                    sectionData.setRegisteredUsers(new ArrayList<>());
                                }

                                initViews();
                                updateButtonVisibility();
                            }
                        } else {
                            Toast.makeText(fSection.this, "Секция не найдена", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(fSection.this, "Ошибка загрузки секции", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void updateButtonVisibility() {
        if (sectionData.getRegisteredUsers().contains(currentUser.getUid())) {
            // Пользователь уже записан - показываем DELETE
            addButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            // Пользователь не записан - показываем ADD
            addButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.GONE);

            // Если секция заполнена, делаем кнопку ADD неактивной
            if (sectionData.getCurrentParticipants() >= sectionData.getMaxParticipants()) {
                addButton.setEnabled(false);
                addButton.setText("ЗАПОЛНЕНО");
            } else {
                addButton.setEnabled(true);
                addButton.setText("ADD");
            }
        }
    }

    private void initViews() {
        // Заполнение всех полей данными из sectionData
        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText(sectionData.getTitle());

        TextView descriptionTextView = findViewById(R.id.description_text_view);
        descriptionTextView.setText(sectionData.getDescription());

        TextView categoryTextView = findViewById(R.id.category_text_view);
        categoryTextView.setText(sectionData.getCategory());

        TextView cityTextView = findViewById(R.id.city_text_view);
        cityTextView.setText(sectionData.getCity());

        TextView locationTextView = findViewById(R.id.location_text_view);
        locationTextView.setText(sectionData.getLocation());

        TextView priceTextView = findViewById(R.id.price_text_view);
        priceTextView.setText(sectionData.getPrice());

        // Обработка расписания
        TextView scheduleDayTextView = findViewById(R.id.schedule_day_text_view);
        TextView scheduleTimeTextView = findViewById(R.id.schedule_time_text_view);
        List<String> days = sectionData.getScheduleDay();
        List<String> times = sectionData.getScheduleTime();

        StringBuilder daysBuilder = new StringBuilder();
        StringBuilder timesBuilder = new StringBuilder();

        for (int i = 0; i < days.size(); i++) {
            daysBuilder.append(days.get(i));
            timesBuilder.append(times.get(i));

            if (i < days.size() - 1) {
                daysBuilder.append("\n");
                timesBuilder.append("\n");
            }
        }

        scheduleDayTextView.setText(daysBuilder.toString());
        scheduleTimeTextView.setText(timesBuilder.toString());

        // Участники в формате "текущие/максимальные"
        maxParticipantsTextView = findViewById(R.id.maxParticipants_text_view);
        updateParticipantsText();

        TextView coachTextView = findViewById(R.id.coach_text_view);
        coachTextView.setText(sectionData.getCoach());

        TextView phoneTextView = findViewById(R.id.phone_text_view);
        phoneTextView.setText(sectionData.getContactPhone());

        TextView emailTextView = findViewById(R.id.email_text_view);
        emailTextView.setText(sectionData.getContactEmail());

        // Настройка обработчиков кнопок
        addButton.setOnClickListener(v -> registerForSection());
        deleteButton.setOnClickListener(v -> unregisterFromSection());
    }

    private void registerForSection() {
        if (sectionData.getCurrentParticipants() >= sectionData.getMaxParticipants()) {
            Toast.makeText(this, "Секция уже заполнена", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем новый список на основе текущего
        List<String> updatedRegisteredUsers = new ArrayList<>(
                sectionData.getRegisteredUsers() != null ?
                        sectionData.getRegisteredUsers() :
                        new ArrayList<>()
        );

        // Проверяем, не записан ли уже пользователь
        if (updatedRegisteredUsers.contains(currentUser.getUid())) {
            Toast.makeText(this, "Вы уже записаны на эту секцию", Toast.LENGTH_SHORT).show();
            return;
        }

        updatedRegisteredUsers.add(currentUser.getUid());

        // Обновляем данные в Firestore
        db.collection("users").document(currentUser.getUid())
                .collection("sections").document(sectionId)
                .update(
                        "currentParticipants", sectionData.getCurrentParticipants() + 1,
                        "registeredUsers", updatedRegisteredUsers
                )
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(fSection.this, "Вы успешно записаны", Toast.LENGTH_SHORT).show();

                        // Обновляем локальные данные через новый метод
                        updateLocalSectionData(
                                sectionData.getCurrentParticipants() + 1,
                                updatedRegisteredUsers
                        );

                        updateParticipantsText();
                        updateButtonVisibility();

                        // Возвращаем обновленные данные
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updated_section_id", sectionId);
                        resultIntent.putExtra("new_participants_count", sectionData.getCurrentParticipants());
                        setResult(RESULT_OK, resultIntent);
                    } else {
                        Toast.makeText(fSection.this, "Ошибка записи: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unregisterFromSection() {
        // Создаем новый список на основе текущего
        List<String> updatedRegisteredUsers = new ArrayList<>(
                sectionData.getRegisteredUsers() != null ?
                        sectionData.getRegisteredUsers() :
                        new ArrayList<>()
        );

        if (!updatedRegisteredUsers.contains(currentUser.getUid())) {
            Toast.makeText(this, "Вы не записаны на эту секцию", Toast.LENGTH_SHORT).show();
            return;
        }

        updatedRegisteredUsers.remove(currentUser.getUid());

        // Обновляем данные в Firestore
        db.collection("users").document(currentUser.getUid())
                .collection("sections").document(sectionId)
                .update(
                        "currentParticipants", sectionData.getCurrentParticipants() - 1,
                        "registeredUsers", updatedRegisteredUsers
                )
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(fSection.this, "Вы отменили запись", Toast.LENGTH_SHORT).show();

                        // Обновляем локальные данные через новый метод
                        updateLocalSectionData(
                                sectionData.getCurrentParticipants() - 1,
                                updatedRegisteredUsers
                        );

                        updateParticipantsText();
                        updateButtonVisibility();

                        // Возвращаем обновленные данные
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updated_section_id", sectionId);
                        resultIntent.putExtra("new_participants_count", sectionData.getCurrentParticipants());
                        setResult(RESULT_OK, resultIntent);
                    } else {
                        Toast.makeText(fSection.this, "Ошибка отмены записи: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Новый метод для обновления локальных данных
    private void updateLocalSectionData(int newParticipantsCount, List<String> newRegisteredUsers) {
        sectionData.setCurrentParticipants(newParticipantsCount);
        sectionData.setRegisteredUsers(newRegisteredUsers);
    }

    private void updateParticipantsText() {
        String participantsText = sectionData.getCurrentParticipants() + "/" + sectionData.getMaxParticipants();
        maxParticipantsTextView.setText(participantsText);
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }
}