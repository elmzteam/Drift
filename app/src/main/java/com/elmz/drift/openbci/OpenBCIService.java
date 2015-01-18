package com.elmz.drift.openbci;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by El1t on 1/17/15.
 */
public class OpenBCIService extends Service implements BrainStateCallback
{
	public static final String TAG = "OpenBCI Service";
	private static final int MAX_READ_LENGTH = 512;
	private static final int PACKET_LENGTH = 33;
	private final float mVref = 4.5f;
	private final double mAccelScale = 0.002d / Math.pow(2,4);
	private double mGain = 24d;
	private double mVoltScale = mVref / (Math.pow(2,23)-1) / mGain * 1000000;
	private static D2xxManager sManager = null;
	private ReadThread mReadThread;
	private FT_Device mDevice = null;
	private boolean streaming;
	private byte[] overflowBuffer = new byte[MAX_READ_LENGTH*2];
	private int overflowLength = 0;
    int[] alphaCheckChannels = {7-1,8-1};

    private Messenger mMessenger;

    StreamReader mStreamReader = new StreamReader(this);
    AlphaDetector mAlphaDetector = new AlphaDetector(this);

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public OpenBCIService getService() {
            return OpenBCIService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static final int MSG_TEST = 1;
    private final Messenger mIncomingMessenger = new Messenger(new IncomingHandler());

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 1:
                    Log.d(TAG, "Stream Start");
                    startDataStream();
                    break;
                case 2:
                    Log.d(TAG, "Stream Stop");
                    stopDataStream();
                    break;
            }
        }
    }

    public Messenger getIncomingMessenger() {
        return mIncomingMessenger;
    }

    final Handler INIT_HANDLER = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final byte[] input = (byte[]) msg.obj;
			final char[] decoded = Arrays.copyOf(Charset.forName("US-ASCII").decode(ByteBuffer.wrap(input)).array(), msg.arg1);
			if (decoded.length > 4 && decoded[decoded.length - 3] == '$' && decoded[decoded.length - 2] == '$' && decoded[decoded.length-1] == '$') {
				Log.d(TAG, "EOT received");
				// Notify the LoginActivity that OpenBCI is initialized
				Message notif = Message.obtain();
				notif.arg1=1;
				try {
					mMessenger.send(notif);
				} catch (Exception e1) {
					Log.w(getClass().getName(), "Exception sending message", e1);
				}

//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						writeToDevice(OpenBCICommands.START_STREAM);
//						streaming = true;
//					}
//				}, 70);
//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						writeToDevice(OpenBCICommands.STOP_STREAM);
//						streaming = false;
//					}
//				}, 20000);

			} else {
				Log.d(TAG, new String(decoded));
			}
		}
	};

	final Handler PACKET_HANDLER = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final byte[] input = (byte[]) msg.obj;
			System.arraycopy(input, 0, overflowBuffer, overflowLength, input.length);
			if ((overflowLength + input.length)/PACKET_LENGTH > 0) {
				DataPacket temp;
				int index;
				for (index = 0; index + PACKET_LENGTH < overflowLength + input.length; index += PACKET_LENGTH) {
					// Verify integrity
					if (overflowBuffer[index] != (byte) 0xA0 || overflowBuffer[index + PACKET_LENGTH - 1] != (byte) 0xC0) {
						Log.d(TAG, "Invalid header/footer in packet");
						continue;
					}
					temp = new DataPacket(8, 3);
					temp.sampleIndex = (int) overflowBuffer[index + 1];
					for (int j = index + 2; j < index + 26; j += 3) {
						temp.values[(j - index - 2) / 3] = interpret24bitAsInt32(overflowBuffer[j],
								overflowBuffer[j + 1], overflowBuffer[j + 2]) * mVoltScale;
					}
					for (int j = index + 26; j < index + PACKET_LENGTH - 1; j += 2) {
						temp.auxValues[(j - index - 26) / 2] = interpret16bitAsInt32(overflowBuffer[j], overflowBuffer[j + 1]) * mAccelScale;
					}
					//temp.printToConsole();
                    analyze(temp);
				}
				System.arraycopy(overflowBuffer, index, overflowBuffer, 0, overflowLength + input.length - index);
				overflowLength += input.length - index;
			} else {
				overflowLength += input.length;
			}
		}
	};

    void analyze(DataPacket dp) {
        mStreamReader.addFrame(dp.values[0]);
        mAlphaDetector.addFrames(new float[]{(float)dp.values[alphaCheckChannels[0]],(float)dp.values[alphaCheckChannels[1]]});
    }

    private class ReadThread extends Thread {
		public ReadThread() {
			this.setPriority(Thread.NORM_PRIORITY);
		}
		@Override
		public void run() {
			byte[] readData;
			int dataLength;
			Message msg;

			while(!isInterrupted()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					break;
				}

				synchronized(mDevice) {
					dataLength = mDevice.getQueueStatus();
					if (dataLength > 0) {
						if (dataLength > MAX_READ_LENGTH) {
							dataLength = MAX_READ_LENGTH;
						}

						readData = new byte[dataLength];
						mDevice.read(readData, dataLength);

						// Object: byte array
						// Arg1: length of array
						if (streaming) {
							msg = PACKET_HANDLER.obtainMessage();
						} else {
							msg = INIT_HANDLER.obtainMessage();
						}
						msg.obj = readData;
						msg.arg1 = dataLength;
						if (streaming) {
							PACKET_HANDLER.sendMessage(msg);
						} else {
							INIT_HANDLER.sendMessage(msg);
						}
					}
				}
			}
			Log.d(TAG, "Reading thread stopped");
		}
	}

    public void startDataStream() {
        writeToDevice(OpenBCICommands.START_STREAM);
        streaming = true;
    }

    public void stopDataStream() {
        writeToDevice(OpenBCICommands.STOP_STREAM);
        streaming = false;
    }

    @Override
    public void blinkStart() {
        // Notify the StatusFragment obtained blink start
        Message notif = Message.obtain();
        notif.arg1=2;
        notif.arg2=1;   //1 is blink start, 2 is blink end, 3 is alpha data
        notif.obj=null;
        try {
            mMessenger.send(notif);
        } catch (Exception e1) {
            Log.w(getClass().getName(), "Exception sending message", e1);
        }
    }

    @Override
    public void blinkEnd(double blinkDuration) {
        // Notify the StatusFragment obtained blink end
        Message notif = Message.obtain();
        notif.arg1=2;
        notif.arg2=2;   //1 is blink start, 2 is blink end, 3 is alpha data
        notif.obj=blinkDuration;
        try {
            mMessenger.send(notif);
        } catch (android.os.RemoteException e1) {
            Log.w(getClass().getName(), "Exception sending message", e1);
        }
    }

    @Override
    public void alpha(AlphaDetector.DetectionData_FreqDomain[] results) {
        double alphampsum = 0;
        for (AlphaDetector.DetectionData_FreqDomain ddfd : results) alphampsum += ddfd.inband_vs_guard_dB;
        Log.d(TAG + "Alpha", Double.toString(alphampsum/results.length));
        // Notify the StatusFragment obtained alpha data
        Message notif = Message.obtain();
        notif.arg1=2;
        notif.arg2=3;   //1 is blink start, 2 is blink end, 3 is alpha data
        notif.obj=results;
        try {
            mMessenger.send(notif);
        } catch (Exception e1) {
            Log.w(getClass().getName(), "Exception sending message", e1);
        }
    }

    @Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating service");
		try {
			sManager = D2xxManager.getInstance(this);
		} catch (D2xxManager.D2xxException e) {
			Log.e(TAG, "D2xx manager", e);
		}
		if(!sManager.setVIDPID(0x0403, 0xada1)) {
			Log.d(TAG, "setVIDPID Error");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "Starting OpenBCI service");
		// Note: intent is null when service restarts itself
		if (intent != null && intent.getExtras() != null) {
			mMessenger = (Messenger) intent.getExtras().get(TAG);
		}
		connectDevice();

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		writeToDevice(OpenBCICommands.STOP_STREAM);
		disconnectDevice();
		Log.d(TAG, "Stopping OpenBCI service");
	}

	void configDevice() {
		if (!mDevice.isOpen()) {
			Log.e(TAG, "setConfig: device not open");
			return;
		}
		mDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		mDevice.setBaudRate(115200);
		mDevice.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
		mDevice.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x0b, (byte) 0x0d);

		Log.d(TAG, "Config finished");
		mDevice.purge(D2xxManager.FT_PURGE_TX);
		mDevice.purge(D2xxManager.FT_PURGE_RX);
		mDevice.restartInTask();
		Log.d(TAG, "Read enabled");

		streaming = false;
		overflowLength = 0;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				writeToDevice(OpenBCICommands.SOFT_RESET);
			}
		}, 3000);
	}

	void connectDevice() {
		if (mDevice == null) {
			if (sManager.createDeviceInfoList(this) > 0) {
				mDevice = sManager.openByIndex(this, 0);
			} else {
				// try again in a few seconds
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						connectDevice();
					}
				}, 3000);
			}
		} else if (mDevice.isOpen()) {
			Log.d(TAG, "Device already open");
			return;
		} else {
			synchronized(mDevice) {
				mDevice = sManager.openByIndex(this, 0);
			}
		}

		if (mDevice == null) {
			Log.d(TAG, "OpenBCI not found");
		} else if (mDevice.isOpen()) {
			configDevice();
			if (mReadThread == null || !mReadThread.isInterrupted()) {
				mReadThread = new ReadThread();
				mReadThread.start();
			}
		} else {
			Log.e(TAG, "Cannot connect to OpenBCI: port not open");
		}
	}

	void disconnectDevice() {
		if (mReadThread != null) {
			mReadThread.interrupt();
		}
		try {
			Thread.sleep(50);
		}
		catch (InterruptedException e) {
			Log.d(TAG, "Thread interrupted");
		}

		if(mDevice != null) {
			synchronized(mDevice) {
				if(mDevice.isOpen()) {
					writeToDevice(OpenBCICommands.STOP_STREAM);
					mDevice.close();
				}
			}
		}
		// Notify the LoginActivity that OpenBCI has been disconnected
		Message notif = Message.obtain();
		notif.arg1 = 0;
		try {
			mMessenger.send(notif);
		} catch (Exception e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}
	}

	void writeToDevice(char value) {
		if (mDevice != null && mDevice.isOpen()) {
			mDevice.setLatencyTimer((byte) 16);
			mDevice.write(new byte[]{(byte) value}, 1);
			Log.d(TAG, "Sent '" + value + "' to device.");
		} else {
			Log.e(TAG, "Write: mDevice not open");
		}
	}

	private int interpret24bitAsInt32(byte a, byte b, byte c) {
		//little endian
		final int newInt = ((0xFF & a) << 16) | ((0xFF & b) << 8) | 0xFF & c;
		if ((newInt & 0x00800000) > 0) {
			return newInt | 0xFF000000;
		}
		return newInt & 0x00FFFFFF;
	}

	private int interpret16bitAsInt32(byte a, byte b) {
		final int newInt = ((0xFF & a) << 8) | (0xFF & b);
		if ((newInt & 0x00008000) > 0) {
			return newInt | 0xFFFF0000;
		}
		return newInt & 0x0000FFFF;
	}
}