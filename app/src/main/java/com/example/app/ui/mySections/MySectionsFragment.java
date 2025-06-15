package com.example.app.ui.mySections;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class MySectionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ButtonAdapterToSections adapter;
    private List<SectionData> userSections = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_sections, container, false);



        // Инициализация Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Настройка RecyclerView с GridLayoutManager (2 колонки)
        recyclerView = view.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // Добавляем отступы между элементами
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        adapter = new ButtonAdapterToSections(userSections, position -> {
            // Обработка клика на секцию - переход в активность fSection
            SectionData selectedSection = userSections.get(position);
            Intent intent = new Intent(getActivity(), fSection.class);
            intent.putExtra("section_id", selectedSection.getId());
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
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .collection("sections")
                .whereArrayContains("registeredUsers", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userSections.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SectionData section = document.toObject(SectionData.class);
                            section.setId(document.getId());
                            userSections.add(section);
                        }
                        adapter.notifyDataSetChanged();

                        if (userSections.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "You haven't added any sections yet",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Error loading sections: " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}