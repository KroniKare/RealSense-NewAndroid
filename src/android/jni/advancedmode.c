#include <jni.h>
#include "error.h"
#include "../../../include/librealsense2/rs.h"
#include "../../../include/librealsense2/h/rs_pipeline.h"
#include "../../../include/librealsense2/h/rs_device.h"
#include "../../../include/librealsense2/rs_advanced_mode.h"
#include "../android_uvc/android_debug.h"


JNIEXPORT void JNICALL
Java_com_intel_realsense_librealsense_AdvancedMode_nSetAdvancedModeOptions(JNIEnv *env, jclass type,
                                                                          jlong deviceHandle,
                                                                          jobject depthTableControl) {
    rs2_error *e = NULL;
    jclass clazz = (*env)->GetObjectClass(env, depthTableControl);

    jfieldID duField    = (*env)->GetFieldID(env, clazz, "depthUnits", "F");
    jfieldID dcminField = (*env)->GetFieldID(env, clazz, "depthClampMin", "I");
    jfieldID dcmaxField = (*env)->GetFieldID(env, clazz, "depthClampMax", "I");
    jfieldID dmField    = (*env)->GetFieldID(env, clazz, "disparityMode", "F");
    jfieldID dsField    = (*env)->GetFieldID(env, clazz, "disparityShift", "I");

     STDepthTableControl  group = {(uint32_t) (*env)->GetFloatField(env, depthTableControl, duField ),
            (*env)->GetIntField(env, depthTableControl,   dcminField ),
            (*env)->GetIntField(env, depthTableControl,   dcmaxField ),
            (uint32_t) (*env)->GetFloatField(env, depthTableControl, dmField ),
            (*env)->GetIntField(env, depthTableControl,   dsField )};
    rs2_set_depth_table((rs2_device*) deviceHandle, &group , &e);

    handle_error(env, e);



}

JNIEXPORT void JNICALL
Java_com_intel_realsense_librealsense_AdvancedMode_nGetAdvancedModeOptions(JNIEnv *env, jclass type,
                                                                          jlong deviceHandle,
                                                                          jobject depthTableControl) {

    jclass clazz2 = (*env)->GetObjectClass(env, depthTableControl);

    jfieldID duField    = (*env)->GetFieldID(env, clazz2, "depthUnits", "F");
    jfieldID dcminField = (*env)->GetFieldID(env, clazz2, "depthClampMin", "I");
    jfieldID dcmaxField = (*env)->GetFieldID(env, clazz2, "depthClampMax", "I");
    jfieldID dmField    = (*env)->GetFieldID(env, clazz2, "disparityMode", "F");
    jfieldID dsField    = (*env)->GetFieldID(env, clazz2, "disparityShift", "I");


    rs2_error *e = NULL;
    STDepthTableControl  group;
    rs2_get_depth_table((rs2_device*) deviceHandle, &group, 0, &e);
    handle_error(env, e);

    (*env)->SetFloatField(env, depthTableControl, duField    , group.depthUnits);
    (*env)->SetIntField(env, depthTableControl,   dcminField , group.depthClampMin);
    (*env)->SetIntField(env, depthTableControl,   dcmaxField , group.depthClampMax);
    (*env)->SetFloatField(env, depthTableControl, dmField    , group.disparityMode);
    (*env)->SetIntField(env, depthTableControl,   dsField    , group.disparityShift);
}


//JNIEXPORT void JNICALL
//Java_com_intel_realsense_librealsense_AdvancedMode_nGetAdvancedModeOptions(JNIEnv *env, jclass type,
//                                                    jlong deviceHandle,jint depthUnits depthTableControl) {
//    jclass clazz2 = (*env)->GetObjectClass(env, depthTableControl);
//    rs2_error *e = NULL;
//    STDepthTableControl * group =  NULL;
//    rs2_get_depth_table((rs2_device*) deviceHandle, group, 0, &e);
//    handle_error(env, e);
//
//
//
//    jfieldID duField    = (*env)->GetFieldID(env, clazz2, "depthUnits", "F");
//    jfieldID dcminField = (*env)->GetFieldID(env, clazz2, "depthClampMin", "I");
//    jfieldID dcmaxField = (*env)->GetFieldID(env, clazz2, "depthClampMax", "I");
//    jfieldID dmField    = (*env)->GetFieldID(env, clazz2, "disparityMode", "F");
//    jfieldID dsField    = (*env)->GetFieldID(env, clazz2, "disparityShift", "I");
//
//
//    (*env)->SetFloatField(env, depthTableControl, duField    , group->depthUnits);
//    (*env)->SetIntField(env, depthTableControl,   dcminField , group->depthClampMin);
//    (*env)->SetIntField(env, depthTableControl,   dcmaxField , group->depthClampMax);
//    (*env)->SetFloatField(env, depthTableControl, dmField    , group->disparityMode);
//    (*env)->SetIntField(env, depthTableControl,   dsField    , group->disparityShift);
//}
//

