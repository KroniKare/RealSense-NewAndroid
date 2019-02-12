package com.intel.realsense.librealsense;

public abstract class Filter extends ProcessingBlock implements FilterInterface{
    protected FrameQueue mQueue = new FrameQueue(1);

    @Override
    public Frame process(Frame original) {
        nInvoke(mHandle, original.getHandle());
//        Frame f = mQueue.waitForFrame();
        Frame f = mQueue.pollForFrame();
        return f != null ? f : original;
    }

    @Override
    public FrameSet process(FrameSet original) {
        nInvoke(mHandle, original.getHandle());
//        FrameSet f  = mQueue.waitForFrames();
        FrameSet f = mQueue.pollForFrames();
        return f != null ? f : original;
    }
}
