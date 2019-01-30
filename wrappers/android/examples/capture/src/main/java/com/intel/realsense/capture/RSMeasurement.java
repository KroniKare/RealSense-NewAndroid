package com.intel.realsense.capture;

import com.intel.realsense.librealsense.IntrinsicParameters;

public class RSMeasurement {

    IntrinsicParameters colorIntrinsicParameters = null;
    IntrinsicParameters depthIntrinsicParameters = null;
    boolean colorIntrinsicRead = false;
    boolean depthIntrinsicRead = false;


    public void setColorIntrinsicParameters(IntrinsicParameters colorIntrinsicParameters) {
        this.colorIntrinsicParameters = colorIntrinsicParameters;
        setColorIntrinsicRead(true);
    }

    public void setDepthIntrinsicParameters(IntrinsicParameters depthIntrinsicParameters) {
        this.depthIntrinsicParameters = depthIntrinsicParameters;
        setDepthIntrinsicRead(true);
    }

    public void setColorIntrinsicRead(boolean colorIntrinsicRead) {
        this.colorIntrinsicRead = colorIntrinsicRead;
    }

    public void setDepthIntrinsicRead(boolean depthIntrinsicRead) {
        this.depthIntrinsicRead = depthIntrinsicRead;
    }
}
