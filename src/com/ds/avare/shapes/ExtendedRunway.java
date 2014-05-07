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

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.ds.avare.R;
import com.ds.avare.gps.GpsParams;
import com.ds.avare.place.Destination;
import com.ds.avare.place.Runway;
import com.ds.avare.position.Origin;
import com.ds.avare.utils.BitmapHolder;
import com.ds.avare.utils.Helper;
import com.ds.avare.utils.ShadowText;

/***
 * A basic class that knows how to draw the ExtendedRunway centerline and pattern shapes
 * 
 * @author Ron
 *
 */
public class ExtendedRunway {
    private BitmapHolder    mBitmap;	// To hold the image of a runway
    private Paint 			mPaint;		// What paint to use on the canvas
    private float			mDipToPix;	// Device Independent Pixels to real pixels

    private static final int SHADOW = 4;	// Shadow radius

    /***
     * Ctor - set up member elements
     * @param context
     * @param mPaint
     * @param mResources
     * @param dipToPix
     */
    public ExtendedRunway(Context context, Paint paint, float textSize, float dipToPix) { 
        mBitmap   = new BitmapHolder(context, R.drawable.runway_extension);
        mPaint    = new Paint(paint);
        mDipToPix = dipToPix;
        mPaint.setTextSize(textSize);
	}

	/***
	 * Draw runway extensions on the indicated canvas for the current destination
	 * 
	 * @param canvas What to draw upon
	 * @param origin the top/left X/Y of the current chart display
	 * @param dest airport where we are intending to go
	 * @param trackUp true if tracking is always UP
	 * @param gpsParams current GPS situation
	 * @param shadowText object used to draw text
	 */
	public void draw(Canvas canvas, Origin origin, Destination dest, 
			boolean trackUp, GpsParams gpsParams, ShadowText shadowText)
	{
		// If we don't have a bitmap, then we can't draw anything
	    if (null == mBitmap) {
	    	return;
	    }

	    // If we don't have any runways, then we can't draw them
	    LinkedList<Runway> runways = dest.getRunways();
        if (null == runways) {
        	return;
        }

        int xfactor;
        int yfactor;

        /*
         * For all runways
         */
        for (Runway r : runways) {
            float heading = r.getTrue();
            if (Runway.INVALID == heading) {
                continue;
            }
            /*
             * Get lat/lon of the runway. If either one is invalid, use
             * airport lon/lat
             */
            double lon = r.getLongitude();
            double lat = r.getLatitude();
            if (Runway.INVALID == lon || Runway.INVALID == lat) {
                lon = dest.getLocation().getLongitude();
                lat = dest.getLocation().getLatitude();
            }
            /*
             * Rotate and position the runway bitmap
             */
            Helper.rotateBitmapIntoPlace(origin, mBitmap, heading, lon, lat,
                    false);
            /*
             * Draw it.
             */
            canvas.drawBitmap(mBitmap.getBitmap(),
                    mBitmap.getTransform(), mPaint);
            /*
             * Get the canvas x/y coordinates of the runway itself
             */
            float x = (float)origin.getOffsetX(lon);
            float y = (float)origin.getOffsetY(lat);
            /*
             * The runway number, i.e. What's painted on the runway
             */
            String num = r.getNumber();

            /*
             * If there are parallel runways, offset their text so it
             * does not overlap
             */
            xfactor = yfactor = (int)(mBitmap.getHeight() + mPaint.getTextSize()/2);
            
            if (num.contains("C")) {
                xfactor = yfactor = xfactor * 3 / 4;
            }
            else if (num.contains("L")) {
                xfactor = yfactor = xfactor / 2;
            }

            /*
             * Determine canvas coordinates of where to draw the runway
             * numbers with simple rotation math.
             */
            float runwayNumberCoordinatesX = x + xfactor
                    * (float) Math.sin(Math.toRadians(heading - 180));
            float runwayNumberCoordinatesY = y - yfactor
                    * (float) Math.cos(Math.toRadians(heading - 180));

            mPaint.setStyle(Style.FILL);
            mPaint.setColor(Color.BLUE);
            mPaint.setAlpha(162);
            mPaint.setShadowLayer(0, 0, 0, 0);
            mPaint.setStrokeWidth(4 * mDipToPix);

            /*
             * Get a vector perpendicular to the vector of the runway
             * heading bitmap
             */
            float vXP = -(runwayNumberCoordinatesY - y);
            float vYP = (runwayNumberCoordinatesX - x);

            /*
             * Reverse the vector of the pattern line if right traffic
             * is indicated for this runway
             */
            if (r.getPattern().equalsIgnoreCase("Right")) {
                vXP = -(vXP);
                vYP = -(vYP);
            }

            /*
             * Draw the base leg of the pattern
             */
            canvas.drawLine(runwayNumberCoordinatesX,
                    runwayNumberCoordinatesY, runwayNumberCoordinatesX
                    + vXP / 3, runwayNumberCoordinatesY + vYP
                    / 3, mPaint);

            /*
             * If in track-up mode, rotate canvas around screen x/y of
             * where we want to draw runway numbers in opposite
             * direction to bearing so they appear upright
             */
            canvas.save();
            if (trackUp && (gpsParams != null)) {
                canvas.rotate((int) gpsParams.getBearing(),
                    runwayNumberCoordinatesX,
                    runwayNumberCoordinatesY);
            }

            /*
             * Draw the text so it's centered within the shadow
             * rectangle, which is itself centered at the end of the
             * extended runway centerline
             */
            
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(Color.WHITE);
            mPaint.setAlpha(255);
            mPaint.setShadowLayer(SHADOW, SHADOW, SHADOW, Color.BLACK);
            shadowText.draw(canvas, mPaint, num, Color.DKGRAY,
                    runwayNumberCoordinatesX, runwayNumberCoordinatesY);
            if (trackUp) {
                canvas.restore();
            }
        }
	}
}
