package settings;

import utilities.utilities;
import ij.IJ;
import ij.gui.YesNoCancelDialog;

/**
 * This class includes all methods to check that parameters have been set
 * @author fab
 *
 */
public class checks{
	/**
	 * Checks if the user wants to continue using the predefined working directory or point to a new one
	 * @return true if the operation was successfull, false otherwise
	 */
	public static boolean checkWorkingDirectory(){
		settings.reset();
		boolean out=true;
		if(!(settings.dir.equals("") || settings.dir.equals(null))){
			YesNoCancelDialog box=new YesNoCancelDialog(null, "Check working directory", "Do you want to work on the following directory ?\n"+settings.dir);
			if(box.cancelPressed()) return false;
			out=box.yesPressed();
		}
		
		if(!out) return filesAndFolders.setWorkingDirectory();
		
		if(!(settings.dir.equals("") || settings.dir.equals(null))){
			filesAndFolders.buildCellsList();
			for(int i=0; i<settings.cellsList.length; i++) filesAndFolders.createFolders(settings.cellsList[i]);
			return true;
		}else{
			return filesAndFolders.selectAndCreateFolders();
		}
	}
	
	/**
	 * Checks if the user has defined the bleach timepoint
	 * @return true if the operation was successfull, false otherwise
	 */
	public static boolean checkBleach(){
		if(settings.bleachTP==-1){
			double value=IJ.getNumber("Number of the last bleach timepoint ?", 15);
			if(value!=IJ.CANCELED){
				settings.bleachTP=(int) value;
				return true;
			}else{
				settings.bleachTP=-1;
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Checks if the user has defined the XY calibration
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean checkCalib(){
		if(settings.calibXY==-1){
			double value=IJ.getNumber("XY calibration ("+utilities.MICRO_SIGN+") ?", 0.133);
			if(value!=IJ.CANCELED){
				settings.calibXY=value;
				return checkCalibT();
			}else{
				settings.calibXY=-1;
				return false;
			}
		}
		
		return checkCalibT();
	}
	
	/**
	 * Checks if the user has defined the time calibration
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean checkCalibT(){
		if(settings.calibT==-1){
			double value=IJ.getNumber("Time calibration (sec) ?", 0.1);
			if(value!=IJ.CANCELED){
				settings.calibT=value;
				return true;
			}else{
				settings.calibT=-1;
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the user has defined the GCamp segments' length
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean checkGCampLength(){
		if(settings.GCampLength==-1){
			double value=IJ.getNumber("Length of segment for GCamp analysis (microns) ?", 4);
			if(value!=IJ.CANCELED){
				settings.GCampLength=value;
				return true;
			}else{
				settings.GCampLength=-1;
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the user has defined the wavelength for FRAP
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean checkLambda(){
		if(settings.lambda.equals("-1")){
			settings.lambdaSynapse="561";
			settings.lambda="488";
			settings.lambda=IJ.getString("Name of the lambda for FRAP channel", "561");
			if(settings.lambda.equals("")) settings.lambda="488";
			if(settings.lambda.equals("488")) settings.lambdaSynapse="561";
			if(settings.lambda.equals("561")) settings.lambdaSynapse="488";
			return true;
		}
		return true;
	}
	
	/**
	 * Checks if the user has defined the synapse's width of influence
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean checkSynapseZone(){
		if(settings.synapseZone==-1){
			int value=(int) IJ.getNumber("Synapse's influence zone (diameter, in pixels) ?", 7);
			if(value!=IJ.CANCELED){
				settings.synapseZone=value;
				return true;
			}else{
				settings.synapseZone=-1;
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the user wants to continue using the predefined working directory or point to a new one, 
	 * and checks if the bleach timepoint has been set
	 * @return true if the operation was successfull, false otherwise
	 */
	public static boolean checkFolderAndBleach(){
		return checkWorkingDirectory() && checkBleach();
	}
	
	/**
	 * Does the following checks: working directory, bleach timepoint, calibrations and FRAP lambda defined
	 * @return true if the operation was successfull, false otherwise
	 */
	public static boolean checkAll(){
		return checkWorkingDirectory() && checkBleach() && checkCalib() && checkLambda() && (settings.isGCampExperiment?checkGCampLength():true);
	}
	
	
}
