package DownloadHub;

import java.util.ArrayList;

import DownloadHub.Data.Subscription;

public class ArrayUtil
{
    public static Subscription[] AddToSubscriptionArr(Subscription[] arr, Subscription thing)
    {
        int i;
    
        // create a new array of size n+1
        ArrayList<Subscription> newarr = new ArrayList<Subscription>();
    
        // insert the elements from
        // the old array into the new array
        // insert all elements till n
        // then insert x at n+1
        for (i = 0; i < arr.length; i++)
            newarr.add(arr[i]);
    
        newarr.add(thing);

        Subscription[] tarr = new Subscription[newarr.size()];
        tarr = newarr.toArray(tarr);
    
        return tarr;
    }
}
