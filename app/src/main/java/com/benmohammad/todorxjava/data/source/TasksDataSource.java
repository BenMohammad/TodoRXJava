package com.benmohammad.todorxjava.data.source;

import androidx.annotation.NonNull;

import com.benmohammad.todorxjava.data.Task;
import com.google.common.base.Optional;

import java.util.List;

import io.reactivex.Flowable;

public interface TasksDataSource {

    Flowable<List<Task>> getTasks();
    Flowable<Optional<Task>> getTask(@NonNull String taskId);

    void saveTask(@NonNull Task task);
    void completeTask(@NonNull Task task);
    void completeTask(@NonNull String taskId);
    void activateTask(@NonNull Task task);
    void activateTask(@NonNull String taskId);
    void clearCompletedTasks();
    void refreshTasks();
    void deleteAllTasks();
    void deleteTask(@NonNull String taskId);
}
