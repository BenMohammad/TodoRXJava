package com.benmohammad.todorxjava.data.source;

import android.graphics.Path;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.benmohammad.todorxjava.data.Task;
import com.benmohammad.todorxjava.data.source.remote.TasksRemoteDataSource;
import com.google.common.base.Optional;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;

import io.reactivex.Flowable;

import static com.google.common.base.Preconditions.checkNotNull;

public class TasksRepository implements TasksDataSource {

    @Nullable
    private static TasksRepository INSTANCE = null;

    @NonNull
    private final TasksDataSource mTasksRemoteDataSource;

    @NonNull
    private final TasksDataSource mTasksLocalDataSource;

    @VisibleForTesting
    @Nullable
    Map<String, Task> mCachedTasks;

    @VisibleForTesting
    boolean mCacheIsDirty = false;

    private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                            @NonNull TasksDataSource tasksLocalDataSource) {
        mTasksLocalDataSource = tasksLocalDataSource;
        mTasksRemoteDataSource = tasksRemoteDataSource;
    }

    public static TasksRepository getInstance(@NonNull TasksDataSource tasksRemoteDataSource,
                                              @NonNull TasksDataSource tasksLocalDataSource) {
        if(INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }

        return INSTANCE;
    }


    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Flowable<List<Task>> getTasks() {
        if(mCachedTasks != null && !mCacheIsDirty) {
            return Flowable.fromIterable(mCachedTasks.values()).toList().toFlowable();
        } else if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        Flowable<List<Task>> remoteTasks = getAndSaveRemoteTasks();
        if(mCacheIsDirty) {
            return remoteTasks;
        } else {
            Flowable<List<Task>> localTasks = getAndCacheLocalTasks();
            return Flowable.concat(localTasks, remoteTasks)
                    .filter(tasks -> !tasks.isEmpty())
                    .firstOrError()
                    .toFlowable();
        }
    }

    private Flowable<List<Task>> getAndCacheLocalTasks() {
        return mTasksLocalDataSource.getTasks()
                .flatMap(tasks -> Flowable.fromIterable(tasks)
                        .doOnNext(task -> mCachedTasks.put(task.getId(), task))
                        .toList()
                        .toFlowable());
    }

    private Flowable<List<Task>> getAndSaveRemoteTasks() {
        return mTasksRemoteDataSource.getTasks()
                .flatMap(tasks -> Flowable.fromIterable(tasks)
                .doOnNext(task -> {
                    mTasksLocalDataSource.saveTask(task);
                    mCachedTasks.put(task.getId(), task);

                }).toList().toFlowable())
                        .doOnComplete(() -> mCacheIsDirty = false);
    }

    @Override
    public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
        checkNotNull(taskId);

        final Task cachedTask = getTaskWithId(taskId);

        if(cachedTask != null) {
            return Flowable.just(Optional.of(cachedTask));
        }

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        Flowable<Optional<Task>> localTask = getTaskWithIdFromLocalRepository(taskId);
        Flowable<Optional<Task>> remoteTask = mTasksRemoteDataSource
                .getTask(taskId)
                .doOnNext(taskOptional -> {
                    if(taskOptional.isPresent()) {
                        Task task = taskOptional.get();
                        mTasksLocalDataSource.saveTask(task);
                        mCachedTasks.put(task.getId(), task);
                    }
                });
        return Flowable.concat(localTask, remoteTask)
                .firstElement()
                .toFlowable();
    }



    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.saveTask(task);
        mTasksLocalDataSource.saveTask(task);

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.completeTask(task);
        mTasksLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), completedTask);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        Task taskWithId = getTaskWithId(taskId);
        if(taskWithId != null) {
            completeTask(taskWithId);
        }
    }

    @Override
    public void activateTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.activateTask(task);
        mTasksLocalDataSource.activateTask(task);

        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        mCachedTasks.put(task.getId(), activeTask);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        Task taskWithId = getTaskWithId(taskId);
        if(taskWithId != null) {
            activateTask(taskWithId);
        }
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks();
        mTasksLocalDataSource.clearCompletedTasks();

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if(entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    @Override
    public void refreshTasks() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks();
        mTasksLocalDataSource.deleteAllTasks();

        if(mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }

        mCachedTasks.clear();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId));

        mCachedTasks.remove(taskId);
    }

    @Nullable
    private Task getTaskWithId(@NonNull String id) {
        checkNotNull(id);
        if(mCachedTasks == null || mCachedTasks.isEmpty()) {
            return null;
        } else {
            return mCachedTasks.get(id);
        }
    }

    @NonNull
    Flowable<Optional<Task>> getTaskWithIdFromLocalRepository(@NonNull final String taskId) {
        return mTasksLocalDataSource
                .getTask(taskId)
                .doOnNext(taskOptional -> {
                    if(taskOptional.isPresent()) {
                        mCachedTasks.put(taskId, taskOptional.get());
                    }
                }).firstElement().toFlowable();
    }
}
