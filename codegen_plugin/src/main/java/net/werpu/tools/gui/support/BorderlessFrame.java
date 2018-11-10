package net.werpu.tools.gui.support;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BorderlessFrame extends JDialog {

    public BorderlessFrame() {
        postInit();
    }

    public BorderlessFrame(Frame owner) {
        super(owner);
        postInit();
    }

    public BorderlessFrame(Frame owner, boolean modal) {
        super(owner, modal);
        postInit();
    }

    public BorderlessFrame(Frame owner, String title) {
        super(owner, title);
        postInit();
    }

    public BorderlessFrame(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        postInit();
    }

    public BorderlessFrame(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        postInit();
    }

    public BorderlessFrame(Dialog owner) {
        super(owner);
        postInit();
    }

    public BorderlessFrame(Dialog owner, boolean modal) {
        super(owner, modal);
        postInit();
    }

    public BorderlessFrame(Dialog owner, String title) {
        super(owner, title);
        postInit();
    }

    public BorderlessFrame(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        postInit();
    }

    public BorderlessFrame(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        postInit();
    }

    public BorderlessFrame(Window owner) {
        super(owner);
        postInit();
    }

    public BorderlessFrame(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
        postInit();
    }

    public BorderlessFrame(Window owner, String title) {
        super(owner, title);
        postInit();
    }

    public BorderlessFrame(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
        postInit();
    }

    public BorderlessFrame(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
        postInit();
    }

    private void postInit() {
        setUndecorated(true);
        this.addMouseListener(new MouseAdapter() {

            int posX;
            int posY;
            boolean mouseDown = false;

            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
                mouseDown = true;
            }

            public void mouseReleased(MouseEvent ev) {
                mouseDown = false;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!mouseDown) {
                    return;
                }
                setLocation(e.getXOnScreen() - posX, e.getYOnScreen() - posY);
            }
        });
        this.getRootPane().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispose();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispose();
                }
            }
        });
    }
}
