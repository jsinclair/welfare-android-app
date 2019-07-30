package za.co.aws.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.DatePickerFragment;
import za.co.aws.welfare.databinding.ActivityPetBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.PetViewModel;

/** Allow the user to view and edit a pet. */
public class PetActivity extends AppCompatActivity implements DatePickerFragment.DatePickerUser {

    // Used for the alert dialog to inform user of errors.
    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";

    // Used for the date dialog.
    private static final String DATE_TAG = "DATE_TAG";

//TODO: set title
    //TODO: Set and nav on Residence + ALLOW TO CHANGE RES>
    // navigation
    // New stuff

    // Data controller.
    private PetViewModel mModel;

    private TextInputLayout mNameContainer;
    private TextInputLayout mDOBContainer;
    private TextInputLayout mNotesContainer;
    private TextInputLayout mTreatmentsContainer;
    private TextInputLayout mWelfareIDContainer;
    private Spinner mSpecies;

    private Button mNavResButton;

    // Allow the user to edit the view.
    private FloatingActionButton mEditButton;

    // Allow the user to cancel the edit.
    private FloatingActionButton mCancelEditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle details = getIntent().getExtras();
        boolean isNew = true;
        int resID = -1;
        if (details != null) {
            isNew = details.getBoolean("RequestNewEntry", true);
            if (!isNew) {
                resID = details.getInt("petID", -1);
            }
        }

        ActivityPetBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_pet);
        mModel = ViewModelProviders.of(this).get(PetViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        //TODO: DISABLE OR HIDE IF NO RESIDENCE PRESENT>
        mNavResButton = findViewById(R.id.nav_res);
        mNavResButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetActivity.this, ResidentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("ResidentID", mModel.getResidenceID());
                intent.putExtra("RequestNewEntry", false);
                startActivity(intent);
            }
        });

        mCancelEditButton = findViewById(R.id.cancel_edit);
        mCancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mModel.isNew()) {
                    finish();
                } else {
                    mModel.cancelEdit();
                }
            }
        });
        mEditButton = findViewById(R.id.edit);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.toggleSaveEdit();
            }
        });

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
        mWelfareIDContainer = findViewById(R.id.welfareID_container);

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

        mModel.getHasDownloadError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null && aBoolean) {
                    findViewById(R.id.error_view).setVisibility(View.VISIBLE);
                    findViewById(R.id.data_container).setVisibility(View.GONE);
                    findViewById(R.id.edit).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.error_view).setVisibility(View.GONE);
                    findViewById(R.id.data_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.edit).setVisibility(View.VISIBLE);
                }
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
            mModel.setup(isNew, resID);
        }
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
        }
    }

    /** Handle once off events.*/
    private void handleEvent(Pair<PetViewModel.Event, String> eventData) {
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
        mTreatmentsContainer.setEnabled(editable);
        mWelfareIDContainer.setEnabled(editable);
        mNavResButton.setEnabled(!editable);
        mSpecies.setEnabled(editable);
        if (editable) {
            mCancelEditButton.show();
            findViewById(R.id.change_res).setVisibility(View.VISIBLE);
            mEditButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_save_white_24));
        } else {
            mCancelEditButton.hide();
            findViewById(R.id.change_res).setVisibility(View.GONE);
            mEditButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_edit_white_24));
        }
    }

    @Override
    public void onDateChosen(String date) {
        mModel.setDate(date);
    }
}
