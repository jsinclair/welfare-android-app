package za.co.aws.welfare.activity;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityLoginBinding;
import za.co.aws.welfare.databinding.ActivityViewResidentBinding;
import za.co.aws.welfare.databinding.ResidencesBinding;
import za.co.aws.welfare.viewModel.LoginViewModel;
import za.co.aws.welfare.viewModel.ResidenceViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

/** Allows the user to view and, if they have permission, edit a residence. */
public class ResidentActivity extends AppCompatActivity {

    private ResidenceViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityViewResidentBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_view_resident);
        mModel = ViewModelProviders.of(this).get(ResidenceViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);
    }
}
