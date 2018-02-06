package utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    //https://stackoverflow.com/questions/1086123/string-conversion-to-title-case
    public static String toCamelCase(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/\\."; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : c;
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replaceAll("[-/\\.]", "");
    }

    public static String makeGet(String in) {
        return "get"+in.substring(0, 1).toUpperCase()+in.substring(1);
    }

    public static String toLowerDash(String s) {

        String camelCase = "[A-Z]";
        String lowerCase = "[a-z]";

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;
        char lastChar = ' ';
        for (char c : s.toCharArray()) {
            String cStr = String.valueOf(c);

            capNext = regexIndexOf(lowerCase, String.valueOf(lastChar)) >= 0 &&  (regexIndexOf(camelCase,  String.valueOf(c)) >= 0); // explicit cast not needed
            cStr = (capNext)
                    ? "_"+cStr
                    : cStr;
            sb.append(cStr.toLowerCase());

            lastChar = c;
        }
        return sb.toString();
    }

    public static int regexIndexOf(String regexp, String str) {

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    public static <T> Optional<T> elVis(Object root, String... accessors) {
        for (String accessor : accessors) {
            try {
                Method m = root.getClass().getMethod(makeGet(accessor), new Class[0]);
                root = m.invoke(root);
                if (root == null) {
                    return Optional.empty();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable((T) root);
    }
}
