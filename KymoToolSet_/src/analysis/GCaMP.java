/**
  * GCaMP.java v1, 5 août 2016
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
import java.util.Arrays;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import io.KTSdataFileImporter;
import io.dataIO;
import utilities.filesAndFolders;

/**
 * This class handles GCaMP related operations
 * @author Fabrice P Cordelieres
 *
 */
public class GCaMP {
	/** Stores the path to the files and folders to be used **/
	public filesAndFolders faf=null;
	
	/** Header for the output xls file **/
	public static final String[] HEADER=new String[]{"Time_sec", "Raw_intensity_dendrite", "DeltaF_dendrite", "DeltaF/F_dendrite", "Raw_intensity_synapses", "DeltaF_synapses", "DeltaF/F_synapses"};
	
	/** Header for the output xls file, data part **/
	public static final String[] HEADER_DATA=new String[]{"Nb_vesicles", "Nb_pausing_vesicles", "Pct_pausing_vesicles"};
	
	/** IN direction **/
	public static final int IN=0;
	
	/** PAUSE **/
	public static final int PAUSE=1;
	
	/** OUT direction **/
	public static final int OUT=2;
	
	
	
	
	/**
	 * Sets the different paths for file to be used when dealing with GCaMP analysis
	 * @param rootPath the root path
	 * @param name the cell name
	 */
	public GCaMP(String rootPath, String name){
		faf=new filesAndFolders(rootPath, name);
	}
	
	/**
	 * Performs the analysis and returns a 2D array, double precision, containing the data (1st index: 0: timepoint, 1: raw data dendrite, 2: DF dendrite, 3: DF/F dendrite, 4: raw data synapses, 5: DF synapses, 6: DF/F synapses)
	 * @return a 2D array, double precision, containing the data
	 */
	public double[][] extractData(){
		double[][] out=null; //Stores the data: 0: timepoint, 1: raw data dendrite, 2: DF dendrite, 3:DF/F dendrite, 4: raw data synapses, 5: DF synapses, 6:DF/F synapses
		
		if(new File(faf.imagePath_crop).exists()){
			ImagePlus ip=new CompositeImage(new ImagePlus(faf.imagePath_crop));
			Roi kymoPath=null;
			try {
				kymoPath = new RoiDecoder(faf.kymoPath_ROI).getRoi();
			} catch (IOException e) {
				GUI.log.logInfo("Kymo path ROI not found rootPath: "+faf.rootPath+", name: "+faf.name);
				e.printStackTrace();
			}
			
			ip.setRoi(kymoPath);
			IJ.run(ip, "Line to Area", "");
			Roi roiDendrite=ip.getRoi();
			
			kymoPath.setStrokeWidth(kymoPath.getStrokeWidth()*5);
			ip.setRoi(kymoPath);
			IJ.run(ip, "Line to Area", "");
			Roi roiSynapses=ip.getRoi();
			
			Calibration cal=ip.getCalibration();
			
			out=new double[7][ip.getNSlices()];
			double dt=ip.getCalibration().frameInterval;
			ip.setC(2);
			
			for(int i=0; i<ip.getNSlices(); i++){
				ip.setZ(i+1);
				
				ImageProcessor iproc=ip.getChannelProcessor();
				//Dendrite
				iproc.setRoi(roiDendrite);
				ImageStatistics isDendrite=ImageStatistics.getStatistics(iproc, ImageStatistics.MEAN+ImageStatistics.AREA, cal);
				
				//Synapses
				iproc.setRoi(roiSynapses);
				ImageStatistics isSynapses=ImageStatistics.getStatistics(iproc, ImageStatistics.MEAN+ImageStatistics.AREA, cal);
				
				out[0][i]=i*dt;
				out[1][i]=isDendrite.mean;
				out[4][i]=(isSynapses.mean*isSynapses.area-isDendrite.mean*isDendrite.area)/(isSynapses.area-isDendrite.area);
			}
			//Dendrite
			out[2]=calculateDeltaF(out[1]);
			out[3]=calculateDeltaFOverF(out[1]);
			
			//Synapses
			out[5]=calculateDeltaF(out[4]);
			out[6]=calculateDeltaFOverF(out[4]);
			
			ip.close();
			System.gc();
		}
		
		return out;
	}
	
	/**
	 * Calculates the DeltaF: subtracts the lowest value
	 * @param data input data as a double array
	 * @return DeltaF as a double array
	 */
	public double[] calculateDeltaF(double[] data){
		double[] out=data.clone();
		Arrays.sort(out);
		double min=out[0];
		
		for(int i=0; i<data.length; i++) out[i]=data[i]-min;
		
		return out;
	}
	
	
	/**
	 * Calculates the DeltaF/F: subtracts the lowest value, divides by the lowest value
	 * @param data input data as a double array
	 * @return DeltaF/F as a double array
	 */
	public double[] calculateDeltaFOverF(double[] data){
		double[] out=data.clone();
		Arrays.sort(out);
		double min=out[0];
		
		for(int i=0; i<data.length; i++) out[i]=(data[i]-min)/min;
		
		return out;
	}
	
	/**
	 * Saves the GCaMPData to a XLS file, in the appropriate normalised output folder
	 */
	public void exportGCamPData(){
		if(!new File(faf.GCaMPPath).exists()) new File(faf.GCaMPPath).mkdirs();
		
		
		//Converts header to ArrayList<String>
		ArrayList<String> headerList=new ArrayList<String>();
		for(int i=0; i<HEADER.length; i++) headerList.add(""+HEADER[i]);
		
		//Converts data to ArrayList<String[]>
		double[][] data=extractData();
		
		ArrayList<String[]> dataList=new ArrayList<String[]>();
		
		for(int i=0; i<data[0].length; i++){
			double[] line=new double[HEADER.length];
			for(int j=0; j<line.length; j++) line[j]=data[j][i];
			String[] lineString=new String[line.length];
			for(int j=0; j<line.length; j++) lineString[j]=""+line[j];
			dataList.add(lineString);
		}
		
		new dataIO(headerList, dataList).writeData(faf.GCaMPPath_data);
	}
	
	
	/**
	 * Correlates GCaMP data to instant speed data and writes the corresponding file
	 * @param minSpeed minimum speed to consider for pausing
	 */
	public void correlateToSpeed(double minSpeed){
		if(new File(faf.GCaMPPath).exists() && new File(faf.GCaMPPath_data).exists() && new File(faf.kymoPath_coord).exists()){
			//Load the files to correlate
			KTSdataFileImporter GCaMP=new KTSdataFileImporter(faf.GCaMPPath, faf.name+"_GCaMP_data.xls");
			KTSdataFileImporter coord=new KTSdataFileImporter(faf.kymoPath, faf.name+"_kymo_coord.xls");
			
			ArrayList<String[]> coordData=coord.getData();
			int nbVesicules=Integer.parseInt(coordData.get(coordData.size()-1)[1]);
			
			//Prepares data containers
			ArrayList<String> header=GCaMP.getHeader();
			ArrayList<String[]> data=GCaMP.getData();
			
			//Adds new headers and prepares extra empty columns for data: DATA
			for(int i=0; i<HEADER_DATA.length; i++){
				header.add(HEADER_DATA[i]);
				for(int j=0; j<data.size(); j++){
					String[] currLine=data.get(j);
					String[] newLine=new String[currLine.length+1];
					
					for(int k=0; k<currLine.length; k++) newLine[k]=currLine[k];
					newLine[newLine.length-1]="";
					data.set(j, newLine);
				}
			}
			
			//Adds new headers and prepares extra empty columns for data: Vesicles
			for(int i=0; i<nbVesicules; i++){
				header.add("Instant_Speed_Vesicule_"+(i+1)+"_(microns_per_sec)");
				for(int j=0; j<data.size(); j++){
					String[] currLine=data.get(j);
					String[] newLine=new String[currLine.length+1];
					
					for(int k=0; k<currLine.length; k++) newLine[k]=currLine[k];
					newLine[newLine.length-1]="";
					data.set(j, newLine);
				}
			}
			
			//For each line in the coord file, finds where to place the speed, based on the vesicule's number and timepoint
			for(int i=0; i<coordData.size(); i++){
				int vesicule=Integer.parseInt(coordData.get(i)[1])-1;
				
				//Find corresponding timepoint on the data table
				for(int j=0; j<data.size(); j++){
					//Trick to correct rounding issues: anyway we can't go to the microsecond precision !
					if(Math.abs(Double.parseDouble(data.get(j)[0])-Double.parseDouble(coordData.get(i)[2]))<0.001){
						data.get(j)[vesicule+HEADER.length+HEADER_DATA.length]=coordData.get(i)[6].equals("NaN")?"":coordData.get(i)[6];
						break;
					}
				}
			}
			
			//Calculates the nb of vesicles, the nb of pausing vesicles and the pct of pausing vesicles
			for(int i=0; i<data.size(); i++){
				int nbVesicles=0;
				int nbPausingVesicles=0;
				//Examines in turn each vesicle column
				for(int j=HEADER.length+HEADER_DATA.length; j<header.size(); j++){
					String column=data.get(i)[j];
					if(column!=""){
						nbVesicles++;
						if(Math.abs(Double.parseDouble(column))<=minSpeed) nbPausingVesicles++;
					}
					//Log data
					data.get(i)[HEADER.length]=nbVesicles==0?"":""+nbVesicles;
					data.get(i)[HEADER.length+1]=nbVesicles==0?"":""+nbPausingVesicles;
					data.get(i)[HEADER.length+2]=nbVesicles==0?"":""+(((double) nbPausingVesicles)/((double) nbVesicles));
				}
			}
			
			
			new dataIO(header, data).writeData(faf.GCaMPPath_data_correl_speed);
		}else{
			GUI.log.logInfo("Either the kymograph and/or GCaMP analysis/are missing for "+faf.name);
		}
	}
	
	/**
	 * Extracts from the GCaMP/speed correlated data only the speeds that are in the input direction (IN/PAUSE/OUT)
	 * @param direction input direction: use the static class field IN, PAUSE or OUT
	 * @param minSpeed the speed limit to consider for pausing
	 */
	public void separateDirections(int direction, double minSpeed){
		if(new File(faf.GCaMPPath).exists() && new File(faf.GCaMPPath_data_correl_speed).exists()){
			//Load the data to split
			KTSdataFileImporter GCaMPCorrel=new KTSdataFileImporter(faf.GCaMPPath_data_correl_speed);
			GCaMPCorrel.read();
			
			//Prepares data containers
			ArrayList<String> header=GCaMPCorrel.getHeader();
			ArrayList<String[]> data=GCaMPCorrel.getData();
			
			//For each line and each vesicle, erases any value of speed that is not in the input state
			for(int i=0; i<data.size(); i++){
				String[] currLine=data.get(i);
				
				//Vesicles' columns start at position HEADER.length+HEADER_DATA.length
				for(int j=HEADER.length+HEADER_DATA.length; j<currLine.length; j++){
					double value=Double.NaN;
					String stringToPush="";
					try {
						value=Double.parseDouble(currLine[j]);
						//Checks the value is in the expected range and modifies the stringToPush accordingly
						switch(direction){
							case IN: if(value<-minSpeed) stringToPush=""+value; break;
							case PAUSE: if(Math.abs(value)<=minSpeed) stringToPush=""+value; break;
							case OUT: if(value>minSpeed) stringToPush=""+value; break;
						}
					} catch (Exception e) {
						//In case the String is not parsable, does nothing and do not display error message
					}
					currLine[j]=stringToPush; //Replaces the cell with its new content
				}
				data.set(i, currLine); //Replaces the line with its new version
			}
			
			String outFilePath="";
			switch(direction){
				case IN: outFilePath=faf.GCaMPPath_data_correl_speed_IN; break;
				case PAUSE: outFilePath=faf.GCaMPPath_data_correl_speed_PAUSE; break;
				case OUT: outFilePath=faf.GCaMPPath_data_correl_speed_OUT; break;
			}
			new dataIO(header, data).writeData(outFilePath);
		}else{
			GUI.log.logInfo("Either the GCaMP folder and/or the GCaMP data correlation file is/are missing for "+faf.name);
		}
	}
}
