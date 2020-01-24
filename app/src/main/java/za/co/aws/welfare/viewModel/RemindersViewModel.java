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

    public MutableLiveData<Date> mDateSelected;
    public MutableLiveData<String> mNotes;

    // List of animals to associate with reminder.
    public MutableLiveData<List<PetMinDetail>> mAnimalList;

    public RemindersViewModel(Application application) {
        super(application);
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();
    }

    public MutableLiveData<List<PetMinDetail>> getAnimalList() {
        return mAnimalList;
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


}
