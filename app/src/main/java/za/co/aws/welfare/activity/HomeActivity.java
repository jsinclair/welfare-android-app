package za.co.aws.welfare.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.AnimalsFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.fragment.RemindersFragment;
import za.co.aws.welfare.fragment.ResidencesFragment;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.HomeViewModel;

/** Contains the 3 search fragments. */
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "HomeActivity";
    public static final String PROGRESS_DIALOG_TAG = "PROGRESS_DIALOG_TAG";
    public static final String ALERT_DIALOG_TAG = "ALERT_DIALOG_TAG";
    private HomeViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set default fragment
        if (savedInstanceState == null) {
            Fragment newFragment = new AnimalsFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, newFragment);
            ft.addToBackStack(null);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set the full name and organisation name
        View headerLayout = navigationView.getHeaderView(0);
        ((TextView)headerLayout.findViewById(R.id.navUserName)).
                setText(((WelfareApplication)this.getApplication()).getFullName());
        ((TextView)headerLayout.findViewById(R.id.navOrganisationName)).
                setText(((WelfareApplication)this.getApplication()).getOrganisationName());

        mModel.getNetworkHandler().observe(this, new Observer<HomeViewModel.NetworkStatus>() {
            @Override
            public void onChanged(HomeViewModel.NetworkStatus networkStatus) {
                if (networkStatus != null) {
                    handleNetworkStatus(networkStatus);
                }
            }
        });

        mModel.getEventHandler().observe(this, new Observer<Pair<HomeViewModel.Event, String>>() {
            @Override
            public void onChanged(Pair<HomeViewModel.Event, String> eventData) {
                if (eventData != null) {
                    handleEvent(eventData);
                }
            }
        });

        mModel.getNavigationHandler().observe(this, new Observer<Pair<HomeViewModel.Navigate, Integer>>() {
            @Override
            public void onChanged(Pair<HomeViewModel.Navigate, Integer> eventData) {
                navigate(eventData);
            }
        });

    }

    // Used to navigate to other activities
    private void navigate(Pair<HomeViewModel.Navigate, Integer> data) {
        if (data != null && data.first != null && data.second != null) {
            switch (data.first) {
                case RESIDENCE:
                    Intent intent = new Intent(this, ResidentActivity.class);
                    intent.putExtra("ResidentID", data.second);
                    intent.putExtra("RequestNewEntry", false);
                    startActivity(intent);
                    break;
                case ADD_RESIDENCE:
                    Intent addIntent = new Intent(this, ResidentActivity.class);
                    addIntent.putExtra("RequestNewEntry", true);
                    startActivity(addIntent);
                    break;
                case ANIMAL:
                    break;
            }
        }
    }

    /** Handle once network events. */
    private void handleNetworkStatus(HomeViewModel.NetworkStatus status) {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) fm.findFragmentByTag(PROGRESS_DIALOG_TAG);
        switch (status) { //TODO: REPLACE WITH UPDATING PROGRESS DIALOG!!!!!
            case IDLE:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case SEARCHING_RESIDENCE:
                ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getString(R.string.search_residence));
                Utils.showDialog(fm, progress, PROGRESS_DIALOG_TAG, false);
                break;

        }
    }

    /** Handle one time events triggered from the model. */
    private void handleEvent(Pair<HomeViewModel.Event, String> eventData) {
        switch (eventData.first) {
            case SEARCH_RES_ERROR:
                showAlert(getString(R.string.download_err), eventData.second);
                break;
            case SEARCH_RES_DATA_REQ:
                showAlert(getString(R.string.data_required), eventData.second);
                break;
        }
    }

    // Convenience method to show an alert dialog.
    private void showAlert(String title, String message) {
        FragmentManager fm = getSupportFragmentManager();
        AlertDialogFragment alert = AlertDialogFragment.newInstance(title, message);
        Utils.showDialog(fm, alert, ALERT_DIALOG_TAG, true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_animals) {
            // Handle the camera action
            Log.i(TAG, "onNavigationItemSelected: nav_animals");
            fragment = new AnimalsFragment();
        } else if (id == R.id.nav_residences) {
            Log.i(TAG, "onNavigationItemSelected: nav_residences");
            fragment = new ResidencesFragment();
        } else if (id == R.id.nav_reminders) {
            Log.i(TAG, "onNavigationItemSelected: nav_reminders");
            fragment = new RemindersFragment();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
