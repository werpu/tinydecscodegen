package gui;


import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class TTIcons {
  private static Icon load(String path) {
    return IconLoader.getIcon(path, TTIcons.class);
  }

  public static final Icon LogoSm = load("/images/tt.png"); // 16x16

}
