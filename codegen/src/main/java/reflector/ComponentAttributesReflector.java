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
        while (m.find()) {
            String match = m.group(1);
            ComponentAttribute found = new ComponentAttribute(match, ArgumentType.Input, "any", false);
            matches.add(found);

        }
        return matches.stream()
                .sorted(Comparator.comparing(o -> Strings.nullToEmpty(o.getName())))
                .filter(ComponentAttributesReflector::filterInvalidEntries)
                .map(ComponentAttributesReflector::guessType)
                .collect(Collectors.toList());
    }

    private static boolean filterInvalidEntries(ComponentAttribute attr) {
        String name = attr.getName();

        if (name.contains("(") && name.endsWith("(") || name.replaceAll("\\s", "").contains("($")) {
            return true;
        } else if (name.contains("(")) {
            return false;
        }
        return true;
    }

    /**
     * tries to make type assumptions by detecting
     * common naming patterns
     *
     * @param attr
     * @return
     */
    private static ComponentAttribute guessType(ComponentAttribute attr) {
        String name = attr.getName();

        if (name.contains("(")) {
            name = name.substring(0, name.indexOf("("));
            return new ComponentAttribute(name, ArgumentType.Func, "Function", false);
        } else if (name.equals("value") || name.equals("ngModel")) {
            return new ComponentAttribute(name, ArgumentType.Both, "any", false);
        } else if (name.matches("is[A-Z].*") || name.matches("can[A-Z].*") || name.matches("has[A-Z].*")) {
            return new ComponentAttribute(name, ArgumentType.Input, "boolean", false);
        } else if (name.toLowerCase().contains("lbl") ||
                name.toLowerCase().contains("label") ||
                name.toLowerCase().contains("str") ||
                name.toLowerCase().contains("txt") ||
                name.toLowerCase().contains("text")) {
            return new ComponentAttribute(name, ArgumentType.Input, "string", false);
        }

        return attr;
    }
}
