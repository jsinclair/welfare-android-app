package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import za.co.aws.welfare.R;
import za.co.aws.welfare.viewModel.HomeViewModel;

public class RemindersFragment extends Fragment {

    private HomeViewModel mModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.reminders, container, false);

    }
}
