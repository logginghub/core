package com.logginghub.logging.frontend.brainscan;

import java.util.HashMap;
import java.util.Map;

import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

public class MutlipleThreadViewModel extends Observable {

    private ObservableList<SingleThreadViewModel> threads = createListProperty("threads", SingleThreadViewModel.class);

    private Map<Long, SingleThreadViewModel> modelsByThreadID = new HashMap<Long, SingleThreadViewModel>();

    public SingleThreadViewModel getModelForThread(long id, StackSnapshot snapshot) {
        SingleThreadViewModel threadViewModel = modelsByThreadID.get(id);
        if (threadViewModel == null) {
            threadViewModel = new SingleThreadViewModel();

            // These need to be set on the model before it goes into the observable list!
            threadViewModel.getEnvironment().set(snapshot.getEnvironment());
            threadViewModel.getHost().set(snapshot.getHost());
            threadViewModel.getInstanceType().set(snapshot.getInstanceType());
            threadViewModel.getInstanceNumber().set(snapshot.getInstanceNumber());
            
            modelsByThreadID.put(id, threadViewModel);
            threads.add(threadViewModel);
        }
        return threadViewModel;
    }

    public ObservableList<SingleThreadViewModel> getThreads() {
        return threads;
    }

}
