/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <MyOnTouchListener.java> is part of Minimal Dicom Viewer.
 *
 * Minimal Dicom Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minimal Dicom Viewer is distributed as Open Source Software ( OSS )
 * and comes WITHOUT ANY WARRANTY and even with no IMPLIED WARRANTIES OF MERCHANTABILITY,
 * OF SATISFACTORY QUALITY, AND OF FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License ( GPLv3 ) for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Minimal Dicom Viewer. If not, see <http://www.gnu.org/licenses/>.
 *
 * Released date: 13-11-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView.ScaleType;

public class MyOnTouchListener implements OnTouchListener {

	
	DicomImageView div;
	Context context;
	
	
	public MyOnTouchListener(Context context, DicomImageView div)
	{
		this.div = div;
		this.context = context;
		div.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		div.matrix.setTranslate(1f, 1f);
		div.m = new float[9];
		div.setImageMatrix(div.matrix);
		div.setScaleType(ScaleType.MATRIX);
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		div.mScaleDetector.onTouchEvent(event);

		div.matrix.getValues(div.m);
        float x = div.m[Matrix.MTRANS_X];
        float y = div.m[Matrix.MTRANS_Y];
        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	div.last.set(event.getX(), event.getY());
            	div.start.set(div.last);
                div.mode = DicomImageView.DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (div.mode == DicomImageView.DRAG) {
                    float deltaX = curr.x - div.last.x;
                    float deltaY = curr.y - div.last.y;
                    float scaleWidth = Math.round(div.origWidth * div.saveScale);
                    float scaleHeight = Math.round(div.origHeight * div.saveScale);
                    if (scaleWidth < div.width) {
                        deltaX = 0;
                        if (y + deltaY > 0)
                            deltaY = -y;
                        else if (y + deltaY < -div.bottom)
                            deltaY = -(y + div.bottom); 
                    } else if (scaleHeight < div.height) {
                        deltaY = 0;
                        if (x + deltaX > 0)
                            deltaX = -x;
                        else if (x + deltaX < -div.right)
                            deltaX = -(x + div.right);
                    } else {
                        if (x + deltaX > 0)
                            deltaX = -x;
                        else if (x + deltaX < -div.right)
                            deltaX = -(x + div.right);

                        if (y + deltaY > 0)
                            deltaY = -y;
                        else if (y + deltaY < -div.bottom)
                            deltaY = -(y + div.bottom);
                    }
                    div.matrix.postTranslate(deltaX, deltaY);
                    div.last.set(curr.x, curr.y);
                }
                break;

            case MotionEvent.ACTION_UP:
            	div.mode = DicomImageView.NONE;
                int xDiff = (int) Math.abs(curr.x - div.start.x);
                int yDiff = (int) Math.abs(curr.y - div.start.y);
                if (xDiff < DicomImageView.CLICK && yDiff < DicomImageView.CLICK)
                	div.performClick();
                break;

            case MotionEvent.ACTION_POINTER_UP:
            	div.mode = DicomImageView.NONE;
                break;
        }
        div.setImageMatrix(div.matrix);
        this.div.invalidate();
        return true; // indicate event was handled
    }
	
	
	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScaleBegin(ScaleGestureDetector detector) {
	    	div.mode = DicomImageView.ZOOM;
	        return true;
	    }

	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	        float mScaleFactor = (float)Math.min(Math.max(.95f, detector.getScaleFactor()), 1.05);
	        float origScale = div.saveScale;
	        div.saveScale *= mScaleFactor;
	        if (div.saveScale > div.maxScale) {
	        	div.saveScale = div.maxScale;
	            mScaleFactor = div.maxScale / origScale;
	        } else if (div.saveScale < div.minScale) {
	        	div.saveScale = div.minScale;
	            mScaleFactor = div.minScale / origScale;
	        }
	        div.right = div.width * div.saveScale - div.width - (2 * div.redundantXSpace * div.saveScale);
	        div.bottom = div.height * div.saveScale - div.height - (2 * div.redundantYSpace * div.saveScale);
	        if (div.origWidth * div.saveScale <= div.width || div.origHeight * div.saveScale <= div.height) {
	        	div.matrix.postScale(mScaleFactor, mScaleFactor, div.width / 2, div.height / 2);
	            if (mScaleFactor < 1) {
	            	div.matrix.getValues(div.m);
	                float x = div.m[Matrix.MTRANS_X];
	                float y = div.m[Matrix.MTRANS_Y];
	                if (mScaleFactor < 1) {
	                    if (Math.round(div.origWidth * div.saveScale) < div.width) {
	                        if (y < -div.bottom)
	                        	div.matrix.postTranslate(0, -(y + div.bottom));
	                        else if (y > 0)
	                        	div.matrix.postTranslate(0, -y);
	                    } else {
	                        if (x < -div.right) 
	                        	div.matrix.postTranslate(-(x + div.right), 0);
	                        else if (x > 0) 
	                        	div.matrix.postTranslate(-x, 0);
	                    }
	                }
	            }
	        } 
	        else 
	        {
	        	div.matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
	        	div.matrix.getValues(div.m);
	            float x = div.m[Matrix.MTRANS_X];
	            float y = div.m[Matrix.MTRANS_Y];
	            if (mScaleFactor < 1) {
	                if (x < -div.right) 
	                	div.matrix.postTranslate(-(x + div.right), 0);
	                else if (x > 0) 
	                	div.matrix.postTranslate(-x, 0);
	                if (y < -div.bottom)
	                	div.matrix.postTranslate(0, -(y + div.bottom));
	                else if (y > 0)
	                	div.matrix.postTranslate(0, -y);
	            }
	        }
	        return true;
	    }
	}
	
	
	public void changeSize(float factor)
	{
		float mScaleFactor = (float)Math.min(Math.max(.95f, factor), 1.05);
        float origScale = div.saveScale;
        div.saveScale *= mScaleFactor;
        if (div.saveScale > div.maxScale) {
        	div.saveScale = div.maxScale;
            mScaleFactor = div.maxScale / origScale;
        } else if (div.saveScale < div.minScale) {
        	div.saveScale = div.minScale;
            mScaleFactor = div.minScale / origScale;
        }
        div.right = div.width * div.saveScale - div.width - (2 * div.redundantXSpace * div.saveScale);
        div.bottom = div.height * div.saveScale - div.height - (2 * div.redundantYSpace * div.saveScale);
        if (div.origWidth * div.saveScale <= div.width || div.origHeight * div.saveScale <= div.height) {
        	div.matrix.postScale(mScaleFactor, mScaleFactor, div.width / 2, div.height / 2);
            if (mScaleFactor < 1) {
            	div.matrix.getValues(div.m);
                float x = div.m[Matrix.MTRANS_X];
                float y = div.m[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (Math.round(div.origWidth * div.saveScale) < div.width) {
                        if (y < -div.bottom)
                        	div.matrix.postTranslate(0, -(y + div.bottom));
                        else if (y > 0)
                        	div.matrix.postTranslate(0, -y);
                    } else {
                        if (x < -div.right) 
                        	div.matrix.postTranslate(-(x + div.right), 0);
                        else if (x > 0) 
                        	div.matrix.postTranslate(-x, 0);
                    }
                }
            }
        } 
	}


}
