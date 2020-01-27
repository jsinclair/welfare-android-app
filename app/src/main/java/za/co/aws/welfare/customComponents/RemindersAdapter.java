package za.co.aws.welfare.customComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.dataObjects.ReminderData;

public class RemindersAdapter extends ArrayAdapter<ReminderData> {

    /** Suggested as an optimisation. Object that holds view components. */
    private class ViewHolder {
        TextView date;
        TextView pets;
        int ref;
    }

    /** List of original objects.*/
    private LinkedList<ReminderData> originalObjects;

    /** Constructor.
     *
     * @param context calling context
     * @param textResourceId resource ID
     * @param objects The list of objects to display.
     */
    public RemindersAdapter(Context context, int textResourceId, LinkedList<ReminderData> objects) {
        super(context, textResourceId, objects);
        this.originalObjects = objects;
    }

    @Override
    public int getCount() {
        return originalObjects.size();
    }

    @Override
    public ReminderData getItem(int position) {
        return originalObjects.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        ReminderData i = originalObjects.get(position);
        ViewHolder holder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.content_reminder_entry, null);
            holder.date = convertView.findViewById(R.id.date);
            holder.pets = convertView.findViewById(R.id.pets);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ref = position;
        holder.date.setText(i.getDate());
        holder.pets.setText(i.getAnimalNames());
        return convertView;
    }
}
