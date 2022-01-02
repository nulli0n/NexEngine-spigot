package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static void copy(@NotNull InputStream inputStream, @NotNull File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] array = new byte[1024];
            int read;
            while ((read = inputStream.read(array)) > 0) {
                fileOutputStream.write(array, 0, read);
            }
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void mkdir(@NotNull File file) {
        try {
            file.mkdir();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean create(@NotNull File file) {
        if (file.exists())
            return false;

        File parent = file.getParentFile();
        if (parent == null)
            return false;

        parent.mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NotNull
    public static List<File> getFiles(@NotNull String path, boolean deep) {
        List<File> names = new ArrayList<>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return names;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                names.add(file);
            }
            else if (file.isDirectory() && deep) {
                names.addAll(getFiles(file.getPath(), deep));
            }
        }
        return names;
    }

    @NotNull
    public static List<File> getFolders(@NotNull String path) {
        List<File> dirs = new ArrayList<>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return dirs;

        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                dirs.add(file);
            }
        }

        return dirs;
    }

    public static boolean deleteRecursive(@NotNull String path) {
        File dir = new File(path);
        return dir.exists() && deleteRecursive(dir);
    }

    public static boolean deleteRecursive(@NotNull File dir) {
        File[] inside = dir.listFiles();
        if (inside != null) {
            for (File file : inside) {
                deleteRecursive(file);
            }
        }
        return dir.delete();
    }
}
