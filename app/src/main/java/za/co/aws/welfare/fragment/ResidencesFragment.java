package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.ResidenceSearchListAdapter;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.databinding.ResidencesBinding;
import za.co.aws.welfare.viewModel.HomeViewModel;

public class ResidencesFragment extends Fragment {

    private LinearLayout searchView;
    private FloatingActionButton expandButton;
    private Button searchButton;
    private ListView results;

    private HomeViewModel mModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ResidencesBinding binding =  DataBindingUtil.inflate(inflater,
                R.layout.residences, container, false);

        final HomeViewModel mModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setViewModel(mModel);

        View v = binding.getRoot();

        results = v.findViewById(R.id.result_residencess);

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
//                searchView.setVisibility(View.GONE);
//                expandButton.show();
                mModel.doResidenceSearch();
            }
        });

        //TODO: check tht this is the corect way to observe from a fragment (to avoid memory leaks)
        mModel.getResidentResults().observe(getViewLifecycleOwner(), new Observer<LinkedList<ResidenceSearchData>>() {
            @Override
            public void onChanged(LinkedList<ResidenceSearchData> residenceSearchData) {
                //TODO: Handle errors
                //TODO: Handle empty results.
                if (residenceSearchData != null) {
                    Log.i(">>> SUCESS", residenceSearchData.size() + " ");
                    results.setAdapter(new ResidenceSearchListAdapter(getContext(), R.layout.content_residence_search_entry, residenceSearchData));
                }
            }
        });

        return v;
    }
}
