package za.co.aws.welfare.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedList;
import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.activity.PetActivity;
import za.co.aws.welfare.customComponents.PetSearchListAdapter;
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.databinding.SearchPetsBinding;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.SearchPetsViewModel;

/**
 * This fragment is used to search for pets and return a selected pet.
 * It should display pets based on the user's search result. On pet picked, it should return the
 * pet id to the calling activity.
 *
 * ALWAYS CALL setPetSearcher() TO MAKE SURE RESULT IS OBTAINED.
 */
public class SearchPetsFragment extends DialogFragment {

    // Tag used for alert dialog.
    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";

    /** Should be implemented by all users of this dialog. */
    public interface PetSearcher {
        /**
         * Will be called when once an option is selected.
         * @param result Details of the pet selected.
         **/
        void onPetSelected(PetMinDetail result);
    }

    // Container for the search result. Hidden on error / empty.
    private LinearLayout searchView;

    // List of results.
    private ListView results;

    // Spinner for species.
    private Spinner mSpecies;

    // The interface to send the result to.
    private PetSearcher mPetSearcher;

    // The view model of this fragment.
    private SearchPetsViewModel mModel;

    // Code used to obtain result from adding new pet.
    private static final int PET_RESULT = 90;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle(getString(R.string.pet_search_title));
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        SearchPetsBinding binding = DataBindingUtil.inflate(inflater, R.layout.search_pets, container, false);
        mModel = ViewModelProviders.of(this).get(SearchPetsViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(mModel);

        final View v = binding.getRoot();

        searchView = v.findViewById(R.id.search_menu);
        final FloatingActionButton expandButton = v.findViewById(R.id.expand_button);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setVisibility(View.VISIBLE);
                expandButton.hide();
            }
        });

        FloatingActionButton add = v.findViewById(R.id.add_pet);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PetActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Utils.INTENT_FROM_SEARCH, true);
                startActivityForResult(intent, PET_RESULT);
            }
        });

        results = v.findViewById(R.id.result_pets);
        searchView = v.findViewById(R.id.search_menu);
        mSpecies = v.findViewById(R.id.species);

        mModel.getSpeciesAvailable().observe(getViewLifecycleOwner(), new Observer<List<AnimalType>>() {
            @Override
            public void onChanged(List<AnimalType> animalTypes) {
                ArrayAdapter<AnimalType> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, animalTypes);
                mSpecies.setAdapter(adapter);

                AnimalType selected = mModel.getSpeciesTypeSelected().getValue();
                if (selected != null) {
                    int spinnerPosition = adapter.getPosition(selected);
                    mSpecies.setSelection(spinnerPosition);
                }
            }
        });

        mSpecies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mModel.mSelectedSpecies.setValue((AnimalType) adapterView.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button searchButton = v.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.doAnimalSearch();
                searchView.setVisibility(View.GONE);
                expandButton.show();
            }
        });

        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mModel.getNetworkHandler().observe(this, new Observer<SearchPetsViewModel.NetworkStatus>() {
            @Override
            public void onChanged(SearchPetsViewModel.NetworkStatus networkStatus) {
                if (networkStatus != null) {
                    handleNetworkStatus(networkStatus);
                }
            }
        });

        mModel.getEventHandler().observe(this, new Observer<Pair<SearchPetsViewModel.Event, String>>() {
            @Override
            public void onChanged(Pair<SearchPetsViewModel.Event, String> eventStringPair) {
                handleEvent(eventStringPair);
            }
        });

        mModel.getSearchResults().observe(getViewLifecycleOwner(), new Observer<LinkedList<PetSearchData>>() {
            @Override
            public void onChanged(final LinkedList<PetSearchData> searchData) {
                LinearLayout emptyView = v.findViewById(R.id.empty_view);
                if (searchData != null && !searchData.isEmpty()) {
                    emptyView.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
                    results.setAdapter(new PetSearchListAdapter(getContext(), R.layout.content_pet_search_entry, searchData));
                    results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (mPetSearcher != null) {
                                PetSearchData sel = ((PetSearchData) results.getAdapter().getItem(i));
                                mPetSearcher.onPetSelected(new PetMinDetail(sel.getID(), sel.getPetName(), sel.isSterilised()));
                            }
                            dismiss();
                        }
                    });
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    results.setVisibility(View.GONE);
                }
            }
        });
        return v;
    }

    /**
     * Take care of displaying the network status to the user.
     */
    private void handleNetworkStatus(SearchPetsViewModel.NetworkStatus status) {
        FragmentManager fm = getChildFragmentManager();
        ProgressDialogFragment progressDialog = Utils.getProgressDialog(fm);
        switch (status) {
            case IDLE:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case SEARCHING_PET:
                Utils.updateProgress(fm, progressDialog, getString(R.string.search_pets));
                break;
        }
    }

    // Take care of error while searching.
    private void handleEvent(Pair<SearchPetsViewModel.Event, String> eventDetails) {
        if (eventDetails != null && eventDetails.first != null) {
            switch (eventDetails.first) {
                case SEARCH_PET_ERROR:
                    FragmentManager fm = getChildFragmentManager();
                    AlertDialogFragment alert = AlertDialogFragment.newInstance(getString(R.string.download_err), eventDetails.second);
                    Utils.showDialog(fm, alert, ALERT_DIALOG_TAG, true);
                    break;
            }
        }
    }

    /** The calling activity MUST set this to receive the resulting value. */
    public void setPetSearcher(PetSearcher petSearcher) {
        this.mPetSearcher = petSearcher;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PET_RESULT && data != null) {
            if (data.hasExtra(Utils.INTENT_PET_RETURN_ID)) {
                int sterilised = data.getIntExtra(Utils.INTENT_PET_RETURN_STERILISED, Utils.STERILISED_UNKNOWN);
                String name = data.getStringExtra(Utils.INTENT_PET_RETURN_NAME);
                int id = data.getIntExtra(Utils.INTENT_PET_RETURN_ID, -1);
                if (id != -1 && mPetSearcher != null) {
                    mPetSearcher.onPetSelected(new PetMinDetail(id, name, sterilised));
                }
                dismiss();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
