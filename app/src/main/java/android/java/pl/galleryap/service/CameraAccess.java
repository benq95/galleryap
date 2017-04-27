package android.java.pl.galleryap.service;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Service class to provide camera interaction.
 * Singleton
 */

public class CameraAccess {
    /** A safe way to get an instance of the Camera object. */

    private Camera camera;

    private static CameraAccess instance = null;

    private static Context context;

    private static HttpService httpService;

    //get instance of http service or insert new activity context to it(which is related to already active activity)
    private static void refreshHttpService(Context context){
        CameraAccess.context = context;
        httpService = HttpService.getInstance(context);
    }

    protected CameraAccess(){}

    //singleton pattern implementation
    public static CameraAccess getInstance(Context context){
        refreshHttpService(context);
        if(instance==null){
            instance = new CameraAccess();
            return instance;
        }
        return instance;
    }

    //get instance of camera
    public Camera getCameraInstance(){
        camera = null;
        try {
            camera = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return camera; // returns null if camera is unavailable
    }

    //catch photo and send it to the server
    public void takePhoto(final HttpResponseListener activity){
        //take picture
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    httpService.sendPhoto(data,activity);//send photo to server
                } catch (ServiceException e) {
                    e.printStackTrace();
                    activity.GetError("Internal error, please try again later.");
                }
            }
        });
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


}
