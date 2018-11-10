package net.werpu.tools.gui;


import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class TTIcons {
    public static final Icon LogoSm = load("/images/tt.png"); // 16x16
    public static final Icon LogoNgSm = load("/images/ng.png"); // 16x16

    private static Icon load(String path) {
        return IconLoader.getIcon(path, TTIcons.class);
    }

}
