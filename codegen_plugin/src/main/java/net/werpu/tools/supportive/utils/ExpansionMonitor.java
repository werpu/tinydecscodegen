package net.werpu.tools.supportive.utils;

import net.werpu.tools.supportive.fs.common.IAngularFileContext;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExpansionMonitor {

    JTree tree;
    AtomicBoolean locked = new AtomicBoolean(false);
    Set<String> expanded = ConcurrentHashMap.newKeySet();

    public ExpansionMonitor(JTree tree) {
        this.tree = tree;
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                if(locked.get()) {
                    return;
                }
                Object lastPathComponent = event.getPath().getLastPathComponent();
                if(lastPathComponent instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) lastPathComponent).getUserObject();
                    if(userObject instanceof String) {
                        expanded.add((String)userObject);
                    } else if(userObject instanceof IAngularFileContext) {
                        expanded.add(((IAngularFileContext)userObject).getVirtualFile().getPath());
                    }
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                if(locked.get()) {
                    return;
                }
                Object lastPathComponent = event.getPath().getLastPathComponent();
                if(lastPathComponent instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) lastPathComponent).getUserObject();
                    if(userObject instanceof String) {
                        String key = (String) userObject;
                        expanded.remove(key);
                    } else if(userObject instanceof IAngularFileContext) {
                        String key = ((IAngularFileContext) userObject).getVirtualFile().getPath();
                        expanded.remove(key);
                    }
                }
            }
        });
    }

    public void restore() {
        Enumeration<DefaultMutableTreeNode> en = ((DefaultMutableTreeNode)tree.getModel().getRoot()).breadthFirstEnumeration();

        locked.set(true);
        try {
            Collections.list(en).stream().forEach(node -> {
                Object userObject = node.getUserObject();
                String key = null;
                if(userObject instanceof String) {
                    key = (String) userObject;
                } else if(userObject instanceof IAngularFileContext) {
                    key = ((IAngularFileContext)userObject).getVirtualFile().getPath();
                }
                if(key != null && expanded.contains(key)) {
                    tree.expandPath(new TreePath(node.getPath()));
                }
            });
            tree.doLayout();
        } finally {
            locked.set(false);
        }

    }
}
