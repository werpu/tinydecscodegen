/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.supportive.transformations.shared.modelHelpers;

/**
 * probably the most complicated context of all
 * the component context.
 * <p>
 * The aim for this is following
 * a) find out all the needed imports functions etc...
 * b) find out the component as and if not present use ctrl per default
 * c) Find out all the inlined functions and try to push them to the class level
 * d) Find out all the contextual information regarding the component injects
 * e) find the template reference and try to load the template
 * f) find out about all the watchers currently
 * <p>
 * Upon all this info we should make a transformation source which tries to transform the template
 * depending on the angular level (not part of this class, will be written later)
 * A simple replacer like we have it for the Module Transformation does not cut it anymore
 */
public enum BindingType {
    INPUT, BOTH, ASTRING, FUNC, OPT_INPUT, OPT_BOTH, OPT_ASTRING, OPT_FUNC;

    public static BindingType translate(String in) {
        if (in.startsWith("<")) {
            return INPUT;
        } else if (in.startsWith("<?")) {
            return OPT_INPUT;
        } else if (in.startsWith("@")) {
            return ASTRING;
        } else if (in.startsWith("@?")) {
            return OPT_ASTRING;
        } else if (in.startsWith("=")) {
            return BOTH;
        } else if (in.startsWith("=?")) {
            return OPT_BOTH;
        } else if (in.startsWith("&")) {
            return FUNC;
        } else {
            return OPT_FUNC;
        }
    }
}
