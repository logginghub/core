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
    
    private Map<String, MutlipleThreadViewModel> threadViewModels = new HashMap<String, MutlipleThreadViewModel>();
    private ObservableList<MutlipleThreadViewModel> threadViewModelsList= new ObservableList<MutlipleThreadViewModel>(new ArrayList<MutlipleThreadViewModel>());
    
    private ThreadGroupingModel threadGroupingModel = new ThreadGroupingModel();

    public BrainScanController(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public CountingTreeMap getCountingTreeMap() {
        return countingTreeMap;
    }

    public void sendStrobeRequest(StackStrobeRequest t) {

        ChannelMessage message = new ChannelMessage(Channels.strobeRequests, t);
        try {
            messagingService.send(message);
        }
        catch (LoggingMessageSenderException e) {
            logger.warning(e, "Failed to send strobe request '{}'", message);
        }

    }

    public MutlipleThreadViewModel getThreadViewModel(String key) {
        return threadViewModels.get(key);
    }

    public ObservableList<MutlipleThreadViewModel> getThreadViewModelsList() {
        return threadViewModelsList;
    }
    
    public void processStackTrace(StackSnapshot snapshot, StackTrace stackTrace) {
        long threadID = stackTrace.getThreadID();
        
        String instanceKey = snapshot.buildKey();
        MutlipleThreadViewModel threadViewModel = threadViewModels.get(instanceKey);
        if(threadViewModel == null) {
            threadViewModel = new MutlipleThreadViewModel();
            threadViewModels.put(instanceKey, threadViewModel);
            threadViewModelsList.add(threadViewModel);
        }
        
        SingleThreadViewModel modelForThread = threadViewModel.getModelForThread(threadID, snapshot);
        
        modelForThread.getEnvironment().set(snapshot.getEnvironment());
        modelForThread.getHost().set(snapshot.getHost());
        modelForThread.getInstanceType().set(snapshot.getInstanceType());
        modelForThread.getInstanceNumber().set(snapshot.getInstanceNumber());
        
        modelForThread.getName().set(stackTrace.getThreadName());
        modelForThread.getStack().set(stackTrace.formatStack());
        modelForThread.getState().set(stackTrace.getThreadState());
        
    }

    public ThreadGroupingModel getThreadGroupingModel() {
        return threadGroupingModel;
         
    }

    public void clearData() {
        threadViewModelsList.clear();
        threadViewModels.clear();
    }
}
