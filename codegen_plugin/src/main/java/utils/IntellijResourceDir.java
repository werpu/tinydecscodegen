package utils;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    public IntellijResourceDir(String relativePath) {
        resource = new File(IntellijResourceDir.class.getResource(".").getFile()).getParentFile();
        resource = new File(resource.getPath() + relativePath);

        try {
            jar = new ZipFile(resource.getPath().substring(5, resource.getPath().indexOf(".jar!") + 4));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.relativePath = relativePath;
    }

    public List<ZipEntry> getAllFiles() {
        String rel = relativePath.substring(relativePath.indexOf("resources/") + "resources/".length());
        return Collections.list(jar.entries()).stream()
                .filter(entry -> entry.getName().indexOf(rel) >= 0)
                .filter(entry -> !entry.isDirectory()).collect(Collectors.toList());
    }

    public void copyTo(File targetDir, TextTransformer transformer) {

        getAllFiles().stream().forEach(file -> {
            File destFile = new File(targetDir.getPath() + "/" + file.getName().substring("projectLayout/tnDec/".length()));
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
                fileName.endsWith(".ts") ||
                fileName.endsWith(".java") ||
                fileName.endsWith(".html") ||
                fileName.endsWith(".css") ||
                fileName.endsWith(".scss") ||
                fileName.endsWith(".sass") ||
                fileName.endsWith(".json");
    }
}
