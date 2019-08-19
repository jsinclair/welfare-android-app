package za.co.aws.welfare.customComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import za.co.aws.welfare.R;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;

public class RemoveAnimalAdapter extends ArrayAdapter<ResidentAnimalDetail> {

    /** Suggested as an optimisation. Object that holds view components. */
    private class ViewHolder {
        TextView name;
        ImageView deleteButton;
        int ref;
    }

    private List<ResidentAnimalDetail> displayObjects;

    private View.OnClickListener deleteListener;

    /** Constructor.
     *
     * @param context calling context
     * @param textResourceId resource ID
     * @param objects The list of objects to display.
     */
    public RemoveAnimalAdapter(Context context, int textResourceId, List<ResidentAnimalDetail> objects, View.OnClickListener deleteListener) {
        super(context, textResourceId, objects);
        this.displayObjects = objects;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        ResidentAnimalDetail i = displayObjects.get(position);
        ViewHolder holder;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.remove_animal_content, null);

            holder.name = convertView.findViewById(R.id.pet_name);
            holder.deleteButton = convertView.findViewById(R.id.delete_button);
            holder.deleteButton.setOnClickListener(deleteListener);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ref = position;
        holder.name.setText(i.getName());
        holder.deleteButton.setTag(i);
        return convertView;
    }

}
