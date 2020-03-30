/**
  * GCampTools.java v1, 29 mai 2016
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
package steps;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.plugin.frame.RoiManager;
import settings.settings;
import utilities.utilities;

/**
 * This class is aimed at handling GCamp related operations
 * @author Fabrice P Cordelieres
 *
 */
public class GCampTools{
	/**
	 * Defines the baseline for GCamp measurements as the average intensity over the full image
	 * @param cell the cell to be analysed
	 * @return the baseline, as a double array
	 */
	public static double[] getBaseline(String cell){
		settings.setFolderAndCurrentCell(cell);
		ImagePlus ip=new ImagePlus(settings.destForPPT+settings.currCell+"_composite.zip");
		if(ip!=null){
			ip.show();
			double[] baseline=new double[ip.getNSlices()];
			ip.setC(2);
			if(settings.lambda==settings.LAMBDA_ARRAY[1]) ip.setC(1);
			for(int i=1; i<=ip.getNSlices(); i++){
				ip.setZ(i);
				ip.deleteRoi();
				baseline[i-1]=ip.getStatistics(Measurements.MEAN).mean;
			}
			ip.changes=false;
			ip.close();
			return baseline;
		}
		return null;
	}
	
	/**
	 * Load a crop version of the original composite, then performs the raw intensities measurements
	 * within the input ROIs
	 * @param cell the cell to process
	 * @param pathToROIs path to the ROIs to analyse
	 * @param pathToImage path to the image on which to quantify
	 * @return a 2D double array, first dimension being the ROI, second the time
	 */
	public static double[][] getGcampFromROIs(String cell, String pathToROIs, String pathToImage){
		settings.setFolderAndCurrentCell(cell);
		RoiManager rm=RoiManager.getInstance();
		if(rm==null){
			rm=new RoiManager();
		}else{
			rm.reset();
		}
		rm.runCommand("Open", pathToROIs);
		
		ImagePlus ip=new ImagePlus(pathToImage);
		if(ip!=null){
			ip.show();
			double[][] intensities=new double[rm.getCount()][ip.getNSlices()];
			ip.setC(2);
			if(settings.lambda==settings.LAMBDA_ARRAY[1]) ip.setC(1);
			
			for(int i=1; i<=ip.getNSlices(); i++){
				for(int j=0; j<rm.getCount(); j++){
					ip.setRoi(rm.getRoi(j));
					ip.setZ(i);
					intensities[j][i-1]=ip.getStatistics(Measurements.MEAN).mean;
				}
			}
			ip.changes=false;
			ip.close();
			return intensities;
		}
		return null;
	}
	
	/**
	 * Calculates and saves DF on F based on input data
	 * @param cell the cell to proceed
	 * @param pathToROIs the path to the ROIs to load
	 * @param pathToImage the path to the image on which measurements are to be made
	 * @param pathToSave the path where the CSV file should be saved
	 */
	public static void saveDFonF(String cell, String pathToROIs, String pathToImage, String pathToSave){
		double[] baseline=getBaseline(cell);
		double[][] rawIntensities=getGcampFromROIs(cell, pathToROIs, pathToImage);
		String[] header=new String[rawIntensities.length*2+2];
		ArrayList<String[]> data = new ArrayList<String[]>();
		RoiManager rm=RoiManager.getInstance();
		
		//Build header
		header[0]="Time";
		header[1]="Baseline";
		int index=2;
		for(int i=0; i<rm.getCount(); i++){
			String roiName=rm.getName(i);
			header[index++]="Raw_"+roiName;
			header[index++]="DFonF_"+roiName;
		}
		
		for(int i=0; i<baseline.length; i++){
			String[] line=new String[rawIntensities.length*2+2];
			line[0]=i*settings.calibT+"";
			line[1]=baseline[i]+"";
			index=2;
			for(int j=0; j<rawIntensities.length; j++){
				line[index++]=rawIntensities[j][i]+"";
				line[index++]=((rawIntensities[j][i]-baseline[i])/baseline[i])+"";
			}
			data.add(line);
		}
		
		utilities.saveAsCSV(header, data, pathToSave);
	}

}
