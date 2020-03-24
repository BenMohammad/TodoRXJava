package com.benmohammad.todorxjava.tasks;

import androidx.annotation.NonNull;

import com.benmohammad.todorxjava.BasePresenter;
import com.benmohammad.todorxjava.BaseView;
import com.benmohammad.todorxjava.data.Task;

import java.util.List;

public interface TasksContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);
        void showTasks(List<Task> tasks);
        void showAddTask();
        void showTaskDetailsUI(String taskId);
        void showTaskMarkedComplete();
        void showTaskMarkedActive();
        void showCompletedTasksCleared();
        void showLoadingTasksError();
        void showNoTasks();
        void showActiveFilterLabel();
        void showCompletedFilterLabel();
        void showAllFilterLabel();
        void showNoActiveTasks();
        void showNoCompletedTasks();
        void showSuccessfullySavedMessage();
        boolean isActive();
        void showFilteringPopUpMenu();
    }
    interface Presenter extends BasePresenter {
        void result(int requestCode, int resultCode);
        void loadTasks(boolean forceUpdate);
        void addNewTask();
        void openTaskDetails(@NonNull Task requestedTask);
        void completeTasks(@NonNull Task completedTask);
        void activeTask(@NonNull Task activeTask);
        void clearCompletedTasks();
        void setFiltering(TasksFilterType requestType);

        TasksFilterType getFiltering();

    }
}
