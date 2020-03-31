/**
  * filesAndFolders.java v1, 4 août 2016
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
package utilities;

import java.io.File;
import java.io.FilenameFilter;

import ij.io.OpenDialog;

/**
 * @author Fabrice P Cordelieres
 *
 */
public class filesAndFolders {
	/** Root path for the experiment **/
	public String rootPath=null;
	
	/** Base names **/
	public String[] basenames=null;
	
	/** Sub-folder names **/
	public String[] names=null;
	
	/** Path to the processed data for the input cell **/
	public String inPath=null;
	
	/** Name of the cell **/
	public String name=null;
	
	/** Path to the images **/
	public String imagePath=null;
	
	/** Path to the composite image **/
	public String imagePath_composite=null;
	
	/** Path to the overview image (jpg) **/
	public String imagePath_overview=null;
	
	/** Path to the cropped area image **/
	public String imagePath_crop=null;
	
	/** Path to the projected cropped area image **/
	public String imagePath_crop_proj=null;
	
	/** Path to the kymograph-related data **/
	public String kymoPath=null;
	
	/** Path to the ROI used to generate the kymograph **/
	public String kymoPath_ROI=null;
	
	/** Path to the kymograph **/
	public String kymoPath_kymo=null;
	
	/** Path to the ROIs representing the travelled segment for each vesicule on the kymograph **/
	public String kymoPath_segments=null;
	
	/** Path to csv file containing data extracted from each vesicule on the kymograph **/
	public String kymoPath_data=null;
	
	/** Path to csv file containing extrapolated coordinates from each vesicule on the kymograph **/
	public String kymoPath_coord=null;
	
	/** Path to the annotated kymograph **/
	public String kymoPath_annotated=null;
	
	/** Path to the GCaMP analysis **/
	public String GCaMPPath=null;
	
	/** Path to the GCaMP data **/
	public String GCaMPPath_data=null;
	
	/** Path to the speed/GCaMP correlated data **/
	public String GCaMPPath_data_correl_speed=null;
	
	/** Path to the speed/GCaMP correlated data, IN direction **/
	public String GCaMPPath_data_correl_speed_IN=null;
	
	/** Path to the speed/GCaMP correlated data, pause **/
	public String GCaMPPath_data_correl_speed_PAUSE=null;
	
	/** Path to the speed/GCaMP correlated data, OUT direction **/
	public String GCaMPPath_data_correl_speed_OUT=null;
	
	/** Path to the GCaMP Uncaging analysis **/
	public String GCaMPUncagingPath=null;
	
	/** Path to the GCaMP Uncaging rois **/
	public String GCaMPUncagingPath_rois=null;
	
	/** Path to the GCaMP Uncaging data **/
	public String GCaMPUncagingPath_data=null;
	
	/** Path to the speed/GCaMP Uncaging correlated data **/
	public String GCaMPUncagingPath_data_correl_speed=null;
	
	/** Path to the speed/GCaMP Uncaging correlated data, IN direction **/
	public String GCaMPUncagingPath_data_correl_speed_IN=null;
	
	/** Path to the speed/GCaMP Uncaging correlated data, pause **/
	public String GCaMPUncagingPath_data_correl_speed_PAUSE=null;
	
	/** Path to the speed/GCaMP Uncaging correlated data, OUT direction **/
	public String GCaMPUncagingPath_data_correl_speed_OUT=null;
	
	/** Path to the flux analysis **/
	public String fluxPath=null;
	
	/** Path to the flux detections rois **/
	public String fluxPath_ROI=null;
	
	/** Path to the flux detections image **/
	public String fluxPath_image=null;
	
	/** Path to the flux detections data **/
	public String fluxPath_data=null;
	
	/** Path to the pulled data **/
	public String pulledDataPath=null;

	/** Path to the pulled data: kymo coord **/
	public String pulledDataPath_kymoCoord=null;

	/** Path to the pulled data: kymo data **/
	public String pulledDataPath_kymoData=null;

	/** Path to the pulled data: flux data **/
	public String pulledDataPath_fluxData=null;

	/** Path to the pulled data: GCaMP data **/
	public String pulledDataPath_GCaMPData=null;
	
	/** Path to the pulled data: GCaMP correl data **/
	public String pulledDataPath_GCaMPDataCorrelSpeed=null;
	
	/** Path to the pulled data: GCaMP correl data, inward speeds **/
	public String pulledDataPath_GCaMPDataCorrelSpeed_IN=null;
	
	/** Path to the pulled data: GCaMP correl data, pauses **/
	public String pulledDataPath_GCaMPDataCorrelSpeed_PAUSE=null;
	
	/** Path to the pulled data: GCaMP correl data, outward speeds **/
	public String pulledDataPath_GCaMPDataCorrelSpeed_OUT=null;
	
	/** Path to the pulled data: GCaMP Uncaging data **/
	public String pulledDataPath_GCaMPUncagingData=null;
	
	/** Path to the pulled data: GCaMP Uncaging correl data **/
	public String pulledDataPath_GCaMPUncagingDataCorrelSpeed=null;
	
	/** Path to the pulled data: GCaMP Uncaging correl data, inward speeds **/
	public String pulledDataPath_GCaMPUncagingDataCorrelSpeed_IN=null;
	
	/** Path to the pulled data: GCaMP Uncaging correl data, pauses **/
	public String pulledDataPath_GCaMPUncagingDataCorrelSpeed_PAUSE=null;
	
	/** Path to the pulled data: GCaMP Uncaging correl data, outward speeds **/
	public String pulledDataPath_GCaMPUncagingDataCorrelSpeed_OUT=null;
	
	/** Path to the pulled data: Prism file **/
	public String pulledDataPath_prism=null;
	
	
	/**
	 * Asks the user to point at the folder to start the analysis
	 */
	public filesAndFolders(){
		OpenDialog od=new OpenDialog("Select the input folder");
		rootPath=od.getDirectory();
		getBasenames();
		getNames();
	}
	
	/**
	 * Sets the different paths for files to be used
	 * @param rootPath the root path
	 * @param name the cell name
	 */
	public filesAndFolders(String rootPath, String name){
		this.rootPath=rootPath.endsWith(File.separator)?rootPath:rootPath+File.separator;
		getBasenames();
		getNames();
		inPath=this.rootPath+name+File.separator;
		this.name=name;
		buildPaths();
	}
	
	/**
	 * Sets the different paths for files to be used
	 * @param rootPath the root path
	 */
	public filesAndFolders(String rootPath){
		this.rootPath=rootPath.endsWith(File.separator)?rootPath:rootPath+File.separator;
		getBasenames();
		getNames();
	}
	
	/**
	 * Sets the name to be used to build the different paths for files
	 * @param name the cell name
	 */
	public void setName(String name){
		this.name=name;
		inPath=rootPath+name+File.separator;
		buildPaths();
	}
	
	/**
	 * Sets the root to be used to build the different paths for files
	 * @param rootPath the root path
	 */
	public void setPath(String rootPath){
		this.rootPath=rootPath.endsWith(File.separator)?rootPath:rootPath+File.separator;
		getBasenames();
		inPath=rootPath+name+File.separator;
		buildPaths();
	}
	
	/**
	 * Builds the list of basenames within the rootPath, based on rgn files 
	 */
	public void getBasenames(){
		if(rootPath!=null && rootPath!=""){
			String[] allRois=getSpecificFileList(".rgn");
			String[] unCagingRois=getSpecificFileList("_uncaging.rgn");
			
			int nUncagingRois=unCagingRois==null?0:unCagingRois.length;
			int nRois=allRois==null?0:allRois.length;
					
			basenames=new String[nRois-nUncagingRois];
			int index=0;
			
			for(int i=0; i<nRois; i++){
				if(allRois[i].indexOf("-roi")!=-1) allRois[i]=allRois[i].replaceAll("-roi.rgn", "");
				//if(basenames[i].indexOf("_cell")!=-1) basenames[i]=basenames[i].replaceAll(".rgn", ".nd");
				if(!allRois[i].endsWith("_uncaging.rgn")) basenames[index++]=allRois[i].replaceAll(".rgn", ".nd");;
			}
		}
	}
	
	/**
	 * Builds the list of cell folders' names within the rootPath
	 */
	public void getNames(){
		if(rootPath!=null && rootPath!=""){
			FilenameFilter ff=new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return new File(dir.getPath()+File.separator+name).isDirectory() && name.startsWith("Cell");
				}
			};
			names=new File(rootPath).list(ff);
		}
	}
	
	/**
	 * Build all paths based on the rootpath and dataset's name
	 */
	public void buildPaths(){
		imagePath=inPath+"Images"+File.separator;
		imagePath_composite=imagePath+name+".zip";
		imagePath_overview=imagePath+name+".jpg";
		imagePath_crop=imagePath+name+"_crop.zip";
		imagePath_crop_proj=imagePath+name+"_crop_proj.zip";
		
		kymoPath=inPath+"Kymographs"+File.separator;
		kymoPath_ROI=kymoPath+name+"_kymo_path.roi";
		kymoPath_kymo=kymoPath+name+"_kymo.zip";
		kymoPath_annotated=kymoPath+name+"_kymo_annotated.zip";
		kymoPath_segments=kymoPath+name+"_kymo_segments_rois.zip";
		kymoPath_data=kymoPath+name+"_kymo_data.xls";
		kymoPath_coord=kymoPath+name+"_kymo_coord.xls";
		
		GCaMPPath=inPath+"GCaMP"+File.separator;
		GCaMPPath_data=GCaMPPath+name+"_GCaMP_data.xls";
		GCaMPPath_data_correl_speed=GCaMPPath+name+"_GCaMP_data_correl_speed.xls";
		GCaMPPath_data_correl_speed_IN=GCaMPPath+name+"_GCaMP_data_correl_speed_IN.xls";
		GCaMPPath_data_correl_speed_PAUSE=GCaMPPath+name+"_GCaMP_data_correl_speed_PAUSE.xls";
		GCaMPPath_data_correl_speed_OUT=GCaMPPath+name+"_GCaMP_data_correl_speed_OUT.xls";
		
		GCaMPUncagingPath=inPath+"GCaMP_Uncaging"+File.separator;
		GCaMPUncagingPath_data=GCaMPUncagingPath+name+"_GCaMP_Uncaging_data.xls";
		GCaMPUncagingPath_rois=GCaMPUncagingPath+name+"_Uncaging_rois.zip";
		GCaMPUncagingPath_data_correl_speed=GCaMPUncagingPath+name+"_GCaMP_Uncaging_data_correl_speed.xls";
		GCaMPUncagingPath_data_correl_speed_IN=GCaMPUncagingPath+name+"_GCaMP_Uncaging_data_correl_speed_IN.xls";
		GCaMPUncagingPath_data_correl_speed_PAUSE=GCaMPUncagingPath+name+"_GCaMP_Uncaging_data_correl_speed_PAUSE.xls";
		GCaMPUncagingPath_data_correl_speed_OUT=GCaMPUncagingPath+name+"_GCaMP_Uncaging_data_correl_speed_OUT.xls";
		
		fluxPath=inPath+"Flux"+File.separator;
		fluxPath_ROI=fluxPath+name+"_flux_roi.zip";
		fluxPath_image=fluxPath+name+"_flux_image.zip";
		fluxPath_data=fluxPath+name+"_flux_data.xls";
		
		pulledDataPath=rootPath+"_Pulled_data"+File.separator;
		pulledDataPath_kymoCoord=pulledDataPath+"Kymo_coord.xls";
		pulledDataPath_kymoData=pulledDataPath+"Kymo_data.xls";
		pulledDataPath_fluxData=pulledDataPath+"Flux_data.xls";
		pulledDataPath_GCaMPData=pulledDataPath+"GCaMP_data.xls";
		pulledDataPath_GCaMPDataCorrelSpeed=pulledDataPath+"GCaMP_data_correl_speed.xls";
		pulledDataPath_GCaMPDataCorrelSpeed_IN=pulledDataPath+"GCaMP_data_correl_speed_IN.xls";
		pulledDataPath_GCaMPDataCorrelSpeed_PAUSE=pulledDataPath+"GCaMP_data_correl_speed_PAUSE.xls";
		pulledDataPath_GCaMPDataCorrelSpeed_OUT=pulledDataPath+"GCaMP_data_correl_speed_OUT.xls";
		
		pulledDataPath_GCaMPUncagingData=pulledDataPath+"GCaMP_Uncaging_data.xls";
		pulledDataPath_GCaMPUncagingDataCorrelSpeed=pulledDataPath+"GCaMP_Uncaging_data_correl_speed.xls";
		pulledDataPath_GCaMPUncagingDataCorrelSpeed_IN=pulledDataPath+"GCaMP_Uncaging_data_correl_speed_IN.xls";
		pulledDataPath_GCaMPUncagingDataCorrelSpeed_PAUSE=pulledDataPath+"GCaMP_Uncaging_data_correl_speed_PAUSE.xls";
		pulledDataPath_GCaMPUncagingDataCorrelSpeed_OUT=pulledDataPath+"GCaMP_Uncaging_data_correl_speed_OUT.xls";
		pulledDataPath_prism=pulledDataPath+"All_in_one.pzfx";
	}
	
	/**
	 * Generates a filtered list of files present within the root path, ending by the provided extension
	 * @param extension the extension to look for (could be both lower or upper case)
	 * @return the filtered file list, as a String array
	 */
	public String[] getSpecificFileList(String extension){
		if(rootPath!=null){
			FilenameFilter ff=new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(extension) && !name.toLowerCase().startsWith("."); //Second part is to not take into account system thumbnails files
				}
			};
			return new File(rootPath).list(ff);
		}else{
			return null;
		}
	}
	
	@Override
	public String toString(){
		String out=	"rootPath: "+rootPath+"\n"
					+"basenames: ";
		
		for(int i=0; i<basenames.length; i++) out+= basenames[i]+"\t";
		out+="\nnames: ";
		for(int i=0; i<names.length; i++) out+= names[i]+"\t";
				
		out+= 	"\ninPath: "+inPath+"\n"
				+"name: "+name+"\n"
				+"imagePath: "+imagePath+"\n"
				+"imagePath_composite: "+imagePath_composite+"\n"
				+"imagePath_overview: "+imagePath_overview+"\n"
				+"imagePath_crop: "+imagePath_crop+"\n"
				+"imagePath_crop_proj: "+imagePath_crop_proj+"\n"
				+"kymoPath: "+kymoPath+"\n"
				+"kymoPath_ROI: "+kymoPath_ROI+"\n"
				+"kymoPath_kymo: "+kymoPath_kymo+"\n"
				+"kymoPath_annotated: "+kymoPath_annotated+"\n"
				+"kymoPath_segments: "+kymoPath_segments+"\n"
				+"kymoPath_data: "+kymoPath_data+"\n"
				+"kymoPath_coord: "+kymoPath_coord+"\n"
				+"GCaMPPath: "+GCaMPPath+"\n"
				+"GCaMPPath_data: "+GCaMPPath_data+"\n"
				+"GCaMPPath_data_correl_speed: "+GCaMPPath_data_correl_speed+"\n"
				+"GCaMPUncagingPath: "+GCaMPUncagingPath+"\n"
				+"GCaMPUncagingPath_data: "+GCaMPUncagingPath_data+"\n"
				+"GCaMPUncagingPath_rois: "+GCaMPUncagingPath_rois+"\n"
				+"GCaMPUncagingPath_data_correl_speed: "+GCaMPUncagingPath_data_correl_speed+"\n"
				+"fluxPath: "+fluxPath+"\n"
				+"fluxPath_ROI: "+fluxPath_ROI+"\n"
				+"fluxPath_image: "+fluxPath_image+"\n"
				+"fluxPath_data: "+fluxPath_data+"\n"
				+"pulledDataPath: "+pulledDataPath+"\n"
				+"pulledDataPath_kymoCoord: "+pulledDataPath_kymoCoord+"\n"
				+"pulledDataPath_kymoData: "+pulledDataPath_kymoData+"\n"
				+"pulledDataPath_fluxData: "+pulledDataPath_fluxData+"\n"
				+"pulledDataPath_GCaMPData: "+pulledDataPath_GCaMPData+"\n"
				+"pulledDataPath_GCaMPDataCorrelSpeed: "+pulledDataPath_GCaMPDataCorrelSpeed+"\n"
				+"pulledDataPath_GCaMPDataCorrelSpeed_IN: "+pulledDataPath_GCaMPDataCorrelSpeed_IN+"\n"
				+"pulledDataPath_GCaMPDataCorrelSpeed_PAUSE: "+pulledDataPath_GCaMPDataCorrelSpeed_PAUSE+"\n"
				+"pulledDataPath_GCaMPDataCorrelSpeed_OUT: "+pulledDataPath_GCaMPDataCorrelSpeed_OUT+"\n"
				+"pulledDataPath_GCaMPUncagingData: "+pulledDataPath_GCaMPUncagingData+"\n"
				+"pulledDataPath_GCaMPUncagingDataCorrelSpeed: "+pulledDataPath_GCaMPUncagingDataCorrelSpeed+"\n"
				+"pulledDataPath_GCaMPUncagingDataCorrelSpeed_IN: "+pulledDataPath_GCaMPUncagingDataCorrelSpeed_IN+"\n"
				+"pulledDataPath_GCaMPUncagingDataCorrelSpeed_PAUSE: "+pulledDataPath_GCaMPUncagingDataCorrelSpeed_PAUSE+"\n"
				+"pulledDataPath_GCaMPUncagingDataCorrelSpeed_OUT: "+pulledDataPath_GCaMPUncagingDataCorrelSpeed_OUT+"\n"
				+"pulledDataPath_prism: "+pulledDataPath_prism;
		
		return out;
	}
}
