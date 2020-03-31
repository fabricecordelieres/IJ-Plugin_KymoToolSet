/**
 * kymoROITimeCropper.java v1, 16 may 2017
   Fabrice P Cordelieres, fabrice.cordelieres at gmail.com
   
   Copyright (C) 2016 Fabrice P. Cordelieres
 
   License:
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package io;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;

/**
 * This class is aimed at cropping a kymo/serie of ROIs along time 
 * @author fab
 *
 */
public class kymoROITimeCropper {
	/** Stores all ROIs in an array **/
	Roi[] rois=null;
	
	/**
	 * Creates a new instance of kymoROITimeCropper, based on the ROI Manager's content
	 */
	public kymoROITimeCropper(){
		RoiManager rm=RoiManager.getInstance();
		if(rm!=null){
			rois=rm.getRoisAsArray();
		}else{
			throw new IllegalArgumentException("ROI Manager is empty");
		}
	}
	
	/**
	 * Creates a new instance of kymoROITimeCropper, based on a file
	 * @param path path to the ROIs file
	 */
	public kymoROITimeCropper(String path){
		RoiManager rm=getEmptyRoiManager();
		rm.runCommand("open", path);
		rois=rm.getRoisAsArray();
	}
	
	/**
	 * Creates a new instance of kymoROITimeCropper, based on a file
	 * @param dir path to the ROIs file
	 * @param fileName name of the ROIs file
	 */
	public kymoROITimeCropper(String dir, String fileName){
		this(dir+fileName);
	}
	
	/**
	 * Crops ROIs from start timepoint to end timepoint (both included)
	 * @param startTimePoint starting timepoint
	 * @param endTimePoint ending timepoint
	 */
	public void timeCropROIs(int startTimePoint, int endTimePoint){
		RoiManager rm=getEmptyRoiManager();
		
		for(int i=0; i<rois.length; i++){
			Polygon pol=timeCropPolygon(rois[i].getPolygon(), startTimePoint, endTimePoint);
			if(pol!=null){
				float strokeWidth=rois[i].getStrokeWidth();
				rois[i]=new PolygonRoi(pol, Roi.POLYLINE);
				rois[i].setName("Time_Cropped_ROI_"+(i+1));
				rois[i].setStrokeColor(Color.RED);
				rois[i].setStrokeWidth(strokeWidth);
				rm.addRoi(rois[i]);
			}
		}
	}
	
	/**
	 * Time crop the input KymoROI related polygon
	 * @param pol input polygon
	 * @param startTimePoint start timepoint (included)
	 * @param endTimePoint end timepoint (included)
	 * @return a polygon with the cropped kymoROI
	 */
	public Polygon timeCropPolygon(Polygon pol, int startTimePoint, int endTimePoint){
		ArrayList<Point> outPoints=new ArrayList<Point>();
		
		//Case where the ROI is out of the time interval
		if(pol.ypoints[0]>endTimePoint) return null;
		
		//Find first point
		if(pol.ypoints[0]>startTimePoint) outPoints.add(new Point(pol.xpoints[0], pol.ypoints[0])); //Case where the ROI starts after the startTimePoint
		if(outPoints.size()==0){
			//Get borders
			//Calculate position of the point
			Point[] pointsAround=getPointsAroundTimepoints(pol, startTimePoint);
			if(pointsAround==null) return null;
			outPoints.add(getPointAtTimepoint(pointsAround, startTimePoint));
		}
		
		
		//Find last point
		if(pol.ypoints[pol.npoints-1]<endTimePoint) outPoints.add(new Point(pol.xpoints[pol.npoints-1], pol.ypoints[pol.npoints-1])); //Case where the ROI ends before the endTimePoint
		if(outPoints.size()==1){
			//Get borders
			//Calculate position of the point
			Point[] pointsAround=getPointsAroundTimepoints(pol, endTimePoint);
			if(pointsAround==null) return null;
			outPoints.add(getPointAtTimepoint(pointsAround, endTimePoint));
		}
		
		//Fills the positions in between
		for(int i=0; i<pol.npoints; i++){
			if(pol.ypoints[i]>startTimePoint && pol.ypoints[i]<endTimePoint) outPoints.add(outPoints.size()-1, new Point(pol.xpoints[i], pol.ypoints[i]));
		}
		
		return arrayPointToPolygon(outPoints);
	}
	
	
	/**
	 * Looks for a point at the input timepoint. In case it is not found, returns null
	 * @param pol a polygon where x is dimension along an axis, y the timepoint
	 * @param timePoint the timepoint to look for
	 * @return the point at input timepoint in case it is found, null otherwise
	 */
	public Point getPointAtTimepoint(Polygon pol, int timePoint){
		for(int i=0; i<pol.npoints; i++) if(pol.ypoints[i]==timePoint) return new Point(pol.xpoints[i], pol.ypoints[i]);
		
		return null;
	}
	
	/**
	 * Looks for the 2 points on the sides of the input timepoint. In case they are not found, returns null
	 * @param pol a polygon where x is dimension along an axis, y the timepoint
	 * @param timePoint the timepoint to look for
	 * @return the 2 points on the sides of the input timepoint in case they are found, as a Point array, null other wise
	 */
	public Point[] getPointsAroundTimepoints(Polygon pol, int timePoint){
		for(int i=1; i<pol.npoints; i++) if(pol.ypoints[i]>timePoint) return new Point[]{new Point(pol.xpoints[i-1], pol.ypoints[i-1]), new Point(pol.xpoints[i], pol.ypoints[i])};
		
		return null;
	}
	
	/**
	 * Looks for a point at the input timepoint. In case it is not found, returns null
	 * @param points the two points on both sides of the target timepoint
	 * @param timePoint the timepoint to look for
	 * @return the point at input timepoint in case it is found, null otherwise
	 */
	public Point getPointAtTimepoint(Point[] points, int timePoint){
		FloatPolygon line=new Line(points[0].x, points[0].y, points[1].x, points[1].y).getInterpolatedPolygon(1, true);
		int index=line.npoints-1;
		while (index>=0){
			if((int) line.ypoints[index]==timePoint){
				return new Point(Math.round(line.xpoints[index]), Math.round(line.ypoints[index]));
			}
			index--;
		}
		
		return null;
	}
	
	/**
	 * Transforms an ArrayList of Points to a polygon
	 * @param points the input ArrayList of Points
	 * @return a Polygon
	 */
	public Polygon arrayPointToPolygon(ArrayList<Point> points){
		int[] x=new int[points.size()];
		int[] y=new int[points.size()];
		
		for(int i=0; i<points.size(); i++){
			x[i]=points.get(i).x;
			y[i]=points.get(i).y;
		}
		
		return new Polygon(x, y, x.length);
	}
	
	/**
	 * Saves the cropped ROIs that were temporarily stored in the ROI Manager
	 * @param path full path where the file should be saved
	 */
	public void saveCroppedRois(String path){
		RoiManager rm=RoiManager.getInstance();
		if(rm!=null) rm.runCommand("save", path);
	}
	
	/**
	 * Gets the current ROI Manager, empties it and returns it or creates a new instance
	 * @return an empty ROI Manager instance
	 */
	private RoiManager getEmptyRoiManager(){
		RoiManager rm=RoiManager.getInstance();
		if(rm==null){
			rm=new RoiManager();
			rm.setVisible(true);
		}else{
			rm.reset();
			rm.setVisible(false);
			rm.setVisible(true);
		}
		return rm;
	}
}
