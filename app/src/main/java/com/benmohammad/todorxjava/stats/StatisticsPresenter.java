package com.benmohammad.todorxjava.stats;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.benmohammad.todorxjava.data.Task;
import com.benmohammad.todorxjava.data.source.TasksRepository;
import com.benmohammad.todorxjava.util.EspressoIdlingResource;
import com.benmohammad.todorxjava.util.schedulers.BaseSchedulerProvider;
import com.google.common.primitives.Ints;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatisticsPresenter implements StatisticsContract.Presenter {

    @NonNull
    private final TasksRepository mTasksRepository;

    @Nonnull
    private final StatisticsContract.View mStatisticsView;

    @Nonnull
    private final BaseSchedulerProvider mSchedulerProvider;

    @Nonnull
    private CompositeDisposable compositeDisposable;

    public StatisticsPresenter(@Nonnull TasksRepository tasksRepository,
                               @Nonnull StatisticsContract.View statisticsView,
                               @Nonnull BaseSchedulerProvider schedulerProvider) {
        mTasksRepository = checkNotNull(tasksRepository, "repo cannot be null");
        mStatisticsView = checkNotNull(statisticsView, "view cannot be null");
        mSchedulerProvider = checkNotNull(schedulerProvider, "schedulers cannot be null");
        compositeDisposable = new CompositeDisposable();
        mStatisticsView.setPresenter(this);
    }



    @Override
    public void subscribe() {
        loadStatistics();
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    private void loadStatistics() {
        mStatisticsView.setProgressIndicator(true);
        EspressoIdlingResource.increment();

        Flowable<Task> tasks = mTasksRepository
                .getTasks()
                .flatMap(Flowable::fromIterable);

        Flowable<Long> completedTasks = tasks.filter(Task::isCompleted).count().toFlowable();
        Flowable<Long> activeTasks = tasks.filter(Task::isActive).count().toFlowable();

        Disposable disposable = Flowable
                .zip(completedTasks, activeTasks, (completed, active) -> Pair.create(active, completed))
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .doFinally(() -> {
                    if(!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                        EspressoIdlingResource.decrement();
                    }
                })
                .subscribe(
                        stats -> mStatisticsView.showStatistics(Ints.saturatedCast(stats.first), Ints.saturatedCast(stats.second)),
                        throwable -> mStatisticsView.showLoadingStatisticsError(),
                        () -> mStatisticsView.setProgressIndicator(false));

        compositeDisposable.add(disposable);

    }
}
