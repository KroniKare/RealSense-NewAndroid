package com.intel.realsense.librealsense;

public class Device extends LrsClass {


    public Device(Pipeline pipeline){
        mHandle = nCreate(pipeline.getmPipelineProfile().getHandle());
    }

    public Device(RsContext rsContext){
        mHandle = nCreateWithContext(rsContext.getHandle());
    }


    @Override
    public void close() throws Exception {

    }


    private static native long nCreate(long profileHandle);
    private static native long nCreateWithContext(long contextHandle);
//    public static native float nGetDepthScale(long handle);


}
