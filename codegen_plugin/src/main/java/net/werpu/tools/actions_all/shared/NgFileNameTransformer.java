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

package net.werpu.tools.actions_all.shared;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.werpu.tools.supportive.utils.IntellijUtils.getTsExtension;

public class NgFileNameTransformer implements FileNameTransformer {

    private String postfix;

    public NgFileNameTransformer(String postfix) {
        this.postfix = postfix;
    }

    @Override
    public String transform(String className) {

        Pattern pattern = Pattern.compile("([A-Z]{1})");
        Matcher matcher = pattern.matcher(className);

        StringBuilder builder = new StringBuilder();
        int i = 0;
        boolean first = true;
        while (matcher.find()) {
            String replacement = matcher.group(1);
            builder.append(className.substring(i, matcher.start()));
            if (first) {
                first = false;
                builder.append(replacement);
            } else {
                builder.append("-" + replacement);
            }

            i = matcher.end();
        }
        builder.append(className.substring(i, className.length()));

        return builder.toString().toLowerCase() + "." + postfix + getTsExtension();
    }
}
