package com.elmz.drift.openbci;

/**
 * Created by El1t on 1/17/15.
 */
public class OpenBCICommands
{
	public static final char SOFT_RESET = 'v';
	public static final char STOP_STREAM = 's';
	public static final char START_STREAM = 'b';
	public static final char EIGHT_CHANNEL = 'c';
	public static final char SIXTEEN_CHANNEL = 'C';
	public static final char RESET_CHANNELS = 'd';
	public static final char GET_CHANNELS = 'D';
	public static final char GET_REGISTER = '?';

	final static char[] ACTIVATE_CHANNEL = {'!', '@', '#', '$', '%', '^', '&', '*','Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I'};
	final static char[] DEACTIVATE_CHANNEL = {'1', '2', '3', '4', '5', '6', '7', '8', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i'};

	private static class Channel {
		public final char ACTIVATE;
		public final char DEACTIVATE;
		public Channel(int channel) {
			ACTIVATE = ACTIVATE_CHANNEL[channel];
			DEACTIVATE = DEACTIVATE_CHANNEL[channel];
		}
	}

	public static Channel channel(int channel) {
		return new Channel(channel);
	}
}
