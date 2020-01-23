package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;

public class RemindersViewModel extends AndroidViewModel {

    public MutableLiveData<Date> mDateSelected;
    public MutableLiveData<String> mNotes;

    public RemindersViewModel(Application application) {
        super(application);
        mNotes = new MutableLiveData<>();
    }
}
