package com.intel.realsense.librealsense;

public class RemoveBackground extends Filter {

    public RemoveBackground(int threshold,float depthUnits){
        mHandle = nCreate(mQueue.getHandle(),threshold,depthUnits);
    }
    private static native long nCreate(long queueHandle,int threshold,float depthUnits);
}
