package DownloadHub;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;

import DownloadHub.Data.DHContent;
import DownloadHub.Data.DHInfo;
import DownloadHub.Data.Manifest;
import DownloadHub.Data.Subscription;
import DownloadHub.Data.Subscriptions;

public class App
{
    public static void PipeStream(InputStream input, OutputStream output) throws IOException
    {
        byte buffer[] = new byte[1024];
        int numRead = 0;

        if (input.available() <= 0)
        {
            return;
        }

        do
        {
            numRead = input.read(buffer);
            output.write(buffer, 0, numRead);
        }
        while (input.available() > 0);

        output.flush();
    }

    private static void RunFromCmd(String[] argstring) throws Exception
    {
        Gson gson = new Gson();

        Subscriptions subscriptions = gson.fromJson(Filesystem.ReadFileAsString("settings/subscriptions.json"), Subscriptions.class);

        if (argstring.length > 0)
        {
            switch (argstring[0])
            {
                case "subscribe":
                case "sub":
                {
                    if (argstring.length >= 2)
                    {
                        try
                        {
                            String manifestContent = Downloader.GetFileContents(argstring[1]);
                            Manifest manifest = gson.fromJson(manifestContent, Manifest.class);

                            Subscription subscription = new Subscription();
                            subscription.autoUpdate = false;
                            subscription.manifestURL = argstring[1];
                            subscription.nickname = manifest.name;
                            subscription.manifestName = manifest.name;

                            System.out.println("[info] Manifest info retrieved. Adding subscription...");

                            subscriptions.active = ArrayUtil.AddToSubscriptionArr(subscriptions.active, subscription);
                            Filesystem.WriteToFile("settings/subscriptions.json", gson.toJson(subscriptions, Subscriptions.class));

                            System.out.println("[info] Subscription to " + manifest.name + " sucessfully added.");
                        }
                        catch (IOException ioE)
                        {
                            System.out.println("[error] Failed to retrive info for requested manifest");
                        }
                    }
                    else
                    {
                        System.out.println("[error] no url to subscribe to was provided.");
                    }

                    break;
                }

                case "update":
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                    for (Subscription sub : subscriptions.active)
                    {
                        String manifestContent;
                        try
                        {
                            manifestContent = Downloader.GetFileContents(sub.manifestURL);
                        }
                        catch (Exception e)
                        {
                            System.out.println("[error] Failed to retrive info for requested manifest " + sub.manifestURL);
                            break;
                        }

                        Manifest manifest = gson.fromJson(manifestContent, Manifest.class);

                        String destFolderpath = ("downloads/" + manifest.name + "/");
                        File dhInfoFile = new File(destFolderpath + "dh_info.json");

                        boolean updateNeeded = false;

                        if (!dhInfoFile.exists())
                        {
                            if (!sub.autoUpdate)
                            {
                                System.out.print("[input] > Do you want to download " + sub.nickname + "? (y/n)\n[input] < ");
                                String s = in.readLine();
                                if (!(s.equals("y") || s.equals("Y")))
                                {
                                    System.out.println("[info] skiping download for " + sub.nickname);
                                    continue;
                                }
                            }

                            updateNeeded = true;
                        }
                        else
                        {
                            DHInfo dhInfo = gson.fromJson(Filesystem.ReadFileAsString(destFolderpath + "dh_info.json"), DHInfo.class);
                            if (dhInfo.version < manifest.version)
                            {
                                System.out.println("[info] Upgrade for " + sub.nickname + " to version " + manifest.version + " is available.");

                                boolean force = (argstring.length >= 2 && argstring[1].equals("-f"));

                                if (!sub.autoUpdate && !force)
                                {
                                    System.out.print("[input] > Do you want to download update for " + sub.nickname + "? (y/n)\n[input] < ");
                                    String s = in.readLine();
                                    if (!(s.equals("y") || s.equals("Y")))
                                    {
                                        System.out.println("[info] skiping update for " + sub.nickname);
                                        continue;
                                    }
                                }

                                updateNeeded = true;
                            }
                        }

                        if (updateNeeded)
                        {
                            System.out.println("[info] Downloading " + sub.nickname + " v" + manifest.version);
                            Downloader.DownloadAndUnpackFromManifest(manifest, sub.manifestURL);
                            System.out.println("[info] Download complete.\n");
                        }
                        else
                        {
                            System.out.println("[info] " + sub.nickname + " is up to date. Currently on v" + manifest.version);
                        }
                    }
                    break;
                }

                case "status":
                {
                    for (Subscription sub : subscriptions.active)
                    {
                        System.out.println(sub.nickname);
                        String manifestContent;
                        try
                        {
                            manifestContent = Downloader.GetFileContents(sub.manifestURL);
                        }
                        catch (Exception e)
                        {
                            System.out.println("    [error] Failed to retrive info for requested manifest " + sub.manifestURL);
                            continue;
                        }

                        Manifest manifest = gson.fromJson(manifestContent, Manifest.class);

                        String destFolderpath = ("downloads/" + manifest.name + "/");
                        File dhInfoFile = new File(destFolderpath + "dh_info.json");

                        if (!dhInfoFile.exists())
                        {
                            System.out.println("    Current Version: n/a (not downloaded)");
                        }
                        else
                        {
                            DHInfo dhInfo = gson.fromJson(Filesystem.ReadFileAsString(destFolderpath + "dh_info.json"), DHInfo.class);
                            System.out.println("    Current Version: " + dhInfo.version);
                        }
                        System.out.println("    Latest Version: " + manifest.version);
                    }

                    if (subscriptions.active.length == 0)
                    {
                        System.out.println("[info] You currently have no subscriptions.");
                    }
                    break;
                }

                case "run":
                case "r":
                {
                    if (argstring.length >= 2)
                    {
                        boolean success = false;
                        for (Subscription sub : subscriptions.active)
                        {
                            if (sub.nickname.equals(argstring[1]))
                            {
                                String destFolderpath = ("downloads/" + sub.manifestName + "/");
                                File dhInfoFile = new File(destFolderpath + "dh_info.json");

                                if (!dhInfoFile.exists())
                                {
                                    break;
                                }

                                success = true;

                                File dhcontentFile = new File(destFolderpath + "dh_content.json");
                                if (!dhcontentFile.exists())
                                {
                                    System.out.println("[error] Requested package is not runnable.");
                                    break;
                                }

                                DHContent dhContent = gson.fromJson(Filesystem.ReadFileAsString(dhcontentFile.getAbsolutePath()), DHContent.class);
                                switch (dhContent.type)
                                {
                                    case "app":
                                    {
                                        System.out.println("[info] Running app.");
                                        System.out.println("    [subprocess] " + (dhContent.exec));

                                        Process process;
                                        try
                                        {
                                            String[] strarr = new String[dhContent.args.length + 1];
                                            strarr[0] = dhContent.exec;

                                            for (int i = 1; i < strarr.length; i++)
                                            {
                                                strarr[i] = dhContent.args[i - 1];
                                            }

                                            ProcessBuilder processBuilder = new ProcessBuilder(strarr);
                                            processBuilder.directory(new File(destFolderpath));
                                            
                                            process = processBuilder.start();
                                            
                                            InputStream is = process.getInputStream();
                                            OutputStream os = process.getOutputStream();
                                            while (process.isAlive())
                                            {
                                                App.PipeStream(is, System.out);

                                                if (System.in.available() > 0)
                                                {
                                                    App.PipeStream(System.in, os);
                                                }
                                            }

                                            int returnCode = process.exitValue();
                                            if (returnCode == 237)
                                            {
                                                String[] strs = new String[3];
                                                strs[0] = "update";
                                                strs[1] = argstring[1];
                                                strs[1] = "-f";

                                                RunFromCmd(strs);
                                            }
                                        }
                                        catch (IOException e)
                                        {
                                            System.out.println("[error] App execution file does not exist. Terminating.");
                                            break;
                                        }

                                        break;
                                    }

                                    default:
                                        System.out.println("[error] Requested package is not runnable.");
                                        break;
                                }
                            }
                        }
                        if (!success)
                        {
                            System.out.println("[error] no package with name " + argstring[1] + " exists");
                        }
                    }
                    else
                    {
                        System.out.println("[error] could not run, no package name givin");
                    }
                    break;
                }

                case "unsubscribe":
                case "unsub":
                {
                    if (argstring.length >= 2)
                    {
                        boolean success = false;
                        Subscription sub = null;
                        for (Subscription s : subscriptions.active)
                        {
                            if (s.nickname.equals(argstring[1]))
                            {
                                sub = s;
                                success = true;
                            }
                        }

                        if (success && (sub != null))
                        {
                            System.out.println("[info] Removing " + sub.nickname + "(" + sub.manifestName + ") from subscriptions");
                            ArrayList<Subscription> subsList = new ArrayList<Subscription>(Arrays.asList(subscriptions.active));

                            subsList.remove(sub);

                            subscriptions.active = new Subscription[subscriptions.active.length - 1];
                            subscriptions.active = subsList.toArray(subscriptions.active);
                            Filesystem.WriteToFile("settings/subscriptions.json", gson.toJson(subscriptions, Subscriptions.class));

                            System.out.println("[info] Deleting downloaded files...");

                            String destFolderpath = ("downloads/" + sub.manifestName + "/");
                            File destFolder = new File(destFolderpath);
                            if (destFolder.exists())
                            {
                                Filesystem.DeleteDirectory(destFolder);
                                System.out.println("[info] Deleted");
                            }
                            else
                            {
                                System.out.println("[warning] Content already deleted?");
                            }

                            System.out.println("[info] Sucessfully unsubscribed from " + sub.manifestURL);
                        }
                        else
                        {
                            System.out.println("[error] No package with name " + argstring[1] + " exists");
                        }
                    }
                    else
                    {
                        System.out.println("[error] No name givin");
                    }
                    break;
                }

                default:
                    System.out.println("[error] Command not known.");
                    break;
            }
        }
    }

    public static void main(String[] argstring) throws Exception
    {
        RunFromCmd(argstring);
    }
}
