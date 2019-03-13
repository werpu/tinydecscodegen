package net.werpu.tools.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * central configuration storage
 * for storing local values temporarily
 * or permanently and restroring them
 */
@Data
@AllArgsConstructor
public class TinyDecsConfiguration implements Serializable {

    private int returnValueStripLevel = 0;
    private String sourceRestFramework = "";
    private String targetClientFramework = "";

    private boolean moduleGenerateFolder = false;
    private boolean moduleGenerateStructure = false;
    private boolean moduleExport = false;
    private boolean componentExport = false;
    private boolean directiveExport = false;
    private boolean filterExport = false;

    private boolean ngRest = false;
    private String restType = "GET";
    private boolean calcRest = true;
    private boolean syncTs = true;

    private boolean calcRestService = true;

    private String lastSelectedL18NFileAllFiles;

    private String lastSelectedL18NFileExistsFiles;

    public TinyDecsConfiguration() {
    }
    
}
