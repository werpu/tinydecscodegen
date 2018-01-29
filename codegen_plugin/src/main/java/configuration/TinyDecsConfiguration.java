package configuration;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TinyDecsConfiguration implements Serializable {

    private int returnValueStripLevel  = 0;
    private String sourceRestFramework = "";
    private String targetClientFramework = "";

    private boolean moduleGenerateFolder = false;
    private boolean moduleGenerateStructure = false;
    private boolean moduleExport = false;
    private boolean componentExport = false;
    private boolean directiveExport = false;
    private boolean filterExport = false;


    public TinyDecsConfiguration() {
    }

    public int getReturnValueStripLevel() {
        return returnValueStripLevel;
    }

    public void setReturnValueStripLevel(int returnValueStripLevel) {
        this.returnValueStripLevel = returnValueStripLevel;
    }

    public String getSourceRestFramework() {
        return sourceRestFramework;
    }

    public void setSourceRestFramework(String sourceRestFramework) {
        this.sourceRestFramework = sourceRestFramework;
    }

    public String getTargetClientFramework() {
        return targetClientFramework;
    }

    public void setTargetClientFramework(String targetClientFramework) {
        this.targetClientFramework = targetClientFramework;
    }

    public boolean isModuleGenerateFolder() {
        return moduleGenerateFolder;
    }

    public void setModuleGenerateFolder(boolean moduleGenerateFolder) {
        this.moduleGenerateFolder = moduleGenerateFolder;
    }

    public boolean isModuleGenerateStructure() {
        return moduleGenerateStructure;
    }

    public void setModuleGenerateStructure(boolean moduleGenerateStructure) {
        this.moduleGenerateStructure = moduleGenerateStructure;
    }

    public boolean isModuleExport() {
        return moduleExport;
    }

    public void setModuleExport(boolean moduleExport) {
        this.moduleExport = moduleExport;
    }

    public boolean isComponentExport() {
        return componentExport;
    }

    public void setComponentExport(boolean componentExport) {
        this.componentExport = componentExport;
    }

    public boolean isDirectiveExport() {
        return directiveExport;
    }

    public void setDirectiveExport(boolean directiveExport) {
        this.directiveExport = directiveExport;
    }

    public boolean isFilterExport() {
        return filterExport;
    }

    public void setFilterExport(boolean filterExport) {
        this.filterExport = filterExport;
    }
}
