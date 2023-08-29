package DownloadHub;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class CmdArgs
{
    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "subscribe" }, description = "subscribe to a new manifest")
    public String addSubscriptions = "";

    // @Parameter(names = { "-v", "--verbose" }, description = "print all file names")
    // public boolean verbose = false;

    // @Parameter(names = { "-ng", "--nogui" }, description = "dont open the search GUI")
    // public boolean noGUI = false;

    // @Parameter(names = { "-c", "--count" }, description = "Display the top x results")
    // public int resultCount = 10;

    // @Parameter(names = { "-k", "--keyword" }, description = "Display the top x results")
    // public String keyword;

    // @Parameter(names = { "-p", "--profile" }, description = "which search profile to use")
    // public String profileName;
}