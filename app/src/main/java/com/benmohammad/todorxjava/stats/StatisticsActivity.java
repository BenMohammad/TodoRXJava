package com.benmohammad.todorxjava.stats;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.benmohammad.todorxjava.R;
import com.benmohammad.todorxjava.util.ActivityUtils;
import com.benmohammad.todorxjava.util.Injection;
import com.google.android.material.navigation.NavigationView;

public class StatisticsActivity extends AppCompatActivity {


    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.statistics_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setHomeButtonEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);

        if(navigationView != null) {
            setupDrawerContent(navigationView);
        }

        StatisticsFragment fragment = (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(fragment == null) {
            fragment = StatisticsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.contentFrame);
        }

        new StatisticsPresenter(
                Injection.provideTasksRepository(getApplicationContext()),
                fragment, Injection.provideSchedulerProvider()
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    switch(menuItem.getItemId()) {
                        case R.id.list_navigation_menu_item:
                            NavUtils.navigateUpFromSameTask(StatisticsActivity.this);
                            break;
                        case R.id.statistics_navigation_menu_item:
                            break;
                        default:
                            break;
                    }

                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    return true;
                }
        );
    }
}
