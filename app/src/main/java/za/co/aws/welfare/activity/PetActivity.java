package za.co.aws.welfare.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityPetBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.PetViewModel;

/** Allow the user to view and edit a pet. */
public class PetActivity extends AppCompatActivity {

    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";

//TODO: set title
    //TODO: ENable and disable
    // save and cancel save
    // update
    // navigation
    // New stuff
    //???? STUFF

    private PetViewModel mModel;

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


        if (savedInstanceState == null) {
            mModel.setup(isNew, resID);
        }
    }

    //TODO: UPDATE OTHER NETWORK HANDLERS
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
                Utils.updateProgress(fm, progressDialog, getString(R.string.retrieving_res_data));
                break;
            case UPDATING_DATA:
                Utils.updateProgress(fm, progressDialog, getString(R.string.updating_res_data));
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

}
