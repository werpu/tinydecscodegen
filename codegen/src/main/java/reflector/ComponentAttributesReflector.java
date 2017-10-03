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

package reflector;

import com.google.common.base.Strings;
import dtos.ArgumentType;
import dtos.ComponentAttribute;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * a very simple regexp based prop parser
 * to determine the possible attribute candidates
 */
public class ComponentAttributesReflector {

    public static List<ComponentAttribute> reflect(String text, String controllerAs) {
        Pattern pattern = Pattern.compile("[^\\w]+ctrl\\.([A-Za-z0-9\\_\\$\\(]+)");
        Matcher m = pattern.matcher(text);
        Set<ComponentAttribute> matches = new HashSet<>();
        while(m.find()) {
            String match = m.group(1);
            if(match.contains("(")) {
                match = match.replaceAll("\\(","");
                ComponentAttribute found = new ComponentAttribute(match, ArgumentType.Func, false);
                matches.add(found);
            } else {
                ComponentAttribute found = new ComponentAttribute(match, ArgumentType.Input, false);
                matches.add(found);
            }
        }
        return matches.stream()
                .sorted(Comparator.comparing(o -> Strings.nullToEmpty(o.getName())))
                .collect(Collectors.toList());
    }
}
