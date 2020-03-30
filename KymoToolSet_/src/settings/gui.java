package settings;

import utilities.utilities;
import ij.gui.GenericDialog;

/**
 * This class includes all methods to generate the GUIs
 * @author fab
 *
 */
public class gui{
	/**
	 * Adds boxes to retrieve calibration to the current dialog box
	 */
	public static void addCalibration(){
		settings.Dialog.addMessage("Calibration");
		settings.Dialog.addNumericField("XY_("+utilities.MICRO_SIGN+")", 0.212, 3);
		settings.Dialog.addNumericField("Time_(sec)", 0.1, 3);
		settings.Dialog.addChoice("Lambda_for_kymos", settings.LAMBDA_ARRAY, "561");
	}

	/**
	 * Retrieves the calibration informations from the current dialog box
	 */
	public static void getCalibration(){
		settings.calibXY=settings.Dialog.getNextNumber();
		settings.calibT=settings.Dialog.getNextNumber();
		settings.lambda=settings.Dialog.getNextChoice();
	}

	/**
	 * Adds boxes to retrieve analysis checks to the current dialog box
	 */
	public static void addPrepAnalysis(){
		settings.Dialog.addMessage("Projections");
		settings.Dialog.addNumericField("Last bleaching timepoint", 15, 0);
		//settings.Dialog.addNumericField("Increment for sub-stack", settings.incProj, 0);

		settings.Dialog.addMessage("Filtering");
		settings.Dialog.addCheckbox("Improve kymo", settings.impKymo);
		settings.Dialog.addNumericField("Start", settings.startKymo, 0);
		settings.Dialog.addNumericField("Stop", settings.stopKymo, 0);

		settings.Dialog.addCheckbox("Denoise", settings.denoise);
		settings.Dialog.addNumericField("Cycle-spins", settings.cycleDen, 0);
		settings.Dialog.addNumericField("Multiframe", settings.frameDen, 0);

		settings.Dialog.addCheckbox("Top-Hat", settings.doTH);
		settings.Dialog.addNumericField("Radius", settings.radTH, 2);
	}

	/**
	 * Retrieves the pre-processing informations from the current dialog box
	 */
	public static void getPrepAnalysis(){
		settings.bleachTP=(int) settings.Dialog.getNextNumber();
		//settings.incProj=(int) settings.Dialog.getNextNumber();
		settings.impKymo=settings.Dialog.getNextBoolean();
		settings.startKymo=(int) settings.Dialog.getNextNumber();
		settings.stopKymo=(int) settings.Dialog.getNextNumber();
		settings.denoise=settings.Dialog.getNextBoolean();
		settings.cycleDen=(int) settings.Dialog.getNextNumber();
		settings.frameDen=(int) settings.Dialog.getNextNumber();
		settings.doTH=settings.Dialog.getNextBoolean();
		settings.radTH=settings.Dialog.getNextNumber();
	}
	
	/**
	 * Asks for the calibration and pre-processing checks, then stores values
	 */
	public static boolean preprocessGUI(){
		settings.Dialog=new GenericDialog("Kymographs Analysis: Parameters for images' pre-processing");
		addCalibration();
		addPrepAnalysis();
		settings.Dialog.showDialog();
		
		if(settings.Dialog.wasCanceled()){
			return false;
		}else{
			getCalibration();
			getPrepAnalysis();
			return true;
		}
	}
}
