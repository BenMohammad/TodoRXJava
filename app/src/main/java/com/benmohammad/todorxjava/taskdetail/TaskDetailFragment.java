package com.benmohammad.todorxjava.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.benmohammad.todorxjava.R;
import com.benmohammad.todorxjava.addedittask.AddEditTaskActivity;
import com.benmohammad.todorxjava.addedittask.AddEditTaskFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskDetailFragment extends Fragment implements TaskDetailContract.View {

    @NonNull
    private static final String ARGUMENT_TASK_ID = "TASK_ID";

    @NonNull
    private static final int REQUEST_EDIT_TASK = 1;

    private TaskDetailContract.Presenter mPresenter;
    private TextView mDetailTitle;
    private TextView mDetailDescription;
    private CheckBox mDetailCompleteStatus;

    public static TaskDetailFragment newInstance(@Nullable String taskId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_TASK_ID, taskId);
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tasksdetails_frag, container, false);
        setHasOptionsMenu(true);
        mDetailTitle = root.findViewById(R.id.task_detail_title);
        mDetailDescription = root.findViewById(R.id.task_detail_description);
        mDetailCompleteStatus = root.findViewById(R.id.task_detail_complete);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab_edit_task);

        fab.setOnClickListener(v -> mPresenter.editTask());

        return root;
    }


    @Override
    public void setLoadingIndicator(boolean active) {
        if(active) {
            mDetailTitle.setText("");
            mDetailDescription.setText(getString(R.string.loading));

        }
    }

    @Override
    public void showMissingTask() {
        mDetailTitle.setText("");
        mDetailDescription.setText(getString(R.string.no_data));
    }

    @Override
    public void hideTitle() {
        mDetailTitle.setVisibility(View.GONE);
    }

    @Override
    public void showTitle(String title) {
        mDetailTitle.setVisibility(View.VISIBLE);
        mDetailTitle.setText(title);
    }

    @Override
    public void hideDescription() {
        mDetailDescription.setVisibility(View.GONE);
    }

    @Override
    public void showDescription(String description) {
        mDetailDescription.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCompletionStatus(boolean complete) {
        Preconditions.checkNotNull(mDetailCompleteStatus);

        mDetailCompleteStatus.setChecked(complete);
        mDetailCompleteStatus.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if(isChecked) {
                        mPresenter.completeTask();
                    } else {
                        mPresenter.activateTask();
                    }
                }
        );
    }

    @Override
    public void showEditTask(String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void showTaskDeleted() {
        getActivity().finish();
    }

    @Override
    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_EDIT_TASK) {
            if(resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setPresenter(TaskDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                mPresenter.deleteTask();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
