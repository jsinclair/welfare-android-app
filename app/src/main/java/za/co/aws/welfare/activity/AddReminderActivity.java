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
import za.co.aws.welfare.databinding.ActivityAddReminderBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.fragment.ReminderDatePickerFragment;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.fragment.YesNoDialogFragment;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.RemindersViewModel;

/** Allow the user to add / edit a reminder. */
public class AddReminderActivity extends AppCompatActivity implements YesNoDialogFragment.YesNoDialogUser, ReminderDatePickerFragment.DatePickerUser {

    // Used as tag for the search pets dialog.
    private static final String SEARCH_PETS_FRAGMENT = "SEARCH_PETS_FRAGMENT";

    // Used as tag for the confirmation dialog to remove pet from reminder.
    private static final String REMOVE_PET_CONFIRM = "REMOVE_PET_CONFIRM";
    private static final String REMOVE_THIS_REMINDER = "REMOVE_THIS_REMINDER";

    // Used as tag for the date picker.
    private static final String DATE_TAG = "DATE_TAG";

    // Used as tag for the alert dialog.
    private static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";
    private static final int PET_RESULT = 21;

    // Button to display date of reminder, as well as open the date picker.
    private Button mDatePicker;

    // The notes text input layout.
    private TextInputLayout mNotes;

    // Viewmodel takes care of backend calls and data.
    private RemindersViewModel mModel;

    // Used to add a pet to the reminder.
    private Button mAddPet;

    // Lists pets that can then be added/removed from reminder.
    private ListView mPetsEditList;

    private FlexboxLayout mAnimalDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle details = getIntent().getExtras();
        boolean isNew = true;
        boolean fromSearch = false;
        int reminderID = -1;
        if (details != null) {
            fromSearch = details.getBoolean(Utils.INTENT_FROM_SEARCH, false);
            isNew = details.getBoolean("RequestNewEntry", true);
            if (!isNew) {
                reminderID = details.getInt("ReminderID", -1);
            }
        }

        ActivityAddReminderBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_add_reminder);
        mModel = ViewModelProviders.of(this).get(RemindersViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mPetsEditList = findViewById(R.id.pets_edit_list);
        mDatePicker = findViewById(R.id.date_picker);
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = mModel.getSelectedDate().getValue();
                if (RemindersViewModel.UNKNOWN_DATE.equals(date)) {
                    date = "";
                }
                ReminderDatePickerFragment dialog = ReminderDatePickerFragment.newInstance(date, "-");
                dialog.show(getSupportFragmentManager(), DATE_TAG);
            }
        });

        mNotes = findViewById(R.id.notes_container);

        mAnimalDisplay = findViewById(R.id.animal_nav_list);
        mAddPet = findViewById(R.id.add_pet_button);
        mAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchPetsFragment newFragment = new SearchPetsFragment();
                newFragment.setPetSearcher(mModel);
                newFragment.show(getSupportFragmentManager(), SEARCH_PETS_FRAGMENT);
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.reloadData();
            }
        });

        mModel.getAnimalList().observe(this, new Observer<List<PetMinDetail>>() {
            @Override
            public void onChanged(List<PetMinDetail> petMinDetails) {
                setupAnimalViews(petMinDetails);
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

        mModel.getEventHandler().observe(this, new Observer<Pair<RemindersViewModel.Event, String>>() {
            @Override
            public void onChanged(Pair<RemindersViewModel.Event, String> eventStringPair) {
                handleEvent(eventStringPair);
            }
        });

        mModel.getmNetworkHandler().observe(this, new Observer<RemindersViewModel.NetworkAction>() {
            @Override
            public void onChanged(RemindersViewModel.NetworkAction networkAction) {
                if (networkAction != null) {
                    handleNetworkStatus(networkAction);
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
            mModel.setup(isNew, reminderID, fromSearch);
        }
    }


    /** Handle once off events.*/
    private void handleEvent(Pair<RemindersViewModel.Event, String> eventData) {
        switch (eventData.first) {
            case DATE_REQUIRED:
                showAlert(getString(R.string.data_required), eventData.second);
                break;
            case EDIT_ATTEMPT_TODAY:
                showAlert(getString(R.string.not_editable), eventData.second);
                break;
            case UPDATE_ERROR:
                showAlert(getString(R.string.update_error_title), eventData.second);
                break;
            case RETRIEVAL_ERROR:
                showAlert(getString(R.string.fetch_error_title), eventData.second);
                break;
            case DELETE_DONE:
                Intent outputDelete = new Intent();
                outputDelete.putExtra(Utils.INTENT_REMINDER_ID, mModel.getReminderID());
                setResult(RESULT_OK, outputDelete);
                finish();
                break;
            case DELETE_ERROR:
                showAlert(getString(R.string.delete_error), eventData.second);
                break;
        }
    }

    private void handleNetworkStatus(RemindersViewModel.NetworkAction action) {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialogFragment progressDialog = Utils.getProgressDialog(fm);
        switch (action) {
            case IDLE:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case UPDATING:
                Utils.updateProgress(fm, progressDialog, getString(R.string.updating_reminder_data));
                break;
            case RETRIEVING_DATA:
                Utils.updateProgress(fm, progressDialog, getString(R.string.retrieving_reminder_data));
                break;
            case DELETING_REMINDER:
                Utils.updateProgress(fm, progressDialog, getString(R.string.delete_reminder_data));
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
        mDatePicker.setEnabled(editable);
        mNotes.setEnabled(editable);
        if (editable) {
            mPetsEditList.setVisibility(View.VISIBLE);
            mAddPet.setVisibility(View.VISIBLE);
            mAnimalDisplay.setVisibility(View.GONE);
        } else {
            mPetsEditList.setVisibility(View.GONE);
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
                    Intent intent = new Intent(AddReminderActivity.this, PetActivity.class);
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

        mPetsEditList.setAdapter(new RemoveAnimalAdapter(this, R.layout.remove_animal_content, list, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestRemovePet((PetMinDetail)view.getTag());
            }
        }));
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

    // Confirm that the user wishes to remove selected pet.
    private void requestRemovePet(PetMinDetail pet) {
        mModel.setRemoveRequest(pet);
        DialogFragment dialog = YesNoDialogFragment.newInstance(getString(R.string.remove_pet_reminder_title),
                getString(R.string.remove_pet__reminder_message, pet.getName()),
                getString(R.string.remove_pet_yes),
                getString(R.string.remove_pet_no), REMOVE_PET_CONFIRM);
        dialog.show(getSupportFragmentManager(), REMOVE_PET_CONFIRM);
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
            requestDeleteReminder();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestDeleteReminder() {
        DialogFragment dialog = YesNoDialogFragment.newInstance(getString(R.string.delete_reminder_title),
                getString(R.string.delete_reminder_msg),
                getString(R.string.delete),
                getString(R.string.keep), REMOVE_THIS_REMINDER);
        dialog.show(getSupportFragmentManager(), REMOVE_THIS_REMINDER);
    }

    @Override
    public void onDialogYesSelected(String tag) {
        if (REMOVE_PET_CONFIRM.equals(tag)) {
            mModel.removePet();
        } else if (REMOVE_THIS_REMINDER.equals(tag)) {
            mModel.permanentlyDelete();
        }
    }

    @Override
    public void onDialogNoSelected(String tag) {

    }

    @Override
    public void onDateChosen(int year, int month, int day) {
        String monthString = String.format("%02d", month);
        String dayString = String.format("%02d", day);
        String date = year + "-" + monthString + "-" + dayString;
        mModel.setDate(date);
    }
}
