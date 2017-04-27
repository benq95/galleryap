package android.java.pl.galleryap.activity;

import android.content.DialogInterface;
import android.hardware.Camera;
import android.java.pl.galleryap.R;
import android.java.pl.galleryap.service.CameraAccess;
import android.java.pl.galleryap.service.HttpResponseListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Main acitivty used to take and send photos
 */
public class MainActivity extends AppCompatActivity implements HttpResponseListener {

    private CameraPreview mPreview;
    private CameraAccess cameraAccess;
    private ImageButton photoButton;
    private RelativeLayout preview;
    private MainActivity selfInstance;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create an instance of Camera
        cameraAccess = CameraAccess.getInstance(this.getApplicationContext());
        Camera mCamera = cameraAccess.getCameraInstance();
        mCamera.setDisplayOrientation(90);
        selfInstance = this;

        mPreview = new CameraPreview(this, mCamera);
        preview = (RelativeLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        photoButton = (ImageButton) findViewById(R.id.imageButton1);
        photoButton.setOnClickListener(takePhoto);
        photoButton.bringToFront();
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    //take photo listener
    private View.OnClickListener takePhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            enable(false);//disable activity, wait for http response
            cameraAccess.takePhoto(selfInstance);
        }
    };

    //http error event
    @Override
    public void GetError(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Connection error");
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                selfInstance.finish();//close activity while error
            }
        });
        final AlertDialog alert = dialog.create();//show error message
        alert.show();
    }

    //http response event
    @Override
    public void GetResponse() {
        progressBar.setVisibility(View.INVISIBLE);
        enable(true);
    }

    protected boolean enabled = true;

    private void enable(boolean b) {
        enabled = b;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return enabled ?
                super.dispatchTouchEvent(ev) :
                true;
    }
}
