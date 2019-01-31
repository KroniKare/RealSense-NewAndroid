package com.intel.realsense.librealsense;


import android.util.Log;

public class Pipeline extends LrsClass{
    long mPipelineProfileHandle;
    public Pipeline(){
        RsContext ctx = new RsContext();
        mHandle = nCreate(ctx.getHandle());
    }
    public Pipeline(RsContext ctx){
        mHandle = nCreate(ctx.getHandle());
    }

    public void start(){
        nStart(mHandle);
    }

    public void start(Config config){
        mPipelineProfileHandle=nStartWithConfig(mHandle, config.getHandle());
    }
    public void stop(){
        nStop(mHandle);
    }

    public FrameSet waitForFrames() {
        return waitForFrames(5000);
    }

    public FrameSet waitForFrames(int timeoutMilliseconds) {
        long frameHandle = nWaitForFrames(mHandle, timeoutMilliseconds);
        return new FrameSet(frameHandle);
    }

    @Override
    public void close() throws Exception {
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native long nCreate(long context);
    private static native void nDelete(long handle);
    private static native void nStart(long handle);
    private static native long nStartWithConfig(long handle, long configHandle);
    private static native void nStop(long handle);
    private static native long nWaitForFrames(long handle, int timeout);

    public long getmPipelineProfileHandle() {
        return mPipelineProfileHandle;
    }
}
