package net.werpu.tools.supportive.utils;

import com.google.common.base.Strings;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.IAngularFileContext;
import net.werpu.tools.toolWindows.supportive.ClickHandler;
import net.werpu.tools.toolWindows.supportive.MouseController;
import net.werpu.tools.toolWindows.supportive.NodeKeyController;
import net.werpu.tools.toolWindows.supportive.SwingRootParentNode;

import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.werpu.tools.supportive.utils.IntellijRunUtils.invokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.readAction;
import static net.werpu.tools.supportive.utils.IntellijUtils.convertToSearchableString;

@Getter
@Setter
public class SearchableTree<V> {

    private Tree tree = new Tree();
    private TreeSpeedSearch speedSearch;
    private ExpansionMonitor expansionMonitor;

    private TreeModel originalModel;

    private String label;

    DefaultMutableTreeNode lastSelectedNode;
    TreePath[] lastSelectedPaths;

    int[] selectionRows;

    public SearchableTree() {
        new ExpansionMonitor(tree);

        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                invokeLater(() -> readAction(() -> restoreSelection()));
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        });

    }

    private void setTitle(DefaultMutableTreeNode root, String title) {
        String newTitle = Strings.nullToEmpty(title);
        Collections.list(root.children()).stream().forEach(node -> {
            ((DefaultMutableTreeNode) node).setUserObject(newTitle);

        });
    }

    public void addKeyListener(KeyListener l) {
        this.tree.addKeyListener(l);
    }


    public <T> void makeSearchable(ClickHandler<T, MouseEvent> cHandler) {
        if (this.getExpansionMonitor() != null) {
            this.getExpansionMonitor().restore();
        } else {
            attachSearch(this.getTree(), cHandler,
                    this::setExpansionMonitor,
                    this::setSpeedSearch
            );
        }
    }

    public void refreshContent(String label, Consumer<SwingRootParentNode> treeBuilder) {
        saveSelection();

        buildTree(tree, label, treeBuilder);


        //restoreExpansion();

    }

    public void saveSelection() {
        int[] selectionRows = tree.getSelectionRows();
        if (selectionRows != null && selectionRows.length > 0) {
            this.selectionRows = selectionRows;
        }
    }

    public void filterTree(String filterStr, String subTitle) {
        originalModel = (originalModel != null) ? originalModel : tree.getModel();

        filterStr = Strings.nullToEmpty(filterStr);


        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) originalModel.getRoot();

        //we need to double filter to get the empty branches out
        DefaultMutableTreeNode filtered = filteredClone(rootNode, filterStr);
        removeEmpties(filtered);
        DefaultTreeModel newModel = new DefaultTreeModel(filtered);
        setTitle((DefaultMutableTreeNode) newModel.getRoot(), subTitle);
        tree.setModel(newModel);
    }

    public DefaultMutableTreeNode cloneNodes(Function<DefaultMutableTreeNode, Boolean> filterCrit) {
        DefaultMutableTreeNode source = (DefaultMutableTreeNode) tree.getModel().getRoot();

        return cloneNodes(source, filterCrit);
    }

    @NotNull
    private DefaultMutableTreeNode cloneNodes(DefaultMutableTreeNode source, Function<DefaultMutableTreeNode, Boolean> filterCrit) {
        final Function<DefaultMutableTreeNode, Boolean> finalFilterFunc = (filterCrit != null) ? filterCrit : (node) -> true;

        Enumeration en = source.breadthFirstEnumeration();
        DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(source.getUserObject());
        final Map<TreeNode, DefaultMutableTreeNode> nodeIdx = new HashMap<>();

        Collections.list(en).stream()
                // .map(node ->  (DefaultMutableTreeNode) node)
                .filter(node -> {
                    DefaultMutableTreeNode nodeCast = (DefaultMutableTreeNode) node;
                    if (nodeCast.getParent() == null) {
                        return false;
                    }
                    return finalFilterFunc.apply(nodeCast);
                })
                .forEach(node -> {
                    DefaultMutableTreeNode nodeCast = (DefaultMutableTreeNode) node;
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(nodeCast.getUserObject());
                    nodeIdx.put(nodeCast, newNode);

                    DefaultMutableTreeNode nodePar = Optional.ofNullable(nodeIdx.get(nodeCast.getParent())).orElse(newRootNode);
                    nodePar.add(newNode);
                });
        return newRootNode;
    }


    private void removeEmpties(DefaultMutableTreeNode source) {
        Enumeration en =  source.depthFirstEnumeration();

        int depth = source.getDepth();
        Set<DefaultMutableTreeNode> nodesToRemove = new ArrayListSet<>();

        //we have to iterate depth times to cover all
        //for(int cnt = 0; cnt < depth; cnt++) {
        Collections.list(en)
                .stream()
                .forEach(node -> {
                    DefaultMutableTreeNode nodeCast = (DefaultMutableTreeNode) node;

                    boolean removalCandidate = !(nodeCast.getUserObject() instanceof IAngularFileContext) &&
                            (nodeCast.getChildCount() == 0 ||
                                    nodesToRemove.containsAll(Collections.list(nodeCast.children())));
                    if (removalCandidate) {
                        nodesToRemove.add(nodeCast);
                    }

                });
        //}
        nodesToRemove.forEach(DefaultMutableTreeNode::removeFromParent);

    }


    private DefaultMutableTreeNode filteredClone(DefaultMutableTreeNode source, String pathFilter) {
        return cloneNodes(source, node -> {
            Object userObject = node.getUserObject();
            if (userObject instanceof IAngularFileContext && node.isLeaf()) {
                String vPATH = ((IAngularFileContext) userObject).getVirtualFile().getPath();
                vPATH = StringUtils.normalizePath(vPATH);
                return vPATH.toLowerCase().contains(pathFilter.toLowerCase());
            }
            return node.getChildCount() > 0;
        });
    }


    private void buildTree(Tree target, String label, Consumer<SwingRootParentNode> treeBuilder) {
        SwingRootParentNode rootNode = new SwingRootParentNode(label);
        treeBuilder.accept(rootNode);
        DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
        target.setRootVisible(false);
        target.setModel(newModel);

        originalModel = null;
        this.label = label;

    }

    private <T> void attachSearch(Tree tree, ClickHandler<T, MouseEvent> cHandler, Consumer<ExpansionMonitor> applyExpansionMonitor, Consumer<TreeSpeedSearch> applySearchSearch) {
        /*found this useful helper in the jetbrains intellij sources*/

        applyExpansionMonitor.accept(new ExpansionMonitor(tree));


        MouseController contextMenuListener = new MouseController(tree, cHandler);
        tree.addMouseListener(contextMenuListener);

        applySearchSearch.accept(new TreeSpeedSearch(tree, convertToSearchableString(tree)));

    }

    public void restoreExpansion() {
        invokeLater(() -> readAction(() -> this.expansionMonitor.restore()));

    }

    public void restoreSelection() {
        if (selectionRows == null) {
            return;
        }
        TreePath pathForRow = tree.getPathForRow(selectionRows[selectionRows.length - 1]);
        tree.expandPath(pathForRow);

        tree.setSelectionPath(pathForRow);


        Rectangle r = tree.getPathBounds(pathForRow);
        if (selectionRows != null && selectionRows.length > 0 && r == null) {
            //not done yet
            TimeoutWorker.setTimeout(() -> {
                restoreSelection();
                click(r);

            }, 800);
            return;
        } else {
            click(r);

        }

    }

    public void click(Rectangle r) {
        invokeLater(() -> readAction(() -> {
            Arrays.stream(tree.getMouseListeners()).forEach((ml) -> {
                //ml.mousePressed(new MouseEvent(tree, 100000, 10l, 0, (int) r.getCenterX(), (int) r.getCenterY(), 0, 0, 1, false, 0));
                //ml.mouseReleased(new MouseEvent(tree, 100000, 10l, 0, (int) r.getCenterX(), (int) r.getCenterY(), 0, 0, 1, false, 0));
                ml.mouseClicked(new MouseEvent(tree, 100000, 10l, 0, (int) r.getCenterX(), (int) r.getCenterY(), 0, 0, 1, false, 0));

            });
        }));
    }


    /**
     * registers the default behavior for click and double click
     *
     * @param singleClickAction the click action to be performed
     * @param doubleClickAction the double click action to be performed
     * @param <T>               must be of instance IAngularFileContext
     */
    public <T extends IAngularFileContext> void createDefaultClickHandlers(Consumer<T> singleClickAction, Consumer<T> doubleClickAction) {
        MouseListener ml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

                    if (!e.isConsumed()) {
                        e.consume();


                        Object userObject = node.getUserObject();
                        if (!(userObject instanceof IAngularFileContext)) {
                            return;
                        }
                        if (e.getClickCount() < 2) {
                            singleClickAction.accept((T) userObject);
                            return;
                        }

                        doubleClickAction.accept((T) userObject);
                    }
                }
            }
        };
        tree.addMouseListener(ml);
    }

    /**
     * @param enter    enter key pressed handler
     * @param altEnter alt enter key pressed handler
     * @param copy
     * @param <T>      must be of instance IAngularFileContext
     */
    public <T extends IAngularFileContext> void createDefaultKeyController(Consumer<T> enter, Consumer<T> altEnter, Consumer<T> copy) {
        NodeKeyController<T> retVal = new NodeKeyController<>(tree,
                enter, altEnter, copy);
        this.addKeyListener(retVal);
    }


}
