#include <jni.h>
#include "error.h"

#include "../../../include/librealsense2/rs.h"
#include "../usb_host/android_debug.h"
#include "../../../../../Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdio.h"


void savingDataFrame(rs2_frame *frame , rs2_source *src,void * a);
void (*onFrameCallback)(rs2_frame *p, rs2_source *q,void * a);
const char * fileName;


JNIEXPORT void JNICALL
Java_com_intel_realsense_librealsense_ProcessingBlock_nDelete(JNIEnv *env, jclass type,
                                                              jlong handle) {
    rs2_delete_processing_block(handle);
}

JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_Colorizer_nCreate(JNIEnv *env, jclass type, jlong queueHandle) {
    rs2_error *e = NULL;
    rs2_processing_block *rv = rs2_create_colorizer(&e);
    handle_error(env, e);
    rs2_start_processing_queue(rv, queueHandle, &e);
    handle_error(env, e);
    return (jlong) rv;
}

JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_Decimation_nCreate(JNIEnv *env, jclass type,
                                                         jlong queueHandle) {
    rs2_error *e = NULL;
    rs2_processing_block *rv = rs2_create_decimation_filter_block(&e);
    handle_error(env, e);
    rs2_start_processing_queue(rv, queueHandle, &e);
    handle_error(env, e);
    return (jlong) rv;
}

JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_Alignment_nCreate(JNIEnv *env, jclass type,
                                                        jlong queueHandle,jlong alignTo) {
    //TODO: Cast alignTo input to rs2_stream
    rs2_error *e = NULL;
    rs2_processing_block *rv = rs2_create_align(RS2_STREAM_COLOR,&e);
    handle_error(env, e);
    rs2_start_processing_queue(rv, queueHandle, &e);
    handle_error(env, e);
    return (jlong) rv;
}


JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_SavingData_nCreate(JNIEnv *env, jclass type,
                                                        jlong queueHandle,jstring filename_) {
    //TODO: Cast alignTo input to rs2_stream
    fileName = (*env)->GetStringUTFChars(env,filename_, 0);

    LOGD("Creating Saving Data Processing Block");
    rs2_error *e;
    e = NULL;
    onFrameCallback = &savingDataFrame;
    LOGD("Assigned Block to function");
    rs2_frame_processor_callback_ptr  proc= *onFrameCallback;
    LOGD("Created Callback");
    rs2_processing_block *rv = rs2_create_processing_block_fptr(proc,NULL,&e);
    LOGD("Created Processing Block");
    handle_error(env, e);
    rs2_start_processing_queue(rv, queueHandle, &e);
    LOGD("Started Processing Queue");
    handle_error(env, e);
    return (jlong) rv;
}



JNIEXPORT void JNICALL
Java_com_intel_realsense_librealsense_ProcessingBlock_nInvoke(JNIEnv *env, jclass type,
                                                              jlong handle, jlong frameHandle) {
    rs2_error *e = NULL;
    rs2_frame_add_ref((rs2_frame *) frameHandle, &e);
    handle_error(env, e);
    rs2_process_frame((rs2_processing_block *) handle, (rs2_frame *) frameHandle, &e);

    handle_error(env, e);
}


void savingDataFrame(rs2_frame *frame , rs2_source *src, void * a){
    LOGD("Entered SavingData Frame");
//    cvOpenFileStorage();
    rs2_error *e = NULL;

    int height = rs2_get_frame_height(frame,&e);
    int width = rs2_get_frame_width(frame,&e);
    const void* data = rs2_get_frame_data(frame,&e);

    FILE *xml_file;
    xml_file = fopen(fileName, "w");
    fclose(xml_file);

//    CvMat depthMat;// = cvCreateMat(height, width, CV_32FC1);
//    cvInitMatHeader(&depthMat, height, width, CV_32FC1, data);

//    CvMat depthMat = cvCreateMat(height, width, CV_MAT);
//    depthMat.data = data;
//    LOGD("Field 1");
//    cv::Mat myMat(480, 640, CV_32FC1, depthMat);
//    LOGD("Field 2");
//
//    cv::FileStorage fs(fileName, cv::FileStorage::WRITE); // create FileStorage object
//    fs << "depthMat" <<  myMat;
//    LOGD("Field 3");
//
//    fs.release(); // releasing the file.

}


