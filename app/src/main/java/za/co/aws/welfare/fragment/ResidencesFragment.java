package za.co.aws.welfare.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityLoginBinding;
import za.co.aws.welfare.databinding.ResidencesBinding;
import za.co.aws.welfare.viewModel.HomeViewModel;
import za.co.aws.welfare.viewModel.LoginViewModel;

public class ResidencesFragment extends Fragment {

    private LinearLayout searchView;
    private FloatingActionButton expandButton;
    private Button searchButton;

    private HomeViewModel mModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ResidencesBinding binding =  DataBindingUtil.inflate(inflater,
                R.layout.residences, container, false);

        HomeViewModel mModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setViewModel(mModel);

        View v = binding.getRoot();

        searchView = v.findViewById(R.id.search_menu);
        searchButton = v.findViewById(R.id.search_button);
        expandButton = v.findViewById(R.id.expand_button);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    searchView.setVisibility(View.VISIBLE);
                    expandButton.hide();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: START SEARCH HERE> ONLY Hide search menu on successful search!!
                searchView.setVisibility(View.GONE);
                expandButton.show();
            }
        });

        mModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);

//        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
//        mModel = ViewModelProviders.of(this).get(LoginViewModel.class);
//        binding.setViewModel(mModel);
//        binding.setLifecycleOwner(this);

        return v;
    }
}
