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
package net.werpu.tools.gui.support;


import javax.swing.text.DefaultFormatter;
import java.text.ParseException;

/**
 * A regexp formatter since swing does not have one
 */
public class RegexpFormatter extends DefaultFormatter {

    String pattern;

    public RegexpFormatter() {
        super();
    }

    public RegexpFormatter(String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public Object stringToValue(String string) throws ParseException {
        if (!string.matches(pattern)) {
            throw new ParseException("Supplied pattern does not match", 0);
        }
        return super.stringToValue(string);
    }
}