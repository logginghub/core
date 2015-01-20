package com.logginghub.logging.receivers;

import com.logginghub.logging.natives.Win32ConsoleAPI;
import com.logginghub.utils.WorkerThread;

public class ConsoleInput
{
    private Win32ConsoleAPI m_consoleAPI = new Win32ConsoleAPI();

    public ConsoleInput(final ConsoleInputListener listener)
    {
        WorkerThread thread = new WorkerThread("fooo")
        {
            @Override protected void onRun() throws Throwable
            {
                char c = (char)m_consoleAPI.getch();
                listener.onNewChar(c);
            }
        };
        thread.start();
    }

}
