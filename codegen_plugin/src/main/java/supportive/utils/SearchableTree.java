package supportive.utils;

import com.google.common.base.Strings;
import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.containers.ArrayListSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import supportive.fs.common.IAngularFileContext;
import toolWindows.supportive.ClickHandler;
import toolWindows.supportive.MouseController;
import toolWindows.supportive.SwingRootParentNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Consumer;

import static supportive.utils.IntellijUtils.convertToSearchableString;

@Getter
@Setter
@NoArgsConstructor
public class SearchableTree<V> {

    private Tree tree = new Tree();
    private TreeSpeedSearch speedSearch;
    private TreeExpansionMonitor expansionMonitor;

    private TreeModel originalModel;

    private String label;




    private void setTitle(DefaultMutableTreeNode root, String title) {
        String newTitle = Strings.nullToEmpty(title);
        Collections.list(root.children()).stream().forEach(node -> {
            ((DefaultMutableTreeNode)node).setUserObject(newTitle);

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
        buildTree(tree, label, treeBuilder);


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

    private void removeEmpties(DefaultMutableTreeNode source) {
        Enumeration<DefaultMutableTreeNode> en = source.depthFirstEnumeration();

        int depth = source.getDepth();
        Set<DefaultMutableTreeNode> nodesToRemove = new ArrayListSet<>();

        //we have to iterate depth times to cover all
        //for(int cnt = 0; cnt < depth; cnt++) {
        Collections.list(en)
                .stream()
                .forEach(node -> {
                    boolean removalCandidate = !(node.getUserObject() instanceof IAngularFileContext) &&
                            (node.getChildCount() == 0 ||
                                    nodesToRemove.containsAll(Collections.list(node.children())));
                    if (removalCandidate) {
                        nodesToRemove.add(node);
                    }

                });
        //}
        nodesToRemove.forEach(DefaultMutableTreeNode::removeFromParent);

    }


    private DefaultMutableTreeNode filteredClone(DefaultMutableTreeNode source, String pathFilter) {
        Enumeration<DefaultMutableTreeNode> en = source.breadthFirstEnumeration();
        DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(source.getUserObject());
        final Map<TreeNode, DefaultMutableTreeNode> nodeIdx = new HashMap<>();

        Collections.list(en).stream()
                // .map(node ->  (DefaultMutableTreeNode) node)
                .filter(node -> {
                    if (node.getParent() == null) {
                        return false;
                    }
                    Object userObject = node.getUserObject();
                    if (userObject instanceof IAngularFileContext && node.isLeaf()) {
                        String vPATH = ((IAngularFileContext) userObject).getVirtualFile().getPath();
                        vPATH = StringUtils.normalizePath(vPATH);
                        return vPATH.toLowerCase().contains(pathFilter.toLowerCase());
                    }
                    return (node.getChildCount() > 0);
                })
                .forEach(node -> {
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getUserObject());
                    nodeIdx.put(node, newNode);

                    DefaultMutableTreeNode nodePar = Optional.ofNullable(nodeIdx.get(node.getParent())).orElse(newRootNode);
                    nodePar.add(newNode);
                });
        return newRootNode;
    }


    private void buildTree(Tree target, String label, Consumer<SwingRootParentNode> c) {
        SwingRootParentNode rootNode = new SwingRootParentNode(label);
        c.accept(rootNode);
        DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
        target.setRootVisible(false);
        target.setModel(newModel);
        originalModel = null;
        this.label = label;
    }

    private <T> void attachSearch(Tree tree, ClickHandler<T, MouseEvent> cHandler, Consumer<TreeExpansionMonitor> applyExpansionMonitor, Consumer<TreeSpeedSearch> applySearchSearch) {
        /*found this useful helper in the jetbrains intellij sources*/

        applyExpansionMonitor.accept(TreeExpansionMonitor.install(tree));


        MouseController contextMenuListener = new MouseController(tree, cHandler);
        tree.addMouseListener(contextMenuListener);

        applySearchSearch.accept(new TreeSpeedSearch(tree, convertToSearchableString(tree)));

    }


}