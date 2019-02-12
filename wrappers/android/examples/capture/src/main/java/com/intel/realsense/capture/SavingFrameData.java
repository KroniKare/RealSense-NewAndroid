package com.intel.realsense.capture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.IntrinsicParameters;
import com.intel.realsense.librealsense.VideoFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class SavingFrameData {
    private static final String TAG = "SavingFrameData";


    /*
    Methods for saving Video Frame into jpeg Image
     */
    private static Bitmap createBitmapFromFrame(Frame frame){
        Bitmap lBitmap;
        ByteBuffer lByteBuffer;
        lBitmap = Bitmap.createBitmap(frame.as(VideoFrame.class).getWidth(),
                frame.as(VideoFrame.class).getHeight(), Bitmap.Config.ARGB_8888);
        lByteBuffer = ByteBuffer.allocateDirect(lBitmap.getByteCount());
        frame.getData(lByteBuffer.array());
        lByteBuffer.rewind();
        lBitmap.copyPixelsFromBuffer(lByteBuffer);
        return lBitmap;
    }


    private static void saveVideoBitmap(final Context context, final String filename, final Bitmap bitmap) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                File file = new File(context.getFilesDir().getAbsolutePath(), filename);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
    }

    public static void saveVideoFrame(final Context context, Frame frame, final String filename){
        saveVideoBitmap(context,filename,createBitmapFromFrame(frame));
    }

    //--------------------------------------------------------------------------------------------//

    /*
    Methods for saving depth frame data into txt file
     */

    private static void writeToFile(byte[] data, String fileName) throws IOException{
        FileOutputStream out = new FileOutputStream(fileName,true);
        out.write(data);
        out.close();
    }

    public static void saveDepthBytes(final Context context, Frame frame, final  String filename){
        File file = new File(context.getFilesDir().getAbsolutePath(), filename);
        try {
            Log.i(TAG, "removeBackground: Buffer Size: "+createDepthByteArray(frame).length);
            writeToFile(createDepthByteArray(frame),file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte [] createDepthByteArray(Frame depthFrame){
        ByteBuffer mBufferDepth = ByteBuffer.allocateDirect(depthFrame.as(VideoFrame.class).getStride()*depthFrame.as(VideoFrame.class).getHeight());
        Log.i(TAG, "removeBackground: Buffer Size Before: "+mBufferDepth.array().length);
        depthFrame.getData(mBufferDepth.array());
        mBufferDepth.rewind();
        return mBufferDepth.array();
    }

    public static void saveIntrinsicParameters(final Context context,
                                               IntrinsicParameters intrinsicParameters,
                                               final  String filename){
        File file = new File(context.getFilesDir().getAbsolutePath(), filename);
        try {
            writeToFile(intrinsicParameters.toString().getBytes(),file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
