package com.intel.realsense.capture;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.intel.realsense.librealsense.AdvancedMode;
import com.intel.realsense.librealsense.Alignment;
import com.intel.realsense.librealsense.Colorizer;
import com.intel.realsense.librealsense.Config;
import com.intel.realsense.librealsense.Decimation;
import com.intel.realsense.librealsense.Device;
import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.DeviceManager;
import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.FrameSet;
import com.intel.realsense.librealsense.Pipeline;
import com.intel.realsense.librealsense.RemoveBackground;
import com.intel.realsense.librealsense.RsContext;
import com.intel.realsense.librealsense.SavingData;
import com.intel.realsense.librealsense.StreamFormat;
import com.intel.realsense.librealsense.StreamType;
import com.intel.realsense.librealsense.VideoFrame;
import com.intel.realsense.librealsense.VideoStreamProfile;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.intel.realsense.capture.SavingFrameData.saveDepthBytes;
import static com.intel.realsense.capture.SavingFrameData.saveIntrinsicParameters;
import static com.intel.realsense.capture.SavingFrameData.saveVideoFrame;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }
    private static final String TAG = "lrs capture example";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    private android.content.Context mContext = this;
    private Button mStartStopButton;
    private Button mCaptureButton;
    private LinearLayoutCompat mButtonPanel;
    Handler mBackgroundHandler;
    Handler mCaptureHandler;
    private Boolean isCaptureDepth=false;
    private boolean isCaptureVideo = false;
    private Boolean isCaptureVideRB = false;
    String mFormatedDate;


    private boolean mIsStreaming = false;
    private final Handler mHandler = new Handler();

    private FrameViewer mColorFrameViewer;
    private FrameViewer mDepthFrameViewer;

    private Config mConfig = new Config();
    private Pipeline mPipeline;
    private Device mDevice;
    private AdvancedMode mAdvancedMode;
    private AdvancedMode.DepthTableControl mDepthTableControl;

    private Alignment mAlignment = new Alignment();
    private RemoveBackground mRemoveBackground;
    RSMeasurement rsMeasurement = new RSMeasurement();

    private ImageView mImageView;

    private DeviceListener mListener = new DeviceListener() {
        @Override
        public void onDeviceAttach() {
            RsContext rsContext = new RsContext();

            mDevice= new Device(rsContext);
            mAdvancedMode = new AdvancedMode(mDevice);
            mDepthTableControl=mAdvancedMode.getmDepthTableControl();
            Log.i(TAG, "onDeviceAttach: depthUnits: "+mDepthTableControl.depthUnits);
            mDepthTableControl.disparityShift = 60;
            mAdvancedMode.setmDepthTableControl(mDepthTableControl);

            mDepthTableControl=mAdvancedMode.getmDepthTableControl();
            mRemoveBackground = new RemoveBackground(40,mDepthTableControl.depthUnits);

            mPipeline = new Pipeline(rsContext);
            mPipeline.getmPipelineProfileHandle();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mButtonPanel.setVisibility(View.VISIBLE);
                }
            });

        }

        @Override
        public void onDeviceDetach() {
            mPipeline = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mButtonPanel.setVisibility(View.GONE);
                }
            });
            try {
                mPipeline.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            mPipeline = null;
            stop();
        }
    };
    private Bitmap mBitmapDepth;
    private ByteBuffer mBufferDepth;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_CAMERA);
            return;
        }
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 9 also requires camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            return;
        }
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    void init(){
        //DeviceManager must be initialized before any interaction with physical RealSense devices.
        DeviceManager.init(mContext);

        //The UsbHub provides notifications regarding RealSense devices attach/detach events via the DeviceListener.
        DeviceManager.getUsbHub().addListener(mListener);

        mColorFrameViewer = new FrameViewer((ImageView) findViewById(R.id.colorImageView));
        mDepthFrameViewer = new FrameViewer((ImageView) findViewById(R.id.depthImageView));

        mStartStopButton = findViewById(R.id.btnStart);
        mButtonPanel = findViewById(R.id.buttonPanel);
        mCaptureButton = findViewById(R.id.btnCapture);

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsStreaming) {
                    stop();
                } else {
                    start();
                }
            }
        });

        mConfig.enableStream(StreamType.DEPTH,-1, 640, 480, StreamFormat.Z16,30);
        mConfig.enableStream(StreamType.COLOR,-1, 640, 480, StreamFormat.RGBA8,30);
        //TODO: Add IR stream
//        mConfig.enableStream(StreamType.INFRARED,-1, 640, 480, StreamFormat.Y8,30);

        mImageView= (ImageView) findViewById(R.id.colorImageView);
    }

    Runnable updateBitmap = new Runnable() {
        @Override
        public void run() {
            try {
                try (FrameSet frames = mPipeline.waitForFrames()) {
                    try (FrameSet processed_ = frames.applyFilter(mAlignment)) {

                        if (isCaptureVideo) {
                            try (Frame f = processed_.first(StreamType.COLOR)) {
                                saveVideoFrame(MainActivity.this,
                                        f.as(VideoFrame.class),
                                        "color_image_"+mFormatedDate+".png");
                            }
                            try (Frame f = processed_.first(StreamType.DEPTH)){
                                String fileName = "depth_byte_"+mFormatedDate+".txt";
                                saveDepthBytes(MainActivity.this,
                                        f.as(VideoFrame.class), fileName);
                                VideoStreamProfile videoStreamProfile =
                                        ((VideoStreamProfile) f.getProfile());
                                saveIntrinsicParameters(MainActivity.this,
                                        videoStreamProfile.getmIntrinsicParameters(),fileName);

                            }
                            isCaptureVideo = false;
                        }


                        // For viewing depth and color frames (background removed)
                        try (FrameSet processed =processed_.applyFilter(mRemoveBackground)) {
                            try (Frame f = processed.first(StreamType.COLOR)) {
                                mColorFrameViewer.show(MainActivity.this, f.as(VideoFrame.class));
                                if (isCaptureVideRB) {
                                    saveVideoFrame(MainActivity.this, f.as(VideoFrame.class),
                                            "color_image_rb.png");
                                    isCaptureVideRB = false;
                                }
                            }
                            try (Frame f = processed.first(StreamType.DEPTH)) {
                                mDepthFrameViewer.show(MainActivity.this, f.as(VideoFrame.class));
                            }
                        } catch (Exception e) { Log.e(TAG, "Remove Background ::" + e.getMessage());}
                    } catch (Exception e) { Log.e(TAG, "Alignment ::" + e.getMessage()); }
                } catch (Exception e) { Log.e(TAG, "Wait for Frames :: " + e.getMessage());}
            } finally {
                mHandler.post(updateBitmap);
            }
        }
    };

    synchronized void start(){
        if(mIsStreaming)
            return;
        mIsStreaming = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartStopButton.setText(R.string.stream_stop);
            }
        });
        startRepeatingTask();
    }

    synchronized void stop(){
        if(mIsStreaming == false)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartStopButton.setText(R.string.stream_start);
            }
        });
        stopRepeatingTask();
        mIsStreaming = false;
    }

    void startRepeatingTask() {
        try {
            mPipeline.start(mConfig);
            mCaptureButton.setVisibility(View.VISIBLE);
            updateBitmap.run();
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(updateBitmap);
        if(mPipeline != null)
            mPipeline.stop();
        mCaptureButton.setVisibility(View.INVISIBLE);
    }


    public native void nSaveDepthColor(long depthHandle,long colorHandle,String filename);
    public native void nSaveDepthwithData(byte [] data,int width,int height,String filename);
    public native void nSaveDepth(long depthHandle,String filename);


    public void captureFrames(View view) {
//        getBackgroundHandler().post(new Runnable() {
//            @Override
//            public void run() {
//                nSaveDepthColor(mBackgroundRemover.getDepthFrame().as(VideoFrame.class).getHandle(),
//                        mBackgroundRemover.getColorFrame().as(VideoFrame.class).getHandle(),getApplication().getFilesDir().getAbsolutePath()+"/"+"depthFrame.xml");
//            }
//        });
        isCaptureDepth = true;
        isCaptureVideo = true;
        isCaptureVideRB = true;
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
        mFormatedDate = sdf.format(new Date());
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }


    private String createFilePath(String filename){
        return getApplication().getFilesDir().getAbsolutePath()+"/"+filename;
    }

}
