/*
Copyright (c) 2014, Avare software (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.instruments;

import com.ds.avare.gps.GpsParams;

/***
 * Implementation of a Vertical Speed Indicator. Accepts updates of
 * GPS information to calculate the instantaneous up/down speed in
 * feet per minute. Returns that value when asked.
 * @author RJW
 *
 */
public class VSI {
	private GpsParams	mGpsParams;
	private double 		mVSI;
	
/***
 * Call periodically from a background service with GPS information
 * @param gpsParams - new GPS info
 */
	public void update(GpsParams gpsParams) {
		// If we don't have any "previous" info, then make the
		// new data the one
		if(null == mGpsParams) {
			mGpsParams = gpsParams;
			return;
		}

		// Get the time difference between our old info and the new info
	    double tdiff = ((double)(gpsParams.getTime() - 
	    		mGpsParams.getTime()) / 1000.0);

	    // If more than 1 second has passed then calculate the 
	    // instantaneous vertical speed in ft/min
		if(tdiff > 1) {
			mVSI = ((double)(gpsParams.getAltitude() - mGpsParams.getAltitude()))
					* (60 / tdiff);
			mGpsParams = gpsParams;
		}
	}

	/***
	 * Return the current value of our vertical speed
	 * @return Current VSI value
	 */
	public double getValue() {
		return mVSI;
	}
}
