package za.co.aws.welfare.fragment;

import android.app.Dialog;
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
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.customComponents.PetSearchListAdapter;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.viewModel.ResidenceViewModel;

/**
 * This fragment is strictly used from the Pet Activity to MOVE a pet from one residence to another.
 * It should display residences based on the user's search result. On residence picked, it should return the
 * residence id to the pet activity.
 */
public class SearchPetsFragment extends DialogFragment {

    private LinearLayout searchView;
    private Button searchButton;
    private ListView results;

    private ResidenceViewModel mModel;
    private TextInputEditText mPetName;

    private RadioGroup mSterilisedGroup;
    private RadioGroup mGenderGroup;
    private Spinner mSpecies;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle("Search and Choose a pet");
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        final View v = inflater.inflate(R.layout.search_pets, container, false);

        mModel = ViewModelProviders.of(getActivity()).get(ResidenceViewModel.class);

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

                mModel.doAnimalSearch(speciesID, petName); //TODO add the other stuff;
            }
        });

        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
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
                            PetSearchData sel = ((PetSearchData) results.getAdapter().getItem(i));
                            mModel.addPet(new ResidentAnimalDetail(sel.getID(), sel.getPetName()));
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
}
