/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package utils;

import javax.swing.*;
import java.awt.*;

public class SwingUtils {

    //https://stackoverflow.com/questions/213266/how-do-i-center-a-jdialog-on-screen

    // Center on screen ( absolute true/false (exact center or 25% upper left) )
    public void centerOnScreen(final Component c, final boolean absolute) {
        final int width = c.getWidth();
        final int height = c.getHeight();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width / 2) - (width / 2);
        int y = (screenSize.height / 2) - (height / 2);
        if (!absolute) {
            x /= 2;
            y /= 2;
        }
        c.setLocation(x, y);
    }

    public static void centerOnParent(final Window child, final boolean absolute) {
        child.pack();
        boolean useChildsOwner = child.getOwner() != null ? ((child.getOwner() instanceof JFrame) || (child.getOwner() instanceof JDialog)) : false;
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension parentSize = useChildsOwner ? child.getOwner().getSize() : screenSize ;
        final Point parentLocationOnScreen = useChildsOwner ? child.getOwner().getLocationOnScreen() : new Point(0,0) ;
        final Dimension childSize = child.getSize();
        childSize.width = Math.min(childSize.width, screenSize.width);
        childSize.height = Math.min(childSize.height, screenSize.height);
        child.setSize(childSize);
        int x;
        int y;
        if ((child.getOwner() != null) && child.getOwner().isShowing()) {
            x = (parentSize.width - childSize.width) / 2;
            y = (parentSize.height - childSize.height) / 2;
            x += parentLocationOnScreen.x;
            y += parentLocationOnScreen.y;
        } else {
            x = (screenSize.width - childSize.width) / 2;
            y = (screenSize.height - childSize.height) / 2;
        }
        if (!absolute) {
            x /= 2;
            y /= 2;
        }
        child.setLocation(x, y);
    }
}
