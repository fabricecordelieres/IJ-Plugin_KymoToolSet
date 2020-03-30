package settings;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import utilities.utilities;

/**
 * This class includes all parameters used in the plugin
 * @author fab
 *
 */
public class settings{
	
	//------------------------------------------
	/**Working directory**/
	public static String dir="";
	
	/**Files list from the working directory**/
	public static String[] files=null;
	
	/**Cells list from the working directory**/
	public static String[] cellsList=null;
	
	/**Name of the cell being processed**/
	public static String currCell="";
	
	
	//-----------Destination folders-------------
	/**Cell folder (parent folder)**/
	public static String destCell="";
	
	/**Processed stacks folder**/
	public static String destStkProc="";
	
	/**Projections folder**/
	public static String destProj="";
	
	
	//-----------Destination folders: Kymos-------------
	/**Kymos analysis folder (parent folder)**/
	public static String destKymos="";

	/**Paths (ROIs) for kymos analysis folder**/
	public static String destKymosPaths="";
	
	/**Paths (ROIs) for kymos analysis folder, recentered on the full image**/
	public static String destKymosPathsFullImg="";

	/**Kymograms folder**/
	public static String destKymosKymos="";
	
	/**Structures paths (ROIs) on the kymographs folder**/
	public static String destKymosROIs="";

	/**Data from kymographs analysis folder**/
	public static String destKymosData="";

	/**Data from kymographs analysis folder: images**/
	public static String destKymosDataImg="";

	/**Data from kymographs analysis folder: XLS filesAndFolders**/
	public static String destKymosDataXLS="";
	
	//-----------Destination folders: Synapses-------------
	/**Synapses folder (parent folder)**/
	public static String destSynapses="";
	
	//-----------Destination folders: Flux-------------
	/**Flux analysis folder (parent folder)**/
	public static String destFlux="";
	
	
	//-----------Destination folders: Illus-------------
	/**Illustration for Powerpoint folder (parent folder)**/
	public static String destForPPT="";
	
	//-----------Destination folders: GCamp-------------
	/**True if GCamp data have been detected**/
	public static boolean isGCampExperiment=false;
	
	/**GCamp experiment's name**/
	public static String GCampExperimentName="";
	
	/**GCamp original images folder**/
	public static String oriGCamp="";
	
	/**GCamp folder**/
	public static String destGCamp="";
	
	/**GCamp ROI folder**/
	public static String destGCampROI="";
	
	/**GCamp flux folder**/
	public static String destGCampFlux="";
	
	/**GCamp data folder**/
	public static String destGCampData="";
	
	/**GCamp data images folder**/
	public static String destGCampDataImg="";
	
	/**GCamp data XLS folder**/
	public static String destGCampDataXLS="";
	
	/**GCamp data synapses folder**/
	public static String destGCampDataSynapses="";
	
	/**GCamp length for segments in microns**/
	public static double GCampLength=-1;
	
	//-----------Destination folders: Global data-------------
	/**Global data folder (parent folder)**/
	public static String destGlobalData="";
	
	/**Experiment's name**/
	public static String expName="";
	
	/**File containing data for all cells**/
	public static String allCellsData="";
	
	/**File containing flux data for all cells**/
	public static String allCellsFlux="";
	
	/**File containing synapses data for all cells**/
	public static String allCellsSynapses="";
	
	/**File containing data for all cells tagged with synapses infos**/
	public static String allCellsDataSynapses="";
	
	/**File containing summarized data for all cells**/
	public static String allCellsSummary="";
	
	/**File containing summarized data for all cells+white spaces**/
	public static String allCellsSummaryWS="";
	
	
	//-----------Calibration data-------------
	/**XY pixel size in microns**/
	public static double calibXY=-1;
	
	/**Time intervalle between images in sec**/
	public static double calibT=-1;
	
	/**Wavelengths name**/
	public static final String[] LAMBDA_ARRAY=new String[]{"488", "561"};
	
	/**FRAP wavelength name**/
	public static String lambda="561";

	/**Synapse wavelength name**/
	public static String lambdaSynapse="488";
	
	/**Output: size of the scale bar to display in microns**/
	public static double scaleMicrons=5;

	/**Output: size of the time scale bar to display in seconds**/
	public static double scaleSeconds=5;
	
	
	//-----------Projections-------------
	/**Bleach timepoint**/
	public static int bleachTP=-1;
	
	/**Nb of timepoints per projection**/
	public static int incProj=150;
	
	
	//-----------Improve kymo: checks-------------
	/**Perform the kymo improvement ?**/
	public static boolean impKymo=true;
	
	/**First wavelet plane**/
	public static int startKymo=1;
	
	/**Last wavelet plane**/
	public static int stopKymo=3;

	
	//-----------Denoising: checks-------------
	/**Perform the denoising ?**/
	public static boolean denoise=true;
	
	/**Nb cycles**/
	public static int cycleDen=4;
	
	/**Nb frames**/
	public static int frameDen=3;
	

	//-----------Top-Hat: checks-------------
	/**Perform the top-hat filtering ?**/
	public static boolean doTH=true;
	
	/**Radius for top-hat**/
	public static double radTH=2;
	
	
	//-----------Flux analysis: checks-------------
	/**Tolerance for maxima detection**/
	public static int toleranceFlux=6500;
	
	
	//-----------Kymo analysis: parameters-------------
	/**Speed threshold defining pauses (microns per sec)**/
	public static double speedLimit=0.1;
	/**Line width for kymo display**/
	public static int lineWidth=3;
	/**Dot size for kymo display**/
	public static int dotSize=4;
	
	/**Tolerance for synapse detection**/
	public static int synapseTolerance=10000;
	/**Synapse's influence zone (diameter in pixels)**/
	public static int synapseZone=-1;
	
	
	
	//-----------Headers for output-------------
	/**Labels for output data table**/
	public static final String[] LABELS=new String[]{"Cell", "Image", "Kymo nb", "Ttl Time (sec)", 
			"Cum Dist (micron)", "Mean Speed (micron per sec)",
			"Mean Speed In (micron per sec)", "Mean Speed Out (micron per sec)",
			"Cum Dist (micron)", "Cum Dist In (micron)", "Cum Dist Out (micron)", "Min Dist Start-End (micron)",
			"Persistence", "Freq In>Out (sec-1)", "Freq In>Pause (sec-1)",
			"Freq Out>In (sec-1)", "Freq Out>Pause (sec-1)", "Freq Pause>In (sec-1)",
			"Freq Pause>Out (sec-1)", "Ttl Time (sec)", "% Time In", "% Time Out", "% Time Pause"};
	
	
	//-----------Dialog box-------------
	public static GenericDialog Dialog=null;
	
	
	/**
	 * IJ configuration reset: empties the RoiManager and the ResutsTable,
	 * sets the measurements to area and mean without redirection, resets 
	 * the foreground and background colors to white and black, respectively.
	 */
	public static void reset(){
		//RoiManager
		RoiManager rm=RoiManager.getInstance();
		if(rm!=null){
			rm.runCommand("Show None");
			rm.close();
		}
		
		//ResultsTable
		ResultsTable.getResultsTable().reset();
		
		//Measurements
		Analyzer.setMeasurement(Measurements.AREA+Measurements.MEAN, true);
		Analyzer.setRedirectImage(null);
		
		//General settings
		//TODO clean up here
		ImagePlus imp=WindowManager.getCurrentImage();
		if (imp!=null) imp.getProcessor().setLineWidth(2);
		Toolbar.setBackgroundColor(Color.black);
		Toolbar.setForegroundColor(Color.white);
		IJ.run("Colors...", "foreground=white background=black selection=yellow");
	}
	
	/**
	 * Return a calibration object containing all calibration values
	 * @return a calibration object containing all calibration values
	 */
	public static Calibration getCalibration(){
		Calibration out=new Calibration();
		
		out.setUnit(utilities.MICRO_SIGN);
		out.setTimeUnit("sec");
		out.pixelWidth=calibXY;
		out.pixelHeight=calibXY;
		out.frameInterval=calibT;
		
		return out;
	}
	
	/**
	 * Sets the working directory and the current cell to analyze
	 * @param cell
	 */
	public static void setFolderAndCurrentCell(String cell){
		reset();
		filesAndFolders.setFolders(cell);
		currCell="Cell"+cell;
	}
	
	public static String getDebugLog(){
		String out=	"------------------------------------------\n"
				+"Working directory, dir: " +dir+"\n"
				+"Files list from the working directory:\n";
	
		if(files!= null){
			for(int i=0; i<files.length; i++) out+="files["+i+"]: "+files[i]+"\n";
		}else{
			out+="NULL\n";
		}
		
		out+="Cells list from the working directory: \n";
		if(cellsList!=null){
			for(int i=0; i<cellsList.length; i++) out+="cellsList["+i+"]: "+cellsList[i]+"\n";
		}else{
			out+="NULL\n";
		}
		
		out+=	"Name of the cell being processed, currCell: "+currCell+"\n\n"
				
				+"-----------Destination folders-------------\n"
				+"Cell folder (parent folder), destCell: "+destCell+"\n"
				+"Processed stacks folder, destStkProc: "+destStkProc+"\n"
				+"Projections folder, destProj: "+destProj+"\n\n"
		
				+"-----------Destination folders: Kymos-------------\n"
				+"Kymos analysis folder (parent folder), destKymos: "+destKymos+"\n"
				+"Paths (ROIs) for kymos analysis folder, destKymosPaths: "+destKymosPaths+"\n"
				+"Paths (ROIs) for kymos analysis folder, recentered on the full image, destKymosPathsFullImg: "+destKymosPathsFullImg+"\n"
				+"Kymograms folder, destKymosKymos: "+destKymosKymos+"\n"
				+"Structures paths (ROIs) on the kymographs folder, destKymosROIs: "+destKymosROIs+"\n"
				+"Data from kymographs analysis folder, destKymosData: "+destKymosData+"\n"
				+"Data from kymographs analysis folder: images, destKymosDataImg: "+destKymosDataImg+"\n"
				+"Data from kymographs analysis folder: XLS filesAndFolders, destKymosDataXLS: "+destKymosDataXLS+"\n\n"
		
				+"-----------Destination folders: Flux-------------\n"
				+"Flux analysis folder (parent folder), destFlux: "+destFlux+"\n\n"
		
				+"-----------Destination folders: Illus-------------\n"
				+"Illustration for Powerpoint folder (parent folder), destForPPT: "+destForPPT+"\n\n"
				
				+"-----------Destination folders: GCamp data-------------\n"
				+"Is this a GCamp experiment ?, isGCampExperiment: "+isGCampExperiment+"\n"
				+"GCamp experiment's name ?, GCampExperimentName: "+GCampExperimentName+"\n"
				+"GCamp folder, destGCamp: "+destGCamp+"\n"
				+"GCamp ROI folder, destGCampROI: "+destGCampROI+"\n"
				+"GCamp flux folder, destGCampFlux: "+destGCampFlux+"\n"
				+"GCamp data folder, destGCampData: "+destGCampData+"\n"
				+"GCamp data images folder, destGCampDataImg: "+destGCampDataImg+"\n"
				+"GCamp data XLS folder, destGCampDataXLS: "+destGCampDataXLS+"\n\n"
				+"GCamp data synapses folder, destGCampDataSynapses: "+destGCampDataSynapses+"\n\n"
				
				+"-----------Destination folders: Global data-------------\n"
				+"Global data folder (parent folder), destGlobalData: "+destGlobalData+"\n"
				+"Experiment's name, expName: "+expName+"\n"
				+"File containing data for all cells, allCellsData: "+allCellsData+"\n"
				+"File containing flux data for all cells, allCellsFlux: "+allCellsFlux+"\n"
				+"File containing synapses data for all cells, allCellsFlux: "+allCellsSynapses+"\n"
				+"File containing synapses data for all cells, allCellsSynapses: "+allCellsSynapses+"\n"
				+"File containing data for all cells tagged with synapses infos, allCellsDataSynapses: "+allCellsDataSynapses+"\n"
				+"File containing summarized data for all cells, allCellsSummary: "+allCellsSummary+"\n"
				+"File containing summarized data for all cells+white spaces, allCellsSummaryWS: "+allCellsSummaryWS+"\n\n"
		
		
				+"-----------Calibration data-------------\n"
				+"XY pixel size in microns, calibXY: "+calibXY+"\n"
				+"Time intervalle between images in sec, calibT: "+calibT+"\n"
				+"Wavelengths name: \n";
		
		for(int i=0; i<LAMBDA_ARRAY.length; i++) out+="LAMBDA_ARRAY["+i+"]: "+LAMBDA_ARRAY[i]+"\n";
				
		out+=	"FRAP wavelength name, lambda: "+lambda+"\n"
				+"Synapse wavelength name, lambdaSynapse: "+lambdaSynapse+"\n"
				+"Output: size of the scale bar to display in microns, scaleMicrons: "+scaleMicrons+"\n"
				+"Output: size of the time scale bar to display in seconds, scaleSeconds: "+scaleSeconds+"\n\n"
		
		
				+"-----------Projections-------------\n"
				+"Bleach timepoint, bleachTP: "+bleachTP+"\n"
				+"Nb of timepoints per projection, incProj: "+incProj+"\n\n"
				
		
				+"-----------Improve kymo: checks-------------\n"
				+"Perform the kymo improvement ?, impKymo: "+impKymo+"\n"
				+"First wavelet plane, startKymo: "+startKymo+"\n"
				+"Last wavelet plane, stopKymo: "+stopKymo+"\n\n"

		
				+"-----------Denoising: checks-------------\n"
				+"Perform the denoising ?, denoise: "+denoise+"\n"
				+"Nb cycles, cycleDen: "+cycleDen+"\n"
				+"Nb frames, frameDen: "+frameDen+"\n\n"
		

				+"-----------Top-Hat: checks-------------\n"
				+"Perform the top-hat filtering ? doTH: "+doTH+"\n"
				+"Radius for top-hat, radTH: "+radTH+"\n\n"
		
		
				+"-----------Flux analysis: checks-------------\n"
				+"Tolerance for maxima detection, toleranceFlux: "+toleranceFlux+"\n\n"
		
				+"-----------Kymo analysis: parameters-------------\n"
				+"Speed threshold defining pauses (microns per sec), speedLimit: "+speedLimit+"\n"
				+"Line width for kymo display, lineWidth: "+lineWidth+"\n"
				+"Dot size for kymo display, dotSize: "+dotSize+"\n"
				+"Tolerance for synapse detection, synapseTolerance: "+synapseTolerance+"\n"
				+"Synapse's influence zone (in pixels), synapse zone: "+synapseZone;
				
				return out;
	}
	
	/**
	 * Writes the content of the debug infos to a file, in the _Data folder
	 */
	public static void writeLog(){
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String date=dateFormat.format(new Date());
			FileWriter fw=new FileWriter(settings.dir+"_Data"+File.separator+date+"_Parameters.txt");
			fw.write(getDebugLog());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
