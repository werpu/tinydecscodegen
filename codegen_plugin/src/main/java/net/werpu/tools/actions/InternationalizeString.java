package net.werpu.tools.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.gui.SingleL18n;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.gui.support.IntelliFileContextComboboxModelEntry;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.transformations.L18NTransformationModel;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotTs;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.TEXT_EQ;
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;


/**
 * Action for single string interationalization
 *
 * the idea is
 */
public class InternationalizeString extends AnAction {

    public InternationalizeString() {
    }


    //TODO check whether editor in template or string and template


    @Override
    public void update(AnActionEvent anActionEvent) {
        //must be either typescript or html to be processable
        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        //if typescript file the cursor at least must be in a string
        if(!assertNotTs(ctx)) {
               VisibleAssertions.cursorInTemplate(anActionEvent);
        }
        L18NTransformationModel model = new L18NTransformationModel(new IntellijFileContext(anActionEvent));
        model.getFrom();
    }


    /*
     *
     * Workflow, visible only in template or string ... done‚
     *
     * and
     * then potential replacement (cursor pos)
     *
     * load all potential l18n json files
     *
     *
     * check if the string exists and fetch the key if it exists
     * if not map the string to uppercase lowerdash
     *
     * display the dialog for final editing
     *
     * and then replace the string with a translation key construct
     * (check for already existing translate patterns)
     *
     * Add the json entry if not existing to the bottom of the entries
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        //first action, try to determine the extend of the string  which needs to be parsed
        //theoretically we have three possible constructs
        //attr="string"  --> attr="{{'string' | translate}}"  special case translate="string" -> translate="key"

        //non attr text -> {'key' | translate}
        //non attr {'text' | translate} -> {'key' | translate}

        final IntellijFileContext fileContext = new IntellijFileContext(e);
        L18NTransformationModel model = new L18NTransformationModel(fileContext);

        SingleL18n mainForm = new SingleL18n();

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(fileContext.getProject(), mainForm.getRootPanel())
                .withDimensionKey("SingleL18n")
                .create();
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //TODO mechanism if no json file exists (we implement that later)

        //TODO store the selected json file permanently in case of multiple files (in the project store)
     //   java.util.List<IntelliFileContextComboboxModelEntry> possibleL18nFiles =
     //           Lists.transform(L18NIndexer.getAllAffectedFiles(fileContext.getProject()),
     //                   item -> new IntelliFileContextComboboxModelEntry(new L18NTransformationModel(item)));

        java.util.List<IntelliFileContextComboboxModelEntry> possibleL18nFiles = Lists.transform(L18NIndexer.getAllAffectedFiles(fileContext.getProject()), IntelliFileContextComboboxModelEntry::new);

        //next part, we check whether the string already exists in one of the files
        //first we have to fetch the value of our parsed element
        //first we have to fetch the value of our parsed element

        java.util.List<IntelliFileContextComboboxModelEntry> alreadyExistsIn = possibleL18nFiles.stream()
                .filter(item -> !item.getValue().getKey(model.getValue()).isEmpty())
                .collect(Collectors.toList());

        mainForm.setAllFiles(possibleL18nFiles);
        mainForm.setContainingFiles(alreadyExistsIn);

        mainForm.addFileChangeListener(itemEvent -> {
            IntelliFileContextComboboxModelEntry selectedItem = (IntelliFileContextComboboxModelEntry) itemEvent.getItem();
            applyKey(mainForm, selectedItem, model);
        });



        if(!alreadyExistsIn.isEmpty()) {
            mainForm.switchToContainingFiles();
            applyKey(mainForm, alreadyExistsIn.get(0), model);
        } else {
            mainForm.switchToAllFiles();
            applyKey(mainForm, possibleL18nFiles.get(0), model);
        }


        mainForm.getTxtText().setText(model.getValue());

        //then make a proposal which the user can alter
        //and then save everything away




        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            //handle ok refactoring
        }

    }

    public void applyKey(SingleL18n mainForm, IntelliFileContextComboboxModelEntry selectedItem,   L18NTransformationModel templateModel) {
        List<String> possibleKeys = selectedItem.getValue().getKey(templateModel.getValue());
        //we now transfer all possible keys in
        if(!possibleKeys.isEmpty()) {
            mainForm.getCbL18nKey().setModel(new ListComboBoxModel<>(possibleKeys));
            mainForm.getCbL18nKey().setSelectedItem(possibleKeys.get(0));
        } else {
            if(isNullOrEmpty((String) mainForm.getCbL18nKey().getSelectedItem())) {
                mainForm.getCbL18nKey().setSelectedItem(templateModel.getKey());
            }
        }
    }


    java.util.List<PsiElementContext> getAffextedContexts(IntellijFileContext ctx, java.util.List<IntellijFileContext> files, String value) {

        return files.stream()
                .map(l18nFile -> l18nFile.$q(TEXT_EQ(value)).findFirst().orElse(null))
                .filter(el -> el != null)
                .collect(Collectors.toList());

    }

}
