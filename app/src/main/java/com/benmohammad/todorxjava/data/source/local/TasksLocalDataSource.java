package com.benmohammad.todorxjava.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.todorxjava.data.Task;
import com.benmohammad.todorxjava.data.source.TasksDataSource;
import com.benmohammad.todorxjava.util.schedulers.BaseSchedulerProvider;
import com.google.common.base.Optional;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.List;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import static com.benmohammad.todorxjava.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED;
import static com.benmohammad.todorxjava.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION;
import static com.benmohammad.todorxjava.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID;
import static com.benmohammad.todorxjava.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE;
import static com.benmohammad.todorxjava.data.source.local.TasksPersistenceContract.TaskEntry.TABLE_NAME;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

public class TasksLocalDataSource implements TasksDataSource {

    @Nullable
    private static TasksLocalDataSource INSTANCE;
    @NonNull
    private final BriteDatabase mDatabaseHelper;
    @NonNull
    private Function<Cursor, Task> mTaskMapperFunction;

    private TasksLocalDataSource(@NonNull Context context,
                                 @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduler cannot be null");
        TasksDbHelper dbHelper = new TasksDbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mTaskMapperFunction = this::getTask;
    }

    @NonNull
    private Task getTask(@NonNull Cursor c) {
        String itemId = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_TITLE));
        String description = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION));
        boolean completed = c.getInt(c.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, itemId, completed);
    }

    public static TasksLocalDataSource getInstance(@NonNull Context context, @NonNull BaseSchedulerProvider schedulerProvider) {
        if(INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Flowable<List<Task>> getTasks() {
        String[] projection = {
                COLUMN_NAME_ENTRY_ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME);
        return mDatabaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql)
                .mapToList(mTaskMapperFunction)
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
        String[] projection = {
                COLUMN_NAME_ENTRY_ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?", TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME, COLUMN_NAME_ENTRY_ID);
        return mDatabaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql, taskId)
                .mapToOneOrDefault(cursor -> Optional.of(mTaskMapperFunction.apply(cursor)), Optional.<Task>absent())
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ENTRY_ID, task.getId());
        values.put(COLUMN_NAME_TITLE, task.getTitle());
        values.put(COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(COLUMN_NAME_COMPLETED, task.isCompleted());
        mDatabaseHelper.insert(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        completeTask(task.getId());
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_COMPLETED, true);
        String selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.update(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void activateTask(@NonNull Task task) {
        activateTask(task.getId());
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_COMPLETED, false);
        String selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.update(TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void clearCompletedTasks() {
        String selection = COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};
        mDatabaseHelper.delete(TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void deleteAllTasks() {
        mDatabaseHelper.delete(TABLE_NAME, null);
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        String selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.delete(TABLE_NAME, selection, selectionArgs);
    }
}
