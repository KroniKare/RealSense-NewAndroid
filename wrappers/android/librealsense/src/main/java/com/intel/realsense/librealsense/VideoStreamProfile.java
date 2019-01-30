package com.intel.realsense.librealsense;

import android.util.Log;

public class VideoStreamProfile extends StreamProfile {
    ResolutionParams mResolutionParams;
    IntrinsicParameters mIntrinsicParameters;

    private class ResolutionParams {
        public int width;
        public int height;
    }



    VideoStreamProfile(long handle) {
        super(handle);
        mResolutionParams = new ResolutionParams();
        mIntrinsicParameters = new IntrinsicParameters();
        nGetResolution(mHandle, mResolutionParams);
        nGetIntrinsicParams(mHandle, mIntrinsicParameters);
        Log.d("VideoStreamProfile", "VideoStreamProfile:  width: "+mIntrinsicParameters.width+" ppx: "+mIntrinsicParameters.ppx+ " fx: "+mIntrinsicParameters.fx);
    }

    public int getWidth() {
        return mResolutionParams.width;
    }

    public int getHeight() {
        return mResolutionParams.height;
    }
    public IntrinsicParameters getmIntrinsicParameters(){
        return mIntrinsicParameters;
    }

    private static native void nGetResolution(long handle, ResolutionParams params);
    private static native void nGetIntrinsicParams(long handle, IntrinsicParameters params);

}
