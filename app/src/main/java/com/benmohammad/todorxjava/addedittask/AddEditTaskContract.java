package com.benmohammad.todorxjava.addedittask;

import com.benmohammad.todorxjava.BasePresenter;
import com.benmohammad.todorxjava.BaseView;

public interface AddEditTaskContract {

    interface View extends BaseView<Presenter> {

        void showEmptyTaskError();
        void showTasksList();
        void setTitle(String title);
        void setDescription(String description);
        boolean isActive();


    }

    interface Presenter extends BasePresenter {
        void saveTask(String title, String description);
        void populateTask();
        boolean isDataMissing();
    }
}
