package com.benmohammad.todorxjava.taskdetail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.todorxjava.data.Task;
import com.benmohammad.todorxjava.data.source.TasksRepository;
import com.benmohammad.todorxjava.util.schedulers.BaseSchedulerProvider;
import com.google.common.base.Optional;
import com.google.common.base.Strings;


import io.reactivex.disposables.CompositeDisposable;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskDetailPresenter implements TaskDetailContract.Presenter {

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final TaskDetailContract.View mTaskDetailView;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @Nullable private String mTaskId;

    @NonNull
    private CompositeDisposable compositeDisposable;

    public TaskDetailPresenter(@Nullable String taskId,
                               @NonNull TasksRepository tasksRepository,
                               @NonNull TaskDetailContract.View taskDetailView,
                               @NonNull BaseSchedulerProvider schedulerProvider) {
        this.mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository, "tasks repo cannot be null");
        mTaskDetailView = checkNotNull(taskDetailView, "tasks detail view cannot be null");
        mSchedulerProvider = checkNotNull(schedulerProvider, "scheduler cannot be null");
        compositeDisposable = new CompositeDisposable();
        mTaskDetailView.setPresenter(this);
    }


    @Override
    public void editTask() {
        if(Strings.isNullOrEmpty(mTaskId)) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.showEditTask(mTaskId);
    }

    @Override
    public void deleteTask() {
        if(Strings.isNullOrEmpty(mTaskId)) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.deleteTask(mTaskId);
        mTaskDetailView.showTaskDeleted();

    }

    @Override
    public void completeTask() {
        if(Strings.isNullOrEmpty(mTaskId)) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.completeTask(mTaskId);
        mTaskDetailView.showTaskMarkedComplete();
    }

    @Override
    public void activateTask() {
        if(Strings.isNullOrEmpty(mTaskId)) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.activateTask(mTaskId);
        mTaskDetailView.showTaskMarkedActive();
    }

    @Override
    public void subscribe() {
        openTask();
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    private void openTask() {
        if(Strings.isNullOrEmpty(mTaskId)) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.setLoadingIndicator(true);
        compositeDisposable.add(mTasksRepository
        .getTask(mTaskId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .subscribeOn(mSchedulerProvider.computation())
        .observeOn(mSchedulerProvider.ui())
        .subscribe(
                this::showTask,
                throwable -> {
                },
                () -> mTaskDetailView.setLoadingIndicator(false)));


    }

    private void showTask(@NonNull Task task) {
        String title = task.getTitle();
        String description = task.getDescription();
        if(Strings.isNullOrEmpty(title)) {
            mTaskDetailView.hideTitle();
        } else {
            mTaskDetailView.showTitle(title);
        }

        if(Strings.isNullOrEmpty(description)) {
            mTaskDetailView.hideDescription();
        } else {
            mTaskDetailView.showDescription(description);
        }

        mTaskDetailView.showCompletionStatus(task.isCompleted());
    }
}
