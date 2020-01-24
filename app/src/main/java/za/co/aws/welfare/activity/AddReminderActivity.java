package za.co.aws.welfare.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityAddReminderBinding;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.viewModel.RemindersViewModel;

/** Allow the user to add / edit a reminder. */
public class AddReminderActivity extends AppCompatActivity {

    private static final String SEARCH_PETS_FRAGMENT = "SEARCH_PETS_FRAGMENT";

    private DatePicker mDatePicker;
    private RemindersViewModel mModel;
    private Button mAddPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAddReminderBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_add_reminder);
        mModel = ViewModelProviders.of(this).get(RemindersViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mDatePicker = findViewById(R.id.date_picker);
        mDatePicker.setMinDate(System.currentTimeMillis() + 24 * 60 * 60 * 1000);

        mAddPet = findViewById(R.id.add_pet_button);
        mAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchPetsFragment newFragment = new SearchPetsFragment();
                newFragment.setPetSearcher(mModel);
                newFragment.show(getSupportFragmentManager(), SEARCH_PETS_FRAGMENT);
            }
        });
    }
}
