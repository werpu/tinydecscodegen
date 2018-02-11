package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;


/**
 * placeholder for a module file context
 * <p>
 * this is a context which basically implements
 * all the required add operations for our artifacts, including
 * also the includes statement and omitting double insers
 */
public class NgModuleFileContext extends TypescriptFileContext {

    PsiElementContext ngModuleDeclationalPart = null;

    public NgModuleFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
        //fish the ngModuleDeclationalPart


    }

    public NgModuleFileContext(AnActionEvent event) {
        super(event);
    }

    public NgModuleFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public NgModuleFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }


    protected void init() {

    }


    public String getModuleName() {
        Optional<String> moduleClassName = findClassName();
        if(moduleClassName.isPresent()) {
            return  moduleClassName.get();
        }
        return "";
    }


    public Optional<String> findClassName() {
        PsiElement moduleAnnotation = walkPsiTree(getPsiFile(), PsiWalkFunctions::isNgModule, false).get(0);

        List<PsiElement> classDefs = findPsiElements(PsiWalkFunctions::isClass);
        Optional<String> moduleClassDef = classDefs.stream()
                .filter(classDef -> moduleAnnotation.getTextOffset() < classDef.getTextOffset())
                .map(el -> (String) elVis(el, "nameIdentifier", "text").get()).findFirst();

        return moduleClassDef;
    }

    public void appendDeclaration(String variableName) throws IOException {
        //First add the needed declarational part if missing
        //then sync the code back in
        //then add the declaration into the declarational part
        insertUpdateDefSection("declarations", variableName);
    }

    public void appendImports(String variableName) throws IOException {
        insertUpdateDefSection("imports", variableName);
    }

    public void appendExports(String variableName) throws IOException {
        insertUpdateDefSection("exports", variableName);
    }

    public void appendProviders(String variableName) throws IOException {
        insertUpdateDefSection("providers", variableName);
    }

    /**
     * all the needed operations to add a value
     *
     * to a def section in ngmodule aka associative array with an array of
     * values as value {[key: string]: Function | String}
     * @param sectionName
     * @param partName
     * @return
     * @throws IOException
     */
    protected boolean insertUpdateDefSection(String sectionName, String partName) throws IOException {
        Optional<PsiElementContext> ngModuleArgs = queryContent(JS_ES_6_DECORATOR, "TEXT*:(@NgModule)", JS_ARGUMENTS_LIST).findFirst();

        addArgumentsSection(ngModuleArgs);
        addOrInsertSection(sectionName);
        addInsertValue(sectionName, partName);

        return false;
    }

    /**
     * adds or inserts a value if a section already exists
     *
     *
     * @param sectionName
     * @param value
     * @throws IOException
     */
    private void addInsertValue(String sectionName, String value) throws IOException {
        //PsiElement(JS:COMMA)
        Optional<PsiElementContext> propsArray = queryContent(JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY, "NAME:(" + sectionName + ")", JS_ARRAY_LITERAL_EXPRESSION).findFirst();
        boolean hasElement = propsArray.get().queryContent(PSI_ELEMENT_JS_IDENTIFIER).findFirst().isPresent();
        int insertPos =  propsArray.get().queryContent(PSI_ELEMENT_JS_RBRACKET).reduce((el1, el2) -> el2).get().getTextOffset();
        StringBuilder insertStr = new StringBuilder("");
        if(hasElement) {
            insertStr.append(", ");
        }
        insertStr.append(value);
        addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), insertStr.toString()));
        commit();
    }

    /**
     * adds the core of the arguments section
     * @param ngModuleArgs
     * @throws IOException
     */
    private void addArgumentsSection(Optional<PsiElementContext> ngModuleArgs) throws IOException {
        if(ngModuleArgs.get().queryContent(JS_OBJECT_LITERAL_EXPRESSION).findFirst().isPresent()) {
            return;
        }
        addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(ngModuleArgs.get().getTextOffset() + 1), "{}"));
        commit();
    }


    /**
     * adds or inserts a section
     *
     * @param sectionName
     */
    private void addOrInsertSection(String sectionName) throws IOException {
        Optional<PsiElementContext> ngModuleArgs = queryContent(JS_ES_6_DECORATOR, "TEXT*:(@NgModule)", JS_ARGUMENTS_LIST).findFirst();

        if(ngModuleArgs.get().queryContent(JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY, "NAME:(" + sectionName + ")").findFirst().isPresent()) {
            return;
        }

        Optional<PsiElementContext> lastSectionEnd = ngModuleArgs.get().
                //last
                queryContent(PSI_ELEMENT_JS_RBRACKET).reduce((el1, el2) -> el2);
        if(!lastSectionEnd.isPresent()) {
            addSection(sectionName);
        } else {
            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(lastSectionEnd.get().getTextOffset()+1), ",\n    "+sectionName+": []"));

        }
        commit();
    }

    /**
     * adds a section
     * @param sectionName
     */
    private void addSection(String sectionName) throws IOException {
        Optional<PsiElementContext> ngModuleArgs = queryContent(JS_ES_6_DECORATOR, "TEXT*:(@NgModule)", JS_ARGUMENTS_LIST).findFirst();
        PsiElementContext argsList = ngModuleArgs.get();
        int insertPos = argsList.getTextOffset()+ Math.round(argsList.getTextLength() / 2);
        addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), "\n    "+sectionName+": []\n"));
        commit();
    }
}
