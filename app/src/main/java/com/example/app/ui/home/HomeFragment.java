package com.example.app.ui.home;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.app.UtilityClasses.ButtonAdapter;
import com.example.app.dEnter_info;
import com.example.app.databinding.FragmentCreateSectionBinding;
import com.example.app.data.SectionData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentCreateSectionBinding binding;
    private ButtonAdapter adapter;
    private final List<SectionData> sections = new ArrayList<>();
    private ActivityResultLauncher<Intent> addButtonLauncher;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateSectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initializeActivityLauncher();
        setupRecyclerView();
        setupAddButton();
        loadSections();
    }

    private void initializeActivityLauncher() {
        addButtonLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        loadSections();
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new ButtonAdapter(sections, position -> {
            SectionData section = sections.get(position);
            openSectionEditor(section.getId(), section.getTitle());
        });

        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupAddButton() {
        binding.addSection.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), dEnter_info.class);
            addButtonLauncher.launch(intent);
        });
    }

    private void loadSections() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .collection("sections")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        Toast.makeText(requireContext(), "Error loading sections", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sections.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            SectionData section = doc.toObject(SectionData.class);
                            section.setId(doc.getId());
                            sections.add(section);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void openSectionEditor(String sectionId, String sectionTitle) {
        Intent intent = new Intent(requireActivity(), dEnter_info.class);
        intent.putExtra("section_id", sectionId);
        intent.putExtra("section_title", sectionTitle);
        intent.putExtra("is_edit_mode", true);
        addButtonLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}