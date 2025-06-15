package com.example.app.UtilityClasses;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.data.SectionData;

import java.util.List;

public class ButtonAdapterToSections extends RecyclerView.Adapter<ButtonAdapterToSections.SectionViewHolder> {

    private final List<SectionData> sections;
    private final OnSectionClickListener listener;

    public interface OnSectionClickListener {
        void onSectionClick(int position);
    }

    public ButtonAdapterToSections(List<SectionData> sections, OnSectionClickListener listener) {
        this.sections = sections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(parent.getContext());
        constraintLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int padding = dpToPx(parent, 8);
        constraintLayout.setPadding(padding, padding, padding, padding);

        // 1. Создаем фон карточки
        View cardBackground = new View(parent.getContext());
        cardBackground.setId(View.generateViewId());
        cardBackground.setBackgroundResource(R.drawable.rectangle);

        ConstraintLayout.LayoutParams bgParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                dpToPx(parent, 120)
        );
        bgParams.startToStart = ConstraintSet.PARENT_ID;
        bgParams.endToEnd = ConstraintSet.PARENT_ID;
        bgParams.topToTop = ConstraintSet.PARENT_ID;
        cardBackground.setLayoutParams(bgParams);
        constraintLayout.addView(cardBackground);

        // 2. Синяя полоса сверху
        View blueStrip = new View(parent.getContext());
        blueStrip.setId(View.generateViewId());
        blueStrip.setBackgroundColor(Color.parseColor("#4FC3F7"));

        ConstraintLayout.LayoutParams stripParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                dpToPx(parent, 32)
        );
        stripParams.startToStart = cardBackground.getId();
        stripParams.endToEnd = cardBackground.getId();
        stripParams.topToTop = cardBackground.getId();
        blueStrip.setLayoutParams(stripParams);
        constraintLayout.addView(blueStrip);

        // 3. Текст названия секции
        TextView nameText = new TextView(parent.getContext());
        nameText.setId(R.id.text_name);
        nameText.setTextColor(Color.parseColor("#0D47A1"));
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        nameText.setMaxLines(1);
        nameText.setEllipsize(TextUtils.TruncateAt.END);

        ConstraintLayout.LayoutParams nameParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.startToStart = blueStrip.getId();
        nameParams.endToEnd = blueStrip.getId();
        nameParams.topToTop = blueStrip.getId();
        nameParams.bottomToBottom = blueStrip.getId();
        nameParams.leftMargin = dpToPx(parent, 8);
        nameParams.rightMargin = dpToPx(parent, 8);
        nameText.setLayoutParams(nameParams);
        constraintLayout.addView(nameText);

        // 4. Текст цены
        TextView priceText = new TextView(parent.getContext());
        priceText.setId(R.id.text_price);
        priceText.setTextColor(Color.parseColor("#4FC3F7"));
        priceText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        priceText.setMaxLines(1);

        ConstraintLayout.LayoutParams priceParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.startToStart = cardBackground.getId();
        priceParams.topToBottom = blueStrip.getId();
        priceParams.leftMargin = dpToPx(parent, 8);
        priceParams.topMargin = dpToPx(parent, 8);
        priceText.setLayoutParams(priceParams);
        constraintLayout.addView(priceText);

        return new SectionViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        SectionData section = sections.get(position);

        TextView nameTextView = holder.itemView.findViewById(R.id.text_name);
        TextView priceTextView = holder.itemView.findViewById(R.id.text_price);

        nameTextView.setText(section.getTitle() != null ? section.getTitle() : "No title");
        priceTextView.setText(section.getPrice() != null ? section.getPrice() : "No price");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSectionClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        SectionViewHolder(View itemView) {
            super(itemView);
        }
    }

    private int dpToPx(ViewGroup parent, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                parent.getResources().getDisplayMetrics()
        );
    }
}