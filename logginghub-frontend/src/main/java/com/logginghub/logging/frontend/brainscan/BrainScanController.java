package com.logginghub.logging.frontend.brainscan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;

public class BrainScanController {

    private static final Logger logger = Logger.getLoggerFor(BrainScanController.class);
    private CountingTreeMap countingTreeMap = new CountingTreeMap();
    private EnvironmentMessagingService messagingService;
    
    private Map<String, ThreadsInProcessViewModel> threadViewModels = new HashMap<String, ThreadsInProcessViewModel>();
    private ObservableList<ThreadsInProcessViewModel> threadViewModelsList= new ObservableList<ThreadsInProcessViewModel>(new ArrayList<ThreadsInProcessViewModel>());
    
    private ThreadGroupingModel threadGroupingModel = new ThreadGroupingModel();

    public BrainScanController(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public CountingTreeMap getCountingTreeMap() {
        return countingTreeMap;
    }

    public void sendStrobeRequest(StackStrobeRequest t) {

        ChannelMessage message = new ChannelMessage(Channels.stackStrobeRequests, t);
        try {
            messagingService.send(message);
        }
        catch (LoggingMessageSenderException e) {
            logger.warning(e, "Failed to send strobe request '{}'", message);
        }

    }

    public ThreadsInProcessViewModel getThreadViewModel(String key) {
        return threadViewModels.get(key);
    }

    public ObservableList<ThreadsInProcessViewModel> getThreadViewModelsList() {
        return threadViewModelsList;
    }
    
    public synchronized void processStackTrace(StackSnapshot snapshot, StackTrace stackTrace) {
        long threadID = stackTrace.getThreadID();
        
        String instanceKey = snapshot.buildKey();
        ThreadsInProcessViewModel threadViewModel = threadViewModels.get(instanceKey);
        if(threadViewModel == null) {
            threadViewModel = new ThreadsInProcessViewModel();
            threadViewModels.put(instanceKey, threadViewModel);
            threadViewModelsList.add(threadViewModel);
        }
        
        SingleThreadViewModel modelForThread = threadViewModel.getModelForThread(threadID, snapshot);
        
//        modelForThread.getEnvironment().set(snapshot.getEnvironment());
//        modelForThread.getHost().set(snapshot.getHost());
//        modelForThread.getInstanceType().set(snapshot.getInstanceType());
//        modelForThread.getInstanceNumber().set(snapshot.getInstanceNumber());
        
        modelForThread.getName().set(stackTrace.getThreadName());
        modelForThread.getStack().set(stackTrace.formatStack());
        modelForThread.getState().set(stackTrace.getThreadState());
        
    }

    public ThreadGroupingModel getThreadGroupingModel() {
        return threadGroupingModel;
         
    }

    public synchronized void clearData() {
        threadViewModelsList.clear();
        threadViewModels.clear();
    }
}
