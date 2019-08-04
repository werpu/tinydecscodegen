/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
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
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.i18n.*;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ElementNotResolvableException;
import net.werpu.tools.supportive.utils.FileEndings;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
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
 * <p>
/TODO json only case
 */
public class InternationalizeString extends AnAction {

    public InternationalizeString() {
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        //must be either typescript or html to be processable
        VisibleAssertions.templateVisible(anActionEvent);
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }


        try {
            IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
            //if typescript file the cursor at least must be in a string
            if (!assertNotTs(ctx)) {
                VisibleAssertions.cursorInTemplate(anActionEvent);
            }

            if (anActionEvent.getPresentation().isEnabledAndVisible()) {
                I18NTransformationModel model = new I18NTransformationModel(new IntellijFileContext(anActionEvent));
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
     *ƒ
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
        I18NTransformationModel model = new I18NTransformationModel(fileContext);

        SingleL18n mainForm = new SingleL18n(fileContext.getProject());
        final DialogHolder oDialog = new DialogHolder();

        Project project = fileContext.getProject();
        final DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.getRootPanel())
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
                    mainForm.saveSettings();
                    return true;

                })
                .create();

        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));
        final java.util.List<IntelliFileContextComboboxModelEntry> possibleL18nFiles = new ArrayList<>();
        possibleL18nFiles.addAll(createComboboxEntries(project));

        mainForm.getBtnLoadCreate().addActionListener((ActionEvent event) -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            descriptor.setTitle("Select i18n target directory");
            descriptor.setDescription("Please choose a file or target directory for your internationalization file");
            FileChooserDescriptor descriptor1 = new FileChooserDescriptor(true, true, false, false, false, false);
            selectOrCreateI18nFile(project, possibleL18nFiles, descriptor1);

        });


        //no l18n file exists,
        if (possibleL18nFiles.isEmpty()) {
            //no i18n file found we make a dialog prompt to create one
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            descriptor.setTitle("Select i18n target directory");
            descriptor.setDescription("Please choose a target directory for your internationalization file");


            selectOrCreateI18nFile(project, possibleL18nFiles, descriptor);

        }


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
            applyFileName(mainForm, selectedItem);
        });


        if (!alreadyExistsIn.isEmpty()) {
            mainForm.switchToContainingFiles();
            applyKey(mainForm, alreadyExistsIn.get(0), model);
        } else {
            mainForm.switchToAllFiles();
            applyKey(mainForm, possibleL18nFiles.get(0), model);
            applyFileName(mainForm, possibleL18nFiles.get(0));
        }


        mainForm.getTxtText().setText(model.getValue());

        //then make a proposal which the user can alter
        //and then save everything away
        TinyDecsConfiguration conf = ConfigSerializer.getInstance().getState();
        String storedFileNameAll = conf.getLastSelectedL18NFileAllFiles();
        String storedFileNameExists = conf.getLastSelectedL18NFileExistsFiles();
        if (mainForm.getRbAll().isSelected() && !Strings.isNullOrEmpty(storedFileNameAll)) {
            //select the item which matches
            readjustFileSelection(mainForm, storedFileNameAll, true);
        }

        if (mainForm.getRbExists().isSelected() && !Strings.isNullOrEmpty(conf.getLastSelectedL18NFileExistsFiles())) {
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

            boolean isMultiType = mainForm.isMultiType();


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
                    if (mainForm.getRbAll().isSelected()) {
                        conf.setLastSelectedL18NFileAllFiles(virtualFile.getPath() + virtualFile.getName());
                    }

                    if (mainForm.getRbExists().isSelected()) {
                        conf.setLastSelectedL18NFileExistsFiles(virtualFile.getPath() + virtualFile.getName());
                    }

                } catch (IOException e1) {
                    IntellijUtils.showErrorDialog(project, "Error", e1.getMessage());
                    e1.printStackTrace();
                }
            }));

        }

    }


    @NotNull
    public List<IntelliFileContextComboboxModelEntry> createComboboxEntries(Project project) {
        List<IntellijFileContext> allAffectedFiles = L18NIndexer.getAllAffectedFiles(project);
        //we have to group them by name and type
        allAffectedFiles = sortByName(allAffectedFiles);

        IntellijFileContext lastJSonFile = null;
        IntellijFileContext lastTsFile = null;
        List<IntelliFileContextComboboxModelEntry> retVal = new ArrayList<>();
        for (IntellijFileContext affectedFile : allAffectedFiles) {
            FileTypeGrouper applyValues = new FileTypeGrouper(lastJSonFile, lastTsFile, retVal, affectedFile).invoke();
            if (applyValues.is()) break;
            lastJSonFile = applyValues.getLastJSonFile();
            lastTsFile = applyValues.getLastTsFile();
        }
        new FileTypeGrouper(lastJSonFile, lastTsFile, retVal, null).invoke();

        return retVal;
    }

    @NotNull
    public List<IntellijFileContext> sortByName(List<IntellijFileContext> allAffectedFiles) {
        return allAffectedFiles.stream().sorted((item1, item2) -> {
            if (item1 == null && item2 == null) {
                return 0;
            }
            if (item1 == null) {
                return -1;
            }
            if (item2 == null) {
                return 1;
            }
            return (item1.getFileName()).compareTo(item2.getFileName());
        }).collect(Collectors.toList());
    }

    public boolean isOverlapping(IntellijFileContext lastJSonFile, IntellijFileContext lastTsFile) {
        return lastTsFile != null && lastJSonFile != null && lastTsFile.getBaseName().equals(lastJSonFile.getBaseName());
    }

    private void applyFileName(SingleL18n mainForm, IntelliFileContextComboboxModelEntry selectedItem) {
        mainForm.getLblFileName().setText(selectedItem.getValue().getModuleRelativePath());
    }

    private void selectOrCreateI18nFile(Project project, List<IntelliFileContextComboboxModelEntry> possibleL18nFiles, FileChooserDescriptor descriptor) {
        final VirtualFile vfile1 = FileChooser.chooseFile(descriptor, project, project.getProjectFile());
        if (vfile1 != null) {
            writeTransaction(project, () -> {
                try {
                    if (vfile1.isDirectory()) {
                        VirtualFile i18nFile = IntellijUtils.createFileDirectly(project, vfile1, "{}", FileEndings.LABELS_EN_JSON);
                        final IntellijFileContext alternative = alternativeFileExists(project, i18nFile);
                        possibleL18nFiles.addAll(Lists.transform(Arrays.asList(new IntellijFileContext(project, i18nFile)), e -> new IntelliFileContextComboboxModelEntry(e, alternative)));
                    } else {
                        final IntellijFileContext alternative = alternativeFileExists(project, vfile1);
                        possibleL18nFiles.addAll(Lists.transform(Arrays.asList(new IntellijFileContext(project, vfile1)), e -> new IntelliFileContextComboboxModelEntry(e, alternative)));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Messages.showErrorDialog(project, ex.getMessage(), net.werpu.tools.actions_all.shared.Messages.ERR_OCCURRED);
                }
            });

        }
    }


    @Nullable
    public IntellijFileContext alternativeFileExists(Project project, VirtualFile i18nFile) {

        IntellijFileContext intellijFileContext = new IntellijFileContext(project, i18nFile);

        String path = i18nFile.getPath();
        String rawName = path.substring(0, path.lastIndexOf("."));
        String extension = i18nFile.getExtension();
        String subExtension = extension.equals(FileEndings.TS) ? FileEndings.JSON : FileEndings.TS;
        String alternatePath = rawName + "." + subExtension;
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file:///" + alternatePath);
        if (virtualFile != null) {
            return new IntellijFileContext(project, virtualFile);
        }
        return null;
    }

    public void readjustFileSelection(SingleL18n mainForm, String fileName, boolean allFiles) {
        int cnt = 0;
        List<IntelliFileContextComboboxModelEntry> filesModel = (allFiles) ? mainForm.getAllFiles() : mainForm.getContainingFiles();
        for (IntelliFileContextComboboxModelEntry entry : filesModel) {
            String fileName2 = entry.getValue().getVirtualFile().getPath() + entry.getValue().getVirtualFile().getName();
            if (fileName.equals(fileName2)) {
                mainForm.getCbL18NFile().setSelectedIndex(cnt);
            }
            cnt++;
        }
    }


    /**
     * case 2 overwrite the internationalization value with the new one
     */
    public void invokeOverwriteReplacement(IntellijFileContext fileContext, I18NTransformationModel
            model, SingleL18n mainForm, Document document, IntelliFileContextComboboxModelEntry l18nFile) throws IOException {

        String finalKey = (String) mainForm.getCbL18nKey().getSelectedItem();
        String prefix = mainForm.getTxtPrefix().getText();
        boolean typescriptReplacement = mainForm.isTypescriptReplacement();
        I18NFileContext i18NFileContext = l18nFile.getValue();

        updateL18nFileWithNewValue(mainForm, finalKey, i18NFileContext);
        replaceTextWithKey(mainForm, model, l18nFile, typescriptReplacement, prefix, finalKey);
        updateShadowEditor(model, document);

    }

    public void updateL18nFileWithNewValue(SingleL18n mainForm, String finalKey, I18NFileContext i18NFileContext) throws IOException {
        Optional<PsiElementContext> foundValue = i18NFileContext.getValue(finalKey);
        //no key present, simply add it as last entry at the end of the L18nfile
        if (IntellijUtils.isJSON(i18NFileContext.getVirtualFile().getFileType())) {
            I18NJsonDeclFileTransformation transformation = new I18NJsonDeclFileTransformation(i18NFileContext, finalKey, mainForm.getTxtText().getText());
            i18NFileContext.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());
        } else {
            I18NTypescriptDeclFileTransformation transformation = new I18NTypescriptDeclFileTransformation(i18NFileContext, finalKey, mainForm.getTxtText().getText());
            i18NFileContext.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());
        }
        i18NFileContext.commit();
        i18NFileContext.reformat();
    }

    /**
     * case 2 simply create a new entry
     */
    public void invokeNewEntry(IntellijFileContext fileContext, I18NTransformationModel model, SingleL18n
            mainForm, Document document, IntelliFileContextComboboxModelEntry l18nFile) throws IOException {

        String key = (String) mainForm.getCbL18nKey().getSelectedItem();
        String prefix = (String) mainForm.getTxtPrefix().getText();
        boolean typescriptReplacement = mainForm.isTypescriptReplacement();
        final String originalKey = key;
        Optional<PsiElementContext> foundValue = l18nFile.getValue().getValue(key);
        int cnt = 0;
        while (foundValue.isPresent()) {
            key = originalKey + "_" + cnt;
            foundValue = l18nFile.getValue().getValue(key);
        }

        replaceTextWithKey(mainForm, model, l18nFile, typescriptReplacement, prefix, key);
        updateShadowEditor(model, document);
        final I18NTransformationModel finalModel = model.cloneWithNewKey(key);

        //fetch the key if it is there and then just ignore any changes on the target file and simply insert the key
        //in the editor

        I18NFileContext tsFile = l18nFile.getTsFile().orElse(null);
        I18NFileContext jsonFile = l18nFile.getJSONFile().orElse(null);
        if (mainForm.getRbTS().isSelected()) {
            updateI18nFile(finalModel, tsFile);
        } else {

            if (mainForm.getRbBoth().isSelected()) { //both
                updateI18nFile(finalModel, tsFile);
                updateI18nFile(finalModel, jsonFile);
            } else {
                updateI18nFile(finalModel, jsonFile);
            }
        }
    }


    /**
     * case 1, normal replacement in text editor
     * and or
     */
    public void invokeNormalReplacement(IntellijFileContext fileContext, I18NTransformationModel model, SingleL18n
            mainForm, Document document, IntelliFileContextComboboxModelEntry l18nFile) throws IOException {


        String finalKey = (String) mainForm.getCbL18nKey().getSelectedItem();
        String prefix = (String) mainForm.getTxtPrefix().getText();
        boolean typescriptReplacement = mainForm.isTypescriptReplacement();
        Optional<PsiElementContext> foundItem = l18nFile.getValue().getValue(finalKey);

        //no key present, simply add it as last entry at the end of the L18nfile

        replaceTextWithKey(mainForm, model, l18nFile, typescriptReplacement, prefix, finalKey);
        updateShadowEditor(model, document);

        //fetch the key if it is there and then just ignore any changes on the target file and simply insert the key
        //in the editor


        I18NTransformationModel cloned = model.cloneWithNewKey(finalKey);
        I18NFileContext tsFile = l18nFile.getTsFile().orElse(null);
        if (mainForm.getRbTS().isSelected()) {
            updateI18nFile(Optional.of(finalKey), cloned, tsFile);
        } else {
            I18NFileContext jsonFile = l18nFile.getJSONFile().orElse(null);
            if (mainForm.getRbBoth().isSelected()) { //both
                if (tsFile != null) {
                    updateI18nFile(Optional.of(finalKey), cloned, tsFile);
                }
                if (jsonFile != null) {
                    updateI18nFile(Optional.of(finalKey), cloned, jsonFile);
                }
            } else {
                updateI18nFile(Optional.of(finalKey), cloned, jsonFile);
            }
        }
    }

    public void updateI18nFile(Optional<String> finalKey, I18NTransformationModel modelSupplier, I18NFileContext fileProducer) throws IOException {
        addReplaceL18NConfig(modelSupplier, fileProducer, fileProducer.getValue(finalKey.get()));
    }

    public void updateI18nFile(I18NTransformationModel modelSupplier, I18NFileContext fileProducer) throws IOException {
        addReplaceL18NConfig(modelSupplier, fileProducer, Optional.empty());
    }

    public void addReplaceL18NConfig(I18NTransformationModel transformationModel, I18NFileContext i18nFile, Optional<PsiElementContext> i18nValue) throws IOException {
        if (!i18nValue.isPresent()) {


            if (IntellijUtils.isJSON(i18nFile.getVirtualFile().getFileType())) {
                I18NJsonDeclFileTransformation transformation = new I18NJsonDeclFileTransformation(i18nFile, transformationModel.getKey(), transformationModel.getValue());
                i18nFile.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());
            } else {
                I18NTypescriptDeclFileTransformation transformation = new I18NTypescriptDeclFileTransformation(i18nFile, transformationModel.getKey(), transformationModel.getValue());
                i18nFile.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());
            }

            i18nFile.commit();
            i18nFile.reformat();
        }
    }


    public void updateShadowEditor(I18NTransformationModel transformationModel, Document document) {
        //in case of an open template we need to update the template text in the editor
        if (transformationModel.getFileContext().getPsiFile().getVirtualFile().getPath().substring(1).startsWith(TEMPLATE_OF)) {
            document.setText(transformationModel.getFileContext().getShadowText());
        }
    }

    public void replaceTextWithKey(SingleL18n uiForm, I18NTransformationModel transformationModel, IntelliFileContextComboboxModelEntry i18nFile, boolean typescriptReplacement, String prefix, String finalKey) throws IOException {

        if(!typescriptReplacement) {
            I18NAngularTranslateTransformation transformation =  new I18NAngularTranslateTransformation(transformationModel, finalKey, uiForm.getTxtText().getText());
            transformationModel.getFileContext().refactorContent(Arrays.asList(transformation.getTnDecRefactoring()));
            transformationModel.getFileContext().commit();
        } else {

            I18NTypescriptTransformation transformation =  new I18NTypescriptTransformation(transformationModel, prefix, finalKey, uiForm.getTxtText().getText());
            transformationModel.getFileContext().refactorContent(Arrays.asList(transformation.getTnDecRefactoring()));
            transformationModel.getFileContext().commit();
        }

    }

    /**
     * apply the
     *
     * @param mainForm
     * @param selectedItem
     * @param templateModel
     */
    public void applyKey(SingleL18n mainForm, IntelliFileContextComboboxModelEntry selectedItem, I18NTransformationModel templateModel) {
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

    /**
     * GroupingHelper
     */
    private class FileTypeGrouper {
        private boolean myResult;
        private IntellijFileContext lastJSonFile;
        private IntellijFileContext lastTsFile;
        private List<IntelliFileContextComboboxModelEntry> retVal;
        private IntellijFileContext affectedFile;

        public FileTypeGrouper(IntellijFileContext lastJSonFile, IntellijFileContext lastTsFile, List<IntelliFileContextComboboxModelEntry> retVal, IntellijFileContext affectedFile) {
            this.lastJSonFile = lastJSonFile;
            this.lastTsFile = lastTsFile;
            this.retVal = retVal;
            this.affectedFile = affectedFile;
        }

        boolean is() {
            return myResult;
        }

        public IntellijFileContext getLastJSonFile() {
            return lastJSonFile;
        }

        public IntellijFileContext getLastTsFile() {
            return lastTsFile;
        }

        public FileTypeGrouper invoke() {

            boolean isJson = affectedFile != null ? IntellijUtils.isJSON(affectedFile.getPsiFile().getFileType()) : true;
            boolean isTS = affectedFile != null ? IntellijUtils.isTypescript(affectedFile.getPsiFile().getFileType()) : true;

            if (isOverlapping(lastJSonFile, lastTsFile)) {
                retVal.add(new IntelliFileContextComboboxModelEntry(lastJSonFile, lastTsFile));
                lastJSonFile = null;
                lastTsFile = null;
                myResult = true;
                return this;
            }

            if ((lastJSonFile != null && isJson)) {
                retVal.add(new IntelliFileContextComboboxModelEntry(lastJSonFile));

            } else if ((lastTsFile != null && isTS)) {
                retVal.add(new IntelliFileContextComboboxModelEntry(lastTsFile));
            }
            if (affectedFile != null) {
                if (isJson) {
                    lastJSonFile = affectedFile;
                } else {
                    lastTsFile = affectedFile;
                }
            }
            myResult = false;
            return this;
        }
    }
}
