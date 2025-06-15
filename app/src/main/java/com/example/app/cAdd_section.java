package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.app.UtilityClasses.FirebaseConstants;
import com.example.app.databinding.ActivityMain2Binding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class cAdd_section  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private static final String TAG = "AddSectionActivity";
    private static final int BACK_PRESS_INTERVAL = 2000;
    private static final String PROFILE_DOCUMENT = "userData";
    private static final String NAME_FIELD = "name";

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMain2Binding binding;
    private NavController navController;
    private long backPressedTime;
    private Toast backToast;
    private TextView emailText;
    private TextView nameSurnameText;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userDatabaseRef;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();

        if (!checkUserAuthentication()) {
            return;
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

// После установки меню
        Menu menu = navigationView.getMenu();
        MenuItem outItem = menu.findItem(R.id.nav_out);

// Создаем SpannableString с красным цветом
        SpannableString spannableString = new SpannableString(outItem.getTitle());
        spannableString.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.red)),
                0,
                spannableString.length(),
                0
        );
        outItem.setTitle(spannableString);

        initializeUI();
        setupNavigation();
        loadUserData();
    }

    private void initializeFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
                userId = currentUser.getUid();
                db = FirebaseFirestore.getInstance();
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            showToast("Firebase initialization failed");
        }
    }

    private boolean checkUserAuthentication() {
        if (currentUser == null) {
            showToast(getString(R.string.user_not_authenticated));
            finish();
            return false;
        }
        return true;
    }

    private void initializeUI() {
        try {
            setSupportActionBar(binding.appBarMain2.toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.e(TAG, "UI initialization error", e);
        }
    }

    private void setupNavigation() {
        try {
            DrawerLayout drawer = binding.drawerLayout;
            NavigationView navigationView = binding.navView;

            View headerView = navigationView.getHeaderView(0);
            emailText = headerView.findViewById(R.id.email_text);
            nameSurnameText = headerView.findViewById(R.id.name_surname_text);

            if (emailText != null && currentUser != null) {
                emailText.setText(currentUser.getEmail() != null ?
                        currentUser.getEmail() : getString(R.string.no_email));
            }

            // Установка временного имени
            if (nameSurnameText != null) {
                nameSurnameText.setText(getString(R.string.default_user_name));
            }

            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home,
                    R.id.nav_gallery,
                    R.id.nav_slideshow)
                    .setOpenableLayout(drawer)
                    .build();

            navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main2);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            navigationView.setNavigationItemSelectedListener(item ->
                    NavigationUI.onNavDestinationSelected(item, navController) ||
                            super.onOptionsItemSelected(item));
        } catch (Exception e) {
            Log.e(TAG, "Navigation setup error", e);
        }
    }

    private void loadUserData() {
        if (db == null || userId == null || currentUser == null) {
            Log.w(TAG, "Firestore, userId or currentUser not initialized");
            return;
        }

        db.collection(FirebaseConstants.COLLECTION_USERS)
                .document(userId)
                .collection("profile")
                .document(PROFILE_DOCUMENT)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore error", e);
                        showToast(getString(R.string.error_loading_user_data));
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString(NAME_FIELD);
                        if (name != null && !name.isEmpty()) {
                            updateUserNameUI(name);
                        } else {
                            Log.d(TAG, "Name field is empty in profile");

                        }
                    } else {
                        Log.d(TAG, "Profile document doesn't exist");

                        // Создаем документ профиля если его нет
                        createInitialProfileDocument();
                    }
                });
    }

    private void createInitialProfileDocument() {
        Map<String, Object> profileData = new HashMap<>();
        String defaultName = currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() :
                (currentUser.getEmail() != null ?
                        currentUser.getEmail().split("@")[0] :
                        getString(R.string.default_user_name));

        profileData.put(NAME_FIELD, defaultName);

        db.collection(FirebaseConstants.COLLECTION_USERS)
                .document(userId)
                .collection("profile")
                .document(PROFILE_DOCUMENT)
                .set(profileData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile document created"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating profile document", e));
    }

    private void updateUserNameUI(String name) {
        runOnUiThread(() -> {
            if (nameSurnameText != null) {
                nameSurnameText.setText(name);
            } else {
                Log.w(TAG, "nameSurnameText is null");
            }
        });
    }



    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(cAdd_section.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) ||
                super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) ||
                super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Обработка выбора пунктов меню
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            // Переход в профиль
        } else if (id == R.id.nav_my_sections) {
            // Переход к добавленным секциям
        } else if (id == R.id.nav_home) {
            // Создание секции
        } else if (id == R.id.nav_slideshow) {
            // Просмотр секций
        } else if (id == R.id.nav_out) {
            // Выход из аккаунта
            signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, aLog_in.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView);
            return;
        }

        if (navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == R.id.nav_home) {
            handleDoubleBackPressToExit();
        } else {
            super.onBackPressed();
        }
    }

    private void handleDoubleBackPressToExit() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            if (backToast != null) {
                backToast.cancel();
            }
            finishAffinity();
        } else {
            backToast = Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT);
            backToast.show();
            backPressedTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backToast != null) {
            backToast.cancel();
        }
    }
}