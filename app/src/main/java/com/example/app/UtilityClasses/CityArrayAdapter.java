package com.example.app.UtilityClasses;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;

public class CityArrayAdapter extends ArrayAdapter<String> {
    private List<String> citiesAll; // Полный список городов

    public CityArrayAdapter(Context context, int resource, List<String> cities) {
        super(context, resource, new ArrayList<>(cities)); // Создаем изменяемую копию
        this.citiesAll = new ArrayList<>(cities); // Сохраняем копию полного списка
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(citiesAll);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (String city : citiesAll) {
                        if (city.toLowerCase().startsWith(filterPattern)) {
                            filteredList.add(0, city);
                        } else if (city.toLowerCase().contains(filterPattern)) {
                            filteredList.add(city);
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    clear();
                    addAll((List<String>) results.values);
                    notifyDataSetChanged();
                }
            }
        };
    }
}