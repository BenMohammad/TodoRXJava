package com.benmohammad.todorxjava.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.benmohammad.todorxjava.data.source.TasksRepository;
import com.benmohammad.todorxjava.data.source.local.TasksLocalDataSource;
import com.benmohammad.todorxjava.data.source.remote.TasksRemoteDataSource;
import com.benmohammad.todorxjava.util.schedulers.BaseSchedulerProvider;
import com.benmohammad.todorxjava.util.schedulers.SchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class Injection {

    public static TasksRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        return TasksRepository.getInstance(TasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }
}