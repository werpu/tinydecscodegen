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
