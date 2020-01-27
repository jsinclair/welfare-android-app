package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.RemindersAdapter;
import za.co.aws.welfare.dataObjects.ReminderData;
import za.co.aws.welfare.viewModel.HomeViewModel;

// Allow the user to view and add reminders.
public class RemindersFragment extends Fragment {

    private HomeViewModel mModel;
    private ListView results;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        final View v = inflater.inflate(R.layout.reminders, container, false);

        v.findViewById(R.id.add_reminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModel.triggerAddReminder();
            }
        });

        results = v.findViewById(R.id.reminders_list);

        mModel.getReminders().observe(getViewLifecycleOwner(), new Observer<LinkedList<ReminderData>>() {
            @Override
            public void onChanged(LinkedList<ReminderData> reminderData) {
                LinearLayout emptyView = v.findViewById(R.id.empty_view);
                if (reminderData != null && !reminderData.isEmpty()) {
                    emptyView.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
                    results.setAdapter(new RemindersAdapter(getContext(), R.layout.content_reminder_entry, reminderData));
                    results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mModel.triggerViewReminder(((ReminderData) results.getAdapter().getItem(i)).getID());
                        }
                    });
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    results.setVisibility(View.GONE);
                }
            }
        });

        if (savedInstanceState == null) {
            mModel.reloadReminders();
        }

        return v;
    }
}
