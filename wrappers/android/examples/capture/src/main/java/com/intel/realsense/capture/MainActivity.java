package com.intel.realsense.capture;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.intel.realsense.librealsense.AdvancedMode;
import com.intel.realsense.librealsense.Alignment;
import com.intel.realsense.librealsense.Config;
import com.intel.realsense.librealsense.Device;
import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.FrameSet;
import com.intel.realsense.librealsense.GLRsSurfaceView;
import com.intel.realsense.librealsense.Pipeline;
import com.intel.realsense.librealsense.RemoveBackground;
import com.intel.realsense.librealsense.RsContext;
import com.intel.realsense.librealsense.StreamFormat;
import com.intel.realsense.librealsense.StreamType;
import com.intel.realsense.librealsense.VideoFrame;
import com.intel.realsense.librealsense.VideoStreamProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.intel.realsense.capture.SavingFrameData.saveDepthBytes;
import static com.intel.realsense.capture.SavingFrameData.saveIntrinsicParameters;
import static com.intel.realsense.capture.SavingFrameData.saveVideoFrame;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "lrs capture example";
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;

    private boolean mPermissionsGrunted = false;
    private android.content.Context mContext = this;
    private LinearLayoutCompat mButtonPanel;
    Handler mBackgroundHandler;
    Handler mCaptureHandler;
    private Boolean isCaptureDepth = false;
    private boolean isCaptureVideo = false;
    private Boolean isCaptureVideoRB = false;
    String mFormatedDate;


    private Context mAppContext;
    private TextView mBackGroundText;
    private GLRsSurfaceView mGLSurfaceView;
    private boolean mIsStreaming = false;
    private final Handler mHandler = new Handler();

    private Pipeline mPipeline;
    private Config mConfig;
    private RsContext mRsContext;
    private Device mDevice;
    private AdvancedMode mAdvancedMode;
    private AdvancedMode.DepthTableControl mDepthTableControl;

    private Alignment mAlignment = new Alignment();
    private RemoveBackground mRemoveBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = getApplicationContext();
        mBackGroundText = findViewById(R.id.connectCameraText);
        mGLSurfaceView = findViewById(R.id.glSurfaceView);
        mGLSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // Android 9 also requires camera permissions
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            return;
        }

        mPermissionsGrunted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            return;
        }
        mPermissionsGrunted = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionsGrunted)
            init();
        else
            Log.e(TAG, "missing permissions");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRsContext.close();
        stop();
    }

    private void init() {
        //RsContext.init must be called once in the application lifetime before any interaction with physical RealSense devices.
        //For multi activities applications use the application context instead of the activity context
        RsContext.init(mAppContext);

        //Register to notifications regarding RealSense devices attach/detach events via the DeviceListener.
        mRsContext = new RsContext();

        mPipeline = new Pipeline(mRsContext);
        mRsContext.setDevicesChangedCallback(mListener);

        mConfig = new Config();

//        mConfig.enableStream(StreamType.DEPTH, 640, 480);
//        mConfig.enableStream(StreamType.COLOR, 640, 480);
        mConfig.enableStream(StreamType.DEPTH, -1, 640, 480, StreamFormat.Z16, 30);
        mConfig.enableStream(StreamType.COLOR, -1, 640, 480, StreamFormat.RGBA8, 30);

        if (mRsContext.getDeviceCount() > 0) {
            showConnectLabel(false);
            start();
        }
    }

    private void showConnectLabel(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBackGroundText.setVisibility(state ? View.VISIBLE : View.GONE);
            }
        });
    }

    private DeviceListener mListener = new DeviceListener() {
        @Override
        public void onDeviceAttach() {
            showConnectLabel(false);
        }

        @Override
        public void onDeviceDetach() {
            showConnectLabel(true);
            stop();
        }
    };

    Runnable mStreaming = new Runnable() {
        @Override
        public void run() {
            try {
                try (FrameSet frames = mPipeline.waitForFrames(1000)) {
                    try (FrameSet processed_ = frames.applyFilter(mAlignment)) {

                        if (isCaptureVideo) {
                            try (Frame f = processed_.first(StreamType.COLOR)) {
                                saveVideoFrame(MainActivity.this,
                                        f.as(VideoFrame.class),
                                        "color_image_" + mFormatedDate + ".png");
                            }
                            try (Frame f = processed_.first(StreamType.DEPTH)) {
                                String fileName = "depth_byte_" + mFormatedDate + ".txt";
                                saveDepthBytes(MainActivity.this,
                                        f.as(VideoFrame.class), fileName);
                                VideoStreamProfile videoStreamProfile =
                                        ((VideoStreamProfile) f.getProfile());
                                saveIntrinsicParameters(MainActivity.this,
                                        videoStreamProfile.getmIntrinsicParameters(), fileName);

                            }
                            isCaptureVideo = false;
                        }
                        mGLSurfaceView.upload(processed_);
                        try (FrameSet processed = processed_.applyFilter(mRemoveBackground)) {
                            mGLSurfaceView.upload(processed);
                            try (Frame f = processed.first(StreamType.COLOR)) {
                                if (isCaptureVideoRB) {
                                    saveVideoFrame(MainActivity.this, f.as(VideoFrame.class),
                                            "color_image_rb.png");
                                    isCaptureVideoRB = false;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Remove Background ::" + e.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Alignment ::" + e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Wait for Frames :: " + e.getMessage());
                }
                mHandler.post(mStreaming);
            } catch (Exception e) {
                Log.e(TAG, "streaming, error: " + e.getMessage());
            }
        }
    };

    private synchronized void start() {
        if (mIsStreaming)
            return;
        try {
            Log.d(TAG, "try start streaming");
            mDevice = new Device(mRsContext);
            mAdvancedMode = new AdvancedMode(mDevice);
            mDepthTableControl = mAdvancedMode.getmDepthTableControl();
            Log.i(TAG, "onDeviceAttach: depthUnits: " + mDepthTableControl.depthUnits);
            mDepthTableControl.disparityShift = 60;
            mAdvancedMode.setmDepthTableControl(mDepthTableControl);
            mDepthTableControl = mAdvancedMode.getmDepthTableControl();
            mRemoveBackground = new RemoveBackground(65, 1000);


            mGLSurfaceView.clear();
            mPipeline.start(mConfig);
            mIsStreaming = true;
            mHandler.post(mStreaming);
            Log.d(TAG, "streaming started successfully");
        } catch (Exception e) {
            Log.d(TAG, "failed to start streaming");
        }
    }

    private synchronized void stop() {
        if (!mIsStreaming)
            return;
        try {
            Log.d(TAG, "try stop streaming");
            mIsStreaming = false;
            mHandler.removeCallbacks(mStreaming);
            mPipeline.stop();
            Log.d(TAG, "streaming stopped successfully");
        } catch (Exception e) {
            Log.d(TAG, "failed to stop streaming");
            mPipeline = null;
        }
    }


    public void captureFrames(View view) {
        isCaptureDepth = true;
        isCaptureVideo = true;
        isCaptureVideoRB = true;
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
        mFormatedDate = sdf.format(new Date());
    }

    private String createFilePath(String filename) {
        return getApplication().getFilesDir().getAbsolutePath() + "/" + filename;
    }

}



