package refactor;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A simple set of refactorings for the tiny decs,
 * since there is no real Psi Api for Typescript we rely simply on json parsing
 *
 * @NgModule({ name:"moduleName
 * })
 */

@Data
class NgModuleJson {
    String name;
    String[] declarations;
    String[] imports;
    String[] exports;
}


public class TinyRefactoringUtils {

    public static String ngModuleAddDeclare(String declareClass, String contentToAlter) {
        if(contentToAlter.trim().indexOf("@NgModule") == -1) {
            return contentToAlter;
        }
        int skip = calculateSkip(contentToAlter);
       ;

        List<String> ngModuleBlocks = getNgModuleBlocks(contentToAlter);
        String unprocessed  = getPrefix(skip, ngModuleBlocks);
        List<String> codeBlocks = ngModuleBlocks.stream().skip(skip).map(modulePart -> {
            StringBuilder jsonString = getJsonString(modulePart);

            Gson gson = new Gson();
            NgModuleJson moduleData = gson.fromJson(jsonString.toString(), NgModuleJson.class);
            appendDeclare(declareClass, moduleData);
            String replacement = gson.toJson(moduleData);

            return  modulePart.replace(jsonString, replacement);
        }).collect(Collectors.toList());


        List summary = join(unprocessed, codeBlocks);

        return Joiner.on(" @NgModule").join(summary);
    }

    private static int calculateSkip(String contentToAlter) {
        int skip = 1;
        if(contentToAlter.trim().indexOf("@NgModule") == 0) {
            skip = 0;
        }
        return skip;
    }

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
            appendImport(declareClass, moduleData);
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
            appendExport(declareClass, moduleData);
            String replacement = gson.toJson(moduleData);

            return  modulePart.replace(jsonString, replacement);
        }).collect(Collectors.toList());

        List summary = join(unprocessed, codeBlocks);

        return Joiner.on(" @NgModule").join(summary);
    }

    private static void appendDeclare(String declareClass, NgModuleJson moduleData) {
        List<String> declares = Lists.newArrayList(Arrays.asList(moduleData.declarations == null ? new String[0]: moduleData.declarations));
        declares.add(declareClass);
        moduleData.declarations = declares.toArray(new String[declares.size()]);
    }

    private static void appendImport(String declareClass, NgModuleJson moduleData) {
        List<String> declares = Lists.newArrayList(Arrays.asList(moduleData.imports == null ? new String[0]: moduleData.imports));
        declares.add(declareClass);
        moduleData.imports = declares.toArray(new String[declares.size()]);
    }

    private static void appendExport(String declareClass, NgModuleJson moduleData) {
        List<String> exports = Lists.newArrayList(Arrays.asList(moduleData.exports == null ? new String[0]: moduleData.exports));
        exports.add(declareClass);
        moduleData.exports = exports.toArray(new String[exports.size()]);
    }

    private static List<String> getNgModuleBlocks(String contentToAlter) {
        return Arrays.asList(contentToAlter.split("@NgModule"));
    }



    private static StringBuilder getJsonString(String modulePart) {
        modulePart = stripComments(modulePart);
        char[] tokens = modulePart.toCharArray();

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

//^[^@NgModule]*(@NgModule)[\n\(\{\}\)A-Za-z0-9\s]*
