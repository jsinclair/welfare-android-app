package za.co.aws.welfare.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.LinkedList;

import za.co.aws.welfare.R;
import za.co.aws.welfare.customComponents.ResidenceSearchListAdapter;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.viewModel.PetViewModel;

/**
 * This fragment is strictly used from the Pet Activity to MOVE a pet from one residence to another.
 * It should display residences based on the user's search result. On residence picked, it should return the
 * residence id to the pet activity.
 */
public class SearchResidenceFragment extends DialogFragment {

    private LinearLayout searchView;
    private Button searchButton;
    private ListView results;

    private PetViewModel mModel;
    private TextInputEditText mAddress;
    private TextInputEditText mShack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle("Search and Choose a residence");
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        final View v = inflater.inflate(R.layout.search_residences, container, false);

        mModel = ViewModelProviders.of(getActivity()).get(PetViewModel.class);

        mAddress = v.findViewById(R.id.address);
        mShack = v.findViewById(R.id.shack);
        results = v.findViewById(R.id.result_residences);
        searchView = v.findViewById(R.id.search_menu);

        searchButton = v.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = mAddress.getText() == null ? null : mAddress.getText().toString();
                String shack = mShack.getText() == null ? null : mShack.getText().toString();
                mModel.doResidenceSearch(address, shack);
            }
        });

        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
//
//        v.findViewById(R.id.add_residence).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mModel.triggerAddResident();
//            }
//        });
//
//        //TODO: check tht this is the corect way to observe from a fragment (to avoid memory leaks)
        mModel.getResidenceSearchResult().observe(getViewLifecycleOwner(), new Observer<LinkedList<ResidenceSearchData>>() {
            @Override
            public void onChanged(final LinkedList<ResidenceSearchData> residenceSearchData) {
                LinearLayout emptyView = v.findViewById(R.id.empty_view);
                if (residenceSearchData != null && !residenceSearchData.isEmpty()) {
                    emptyView.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
                    results.setAdapter(new ResidenceSearchListAdapter(getContext(), R.layout.content_residence_search_entry, residenceSearchData));
                    results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            ResidenceSearchData sel = ((ResidenceSearchData) results.getAdapter().getItem(i));
                            mModel.setResidence(sel.getID(), sel.getStreetAddress());
                            dismiss();
//                            mModel.triggerViewResident(((ResidenceSearchData) results.getAdapter().getItem(i)).getID());
                        }
                    });
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    results.setVisibility(View.GONE);
                }
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
