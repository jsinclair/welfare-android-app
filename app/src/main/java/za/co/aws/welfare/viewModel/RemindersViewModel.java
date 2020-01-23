package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.List;

import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;

public class RemindersViewModel extends AndroidViewModel {

    public MutableLiveData<Date> mDateSelected;
    public MutableLiveData<String> mNotes;
    public MutableLiveData<List<ResidentAnimalDetail>> mAnimalList;

    public RemindersViewModel(Application application) {
        super(application);
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();
    }


}
