package com.benmohammad.todorxjava.tasks;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.benmohammad.todorxjava.addedittask.AddEditTaskActivity;
import com.benmohammad.todorxjava.data.Task;
import com.benmohammad.todorxjava.data.source.TasksRepository;
import com.benmohammad.todorxjava.util.EspressoIdlingResource;
import com.benmohammad.todorxjava.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import javax.annotation.Nonnegative;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.google.common.base.Preconditions.checkNotNull;

public class TasksPresenter implements TasksContract.Presenter {

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final TasksContract.View mTasksView;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @NonNull
    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private boolean mFirstLoad = true;

    @NonNull
    private CompositeDisposable mCompositeDisposable;

    public TasksPresenter(@NonNull TasksRepository tasksRepository,
                          @NonNull TasksContract.View tasksView,
                          @NonNull BaseSchedulerProvider schedulerProvider) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepo cannot be null");
        mTasksView = checkNotNull(tasksView, "tasksView cannot be null");
        mSchedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null");
        mCompositeDisposable = new CompositeDisposable();
        mTasksView.setPresenter(this);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if(AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mTasksView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        loadTasks(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    private void loadTasks(final boolean forceUpdate, final boolean showLoadingUi) {
        if(showLoadingUi) {
            mTasksView.setLoadingIndicator(true);
        }
        if(forceUpdate) {
            mTasksRepository.refreshTasks();
        }

        EspressoIdlingResource.increment();

        mCompositeDisposable.clear();
        Disposable disposable = mTasksRepository
                .getTasks()
                .flatMap(Flowable::fromIterable)
                .filter(task -> {
                    switch(mCurrentFiltering) {
                        case ACTIVE_TASKS:
                            return task.isActive();
                        case COMPLETED_TASKS:
                            return task.isCompleted();
                        case ALL_TASKS:
                        default:
                            return true;
                    }
                }).toList()
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doFinally(() -> {
                    if(!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                        EspressoIdlingResource.decrement();
                    }
                }).subscribe(tasks -> {
                        processTasks(tasks);
                        mTasksView.setLoadingIndicator(false);
                },
                        throwable -> mTasksView .showLoadingTasksError());

        mCompositeDisposable.add(disposable);
    }

    private void processTasks(@NonNull List<Task> tasks) {
        if(tasks.isEmpty()) {
            processEmptyTasks();
        } else {
            mTasksView.showTasks(tasks);
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch(mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTasksView.showActiveFilterLabel();
                break;
            case COMPLETED_TASKS:
                mTasksView.showCompletedFilterLabel();
                break;
            default:
                mTasksView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTasks() {
        switch(mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTasksView.showNoActiveTasks();
                break;
            case COMPLETED_TASKS:
                mTasksView.showNoCompletedTasks();
                break;
            default:
                mTasksView.showNoTasks();
                break;
        }
    }

    @Override
    public void addNewTask() {
        mTasksView.showAddTask();
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        checkNotNull(requestedTask, "requestedTask cannot be null");
        mTasksView.showTaskDetailsUI(requestedTask.getId());
    }

    @Override
    public void completeTasks(@NonNull Task completedTask) {
        checkNotNull(completedTask, "completedTask cannot be null");
        mTasksRepository.completeTask(completedTask);
        mTasksView.showTaskMarkedComplete();
        loadTasks(false, false);
    }

    @Override
    public void activeTask(@NonNull Task activeTask) {
        checkNotNull(activeTask, "activeTask cannot be null");
        mTasksRepository.activateTask(activeTask);
        mTasksView.showTaskMarkedActive();
        loadTasks(false, false);
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mTasksView.showCompletedTasksCleared();
        loadTasks(false, false);
    }

    @Override
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public TasksFilterType getFiltering() {
        return mCurrentFiltering;
    }

    @Override
    public void subscribe() {
        loadTasks(false);

    }

    @Override
    public void unsubscribe() {
        mCompositeDisposable.clear();
    }
}
