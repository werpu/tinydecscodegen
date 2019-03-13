package net.werpu.tools.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.configuration.ConfigSerializer;
import net.werpu.tools.configuration.TinyDecsConfiguration;
import net.werpu.tools.gui.OverwriteNewDialog;
import net.werpu.tools.gui.OverwriteNewDialogWrapper;
import net.werpu.tools.gui.SingleL18n;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.gui.support.IntelliFileContextComboboxModelEntry;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.L18NDeclFileTransformation;
import net.werpu.tools.supportive.transformations.L18NTransformation;
import net.werpu.tools.supportive.transformations.L18NTransformationModel;
import net.werpu.tools.supportive.transformations.modelHelpers.ElementNotResolvableException;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static net.werpu.tools.actions_all.EditTemplate.TEMPLATE_OF;
import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotTs;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.invokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;

@Data
class DialogHolder {
    private OverwriteNewDialog oDialog;
}


/**
 * Action for single string interationalization
 */
public class InternationalizeString extends AnAction {

    public InternationalizeString() {
    }


    //TODO check whether the editor in template or string and template


    @Override
    public void update(AnActionEvent anActionEvent) {
        //must be either typescript or html to be processable
        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        //if typescript file the cursor at least must be in a string
        if (!assertNotTs(ctx)) {
            VisibleAssertions.cursorInTemplate(anActionEvent);
        }
        try {
            if (anActionEvent.getPresentation().isEnabledAndVisible()) {
                L18NTransformationModel model = new L18NTransformationModel(new IntellijFileContext(anActionEvent));
                model.getFrom();
            }
        } catch (ElementNotResolvableException ex) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
        }
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
        final DialogHolder oDialog = new DialogHolder();

        final DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(fileContext.getProject(), mainForm.getRootPanel())
                .withDimensionKey("SingleL18n")
                .withOkHandler(() -> {
                    String finalKey = (String) mainForm.getCbL18nKey().getSelectedItem();
                    String finalValue = mainForm.getTxtText().getText();
                    IntelliFileContextComboboxModelEntry targetFile = (IntelliFileContextComboboxModelEntry) mainForm.getCbL18NFile().getSelectedItem();
                    Optional<String> foundValue = targetFile.getValue().getValueAsStr(finalKey.split("\\."));

                    finalValue = finalValue.replaceAll("\\s+", "").toLowerCase();
                    if (foundValue.isPresent()) {
                        String sFoundValue = foundValue.get().replaceAll("\\s+", "").toLowerCase();
                        if (!literalEquals(finalValue, sFoundValue)) {
                            oDialog.setODialog(new OverwriteNewDialog());
                            OverwriteNewDialogWrapper oDlgWrapper = new OverwriteNewDialogWrapper(e.getProject(), oDialog.getODialog());


                            oDlgWrapper.pack();
                            oDlgWrapper.show();
                            return oDialog.getODialog().isNewEntryOutcome() || oDialog.getODialog().isOverwriteOutcome();
                        }
                    }
                    return true;

                })
                .create();

        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //TODO mechanism if no json file exists (we implement that later)
        java.util.List<IntelliFileContextComboboxModelEntry> possibleL18nFiles = Lists.transform(L18NIndexer.getAllAffectedFiles(fileContext.getProject()), IntelliFileContextComboboxModelEntry::new);

        //next part, we check whether the string already exists in one of the files
        //first we have to fetch the value of our parsed element
        //first we have to fetch the value of our parsed element
        Editor editor = IntellijUtils.getEditor(e);
        Document document = editor.getDocument();
        java.util.List<IntelliFileContextComboboxModelEntry> alreadyExistsIn = possibleL18nFiles.stream()
                .filter(item -> !item.getValue().getKey(model.getValue()).isEmpty())
                .collect(Collectors.toList());

        mainForm.setAllFiles(possibleL18nFiles);
        mainForm.setContainingFiles(alreadyExistsIn);

        mainForm.addFileChangeListener(itemEvent -> {
            IntelliFileContextComboboxModelEntry selectedItem = (IntelliFileContextComboboxModelEntry) itemEvent.getItem();
            applyKey(mainForm, selectedItem, model);
        });


        if (!alreadyExistsIn.isEmpty()) {
            mainForm.switchToContainingFiles();
            applyKey(mainForm, alreadyExistsIn.get(0), model);
        } else {
            mainForm.switchToAllFiles();
            applyKey(mainForm, possibleL18nFiles.get(0), model);
        }


        mainForm.getTxtText().setText(model.getValue());

        //then make a proposal which the user can alter
        //and then save everything away
        TinyDecsConfiguration conf = ConfigSerializer.getInstance().getState();
        String storedFileNameAll = conf.getLastSelectedL18NFileAllFiles();
        String storedFileNameExists = conf.getLastSelectedL18NFileExistsFiles();
        if(mainForm.getRbAll().isSelected() && !Strings.isNullOrEmpty(storedFileNameAll)) {
            //select the item which matches
            readjustFileSelection(mainForm, storedFileNameAll, true);
        }

        if(mainForm.getRbExists().isSelected() && !Strings.isNullOrEmpty(conf.getLastSelectedL18NFileExistsFiles())) {
           //select the item which matches
            readjustFileSelection(mainForm, storedFileNameExists, false);
        }

        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            //handle ok refactoring

            IntelliFileContextComboboxModelEntry targetFile = (IntelliFileContextComboboxModelEntry) mainForm.getCbL18NFile().getSelectedItem();
            if (targetFile == null) {
                //todo target file creation logic here
                return;
            }


            boolean noConflict = oDialog.getODialog() == null;
            boolean conflictOverwrite = oDialog.getODialog() != null && oDialog.getODialog().isOverwriteOutcome();
            boolean conflictNewEntry = oDialog.getODialog() != null && oDialog.getODialog().isNewEntryOutcome();
            invokeLater(() -> writeTransaction(model.getProject(), () -> {
                try {
                    if (noConflict) {
                        invokeNormalReplacement(fileContext, model, mainForm, document, targetFile);
                    } else if (conflictOverwrite) {
                        invokeOverwriteReplacement(fileContext, model, mainForm, document, targetFile);
                    } else if (conflictNewEntry) {
                        invokeNewEntry(fileContext, model, mainForm, document, targetFile);
                    }

                    VirtualFile virtualFile = targetFile.getValue().getVirtualFile();
                    if(mainForm.getRbAll().isSelected()) {
                        conf.setLastSelectedL18NFileAllFiles(virtualFile.getPath()+virtualFile.getName());
                    }

                    if(mainForm.getRbExists().isSelected()) {
                        conf.setLastSelectedL18NFileExistsFiles(virtualFile.getPath()+virtualFile.getName());
                    }

                } catch (IOException e1) {
                    IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", e1.getMessage());
                    e1.printStackTrace();
                }
            }));

        }

    }

    public void readjustFileSelection(SingleL18n mainForm, String fileName, boolean allFiles) {
        int cnt = 0;
        List<IntelliFileContextComboboxModelEntry> filesModel = (allFiles) ? mainForm.getAllFiles() : mainForm.getContainingFiles();
        for(IntelliFileContextComboboxModelEntry entry: filesModel) {
            String fileName2 = entry.getValue().getVirtualFile().getPath() + entry.getValue().getVirtualFile().getName();
            if(fileName.equals(fileName2)) {
                mainForm.getCbL18NFile().setSelectedIndex(cnt);
            }
            cnt++;
        }
    }


    /**
     * case 2 overwrite the internationalization value with the new one
     */
    public void invokeOverwriteReplacement(IntellijFileContext fileContext, L18NTransformationModel
            model, SingleL18n mainForm, Document document, IntelliFileContextComboboxModelEntry l18nFile) throws IOException {

        String finalKey = (String) mainForm.getCbL18nKey().getSelectedItem();
        L18NFileContext l18nFileContext = l18nFile.getValue();

        updateL18nFileWithNewValue(mainForm, finalKey, l18nFileContext);
        replaceTextWithKey(mainForm, model, l18nFile, finalKey);
        updateShadowEditor(model, document);

    }

    public void updateL18nFileWithNewValue(SingleL18n mainForm, String finalKey, L18NFileContext l18nFileContext) throws IOException {
        Optional<PsiElementContext> foundValue = l18nFileContext.getValue(finalKey);
        //no key present, simply add it as last entry at the end of the L18nfile

        L18NDeclFileTransformation transformation = new L18NDeclFileTransformation(l18nFileContext, finalKey, mainForm.getTxtText().getText());

        l18nFileContext.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());
        l18nFileContext.commit();
        l18nFileContext.reformat();
    }

    /**
     * case 2 simply create a new entry
     */
    public void invokeNewEntry(IntellijFileContext fileContext, L18NTransformationModel model, SingleL18n
            mainForm, Document document, IntelliFileContextComboboxModelEntry l18nFile) throws IOException {

        String key = (String) mainForm.getCbL18nKey().getSelectedItem();
        final String originalKey = key;
        Optional<PsiElementContext> foundValue = l18nFile.getValue().getValue(key);
        int cnt = 0;
        while (foundValue.isPresent()) {
            key = originalKey + "_" + cnt;
            foundValue = l18nFile.getValue().getValue(key);
        }

        replaceTextWithKey(mainForm, model, l18nFile, key);
        updateShadowEditor(model, document);
        model = model.cloneWithNewKey(key);

        addReplaceL18NConfig(model, l18nFile, Optional.empty());

    }


    /**
     * case 1, normal replacement in text editor
     * and or
     */
    public void invokeNormalReplacement(IntellijFileContext fileContext, L18NTransformationModel model, SingleL18n
            mainForm, Document document, IntelliFileContextComboboxModelEntry l18nFile) throws IOException {


        //fetch the key if it is there and then just ignore any changes on the target file and simply insert the key
        //in the editor
        String finalKey = (String) mainForm.getCbL18nKey().getSelectedItem();
        Optional<PsiElementContext> foundItem = l18nFile.getValue().getValue(finalKey);

        //no key present, simply add it as last entry at the end of the L18nfile

        replaceTextWithKey(mainForm, model, l18nFile, finalKey);
        updateShadowEditor(model, document);
        addReplaceL18NConfig(model.cloneWithNewKey(finalKey), l18nFile, foundItem);


    }

    public void addReplaceL18NConfig(L18NTransformationModel transformationModel, IntelliFileContextComboboxModelEntry i18nFile, Optional<PsiElementContext> i18nValue) throws IOException {
        if (!i18nValue.isPresent()) {

            L18NDeclFileTransformation transformation = new L18NDeclFileTransformation(i18nFile.getValue(), transformationModel.getKey() , transformationModel.getValue());
            i18nFile.getValue().addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());

            i18nFile.getValue().commit();
            i18nFile.getValue().reformat();
        }
    }


    public void updateShadowEditor(L18NTransformationModel transformationModel, Document document) {
        //in case of an open template we need to update the template text in the editor
        if (transformationModel.getFileContext().getPsiFile().getVirtualFile().getPath().substring(1).startsWith(TEMPLATE_OF)) {
            //java.util.List<CaretState> caretsAndSelections = editor.getCaretModel().getCaretsAndSelections();
            document.setText(transformationModel.getFileContext().getShadowText());
            //editor.getCaretModel().setCaretsAndSelections(caretsAndSelections);
        }
    }

    public void replaceTextWithKey(SingleL18n uiForm, L18NTransformationModel transformationModel, IntelliFileContextComboboxModelEntry i18nFile, String finalKey) throws IOException {

        L18NTransformation transformation = new L18NTransformation(transformationModel, finalKey, uiForm.getTxtText().getText());

        transformationModel.getFileContext().refactorContent(Arrays.asList(transformation.getTnDecRefactoring()));
        transformationModel.getFileContext().commit();

    }

    /**
     * apply the
     *
     * @param mainForm
     * @param selectedItem
     * @param templateModel
     */
    public void applyKey(SingleL18n mainForm, IntelliFileContextComboboxModelEntry selectedItem, L18NTransformationModel templateModel) {
        List<String> possibleKeys = selectedItem.getValue().getKey(templateModel.getValue());
        //we now transfer all possible keys in
        if (!possibleKeys.isEmpty()) {
            mainForm.getCbL18nKey().setModel(new ListComboBoxModel<>(possibleKeys));
            mainForm.getCbL18nKey().setSelectedItem(possibleKeys.get(0));
        } else {
            if (isNullOrEmpty((String) mainForm.getCbL18nKey().getSelectedItem())) {
                mainForm.getCbL18nKey().setSelectedItem(templateModel.getKey());
            }
        }
    }

}
