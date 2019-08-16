package za.co.aws.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import za.co.aws.welfare.databinding.ActivityPetBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.fragment.SearchResidenceFragment;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.PetViewModel;

/** Allow the user to view and edit a pet. */
public class PetActivity extends AppCompatActivity implements DatePickerFragment.DatePickerUser {

    // Used for the alert dialog to inform user of errors.
    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";

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
    private TextInputLayout mWelfareIDContainer;
    private Spinner mSpecies;

    private Button mNavResButton;
    private Button mChangeResButton;

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

        mChangeResButton = findViewById(R.id.change_res);
        mChangeResButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new SearchResidenceFragment();
                newFragment.show(getSupportFragmentManager(), SEARCH_RES_FRAGMENT);
            }
        });

        mNavResButton = findViewById(R.id.nav_res);
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
            case SEARCH_RES_ERROR:
                showAlert(getString(R.string.download_err), eventData.second);
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
        boolean allowAddNav = mModel.getAllowAddressNavigation().getValue() != null && mModel.getAllowAddressNavigation().getValue();
        mNavResButton.setEnabled(!editable && allowAddNav);
        mSpecies.setEnabled(editable);
        if (editable) {
            mChangeResButton.setVisibility(View.VISIBLE);
        } else {
            mChangeResButton.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
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
            setResult(RESULT_OK, output);
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        MenuItem mEditCancel = menu.findItem(R.id.edit);
        MenuItem mCancelAction = menu.findItem(R.id.cancel);
        if (mModel.getEditMode().getValue() != null && mModel.getEditMode().getValue()) {
            mCancelAction.setVisible(true);
            mEditCancel.setIcon(getResources().getDrawable(R.drawable.baseline_save_white_24));
        } else {
            mCancelAction.setVisible(false);
            mEditCancel.setIcon(getResources().getDrawable(R.drawable.baseline_edit_white_24));
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
        }
        return super.onOptionsItemSelected(item);
    }
}