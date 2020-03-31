/**
  * GCaMPUncaging.java v1, 27 janvier 2017
    Fabrice P Cordelieres, fabrice.cordelieres at gmail.com
    
    Copyright (C) 2017 Fabrice P. Cordelieres
  
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
import ij.gui.WaitForUserDialog;
import ij.io.RoiDecoder;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
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
public class GCaMPUncaging {
	/** Stores the path to the files and folders to be used **/
	public filesAndFolders faf=null;
	
	/** Stores the number of pre-perturbation images **/
	public int nPreImages=50;
	
	/** Stores the time intervalle between pre-perturbation sequences acquisition (sec)**/
	public double preIntervalle=0.426;
	
	/** Stores the duration of a single perturbation (sec)**/
	public double perturbationDuration=0.8;
	
	/** Stores the number of perturbation cycles**/
	public int nCycles=10;
	
	/** Stores the number of post-perturbation images per cycle**/
	public int nPostImages=5;
	
	/** Stores the time intervalle between post-perturbation sequences acquisition (sec)**/
	public double postIntervalle=0.426;
	
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
	 * @param parameters
	 */
	public GCaMPUncaging(String rootPath, String name, double[] parameters){
		faf=new filesAndFolders(rootPath, name);
		
		if(parameters.length!=6) throw new IllegalArgumentException("GCaMPUncaging: invalid number of parameters ("+parameters.length+" instead of 6)");
		
		nPreImages=(int) parameters[0];
		preIntervalle=parameters[1];
		perturbationDuration=parameters[2];
		nCycles=(int) parameters[3];
		nPostImages=(int) parameters[4];
		postIntervalle=parameters[5];
	}
	
	/**
	 * Allows moving the targetted synapses' ROIs to fit on the actual synapses, not the surrounding areas where uncaging has been performed
	 * @param name the cell name
	 */
	public void moveSynapsesRois(String name){
		faf.setName(name);
		if(new File(faf.GCaMPUncagingPath_rois).exists() && new File(faf.imagePath_crop_proj).exists()){
			ImagePlus ip=new ImagePlus(faf.imagePath_crop_proj);
			ip.show();
			
			RoiManager rm=RoiManager.getInstance();
			if(rm!=null){
				rm.reset();
			}else{
				rm=new RoiManager();
			}
			rm.runCommand("Open", faf.GCaMPUncagingPath_rois);
			rm.runCommand(ip,"Show All with labels");
			
			new WaitForUserDialog("Move/Delete ROIs on the image\n(drag the numbers in the ROIs,\n or click the number and press Delete),\nthen press Ok to save new ROIs").show();
			
			rm.deselect();
			rm.runCommand("Save", faf.GCaMPUncagingPath_rois);
			ip.close();
			ip.flush();
		}else{
			GUI.log.logInfo("Either the image (crop/proj, "+faf.imagePath_crop_proj+") and/or the synaptic ROIs ("+faf.GCaMPUncagingPath_rois+") has/have not been found");
		}
	}
	
	/**
	 * Performs the analysis and returns a 2D array, double precision, containing the data (1st index: 0: timepoint, 1: raw data dendrite, 2: DF dendrite, 3: DF/F dendrite, 4: raw data synapse 1, 5: DF synapse 1, 6: DF/F synapses1, ....)
	 * @return a 2D array, double precision, containing the data
	 */
	public double[][] extractData(){
		double[][] out=null; //Stores the data: 0: timepoint, 1: raw data dendrite, 2: DF dendrite, 3:DF/F dendrite, 4: raw data synapses, 5: DF synapses, 6:DF/F synapses
		
		if(new File(faf.imagePath_crop).exists()){
			ImagePlus ip=new CompositeImage(new ImagePlus(faf.imagePath_crop));
			
			
			//Gets the dendritic Roi 
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
			
			
			//Gets the synapses
			Roi[] roiSynapses=null;
			RoiManager rm=RoiManager.getInstance();
			if(rm==null){
				rm=new RoiManager();
			}else{
				rm.reset();
			}
			
			if(new File(faf.GCaMPUncagingPath_rois).exists()){
				rm.runCommand("Open", faf.GCaMPUncagingPath_rois);
				roiSynapses=rm.getRoisAsArray();
				rm.reset();
			}else{
				GUI.log.logInfo("Converted uncaging Rois' file not found: "+faf.GCaMPUncagingPath_rois);
			}
			
			
			//Retrieve timing
			double[] time=calculateTime(ip.getNSlices());
			
			Calibration cal=ip.getCalibration();
			out=new double[4+roiSynapses.length*3][ip.getNSlices()];
			double dt=ip.getCalibration().frameInterval;
			ip.setC(2);
			
			for(int i=0; i<ip.getNSlices(); i++){
				ip.setZ(i+1);
				
				ImageProcessor iproc=ip.getChannelProcessor();
				//Dendrite
				iproc.setRoi(roiDendrite);
				ImageStatistics isDendrite=ImageStatistics.getStatistics(iproc, ImageStatistics.MEAN+ImageStatistics.AREA, cal);
				out[0][i]=time!=null?time[i]:i*dt;
				out[1][i]=isDendrite.mean;
				out[2]=calculateDeltaF(out[1]);
				out[3]=calculateDeltaFOverF(out[1]);
				
				
				//Synapses
				for(int j=0; j<roiSynapses.length; j++){
					iproc.setRoi(roiSynapses[j]);
					ImageStatistics isSynapses=ImageStatistics.getStatistics(iproc, ImageStatistics.MEAN+ImageStatistics.AREA, cal);
					out[4+j*3][i]=isSynapses.mean;
				}
			}
			//Dendrite
			out[2]=calculateDeltaF(out[1]);
			out[3]=calculateDeltaFOverF(out[1]);
			
			//Synapses
			for(int i=0; i<roiSynapses.length; i++){
				out[5+i*3]=calculateDeltaF(out[4+i*3]);
				out[6+i*3]=calculateDeltaFOverF(out[4+i*3]);
			}
			
			ip.close();
			System.gc();
		}
		return out;
	}
	
	/**
	 * Calculates the time scale, based on input parameters provided at object's creation
	 * @param nTimePoints total number of timepoints
	 * @return the time scale, as a double array
	 */
	public double[] calculateTime(int nTimePoints){
		if(nTimePoints<nPreImages+nCycles*(1+nPostImages)){
			GUI.log.logInfo("Caution: the number of images on the stack is below the number of images expected in the cycle.");
			return null;
		}else{
			double[] out=new double[nTimePoints];
			
			int index=0;
			double currentTime=0;
			
			//Pre-sequence
			for(int i=0; i<nPreImages; i++){
				out[index++]=currentTime;
				currentTime+=preIntervalle;
			}
			
			//Post-sequence
			for(int i=0; i<nCycles; i++){
				//Perturbation
				out[index++]=currentTime;
				currentTime+=perturbationDuration;
				
				for(int j=0; j<nPostImages; j++){
					out[index++]=currentTime;
					currentTime+=postIntervalle;
				}
			}
			
			while(index<nTimePoints){
				out[index++]=currentTime;
				currentTime+=postIntervalle;
			}
			
			return out;
		}
	}
	
	/**
	 * Calculates the DeltaF: subtracts the min value of the n first timepoints
	 * @param data input data as a double array
	 * @return DeltaF as a double array
	 */
	public double[] calculateDeltaF(double[] data){
		int nImages=nPreImages+nCycles*(1+nPostImages);
		
		double[] out=data.clone();
		
		double[] tmp=Arrays.copyOfRange(data, 0, nImages-1);
		Arrays.sort(tmp);
		double min=tmp[0];
		
		for(int i=0; i<data.length; i++) out[i]=data[i]-min;
		
		return out;
	}
	
	
	/**
	 * Calculates the DeltaF/F: subtracts the min value of the n first timepoints, divides by it
	 * @param data input data as a double array
	 * @return DeltaF/F as a double array
	 */
	public double[] calculateDeltaFOverF(double[] data){
		int nImages=nPreImages+nCycles*(1+nPostImages);
		
		double[] out=data.clone();
		
		double[] tmp=Arrays.copyOfRange(data, 0, nImages-1);
		Arrays.sort(tmp);
		double min=tmp[0];
		
		for(int i=0; i<data.length; i++) out[i]=(data[i]-min)/min;
		
		return out;
	}
	
	/**
	 * Constructs an array of Strings containing the columns' headers
	 * @param nSynapses number of analysed synapses
	 * @return an array of Strings containing the columns' headers
	 */
	public String[] getHeaders(int nSynapses){
		String[] basicHeader=new String[]{"Time_sec", "Raw_intensity_dendrite", "DeltaF_dendrite", "DeltaF/F_dendrite"};
		String[] outHeader=new String[basicHeader.length+3*nSynapses];
		
		for(int i=0; i<basicHeader.length; i++) outHeader[i]=basicHeader[i];
		
		for(int i=0; i<nSynapses; i++){
			outHeader[i*3+basicHeader.length]="Raw_intensity_synapse_"+(i+1);
			outHeader[i*3+basicHeader.length+1]="DeltaF_synapse_"+(i+1);
			outHeader[i*3+basicHeader.length+2]="DeltaF/F_synapse_"+(i+1);
		}
		
		return outHeader;
	}
	
	/**
	 * Saves the GCaMPData Uncaging to a XLS file, in the appropriate normalised output folder
	 */
	public void exportGCamPUncagingData(){
		if(!new File(faf.GCaMPUncagingPath).exists()) new File(faf.GCaMPUncagingPath).mkdirs();
		
		//Converts data to ArrayList<String[]>
		double[][] data=extractData();
		
		//Converts header to ArrayList<String>
		ArrayList<String> headerList=new ArrayList<String>();
		String[] header=getHeaders((data.length-4)/3);
		for(int i=0; i<header.length; i++) headerList.add(""+header[i]);
				
		ArrayList<String[]> dataList=new ArrayList<String[]>();
		
		for(int i=0; i<data[0].length; i++){
			double[] line=new double[header.length];
			for(int j=0; j<line.length; j++) line[j]=data[j][i];
			String[] lineString=new String[line.length];
			for(int j=0; j<line.length; j++) lineString[j]=""+line[j];
			dataList.add(lineString);
		}
		
		new dataIO(headerList, dataList).writeData(faf.GCaMPUncagingPath_data);
	}
	
	
	/**
	 * Correlates GCaMP Uncaging data to instant speed data and writes the corresponding file
	 * @param minSpeed minimum speed to consider for pausing
	 */
	public void correlateToSpeed(double minSpeed){
		if(new File(faf.GCaMPUncagingPath).exists() && new File(faf.GCaMPUncagingPath_data).exists() && new File(faf.kymoPath_coord).exists()){
			//Load the files to correlate
			KTSdataFileImporter GCaMPUncaging=new KTSdataFileImporter(faf.GCaMPUncagingPath, faf.name+"_GCaMP_Uncaging_data.xls");
			KTSdataFileImporter coord=new KTSdataFileImporter(faf.kymoPath, faf.name+"_kymo_coord.xls");
			
			ArrayList<String[]> coordData=coord.getData();
			int nbVesicules=Integer.parseInt(coordData.get(coordData.size()-1)[1]);
			
			//Required to correct speeds
			ImagePlus ip=new CompositeImage(new ImagePlus(faf.imagePath_crop));
			double dt=ip.getCalibration().frameInterval;
			double[] time=calculateTime(ip.getNSlices());
			ip.close();
			
			//In the coord array, columns are as follows [2]: Time_(sec), [5]:Distance_(µm), [6]: Speed_(µm_per_sec)
			if(time!=null){
				for(int i=1; i<coordData.size(); i++){ //No need to modify first line
					String[] currLine=coordData.get(i);
					if(!currLine[6].equals("NaN")){
						int currTimePoint=(int) (Double.parseDouble(currLine[2])/dt);
						currLine[2]=""+time[currTimePoint];
						currLine[6]=""+(Double.parseDouble(currLine[5])/(time[currTimePoint]-time[currTimePoint-1])); //Divide distance by new delta t
						coordData.set(i, currLine);
					}
				}
			}
			
			//Prepares data containers
			ArrayList<String> header=GCaMPUncaging.getHeader();
			int headerLength=header.size();
			ArrayList<String[]> data=GCaMPUncaging.getData();
			
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
				if(time!=null){
					header.add("Instant_Speed_Time_Corrected_Vesicule_"+(i+1)+"_(microns_per_sec)");
				}else{
					header.add("Instant_Speed_Vesicule_"+(i+1)+"_(microns_per_sec)");
				}
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
					if(Math.abs(Double.parseDouble(data.get(j)[0])-Double.parseDouble(coordData.get(i)[2]))<0.001){
						data.get(j)[vesicule+headerLength+HEADER_DATA.length]=coordData.get(i)[6].equals("NaN")?"":coordData.get(i)[6];
						break;
					}
				}
			}
			
			//Calculates the nb of vesicles, the nb of pausing vesicles and the pct of pausing vesicles
			for(int i=0; i<data.size(); i++){
				int nbVesicles=0;
				int nbPausingVesicles=0;
				//Examines in turn each vesicle column
				for(int j=headerLength+HEADER_DATA.length; j<header.size(); j++){
					String column=data.get(i)[j];
					if(column!=""){
						nbVesicles++;
						if(Math.abs(Double.parseDouble(column))<=minSpeed) nbPausingVesicles++;
					}
					//Log data
					data.get(i)[headerLength]=nbVesicles==0?"":""+nbVesicles;
					data.get(i)[headerLength+1]=nbVesicles==0?"":""+nbPausingVesicles;
					data.get(i)[headerLength+2]=nbVesicles==0?"":""+(((double) nbPausingVesicles)/((double) nbVesicles));
				}
			}
			
			new dataIO(header, data).writeData(faf.GCaMPUncagingPath_data_correl_speed);
		}else{
			GUI.log.logInfo("Either the kymograph and/or GCaMP Uncaging analysis/are missing for "+faf.name);
		}
	}
	
	/**
	 * Extracts from the GCaMP Uncaging/speed correlated data only the speeds that are in the input direction (IN/PAUSE/OUT)
	 * @param direction input direction: use the static class field IN, PAUSE or OUT
	 * @param minSpeed the speed limit to consider for pausing
	 */
	public void separateDirections(int direction, double minSpeed){
		if(new File(faf.GCaMPUncagingPath).exists() && new File(faf.GCaMPUncagingPath_data_correl_speed).exists()){
			//The number of synapses might change: those lines are required to know where the vesicules' columns are
			KTSdataFileImporter GCaMPUncaging=new KTSdataFileImporter(faf.GCaMPUncagingPath, faf.name+"_GCaMP_Uncaging_data.xls");
			int GCaMPHeaderLength=GCaMPUncaging.getHeader().size();
			
			//Load the data to split
			KTSdataFileImporter GCaMPCorrel=new KTSdataFileImporter(faf.GCaMPUncagingPath_data_correl_speed);
			GCaMPCorrel.read();
			
			//Prepares data containers
			ArrayList<String> header=GCaMPCorrel.getHeader();
			ArrayList<String[]> data=GCaMPCorrel.getData();
			
			//For each line and each vesicle, erases any value of speed that is not in the input state
			for(int i=0; i<data.size(); i++){
				String[] currLine=data.get(i);
				
				//Vesicles' columns start at position HEADER.length+HEADER_DATA.length
				for(int j=GCaMPHeaderLength+HEADER_DATA.length; j<currLine.length; j++){
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
				case IN: outFilePath=faf.GCaMPUncagingPath_data_correl_speed_IN; break;
				case PAUSE: outFilePath=faf.GCaMPUncagingPath_data_correl_speed_PAUSE; break;
				case OUT: outFilePath=faf.GCaMPUncagingPath_data_correl_speed_OUT; break;
			}
			new dataIO(header, data).writeData(outFilePath);
		}else{
			GUI.log.logInfo("Either the GCaMP_Uncaging folder and/or the GCaMP Uncaging data correlation file is/are missing for "+faf.name);
		}
	}
}
