package net.werpu.tools.supportive.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    public static String makeVarName(String in) {
        return in.substring(0, 1).toLowerCase()+in.substring(1);
    }

    public static String makeGet(String in) {
        return "get"+in.substring(0, 1).toUpperCase()+in.substring(1);
    }

    public static String pointToLowerDash(String s) {
        return s.replaceAll("\\.+", "_");
    }

    public static String toLowerDash(String s) {
        return toAnyDash(s, "_");
    }
    public static String toDash(String s) {
        return toAnyDash(s, "-");
    }

    @NotNull
    public static String toAnyDash(String s, String divider) {
        String camelCase = "[A-Z]";
        String lowerCase = "[a-z]";

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;
        char lastChar = ' ';
        for (char c : s.toCharArray()) {
            String cStr = String.valueOf(c);

            capNext = regexIndexOf(lowerCase, String.valueOf(lastChar)) >= 0 &&  (regexIndexOf(camelCase,  String.valueOf(c)) >= 0); // explicit cast not needed
            cStr = (capNext)
                    ? divider+cStr
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

    public static boolean literalEquals(String literal, String contentToCompare) {
        literal = stripQuotes(literal);
        contentToCompare = stripQuotes(contentToCompare);
        return contentToCompare.equals(literal);
    }

    public static boolean literalContains(String literal, String contentToCompare) {
        literal = stripQuotes(literal);
        contentToCompare = stripQuotes(contentToCompare);
        return literal.contains(contentToCompare);
    }

    public static boolean literalStartsWith(String literal, String contentToCompare) {
        literal = stripQuotes(literal);
        contentToCompare = stripQuotes(contentToCompare);
        return literal.startsWith(contentToCompare);
    }

    public static String stripQuotes(String literal) {
        return literal.replaceAll("^[\\\"\\'](.*)[\\\"\\']$", "$1");
    }

    public static boolean findWithSpaces(String probe, String ...params) {

        String rexp = Arrays.asList(params).stream().map(s -> s.replaceAll("([^A-Za-z0-9])+", "\\\\$1")).reduce((s1, s2) -> s1+"\\s*"+s2).get();

        Matcher m = Pattern.compile(rexp).matcher(probe);
        return m.find();
    }

    public static String normalizePath(String in) {
        if(in == null) {
            return in;
        }
        return in.replaceAll("\\\\", "/");
    }


    @NotNull
    public static String refactor(List<IRefactorUnit> refactorings, String toSplit) {
        int start = 0;
        int end = -1;
        Collections.sort(refactorings, (el1, el2) -> el1.getStartOffset() - el2.getEndOffset());
        List<String> retVal = Lists.newArrayListWithCapacity(refactorings.size() * 2);

        for (IRefactorUnit refactoring : refactorings) {
            if (refactoring.getStartOffset() >= 0 && end < refactoring.getStartOffset()) {
                retVal.add(toSplit.substring(start, refactoring.getStartOffset()));
                start = refactoring.getEndOffset();
            }
            retVal.add(refactoring.getRefactoredText());
            end = refactoring.getEndOffset();
        }
        if (end < toSplit.length()) {
            retVal.add(toSplit.substring(end));
        }
        return  Joiner.on("").join(retVal);
    }


    /**
     * Generic refactoring function
     *
     * Which allows to calculate a refactoring relative from a root
     * element without writing the data back
     * @param refactorings a list of generic refactorings which might be part of the given
     *                     element or not
     * @param rootElement the psi element identifiyng the refactoring root
     *
     * @return a string with the refactored content of the root element
     */
    @NotNull
    public static String refactor(List<IRefactorUnit> refactorings, PsiElement rootElement) {
        Collections.sort(refactorings, (el1, el2) -> el1.getStartOffset() - el2.getEndOffset());
        List<String> retVal = Lists.newArrayListWithCapacity(refactorings.size() * 2);

        int rootElementOffset = rootElement.getTextOffset();
        int elementEndOffset = rootElement.getTextOffset() + rootElement.getTextLength();

        int start = 0;
        int end = -1;

        String toSplit = rootElement.getText();


        for (IRefactorUnit refactoring : refactorings) {
            int refactorUnitRelOffset = calcOffsetDiff(rootElementOffset, refactoring.getStartOffset());
            if ((refactorUnitRelOffset) >= 0 && end < refactorUnitRelOffset
             && refactoring.getEndOffset() < elementEndOffset) {
                retVal.add(toSplit.substring(start, refactorUnitRelOffset));
                start = calcOffsetDiff(rootElementOffset, refactoring.getEndOffset());
            }
            retVal.add(refactoring.getRefactoredText());
            end = calcOffsetDiff(rootElementOffset, refactoring.getEndOffset());
        }
        if (end < toSplit.length()) {
            retVal.add(toSplit.substring(end));
        }
        return  Joiner.on("").join(retVal);
    }

    private static int calcOffsetDiff(int rootElementOffset, int startOffset) {
        return startOffset - rootElementOffset;
    }

    public static boolean isThis(String name) {
        return name.startsWith("this.") || name.startsWith("_t.") || name.startsWith("_t_.");
    }
}
