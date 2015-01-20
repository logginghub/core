package com.logginghub.logging.frontend.monitoringbus;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.HTMLBuilder;
import com.logginghub.web.JettyLauncher;
import com.logginghub.web.WebController;

@WebController(staticFiles = "/static/monitoringbus", defaultUrl = "index")
public class WebView {

    private List<WebRenderable> renderables = new ArrayList<WebRenderable>();

    public void add(WebRenderable renderable) {
        renderables.add(renderable);
    }
    
    public String index() {
        HTMLBuilder builder = new HTMLBuilder();
        for (WebRenderable renderable : renderables) {
            renderable.render(builder);
        }
        builder.close();
        return builder.toString();
    }
    
    public void start() {
        try {
            JettyLauncher.launchBlocking(this, 8080);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
