package com.benmohammad.todorxjava.addedittask;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.test.espresso.IdlingResource;

import com.benmohammad.todorxjava.R;
import com.benmohammad.todorxjava.util.ActivityUtils;
import com.benmohammad.todorxjava.util.EspressoIdlingResource;
import com.benmohammad.todorxjava.util.Injection;

public class AddEditTaskActivity extends AppCompatActivity {
    public static final int REQUEST_ADD_TASK = 100;

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY";
    private AddEditTaskPresenter mAddEditTaskPresenter;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addtask_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddEditTaskFragment fragment = (AddEditTaskFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        String taskId = getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID);

        setToolbarTitle(taskId);

        if(fragment == null) {
            fragment = AddEditTaskFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.contentFrame);


        }

        boolean shouldLoadDataFromRepo = true;

        if(savedInstanceState != null) {
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY);
        }

        mAddEditTaskPresenter = new AddEditTaskPresenter(
                taskId,
                Injection.provideTasksRepository(getApplicationContext()),
                fragment,
                shouldLoadDataFromRepo,
                Injection.provideSchedulerProvider());

    }

    private void setToolbarTitle(@Nullable String taskId) {
        if(taskId == null) {
            mActionBar.setTitle(R.string.add_task);
        } else {
            mActionBar.setTitle(R.string.edit_task);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mAddEditTaskPresenter.isDataMissing());
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @VisibleForTesting
    public IdlingResource getCountIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
