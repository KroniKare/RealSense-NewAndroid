package com.intel.realsense.librealsense;

public class AdvancedMode extends LrsClass {
    Device mDevice;
    public AdvancedMode(Device device){
        mDevice= device;
    }

    public static class DepthTableControl {
        public float  depthUnits; // the native C variable type is uint32_t
        public int  depthClampMin;
        public int  depthClampMax;
        public float  disparityMode; // the native C variable type is uint32_t
        public int  disparityShift;
    }

//    public void setDepthTableAdvancedMode(float dUnits,int dCMin, int dCMax,
//                                          float disMode, int disShift){
//        DepthTableControl depthTableControl = new DepthTableControl();
//        depthTableControl.depthUnits    = dUnits;
//        depthTableControl.depthClampMin = dCMin;
//        depthTableControl.depthClampMax = dCMax;
//        depthTableControl.disparityMode = disMode;
//        depthTableControl.disparityShift = disShift;
//        nSetAdvancedModeOptions(mDevice.getHandle(), depthTableControl);
//    }


//    public synchronized void setmDepthTableControl(DepthTableControl depthTableControl) {
//
//        nSetAdvancedModeOptions(mDevice.getHandle(), depthTableControl.depthUnits,
//                depthTableControl.depthClampMin,depthTableControl.depthClampMax,
//                depthTableControl.disparityMode,depthTableControl.disparityShift);
//    }
//
//    public synchronized DepthTableControl getmDepthTableControl() {
//        DepthTableControl mDepthTableControl=new DepthTableControl();
//        nGetAdvancedModeOptions(mDevice.getHandle(), mDepthTableControl.depthUnits,
//                mDepthTableControl.depthClampMin,mDepthTableControl.depthClampMax,
//                mDepthTableControl.disparityMode,mDepthTableControl.disparityShift);
//        return mDepthTableControl;
//    }
    public synchronized void setmDepthTableControl(DepthTableControl depthTableControl) {
        nSetAdvancedModeOptions(mDevice.getHandle(), depthTableControl);
    }

    public synchronized DepthTableControl getmDepthTableControl() {
        DepthTableControl mDepthTableControl = new DepthTableControl();
        nGetAdvancedModeOptions(mDevice.getHandle(), mDepthTableControl);
        return mDepthTableControl;
    }

    @Override
    public void close() throws Exception {

    }

    private static native void nGetAdvancedModeOptions(long deviceHandle, DepthTableControl depthTableControl);
    private static native void nSetAdvancedModeOptions(long deviceHandle, DepthTableControl depthTableControl);


}
