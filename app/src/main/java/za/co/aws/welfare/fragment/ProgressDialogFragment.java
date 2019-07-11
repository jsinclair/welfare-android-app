package za.co.aws.welfare.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import za.co.aws.welfare.R;

/**
 * Created by zynique on 2018/06/11.
 *
 * Create a progress dialog displaying the given message. Dialog style defined in styles.xml.
 */
public class ProgressDialogFragment extends DialogFragment {

    /**
     * Create a new instance of the Dialog Fragment which will display a PROGRESS dialog. A progress
     * dialog is not cancelable.
     *
     * @param message The message that should be displayed.
     * @return A new AlertDialogFragment.
     */
    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment frag = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("msg", message);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String message = getArguments().getString("msg");
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);
        View v = inflater.inflate(R.layout.fragment_progress_dialog, container, false);

        TextView messageArea = v.findViewById(R.id.message);
        messageArea.setText(message);

        return v;

    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
