package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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

public class aLog_in extends AppCompatActivity {

    private static final String TAG = "AuthLog";
    private Button getInButton;
    private EditText loginEditText;
    private EditText passwordEditText;
    private TextView registrationText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity11_log_in);

        initFirebase();
        checkCurrentUser(); // Проверяем авторизацию при запуске
        initViews();
        setupTextWatchers();
        setupButtonListeners();
        setupWindowInsets();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Пользователь уже авторизован - переходим сразу в приложение
            navigateToMainScreen();
        }
    }

    private void initViews() {
        getInButton = findViewById(R.id.get_in_button);
        loginEditText = findViewById(R.id.login_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        registrationText = findViewById(R.id.registration_text);

        if (getInButton == null || loginEditText == null ||
                passwordEditText == null || registrationText == null) {
            showToast("Ошибка инициализации элементов", Toast.LENGTH_LONG);
            finish();
        }
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
    }

    private void setupButtonListeners() {
        getInButton.setOnClickListener(v -> {
            String email = loginEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInput(email, password)) {
                signIn(email, password);
            }
        });

        registrationText.setOnClickListener(v -> {
            startActivity(new Intent(aLog_in.this, bRegistration.class));
        });
    }

    private boolean validateInput(String email, String password) {
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

        return true;
    }

    private void checkButtonState() {
        boolean isLoginFilled = !TextUtils.isEmpty(loginEditText.getText());
        boolean isPasswordFilled = !TextUtils.isEmpty(passwordEditText.getText());
        getInButton.setEnabled(isLoginFilled && isPasswordFilled);
    }

    private void signIn(String email, String password) {
        getInButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    getInButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        navigateToMainScreen();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        showAuthError(task.getException());
                    }
                });
    }

    private void navigateToMainScreen() {
        startActivity(new Intent(this, cAdd_section.class));
        finish(); // Закрываем текущую активность, чтобы нельзя было вернуться назад
    }

    private void showAuthError(Exception exception) {
        String errorMessage = "Ошибка входа";
        if (exception != null) {
            errorMessage = exception.getLocalizedMessage();
            if (errorMessage.contains("password is invalid")) {
                errorMessage = "Неверный пароль";
            } else if (errorMessage.contains("no user record")) {
                errorMessage = "Пользователь не найден";
            }
        }
        showToast(errorMessage, Toast.LENGTH_LONG);
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
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
}