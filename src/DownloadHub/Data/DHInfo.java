package DownloadHub.Data;

public class DHInfo
{
    public int version;

    public DHInfo(Manifest manifest)
    {
        this.version = manifest.version;
    }
}
