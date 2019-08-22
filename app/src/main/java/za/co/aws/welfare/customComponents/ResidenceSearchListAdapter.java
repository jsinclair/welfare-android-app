package za.co.aws.welfare.customComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;

public class ResidenceSearchListAdapter extends ArrayAdapter<ResidenceSearchData> implements Filterable {

    /** Suggested as an optimisation. Object that holds view components. */
    private class ViewHolder {
        TextView streetAddress;
        TextView shackID;
        TextView residentName;
        TextView residentID;
        TextView residentTel;
        TextView animals;
        TextView sterilisedStat;
        int ref;
    }

    /** List of original objects.*/
    private LinkedList<ResidenceSearchData> originalObjects;

    /** Constructor.
     *
     * @param context calling context
     * @param textResourceId resource ID
     * @param objects The list of objects to display.
     */
    public ResidenceSearchListAdapter(Context context, int textResourceId, LinkedList<ResidenceSearchData> objects) {
        super(context, textResourceId, objects);
        this.originalObjects = objects;
    }

    @Override
    public int getCount() {
        return originalObjects.size();
    }

    @Override
    public ResidenceSearchData getItem(int position) {
        return originalObjects.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        ResidenceSearchData i = originalObjects.get(position);
        ViewHolder holder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.content_residence_search_entry, null);

            holder.streetAddress = convertView.findViewById(R.id.street_address);
            holder.shackID = convertView.findViewById(R.id.shack_id);
            holder.residentName = convertView.findViewById(R.id.resident_name);
            holder.residentID = convertView.findViewById(R.id.resident_id);
            holder.residentTel = convertView.findViewById(R.id.resident_tel);
            holder.animals = convertView.findViewById(R.id.animals);
            holder.sterilisedStat = convertView.findViewById(R.id.all_steri);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ref = position;
        holder.streetAddress.setText(i.getStreetAddress());
        holder.shackID.setText(i.getShackID());
        holder.residentName.setText(i.getResidentName());
        holder.residentID.setText(i.getResidentID());
        holder.residentTel.setText(i.getResidentTel());
        holder.animals.setText(i.getAnimalNames());
        String steriAll = i.getAllAnimalsSterilised();
        if ("Yes".equalsIgnoreCase(steriAll)) {
            holder.sterilisedStat.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
        } else if ("No".equalsIgnoreCase(steriAll)) {
            holder.sterilisedStat.setTextColor(getContext().getResources().getColor(R.color.red));
        } else {
            holder.sterilisedStat.setTextColor(getContext().getResources().getColor(R.color.orange));
        }
        holder.sterilisedStat.setText(steriAll);
        return convertView;
    }

}
