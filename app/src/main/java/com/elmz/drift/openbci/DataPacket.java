package com.elmz.drift.openbci;

//////////////////////////////////////
//
// This file contains classes that are helfpul in some way.
//
// Created: Chip Audette, Oct 2013 - Dec 2014
//
/////////////////////////////////////

public class DataPacket {
	int sampleIndex;
	double[] values;
	double[] auxValues;

	//constructor, give it "nValues", which should match the number of values in the
	//data payload in each data packet from the Arduino.  This is likely to be at least
	//the number of EEG channels in the OpenBCI system (ie, 8 channels if a single OpenBCI
	//board) plus whatever auxiliary data the Arduino is sending.
	public DataPacket(int nValues, int nAuxValues) {
		values = new double[nValues];
		auxValues = new double[nAuxValues];
	}

	public void printToConsole() {
		System.out.print("printToConsole: DataPacket = ");
		System.out.print(sampleIndex);
		for (double value : values) {
			System.out.print(", " + value);
		}
		for (double auxValue : auxValues) {
			System.out.print(", " + auxValue);
		}
		System.out.println();
	}

	public int copyTo(DataPacket target) {
		return copyTo(target, 0, 0);
	}

	public int copyTo(DataPacket target, int target_startInd_values, int target_startInd_aux) {
		target.sampleIndex = sampleIndex;
		System.arraycopy(values, 0, target.values, target_startInd_values, values.length);
		System.arraycopy(auxValues, 0, target.auxValues, target_startInd_aux, auxValues.length);
		return 0;
	}
}