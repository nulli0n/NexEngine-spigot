package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ResourceExtractor {

    private final NexPlugin<?> plugin;
    private final File destination;
    private final String fromPath;
    private String regex;

    @NotNull
    public static ResourceExtractor create(@NotNull NexPlugin<?> plugin, @NotNull String fromPath, @NotNull File destination) {
        return new ResourceExtractor(plugin, fromPath, destination);
    }

    private ResourceExtractor(@NotNull NexPlugin<?> plugin, @NotNull String fromPath, @NotNull File destination) {
        this.plugin = plugin;
        this.destination = destination;
        this.fromPath = fromPath;
    }

    @NotNull
    public ResourceExtractor withRegex(@NotNull String regex) {
        this.regex = regex;
        return this;
    }

    public void extract() throws IOException {
        this.extract(false);
    }

    public void extract(boolean override) throws IOException {
        if (!this.destination.exists()) {
            if (!this.destination.mkdirs()) {
                return;
            }
        }

        JarFile jar = new JarFile(this.plugin.getFile());
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String path = entry.getName();
            if (entry.isDirectory() || !path.startsWith(this.fromPath)) continue;

            File file = new File(this.destination, path.replaceFirst(this.fromPath, ""));
            String name = file.getName();
            if (this.regex == null || name.matches(this.regex)) {
                if (file.exists() && override) {
                    if (!file.delete()) {
                        continue;
                    }
                }

                if (!file.exists()) {
                    FileUtil.create(file);
                    InputStream inputStream = jar.getInputStream(entry);
                    FileOutputStream outputStream = new FileOutputStream(file);

                    while (inputStream.available() > 0) {
                        outputStream.write(inputStream.read());
                    }
                    outputStream.close();
                    inputStream.close();
                }
            }
        }

        jar.close();
    }
}