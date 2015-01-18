package com.elmz.drift.openbci;

/**
 * Created by Lucas on 1/17/15.
 */
public interface BrainStateCallback {
    public void blinkStart();
    public void blinkEnd(double blinkDuration);
    public void alpha(AlphaDetector.DetectionData_FreqDomain[] results);
}
