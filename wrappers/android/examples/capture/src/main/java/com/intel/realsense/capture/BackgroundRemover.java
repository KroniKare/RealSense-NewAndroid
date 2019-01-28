package com.intel.realsense.capture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.VideoFrame;

import java.nio.ByteBuffer;

class BackgroundRemover {
    private Frame depthFrame;
    private Frame colorFrame;
    private long removedFrameHandle;
    private Bitmap mBitmapRemoved;
    private Bitmap mBitmapColor;
    private Bitmap mBitmapDepth;

    private ByteBuffer mBufferDepth;
    private ByteBuffer mBufferColor;


    public BackgroundRemover(){
    }

    public void setColorFrame(Frame colorFrame) {
        this.colorFrame = colorFrame;
    }

    public void setDepthFrame(Frame depthFrame) {
        this.depthFrame = depthFrame;
    }

    public void removeBackground(Activity activity, Frame colorFrame, float depthThreshold, final ImageView imageview){
        if (depthFrame !=null){
            setColorFrame(colorFrame);

            mBitmapColor = Bitmap.createBitmap(colorFrame.as(VideoFrame.class).getWidth(),
                    colorFrame.as(VideoFrame.class).getHeight(), Bitmap.Config.ARGB_8888);
            mBufferColor = ByteBuffer.allocateDirect(mBitmapColor.getByteCount());

            colorFrame.getData(mBufferColor.array());
            mBufferColor.rewind();
            mBitmapColor.copyPixelsFromBuffer(mBufferColor);



            mBitmapDepth = Bitmap.createBitmap(colorFrame.as(VideoFrame.class).getWidth(),
                    colorFrame.as(VideoFrame.class).getHeight(), Bitmap.Config.RGB_565);
            mBufferDepth = ByteBuffer.allocateDirect(mBitmapDepth.getByteCount());

            colorFrame.getData(mBufferDepth.array());
            mBufferColor.rewind();
            mBitmapDepth.copyPixelsFromBuffer(mBufferDepth);

            mBitmapRemoved=Bitmap.createBitmap(mBitmapColor);

            for (int x=0;x<mBitmapDepth.getWidth();x++){
                for (int y=0;y<mBitmapDepth.getHeight();y++){
                    int zValue= mBitmapDepth.getPixel(x,y);
                    long trueValue= Integer.toUnsignedLong(zValue);
                    if (x % 50 ==0 && y % 50 ==0) {
                        Log.i("Main", "removeBackground: " + trueValue);
                    }
//                    if (trueValue){
//                        mBitmapRemoved.setPixel(x,y,setSRGB(255,153,153,153));
//                    }
                }
            }

        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageview.setImageBitmap(mBitmapRemoved);
            }
        });
//        return mBitmapRemoved;

    }

    public int setSRGB(int A, int R, int G, int B){
        int sRGB=0;
        sRGB=(A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        return sRGB;
    }

}
