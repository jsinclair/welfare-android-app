package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
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

/** Displays search view and residence search. */
public class ResidencesFragment extends Fragment {

    // The container for all the search fields.
    private LinearLayout searchView;

    // The button to expand the search fields.
    private FloatingActionButton expandButton;

    // The results of the search.
    private ListView results;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ResidencesBinding binding =  DataBindingUtil.inflate(inflater,
                R.layout.residences, container, false);

        final HomeViewModel mModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setViewModel(mModel);

        final View v = binding.getRoot();

        results = v.findViewById(R.id.result_residences);

        searchView = v.findViewById(R.id.search_menu);
        Button searchButton = v.findViewById(R.id.search_button);
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
                mModel.doResidenceSearch();
            }
        });

        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setVisibility(View.GONE);
                expandButton.show();
            }
        });

        v.findViewById(R.id.add_residence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.triggerAddResident();
            }
        });

        mModel.getIndexScroller().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(final Integer integer) {
                if (integer != null) {
                    results.post(new Runnable() {
                        @Override
                        public void run() {
                            results.smoothScrollToPosition(integer);
                        }
                    });

                }
            }
        });

        mModel.getResidentResults().observe(getViewLifecycleOwner(), new Observer<LinkedList<ResidenceSearchData>>() {
            @Override
            public void onChanged(final LinkedList<ResidenceSearchData> residenceSearchData) {
                LinearLayout emptyView = v.findViewById(R.id.empty_view);
                if (residenceSearchData != null && !residenceSearchData.isEmpty()) {
                    emptyView.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
                    results.setAdapter(new ResidenceSearchListAdapter(getContext(), R.layout.content_residence_search_entry, residenceSearchData));
                    results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mModel.triggerViewResident(((ResidenceSearchData) results.getAdapter().getItem(i)).getID());
                        }
                    });
                    searchView.setVisibility(View.GONE);
                    expandButton.show();
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    results.setVisibility(View.GONE);
                }
            }
        });
        return v;
    }
}
