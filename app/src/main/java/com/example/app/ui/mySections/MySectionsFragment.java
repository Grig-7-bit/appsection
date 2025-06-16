package com.example.app.ui.mySections;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.UtilityClasses.ButtonAdapterToSections;
import com.example.app.UtilityClasses.GridSpacingItemDecoration;
import com.example.app.data.SectionData;
import com.example.app.fSection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MySectionsFragment extends Fragment {
    private static final String TAG = "MySectionsFragment";
    private RecyclerView recyclerView;
    private ButtonAdapterToSections adapter;
    private List<SectionData> userSections = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_sections, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Инициализация RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        adapter = new ButtonAdapterToSections(userSections, position -> {
            SectionData selectedSection = userSections.get(position);
            Intent intent = new Intent(getActivity(), fSection.class);
            intent.putExtra("section_id", selectedSection.getId());
            intent.putExtra("owner_id", selectedSection.getOwnerId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserSections();
    }

    private void loadUserSections() {
        if (currentUser == null || getContext() == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка интернета
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Запрос с обработкой ошибок
        try {
            db.collectionGroup("sections")
                    .whereArrayContains("registeredUsers", currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userSections.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    SectionData section = document.toObject(SectionData.class);
                                    section.setId(document.getId());

                                    // Безопасное получение ownerId
                                    if (document.getReference() != null &&
                                            document.getReference().getParent() != null) {
                                        section.setOwnerId(document.getReference().getParent().getParent().getId());
                                    }

                                    userSections.add(section);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + e.getMessage());
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (userSections.isEmpty()) {
                                Toast.makeText(getContext(),
                                        "You haven't joined any sections yet",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String error = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Unknown error";

                            Log.e(TAG, "Firestore error: " + error);

                            // Специальная обработка для FAILED_PRECONDITION
                            if (error.contains("FAILED_PRECONDITION")) {
                                Toast.makeText(getContext(),
                                        "Database index is being created. Try again later.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(),
                                        "Failed to load: " + error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Query error: " + e.getMessage());
        }
    }
}

