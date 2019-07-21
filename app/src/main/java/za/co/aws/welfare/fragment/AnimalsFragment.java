package za.co.aws.welfare.fragment;

import android.os.Bundle;
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
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.databinding.AnimalsBinding;
import za.co.aws.welfare.viewModel.HomeViewModel;

public class AnimalsFragment extends Fragment {

    //TODO: Populate selection list
    //TODO: Do actual seach
    //TODO: populate list of pets.

    private LinearLayout searchView;
    private FloatingActionButton expandButton;
    private Button searchButton;
    private ListView results;

    private HomeViewModel mModel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AnimalsBinding binding =  DataBindingUtil.inflate(inflater, R.layout.animals, container, false);

        mModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setViewModel(mModel);

        final View v = binding.getRoot();

        results = v.findViewById(R.id.result_pets);

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
                ///TODO: Seac animals
            }
        });

        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setVisibility(View.GONE);
                expandButton.show();
            }
        });

        v.findViewById(R.id.add_pet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: ADD A PET TO THE DB
            }
        });

        //TODO: check tht this is the corect way to observe from a fragment (to avoid memory leaks)
        mModel.getResidentResults().observe(getViewLifecycleOwner(), new Observer<LinkedList<ResidenceSearchData>>() {
            @Override
            public void onChanged(final LinkedList<ResidenceSearchData> residenceSearchData) {
//                LinearLayout emptyView = v.findViewById(R.id.empty_view);
//                if (residenceSearchData != null && !residenceSearchData.isEmpty()) {
//                    emptyView.setVisibility(View.GONE);
//                    results.setVisibility(View.VISIBLE);
//                    results.setAdapter(new ResidenceSearchListAdapter(getContext(), R.layout.content_residence_search_entry, residenceSearchData));
//                    results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                            mModel.triggerViewResident(((ResidenceSearchData) results.getAdapter().getItem(i)).getID());
//                        }
//                    });
//                    searchView.setVisibility(View.GONE);
//                    expandButton.show();
//                } else {
//                    emptyView.setVisibility(View.VISIBLE);
//                    results.setVisibility(View.GONE);
//                }
            }
        });
        return v;
    }

}
