package supportive.utils;

import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import supportive.fs.common.IAngularFileContext;
import toolWindows.supportive.ClickHandler;
import toolWindows.supportive.MouseController;
import toolWindows.supportive.SwingRootParentNode;

import javax.swing.tree.DefaultTreeModel;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import static supportive.utils.IntellijUtils.convertToSearchableString;

@Getter
@Setter
@NoArgsConstructor
public class SearchableTree<T> {

    private Tree tree = new Tree();
    private TreeSpeedSearch speedSearch;
    private TreeExpansionMonitor expansionMonitor;

    public void addKeyListener(KeyListener l) {
        this.tree.addKeyListener(l);
    }


    public <T> void makeSearchable(ClickHandler<T, MouseEvent > cHandler) {
        if (this.getExpansionMonitor() != null) {
            this.getExpansionMonitor().restore();
        } else {
            attachSearch(this.getTree(),cHandler,
                    expansionMonitor -> this.setExpansionMonitor(expansionMonitor),
                    treeSpeedSearch -> this.setSpeedSearch(treeSpeedSearch)
            );
        }
    }

    public void refreshContent(String label, Consumer<SwingRootParentNode> treeBuilder) {
        buildTree(tree, label, treeBuilder);
    }

    
    private void buildTree(Tree target, String label, Consumer<SwingRootParentNode> c) {
        SwingRootParentNode rootNode = new SwingRootParentNode(label);
        c.accept(rootNode);
        DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
        target.setRootVisible(false);
        target.setModel(newModel);
    }

    private <T> void attachSearch(Tree tree, ClickHandler<T, MouseEvent > cHandler, Consumer<TreeExpansionMonitor> applyExpansionMonitor, Consumer<TreeSpeedSearch> applySearchSearch) {
        /*found this useful helper in the jetbrains intellij sources*/

        applyExpansionMonitor.accept(TreeExpansionMonitor.install(tree));


        MouseController<IAngularFileContext> contextMenuListener = new MouseController(tree, cHandler);
        tree.addMouseListener(contextMenuListener);

        applySearchSearch.accept(new TreeSpeedSearch(tree, convertToSearchableString(tree)));

    }


}
