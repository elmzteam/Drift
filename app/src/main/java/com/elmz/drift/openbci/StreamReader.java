package com.elmz.drift.openbci;

public class StreamReader {
	private final int WINDOW_SIZE = 100;
	private final int DIF_SIZE = 10;
	private final double THRESHOLD = 6.0;
	private final int DUPE_SIZE = 10;
	private final double SAMPLE_RATE = 250.0;

	private double[] windowD = new double[WINDOW_SIZE+DIF_SIZE]; 	//differential
	private double[] windowR = new double[WINDOW_SIZE+DIF_SIZE];	//real

	private double[] dupes   = new double[DUPE_SIZE];

	private int index = 0;
	private int indexTotal = 0;

	private int dupeIndex = 0;

	private int mode = 0;
	private int leadingEdge = 0;
	private int fallingEdge = 0;

	private int lastDown = -1;
	private int frameCount = 0;

    private boolean waitOne = true;

    private BrainStateCallback mCallback;

    public StreamReader(BrainStateCallback bsc) {
        mCallback = bsc;
    }

	public void addFrame(double frame) {
		windowR[index] = frame;
		if (indexTotal >= 10) {
			windowD[index] = (frame-windowR[mod(index-DIF_SIZE, WINDOW_SIZE+DIF_SIZE)])/DIF_SIZE; 
		} else {
			windowD[index] = 0;
		}
		index++;
		index = index%(WINDOW_SIZE+DIF_SIZE);
		indexTotal++;
		if (frameCount++ > 50 && !waitOne) {
			scan();
			frameCount = 0;
		}
        if (waitOne) {
            waitOne = false;
        }
	}

	public void scan() {
		for (int i = 0; i < WINDOW_SIZE-1; i++) {
			double actual = windowD[(index+DIF_SIZE+i)%(WINDOW_SIZE+DIF_SIZE)];
			double prevus = windowD[(index+DIF_SIZE+i-1)%(WINDOW_SIZE+DIF_SIZE)];
			int absolt = indexTotal-WINDOW_SIZE+i;
			if (mode == 0) {
				if (prevus < THRESHOLD && actual >= THRESHOLD) {
					mode = 1;
					leadingEdge = absolt;
				} else if (prevus > -THRESHOLD && actual <= -THRESHOLD) {
					mode = -1;
					fallingEdge = absolt;
				}
			} else if (mode == 1) {
				if (actual <= THRESHOLD) {
					double val = ((absolt-leadingEdge)/2.0+leadingEdge);
					if (!dupeContains(val)) {
						//System.out.println("BLINK ENDED AT FRAME: "+val+" WITH BOUNDRIES "+leadingEdge+" & "+absolt);
						dupes[dupeIndex%DUPE_SIZE] = val;
						dupeIndex++;
						if (lastDown > 0) {
							mCallback.blinkEnd(Math.abs((absolt-lastDown)/SAMPLE_RATE));
                            System.out.println("BLINK OCCURRED FOR "+(absolt-lastDown)/SAMPLE_RATE+" SECONDS");
							lastDown = -1;
						}
						mode = 0;
						leadingEdge = 0;
						fallingEdge = 0;
					}
				}
			} else if (mode == -1) {
				if (actual >= -THRESHOLD) {
					double val = ((absolt-fallingEdge)/2.0+fallingEdge);
					if (!dupeContains(val)) {
						//System.out.println("BLINK BEGAN AT FRAME: "+val+" WITH BOUNDRIES "+fallingEdge+" & "+absolt);
						dupes[dupeIndex%DUPE_SIZE] = val;
						dupeIndex++;
						if (lastDown < 0) {
							lastDown = absolt;
                            mCallback.blinkStart();
                        }
						mode = 0;
						leadingEdge = 0;
						fallingEdge = 0;
					}
				}
			}
		}
	}

	public double getFrame(int in) {
		if (in < 0 || in >= WINDOW_SIZE) {
			return -1.0;
		} else {
			return windowD[mod((index+in-1),(WINDOW_SIZE+DIF_SIZE))];
		}
	}

	private boolean dupeContains(double val) {
		for (int i = 0; i < DUPE_SIZE; i++) {
			if (((int)dupes[i]) == ((int)val)) {
				return true;
			}
		}
		return false;
	}

	private int mod(int a, int b) {
		if (a >= 0) {
			return a%b;
		} else {
			return a%b+b;
		}
	}
}