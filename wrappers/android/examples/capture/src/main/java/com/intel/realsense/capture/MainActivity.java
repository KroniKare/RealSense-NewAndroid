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
import com.intel.realsense.librealsense.RsContext;
import com.intel.realsense.librealsense.SavingData;
import com.intel.realsense.librealsense.StreamFormat;
import com.intel.realsense.librealsense.StreamType;
import com.intel.realsense.librealsense.VideoFrame;
import com.intel.realsense.librealsense.VideoStreamProfile;

import java.nio.ByteBuffer;

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
    Boolean isCaptureDepth=false;
    private boolean isCaptureVideo = false;



    private boolean mIsStreaming = false;
    private final Handler mHandler = new Handler();

    private FrameViewer mColorFrameViewer;
    private FrameViewer mDepthFrameViewer;

    private Config mConfig = new Config();
    private Pipeline mPipeline;
    private Device mDevice;
    private AdvancedMode mAdvancedMode;
    private AdvancedMode.DepthTableControl mDepthTableControl;

    private Decimation mDecimation = new Decimation();
    private Alignment mAlignment = new Alignment();
    private BackgroundRemover mBackgroundRemover = new BackgroundRemover();
    RSMeasurement rsMeasurement = new RSMeasurement();

    private ImageView mImageView;

    private DeviceListener mListener = new DeviceListener() {
        @Override
        public void onDeviceAttach() {
            RsContext rsContext = new RsContext();

            mDevice= new Device(rsContext);
            mAdvancedMode = new AdvancedMode(mDevice);
//            mDepthTableControl = new AdvancedMode.DepthTableControl();
            mDepthTableControl=mAdvancedMode.getmDepthTableControl();
            Log.i(TAG, "onDeviceAttach: depthUnits: "+mDepthTableControl.depthUnits);
            mDepthTableControl.disparityShift = 60;
            mDepthTableControl.depthUnits = 1000;
//            mDepthTableControl.disparityMode = 0;
//            mDepthTableControl.depthClampMax = 65536;
//            mDepthTableControl.depthClampMin = 0;
            mAdvancedMode.setmDepthTableControl(mDepthTableControl);

            mDepthTableControl=mAdvancedMode.getmDepthTableControl();
            Log.i(TAG, "onDeviceAttach: depthUnits 2: "+mDepthTableControl.depthUnits);


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

        mImageView= (ImageView) findViewById(R.id.colorImageView);
    }

    Runnable updateBitmap = new Runnable() {
        @Override
        public void run() {
            try {
                try(FrameSet frames = mPipeline.waitForFrames())
                {
                    try(FrameSet processed = frames.applyFilter(mAlignment)) {
                        try(Frame f = processed.first(StreamType.COLOR)) {

                            mBackgroundRemover.removeBackground(MainActivity.this, f,8, mImageView);
                            if (isCaptureVideo){
                                mBackgroundRemover.saveColorBitmap(getApplicationContext(),"color_image.png");
                                isCaptureVideo = false;
                            }

                          if (!rsMeasurement.colorIntrinsicRead) {
                              VideoStreamProfile videoStreamProfile = ((VideoStreamProfile) f.getProfile());
                              rsMeasurement.setColorIntrinsicParameters(videoStreamProfile.getmIntrinsicParameters());
                          }

//                            mColorFrameViewer.show(MainActivity.this, mF.as(VideoFrame.class));
                        }
                        try(Frame f = processed.first(StreamType.DEPTH)) {
                            if (isCaptureDepth) {
//                                    final byte[] larray = new byte[f.as(VideoFrame.class).getWidth()*
//                                            f.as(VideoFrame.class).getHeight()];
//                                    f.getData(larray);
//                                    getBackgroundHandler().post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            nSaveDepthwithData(larray,f.as(VideoFrame.class).getWidth(),
//                                                    f.as(VideoFrame.class).getHeight(),
//                                                    getApplication().getFilesDir().getAbsolutePath()+"/"+"depthFrame.xml");
////                                            mBackgroundRemover.saveDepthBitmap(getApplicationContext(),"depthFrame.xml");
//                                            isCaptureDepth=false;
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
                                mBackgroundRemover.saveDepthBitmap(MainActivity.this,"depth.png");
                                mBackgroundRemover.saveDepthBytes(MainActivity.this,"bytes.txt");
                                SavingData savingData = new SavingData(createFilePath("depthFrame.xml"));
                                f.applyFilter(savingData);
                                isCaptureDepth=false;

                            }
                            mBackgroundRemover.setDepthFrame(f);
                            mDepthFrameViewer.show(MainActivity.this, f.as(VideoFrame.class));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
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
