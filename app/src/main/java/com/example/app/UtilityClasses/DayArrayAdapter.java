package com.example.app.UtilityClasses;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

// DayArrayAdapter.java
public class DayArrayAdapter extends ArrayAdapter<String> {
    private List<String> daysAll;

    public DayArrayAdapter(Context context, int resource, List<String> days) {
        super(context, resource, new ArrayList<>(days));
        this.daysAll = new ArrayList<>(days);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(daysAll);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (String day : daysAll) {
                        if (day.toLowerCase().contains(filterPattern)) {
                            filteredList.add(day);
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                addAll((List<String>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}