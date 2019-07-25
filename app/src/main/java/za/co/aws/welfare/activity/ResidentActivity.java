package za.co.aws.welfare.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.RemoveAnimalAdapter;
import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;
import za.co.aws.welfare.databinding.ActivityViewResidentBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.ResidenceViewModel;

/** Allows the user to view and, if they have permission, edit a residence. */
public class ResidentActivity extends AppCompatActivity {
    //TODO: Allow user to ADD and REMOVE animal from residence (on edit)

    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";

    // The view model
    private ResidenceViewModel mModel;

    // Contains all the animals. Allows the user to navigate to selected animal.
    private FlexboxLayout mAnimalDisplay;
    private ListView mAnimalEditList;

    // The address text input layout.
    private TextInputLayout mAddress;
    // The shack text input layout.
    private TextInputLayout mShackID;
    // The notes text input layout.
    private TextInputLayout mNotes;

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
               resID = details.getInt("ResidentID", -1);
           }
        }

        ActivityViewResidentBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_view_resident);
        mModel = ViewModelProviders.of(this).get(ResidenceViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mAnimalDisplay = findViewById(R.id.animal_nav_list);
        mAnimalEditList = findViewById(R.id.remove_animals_list);
        mAddress = findViewById(R.id.address_container);
        mShackID = findViewById(R.id.shack_container);
        mNotes = findViewById(R.id.notes_container);
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

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.reloadData();
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

        mModel.getAnimalList().observe(this, new Observer<List<ResidentAnimalDetail>>() {
            @Override
            public void onChanged(List<ResidentAnimalDetail> residentAnimalDetails) {
                setupAnimalViews(residentAnimalDetails);
            }
        });

        mModel.getNetworkHandler().observe(this, new Observer<ResidenceViewModel.NetworkStatus>() {
            @Override
            public void onChanged(ResidenceViewModel.NetworkStatus networkStatus) {
                if (networkStatus != null) {
                    handleNetworkStatus(networkStatus);
                }
            }
        });

        mModel.getEventHandler().observe(this, new Observer<Pair<ResidenceViewModel.Event, String>>() {
            @Override
            public void onChanged(Pair<ResidenceViewModel.Event, String> eventStringPair) {
                if (eventStringPair != null && eventStringPair.first != null) {
                    handleEvent(eventStringPair);
                }
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

        mModel.setup(isNew, resID);
    }

    //TODO: UPDATE OTHER NETWORK HANDLERS
    private void handleNetworkStatus(ResidenceViewModel.NetworkStatus status) {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialogFragment progressDialog = Utils.getProgressDialog(fm);
        switch (status) {
            case IDLE:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case RETRIEVING_DATA:
                Utils.updateProgress(fm, progressDialog, getString(R.string.retrieving_res_data));
                break;
            case UPDATING_DATA:
                Utils.updateProgress(fm, progressDialog, getString(R.string.updating_res_data));
                break;
        }
    }

    /** Handle once off events.*/
    private void handleEvent(Pair<ResidenceViewModel.Event, String> eventData) {
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

    /** Update the editable views and icons. */
    private void setEditable(boolean editable) {
        mAddress.setEnabled(editable);
        mShackID.setEnabled(editable);
        mNotes.setEnabled(editable);
        if (editable) {
            mCancelEditButton.show();
            mAnimalEditList.setVisibility(View.VISIBLE);
            mAnimalDisplay.setVisibility(View.GONE);
            mEditButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_save_white_24));
        } else {
            mCancelEditButton.hide();
            mAnimalEditList.setVisibility(View.GONE);
            mAnimalDisplay.setVisibility(View.VISIBLE);
            mEditButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_edit_white_24));
        }
    }

    /** Generate the animal list and setup click listeners. */
    private void setupAnimalViews(List<ResidentAnimalDetail> list) {
        //TODO: Show welfare number!
        //TODO: navigate to animal view
        for (ResidentAnimalDetail animal: list) {
            Button aniButton = new Button(this);
            aniButton.setText(animal.getName());
            aniButton.setTag(animal);
            aniButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("CLICKED", ((ResidentAnimalDetail)view.getTag()).getName());
                }
            });
            mAnimalDisplay.addView(aniButton);
            mAnimalEditList.setAdapter(new RemoveAnimalAdapter(this, R.layout.remove_animal_content, list, null));
            //TODO: update animal edit list here too!
        }
    }

    // Convenience method to show an alert dialog.
    private void showAlert(String title, String message) {
        FragmentManager fm = getSupportFragmentManager();
        AlertDialogFragment alert = AlertDialogFragment.newInstance(title, message);
        Utils.showDialog(fm, alert, ALERT_DIALOG_TAG, true);
    }
}
