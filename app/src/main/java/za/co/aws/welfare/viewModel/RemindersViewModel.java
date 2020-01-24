package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.fragment.SearchPetsFragment;

public class RemindersViewModel extends AndroidViewModel implements SearchPetsFragment.PetSearcher {

    public MutableLiveData<String> mDateSelected;
    public MutableLiveData<String> mNotes;
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
    }

    public MutableLiveData<List<PetMinDetail>> getAnimalList() {
        return mAnimalList;
    }

    // Should set to TRUE if editable.
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
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

                mNotesSave = mNotes.getValue();
                mDateSave = mDateSelected.getValue();
                mSavedAnimalList = new LinkedList<>();
                if (mAnimalList != null && mAnimalList.getValue() != null) {
                    mSavedAnimalList.addAll(mAnimalList.getValue());
                }
                mEditMode.setValue(true);
            } else {
                // Do save actions to backend.
                // set editable back to false once done
                //TODO:
//                saveData();
            }
        }
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
