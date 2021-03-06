package za.co.aws.welfare.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.RemoveAnimalAdapter;
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.databinding.ActivityViewResidentBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.fragment.YesNoDialogFragment;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.ResidenceViewModel;

/** Allows the user to view and, if they have permission, edit a residence. */
public class ResidenceActivity extends AppCompatActivity implements YesNoDialogFragment.YesNoDialogUser {
    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";
    private static final String REMOVE_PET_CONFIRM = "REMOVE_PET_CONFIRM";
    private static final String REMOVE_THIS_RES = "REMOVE_THIS_RES";
    private static final String SEARCH_PETS_FRAGMENT = "SEARCH_PETS_FRAGMENT";
    private static final int PET_RESULT = 42;

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

    private TextInputLayout mResName;
    private TextInputLayout mResID;
    private TextInputLayout mResTel;

    private Button mAddPet;

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
               resID = details.getInt("ResidentID", -1);
           }
        }

        ActivityViewResidentBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_view_resident);
        mModel = ViewModelProviders.of(this).get(ResidenceViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mResName = findViewById(R.id.resident_container);
        mResID = findViewById(R.id.resident_id_container);
        mResTel = findViewById(R.id.resident_tel_container);

        mAddPet = findViewById(R.id.add_pet_button);
        mAnimalDisplay = findViewById(R.id.animal_nav_list);
        mAnimalEditList = findViewById(R.id.remove_animals_list);
        mAddress = findViewById(R.id.address_container);
        mShackID = findViewById(R.id.shack_container);
        mNotes = findViewById(R.id.notes_container);

        if (!mModel.fromSearch()) {
            mAddPet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SearchPetsFragment newFragment = new SearchPetsFragment();
                    newFragment.setPetSearcher(mModel);
                    newFragment.show(getSupportFragmentManager(), SEARCH_PETS_FRAGMENT);
                }
            });
        } else {
            mAddPet.setVisibility(View.GONE);
        }

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

        mModel.getAnimalList().observe(this, new Observer<List<PetMinDetail>>() {
            @Override
            public void onChanged(List<PetMinDetail> petMinDetails) {
                setupAnimalViews(petMinDetails);
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
                } else {
                    findViewById(R.id.error_view).setVisibility(View.GONE);
                    findViewById(R.id.data_container).setVisibility(View.VISIBLE);
                }
                invalidateOptionsMenu();
            }
        });

        if (savedInstanceState == null) {
            mModel.setup(isNew, resID, fromSearch);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PET_RESULT && data != null) {
            boolean updateRequired = data.getBooleanExtra(Utils.INTENT_UPDATE_REQUIRED, false);
            if (updateRequired) {
                mModel.reloadData();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
            case DELETE_RES:
                Utils.updateProgress(fm, progressDialog, getString(R.string.delete_res_data));
                break;
            case SEARCHING_PET:
                Utils.updateProgress(fm, progressDialog, getString(R.string.search_pets));
                break;
        }
    }

    /** Handle once off events.*/
    private void handleEvent(Pair<ResidenceViewModel.Event, String> eventData) {
        if (eventData != null && eventData.first != null) {
            switch (eventData.first) {
                case RETRIEVAL_ERROR:
                    showAlert(getString(R.string.fetch_error_title), eventData.second);
                    break;
                case UPDATE_ERROR:
                    showAlert(getString(R.string.update_error_title), eventData.second);
                    break;
                case DATA_REQUIRED:
                    showAlert(getString(R.string.data_required), eventData.second);
                    break;
                case DELETE_DONE:
                    Intent outputDelete = new Intent();
                    outputDelete.putExtra(Utils.INTENT_RES_ID, mModel.getResidenceID());
                    outputDelete.putExtra(Utils.INTENT_ACTION, Utils.INTENT_ACTION_DELETE);
                    setResult(RESULT_OK, outputDelete);
                    finish();
                    break;
                case DELETE_ERROR:
                    showAlert(getString(R.string.delete_error), eventData.second);
                    break;
                case SPECIAL_ADD_DONE:
                    Intent output = new Intent();
                    output.putExtra(Utils.INTENT_RES_ID, mModel.getResidenceID());
                    output.putExtra(Utils.INTENT_RES_DESC, mModel.getAddress());
                    setResult(RESULT_OK, output);
                    finish();
                    break;
            }
        }
    }

    /** Update the editable views and icons. */
    private void setEditable(boolean editable) {
        mAddress.setEnabled(editable);
        mShackID.setEnabled(editable);
        mNotes.setEnabled(editable);
        mResName.setEnabled(editable);
        mResID.setEnabled(editable);
        mResTel.setEnabled(editable);
        if (editable) {
            mAnimalEditList.setVisibility(View.VISIBLE);
            if (!mModel.fromSearch()) {
                mAddPet.setVisibility(View.VISIBLE);
            }
            mAnimalDisplay.setVisibility(View.GONE);
        } else {
            mAnimalEditList.setVisibility(View.GONE);
            mAddPet.setVisibility(View.GONE);
            mAnimalDisplay.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }

    /** Generate the animal list and setup click listeners. */
    private void setupAnimalViews(List<PetMinDetail> list) {
        mAnimalDisplay.removeAllViews();
        for (PetMinDetail animal: list) {

            Button aniButton = new Button(this);
            aniButton.setText(animal.getName());
            Bitmap steriIcon = null;
            BitmapDrawable n = null;
            if (animal.getSterilised() == Utils.STERILISED_NO) {
                steriIcon = BitmapFactory.decodeResource(getResources(), R.drawable.snip_req);
            } else if (animal.getSterilised() == Utils.STERILISED_UNKNOWN) {
                steriIcon = BitmapFactory.decodeResource(getResources(), R.drawable.snip_unknown);
            }

            if (steriIcon != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.snip_size);
                steriIcon = Utils.resize(steriIcon, size, size);
                n = new BitmapDrawable(getResources(), steriIcon);
            }

            aniButton.setCompoundDrawablesWithIntrinsicBounds(n, null, getResources().getDrawable(R.drawable.baseline_navigate_next_white_24), null);
            aniButton.setTag(animal);
            aniButton.setPadding(4, 4, 4, 4);
            aniButton.setTextColor(getResources().getColor(R.color.colorBackground));
            aniButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));
            aniButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ResidenceActivity.this, PetActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("petID", ((PetMinDetail)view.getTag()).getID());
                    intent.putExtra("RequestNewEntry", false);
                    startActivityForResult(intent, PET_RESULT);
                }
            });

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int padding = getResources().getDimensionPixelSize(R.dimen.pet_gap);
            params.setMargins(padding, padding, padding, padding);

            mAnimalDisplay.addView(aniButton, params);
        }
        mAnimalEditList.setAdapter(new RemoveAnimalAdapter(this, R.layout.remove_animal_content, list, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestRemovePet((PetMinDetail)view.getTag());
            }
        }));
    }

    private void requestRemovePet(PetMinDetail pet) {
        mModel.setRemoveRequest(pet);
        DialogFragment dialog = YesNoDialogFragment.newInstance(getString(R.string.remove_pet_title),
                getString(R.string.remove_pet_message, pet.getName()),
                getString(R.string.remove_pet_yes),
                getString(R.string.remove_pet_no), REMOVE_PET_CONFIRM);
        dialog.show(getSupportFragmentManager(), REMOVE_PET_CONFIRM);
    }

    private void requestDeleteResidence() {
        DialogFragment dialog = YesNoDialogFragment.newInstance(getString(R.string.delete_res_title),
                getString(R.string.delete_res_msg),
                getString(R.string.delete),
                getString(R.string.keep), REMOVE_THIS_RES);
        dialog.show(getSupportFragmentManager(), REMOVE_THIS_RES);
    }

    // Convenience method to show an alert dialog.
    private void showAlert(String title, String message) {
        FragmentManager fm = getSupportFragmentManager();
        AlertDialogFragment alert = AlertDialogFragment.newInstance(title, message);
        Utils.showDialog(fm, alert, ALERT_DIALOG_TAG, true);
    }

    @Override
    public void onDialogYesSelected(String tag) {
        if (REMOVE_PET_CONFIRM.equals(tag)) {
            mModel.removePet();
        } else if (REMOVE_THIS_RES.equals(tag)) {
            mModel.permanentlyDelete();
        }
    }

    @Override
    public void onDialogNoSelected(String tag) {

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

            int id = mModel.getResidenceID();
            output.putExtra(Utils.INTENT_RES_ID, id);
            
            String shackID = mModel.mShackID.getValue();
            String streetAddress = mModel.getAddress();
            String lat = mModel.mLat.getValue();
            String lon = mModel.mLon.getValue();

            String animals = "";
            String allSteri = "Unknown";
            boolean allSteriBool = true;
            boolean unknownSteriBool = false;
            if (mModel.getAnimalList().getValue() != null) {
                boolean first = true;
                for (PetMinDetail pet : mModel.getAnimalList().getValue()) {
                    if (!first) {
                        animals +=", ";
                    }
                    animals += pet.getName();
                    first = false;
                    allSteriBool = allSteriBool && (pet.getSterilised() == Utils.STERILISED_YES);
                    if (pet.getSterilised() == Utils.STERILISED_UNKNOWN) {
                        unknownSteriBool = true;
                    }
                }
            }

            if (unknownSteriBool) {
                allSteri = "Unknown";
            } else if (allSteriBool) {
                allSteri = "Yes";
            } else {
                allSteri = "No";
            }

            String name = mModel.mResidentName.getValue();
            String residentID = mModel.mResidentID.getValue();
            String tel = mModel.mResidentTel.getValue();

            output.putExtra("residence", new ResidenceSearchData(id, shackID, streetAddress, name,
                    residentID, tel, lat, lon, animals, allSteri));
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
            requestDeleteResidence();
        }
        return super.onOptionsItemSelected(item);
    }
}
