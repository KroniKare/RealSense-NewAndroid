package com.intel.realsense.capture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.VideoFrame;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.Utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class BackgroundRemover {
    private Frame depthFrame;
    private Frame colorFrame;
    private long removedFrameHandle;
    private Bitmap mBitmapRemoved;
    private Bitmap mBitmapColor;
    private Bitmap mBitmapDepth;

    private ByteBuffer mBufferDepth;
    private ByteBuffer mBufferColor;
    private static final String TAG = "BackGroundRemover";


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
            mBufferColor = ByteBuffer.allocateDirect(colorFrame.as(VideoFrame.class).getStride()*colorFrame.as(VideoFrame.class).getHeight());
//            Log.i(TAG, "removeBackground: Buffer Size: "+mBufferColor.array().length);
//            Log.i(TAG, "removeBackground: Size from code: "+colorFrame.as(VideoFrame.class).getStride()*colorFrame.as(VideoFrame.class).getHeight());

            colorFrame.getData(mBufferColor.array());
            mBufferColor.rewind();
            mBitmapColor.copyPixelsFromBuffer(mBufferColor);



            mBitmapDepth = Bitmap.createBitmap(depthFrame.as(VideoFrame.class).getWidth(),
                    depthFrame.as(VideoFrame.class).getHeight(), Bitmap.Config.RGB_565);
//            mBufferDepth = ByteBuffer.allocateDirect(mBitmapDepth.getByteCount());
            mBufferDepth = ByteBuffer.allocateDirect(depthFrame.as(VideoFrame.class).getStride()*depthFrame.as(VideoFrame.class).getHeight());
            Log.i(TAG, "removeBackground: Buffer Size Before: "+mBufferDepth.array().length);
//            Log.i(TAG, "removeBackground: Size from code: "+depthFrame.as(VideoFrame.class).getStride()*depthFrame.as(VideoFrame.class).getHeight());
//            Log.i(TAG, "removeBackground:  BitsPerPixel :" +depthFrame.as(VideoFrame.class).getBitsPerPixel());

            depthFrame.getData(mBufferDepth.array());
            mBufferDepth.rewind();
            mBitmapDepth.copyPixelsFromBuffer(mBufferDepth);

            mBitmapRemoved=Bitmap.createBitmap(mBitmapColor);

            //TODO: Write renderscript to do faster
            for (int x=0;x<mBitmapDepth.getWidth();x++){
                for (int y=0;y<mBitmapDepth.getHeight();y++){
                    int zValue= mBitmapDepth.getPixel(x,y);
                    int trueValue=0,trueValueDiv=0;
                    trueValue = (zValue & 0xffff) ;
                    trueValueDiv = trueValue / 1000;
                    if (zValue > -65536){
//                        Log.d(TAG, "removeBackground: Value is below zero");
                    } else {
                        Log.d(TAG, "removeBackground: Value is below known limit");
                    }
//                    if (x % 50 == 0 && y % 50 == 0) {
//                        Log.d(TAG, "removeBackground: zValue: " + zValue);
//                        Log.d(TAG, "removeBackground: true Value: " + trueValue);
//                        Log.d(TAG, "removeBackground: trueValue/1000: " + trueValueDiv);
//                    }
                    if (trueValueDiv > (int) depthThreshold || trueValueDiv <= 0){
                        mBitmapRemoved.setPixel(x,y,setSRGB(255,153,153,153));
                    }
                }
            }

        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageview.setImageBitmap(mBitmapRemoved);
            }
        });

    }



    public void writeToFile(byte[] data, String fileName) throws IOException{
        FileOutputStream out = new FileOutputStream(fileName,true);
        out.write(data);
        out.close();
    }

    public void saveDepthBytes(final  Context context, final  String filename){
        File file = new File(context.getFilesDir().getAbsolutePath(), filename);
        try {
            Log.i(TAG, "removeBackground: Buffer Size: "+mBufferDepth.array().length);
            writeToFile(mBufferDepth.array(),file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public int setSRGB(int A, int R, int G, int B){
        int sRGB=0;
        sRGB=(A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        return sRGB;
    }

    public Frame getColorFrame() {
        return colorFrame;
    }

    public Frame getDepthFrame() {
        return depthFrame;
    }

    public void saveColorBitmap(final Context context, final String filename) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                File file = new File(context.getFilesDir().getAbsolutePath(), filename);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    mBitmapColor.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
    }

//    public void saveDepthBitmap(final Context context, final String filename) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                File file = new File(context.getFilesDir().getAbsolutePath(), filename);
//                Mat tempMat= new Mat(mBitmapDepth.getHeight(),mBitmapDepth.getWidth(), CvType.CV_32FC1);
//                Utils.bitmapToMat(mBitmapDepth,tempMat);
//                nSaveDepthMat(tempMat.nativeObj,file.getAbsolutePath());
//            }
//        };
//        runnable.run();
//    }

    public static Bitmap createBitmapFromFrame(Frame frame){
        Bitmap lBitmap;
        ByteBuffer lByteBuffer;
        lBitmap = Bitmap.createBitmap(frame.as(VideoFrame.class).getWidth(),
                frame.as(VideoFrame.class).getHeight(), Bitmap.Config.RGB_565);
        lByteBuffer = ByteBuffer.allocateDirect(lBitmap.getByteCount());
        frame.getData(lByteBuffer.array());
        lByteBuffer.rewind();
        lBitmap.copyPixelsFromBuffer(lByteBuffer);
        return lBitmap;
    }

    public void saveDepthBitmap(final Context context, final String filename) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                File file = new File(context.getFilesDir().getAbsolutePath(), filename);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    mBitmapDepth.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
    }


    //    private native void nBackgroundRemover(long colorFrameHandle, long depthFrameHandle,
//                                                  float depthScale, float clipping_dist );
    private native void nSaveDepthMat(long handle, String filename);

}
