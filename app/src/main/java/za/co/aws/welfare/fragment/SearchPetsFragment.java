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
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.activity.PetActivity;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.customComponents.PetSearchListAdapter;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.databinding.SearchPetsBinding;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.SearchPetsViewModel;

/**
 * This fragment is strictly used from the Pet Activity to MOVE a pet from one residence to another.
 * It should display pets based on the user's search result. On pet picked, it should return the
 * pet id to the residence activity.
 */
public class SearchPetsFragment extends DialogFragment {

    /** Should be implemented by all users of this dialog. */
    public interface PetSearcher {
        /**
         * Will be called when once an option is selected.
         * @param result Details of the pet selected.
         **/
        void onPetSelected(PetMinDetail result);
    }

    private LinearLayout searchView;
    private Button searchButton;
    private ListView results;
    private TextInputEditText mPetName;
    private RadioGroup mSterilisedGroup;
    private RadioGroup mGenderGroup;
    private Spinner mSpecies;

    private PetSearcher mPetSearcher;

    private SearchPetsViewModel mModel;

    private static final int PET_RESULT = 90;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle("Search and Choose a pet");
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

        mSterilisedGroup = v.findViewById(R.id.selection_sterilised);
        mGenderGroup = v.findViewById(R.id.selection_gender);
        mPetName = v.findViewById(R.id.pet_name);
        results = v.findViewById(R.id.result_pets);
        searchView = v.findViewById(R.id.search_menu);
        mSpecies = v.findViewById(R.id.species);

        ArrayAdapter<AnimalType> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                ((WelfareApplication) getActivity().getApplication()).getAnimalTypes(true));
        mSpecies.setAdapter(adapter);

        searchButton = v.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String petName = mPetName.getText() == null ? null : mPetName.getText().toString();
                int speciesID = ((AnimalType)mSpecies.getSelectedItem()).getId();

                String gender = null;
                switch (mGenderGroup.getCheckedRadioButtonId()) {
                    case R.id.female_check:
                        gender = Utils.GENDER_FEMALE;
                        break;
                    case R.id.male_check:
                        gender = Utils.GENDER_MALE;
                        break;
                }

                String sterilised = null;
                switch (mSterilisedGroup.getCheckedRadioButtonId()) {
                    case R.id.yes_check:
                        sterilised = "1";
                        break;
                    case R.id.no_check:
                        sterilised = "0";
                        break;
                }

                mModel.doAnimalSearch(speciesID, petName, gender, sterilised); //TODO add the other stuff;
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

        //TODO: check tht this istthe corect way to observe from a fragment (to avoid memory leaks)
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
     * @param status
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
