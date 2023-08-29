package DownloadHub;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;

import DownloadHub.Data.DHInfo;
import DownloadHub.Data.Manifest;


public class Downloader
{
    public static void GetFileFromUrl(String filepath, String url) throws IOException
    {
        ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());

        FileOutputStream fileOutputStream = new FileOutputStream(filepath);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

        fileOutputStream.close();
    }

    public static String GetFileContents(String fileUrl) throws IOException
    {
        InputStream in = new URL(fileUrl).openStream();
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    // returns file path that got downloaded
    public static String GetFileFromManifest(String pathTomanifest, Manifest manifest) throws IOException
    {
        int index = pathTomanifest.lastIndexOf('/');
        String serverPath = pathTomanifest.substring(0, index);

        String outputFile = "tmp/" + manifest.file;
        GetFileFromUrl(outputFile, serverPath + "/" + manifest.file);
        
        return outputFile;
    }

    public static void DownloadAndUnpackFromManifest(Manifest manifest, String manifestUrl) throws IOException
    {
        Gson gson = new Gson();

        String tempFilePath = Downloader.GetFileFromManifest(manifestUrl, manifest);
        File tempFile = new File(tempFilePath);

        String destFolderpath = ("downloads/" + manifest.name + "/");

        if (manifest.isZip)
        {
            UnzipUtility unzipUtility = new UnzipUtility();
            unzipUtility.unzip(tempFilePath, destFolderpath);
        }
        else
        {
            File dir = new File(destFolderpath);
            if (!dir.exists())
            {
                Files.createDirectories(Paths.get(destFolderpath));
            }
            
            Files.move(tempFile.toPath(), Paths.get(destFolderpath + tempFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        }

        tempFile.delete();

        // Save dh_info.json in folder
        DHInfo dh_info = new DHInfo(manifest);
        String dh_infoJson = gson.toJson(dh_info, DHInfo.class);
        Filesystem.WriteToFile(destFolderpath + "dh_info.json", dh_infoJson);
    }
}
