package za.co.aws.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.DatePickerFragment;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.databinding.ActivityPetBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.fragment.SearchResidenceFragment;
import za.co.aws.welfare.fragment.YesNoDialogFragment;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.PetViewModel;

/** Allow the user to view and edit a pet. */
public class PetActivity extends AppCompatActivity implements DatePickerFragment.DatePickerUser, YesNoDialogFragment.YesNoDialogUser {

    // Used for the alert dialog to inform user of errors.
    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";
    private static final String REMOVE_THIS_PET = "REMOVE_THIS_PET";

    // Used for the date dialog.
    private static final String DATE_TAG = "DATE_TAG";
    private static final String SEARCH_RES_FRAGMENT = "SEARCH_RES_FRAGMENT";

    private static final int RESIDENCE_RESULT = 12;

    // Data controller.
    private PetViewModel mModel;

    private TextInputLayout mNameContainer;
    private TextInputLayout mDOBContainer;
    private TextInputLayout mNotesContainer;
    private TextInputLayout mTreatmentsContainer;
    private TextInputLayout mDescContainer;
    private Spinner mSpecies;

    private RadioGroup mGenderContainer;
    private RadioGroup mSterilisedContainer;

    private Button mNavResButton;
    private Button mChangeResButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle details = getIntent().getExtras();
        boolean isNew = true;
        boolean fromSearch = false;
        int resID = -1;
        if (details != null) {
            fromSearch = details.getBoolean(Utils.INTENT_FROM_SEARCH, false);
            isNew = details.getBoolean("RequestNewEntry", true);
            if (!isNew) {
                resID = details.getInt("petID", -1);
            }
        }

        ActivityPetBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_pet);
        mModel = ViewModelProviders.of(this).get(PetViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mGenderContainer = findViewById(R.id.selection_gender);
        mSterilisedContainer = findViewById(R.id.selection_sterilised);
        mDescContainer = findViewById(R.id.desc_container);

        mChangeResButton = findViewById(R.id.change_res);
        mNavResButton = findViewById(R.id.nav_res);
        if (!fromSearch) {
            mChangeResButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment newFragment = new SearchResidenceFragment();
                    newFragment.show(getSupportFragmentManager(), SEARCH_RES_FRAGMENT);
                }
            });

            mNavResButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PetActivity.this, ResidenceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("ResidentID", mModel.getResidenceID());
                    intent.putExtra("RequestNewEntry", false);
                    startActivityForResult(intent, RESIDENCE_RESULT);
                }
            });
        } else {
            mChangeResButton.setVisibility(View.GONE);
        }

        mModel.getEditMode().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    setEditable(aBoolean);
                }
            }
        });

        mModel.getSpeciesAvailable().observe(this, new Observer<List<AnimalType>>() {
            @Override
            public void onChanged(List<AnimalType> animalTypes) {
                ArrayAdapter<AnimalType> adapter = new ArrayAdapter<>(PetActivity.this, android.R.layout.simple_spinner_dropdown_item, animalTypes);
                mSpecies.setAdapter(adapter);
            }
        });

        mNameContainer = findViewById(R.id.name_container);
        mSpecies = findViewById(R.id.species);
        mDOBContainer = findViewById(R.id.dob_container);
        findViewById(R.id.dob).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mModel.getEditMode().getValue() != null && mModel.getEditMode().getValue()) {
                    DatePickerFragment dialog = DatePickerFragment.newInstance(mModel.getDateEntered(), "-");
                    dialog.show(getSupportFragmentManager(), DATE_TAG);
                }
            }
        });
        mNotesContainer = findViewById(R.id.notes_container);
        mTreatmentsContainer = findViewById(R.id.treatments_container);

        mModel.getNetworkHandler().observe(this, new Observer<PetViewModel.NetworkStatus>() {
            @Override
            public void onChanged(PetViewModel.NetworkStatus networkStatus) {
                if (networkStatus != null) {
                    handleNetworkStatus(networkStatus);
                }
            }
        });

        mModel.getEventHandler().observe(this, new Observer<Pair<PetViewModel.Event, String>>() {
            @Override
            public void onChanged(Pair<PetViewModel.Event, String> eventStringPair) {
                if (eventStringPair != null && eventStringPair.first != null) {
                    handleEvent(eventStringPair);
                }
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.reloadData();
            }
        });

        mModel.getAllowAddressNavigation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null && aBoolean && (mModel.getEditMode().getValue() != null && !mModel.getEditMode().getValue())) {
                    mNavResButton.setEnabled(true);
                } else {
                    mNavResButton.setEnabled(false);
                }
            }
        });

        mModel.getHasDownloadError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null && aBoolean) {
                    findViewById(R.id.error_view).setVisibility(View.VISIBLE);
                    findViewById(R.id.data_container).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.error_view).setVisibility(View.GONE);
                    findViewById(R.id.data_container).setVisibility(View.VISIBLE);
                }
                invalidateOptionsMenu();
            }
        });

        mModel.getSpecies().observe(this, new Observer<AnimalType>() {
            @Override
            public void onChanged(AnimalType animalType) {
                int spinnerPosition = ((ArrayAdapter)mSpecies.getAdapter()).getPosition(animalType);
                if (spinnerPosition > -1) {
                    mSpecies.setSelection(spinnerPosition);
                }
            }
        });

        mSpecies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                AnimalType chosen = (AnimalType) adapterView.getSelectedItem();
                mModel.setSpecies(chosen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (savedInstanceState == null) {
            mModel.setup(isNew, resID, fromSearch);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESIDENCE_RESULT && data != null) {
            boolean updateRequired = data.getBooleanExtra(Utils.INTENT_UPDATE_REQUIRED, false);
            if (updateRequired) {
                mModel.reloadData();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Correctly show and hide the progress dialog depending on the network action in progress. */
    private void handleNetworkStatus(PetViewModel.NetworkStatus status) {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialogFragment progressDialog = Utils.getProgressDialog(fm);
        switch (status) {
            case IDLE:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case RETRIEVING_DATA:
                Utils.updateProgress(fm, progressDialog, getString(R.string.retrieving_pet_data));
                break;
            case UPDATING_DATA:
                Utils.updateProgress(fm, progressDialog, getString(R.string.updating_pet_data));
                break;
            case SEARCHING_RESIDENCE:
                Utils.updateProgress(fm, progressDialog, getString(R.string.search_residence));
                break;
            case DELETE_PET:
                Utils.updateProgress(fm, progressDialog, getString(R.string.deleting_pet_from_sys));
                break;
        }
    }

    /** Handle once off events.*/
    private void handleEvent(Pair<PetViewModel.Event, String> eventData) {
        if (eventData != null && eventData.first != null) {
            switch (eventData.first) {
                case RETRIEVAL_ERROR:
                    showAlert(getString(R.string.fetch_error_title), eventData.second);
                    break;
                case UPDATE_ERROR:
                    showAlert(getString(R.string.fetch_error_title), eventData.second);
                    break;
                case DATA_REQUIRED:
                    showAlert(getString(R.string.data_required), eventData.second);
                    break;
                case SEARCH_RES_ERROR:
                    showAlert(getString(R.string.download_err), eventData.second);
                    break;
                case DELETE_DONE:
                    Intent outputDelete = new Intent();
                    outputDelete.putExtra(Utils.INTENT_PET_RETURN_ID, mModel.getPetID());
                    outputDelete.putExtra(Utils.INTENT_ACTION, Utils.INTENT_ACTION_DELETE);
                    setResult(RESULT_OK, outputDelete);
                    finish();
                    break;
                case DELETE_ERROR:
                    showAlert(getString(R.string.delete_error), eventData.second);
                    break;
                case SPECIAL_ADD_DONE:
                    Intent output = new Intent();
                    output.putExtra(Utils.INTENT_PET_RETURN_STERILISED, mModel.getSterilised());
                    output.putExtra(Utils.INTENT_PET_RETURN_NAME, mModel.getPetName());
                    output.putExtra(Utils.INTENT_PET_RETURN_ID, mModel.getPetID());
                    setResult(RESULT_OK, output);
                    finish();
                    break;
            }
        }
    }

    // Convenience method to show an alert dialog.
    private void showAlert(String title, String message) {
        FragmentManager fm = getSupportFragmentManager();
        AlertDialogFragment alert = AlertDialogFragment.newInstance(title, message);
        Utils.showDialog(fm, alert, ALERT_DIALOG_TAG, true);
    }

    /** Update the editable views and icons. */
    private void setEditable(boolean editable) {
        mNameContainer.setEnabled(editable);
        mDOBContainer.setEnabled(editable);
        mNotesContainer.setEnabled(editable);
        mDescContainer.setEnabled(editable);
        mTreatmentsContainer.setEnabled(editable);
        for (int i = 0; i < mGenderContainer.getChildCount(); i++) {
            mGenderContainer.getChildAt(i).setEnabled(editable);
        }
        for (int i = 0; i < mSterilisedContainer.getChildCount(); i++) {
            mSterilisedContainer.getChildAt(i).setEnabled(editable);
        }
        boolean allowAddNav = mModel.getAllowAddressNavigation().getValue() != null && mModel.getAllowAddressNavigation().getValue();
        mNavResButton.setEnabled(!editable && allowAddNav);
        mSpecies.setEnabled(editable);
        if (editable && !mModel.fromSearch()) {
            mChangeResButton.setVisibility(View.VISIBLE);
        } else {
            mChangeResButton.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    private void requestDeletePet() {
        DialogFragment dialog = YesNoDialogFragment.newInstance(getString(R.string.delete_pet_title),
                getString(R.string.delete_pet_msg),
                getString(R.string.delete),
                getString(R.string.keep), REMOVE_THIS_PET);
        dialog.show(getSupportFragmentManager(), REMOVE_THIS_PET);
    }

    @Override
    public void onDateChosen(String date) {
        mModel.setDate(date);
    }

    @Override
    public void onBackPressed() {
        if (mModel.editOccurred()) {
            Intent output = new Intent();
            output.putExtra(Utils.INTENT_UPDATE_REQUIRED, true);
            if (mModel.shouldAddToParent()) {
                output.putExtra(Utils.INTENT_ACTION, Utils.INTENT_ACTION_ADD);
            } else {
                output.putExtra(Utils.INTENT_ACTION, Utils.INTENT_ACTION_EDIT);
            }

            int petID = mModel.getPetID();
            output.putExtra(Utils.INTENT_PET_RETURN_ID, petID);

            int animalTypeID = -1;
            String animalTypeDesc = "";
            if (mModel.getSpecies().getValue() != null) {
                animalTypeID = mModel.getSpecies().getValue().getId();
                animalTypeDesc = mModel.getSpecies().getValue().getDescription();
            }

            String petName = mModel.getPetName();
            String petDOB = mModel.getDateEntered();
            String gender = mModel.mGender.getValue();
            int sterilised = mModel.getSterilised();

            String address = mModel.mDisplayAddress.getValue();

            output.putExtra("pet", new PetSearchData(petID, animalTypeID, animalTypeDesc,
                    petName, petDOB, gender, sterilised, address));
            setResult(RESULT_OK, output);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        MenuItem mEditCancel = menu.findItem(R.id.edit);
        MenuItem mCancelAction = menu.findItem(R.id.cancel);
        MenuItem deleteAction = menu.findItem(R.id.delete);
        if (mModel.getEditMode().getValue() != null && mModel.getEditMode().getValue()) {
            mCancelAction.setVisible(true);
            mEditCancel.setIcon(getResources().getDrawable(R.drawable.baseline_save_white_24));
            deleteAction.setVisible(!mModel.isNew());
        } else {
            mCancelAction.setVisible(false);
            deleteAction.setVisible(false);
            mEditCancel.setIcon(getResources().getDrawable(R.drawable.baseline_edit_white_24));
        }
        if (mModel.getHasDownloadError().getValue() != null && mModel.getHasDownloadError().getValue() ) {
            mEditCancel.setVisible(false);
        } else {
            mEditCancel.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.edit) {
            mModel.toggleSaveEdit();
        } else if (id == R.id.cancel) {
            if (mModel.isNew()) {
                finish();
            } else {
                mModel.cancelEdit();
            }
        } else if (id == R.id.delete) {
            requestDeletePet();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogYesSelected(String tag) {
        if (REMOVE_THIS_PET.equals(tag)) {
            mModel.permanentlyDelete();
        }
    }

    @Override
    public void onDialogNoSelected(String tag) {

    }
}