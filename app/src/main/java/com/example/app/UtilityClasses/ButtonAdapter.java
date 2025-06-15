package com.example.app.UtilityClasses;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;

import java.util.List;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder> {

    private final List<String> buttonTitles;
    private final OnButtonClickListener listener;

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    public ButtonAdapter(List<String> buttonTitles, OnButtonClickListener listener) {
        this.buttonTitles = buttonTitles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(parent.getContext());
        constraintLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        constraintLayout.setPadding(
                dpToPx(parent, 16), // left
                dpToPx(parent, 8),  // top
                dpToPx(parent, 16), // right
                dpToPx(parent, 8)   // bottom
        );

        // 2. Создаем кнопку (фон)
        View cardBackground = new View(parent.getContext());
        cardBackground.setId(View.generateViewId());
        cardBackground.setBackgroundResource(R.drawable.rectangle);

        ConstraintLayout.LayoutParams bgParams = new ConstraintLayout.LayoutParams(
                0, // width будет установлен через constraints
                dpToPx(parent, 139)
        );
        bgParams.startToStart = ConstraintSet.PARENT_ID;
        bgParams.endToEnd = ConstraintSet.PARENT_ID;
        bgParams.topToTop = ConstraintSet.PARENT_ID;
        cardBackground.setLayoutParams(bgParams);
        constraintLayout.addView(cardBackground);

        // 3. Синяя полоса сверху
        View blueStrip = new View(parent.getContext());
        blueStrip.setId(View.generateViewId());
        blueStrip.setBackgroundColor(Color.parseColor("#4FC3F7"));

        ConstraintLayout.LayoutParams stripParams = new ConstraintLayout.LayoutParams(
                0, // width
                dpToPx(parent, 28)
        );
        stripParams.startToStart = cardBackground.getId();
        stripParams.endToEnd = cardBackground.getId();
        stripParams.topToTop = cardBackground.getId();
        blueStrip.setLayoutParams(stripParams);
        constraintLayout.addView(blueStrip);

        // 4. Текст "Name"
        TextView nameText = new TextView(parent.getContext());
        nameText.setId(R.id.text_name);
        nameText.setText("Name");
        nameText.setTextColor(Color.parseColor("#0D47A1"));
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        ConstraintLayout.LayoutParams nameParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.startToStart = blueStrip.getId();
        nameParams.topToTop = blueStrip.getId();
        nameParams.bottomToBottom = blueStrip.getId();
        nameParams.leftMargin = dpToPx(parent, 8);
        nameText.setLayoutParams(nameParams);
        constraintLayout.addView(nameText);

        // 5. Текст "Price"
        TextView priceText = new TextView(parent.getContext());
        priceText.setId(R.id.text_price);
        priceText.setText("Price:");
        priceText.setTextColor(Color.parseColor("#4FC3F7"));
        priceText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        ConstraintLayout.LayoutParams priceParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        priceParams.startToStart = cardBackground.getId();
        priceParams.topToBottom = blueStrip.getId();
        priceParams.leftMargin = dpToPx(parent, 8);
        priceParams.topMargin = dpToPx(parent, 8);
        priceText.setLayoutParams(priceParams);
        constraintLayout.addView(priceText);

        return new ButtonViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        TextView nameTextView = holder.itemView.findViewById(R.id.text_name);
        TextView priceTextView = holder.itemView.findViewById(R.id.text_price);

        if (nameTextView != null) {
            nameTextView.setText(buttonTitles.get(position));
        }

        if (priceTextView != null) {
            priceTextView.setText("ID: " + (position + 1)); // Или другая логика отображения
        }

        holder.itemView.setOnClickListener(v -> listener.onButtonClick(position));
    }

    @Override
    public int getItemCount() {
        return buttonTitles.size();
    }

    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        ButtonViewHolder(View itemView) {
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