package com.logginghub.logging.frontend.views.stack;

import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class ThreadsInProcessViewModel extends Observable {

    private ObservableList<SingleThreadViewModel> threads = createListProperty("threads", SingleThreadViewModel.class);

    private Map<Long, SingleThreadViewModel> modelsByThreadID = new HashMap<Long, SingleThreadViewModel>();

    public synchronized SingleThreadViewModel getModelForThread(long id, StackSnapshot snapshot) {
        SingleThreadViewModel threadViewModel = modelsByThreadID.get(id);
        if (threadViewModel == null) {
            threadViewModel = new SingleThreadViewModel();

            // These need to be set on the model before it goes into the observable list!
            threadViewModel.getEnvironment().set(snapshot.getInstanceKey().getEnvironment());
            threadViewModel.getHost().set(snapshot.getInstanceKey().getHost());
            threadViewModel.getInstanceType().set(snapshot.getInstanceKey().getInstanceType());
            threadViewModel.getInstanceIdentifier().set(snapshot.getInstanceKey().getInstanceIdentifier());
            
            modelsByThreadID.put(id, threadViewModel);
            threads.add(threadViewModel);
        }
        return threadViewModel;
    }

    public ObservableList<SingleThreadViewModel> getThreads() {
        return threads;
    }

}
