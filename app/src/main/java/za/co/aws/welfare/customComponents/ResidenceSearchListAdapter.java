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
        TextView lat;
        TextView lon;
        TextView dist;
        TextView animals;
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

//    @Override
//    public int getCount() {
//        return displayObjects.size();
//    }
//todo
//    @Override
//    public PendingEvaluationObject getItem(int position) {
//        return displayObjects.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }

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
            holder.lat = convertView.findViewById(R.id.lat);
            holder.lon = convertView.findViewById(R.id.lon);
            holder.animals = convertView.findViewById(R.id.animals);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ref = position;
        holder.streetAddress.setText(i.getStreetAddress());
        holder.shackID.setText(i.getShackID());
        holder.lat.setText(i.getLat());
        holder.lon.setText(i.getLon());
        holder.animals.setText(i.getAnimalNames());
        return convertView;
    }

}
