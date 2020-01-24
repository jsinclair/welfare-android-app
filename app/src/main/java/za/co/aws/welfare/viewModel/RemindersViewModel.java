package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.text.format.DateUtils;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.utils.SingleLiveEvent;

/** Viewmodel for the add reminders activity. Handle backend calls and data changes. */
public class RemindersViewModel extends AndroidViewModel implements SearchPetsFragment.PetSearcher {

    // Place holder for the date.
    public static final String UNKNOWN_DATE = "Unknown";

    // Once off events.
    public enum Event {
        DATE_REQUIRED,
        EDIT_ATTEMPT_TODAY,
    }

    // Used to indicate an event has triggered.
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // Stores date selected by the user.
    public MutableLiveData<String> mDateSelected;

    // Stores notes for the event.
    public MutableLiveData<String> mNotes;

    // Stores a list of animals associated with the reminder.
    public MutableLiveData<List<PetMinDetail>> mAnimalList;

    private Integer reminderID;
    private boolean isNew;
    private boolean fromSearch;
    private boolean successfulEditOccurred;
    private PetMinDetail mRemoveRequest;

    // These store the values to revert to if the user 'cancels' an edit.
    private String mDateSave, mNotesSave;
    private List<PetMinDetail> mSavedAnimalList;

    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public RemindersViewModel(Application application) {
        super(application);
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();
        mDateSelected = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();
        successfulEditOccurred = false;
        mEventHandler = new SingleLiveEvent<>();
    }

    public MutableLiveData<List<PetMinDetail>> getAnimalList() {
        return mAnimalList;
    }

    public MutableLiveData<String> getSelectedDate() {
        return mDateSelected;
    }

    // Should set to TRUE if editable.
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
    }

    public MutableLiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }


    /** call this to add a pet to the residence. Will only be persisted on save. */
    public void onPetSelected(PetMinDetail petToAdd) {
        if (petToAdd != null) {
            List<PetMinDetail> list = mAnimalList.getValue();
            if (list == null) {
                list = new LinkedList<>();
            }
            boolean hasAni = false;
            for (PetMinDetail ani: list) {
                if (ani.getID() == petToAdd.getID()) {
                    hasAni = true;
                    break;
                }
            }
            if (!hasAni) {
                list.add(petToAdd);
            }
            mAnimalList.setValue(list);
        }
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int reminderID, boolean fromSearch) {
        this.isNew = isNew;
        this.fromSearch = fromSearch;
        mEditMode.setValue(isNew);
        if (!isNew) {
            //TODO: LOAD DATA FROM THE BACKEND loadData(reminderID);
        } else {
            // Start the date selector.
            mDateSelected.setValue(UNKNOWN_DATE);
        }
    }

    public boolean isNew () {
        return isNew;
    }

    /** Either enable edit, or if its enabled already start the saving process. */
    public void toggleSaveEdit() {
        Boolean currentEdit = mEditMode.getValue();
        if (currentEdit != null) {
            if (!currentEdit) {
                if (!isNew && isTodayReminder()) {
                    mEventHandler.setValue(new Pair<>(Event.EDIT_ATTEMPT_TODAY, getApplication().getString(R.string.cannot_edit_today)));
                    return;
                }
                mNotesSave = mNotes.getValue();
                mDateSave = mDateSelected.getValue();
                mSavedAnimalList = new LinkedList<>();
                if (mAnimalList != null && mAnimalList.getValue() != null) {
                    mSavedAnimalList.addAll(mAnimalList.getValue());
                }
                mEditMode.setValue(true);
            } else {
//                 Do save actions to backend.
//                 set editable back to false once done
                saveData();
            }
        }
    }

    /** Return true if the reminder's date is that of todays. */
    private boolean isTodayReminder() {
        String date = mDateSelected.getValue();
        if (!UNKNOWN_DATE.equals(date)) {
            try {
                DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                Date dateOff = formatter.parse(date);
                return DateUtils.isToday(dateOff.getTime());
            } catch (ParseException e) {}
        }
        return false;
    }

    public void setDate(String date) {
        mDateSelected.setValue(date);
    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        String date = mDateSelected.getValue();
        if (date == null || date.isEmpty() || UNKNOWN_DATE.equals(mDateSelected.getValue())) {
            mEventHandler.setValue(new Pair<Event, String>(Event.DATE_REQUIRED, getApplication().getString(R.string.date_required)));
            return;
        }
//        String address = mAddress.getValue();
//        String shackID = mShackID.getValue();
//        String lat = mLat.getValue();
//        String lon = mLon.getValue();
//        String notes = mNotes.getValue();
//
//        String resName = mResidentName.getValue();
//        String resTel = mResidentTel.getValue();
//        String resID = mResidentID.getValue();
//
//        // Ensure the user provides some form of address.
//        if ((address == null || address.isEmpty()) && (shackID == null || shackID.isEmpty())) {
//            mEventHandler.setValue(new Pair<ResidenceViewModel.Event, String>(ResidenceViewModel.Event.DATA_REQUIRED, getApplication().getString(R.string.address_shack_req)));
//            return;
//        }
//
//        if (isNew) {
//            doUpdate(-1, address, shackID, resName, resID, resTel, lat, lon, notes);
//        } else {
//            boolean hasChanged = ((address != null && !address.equals(mAddressSave))
//                    || (shackID != null && !shackID.equals(mShackIDSave))
//                    || (notes !=null && !notes.equals(mNotesSave))
//                    || (resName != null && !resName.equals(mSaveName))
//                    || (resTel != null && !resTel.equals(mSaveTelNumber))
//                    || (resID != null && !resID.equals(mSaveIDNumber))
//                    || (lon != null && !lon.equals(mLongSave))
//                    || (lat != null && !lat.equals(mLatSave)));
//
//            List<PetMinDetail> pets = mAnimalList.getValue();
//            if (!((mSavedAnimalList == null || mSavedAnimalList.isEmpty()) && (pets == null || pets.isEmpty()))) {
//                if ((pets == null && !mSavedAnimalList.isEmpty()) || (pets.size() != mSavedAnimalList.size())) { //todo; check logic here
//                    hasChanged = true;
//                } else {
//                    for (PetMinDetail pet : mSavedAnimalList) {
//                        if (!pets.contains(pet)) {
//                            hasChanged = true;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (hasChanged) {
//                doUpdate(residenceID, address, shackID, resName, resID, resTel, lat, lon, notes);
//            } else {
//                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
//                        Toast.LENGTH_LONG).show();
//                mEditMode.setValue(false);
//            }
//        }
    }

    /* Cancel the current edit and reset the values. */
    public void cancelEdit() {
        mEditMode.setValue(false);
        mDateSelected.setValue(mDateSave);
        mNotes.setValue(mNotesSave);
        mAnimalList.setValue(mSavedAnimalList);
    }

    // Set the last pet that was selected to remove from the reminder.
    public void setRemoveRequest(PetMinDetail deleteRequest) {
        this.mRemoveRequest = deleteRequest;
    }

    // remove a pet from the pet list.
    public void removePet() {
        if (mRemoveRequest != null) {
            List<PetMinDetail> list = mAnimalList.getValue();
            if (list != null) {
                list.remove(mRemoveRequest);
            }
            mAnimalList.setValue(list);
        }
    }
}
