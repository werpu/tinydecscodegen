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

package net.werpu.tools.supportive.fs.common;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.werpu.tools.supportive.utils.IntellijUtils.getTsExtension;
import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;

/**
 * A context pointing to a directory anywhere (jar etc...)
 * <p>
 * We need this class to extract the project data and transform it to our
 * needs. We cannot use the standard intellij template mechanism here
 */
public class IntellijResourceDir {
    File resource;
    ZipFile jar;
    String relativePath;
    String subPath;

    public IntellijResourceDir(String relativePath, String subPath) {
        this.subPath = subPath;
        resource = new File(IntellijResourceDir.class.getResource(".").getFile()).getParentFile();
        String resourcePath = null;
        try {
            resourcePath = URLDecoder.decode(resource.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        resource = new File(resourcePath + relativePath);

        try {

            jar = new ZipFile(resourcePath.substring(5, resourcePath.indexOf(".jar!") + 4));

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
        this.relativePath = relativePath;
    }

    public List<ZipEntry> getAllFiles() {

        String relPath = normalizePath(relativePath);
        String rel = relPath.substring(relPath.indexOf("resources/") + "resources/".length());

        return Collections.list(jar.entries()).stream()
                .filter(entry -> entry.getName().indexOf(rel) >= 0)
                .filter(entry -> !entry.isDirectory()).collect(Collectors.toList());
    }

    public void copyTo(File targetDir, TextTransformer transformer) {

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        getAllFiles().stream().forEach(file -> {
            File destFile = new File(targetDir.getPath() + "/" + file.getName().substring(subPath.length()));
            destFile.getParentFile().mkdirs();
            if (!isTextFile(file.getName())) {

                try {
                    OutputStream target = new BufferedOutputStream(new FileOutputStream(destFile));
                    ByteStreams.copy(jar.getInputStream(file), target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    BufferedReader bufread = new BufferedReader(new InputStreamReader(jar.getInputStream(file)));
                    List<String> replaced = bufread.lines().map(s -> transformer.transform(destFile.getParent(), s)).collect(Collectors.toList());
                    Files.write(Paths.get(destFile.getPath()), replaced);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isTextFile(String fileName) {
        return fileName.endsWith(".txt") ||
                fileName.endsWith(".js") ||
                fileName.endsWith(".md") ||
                fileName.endsWith(getTsExtension()) ||
                fileName.endsWith(".java") ||
                fileName.endsWith(".html") ||
                fileName.endsWith(".css") ||
                fileName.endsWith(".scss") ||
                fileName.endsWith(".sass") ||
                fileName.endsWith(".json");
    }
}
