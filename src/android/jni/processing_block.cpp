//
// Created by kronikare on 1/2/19.


#include <jni.h>
#include "../../../include/librealsense2/rs.hpp"
#include "../usb_host/android_debug.h"

//extern "C"
//JNIEXPORT jlong JNICALL
//Java_com_intel_realsense_librealsense_SavingData_nCreate(JNIEnv *env, jclass type,
//                                                         jlong queueHandle,jstring fileName_) {
//
//    rs2_error *e = NULL;
//    const char *fileName = env->GetStringUTFChars(fileName_, 0);
//    rs2::processing_block pb([&](rs2::frame f, rs2::frame_source& src)
//    {   LOGD("Entered SavingData Processing Block");
//        const int w = f.as<rs2::depth_frame>().get_width();
//        const int h = f.as<rs2::depth_frame>().get_height();
//        LOGD("Assigned width an height");
//
////        // frame --> cv
////        cv::Mat depthMat(cv::Size(w, h), cv::CV_32FC1, (void*)f.get_data(), cv::Mat::AUTO_STEP);
////
////        // Do some processing
////
////        cv::FileStorage fs(fileName, cv::FileStorage::WRITE); // create FileStorage object
////        fs << "depthMat" <<  depthMat;
//
////        fs.release(); // releasing the file.
//
//        env->ReleaseStringUTFChars(fileName_, fileName);
//
//        ((rs2::frame_queue*) queueHandle)->enqueue(f);
//         LOGD("Frame has been sent to queue");
//
//        // Send the resulting frame to the output queue
//        src.frame_ready(f);
//        LOGD("Frame Ready");
//
//    });
//         LOGD("Created Processing Block");
//
//         rs2_processing_block *rv = (rs2_processing_block* ) pb.get();
////        rs2_start_processing_queue(& pb, queueHandle, &e);
//        return (jlong) rv;
//}
