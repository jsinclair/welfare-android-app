package za.co.aws.welfare.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityViewResidentBinding;
import za.co.aws.welfare.viewModel.ResidenceViewModel;

/** Allows the user to view and, if they have permission, edit a residence. */
public class ResidentActivity extends AppCompatActivity {

    private ResidenceViewModel mModel;

    private TextInputLayout mAddress;
    private TextInputLayout mShackID;
    private TextInputLayout mNotes;

    private FloatingActionButton mEditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityViewResidentBinding binding =  DataBindingUtil.setContentView(this, R.layout.activity_view_resident);
        mModel = ViewModelProviders.of(this).get(ResidenceViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

        mModel.getEditMode().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    setEditable(aBoolean);
                }
            }
        });

        mAddress = findViewById(R.id.address_container);
        mShackID = findViewById(R.id.shack_container);
        mNotes = findViewById(R.id.notes_container);
        mEditButton = findViewById(R.id.edit);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModel.toggleEditMode();
            }
        });
    }

    private void setEditable(boolean editable) {
        mAddress.setEnabled(editable);
        mShackID.setEnabled(editable);
        mNotes.setEnabled(editable);
        if (editable) {
            mEditButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_save_white_24));
        } else {
            mEditButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_edit_white_24));
        }
    }
}
