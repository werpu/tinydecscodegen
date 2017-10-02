/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package refactor;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import dtos.NgModuleJson;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple set of refactorings for the tiny decs,
 * since there is no documented Psi Api
 * for Typescript we rely simply on json parsing
 *
 */
public class TinyRefactoringUtils {

    /**
     * adds a declare entry to an existing ngModule
     *
     * @param declareClass
     * @param contentToAlter
     * @return
     */
    public static String ngModuleAddDeclare(String declareClass, String contentToAlter) {
        if(contentToAlter.trim().indexOf("@NgModule") == -1) {
            return contentToAlter;
        }
        int skip = calculateSkip(contentToAlter);


        List<String> ngModuleBlocks = getNgModuleBlocks(contentToAlter);
        String unprocessed  = getPrefix(skip, ngModuleBlocks);
        List<String> codeBlocks = ngModuleBlocks.stream().skip(skip).map(modulePart -> {
            StringBuilder jsonString = getJsonString(modulePart);

            Gson gson = new Gson();
            NgModuleJson moduleData = gson.fromJson(jsonString.toString(), NgModuleJson.class);
            moduleData.appendDeclare(declareClass);
            String replacement = gson.toJson(moduleData);

            return  modulePart.replace(jsonString, replacement);
        }).collect(Collectors.toList());


        List summary = join(unprocessed, codeBlocks);

        return Joiner.on(" @NgModule").join(summary);
    }

    /**
     * adds an import to an existing ngModule
     *
     * @param declareClass
     * @param contentToAlter
     * @return
     */
    public static String ngModuleAddImport(String declareClass, String contentToAlter) {
        if(contentToAlter.trim().indexOf("@NgModule") == -1) {
            return contentToAlter;
        }
        int skip = calculateSkip(contentToAlter);


        List<String> ngModuleBlocks = getNgModuleBlocks(contentToAlter);
        String unprocessed  = getPrefix(skip, ngModuleBlocks);

        List<String> codeBlocks = ngModuleBlocks.stream().skip(skip).map(modulePart -> {
            StringBuilder jsonString = getJsonString(modulePart);

            Gson gson = new Gson();
            NgModuleJson moduleData = gson.fromJson(jsonString.toString(), NgModuleJson.class);
            moduleData.appendImport(declareClass);
            String replacement = gson.toJson(moduleData);

            return  modulePart.replace(jsonString, replacement);
        }).collect(Collectors.toList());

        List summary = join(unprocessed, codeBlocks);

        return Joiner.on(" @NgModule").join(summary);
    }


    /**
     * Adds an export to an existing ngModule
     *
     * @param declareClass
     * @param contentToAlter
     * @return
     */
    public static String ngModuleAddExport(String declareClass, String contentToAlter) {
        if(contentToAlter.trim().indexOf("@NgModule") == -1) {
            return contentToAlter;
        }
        int skip = calculateSkip(contentToAlter);

        List<String> ngModuleBlocks = getNgModuleBlocks(contentToAlter);
        String unprocessed  = getPrefix(skip, ngModuleBlocks);

        List<String> codeBlocks = ngModuleBlocks.stream().skip(skip).map(modulePart -> {
            StringBuilder jsonString = getJsonString(modulePart);

            Gson gson = new Gson();
            NgModuleJson moduleData = gson.fromJson(jsonString.toString(), NgModuleJson.class);
            moduleData.appendExport(declareClass);
            String replacement = gson.toJson(moduleData);

            return  modulePart.replace(jsonString, replacement);
        }).collect(Collectors.toList());

        List summary = join(unprocessed, codeBlocks);

        return Joiner.on(" @NgModule").join(summary);
    }

    private static String getPrefix(int skip,  List<String> ngModuleBlocks) {
        String unprocessed = "";
        if(skip == 1) {
            unprocessed = ngModuleBlocks.get(0);
        }
        return unprocessed;
    }

    private static List join(String unprocessed, List<String> codeBlocks) {
        List summary = Lists.newLinkedList();
        if(unprocessed != null) {
            summary.add(unprocessed);
        }
        summary.addAll(codeBlocks);
        return summary;
    }

    private static int calculateSkip(String contentToAlter) {
        int skip = 1;
        if(contentToAlter.trim().indexOf("@NgModule") == 0) {
            skip = 0;
        }
        return skip;
    }



    private static List<String> getNgModuleBlocks(String contentToAlter) {
        return Arrays.asList(contentToAlter.split("@NgModule"));
    }


    /**
     * unfortunately java does not have recursive Regexps
     * so we have to fish the NgModule metadata the hard way
     *
     * @param modulePart
     * @return
     */
    public static StringBuilder getJsonString(String modulePart) {
        modulePart = stripComments(modulePart);
        char[] tokens = modulePart.toCharArray();
        if(Strings.isNullOrEmpty(modulePart)) {
            return new StringBuilder("{}");
        }
        modulePart = modulePart.trim();
        if(modulePart.startsWith("{") && modulePart.endsWith("}")) {
            return new StringBuilder(modulePart);
        }

        int cnt = 0;
        int parentheses = 1;
        StringBuilder data = new StringBuilder();
        for (; cnt < tokens.length; cnt++) {
            if (tokens[cnt] == '(') {
                break;
            }
        }
        cnt++;
        for (; cnt < tokens.length && parentheses > 0; cnt++) {
            if (tokens[cnt] == '(') {
                parentheses++;
            } else if (tokens[cnt] == ')') {
                parentheses--;
            } else {
                data.append(tokens[cnt]);
            }
        }

        if (parentheses > 0) {
            throw new RuntimeException("Parentheses in an uneven number after @NgModule");
        }
        return data;
    }

    private static String stripComments(String modulePart) {
        modulePart = modulePart.replaceAll("//[^\\n]+","");
        modulePart = modulePart.replaceAll("/\\*[^(\\*/)]*\\*/","");
        return modulePart;
    }

}
