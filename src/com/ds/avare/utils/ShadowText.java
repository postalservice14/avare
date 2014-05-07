/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class ShadowText {
	private Paint 	mPaint;
	private RectF	mBox;
	private Rect	mTextSize;
	private float	mDipToPix;

    int XMARGIN;
    int YMARGIN;
    int SHADOWRECTRADIUS;

    // Allocate some objects in the CTOR since this is called during
    // the main screen paint process and we have to be quick
	public ShadowText(float dipToPix) {
    	mBox 	  = new RectF();
    	mTextSize = new Rect();
    	mPaint    = new Paint();

    	mDipToPix = dipToPix;
        XMARGIN = (int) (5 * mDipToPix);
        YMARGIN = (int) (5 * mDipToPix);
        SHADOWRECTRADIUS = (int) (5 * mDipToPix);
	}
	
    /**
     * Display the text in the indicated paint with a shadow'd background. This aids in readability.
     * 
     * @param canvas where to draw
     * @param paint what paint object to draw the text with.
     * @param text what to display
     * @param shadowColor is the color of the shadow of course
     * @param x center position of the text on the canvas
     * @param y top edge of text on the canvas
     */
    public void draw(Canvas canvas, Paint paint, String text, 
    				 int shadowColor, float x, float y) {

    	// Get the bounds of the text we are to paint
        paint.getTextBounds(text, 0, text.length(), mTextSize);
        
        // Figure out the dimensions of the shadow box based upon the text
        mBox.bottom = mTextSize.bottom + YMARGIN + y - (mTextSize.top / 2);
        mBox.top    = mTextSize.top    - YMARGIN + y - (mTextSize.top / 2);
        mBox.left   = mTextSize.left   - XMARGIN + x - (mTextSize.right / 2);
        mBox.right  = mTextSize.right  + XMARGIN + x - (mTextSize.right / 2);

        // Set the color and transparency
        mPaint.setColor(shadowColor);
        mPaint.setAlpha(0x80);
        
        // The rectangle with rounded corners
        canvas.drawRoundRect(mBox, SHADOWRECTRADIUS, SHADOWRECTRADIUS, mPaint);
        
        // Finish it off with the text
        canvas.drawText(text,  x - (mTextSize.right / 2), y - (mTextSize.top / 2), paint);
    }
}
