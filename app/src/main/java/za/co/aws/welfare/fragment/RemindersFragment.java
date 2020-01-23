package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import za.co.aws.welfare.R;
import za.co.aws.welfare.viewModel.HomeViewModel;

// Allow the user to view and add reminders.
public class RemindersFragment extends Fragment {

    private HomeViewModel mModel;

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

        return v;
    }
}
