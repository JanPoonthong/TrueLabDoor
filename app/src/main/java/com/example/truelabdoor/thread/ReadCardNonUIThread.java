package com.example.truelabdoor.thread;

/**
 * 读卡线程
 * <p>
 * Created by zhoushenghua on 18-6-23.
 */

public class ReadCardNonUIThread extends NotDoHandlerMessageNonUIThread {

    private final Runnable mTarget;

    public ReadCardNonUIThread(Runnable target) {
        this.mTarget = target;
    }

    @Override
    public void doRun() {
        this.mTarget.run();
    }

}
