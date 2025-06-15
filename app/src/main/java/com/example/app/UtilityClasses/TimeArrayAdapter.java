package com.example.app.UtilityClasses;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class TimeArrayAdapter extends ArrayAdapter<String> {
    private final List<String> originalTimes;
    private List<String> filteredTimes;

    public TimeArrayAdapter(Context context, int resource, List<String> times) {
        super(context, resource, new ArrayList<>(times));
        this.originalTimes = new ArrayList<>(times);
        this.filteredTimes = new ArrayList<>(times);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(originalTimes);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (String time : originalTimes) {
                        if (time.toLowerCase().contains(filterPattern)) {
                            filteredList.add(time);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredTimes.clear();
                if (results.values != null) {
                    filteredTimes.addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return filteredTimes.size();
    }

    @Override
    public String getItem(int position) {
        return filteredTimes.get(position);
    }
}