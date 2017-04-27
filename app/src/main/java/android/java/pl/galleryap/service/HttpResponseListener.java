package android.java.pl.galleryap.service;

/**
 * Created by Wojtek on 2017-04-10.
 */

public interface HttpResponseListener {
    public void GetError(String message);
    public void GetResponse();
}
