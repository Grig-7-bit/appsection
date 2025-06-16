package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class bRegistration extends AppCompatActivity {

    private static final String TAG = "Registration";
    private Button getInButton;
    private EditText loginEditText;
    private EditText passwordEditText;
    private EditText passwordVerificationEditText;
    private TextView logInText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity12_registration);

        initFirebase();
        initViews();
        setupTextWatchers();
        setupButtonListeners();
        setupWindowInsets();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        getInButton = findViewById(R.id.get_in_button);
        loginEditText = findViewById(R.id.login_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        passwordVerificationEditText = findViewById(R.id.password_verification_edit_text);
        logInText = findViewById(R.id.log_in_text);

        if (getInButton == null || loginEditText == null ||
                passwordEditText == null || logInText == null ||
                passwordVerificationEditText == null) {
            showToast("Ошибка инициализации элементов", Toast.LENGTH_LONG);
            finish();
            return; // Добавлен return для немедленного выхода
        }

        getInButton.setEnabled(false);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        loginEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        passwordVerificationEditText.addTextChangedListener(textWatcher);
    }

    private void setupButtonListeners() {
        getInButton.setOnClickListener(v -> {
            String email = loginEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String passwordConfirm = passwordVerificationEditText.getText().toString().trim();

            if (!validateInput(email, password, passwordConfirm)) {
                return;
            }

            registerUser(email, password);
        });

        logInText.setOnClickListener(v -> {
            // Изменено с finish() на переход к экрану входа
            startActivity(new Intent(this, aLog_in.class));
            finish();
        });
    }

    private boolean validateInput(String email, String password, String passwordConfirm) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEditText.setError("Введите корректный email");
            loginEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Пароль должен содержать минимум 6 символов");
            passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            passwordVerificationEditText.setError("Пароли не совпадают");
            passwordVerificationEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String email, String password) {
        getInButton.setEnabled(false);
        showToast("Регистрация...", Toast.LENGTH_SHORT);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, email);
                        }
                    } else {
                        handleRegistrationError(task.getException());
                        getInButton.setEnabled(true);
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(user.getUid()).collection("profile").document("userData")
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore");
                    // Немедленный переход без ожидания верификации email
                    navigateToMainScreen();

                    // Отправка письма верификации (если нужно)
                   // sendEmailVerificationInBackground(user);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving user data", e);
                    showToast("Ошибка сохранения данных", Toast.LENGTH_SHORT);
                    navigateToMainScreen(); // Все равно переходим, данные можно сохранить позже
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent.");
                        showToast("Письмо с подтверждением отправлено", Toast.LENGTH_SHORT);
                    } else {
                        Log.w(TAG, "Failed to send verification email", task.getException());
                    }
                    navigateToMainScreen();
                });
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage = "Ошибка регистрации";
        if (exception != null) {
            errorMessage = exception.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("email address is already in use")) {
                    errorMessage = "Этот email уже зарегистрирован";
                } else if (errorMessage.contains("A network error")) {
                    errorMessage = "Ошибка сети. Проверьте подключение";
                } else if (errorMessage.contains("WEAK_PASSWORD")) {
                    errorMessage = "Пароль слишком слабый";
                }
            }
        }
        showToast(errorMessage, Toast.LENGTH_LONG);
    }

    private void navigateToMainScreen() {
        startActivity(new Intent(this, cAdd_section.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void setupWindowInsets() {
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void checkButtonState() {
        boolean allFieldsFilled = !loginEditText.getText().toString().isEmpty() &&
                !passwordEditText.getText().toString().isEmpty() &&
                !passwordVerificationEditText.getText().toString().isEmpty();
        getInButton.setEnabled(allFieldsFilled);
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }
}