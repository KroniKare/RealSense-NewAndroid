package com.intel.realsense.librealsense;

public class Alignment extends Filter {

    public Alignment(){
        mHandle = nCreate(mQueue.getHandle(),1);
    }

    private static native long nCreate(long queueHandle,long alignTo);
}
