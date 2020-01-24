package za.co.aws.welfare.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.RemoveAnimalAdapter;
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.databinding.ActivityAddReminderBinding;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.viewModel.RemindersViewModel;

/** Allow the user to add / edit a reminder. */
public class AddReminderActivity extends AppCompatActivity {

    private static final String SEARCH_PETS_FRAGMENT = "SEARCH_PETS_FRAGMENT";

    private DatePicker mDatePicker;
    private RemindersViewModel mModel;
    private Button mAddPet;
    private ListView mPetsEditList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAddReminderBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_add_reminder);
        mModel = ViewModelProviders.of(this).get(RemindersViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mPetsEditList = findViewById(R.id.pets_edit_list);

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

        mModel.getAnimalList().observe(this, new Observer<List<PetMinDetail>>() {
            @Override
            public void onChanged(List<PetMinDetail> petMinDetails) {
                setupAnimalViews(petMinDetails);
            }
        });
    }

    /** Generate the animal list and setup click listeners. */
    private void setupAnimalViews(List<PetMinDetail> list) {
        //mAnimalDisplay.removeAllViews(); TODO
        for (PetMinDetail animal: list) {

           /* TODO: Button aniButton = new Button(this);
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

            mAnimalDisplay.addView(aniButton, params); */
            mPetsEditList.setAdapter(new RemoveAnimalAdapter(this, R.layout.remove_animal_content, list, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO!!!!!
//                    requestRemovePet((PetMinDetail)view.getTag());
                }
            }));
        }
    }
}
