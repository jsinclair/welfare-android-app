package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedList;
import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.PetSearchListAdapter;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.databinding.AnimalsBinding;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.viewModel.HomeViewModel;

/** Used to search for animals. */
public class AnimalsFragment extends Fragment {

    private LinearLayout searchView;
    private FloatingActionButton expandButton;
    private Button searchButton;
    private ListView results;
    private Spinner mSpecies;

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
        mSpecies = v.findViewById(R.id.species);

        mModel.getSpeciesAvailable().observe(getViewLifecycleOwner(), new Observer<List<AnimalType>>() {
            @Override
            public void onChanged(List<AnimalType> animalTypes) {
                ArrayAdapter<AnimalType> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, animalTypes);
                mSpecies.setAdapter(adapter);

                AnimalType selected = mModel.getAnimalTypeSelected().getValue();
                if (selected != null) {
                    int spinnerPosition = adapter.getPosition(selected);
                    mSpecies.setSelection(spinnerPosition);
                }
            }
        });

        mSpecies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mModel.mSpeciesAvailableSearch.setValue((AnimalType) adapterView.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                mModel.doAnimalSearch();
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
                mModel.triggerAddPet();
            }
        });

        //TODO: check tht this is the corect way to observe from a fragment (to avoid memory leaks)
        mModel.getPetSearchResults().observe(getViewLifecycleOwner(), new Observer<LinkedList<PetSearchData>>() {
            @Override
            public void onChanged(final LinkedList<PetSearchData> petSearchData) {
                LinearLayout emptyView = v.findViewById(R.id.empty_view);
                if (petSearchData != null && !petSearchData.isEmpty()) {
                    emptyView.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
                    results.setAdapter(new PetSearchListAdapter(getContext(), R.layout.content_pet_search_entry, petSearchData));
                    results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mModel.triggerViewPet(((PetSearchData) results.getAdapter().getItem(i)).getID());
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
