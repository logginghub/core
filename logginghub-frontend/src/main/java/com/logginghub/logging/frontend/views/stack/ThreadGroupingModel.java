package com.logginghub.logging.frontend.views.stack;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

public class ThreadGroupingModel extends Observable {
    private ObservableList<ThreadGroupModel> groups = createListProperty("groups", ThreadGroupModel.class);

    public ObservableList<ThreadGroupModel> getGroups() {
        return groups;
    }
}
