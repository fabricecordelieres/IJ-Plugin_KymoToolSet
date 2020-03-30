package settings;

import ij.ImagePlus;
import ij.WindowManager;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import ij.plugin.Duplicator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This class includes all methods to handle input/output
 * @author fab
 *
 */
public class filesAndFolders{
	/**
	 * Sets the working directory to a user defined folder and builds the global ouput folders' names
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean setWorkingDirectory(){
		settings.dir=new DirectoryChooser("Where are the cells ?").getDirectory();
		
		if(settings.dir!="" && settings.dir!=null){
			settings.files=new File(settings.dir).list();
			
			//Required for windows 7
			String pattern=Pattern.quote(System.getProperty("file.separator"));
			String[] tmp=settings.dir.split(pattern);
			
			settings.expName=tmp[tmp.length-1];
			
			//In case at least a single nd file is present in the input folder, the experiment might be a GCAmp one
			String[] ndFiles=getFilteredNames(settings.dir, "", ".nd");
			if(ndFiles.length!=0){
				settings.isGCampExperiment=true;
				settings.expName=ndFiles[0].substring(0, ndFiles[0].lastIndexOf("_"));
				settings.GCampExperimentName=settings.expName;
				settings.oriGCamp=settings.dir;
				settings.dir=settings.dir+settings.expName+File.separator;
			}
			
			settings.destGlobalData=settings.dir+"_Data"+File.separator;
			new File(settings.destGlobalData).mkdirs();
			
			settings.allCellsData=settings.destGlobalData+settings.expName+"_all-data.xls";
			settings.allCellsFlux=settings.destGlobalData+settings.expName+"_all-flux.xls";
			settings.allCellsSynapses=settings.destGlobalData+settings.expName+"_all-synapses.xls";
			settings.allCellsDataSynapses=settings.destGlobalData+settings.expName+"_all-data-synapses.xls";
			settings.allCellsSummary=settings.destGlobalData+settings.expName+"_summary.xls";
			settings.allCellsSummaryWS=settings.destGlobalData+settings.expName+"_white-spaces_summary.xls";
			
			return true;
		}else{
			settings.dir="";
			return false;
		}
	}
	
	
	/**
	 * Sets the folders for processing the cell which number's is provided as input
	 * @param cell the cell number as a String
	 */
	public static void setFolders(String cell){
		settings.currCell="Cell"+cell;
		settings.destCell=settings.dir+"Cell"+cell+File.separator;
		settings.destStkProc=settings.destCell+"Processed_stacks"+File.separator;
		settings.destProj=settings.destCell+"Proj"+File.separator;
		settings.destKymos=settings.destCell+"Kymos"+File.separator;
		settings.destKymosPaths=settings.destKymos+"Paths"+File.separator;
		settings.destKymosPathsFullImg=settings.destKymos+"Paths_Full_Img"+File.separator;
		settings.destKymosKymos=settings.destKymos+"Kymos"+File.separator;
		settings.destKymosROIs=settings.destKymos+"ROIs"+File.separator;
		settings.destKymosData=settings.destKymos+"Data"+File.separator;
		settings.destKymosDataImg=settings.destKymosData+"Images"+File.separator;
		settings.destKymosDataXLS=settings.destKymosData+"XLS"+File.separator;
		settings.destSynapses=settings.destCell+"Synapses"+File.separator;
		settings.destFlux=settings.destCell+"Flux"+File.separator;
		settings.destForPPT=settings.destCell+"For_PPT"+File.separator;
		if(settings.isGCampExperiment){
			settings.destGCamp=settings.destCell+"GCamp"+File.separator;
			settings.destGCampROI=settings.destGCamp+"ROIs"+File.separator;
			settings.destGCampFlux=settings.destGCamp+"Flux"+File.separator;
			settings.destGCampData=settings.destGCamp+"Data"+File.separator;
			settings.destGCampDataImg=settings.destGCampData+"Images"+File.separator;
			settings.destGCampDataXLS=settings.destGCampData+"XLS"+File.separator;
			settings.destGCampDataSynapses=settings.destGCampData+"Synapses"+File.separator;
		}
	}
	
	
	/**
	 * Creates the folders for processing the cell which number's is provided as input
	 * @param cell the cell number as a String
	 */
	public static void createFolders(String cell){
		setFolders(cell);
		new File(settings.destCell).mkdirs();
		new File(settings.destStkProc).mkdirs();
		new File(settings.destProj).mkdirs();
		new File(settings.destKymos).mkdirs();
		new File(settings.destKymosPaths).mkdirs();
		new File(settings.destKymosPathsFullImg).mkdirs();
		new File(settings.destKymosKymos).mkdirs();
		new File(settings.destKymosROIs).mkdirs();
		new File(settings.destKymosData).mkdirs();
		new File(settings.destKymosDataImg).mkdirs();
		new File(settings.destKymosDataXLS).mkdirs();
		new File(settings.destSynapses).mkdirs();
		new File(settings.destFlux).mkdirs();
		new File(settings.destForPPT).mkdirs();
		if(settings.isGCampExperiment){
			new File(settings.destGCamp).mkdirs();
			new File(settings.destGCampROI).mkdirs();
			new File(settings.destGCampFlux).mkdirs();
			new File(settings.destGCampData).mkdirs();
			new File(settings.destGCampDataImg).mkdirs();
			new File(settings.destGCampDataXLS).mkdirs();
			new File(settings.destGCampDataSynapses).mkdirs();
		}
	}
	
	/**
	 * Builds the cells' list, containing the number (as String) for each cell within the folder
	 */
	public static void buildCellsList(){
		ArrayList<String> tmp=new ArrayList<String>();
		
		if(!settings.isGCampExperiment){
			//Handles regular experiments
			for(int i=0; i<settings.files.length; i=i+2){
				if(settings.files[i].indexOf("Cell")!=-1 && settings.files[i].indexOf(File.separator)==-1 && settings.files[i].toLowerCase().endsWith(".tif")) tmp.add(settings.files[i].substring(4, settings.files[i].lastIndexOf("-")));
			}
		}else{
			settings.files=getFilteredNames(settings.oriGCamp, "", ".nd");
			for(int i=0; i<settings.files.length; i++){
				tmp.add(settings.files[i].substring(settings.files[i].lastIndexOf("_Cell")+5, settings.files[i].lastIndexOf(".nd")));
			}
		}
		
		//Used to sort the cells number numerically while keeping the variable as a String array
		int[] tmp2=new int[tmp.size()];//tmp.toArray(new String[tmp.size()]);
		for(int i=0; i<tmp.size(); i++) tmp2[i]=Integer.parseInt(tmp.get(i));
		Arrays.sort(tmp2);
		settings.cellsList=new String[tmp2.length];
		for(int i=0; i<tmp2.length; i++) settings.cellsList[i]=""+tmp2[i];
	}
	
	/**
	 * Asks the user for the working directory, builds the list of cells to analyze,
	 * the creates the output folders
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean selectAndCreateFolders(){
		settings.reset();
		boolean out=setWorkingDirectory();
		if(out){
			buildCellsList();
			for(int i=0; i<settings.cellsList.length; i++) createFolders(settings.cellsList[i]);
		}
		return out;
	}
	
	/**
	 * Opens an ImagePlus and eliminates all timepoints before the bleaching
	 * @param cell the cell to process
	 * @return and ImagePlus lacking all timepoints before the bleaching
	 */
	public static ImagePlus openAndCrop(String cell){
		ImagePlus out=new ImagePlus(settings.dir+cell);
		if(out.getStackSize()>settings.bleachTP) out=new Duplicator().run(out, settings.bleachTP, out.getStackSize());
		
		return out;
	}
	
	/**
	 * Sets the global calibration to the input ImagePlus then saves it at zip.
	 * No need to add the zip extension to the input saving directory
	 * @param ip the ImagePlus to calibrate and save
	 * @param dir the saving directory
	 */
	public static void calibrateAndSave(ImagePlus ip, String dir){
		ip.setCalibration(settings.getCalibration());
		File f=new File(dir+ip.getTitle().replace(".tif", "")+".zip");
		if(f.exists()) f.delete();
		new FileSaver(ip).saveAsZip(dir+ip.getTitle().replace(".tif", "")+".zip");
	}
	
	/**
	 * Closes all opened images
	 */
	public static void closeAll(){
		WindowManager.closeAllWindows();
		System.gc();
	}
	
	/**
	 * Retrieves the file names starting with the input prefix and suffix from a folder, as an array of String
	 * @param inputFolder the input folder
	 * @param nameStart the expected prefix
	 * @param extension the expected suffix
	 * @return the file names starting with the input prefix and suffix from a folder, as an array of String
	 */
	public static String[] getFilteredNames(String inputFolder, String nameStart, String extension){
		final String start=nameStart;
		final String ext=extension;
		
		FilenameFilter fnf=new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith(start.toLowerCase()) && name.toLowerCase().endsWith(ext.toLowerCase());
			}
		};
		
		File[] files=new File(inputFolder).listFiles(fnf);
		
		if(files!=null){
			String[] out=new String[files.length];
			for(int i=0; i<files.length; i++) out[i]=files[i].getName();
			return out;
		}else{
			return null;
		}
	}
}
