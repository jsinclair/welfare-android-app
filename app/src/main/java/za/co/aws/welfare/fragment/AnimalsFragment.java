package za.co.aws.welfare.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import za.co.aws.welfare.R;

public class AnimalsFragment extends Fragment {

    private TextInputLayout textInputContainer;
    private ImageButton expandImageButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.animals, container, false);

        textInputContainer = (TextInputLayout)view.findViewById(R.id.container);
        expandImageButton = (ImageButton)view.findViewById(R.id.expandImageButton);
        expandImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimalsFragment.this.onClickExpandImageButton();
            }
        });

        return view;
    }

    public void onClickExpandImageButton() {

        expandImageButton
                .setRotation(
                        textInputContainer.getVisibility() == View.GONE ?
                                180 :
                                0);

        textInputContainer.setVisibility(
                textInputContainer.getVisibility() == View.GONE ?
                        View.VISIBLE :
                        View.GONE);
    }

}
