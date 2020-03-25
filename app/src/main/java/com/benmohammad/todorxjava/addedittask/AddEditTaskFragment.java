package com.benmohammad.todorxjava.addedittask;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.benmohammad.todorxjava.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTaskFragment extends Fragment implements AddEditTaskContract.View {

    public static final String ARGUMENT_EDIT_TASK_ID = "";

    private AddEditTaskContract.Presenter mPresenter;

    private TextView mTitle;
    private TextView mDescription;

    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addtask_frag, container, false);
        mTitle = root.findViewById(R.id.add_task_title);
        mDescription = root.findViewById(R.id.add_task_description);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(v -> mPresenter.saveTask(mTitle.getText().toString(),
                mDescription.getText().toString()));
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


    @Override
    public void showEmptyTaskError() {
        Snackbar.make(getView(), getString(R.string.empty_task_message), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showTasksList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setDescription(String description) {
        mDescription.setText(description);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setPresenter(AddEditTaskContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }
}
