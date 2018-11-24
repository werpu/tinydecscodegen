package net.werpu.tools.supportive.reflectRefact.navigation;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.werpu.tools.supportive.reflectRefact.SIMPLE_COMMAND;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;
import static net.werpu.tools.supportive.utils.StringUtils.literalStartsWith;

@AllArgsConstructor
public class TreeQueryEngine<T> {

    /*predefined rex for deeper string analysis*/
    public static final String RE_EL_TEXT_EQ = "^\\s*\\:TEXT\\s*\\((.*)\\)\\s*$";
    public static final String RE_EL_TEXT_STARTS_WITH = "^\\s*\\:TEXT\\*\\s*\\((.*)\\)\\s*$";
    public static final String RE_EL_NAME_EQ = "^\\s*\\:NAME\\s*\\((.*)\\)\\s*$";
    public static final String CHILD_ELEM = ">";
    static final String RE_STRING_LITERAL = "^[\\\"\\'](.*)[\\\"\\']$";
    public static final String RE_EL_NAME_STARTS_WITH = "^\\s*\\:NAME\\*\\((.*)\\)\\s*$";
    public static final String RE_PARENTS_EQ = "^\\s*:PARENTS\\s*\\((.*)\\)\\s*$";
    public static final String RE_PARENTS_EQ_FIRST = "^\\s*:PARENTS_FIRST\\s*\\((.*)\\)\\s*$";
    public static final String RE_PARENTS_EQ_LAST = "^\\s*:PARENTS_LAST\\s*\\((.*)\\)\\s*$";
    private static final String ERR_UNDEFINED_QUERY_MAPPING = "Undefined query mapping";




    TreeNavigationAdapter<T> navigationAdapter;

    /*Several helpers, which extend the core functionality
     * (walking matching  via strings) with some typesave high level routines*/
    public static String PARENTS_EQ(String val) {
        return ":PARENTS(" + val + ")";
    }

    public static String PARENTS_EQ_FIRST(String val) {
        return ":PARENTS_FIRST(" + val + ")";
    }

    public static String PARENTS_EQ_LAST(String val) {
        return ":PARENTS_LAST(" + val + ")";
    }

    public static String EL_TEXT_EQ(String val) {
        return ":TEXT(" + val + ")";
    }

    public static String EL_NAME_EQ(String val) {
        return ":NAME(" + val + ")";
    }

    public static String EL_TEXT_STARTS_WITH(String val) {
        return ":TEXT*(" + val + ")";
    }

    public static Object DIRECT_CHILD(String val) {
        return new Object[]{CHILD_ELEM, val};
    }

    /**
     * child search wthin a parent search
     *
     * @param cmdOrFunction
     * @return
     */
    public static Object[] INVERSE(Object... cmdOrFunction) {
        return Lists.reverse(Arrays.asList(cmdOrFunction)).toArray();
    }

    /**
     * This is the central method of our query engine
     * it follows a very simple grammar which distincts
     * between string matches and simple commands and function
     * matches
     * <p>
     * It allows to trace down a tree css/jquery style but also
     * allows simple  parent backtracking
     * this one walks up all parents which match the subsequent criteria
     * once this is done, if you want to avoid that behavior
     * you either can use a firstElem() or reduce
     * or as shortcut if you do not want to leave the query
     * P_FIRST (:FIRST)
     * or
     * P_LAST (:LAST)
     * <p>
     * to make the reduction within the query
     * <p>
     * Syntax
     * <p>
     * QUERY: COMMAND*
     * COMMAND: ELEMENT_TYPE | SIMPLE_COMMAND | FUNC
     * <p>
     * ELEMENT_TYPE: char*
     * <p>
     * SIMPLE_COMMAND: > | :FIRST | :TEXT(<char *>) | :TEXT*(<char *>) | :NAME(<char *>) | :NAME*(<char *>) | PARENTS_EQ:(<char *> | ElementType) | PARENTS_EQ_FIRST:(<char *> | ElementType) | PARENTS_EQ_LAST:(<char *> | ElementType)   | :PARENTS | :PARENT   | :LAST | :FIRST
     * :PARENTS(COMMAND) shortcut for :PARENTS, :TEXT(...) or :PARENTS,COMMAND
     * <p>
     * FUNC: CONSUMER | PREDICATE | FUNCTION
     * CONSUMER: Function as defined by Java
     * PREDICATE: Function as defined by Java
     * FUNCTION: Function as defined by Java
     *
     * @param subItem  item to be queried
     * @param commands the list of commands to be processed
     * @return
     */
    @NotNull
    public Stream<T> exec(Stream<T> subItem, Object[] commands, boolean directionDown) {
        for (Object command : commands) {
            if (isStringElement(command)) {
                //lets reduce mem consumption by distincting the subset results
                subItem = subItem.distinct();

                String strCommand = ((String) command).trim();
                SIMPLE_COMMAND simpleCommand = SIMPLE_COMMAND.fromValue(strCommand);
                if (simpleCommand != null) {//command found
                    switch (simpleCommand) {
                        case CHILD_ELEM:
                            subItem = subItem.flatMap(theItem -> navigationAdapter.findChildren(theItem, el -> Boolean.TRUE).stream());
                            continue;
                        case RE_EL_TEXT_EQ:
                            subItem = handleTextEQ(subItem, strCommand);
                            continue;
                        case RE_EL_TEXT_STARTS_WITH:
                            subItem = handleTextStartsWith(subItem, strCommand);
                            continue;
                        case RE_EL_NAME_EQ:
                            subItem = handleNameEQ(subItem, strCommand);
                            continue;
                        case RE_EL_NAME_STARTS_WITH:
                            subItem = handleNameStartsWith(subItem, strCommand);
                            continue;
                        case RE_PARENTS_EQ:
                            subItem = handleParentsEq(subItem, strCommand);
                            continue;
                        case RE_PARENTS_EQ_FIRST:
                            subItem = handleParentsEqFirst(subItem, strCommand);
                            continue;
                        case RE_PARENTS_EQ_LAST:
                            subItem = handleParentsEqLast(subItem, strCommand);
                            continue;
                        case P_FIRST:
                            subItem = handlePFirst(subItem);
                            continue;
                        case P_PARENTS:
                            subItem = parentsOf(subItem);
                            continue;
                        case P_PARENT:
                            subItem = parentOf(subItem);
                            continue;
                        case P_LAST:
                            subItem = handlePLast(subItem);
                            continue;
                    }
                }
                subItem = elementTypeMatch(subItem, strCommand);
                continue;

            }
            subItem = functionTokenMatch(subItem, command);
        }
        return subItem.distinct();
    }


    @NotNull
    private Stream<T> elementTypeMatch(Stream<T> subItem, String finalSubCommand) {
        subItem = subItem.flatMap(psiItem ->  navigationAdapter.findElements(psiItem, psiElement -> {
            String cmdString = toString(psiElement);
            return cmdString.equalsIgnoreCase(finalSubCommand) || cmdString.startsWith(finalSubCommand + ":");
        }).stream())
                .distinct()
                .collect(Collectors.toList()).stream();
        return subItem;
    }

    /**
     * resolve an incoming function
     * from the query chain
     * <p>
     * Depending on the type you can use the passing of functions for various things
     * <li>Consumer&lt;PsiElementContext&gt;... to extract values from the current query position
     * in this case the returned stream is the same as the one before</li>
     * <li>Predicate&lt;PsiElementContext&gt;... to filter items out, in this case
     * the returned stream is the filtered stream of the old one</li>
     * <li>Function&lt;PsiElementContext, PsiElementContext&gt;</li>
     *
     * @param subItem the subitem to be resolved
     * @param func    a function of Consumer, Predicate, or (Function<PsiElementContext, PsiElementContext>)
     * @return the processed subitem
     */
    @NotNull
    private Stream<T> functionTokenMatch(Stream<T> subItem, Object func) {
        if(func instanceof StreamFunc) {
            subItem = ((StreamFunc<T>) func).apply(subItem);
        }
        else if (func instanceof Consumer) {
            subItem = handleConsumer(subItem, (Consumer) func);
        } else if (func instanceof Predicate) {
            subItem = handlePredicate(subItem, (Predicate) func);
        } else if (func instanceof Function) {
            subItem = handleFunction(subItem, (Function<T, T>) func);
        } else {
            throw new IllegalArgumentException(ERR_UNDEFINED_QUERY_MAPPING);
        }
        return subItem;
    }

    @NotNull
    private  Stream<T> handleFunction(Stream<T> subItem, Function<T, T> item) {
        subItem = subItem.map(item::apply);
        return subItem;
    }

    @NotNull
    private  Stream<T> handlePredicate(Stream<T> subItem, Predicate item) {
        subItem = subItem.filter(item::test);
        return subItem;
    }

    @NotNull
    private  Stream<T> handleConsumer(Stream<T> subItem, Consumer item) {
        subItem.forEach(elem -> {
            item.accept(elem);
        });
        subItem = emptyStream();
        return subItem;
    }



    private static boolean isStringElement(Object item) {
        return item instanceof String;
    }

    @NotNull
    private Stream<T> handleTextStartsWith(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_EL_TEXT_STARTS_WITH);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(getText(psiElementContext), matchText) || getText(psiElementContext).startsWith(matchText);
        });
        return subItem;
    }

    @NotNull
    private Stream<T> handleTextEQ(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_EL_TEXT_EQ);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);
            boolean literalEquals = false;
            if (matchText.matches(RE_STRING_LITERAL)) {
                matchText = matchText.substring(1, matchText.length() - 1);
                literalEquals = true;
            }

            return literalEquals ? literalEquals(getText(psiElementContext), matchText) : matchText.equals(getText(psiElementContext));
        });
        return subItem;
    }

    @NotNull
    private Stream<T> handleNameStartsWith(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_EL_NAME_STARTS_WITH);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(getName(psiElementContext), matchText) ||

                    getName(psiElementContext).startsWith(matchText);
        });
        return subItem;
    }

    @NotNull
    private Stream<T> handleNameEQ(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_EL_NAME_EQ);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);
            boolean literalEquals = false;
            if (matchText.matches(RE_STRING_LITERAL)) {
                matchText = matchText.substring(1, matchText.length() - 1);
                literalEquals = true;
            }

            return literalEquals ? literalEquals(getName(psiElementContext), matchText) : matchText.equals(getName(psiElementContext));
        });
        return subItem;
    }

    private String getName(T psiElementContext) {
        return navigationAdapter.getName(psiElementContext);
    }

    @NotNull
    private Stream<T> handleParentsEq(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_PARENTS_EQ);
        subItem = subItem.flatMap(theItem -> parents(theItem).stream().filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(getName(psiElementContext), matchText) || literalStartsWith(toString(psiElementContext), matchText);
        }));
        return subItem;
    }

    @NotNull
    private Stream<T> handleParentsEqFirst(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_PARENTS_EQ_FIRST);
        return subItem.map(theItem -> parents(theItem).stream().filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(getName(psiElementContext), matchText) || literalStartsWith(toString(psiElementContext), matchText);
        }).findFirst())
                .filter(item -> item.isPresent()).map(Optional::get);


    }

    private String toString(T psiElementContext) {
        return navigationAdapter.toString(psiElementContext);
    }

    private String getText(T psiElementContext) {
        return navigationAdapter.getText(psiElementContext);
    }

    @NotNull
    private  Stream<T> handleParentsEqLast(Stream<T> subItem, String text) {
        Pattern p = Pattern.compile(RE_PARENTS_EQ_LAST);
        return subItem.map(theItem -> parents(theItem).stream().filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(getName(psiElementContext), matchText) || literalStartsWith(toString(psiElementContext), matchText);
        }).reduce((e1, e2) -> e2)).filter(item -> item.isPresent()).map(Optional::get);

    }


    @NotNull
    private Stream<T> handlePFirst(Stream<T> subItem) {
        T firstItem = subItem.findFirst().orElse(null);
        if (firstItem != null) {
            subItem = asList(firstItem).stream();
        } else {
            subItem = emptyStream();
        }
        return subItem;
    }

    @NotNull
    private  Stream<T> handlePLast(Stream<T> subItem) {
        Optional<T> reduced = subItem.reduce((theItem, theItem2) -> theItem2);
        if (reduced.isPresent()) {
            subItem = asList(reduced.get()).stream();
        } else {
            subItem = Collections.<T>emptyList().stream();
        }
        return subItem;
    }


    @NotNull
    private Stream<T> parentsOf(Stream<T> subItem) {
        return subItem.flatMap(theItem -> parents(theItem).stream());
    }

    private List<T> parents(T theItem) {
        return navigationAdapter.parents(theItem);
    }


    @NotNull
    private Stream<T> parentOf(Stream<T> subItem) {
        return subItem.flatMap(item -> parents(item,1).stream());
    }

    private static <T> Stream<T> emptyStream() {
        return Collections.<T>emptyList().stream();
    }

    private List<T> parents(T item, int depth) {
        List<T> allParents = navigationAdapter.walkParents(item, el -> true);
        return allParents.subList(0, Math.min(depth, allParents.size()));
    }
}
