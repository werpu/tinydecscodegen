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
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;
import static net.werpu.tools.supportive.utils.StringUtils.literalStartsWith;

@AllArgsConstructor
public class TreeQueryEngine<T> {

    /*predefined rex for deeper string analysis*/

    public static final UnaryCommand CHILD_ELEM = UnaryCommand.CHILD_ELEM;
    public static final UnaryCommand P_PARENTS = UnaryCommand.P_PARENTS;
    public static final UnaryCommand P_PARENT = UnaryCommand.P_PARENT;
    public static final UnaryCommand P_LAST = UnaryCommand.P_LAST;
    public static final UnaryCommand P_FIRST = UnaryCommand.P_FIRST;
    private static final String ERR_UNDEFINED_QUERY_MAPPING = "Undefined query mapping";



    @Getter
    TreeQueryAdapter<T> navigationAdapter;


    /*Several helpers, which extend the core functionality
     * (walking matching  via strings) with some typesave high level routines*/

    public static <T> QueryExtension<T>  PARENTS_EQ(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .flatMap(theItem -> engine.getNavigationAdapter().parents(theItem).stream())
                .filter(el -> {
            TreeQueryAdapter<T> navigationAdapter = engine.getNavigationAdapter();
            return literalStartsWith(navigationAdapter.getName(el), val) || literalStartsWith(navigationAdapter.toString(el), val);
        });
    }

    public static <T> QueryExtension<T>  PARENTS_EQ_FIRST(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .map(theItem -> engine.getNavigationAdapter().walkParents(theItem,
                        (el) -> {
                            TreeQueryAdapter<T> navigationAdapter = engine.getNavigationAdapter();
                            return literalStartsWith(navigationAdapter.getName(el), val) || literalStartsWith(navigationAdapter.toString(el), val);
                        }
                ).stream()
                        .findFirst()
                        .orElse(null))
                .filter(el -> el != null);
    }

    public static <T> QueryExtension<T>  PARENTS_EQ_LAST(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .map(theItem -> engine.getNavigationAdapter().walkParents(theItem,
                        (el) -> {
                            TreeQueryAdapter<T> navigationAdapter = engine.getNavigationAdapter();
                            return literalStartsWith(navigationAdapter.getName(el), val) || literalStartsWith(navigationAdapter.toString(el), val);
                        }
                ).stream()
                        .reduce((e1, e2) -> e2)
                        .orElse(null))
                .filter(el -> el != null);
    }


    public static <T> QueryExtension<T> TEXT_EQ(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream
                .filter(el -> {
            return literalEquals(engine.getNavigationAdapter().getText(el), val);
        });
    }

    public static <T> QueryExtension<T> NAME_EQ(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream.filter(el -> {
            return literalEquals(engine.getNavigationAdapter().getName(el),val);
        });
    }

    public static <T> QueryExtension<T> TEXT_STARTS_WITH(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream.filter(el -> {
            return literalStartsWith(engine.getNavigationAdapter().getText(el),val);
        });
    }

    public static <T> QueryExtension<T> NAME_STARTS_WITH(String val) {
        return  (TreeQueryEngine<T> engine, Stream<T> stream) -> stream.filter(el -> {
            return literalStartsWith(engine.getNavigationAdapter().getName(el),val);
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
        return (TreeQueryEngine<T> engine, Stream<T> items) -> engine.exec(items.flatMap(item -> engine.getNavigationAdapter().parents(item).stream()), cmdOrFunction, false);
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
     *

     *
     *
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
     * QUERY: COMMAND*
     * COMMAND: UNARY_COMMAND | FUNC | NameMatch
     * UNARY_COMMAND: CHILD_ELEM, P_FIRST, P_PARENTS, P_PARENT, P_LAST
     *
     * FUNC: EXTENSION | CONSUMER | PREDICATE | FUNCTION
     * CONSUMER: Function as defined by Java
     * PREDICATE: Function as defined by Java
     * FUNCTION: Function as defined by Java
     *
     * EXTENSION:
     *  PARENTS_EQ(NameMatch) |
     *  PARENTS_EQ_FIRST(NameMatch) |
     *  PARENTS_EQ_LAST(NameMatch) |
     *  TEXT_EQ(TextMatch) |
     *  NAME_EQ(NameMatch) |
     *  TEXT_STARTS_WITH(TextMatch) |
     *  NAME_STARTS_WITH(NameMatch) |
     *  PARENT_SEARCH(QUERY)
     *
     *  And whatever the user defines
     *
     *
     * @param subItem  item to be queried
     * @param commands the list of commands to be processed
     * @return
     */
    @NotNull
    public Stream<T> exec(Stream<T> subItem, Object[] commands, boolean directionDown) {
        for (Object command : commands) {
            //name match
            if (isStringElement(command)) {
                //lets reduce mem consumption by distincting the subset results
                subItem = subItem.distinct();

                String strCommand = ((String) command).trim();
                subItem = elementTypeMatch(subItem, strCommand);
                continue;

            } else if(command instanceof UnaryCommand) {
                UnaryCommand simpleCommand = (UnaryCommand) command;
                if (simpleCommand != null) {//command found
                    switch (simpleCommand) {
                        case CHILD_ELEM:
                            subItem = subItem.flatMap(theItem -> navigationAdapter.findChildren(theItem, el -> Boolean.TRUE).stream());
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
        } else  if(func instanceof QueryExtension) {
            subItem = ((QueryExtension<T>) func).apply(this, subItem);
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
