package com.intel.realsense.librealsense;

public class Frame extends LrsClass {

    protected Frame(long handle){
        mHandle = handle;
    }

    public static Frame create(long handle){
        if (nIsFrameExtendableTo(handle, Extension.VIDEO_FRAME.value()))
            return new VideoFrame(handle);
        return null;
    }

    public StreamProfile getProfile() {
        return new StreamProfile(nGetStreamProfile(mHandle));
    }

    public void getData(byte[] data) {
        nGetData(mHandle, data);
    }
    public void getDataInt(int[] data) {
        nGetDataInt(mHandle, data);
    }

    public <T extends Frame> T as(Class<T> type) {
        return (T) this;
    }

    public int getNumber(){
        return nGetNumber(mHandle);
    }

    public Frame applyFilter(FilterInterface filter) {
        return filter.process(this);
    }

    @Override
    public void close() throws Exception {
        nRelease(mHandle);
    }
    public void keep(){
        nKeep(mHandle);
    }

    private static native boolean nIsFrameExtendableTo(long handle, int extension);
    private static native void nAddRef(long handle);
    private static native void nRelease(long handle);
    private static native void nKeep(long handle);
    protected static native long nGetStreamProfile(long handle);
    private static native void nGetData(long handle, byte[] data);
    private static native void nGetDataInt(long handle, int[] data);
    private static native int nGetNumber(long handle);
}
