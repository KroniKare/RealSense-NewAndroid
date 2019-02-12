package com.intel.realsense.librealsense;

public class IntrinsicParameters {
    public int     width;     /**< Width of the image in pixels */
    public int     height;    /**< Height of the image in pixels */
    public float   ppx;       /**< Horizontal coordinate of the principal point of the image, as a pixel offset from the left edge */
    public float   ppy;       /**< Vertical coordinate of the principal point of the image, as a pixel offset from the top edge */
    public float   fx;        /**< Focal length of the image plane, as a multiple of pixel width */
    public float   fy;        /**< Focal length of the image plane, as a multiple of pixel height */

    //TODO: Add the following parameters
//        rs2_distortion model;    /**< Distortion model of the image */
//        float         coeffs[5]; /**< Distortion coefficients, order: k1, k2, p1, p2, k3 */


    @Override
    public String toString() {
        return '\n' + "intrinsics" + '\n' + width + "," + height + "," + ppx + "," + ppy + "," + fx + "," + fy;

    }
}
