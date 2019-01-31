#include <jni.h>
#include "error.h"
#include "../../../include/librealsense2/rs.h"
#include "../../../include/librealsense2/h/rs_pipeline.h"
#include "../../../include/librealsense2/h/rs_device.h"


JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_Device_nCreate(JNIEnv *env, jclass type,
                                                       jlong pipelineHandle) {
    rs2_error* e = NULL;
    rs2_device* rppd = rs2_pipeline_profile_get_device((rs2_pipeline_profile* ) pipelineHandle, &e);
    handle_error(env, e);
    return rppd;
}


JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_Device_nCreateWithContext(JNIEnv *env, jclass type,
                                                       jlong contextHandle) {
    rs2_error* e = NULL;
    rs2_device_list* deviceList = rs2_query_devices((rs2_context*) contextHandle, &e);
    rs2_device* rppd = rs2_create_device(deviceList, 0, &e);
    handle_error(env, e);
    return rppd;
}

// JNIEXPORT jlong JNICALL
// Java_com_intel_realsense_librealsense_Device_nGetDepthScale(JNIEnv *env, jclass type,
//                                                        jlong deviceHandle) {
//     rs2_error* e = NULL;
//     float  = rs2_get_depth_scale((rs2_context*) contextHandle, &e);
//     handle_error(env, e);
//     return rppd;
// }
