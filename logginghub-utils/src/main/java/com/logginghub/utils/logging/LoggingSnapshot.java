package com.logginghub.utils.logging;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingSnapshot
{

    private Map<String, Level> m_loggingLevels = new HashMap<String, Level>();
    private Map<Handler, Level> m_handlerLevels = new HashMap<Handler, Level>();
    private Handler[] rootHandlers;

    public void capture()
    {
        LogManager logManager = LogManager.getLogManager();
        Enumeration<String> loggerNames = logManager.getLoggerNames();
        while (loggerNames.hasMoreElements())
        {
            String name = loggerNames.nextElement();

            Logger logger = logManager.getLogger(name);
            if (logger != null)
            {
                Level level = logger.getLevel();
                if (level != null)
                {
                    m_loggingLevels.put(name, level);
                }

                Handler[] handlers = logger.getHandlers();
                for (Handler handler : handlers)
                {
                    Level handlerLevel = handler.getLevel();
                    if (handlerLevel != null)
                    {
                        m_handlerLevels.put(handler, handlerLevel);
                    }
                }
            }
        }
        
        // Dirty hack in case someone switches out a different implementation
        rootHandlers = Logger.getLogger("").getHandlers();
    }

    public void apply()
    {
        Map<Handler, Level> handlerLevels = m_handlerLevels;
        Set<Handler> keySet = handlerLevels.keySet();
        for (Handler handler : keySet)
        {
            Level level = handlerLevels.get(handler);
            handler.setLevel(level);
        }
                
        Map<String, Level> loggingLevels = m_loggingLevels;
        Set<String> loggerNames = loggingLevels.keySet();
        for (String string : loggerNames)
        {
            Level level = m_loggingLevels.get(string);
            Logger.getLogger(string).setLevel(level);
        }
        
        
        // Reinstate the root handlers
        Logger logger = Logger.getLogger("");
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers)
        {
            logger.removeHandler(handler);
        }
        
        for (Handler handler : rootHandlers)
        {
            logger.addHandler(handler);
        }
       
    }

}
