package com.example.app.ui.sections;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.app.R;
import com.example.app.UtilityClasses.ButtonAdapterToSections;
import com.example.app.data.SectionData;
import com.example.app.databinding.FragmentSectionBinding;
import com.example.app.fSection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Sections extends Fragment {

    private static final String TAG = "SectionsFragment";
    private FragmentSectionBinding binding;
    private ButtonAdapterToSections adapter;
    private final List<SectionData> sections = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration listenerRegistration;
    private ActivityResultLauncher<Intent> sectionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        setupActivityLauncher();
        setupRecyclerView();
        loadSectionsData();
    }

    private void setupActivityLauncher() {
        sectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String updatedSectionId = data.getStringExtra("updated_section_id");
                        int newParticipantsCount = data.getIntExtra("new_participants_count", 0);
                        updateSectionParticipants(updatedSectionId, newParticipantsCount);
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new ButtonAdapterToSections(
                sections,
                position -> {
                    if (position >= 0 && position < sections.size()) {
                        SectionData section = sections.get(position);
                        openSectionDetails(section);
                    }
                }
        );

        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
    }

    private void loadSectionsData() {
        if (currentUser == null) {
            showError("Authentication required");
            return;
        }

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }


        binding.errorText.setVisibility(View.GONE);

        // Используем collectionGroup для поиска во всех коллекциях sections
        listenerRegistration = db.collectionGroup("sections")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        showError(getString(R.string.loading_error));
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    processFirestoreData(value);
                });
    }

    private void processFirestoreData(QuerySnapshot value) {
        List<SectionData> newSections = new ArrayList<>();
        for (QueryDocumentSnapshot doc : value) {
            try {
                SectionData section = doc.toObject(SectionData.class);
                section.setId(doc.getId());
                // Сохраняем ID владельца секции
                section.setOwnerId(doc.getReference().getParent().getParent().getId());
                newSections.add(section);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing document: " + doc.getId(), e);
            }
        }

        sections.clear();
        sections.addAll(newSections);
        adapter.notifyDataSetChanged();

        if (sections.isEmpty()) {
            showEmptyState();
        }
    }

    private void openSectionDetails(SectionData section) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), fSection.class);
        intent.putExtra("section_id", section.getId());
        // Передаем ID владельца секции
        intent.putExtra("owner_id", section.getOwnerId());
        sectionLauncher.launch(intent);

    }


    private void updateSectionParticipants(String sectionId, int newCount) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId().equals(sectionId)) {
                sections.get(i).setCurrentParticipants(newCount);
                adapter.notifyItemChanged(i);

                // Прокручиваем к обновленной секции
                binding.recyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }



    private void showEmptyState() {

        binding.errorText.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.errorText.setText(message);
        binding.errorText.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        binding = null;
    }
}