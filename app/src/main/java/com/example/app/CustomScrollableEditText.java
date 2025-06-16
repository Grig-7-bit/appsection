package com.example.app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomScrollableEditText extends androidx.appcompat.widget.AppCompatEditText {

    public CustomScrollableEditText(Context context) {
        super(context);
    }

    public CustomScrollableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        // Если EditText отключен, всё равно позволяем прокрутку
        if (!isEnabled()) {
            return true;
        }
        return super.performClick();
    }
}