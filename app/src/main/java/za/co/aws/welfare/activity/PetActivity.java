package za.co.aws.welfare.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityPetBinding;
import za.co.aws.welfare.databinding.ActivityViewResidentBinding;
import za.co.aws.welfare.viewModel.PetViewModel;
import za.co.aws.welfare.viewModel.ResidenceViewModel;

public class PetActivity extends AppCompatActivity {

//TODO: set title

    private PetViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: get package

        ActivityPetBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_pet);
        mModel = ViewModelProviders.of(this).get(PetViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        setContentView(R.layout.activity_pet);
    }
}
