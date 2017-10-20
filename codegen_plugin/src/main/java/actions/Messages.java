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
package actions;

public class Messages {
    public static final String ERR_OCCURRED = "An Error has occurred";
    public static final String ERR_NO_EDITOR = "No editor found, please focus on a source file with a java type";
    public static final String ERR_INVALID_REF = "The reference into the java file is invalid or outdated, no corresponding java file could be found.";
    public static final String ERR_NO_REF = "Selected Typescript file has no reference into java file";
    public static final String ERR_NAME_VALUE = "The name must have a value";
    public static final String ERR_CTRL_AS_VALUE = "The controller as must have a value";
    public static final String ERR_SELECTOR_PATTERN = "The tag selector must consist of lowercase letters or numbers and - ";
    public static final String ERR_TAG_SELECTOR_MUST_HAVE_A_VALUE = "Tag selector  must have a value";
    public static final String ERR_TAG_SELECTOR_PATTERN = "The tag selector must consist of lowercase letters numbers or '-' ";
    public static final String ERR_CONFIG_PATTERN = "Config name must consist of letters or numbers";
    public static final String ERR_FILTER_PATTERN = "The filter name must consist of letters or numbers only ";
    public static final String ERR_MODULE_PATTERN = "Module name must consist of letters . or numbers";
    public static final String ERR_RUN_PATTERN = "Run name must consist of letters  numbers";
    public static final String ERR_SERVICE_PATTERN = "The service must must consist of letters or numbers";
    public static final String ERR_ELTYPE_SEL = "One element type must be selected";
}
