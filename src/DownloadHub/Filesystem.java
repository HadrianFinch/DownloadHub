package DownloadHub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Filesystem
{
    public static String ReadFileAsString(String fileName) throws Exception
    {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    public static boolean WriteToFile(String path, String data)
    {
        try
        {
            Files.write(Paths.get(path), data.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }

    public static void DeleteDirectory(File directory)
    {

        // if the file is directory or not
        if (directory.isDirectory())
        {
            File[] files = directory.listFiles();

            // if the directory contains any file
            if (files != null)
            {
                for (File file : files)
                {

                    // recursive call if the subdirectory is non-empty
                    DeleteDirectory(file);
                }
            }
        }

        directory.delete();
    }
}
