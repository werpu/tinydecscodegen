package net.werpu.tools.supportive.fs.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface GistListener  extends EventListener {
    Topic<GistListener> FILE_NOT_REACHABLE =
            new Topic<GistListener>("gist events", GistListener.class, Topic.BroadcastDirection.TO_PARENT);


    public void fileNotReachable(VirtualFile virtualFile);
}
