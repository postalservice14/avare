/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.shapes;

import com.ds.avare.R;
import com.ds.avare.gps.GpsParams;
import com.ds.avare.position.Movement;
import com.ds.avare.position.Origin;
import com.ds.avare.position.Scale;
import com.ds.avare.storage.Preferences;
import com.ds.avare.utils.ShadowText;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * 
 * A static class because we do not want allocations in the draw task where this
 * is called.
 * 
 * @author zkhan, rwalker
 *
 */
public class DistanceRings {

	// Some class constants
    public static final int COLOR_DISTANCE_RING = Color.rgb(102, 0, 51);
    public static final int COLOR_SPEED_RING =  Color.rgb(178, 255, 102);
    public static final int COLOR_SHADOW = Color.DKGRAY;
    
    public static final int RING_INNER = 0;
    public static final int RING_MIDDLE = 1;
    public static final int RING_OUTER = 2;
    public static final int RING_SPEED = 3;
    
    private static final int STALLSPEED = 25;
    private static final int RING_INNER_SIZE[] = { 1,  2,  5, 10, 20,  40};
    private static final int RING_MIDDLE_SIZE[] = { 2,  5, 10, 20, 40,  80};
    private static final int RING_OUTER_SIZE[] = { 5, 10, 20, 40, 80, 160};
    private static final int RINGS_1_2_5     = 0;
    private static final int RINGS_2_5_10    = 1;
    private static final int RINGS_5_10_20   = 2;
    private static final int RINGS_10_20_40  = 3;
    private static final int RINGS_20_40_80  = 4;
    private static final int RINGS_40_80_160 = 5;
    
    // Class member objects and data allocations
    float mRings[] = {0, 0, 0, 0};
    String mRingsText[] = {null, null, null, null};
    Paint mPaint;
    float mDipToPix;

    // CTOR - allocate a paint object here to save processing time
    // during the draw call
    public DistanceRings(Resources resources, float dipToPix) {
        mDipToPix = dipToPix;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(resources.getDimension(R.dimen.distanceRingNumberTextSize));
        mPaint.setStrokeWidth(3 * mDipToPix);
        mPaint.setShadowLayer(0, 0, 0, 0);
    }
    
    /**
     * Based upon our current speed, scale and latitude, calculate the
     * size of the distance/speed rings along with their display text 
     * @param context application context
     * @param pref for accessing user preferences
     * @param scale the zoom factor for the display
     * @param movement related to the current display tile
     * @param speed at which we are moving
     */
    private void calculate(Context context,
            Preferences pref, Scale scale, Movement movement, double speed) {
        
    	// Clear out our ring values before doing any work
        mRings[0] = 0;
        mRings[1] = 0;
        mRings[2] = 0;
        mRings[3] = 0;
        
        // Find pixels per nautical mile based upon our latitude
        float pixPerNm = movement.getNMPerLatitude(scale);
        

        // Convert to non MILES or KM if appropriate
        double fac = 1;
        if(pref.getDistanceUnit().equals(context.getString(R.string.UnitMile))) {
            fac *= Preferences.NM_TO_MI;
        }
        else if(pref.getDistanceUnit().equals(context.getString(R.string.UnitKilometer))) {
            fac *= Preferences.NM_TO_KM;
        }

        // Default ring scale of 2/5/10
        int ringScale = RINGS_2_5_10;
        
        // If we dynamically scale the rings, then figure that
        // out here
        if(pref.getDistanceRingType() == 1) {
            int macro = scale.getMacroFactor();
            /* the larger totalZoom is, the more zoomed in we are  */
            if(macro <= 1 && scale.getScaleFactorRaw() > 1) {  
                ringScale = RINGS_1_2_5;        
            } 
            else if(macro <= 1 && scale.getScaleFactorRaw() <= 1) {  
                ringScale = RINGS_2_5_10;
            } 
            else if (macro <= 2) {
                ringScale = RINGS_5_10_20;
            } 
            else if (macro <= 4) {
                ringScale = RINGS_10_20_40;
            } 
            else if (macro <= 8) {
                ringScale = RINGS_20_40_80;
            }  
            else {
                ringScale = RINGS_40_80_160;
            } 
        }
        
        // If we are flying faster than a stall, then figure out
        // the speed ring size
        if(speed >= STALLSPEED && pref.getTimerRingSize() != 0) {
            /*
             * its / 60 as units is in minutes
             */
            mRings[RING_SPEED] = (float) ((float)(speed / 60) * pixPerNm * pref.getTimerRingSize() / fac); 
            mRingsText[RING_SPEED] = String.format("%d", pref.getTimerRingSize());
        }

        // Calculate all 3 distance rings
        mRings[RING_INNER]  = (float)(pixPerNm * RING_INNER_SIZE[ringScale] / fac);
        mRings[RING_MIDDLE] = (float)(pixPerNm * RING_MIDDLE_SIZE[ringScale] / fac);
        mRings[RING_OUTER]  = (float)(pixPerNm * RING_OUTER_SIZE[ringScale] / fac);
        
        // And now the text for each ring
        mRingsText[RING_INNER]  = String.format("%d", RING_INNER_SIZE[ringScale]);
        mRingsText[RING_MIDDLE] = String.format("%d", RING_MIDDLE_SIZE[ringScale]);
        mRingsText[RING_OUTER]  = String.format("%d", RING_OUTER_SIZE[ringScale]);
    }

    /***
     * Draw the distance rings.
     * 
     * @param canvas where to draw them
     * @param shadowText object used to draw any text
     * @param pref used to read user preferences
     * @param gpsParams current GPS reading
     * @paramtmTrackUp true if we are tracking UP 
     * @param origin x/y origin of the chart display
     * @param context application context
     * @param scale what scale we are at
     * @param movement relative to our current tile
     */
    public void draw(Canvas canvas, ShadowText shadowText, 
    		Preferences pref, GpsParams gpsParams, boolean trackUp, 
    		Origin origin, Context context, Scale scale, Movement movement) {

	    // Calculate the size of distance and speed rings
	    double currentSpeed = gpsParams.getSpeed();
	    calculate(context, pref, scale, movement, currentSpeed);

	    // Get our current position. That will be the center of all the rings
	    float x = (float) (origin.getOffsetX(gpsParams.getLongitude()));
	    float y = (float) (origin.getOffsetY(gpsParams.getLatitude()));

	    // Get our current track
	    double bearing = gpsParams.getBearing();
	    if(trackUp) {		// If our direction is always to the top, then set
	    	bearing = 0;	// our bearing to due up as well
	    }

	    // Set the brush up to draw the 3 distance rings
        mPaint.setColor(COLOR_DISTANCE_RING);
        mPaint.setStyle(Style.STROKE);
        mPaint.setAlpha(0x7F);

        // Draw the 3 distance circles now
        canvas.drawCircle(x, y, mRings[RING_INNER],  mPaint);
        canvas.drawCircle(x, y, mRings[RING_MIDDLE], mPaint);
        canvas.drawCircle(x, y, mRings[RING_OUTER],  mPaint);

        // Figure out the adjustment to draw the ring text
        float adjX = (float) Math.sin((bearing - 10) * Math.PI / 180);	// Distance ring numbers, offset from
        float adjY = (float) Math.cos((bearing - 10) * Math.PI / 180);	// the course line for readability

        // Set the paint accordingly
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(COLOR_SPEED_RING);

        // Now draw each rings text
        shadowText.draw(canvas, mPaint,
        		mRingsText[RING_INNER], COLOR_SHADOW,
                x + mRings[RING_INNER] * adjX, 
                y - mRings[RING_INNER] * adjY);
        shadowText.draw(canvas, mPaint,
        		mRingsText[RING_MIDDLE], COLOR_SHADOW,
                x + mRings[RING_MIDDLE] * adjX, 
                y - mRings[RING_MIDDLE] * adjY);
        shadowText.draw(canvas, mPaint,
        		mRingsText[RING_OUTER], COLOR_SHADOW,
                x + mRings[RING_OUTER] * adjX, 
                y - mRings[RING_OUTER] * adjY);

        // Draw our "speed ring" if one was calculated for us
	    if(mRings[RING_SPEED] != 0) {
	
	    	adjX = (float) Math.sin((bearing + 10) * Math.PI / 180);	// So the speed ring number does
	        adjY = (float) Math.cos((bearing + 10) * Math.PI / 180);	// not overlap the distance ring

	        // Configure the paint brush and draw the speed ring
	        mPaint.setStyle(Style.STROKE);
	        mPaint.setColor(COLOR_SPEED_RING);
	        mPaint.setAlpha(0x7F);
	        canvas.drawCircle(x, y, mRings[RING_SPEED], mPaint);

	        // Now set the paint brush up to draw the text
	        mPaint.setStyle(Style.FILL);
	        mPaint.setColor(Color.GREEN);
	        shadowText.draw(canvas, mPaint, 
	        		mRingsText[RING_SPEED], COLOR_SHADOW, 
	        		x + mRings[RING_SPEED] * adjX, 
	        		y - mRings[RING_SPEED] * adjY);
	    }
    }
}
