package com.logginghub.utils.file;

import java.io.File;

public interface FileSearchListener
{
    void onFileFound(File file);
    void onFileIgnored(File file, String reason);    
    void onFolderIgnored(File file, String reason);
}
