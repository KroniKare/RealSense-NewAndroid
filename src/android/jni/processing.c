#include <stdio.h>

#include <jni.h>
#include "error.h"

#include "../../../include/librealsense2/rs.h"
#include "../usb_host/android_debug.h"
#include "../../../../../Android/Sdk/ndk-bundle/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdio.h"


void savingDataFrame(rs2_frame *frame , rs2_source *src,void * a);
void removeBackgroundFrame(rs2_frame *frame , rs2_source *src,void * a);
void (*onFrameCallbackSave)(rs2_frame *p, rs2_source *q,void * a);
void (*onFrameCallbackRemove)(rs2_frame *p, rs2_source *q,void * a);

const char * fileName;
const uint16_t* depth_frame_data;
const uint32_t* video_frame_data;
int depthThreshold;
float depthUnit;
const int bytesPerPixel = 4; /// red, green, blue , alpha
const int fileHeaderSize = 14;
const int infoHeaderSize = 40;



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


//JNIEXPORT jlong JNICALL
//Java_com_intel_realsense_librealsense_SavingData_nCreate(JNIEnv *env, jclass type,
//                                                        jlong queueHandle,jstring filename_) {
//    //TODO: Cast alignTo input to rs2_stream
//    fileName = (*env)->GetStringUTFChars(env,filename_, 0);
//
//    LOGD("Creating Saving Data Processing Block");
//    rs2_error *e;
//    e = NULL;
//    onFrameCallbackSave = &savingDataFrame;
//    LOGD("SD:: Assigned Block to function");
//    rs2_frame_processor_callback_ptr  proc= *onFrameCallbackSave;
//    LOGD("SD:: Created Callback");
//    rs2_processing_block *rv = rs2_create_processing_block_fptr(proc,NULL,&e);
//    LOGD("SD:: Created Processing Block");
//    handle_error(env, e);
//    rs2_start_processing_queue(rv, queueHandle, &e);
//    LOGD("SD:: Started Processing Queue");
//    handle_error(env, e);
//    return (jlong) rv;
//}



JNIEXPORT void JNICALL
Java_com_intel_realsense_librealsense_ProcessingBlock_nInvoke(JNIEnv *env, jclass type,
                                                              jlong handle, jlong frameHandle) {
    rs2_error *e = NULL;
    rs2_frame_add_ref((rs2_frame *) frameHandle, &e);
    handle_error(env, e);
    rs2_process_frame((rs2_processing_block *) handle, (rs2_frame *) frameHandle, &e);
    handle_error(env, e);
}


//void savingDataFrame(rs2_frame *frames , rs2_source *src, void * a){
//    LOGD("Entered SavingData Frame");
////    cvOpenFileStorage();
//    rs2_error *e = NULL;
//
//    // Returns the number of frames embedded within the composite frame
//    int num_of_frames = rs2_embedded_frames_count(frames, &e);
//    LOGD("SD:: Number of Frames identified %d", num_of_frames);
//
//    int i;
//    for (i = 0; i < num_of_frames; ++i)
//    {
//        // The returned object should be released with rs2_release_frame(...)
//        rs2_frame* frame = rs2_extract_frame(frames, i, &e);
//
//        if (1 == rs2_is_frame_extendable_to(frame, RS2_EXTENSION_DEPTH_FRAME, &e))
//        {
//            /* Retrieve depth data, configured as 16-bit depth values */
//            depth_frame_data = (const uint16_t*)(rs2_get_frame_data(frame, &e));
//            int height = rs2_get_frame_height(frame,&e);
//            int width = rs2_get_frame_width(frame,&e);
//            const void* data = rs2_get_frame_data(frame,&e);
//
//            FILE *xml_file;
//            xml_file = fopen(fileName, "w");
//            fclose(xml_file);
//
//            LOGD("RB:: Read Depth Frame Data");
//        }
//
//        if (1 == rs2_is_frame_extendable_to(frame, RS2_EXTENSION_VIDEO_FRAME, &e))
//        {
//            /* Retrieve RGB data, configured as 32-bit RGBA values */
//            /* Retrieve depth data, configured as 16-bit depth values */
//            video_frame_data = (const uint32_t*)(rs2_get_frame_data(frame, &e));
//            height = rs2_get_frame_height(frame,&e);
//            width = rs2_get_frame_width(frame,&e);
//            bpp = rs2_get_frame_bits_per_pixel(frame,&e)/8;
//            LOGD("RB:: Read Video Frame Data  ( %d x %d )",height,width);
//
//            generateBitmapImage((unsigned char *) video_frame_data,height, width,fileName);
//        }
//        rs2_release_frame(frame);
//    }
//}


JNIEXPORT jlong JNICALL
Java_com_intel_realsense_librealsense_RemoveBackground_nCreate(JNIEnv *env, jclass type,
                                                        jlong queueHandle,jint _threshold, jfloat _depthUnit) {

    LOGD("Creating Remove Background Processing Block");
    depthThreshold  = (int) _threshold;
    depthUnit = (int) _depthUnit;
    rs2_error *e =NULL;
    onFrameCallbackRemove = &removeBackgroundFrame;
    LOGD("RB:: Assigned Block to function");
    rs2_frame_processor_callback_ptr  proc= *onFrameCallbackRemove;
    LOGD("RB:: Created Callback");
    rs2_processing_block *rv = rs2_create_processing_block_fptr(proc,NULL,&e);
    LOGD("RB:: Created Processing Block");
    handle_error(env, e);
    rs2_start_processing_queue(rv, queueHandle, &e);
    LOGD("RB:: Started Processing Queue");
    handle_error(env, e);
    return (jlong) rv;
}


void removeBackgroundFrame(rs2_frame *frames , rs2_source *src, void * a){
    LOGD("RB:: Entered RemovedBackground");
    rs2_error *e = NULL;
    int height,width,bpp;


    // 1:: Seperate the Depth and Video frames

    // Returns the number of frames embedded within the composite frame
    int num_of_frames = rs2_embedded_frames_count(frames, &e);
    LOGD("RB:: Number of Frames identified %d", num_of_frames);

    int i;
    for (i = 0; i < num_of_frames; ++i)
    {
        // The returned object should be released with rs2_release_frame(...)
        rs2_frame* frame = rs2_extract_frame(frames, i, &e);

        if (1 == rs2_is_frame_extendable_to(frame, RS2_EXTENSION_DEPTH_FRAME, &e))
        {
            /* Retrieve depth data, configured as 16-bit depth values */
            depth_frame_data = (const uint16_t*)(rs2_get_frame_data(frame, &e));
            LOGD("RB:: Read Depth Frame Data");
        }

        if (1 == rs2_is_frame_extendable_to(frame, RS2_EXTENSION_VIDEO_FRAME, &e))
        {
            /* Retrieve RGB data, configured as 32-bit RGBA values */
            /* Retrieve depth data, configured as 16-bit depth values */
            video_frame_data = (const uint32_t*)(rs2_get_frame_data(frame, &e));
            height = rs2_get_frame_height(frame,&e);
            width = rs2_get_frame_width(frame,&e);
            bpp = rs2_get_frame_bits_per_pixel(frame,&e)/8;
            LOGD("RB:: Read Video Frame Data  ( %d x %d )",height,width);
        }
        rs2_release_frame(frame);
    }

    // 2:: Create Loop on values from the Depth Frame and Gray Background

    uint32_t one_centimeter = (uint32_t) (1.0f * depthUnit /100.0f);
    LOGD("RB:: One Cenitmeter is %d with depth unit of %f",one_centimeter,depthUnit);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            int depth = *depth_frame_data++;
            video_frame_data++;
            if (depth <= 0 || depth > one_centimeter * depthThreshold) {
                memset(video_frame_data, 0x99, bpp);
            }

        }

    }

    LOGD("RB:: Done");
}

void generateBitmapImage(unsigned char *image, int height, int width, char* imageFileName);
unsigned char* createBitmapFileHeader(int height, int width, int paddingSize);
unsigned char* createBitmapInfoHeader(int height, int width);


//int main(){
//    int height = 341;
//    int width = 753;
//    unsigned char image[height][width][bytesPerPixel];
//    char* imageFileName = "bitmapImage.bmp";
//
//    int i, j;
//    for(i=0; i<height; i++){
//        for(j=0; j<width; j++){
//            image[i][j][2] = (unsigned char)((double)i/height*255); ///red
//            image[i][j][1] = (unsigned char)((double)j/width*255); ///green
//            image[i][j][0] = (unsigned char)(((double)i+j)/(height+width)*255); ///blue
//        }
//    }
//
//    generateBitmapImage((unsigned char *)image, height, width, imageFileName);
//    printf("Image generated!!");
//}


void generateBitmapImage(unsigned char *image, int height, int width, char* imageFileName){

    unsigned char padding[3] = {0, 0, 0};
    int paddingSize = (4 - (width*bytesPerPixel) % 4) % 4;

    unsigned char* fileHeader = createBitmapFileHeader(height, width, paddingSize);
    unsigned char* infoHeader = createBitmapInfoHeader(height, width);

    FILE* imageFile = fopen(imageFileName, "wb");

    fwrite(fileHeader, 1, fileHeaderSize, imageFile);
    fwrite(infoHeader, 1, infoHeaderSize, imageFile);

    int i;
    for(i=0; i<height; i++){
        fwrite(image+(i*width*bytesPerPixel), bytesPerPixel, width, imageFile);
        fwrite(padding, 1, paddingSize, imageFile);
    }

    fclose(imageFile);
    free(fileHeader);
    free(infoHeader);
}

unsigned char* createBitmapFileHeader(int height, int width, int paddingSize){
    int fileSize = fileHeaderSize + infoHeaderSize + (bytesPerPixel*width+paddingSize) * height;

    static unsigned char fileHeader[] = {
            0,0, /// signature
            0,0,0,0, /// image file size in bytes
            0,0,0,0, /// reserved
            0,0,0,0, /// start of pixel array
    };

    fileHeader[ 0] = (unsigned char)('B');
    fileHeader[ 1] = (unsigned char)('M');
    fileHeader[ 2] = (unsigned char)(fileSize    );
    fileHeader[ 3] = (unsigned char)(fileSize>> 8);
    fileHeader[ 4] = (unsigned char)(fileSize>>16);
    fileHeader[ 5] = (unsigned char)(fileSize>>24);
    fileHeader[10] = (unsigned char)(fileHeaderSize + infoHeaderSize);

    return fileHeader;
}

unsigned char* createBitmapInfoHeader(int height, int width){
    static unsigned char infoHeader[] = {
            0,0,0,0, /// header size
            0,0,0,0, /// image width
            0,0,0,0, /// image height
            0,0, /// number of color planes
            0,0, /// bits per pixel
            0,0,0,0, /// compression
            0,0,0,0, /// image size
            0,0,0,0, /// horizontal resolution
            0,0,0,0, /// vertical resolution
            0,0,0,0, /// colors in color table
            0,0,0,0, /// important color count
    };

    infoHeader[ 0] = (unsigned char)(infoHeaderSize);
    infoHeader[ 4] = (unsigned char)(width    );
    infoHeader[ 5] = (unsigned char)(width>> 8);
    infoHeader[ 6] = (unsigned char)(width>>16);
    infoHeader[ 7] = (unsigned char)(width>>24);
    infoHeader[ 8] = (unsigned char)(height    );
    infoHeader[ 9] = (unsigned char)(height>> 8);
    infoHeader[10] = (unsigned char)(height>>16);
    infoHeader[11] = (unsigned char)(height>>24);
    infoHeader[12] = (unsigned char)(1);
    infoHeader[14] = (unsigned char)(bytesPerPixel*8);

    return infoHeader;
}

