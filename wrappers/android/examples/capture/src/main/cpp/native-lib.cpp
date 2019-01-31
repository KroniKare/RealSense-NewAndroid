#include <jni.h>

#include <librealsense2/hpp/rs_frame.hpp>
#include "../../../../../../../src/android/android_uvc/android_debug.h"
#include "../../../../../../../src/android/jni/error.h"
#include "../../../../../../../examples/C/example.h"
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

//
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_intel_realsense_capture_BackgroundRemover_nBackgroundRemover(JNIEnv *env, jobject instance,
//                                                    jlong colorFrameHandle, jlong depthFrameHandle,
//                                                    jfloat depth_scale, jfloat clipping_dist) {
//
//    auto depth_frame = reinterpret_cast<rs2::frame*> (depthFrameHandle);
//    auto other_frame = reinterpret_cast<rs2::video_frame*>(reinterpret_cast<rs2::frame*> (colorFrameHandle));
//    LOGD("Field 0.5");
//    auto temp = depth_frame->get_data();
//
//    LOGD("Field 1");
//    auto p_depth_frame = reinterpret_cast<const uint16_t*>(const_cast<void*>(depth_frame->get_data()));
//    LOGD("Field 1.5");
//    auto p_other_frame = reinterpret_cast<uint8_t*>(const_cast<void*>(other_frame->get_data()));
//    LOGD("Field 2");
//
//    int shift = 0;
//    int width = other_frame->get_width();
//    int height = other_frame->get_height();
//    int other_bpp = other_frame->get_bytes_per_pixel();
//    LOGD("Field 3");
//    #pragma omp parallel for schedule(dynamic) //Using OpenMP to try to parallelise the loop
//    for (int y = 0; y < height; y++)
//    {
//        auto depth_pixel_index = y * width;
//        for (int x = 0; x < width; x++, ++depth_pixel_index)
//        {
//            // Get the depth value of the current pixel
//            auto pixels_distance = depth_scale * p_depth_frame[depth_pixel_index - shift];
//
//            // Check if the depth value is invalid (<=0) or greater than the threashold
//            if (pixels_distance <= 0.f || pixels_distance > clipping_dist)
//            {
//                // Calculate the offset in other frame's buffer to current pixel
//                auto offset = depth_pixel_index * other_bpp;
//
//                // Set pixel to "background" color (0x999999)
//                std::memset(&p_other_frame[offset], 0x99, static_cast<size_t>(other_bpp));
//            }
//        }
//    }
//    LOGD("Field 4");
//}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_intel_realsense_capture_MainActivity_nSaveDepthColor(JNIEnv *env,
                                                               jobject instance,
                                                               jlong depthHandle,
                                                               jlong colorHandle,
                                                               jstring fileName_) {

    const rs2_frame* colorFrame = (const rs2_frame*) colorHandle;
    const rs2_frame* depthFrame = (const rs2_frame*) depthHandle;
    LOGD("Field 1");
    rs2_error *e = NULL;
    const char *fileName = env->GetStringUTFChars(fileName_, 0);
    int streamWidth  = rs2_get_frame_width(depthFrame, &e);
    check_error(e);
    int streamHeight =  rs2_get_frame_height(depthFrame, &e);
    check_error(e);
    cv::Mat depthMat = cv::Mat::zeros(streamHeight, streamWidth, CV_32FC1);
    LOGD("Field 1.5: Stream height %d, Stream width %d",streamHeight,streamWidth);
    // Map the zValues to be clearly shown:
    for (int c = 0; c < streamWidth; c++)
    {
        for (int r = 0; r < streamHeight; r++)
        {
            //if (woundMask.at<uchar>(r, c) > 0)//Only save the ROI area depth information
            if (true) // Save all depth points
            {

                float zValue = rs2_depth_frame_get_distance(depthFrame,c, r, &e);
                check_error(e);
                LOGD("Field 1.8");
                LOGD("zvalue: %f",zValue);
                check_error(e);

                if (zValue > 0)
                {
                    //zValue = ((zValue - minDepth) / (maxDepth - minDepth)) * 100;
                    //fill the points and colors Mat to be saved in a PLY file:
                    depthMat.at<float>(r, c) = zValue;
                }
            }
        }
    }
    LOGD("Field 2");
//    cv::Mat processedFrameMat = cv::Mat(cv::Size(streamWidth, streamHeight), CV_8UC3, (void*)(rs2_get_frame_data(colorFrame, &e)));
    LOGD("Field 3");
    cv::FileStorage fs(fileName, cv::FileStorage::WRITE); // create FileStorage object
    fs << "depthMat" << depthMat;
//    fs << "colorMat" << processedFrameMat;
    fs.release(); // releasing the file.
    LOGD("Field 4");

    env->ReleaseStringUTFChars(fileName_, fileName);
    return static_cast<jboolean>(true);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_intel_realsense_capture_MainActivity_nSaveDepth(JNIEnv *env,
                                                              jobject instance,
                                                              jlong depthHandle,
                                                              jstring fileName_) {

    const rs2_frame* depthFrame = (const rs2_frame*) depthHandle;
    LOGD("Field 1");
    rs2_error *e = NULL;
    const char *fileName = env->GetStringUTFChars(fileName_, 0);
    int streamWidth  = rs2_get_frame_width(depthFrame, &e);
    check_error(e);
    int streamHeight =  rs2_get_frame_height(depthFrame, &e);
    check_error(e);
    cv::Mat depthMat = cv::Mat::zeros(streamHeight, streamWidth, CV_32FC1);
    LOGD("Field 1.5: Stream height %d, Stream width %d",streamHeight,streamWidth);
    // Map the zValues to be clearly shown:
    for (int c = 0; c < streamWidth; c++)
    {
        for (int r = 0; r < streamHeight; r++)
        {
            //if (woundMask.at<uchar>(r, c) > 0)//Only save the ROI area depth information
            if (true) // Save all depth points
            {

                float zValue = rs2_depth_frame_get_distance(depthFrame,c, r, &e);
                check_error(e);
                LOGD("Field 1.8");
                LOGD("zvalue: %f",zValue);
                check_error(e);

                if (zValue > 0)
                {
                    //zValue = ((zValue - minDepth) / (maxDepth - minDepth)) * 100;
                    //fill the points and colors Mat to be saved in a PLY file:
                    depthMat.at<float>(r, c) = zValue;
                }
            }
        }
    }
    LOGD("Field 2");
//    cv::Mat processedFrameMat = cv::Mat(cv::Size(streamWidth, streamHeight), CV_8UC3, (void*)(rs2_get_frame_data(colorFrame, &e)));
    LOGD("Field 3");
    cv::FileStorage fs(fileName, cv::FileStorage::WRITE); // create FileStorage object
    fs << "depthMat" << depthMat;
//    fs << "colorMat" << processedFrameMat;
    fs.release(); // releasing the file.
    LOGD("Field 4");

    env->ReleaseStringUTFChars(fileName_, fileName);
    return static_cast<jboolean>(true);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_intel_realsense_capture_MainActivity_nSaveDepthwithData(JNIEnv *env,
                                                         jobject instance,
                                                         jbyteArray depthData,
                                                         jint width, jint height,
                                                         jstring fileName_) {

    LOGD("Field 1");
    rs2_error *e = NULL;
    const char *fileName = env->GetStringUTFChars(fileName_, 0);
    int streamWidth  = width;
    check_error(e);
    int streamHeight = height;
    check_error(e);
    cv::Mat depthMat = cv::Mat::zeros(streamHeight, streamWidth, CV_32FC1);
    LOGD("Field 1.5: Stream height %d, Stream width %d",streamHeight,streamWidth);
    // Map the zValues to be clearly shown:
    for (int c = 0; c < streamWidth; c++)
    {
        for (int r = 0; r < streamHeight; r++)
        {
            //if (woundMask.at<uchar>(r, c) > 0)//Only save the ROI area depth information
            if (true) // Save all depth points
            {

//                float zValue = rs2_depth_frame_get_distance(depthFrame,c, r, &e);
                float zValue = (reinterpret_cast<const uint16_t*>(depthData)[r*streamWidth + c])/1000.f;
                check_error(e);
                LOGD("Field 1.8");
                LOGD("zvalue: %f",zValue);
                check_error(e);

                if (zValue > 0)
                {
                    //zValue = ((zValue - minDepth) / (maxDepth - minDepth)) * 100;
                    //fill the points and colors Mat to be saved in a PLY file:
                    depthMat.at<float>(r, c) = zValue;
                }
            }
        }
    }
    LOGD("Field 2");
//    cv::Mat processedFrameMat = cv::Mat(cv::Size(streamWidth, streamHeight), CV_8UC3, (void*)(rs2_get_frame_data(colorFrame, &e)));
    LOGD("Field 3");
    cv::FileStorage fs(fileName, cv::FileStorage::WRITE); // create FileStorage object
    fs << "depthMat" << depthMat;
//    fs << "colorMat" << processedFrameMat;
    fs.release(); // releasing the file.
    LOGD("Field 4");

    env->ReleaseStringUTFChars(fileName_, fileName);
    return static_cast<jboolean>(true);
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_intel_realsense_capture_BackgroundRemover_nSaveDepthMat(JNIEnv *env,
                                                                 jobject instance,
                                                                 jlong handle,jstring fileName_) {

    const char *fileName = env->GetStringUTFChars(fileName_, 0);

    cv::Mat* depthMat= reinterpret_cast<cv::Mat *> (handle);
    LOGD("Field 1");
    cv::Mat myMat(480, 640, CV_32FC1, depthMat);
    LOGD("Field 2");

    cv::FileStorage fs(fileName, cv::FileStorage::WRITE); // create FileStorage object
    fs << "depthMat" <<  myMat;
    LOGD("Field 3");

    fs.release(); // releasing the file.

    env->ReleaseStringUTFChars(fileName_, fileName);
    return static_cast<jboolean>(true);
}





