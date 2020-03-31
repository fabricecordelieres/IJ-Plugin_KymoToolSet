/**
  * KTS_DataFileImporter.java v1, 21 oct. 2016
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import analysis.GUI;
import ij.plugin.frame.RoiManager;
import utilities.filesAndFolders;

/**
 * This class aims at importing, parsing and manipulation numerical data
 * generated using the KymoToolSet plugin
 * 
 * @author Fabrice P Cordelieres
 *
 */
public class KTSdataFileImporter extends dataIO {
	/** Core data **/
	ArrayList<String[]> data = null;

	/** Data type **/
	int dataType = -1;

	// Headers
	/** Kymo coord header **/
	static final String[] KYMO_COORD_HEADER = new String[] { "Label", "Kymo_nb", "Time_(sec)", "x", "y",
			"Distance_(µm)", "Speed_(µm_per_sec)" };

	/** Kymo data header **/
	static final String[] KYMO_DATA_HEADER = new String[] { "Label", "Kymo_nb", "Ttl_Time_(sec)", "Cum_Dist_(µm)",
			"Mean_Speed_(µm_per_sec)", "Mean_Speed_In_(µm_per_sec)", "Mean_Speed_Out_(µm_per_sec)", "Cum_Dist_In_(µm)",
			"Cum_Dist_Out_(µm)", "Min_Dist_Start-End_(µm)", "Persistence", "Freq_In>Out_(sec-1)",
			"Freq_In>Pause_(sec-1)", "Freq_Out>In_(sec-1)", "Freq_Out>Pause_(sec-1)", "Freq_Pause>In_(sec-1)",
			"Freq_Pause>Out_(sec-1)", "%_Time_In_", "%_Time_Out_", "%_Time_Pause_" };

	/** GCaMP data header **/
	static final String[] GCAMP_DATA_HEADER = new String[] { "Time_sec", "Raw_intensity", "DeltaF", "DeltaF/F" };

	/** GCaMP correl data header **/
	static final String[] GCAMP_DATA_CORREL_SPEED_HEADER = new String[] { "Time_sec", "Raw_intensity_dendrite",
			"DeltaF_dendrite", "DeltaF/F_dendrite", "Raw_intensity_synapses", "DeltaF_synapses", "DeltaF/F_synapses",
			"Nb_vesicles", "Nb_pausing_vesicles", "Pct_pausing_vesicles" };

	/** GCaMP uncaging data header **/
	static final String[] GCAMP_UNCAGING_DATA_HEADER = new String[] { "Time_sec", "Raw_intensity_dendrite",
			"DeltaF_dendrite", "DeltaF/F_dendrite" };

	/** Flux data header **/
	static final String[] FLUX_DATA_HEADER = new String[] { "Time_sec", "Nb_detections", "Delta_nb_detections",
			"Area_microns2" };

	// Types
	/** Data type: kymo coord **/
	public static final int KYMO_COORD = 0;

	/** Data type: kymo data **/
	public static final int KYMO_DATA = 1;

	/** Data type: GCaMP data **/
	public static final int GCAMP_DATA = 2;

	/** Data type: GCaMP correlated data **/
	public static final int GCAMP_DATA_CORREL_SPEED = 3;

	/** Data type: GCaMP correlated data speed inward **/
	public static final int GCAMP_DATA_CORREL_SPEED_IN = 4;

	/** Data type: GCaMP correlated data speed outward **/
	public static final int GCAMP_DATA_CORREL_SPEED_PAUSE = 5;

	/** Data type: GCaMP correlated data speed outward **/
	public static final int GCAMP_DATA_CORREL_SPEED_OUT = 6;

	/** Data type: GCaMP uncaging data **/
	public static final int GCAMP_UNCAGING_DATA = 7;

	/** Data type: GCaMP uncaging correlated data **/
	public static final int GCAMP_UNCAGING_DATA_CORREL_SPEED = 8;

	/** Data type: GCaMP uncaging correlated data speed inward **/
	public static final int GCAMP_UNCAGING_DATA_CORREL_SPEED_IN = 9;

	/** Data type: GCaMP uncaging correlated data speed outward **/
	public static final int GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE = 10;

	/** Data type: GCaMP uncaging correlated data speed outward **/
	public static final int GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT = 11;

	/** Data type: flux data **/
	public static final int FLUX_DATA = 12;

	/**
	 * Builds the new empty KTS data importer
	 */
	public KTSdataFileImporter() {
	}
	
	/**
	 * Builds the new KTS data importer, based on a root path and filename
	 * 
	 * @param path
	 *            the root path
	 * @param filename
	 *            the cell name
	 */
	public KTSdataFileImporter(String path, String filename) {
		this.path = path;
		this.filename = filename;
		read();
	}

	/**
	 * Builds the new KTS data importer, based on a root path
	 * 
	 * @param path
	 *            the root path
	 */
	public KTSdataFileImporter(String path) {
		this.path = path;
		this.filename = "";
	}

	/**
	 * Return an integer representing the data type
	 * 
	 * @return an integer representing the data type
	 */
	public int getType() {
		int type = -1;

		if (Arrays.equals(getHeaderAsStringArray(), KYMO_COORD_HEADER))
			type = KYMO_COORD;
		if (Arrays.equals(getHeaderAsStringArray(), KYMO_DATA_HEADER))
			type = KYMO_DATA;
		if (Arrays.equals(getHeaderAsStringArray(), GCAMP_DATA_HEADER))
			type = GCAMP_DATA;
		if (Arrays.equals(Arrays.copyOf(getHeaderAsStringArray(), GCAMP_DATA_HEADER.length), GCAMP_DATA_HEADER))
			type = GCAMP_DATA_CORREL_SPEED;
		if (Arrays.equals(getHeaderAsStringArray(), FLUX_DATA_HEADER))
			type = FLUX_DATA;

		return type;
	}
	
	/**
	 * Reads the data and defines the data type
	 */
	public void read() {
		readData();
		dataType = getType();
	}

	/**
	 * Merges all files of a certain type into a unique file of same type, adding a column where the cell number is mentionned
	 * @param rootPath the root path where all subfolders and files should be stored
	 * @param dataType the type of data to merge
	 */
	public void merge(String rootPath, int dataType){
		filesAndFolders faf=new filesAndFolders(rootPath);
		
		String pathToFile=null;
		String label=null;
		dataIO di=null;
		int nColumns=0;
		int nFiles=0;
		
		int maxNbSynapses=0;
		int maxNbVesicles=0;
		boolean isTimeCorrected=true;
		
		if(faf.names!=null){
			String[] names=faf.names;
			
			//For uncaging data, retrieve the number of synapses
			int[] nSynapses=new int[names.length];
			
			for(int i=0; i<names.length; i++){
				faf.setName(names[i]);
				pathToFile=faf.GCaMPUncagingPath_rois;
				if(new File(pathToFile).exists()){
					RoiManager rm=RoiManager.getInstance();
					if(rm==null){
						rm=new RoiManager();
					}else{
						rm.reset();
					}
					rm.runCommand("Open", faf.GCaMPUncagingPath_rois);
					nSynapses[i]=rm.getRoisAsArray().length;
					rm.reset();
				}
			}
			int[] tmp=nSynapses.clone();
			Arrays.sort(tmp);
			maxNbSynapses=tmp[tmp.length-1];
			
			//Merge data
			for(int i=0; i<names.length; i++){
				faf.setName(names[i]);
				
				switch(dataType){
					case KYMO_COORD: pathToFile=faf.kymoPath_coord; break; 
					case KYMO_DATA: pathToFile=faf.kymoPath_data; break; 
					case GCAMP_DATA: pathToFile=faf.GCaMPPath_data; label="label"; break; 
					case GCAMP_DATA_CORREL_SPEED: pathToFile=faf.GCaMPPath_data_correl_speed; label="label"; break; 
					case GCAMP_DATA_CORREL_SPEED_IN: pathToFile=faf.GCaMPPath_data_correl_speed_IN; label="label"; break; 
					case GCAMP_DATA_CORREL_SPEED_OUT: pathToFile=faf.GCaMPPath_data_correl_speed_OUT; label="label"; break; 
					case GCAMP_DATA_CORREL_SPEED_PAUSE: pathToFile=faf.GCaMPPath_data_correl_speed_PAUSE; label="label"; break; 
					case GCAMP_UNCAGING_DATA: pathToFile=faf.GCaMPUncagingPath_data; label="label"; break; 
					case GCAMP_UNCAGING_DATA_CORREL_SPEED: pathToFile=faf.GCaMPUncagingPath_data_correl_speed; label="label"; break; 
					case GCAMP_UNCAGING_DATA_CORREL_SPEED_IN: pathToFile=faf.GCaMPUncagingPath_data_correl_speed_IN; label="label"; break; 
					case GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT: pathToFile=faf.GCaMPUncagingPath_data_correl_speed_OUT; label="label"; break; 
					case GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE: pathToFile=faf.GCaMPUncagingPath_data_correl_speed_PAUSE; label="label"; break; 
					case FLUX_DATA: pathToFile=faf.fluxPath_data; label="label"; break;
					default: GUI.log.logInfo("KTSdataFileImporter: Data type not defined");
				}
				
				if(new File(pathToFile).exists()){
					if(nFiles==0){
						di=new dataIO(pathToFile);
						nColumns=di.header.size();
						
						isTimeCorrected=isTimeCorrected && di.getHeaderAsString().contains("Time_Corrected_");
						
						//Shifts columns after synapses, if needed
						if(dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_IN
								|| dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE) for (int j=0; j<(maxNbSynapses-nSynapses[i])*3; j++) di.addEmptyColumn(4+nSynapses[i]*3);
						
						if(label!=null){
							di.header.add(0, label);
							di.data=addCellNumber(di.data, faf.name);
						}						
					}else{
						dataIO currData=new dataIO(pathToFile);
						nColumns=Math.max(nColumns, currData.header.size());
						
						isTimeCorrected=isTimeCorrected && currData.getHeaderAsString().contains("Time_Corrected_");
						
						//Shifts columns after synapses, if needed
						if(dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_IN
								|| dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE) for (int j=0; j<(maxNbSynapses-nSynapses[i])*3; j++) currData.addEmptyColumn(4+nSynapses[i]*3);
						
						if(label!=null) currData.data=addCellNumber(currData.data, faf.name);
						
						for(int j=0; j<currData.data.size(); j++) di.data.add(currData.data.get(j));
					}
					nFiles++;
				}else{
					GUI.log.logInfo("Could not find file "+pathToFile);
				}
			}
		}
		if(nFiles>0){
			//Adapts the header to the max number of vesicles
			maxNbVesicles=nColumns-GCAMP_DATA_CORREL_SPEED_HEADER.length;
			int nbVesicHeader=di.header.size()-GCAMP_DATA_CORREL_SPEED_HEADER.length-1; //-1 is because of the label
			
			switch(dataType){
				case KYMO_COORD: pathToFile=faf.pulledDataPath_kymoCoord; break; 
				case KYMO_DATA: pathToFile=faf.pulledDataPath_kymoData; break; 
				case GCAMP_DATA: pathToFile=faf.pulledDataPath_GCaMPData; break;
				case GCAMP_DATA_CORREL_SPEED:
					pathToFile=faf.pulledDataPath_GCaMPDataCorrelSpeed; break;
				case GCAMP_DATA_CORREL_SPEED_IN: pathToFile=faf.pulledDataPath_GCaMPDataCorrelSpeed_IN; break;
				case GCAMP_DATA_CORREL_SPEED_PAUSE: pathToFile=faf.pulledDataPath_GCaMPDataCorrelSpeed_PAUSE; break;
				case GCAMP_DATA_CORREL_SPEED_OUT: pathToFile=faf.pulledDataPath_GCaMPDataCorrelSpeed_OUT; break;
				case GCAMP_UNCAGING_DATA:
					pathToFile=faf.pulledDataPath_GCaMPUncagingData; break;
				case GCAMP_UNCAGING_DATA_CORREL_SPEED:
					pathToFile=faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed; break;
				case GCAMP_UNCAGING_DATA_CORREL_SPEED_IN: pathToFile=faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed_IN; break;
				case GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE: pathToFile=faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed_PAUSE; break;
				case GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT: pathToFile=faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed_OUT; break;
				case FLUX_DATA: pathToFile=faf.pulledDataPath_fluxData; break;
				default: GUI.log.logInfo("KTSdataFileImporter: Data type not defined");
			}
			
			
			if(dataType==GCAMP_DATA_CORREL_SPEED || dataType==GCAMP_DATA_CORREL_SPEED_IN || dataType==GCAMP_DATA_CORREL_SPEED_PAUSE || dataType==GCAMP_DATA_CORREL_SPEED_OUT) for(int i=nbVesicHeader; i<maxNbVesicles; i++) di.header.add("Instant_Speed_Vesicule_"+(i+1)+"_(microns_per_sec)");
			
			if(dataType==GCAMP_UNCAGING_DATA|| dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_IN || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE || dataType==GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT)
				di.header=getUncagingHeader(maxNbSynapses, nColumns-8-maxNbSynapses*3+1, dataType, isTimeCorrected);
			
			di.writeData(pathToFile);
		}
	}

	/**
	 * Adds a first column to existing data
	 * 
	 * @param data
	 *            original data, as an ArrayList<String[]>
	 * @param firstColumn
	 *            a string to tag the original data
	 * @return and ArrayList<String[]> containing an additional first column
	 */
	private ArrayList<String[]> addCellNumber(ArrayList<String[]> data, String firstColumn) {
		ArrayList<String[]> out = new ArrayList<String[]>();

		for (int i = 0; i < data.size(); i++) {
			String[] line = data.get(i);
			String[] tmp = new String[line.length + 1];

			tmp[0] = firstColumn;

			for (int j = 0; j < line.length; j++)
				tmp[j + 1] = line[j];

			out.add(tmp);
		}

		return out;
	}

	/**
	 * Prepares and return the new header for pulled GCaMP uncaging experiments
	 * @param nSynapses max number of synapses once data have been pulled
	 * @param nVesicles max number of vesicles once data have been pulled
	 * @param dataType datatype
	 * @param isTimeCorrected true if the time has been corrected
	 * @return the generated header, as an ArrayLsit of String
	 */
	private ArrayList<String> getUncagingHeader(int nSynapses, int nVesicles, int dataType, boolean isTimeCorrected) {
		ArrayList<String> out = new ArrayList<String>();
		
		//First columns
		out.add("label"); out.add("Time_sec"); out.add("Raw_intensity_dendrite"); out.add("DeltaF_dendrite"); out.add("DeltaF/F_dendrite");
		
		//Synapses columns
		for(int i=0; i<nSynapses; i++){
			out.add("Raw_intensity_synapse_"+(i+1)); out.add("DeltaF_synapse_"+(i+1)); out.add("DeltaF/F_synapse_"+(i+1));
		}
		
		//Summary columns and vesicles columns
		if(nVesicles>0 && dataType!=GCAMP_UNCAGING_DATA){
			out.add("Nb_vesicles");	out.add("Nb_pausing_vesicles");	out.add("Pct_pausing_vesicles");
			for(int i=0; i<nVesicles; i++) out.add("Instant_Speed_"+(isTimeCorrected?"Time_Corrected_":"")+"Vesicule_"+(i+1)+"_(microns_per_sec)");
		}
				
		return out;
	}
}
