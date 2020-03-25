package com.benmohammad.todorxjava.addedittask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.todorxjava.data.Task;
import com.benmohammad.todorxjava.data.source.TasksDataSource;
import com.benmohammad.todorxjava.util.schedulers.BaseSchedulerProvider;

import javax.annotation.Nonnegative;

import io.reactivex.disposables.CompositeDisposable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTaskPresenter implements AddEditTaskContract.Presenter {

    @NonNull
    private final TasksDataSource mTasksRepository;

    @NonNull
    private  final AddEditTaskContract.View mAddTaskView;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @Nullable
    private String mTaskId;

    private boolean mIsDataMissing;

    @NonNull
    private CompositeDisposable compositeDisposable;

    public AddEditTaskPresenter(@Nullable String taskId,
                                @NonNull TasksDataSource tasksRepository,
                                @NonNull AddEditTaskContract.View addTaskView,
                                boolean shouldLoadDataFromRepo,
                                @NonNull BaseSchedulerProvider schedulerProvider) {
        mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository);
        mAddTaskView = checkNotNull(addTaskView);
        mIsDataMissing = shouldLoadDataFromRepo;
        mSchedulerProvider = checkNotNull(schedulerProvider, "scheduler cannot be null");
        compositeDisposable = new CompositeDisposable();
        mAddTaskView.setPresenter(this);
    }

    @Override
    public void saveTask(String title, String description) {
        if(isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    @Override
    public void populateTask() {
        if(isNewTask()) {
            throw new RuntimeException("populate task was called but task is new");
        }

        compositeDisposable.add(mTasksRepository
        .getTask(mTaskId)
        .subscribeOn(mSchedulerProvider.computation())
        .observeOn(mSchedulerProvider.ui())
        .subscribe(
                taskOptional -> {
                    if(taskOptional.isPresent()) {
                        Task task = taskOptional.get();
                        if(mAddTaskView.isActive()) {
                            mAddTaskView.setTitle(task.getTitle());
                            mAddTaskView.setDescription(task.getDescription());

                            mIsDataMissing = false;
                        }
                    } else {
                        if(mAddTaskView.isActive()) {
                            mAddTaskView.showEmptyTaskError();
                        }
                    }
                },throwable -> {
                    if(mAddTaskView.isActive()) {
                        mAddTaskView.showEmptyTaskError();
                    }
                }));
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    @Override
    public void subscribe() {
        if(!isNewTask() && mIsDataMissing) {
            populateTask();
        }
    }


    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }



    public boolean isNewTask() {
        return mTaskId == null;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if(newTask.isEmpty()) {
            mAddTaskView.showEmptyTaskError();
        } else {
            mTasksRepository.saveTask(newTask);
            mAddTaskView.showTasksList();
        }
    }

    private void updateTask(String title, String description) {
        if(isNewTask()) {
            throw new RuntimeException("updateTask was called but task is null");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        mAddTaskView.showTasksList();
    }
}
