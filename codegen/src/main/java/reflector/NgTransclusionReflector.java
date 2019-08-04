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
package reflector;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * a simple regexp based reflector
 * which searches for translclusion
 * information in a template string
 *
 */
public class NgTransclusionReflector {


    public static boolean hasTransclude(String instr) {
        return Strings.nullToEmpty(instr).contains("ng-content");
    }

    public static List<String> getPossibleTransclusionSlots(String inStr) {
        String regexp1 = "select{0,1}\\s*\\=\\s*[\\\"\\'](\\w+)[\\\"\\']";//$2


        Pattern pattern1 = Pattern.compile(regexp1);


        Set<String> retVal = Sets.newHashSet();

        Matcher m1 = pattern1.matcher(inStr);
        while(m1.find()) {
            retVal.add(m1.group(2));
        }

        return retVal.stream()
                .sorted()
                .collect(Collectors.toList());
    }

}
