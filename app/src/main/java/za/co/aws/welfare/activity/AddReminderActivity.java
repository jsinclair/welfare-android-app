package za.co.aws.welfare.activity;

import android.os.Bundle;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityAddReminderBinding;
import za.co.aws.welfare.viewModel.RemindersViewModel;

/** Allow the user to add / edit a reminder. */
public class AddReminderActivity extends AppCompatActivity {

    private DatePicker mDatePicker;
    private RemindersViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAddReminderBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_add_reminder);
        mModel = ViewModelProviders.of(this).get(RemindersViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mDatePicker = findViewById(R.id.date_picker);
        mDatePicker.setMinDate(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
    }
}
