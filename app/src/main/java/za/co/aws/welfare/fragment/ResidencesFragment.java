package za.co.aws.welfare.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import za.co.aws.welfare.R;
import za.co.aws.welfare.viewModel.HomeViewModel;

public class ResidencesFragment extends Fragment {

    private LinearLayout searchView;
    private FloatingActionButton expandButton;
    private Button searchButton;

    private HomeViewModel mModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.residences, container, false);
        searchView = v.findViewById(R.id.search_menu);
        searchButton = v.findViewById(R.id.search_button);
        expandButton = v.findViewById(R.id.expand_button);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    searchView.setVisibility(View.VISIBLE);
                    expandButton.setVisibility(View.GONE);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: START SEARCH HERE> ONLY Hide search menu on successful search!!
                searchView.setVisibility(View.GONE);
                expandButton.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }
}
