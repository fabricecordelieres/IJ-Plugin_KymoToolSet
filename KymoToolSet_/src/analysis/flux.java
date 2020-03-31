/**
  * flux.java v1, 5 août 2016
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
package analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageStatistics;
import io.KTSdataFileImporter;
import io.dataIO;
import utilities.filesAndFolders;

/**
 * This class handles operations related to flux analysis
 * @author Fabrice P Cordelieres
 *
 */
public class flux {
	/** Stores the path to the files and folders to be used **/
	filesAndFolders faf=null;
	
	/** Header for the output xls file **/
	public static final String[] HEADER=new String[]{"Time_sec", "Nb_detections", "Delta_nb_detections", "Area_microns2"};
	
	
	/**
	 * Sets the different paths for file to be used when dealing with flux analysis
	 * @param rootPath the root path
	 * @param name the cell name
	 */
	public flux(String rootPath, String name){
		faf=new filesAndFolders(rootPath, name);
	}
	
	/**
	 * Analyses the flux by counting the number of vesicules tracked on the kymograph
	 */
	public void analyseFlux(){
		if(new File(faf.kymoPath_coord).exists()){
			new File(faf.fluxPath).mkdirs();
			
			//Loads the coordinates files
			KTSdataFileImporter coord=new KTSdataFileImporter(faf.kymoPath, faf.name+"_kymo_coord.xls");
			
			//Retrieves the number of timepoints and the kymo path ROI
			ImagePlus ip=new ImagePlus(faf.imagePath_crop);
			Roi roi=null;
			try {
				roi = new RoiDecoder(faf.kymoPath_ROI).getRoi();
			} catch (IOException e) {
				GUI.log.logInfo("Kymo path ROI not found rootPath: "+faf.rootPath+", name: "+faf.name);
				e.printStackTrace();
			}
			
			ip.setRoi(roi);
			IJ.run(ip, "Line to Area", "");
			double area=ip.getStatistics(ImageStatistics.AREA).area;
			
			int nTimepoints=ip.getNSlices();
			double timeCalibration=ip.getCalibration().frameInterval;
			ip.flush();
			ip=null;
			System.gc();
			
			//Creates a table to handle detections data
			int[] nbDetections=new int[nTimepoints];
			ArrayList<String[]> data=coord.getData();
			
			for(int i=0; i<data.size(); i++) nbDetections[(int) Math.floor(Double.parseDouble(data.get(i)[2])/timeCalibration)]++;
			
			//Generates appropriate data for output
			ArrayList<String> headerList=new ArrayList<String>();
			for(int i=0; i<HEADER.length; i++) headerList.add(""+HEADER[i]);
			
			data=new ArrayList<>();
			for(int i=0; i<nTimepoints; i++) data.add(new String[]{""+(i*timeCalibration), ""+nbDetections[i], i==0?"NaN":""+(nbDetections[i]-nbDetections[i-1]), ""+area});
			
			new dataIO(headerList, data).writeData(faf.fluxPath_data);
		}
	}
}
