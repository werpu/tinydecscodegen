package net.werpu.tools.supportive.reflectRefact.navigation;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.werpu.tools.supportive.utils.IntellijUtils.flattendArr;
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;
import static net.werpu.tools.supportive.utils.StringUtils.literalStartsWith;

/**
 * central query engine.
 * We are opting here for a typesafe query engine
 * to avoid the parser overhead
 * <p>
 * The syntax is rather straight forward
 * and in a smalltalk/scala way uses a small core
 * of unary/extensions and functions
 * which allows for abitrary tree ops in both directions
 * <p>
 * QUERY: COMMAND*
 * COMMAND: UNARY_COMMAND | FUNC | NAME_MATCH | QUERY
 * UNARY_COMMAND: CHILD_ELEM, FIRST, PARENTS, PARENT, LAST
 * <p>
 * FUNC: EXTENSION | CONSUMER | PREDICATE | FUNCTION
 * <p>
 * CONSUMER: Function as defined by Java
 * PREDICATE: Function as defined by Java
 * FUNCTION: Function as defined by Java
 * <p>
 * EXTENSION:
 * PARENTS_EQ(NAME_MATCH) |
 * PARENTS_EQ_FIRST(NAME_MATCH) |
 * PARENTS_EQ_LAST(NAME_MATCH) |
 * TEXT_EQ(NAME_MATCH) |
 * NAME_EQ(NAME_MATCH) |
 * TEXT_STARTS_WITH(NAME_MATCH) |
 * NAME_STARTS_WITH(NAME_MATCH) |
 * PARENT_SEARCH(QUERY)
 * <p>
 * NAME_MATCH: char*
 * <p>
 * And whatever the user defines
 *
 * @param <T>
 */
@AllArgsConstructor
public class TreeQueryEngine<T> {

    /*unary core commands*/
    public static final UnaryCommand CHILD_ELEM = UnaryCommand.CHILD_ELEM;
    public static final UnaryCommand PARENTS = UnaryCommand.PARENTS;
    public static final UnaryCommand PARENT = UnaryCommand.PARENT;
    public static final UnaryCommand LAST = UnaryCommand.LAST;
    public static final UnaryCommand FIRST = UnaryCommand.FIRST;
    private static final String ERR_UNDEFINED_QUERY_MAPPING = "Undefined query mapping";


    @Getter
    TreeQueryAdapter<T> navigationAdapter;


    /*Several helpers, which extend the core functionality
     * (walking matching  via strings) with some typesave high level routines*/

    public static <T> QueryExtension<T> PARENTS_EQ(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .flatMap(theItem -> engine.getNavigationAdapter().walkParents(theItem, el -> Boolean.TRUE).stream())
                .filter(el -> {
                    TreeQueryAdapter<T> navigationAdapter = engine.getNavigationAdapter();
                    return literalStartsWith(navigationAdapter.getIdentifier(el), val) || literalStartsWith(navigationAdapter.toString(el), val);
                });
    }

    public static <T> QueryExtension<T> PARENTS_EQ_FIRST(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .map(theItem -> engine.getNavigationAdapter().walkParents(theItem,
                        (el) -> {
                            TreeQueryAdapter<T> navigationAdapter = engine.getNavigationAdapter();
                            return literalStartsWith(navigationAdapter.getIdentifier(el), val) || literalStartsWith(navigationAdapter.toString(el), val);
                        }
                ).stream()
                        .findFirst()
                        .orElse(null))
                .filter(el -> el != null);
    }

    private static <T> Stream<T> any(Stream<T> str1, Stream<T> str2) {

        List<T> strList1 = str1.collect(Collectors.toList());
        List<T> strList2 = str2.collect(Collectors.toList());
        return strList1.isEmpty() ? strList2.stream() : strList1.stream();

    }

    public static <T> QueryExtension<T> ANY(Object[] str1, Object[] str2) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> {
            List<T> itemlist = stream.collect(Collectors.toList());
            return any(engine.exec(itemlist.stream(), str1, true),
                    engine.exec(itemlist.stream(), str2, true));


        };
    }

    private static <T> Stream<T> all(Stream<T> str1, Stream<T> str2) {
        return Stream.concat(str1, str2);
    }

    public static <T> QueryExtension<T> ALL(Object[] str1, Object[] str2) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> {
            List<T> itemlist = stream.collect(Collectors.toList());
            return all(engine.exec(itemlist.stream(), str1, true),
                    engine.exec(itemlist.stream(), str2, true));

        };
    }


    public static <T> QueryExtension<T> PARENTS_EQ_LAST(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .map(theItem -> engine.getNavigationAdapter().walkParents(theItem,
                        (el) -> {
                            TreeQueryAdapter<T> navigationAdapter = engine.getNavigationAdapter();
                            return literalStartsWith(navigationAdapter.getIdentifier(el), val) || literalStartsWith(navigationAdapter.toString(el), val);
                        }
                ).stream()
                        .reduce((e1, e2) -> e2)
                        .orElse(null))
                .filter(el -> el != null);
    }


    public static <T> QueryExtension<T> TEXT_EQ(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .filter(el -> {
                    return literalEquals(engine.getNavigationAdapter().getText(el), val);
                });
    }

    public static <T> QueryExtension<T> NAME_EQ(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream.filter(el -> {
            return literalEquals(engine.getNavigationAdapter().getIdentifier(el), val);
        });
    }

    public static <T> QueryExtension<T> TEXT_STARTS_WITH(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream.filter(el -> {
            return literalStartsWith(engine.getNavigationAdapter().getText(el), val);
        });
    }

    public static <T> QueryExtension<T> NAME_STARTS_WITH(String val) {
        return (TreeQueryEngine<T> engine, Stream<T> stream) -> stream.filter(el -> {
            return literalStartsWith(engine.getNavigationAdapter().getIdentifier(el), val);
        });
    }


    /**
     * a helper which inverses the search order into the opposite direction
     * note... some special semantic commands like CHILD (>) do not work in this direction
     * and will throw an error
     *
     * @param cmdOrFunction
     * @return
     */
    public static <T> QueryExtension<T> PARENT_SEARCH(Object... cmdOrFunction) {
        return (TreeQueryEngine<T> engine, Stream<T> items) -> engine.exec(items.flatMap(item -> engine.getNavigationAdapter().walkParents(item, el -> Boolean.TRUE).stream()), cmdOrFunction, false);
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
     * TODO update the grammar after we go for a smalltalk like
     * small extensible core
     * <p>
     * <p>
     * <p>
     * <p>
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
     *
     * @param subItem  item to be queried
     * @param commands the list of commands to be processed
     * @return
     */
    @NotNull
    public Stream<T> exec(Stream<T> subItem, Object[] query, boolean directionDown) {
        Object[] commands = flattendArr(query).stream().toArray(Object[]::new);
        boolean childsOnly = false;
        for (Object command : commands) {
            //name match
            if (NAME_MATCH(command)) {
                //in case of a name match, we have a difference between the search
                //direction down, we simply walk down the entire tree two dimensionally
                //in case of an up we walk up the tree one dimensionally with a match in the row up
                List helpList = subItem.collect(Collectors.toList());
                subItem = helpList.stream();
                if(!childsOnly) {
                    subItem = (directionDown) ? NAME_MATCH(subItem, (String) command) : NAME_UP_MATCH(subItem, (String) command);
                } else {
                    childsOnly = false;
                    subItem = elementTypeEQ(subItem, (String) command);
                }
                continue;

            } else if (UNARY_COMMAND(command)) {
                UnaryCommand simpleCommand = (UnaryCommand) command;
                if (simpleCommand != null) {//command found
                    switch (simpleCommand) {
                        case CHILD_ELEM:
                            //child elements always only take the next element into consideration
                            subItem = subItem.flatMap(theItem -> navigationAdapter.findChildren(theItem, el -> Boolean.TRUE)
                                    .stream());
                            childsOnly = true;
                            continue;
                        case FIRST:
                            subItem = handlePFirst(subItem);
                            continue;
                        case PARENTS:
                            subItem = parentsOf(subItem);
                            continue;
                        case PARENT:
                            subItem = parentOf(subItem);
                            continue;
                        case LAST:
                            subItem = handlePLast(subItem);
                            continue;
                    }
                }
            }
            subItem = FUNC(subItem, command);
        }
        return subItem.distinct();
    }

    public boolean UNARY_COMMAND(Object command) {
        return command instanceof UnaryCommand;
    }

    @NotNull
    public Stream<T> NAME_MATCH(Stream<T> subItem, String command) {
        //lets reduce mem consumption by distincting the subset results
        subItem = subItem.distinct();

        String strCommand = command.trim();
        subItem = elementTypeMatch(subItem, strCommand);
        return subItem;
    }




    @NotNull
    public Stream<T> NAME_UP_MATCH(Stream<T> subItem, String command) {
        //lets reduce mem consumption by distincting the subset results
        subItem = subItem.distinct();

        String strCommand = command.trim();
        subItem = elementTypeEQ(subItem, strCommand);
        return subItem;
    }

    @NotNull
    private Stream<T> elementTypeEQ(Stream<T> subItem, String finalSubCommand) {
        return subItem.filter(el -> {
            String cmdString = toString(el);
            return cmdString.equalsIgnoreCase(finalSubCommand) || cmdString.startsWith(finalSubCommand + ":");
        });
    }

    @NotNull
    private Stream<T> elementTypeMatch(Stream<T> subItem, String finalSubCommand) {
        subItem = subItem.flatMap(psiItem -> navigationAdapter.findElements(psiItem, psiElement -> {
            String cmdString = toString(psiElement);
            return cmdString.equalsIgnoreCase(finalSubCommand) || cmdString.startsWith(finalSubCommand + ":");
        }).stream())
                //.distinct()
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
    private Stream<T> FUNC(Stream<T> subItem, Object func) {
        if (func instanceof StreamFunc) {
            subItem = ((StreamFunc<T>) func).apply(subItem);
        } else if (func instanceof QueryExtension) {
            subItem = ((QueryExtension<T>) func).apply(this, subItem);
        } else if (func instanceof Consumer) {
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
    private Stream<T> handleFunction(Stream<T> subItem, Function<T, T> item) {
        subItem = subItem.map(item::apply);
        return subItem;
    }

    @NotNull
    private Stream<T> handlePredicate(Stream<T> subItem, Predicate item) {
        subItem = subItem.filter(item::test);
        return subItem;
    }

    @NotNull
    private Stream<T> handleConsumer(Stream<T> subItem, Consumer item) {
        subItem.forEach(elem -> {
            item.accept(elem);
        });
        subItem = emptyStream();
        return subItem;
    }


    private static boolean NAME_MATCH(Object item) {
        return item instanceof String;
    }


    private String toString(T psiElementContext) {
        return navigationAdapter.toString(psiElementContext);
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
    private Stream<T> handlePLast(Stream<T> subItem) {
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
        return navigationAdapter.walkParents(theItem, el -> Boolean.TRUE);
    }


    @NotNull
    private Stream<T> parentOf(Stream<T> subItem) {
        return subItem.flatMap(item -> parents(item, 1).stream());
    }

    private static <T> Stream<T> emptyStream() {
        return Collections.<T>emptyList().stream();
    }

    private List<T> parents(T item, int depth) {
        List<T> allParents = navigationAdapter.walkParents(item, el -> true);
        return allParents.subList(0, Math.min(depth, allParents.size()));
    }
}
