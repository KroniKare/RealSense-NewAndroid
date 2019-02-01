package com.intel.realsense.librealsense;

public class SavingData extends Filter {

    public SavingData(String filepath){
        mHandle = nCreate(mQueue.getHandle(),filepath);
    }

    private static native long nCreate(long queueHandle,String filepath);
}
