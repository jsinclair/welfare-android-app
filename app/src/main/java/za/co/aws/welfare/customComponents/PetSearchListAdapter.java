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
import za.co.aws.welfare.dataObjects.PetSearchData;

public class PetSearchListAdapter extends ArrayAdapter<PetSearchData> implements Filterable {

    /** Suggested as an optimisation. Object that holds view components. */
    private class ViewHolder {
        TextView name;
        TextView species;
        TextView dob;
        TextView welfare;
        int ref;
    }

    /** List of original objects.*/
    private LinkedList<PetSearchData> originalObjects;

    /** Constructor.
     *
     * @param context calling context
     * @param textResourceId resource ID
     * @param objects The list of objects to display.
     */
    public PetSearchListAdapter(Context context, int textResourceId, LinkedList<PetSearchData> objects) {
        super(context, textResourceId, objects);
        this.originalObjects = objects;
    }

    @Override
    public int getCount() {
        return originalObjects.size();
    }

    @Override
    public PetSearchData getItem(int position) {
        return originalObjects.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        PetSearchData i = originalObjects.get(position);
        ViewHolder holder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.content_pet_search_entry, null);

            holder.name = convertView.findViewById(R.id.pet_name);
            holder.dob = convertView.findViewById(R.id.pet_dob);
            holder.species = convertView.findViewById(R.id.pet_species);
            holder.welfare = convertView.findViewById(R.id.pet_welfare_no);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ref = position;
        holder.name.setText(i.getPetName());
        holder.dob.setText(i.getPetDOB());
        holder.species.setText(i.getmAnimalTypeDesc());
        holder.welfare.setText(i.getPetWelfareID());
        return convertView;
    }

}