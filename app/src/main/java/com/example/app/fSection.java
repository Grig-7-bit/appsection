package com.example.app;

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
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity4_section);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sectionId = getIntent().getStringExtra("section_id");
        ownerId = getIntent().getStringExtra("owner_id"); // Получаем ID владельца

        if (sectionId == null || ownerId == null) {
            Toast.makeText(this, "Ошибка: недостаточно данных о секции", Toast.LENGTH_SHORT).show();
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
        // Загружаем из коллекции владельца, а не текущего пользователя
        db.collection("users").document(ownerId)
                .collection("sections").document(sectionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            sectionData = document.toObject(SectionData.class);
                            if (sectionData != null) {
                                sectionData.setId(document.getId());
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

    private void registerForSection() {
        if (sectionData.getCurrentParticipants() >= sectionData.getMaxParticipants()) {
            Toast.makeText(this, "Секция уже заполнена", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> updatedRegisteredUsers = new ArrayList<>(
                sectionData.getRegisteredUsers() != null ?
                        sectionData.getRegisteredUsers() :
                        new ArrayList<>()
        );

        if (updatedRegisteredUsers.contains(currentUser.getUid())) {
            Toast.makeText(this, "Вы уже записаны на эту секцию", Toast.LENGTH_SHORT).show();
            return;
        }

        updatedRegisteredUsers.add(currentUser.getUid());

        // Обновляем данные в коллекции владельца
        db.collection("users").document(ownerId)
                .collection("sections").document(sectionId)
                .update(
                        "currentParticipants", sectionData.getCurrentParticipants() + 1,
                        "registeredUsers", updatedRegisteredUsers
                )
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(fSection.this, "Вы успешно записаны", Toast.LENGTH_SHORT).show();
                        updateLocalSectionData(
                                sectionData.getCurrentParticipants() + 1,
                                updatedRegisteredUsers
                        );
                        updateParticipantsText();
                        updateButtonVisibility();
                        setResult(RESULT_OK);
                    } else {
                        Toast.makeText(fSection.this, "Ошибка записи: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unregisterFromSection() {
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

        // Обновляем данные в коллекции владельца
        db.collection("users").document(ownerId)
                .collection("sections").document(sectionId)
                .update(
                        "currentParticipants", sectionData.getCurrentParticipants() - 1,
                        "registeredUsers", updatedRegisteredUsers
                )
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(fSection.this, "Вы отменили запись", Toast.LENGTH_SHORT).show();
                        updateLocalSectionData(
                                sectionData.getCurrentParticipants() - 1,
                                updatedRegisteredUsers
                        );
                        updateParticipantsText();
                        updateButtonVisibility();
                        setResult(RESULT_OK);
                    } else {
                        Toast.makeText(fSection.this, "Ошибка отмены записи: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
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
                daysBuilder.append(", ");  // Запятая + пробел вместо переноса строки
                timesBuilder.append(", ");
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