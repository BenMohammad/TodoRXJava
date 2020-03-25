package com.benmohammad.todorxjava.stats;

import com.benmohammad.todorxjava.BasePresenter;
import com.benmohammad.todorxjava.BaseView;

public interface StatisticsContract {

    interface View extends BaseView<Presenter> {

        void setProgressIndicator(boolean isActive);
        void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTask);
        void showLoadingStatisticsError();
        boolean isActive();

    }

    interface Presenter extends BasePresenter {

    }
}
