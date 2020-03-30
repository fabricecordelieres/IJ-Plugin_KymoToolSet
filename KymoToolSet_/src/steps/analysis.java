package steps;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import Utilities.kymograph.kymograph;
import Utilities.kymograph.analyseKymo;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.io.RoiDecoder;
import ij.measure.Measurements;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.Projector;
import ij.plugin.RGBStackConverter;
import ij.plugin.RGBStackMerge;
import ij.plugin.StackCombiner;
import ij.plugin.Straightener;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import rgnFile.rgnFile;
import settings.filesAndFolders;
import settings.settings;
import utilities.utilities;

/**
 * This class includes all methods to perform the analysis
 * 
 * @author fab
 *
 */
public class analysis implements MouseListener, MouseMotionListener, KeyListener {

	/** Header for the data extracted by flux analysis (overall values) **/
	public static final String[] HEADER_FLUX_SUMMARY = new String[] { "Mean nb of vesicules", "Min nb of vesicules",
			"Max nb of vesicules", "Bleached area (microns2)", "Mean vesicules' density (nb/microns2)",
			"Min vesicules' density (nb/microns2)", "Max vesicules' density (nb/microns2)" };

	/** Header for the data extracted by flux analysis (individual values) **/
	public static final String[] HEADER_FLUX = new String[] { "Time (sec)", "Number of vesicules",
			"Delta number of vesicules" };

	/** Stores the coordinates of the pointed synapses **/
	public ArrayList<Point> synapses = new ArrayList<Point>();

	/** Stores the radius to display pointed synapses **/
	public int synapseRadius = 10;

	/** Stores the ImageProcessor to display pointed synapses **/
	ImageProcessor procSynapses;

	/** Stores the onscreen x coordinate **/
	int x = 0;

	/** Stores the onscreen y coordinate **/
	int y = 0;

	/**
	 * Performs the flux analysis on the designated cell
	 * 
	 * @param cell
	 *            the cell to analyze
	 */
	public static void analyzeFlux(String cell) {
		WindowManager.closeAllWindows();
		settings.setFolderAndCurrentCell(cell);

		ImagePlus[] split = ChannelSplitter.split(new ImagePlus(settings.destForPPT + settings.currCell + "_crop.zip"));
		ImagePlus fluxImg = split[0];
		ImagePlus synapses = split[1];

		split = null;

		Roi roi = null;
		try {
			roi=(new RoiDecoder(settings.destForPPT+settings.currCell+ "_crop.roi")).getRoi();
		} catch (IOException e) {
			System.out.println("Analyze flux: FRAP ROI not found for "+settings.currCell);
		}
		
		fluxImg.setCalibration(settings.getCalibration());
		ImagePlus fluxDetections = NewImage.createImage("detections", fluxImg.getWidth(), fluxImg.getHeight(),
				fluxImg.getStackSize(), fluxImg.getBitDepth(), NewImage.FILL_BLACK);
		fluxDetections.setTitle("Detection");
		fluxDetections.setCalibration(settings.getCalibration());
		
		fluxImg.setRoi(roi);
		double bleachSurf = fluxImg.getStatistics(Measurements.AREA).area;

		int nT = fluxImg.getStackSize();
		double[] time = new double[nT];
		double[] nVesic = new double[nT]; // Double is necessary for plots
		double[] dnVesic = new double[nT]; // Double is necessary for plots
		dnVesic[0] = Double.NaN;

		double meanNVesic = 0;
		int minNVesic = 0;
		int maxNVesic = 0;

		// Detect and store detections
		for (int i = 0; i < nT; i++) {
			fluxImg.setRoi(roi);
			fluxImg.setSlice(i+1);
			fluxImg.getProcessor().setColor(Color.BLACK);
			fluxImg.getProcessor().fillOutside(roi);
			time[i] = i * settings.calibT;
			Polygon detections = new MaximumFinder().getMaxima(fluxImg.getProcessor(), settings.toleranceFlux, true);
			nVesic[i] = detections.npoints;
			meanNVesic += nVesic[i];

			if (i == 0) {
				dnVesic[i] = Double.NaN;
				minNVesic = (int) nVesic[0];
				maxNVesic = (int) nVesic[0];
			} else {
				minNVesic = Math.min(minNVesic, (int) nVesic[i]);
				maxNVesic = Math.max(maxNVesic, (int) nVesic[i]);
				dnVesic[i] = nVesic[i] - nVesic[i - 1];
			}

			// Generate the view of detections
			fluxDetections.setSlice(i);
			for (int j = 0; j < detections.npoints; j++) {
				fluxDetections.getProcessor().setColor(Color.WHITE);
				fluxDetections.getProcessor().setLineWidth(1);
				fluxDetections.getProcessor().drawDot(detections.xpoints[j], detections.ypoints[j]);
				fluxDetections.getProcessor().blurGaussian(1.5);
			}
		}

		meanNVesic /= nT;
		ImagePlus output = new Concatenator().concatenate(fluxImg, synapses, false);
		output = new Concatenator().concatenate(output, fluxDetections, false);
		output = HyperStackConverter.toHyperStack(output, 3, nT, 1, "xyzct", "composite");
		output.setC(1);
		output.resetDisplayRange();
		output.setC(2);
		output.resetDisplayRange();
		output.setC(3);
		output.resetDisplayRange();
		output.setCalibration(settings.getCalibration());
		output.setTitle(settings.currCell + "_flux_detections");
		new FileSaver(output).saveAsZip(settings.destFlux + settings.currCell + "_flux_detections.zip");
		output.close();

		// Creates and saves plots
		Plot plot = new Plot("Nb vesicules vs time", "Time (sec)", "Nb vesicules", time, nVesic);
		new FileSaver(plot.getImagePlus())
				.saveAsJpeg(settings.destFlux + settings.currCell + "_flux_nVesic-vs-time.jpg");

		plot = new Plot("Delta nb vesicules vs time", "Time (sec)", "Delta nb vesicules", time, dnVesic);
		new FileSaver(plot.getImagePlus())
				.saveAsJpeg(settings.destFlux + settings.currCell + "_flux_Delta-nVesic-vs-time.jpg");

		// Saves numerical values

		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { meanNVesic + "", minNVesic + "", maxNVesic + "", bleachSurf + "",
				(meanNVesic / bleachSurf) + "", (minNVesic / bleachSurf) + "", (maxNVesic / bleachSurf) + "" });
		data.add(null);
		data.add(HEADER_FLUX);
		for (int i = 0; i < time.length; i++)
			data.add(new String[] { utilities.round(time[i], 3), ((int) nVesic[i]) + "", ((int) dnVesic[i]) + "" });

		utilities.saveAsCSV(HEADER_FLUX_SUMMARY, data, settings.destFlux + settings.currCell + "_flux_data.xls");

		if (settings.isGCampExperiment) {
			analyzeFluxGCamp(cell, nT);
			analyzeIntensityGCamp(cell, nT);
		}
	}

	/**
	 * Performs the flux analysis for GCamp data on the designated cell
	 * 
	 * @param cell
	 *            the cell to analyze
	 * @param nT
	 *            number of timepoints
	 */
	public static void analyzeFluxGCamp(String cell, int nT) {
		WindowManager.closeAllWindows();
		settings.setFolderAndCurrentCell(cell);

		RoiManager rm = RoiManager.getInstance();
		if (rm == null) {
			rm = new RoiManager();
		} else {
			rm.reset();
		}
		rm.runCommand("Open", settings.destGCampROI + settings.currCell + "_rois-segments_crop.zip");

		int nRois = rm.getCount();
		double[] time = new double[nT];
		double[][] nVesic = new double[nT][nRois]; // Double is necessary for
													// plots
		double[][] dnVesic = new double[nT][nRois]; // Double is necessary for
													// plots

		double[] segmentSurf = new double[nRois];
		double[] meanNVesic = new double[nRois];
		int[] minNVesic = new int[nRois];
		int[] maxNVesic = new int[nRois];

		for (int i = 0; i < rm.getCount(); i++) {
			WindowManager.closeAllWindows();

			ImagePlus[] split = ChannelSplitter
					.split(new ImagePlus(settings.destForPPT + settings.currCell + "_crop.zip"));
			ImagePlus fluxImg = split[0];
			ImagePlus GCamp = split[1];
			split = null;

			fluxImg.setRoi(rm.getRoi(i));

			Roi roi = fluxImg.getRoi();
			fluxImg.setCalibration(settings.getCalibration());
			ImagePlus fluxDetections = NewImage.createImage("detections", fluxImg.getWidth(), fluxImg.getHeight(),
					fluxImg.getStackSize(), fluxImg.getBitDepth(), NewImage.FILL_BLACK);
			fluxDetections.setTitle("Detection");
			fluxDetections.setCalibration(settings.getCalibration());

			segmentSurf[i] = fluxImg.getStatistics(Measurements.AREA).area;

			dnVesic[0][i] = Double.NaN;
			meanNVesic[i] = 0.0;

			// Detect and store detections
			for (int j = 0; j < nT; j++) {
				fluxImg.setRoi(roi);
				fluxImg.setSlice(j+1);
				fluxImg.getProcessor().setColor(Color.BLACK);
				fluxImg.getProcessor().fillOutside(roi);
				time[j] = j * settings.calibT;
				Polygon detections = new MaximumFinder().getMaxima(fluxImg.getProcessor(), settings.toleranceFlux,
						true);
				nVesic[j][i] = detections.npoints;
				meanNVesic[i] += nVesic[j][i];

				if (j == 0) {
					dnVesic[j][i] = Double.NaN;
					minNVesic[i] = (int) nVesic[0][i];
					maxNVesic[i] = (int) nVesic[0][i];
				} else {
					minNVesic[i] = Math.min(minNVesic[i], (int) nVesic[j][i]);
					maxNVesic[i] = Math.max(maxNVesic[i], (int) nVesic[j][i]);
					dnVesic[j][i] = nVesic[j][i] - nVesic[j - 1][i];
				}

				// Generate the view of detections
				fluxDetections.setSlice(j);
				for (int k = 0; k < detections.npoints; k++) {
					fluxDetections.getProcessor().setColor(Color.WHITE);
					fluxDetections.getProcessor().setLineWidth(1);
					fluxDetections.getProcessor().drawDot(detections.xpoints[k], detections.ypoints[k]);
					fluxDetections.getProcessor().blurGaussian(1.5);
				}
			}

			meanNVesic[i] /= nT;
			ImagePlus output = new Concatenator().concatenate(fluxImg, GCamp, false);
			output = new Concatenator().concatenate(output, fluxDetections, false);
			output = HyperStackConverter.toHyperStack(output, 3, nT, 1, "xyzct", "composite");
			output.setC(1);
			output.resetDisplayRange();
			output.setC(2);
			output.resetDisplayRange();
			output.setC(3);
			output.resetDisplayRange();
			output.setCalibration(settings.getCalibration());
			output.setTitle(settings.currCell + "_segment" + (i + 1) + "_flux_detections");
			new FileSaver(output).saveAsZip(
					settings.destGCampFlux + settings.currCell + "_" + roi.getName() + "_flux_detections.zip");
			output.close();

			// Creates and saves plots
			Plot plot = new Plot("Nb vesicules vs time", "Time (sec)", "Nb vesicules", time, nVesic[i]);
			new FileSaver(plot.getImagePlus()).saveAsJpeg(
					settings.destGCampFlux + settings.currCell + "_segment" + (i + 1) + "_flux_nVesic-vs-time.jpg");

			plot = new Plot("Delta nb vesicules vs time", "Time (sec)", "Delta nb vesicules", time, dnVesic[i]);
			new FileSaver(plot.getImagePlus()).saveAsJpeg(settings.destGCampFlux + settings.currCell + "_"
					+ roi.getName() + "_flux_Delta-nVesic-vs-time.jpg");
		}
		// Saves numerical values
		ArrayList<String[]> data = new ArrayList<String[]>();
		String[] headerSummary = new String[] { "Segment", "Mean nb of vesicules", "Min nb of vesicules",
				"Max nb of vesicules", "Segment area (microns2)", "Mean vesicules' density (nb/microns2)",
				"Min vesicules' density (nb/microns2)", "Max vesicules' density (nb/microns2)" };
		String[] headerFlux = new String[1 + 2 * nRois];
		headerFlux[0] = "Time";
		int index = 1;

		for (int i = 0; i < nRois; i++) {
			data.add(new String[] { (i + 1) + "", meanNVesic[i] + "", minNVesic[i] + "", maxNVesic[i] + "",
					segmentSurf[i] + "", (meanNVesic[i] / segmentSurf[i]) + "", (minNVesic[i] / segmentSurf[i]) + "",
					(maxNVesic[i] / segmentSurf[i]) + "" });
			String roiName = rm.getName(i);
			headerFlux[index++] = roiName + "-Number of vesicules";
			headerFlux[index++] = roiName + "-Delta number of vesicules";
		}

		data.add(null);
		data.add(headerFlux);
		for (int i = 0; i < time.length; i++) {
			String[] line = new String[1 + 2 * nRois];
			line[0] = utilities.round(time[i], 3) + "";
			index = 1;
			for (int j = 0; j < nRois; j++) {
				line[index++] = (int) nVesic[i][j] + "";
				line[index++] = (int) dnVesic[i][j] + "";
			}
			data.add(line);
		}
		utilities.saveAsCSV(headerSummary, data, settings.destGCampFlux + settings.currCell + "_flux_data.xls");

		WindowManager.closeAllWindows();
	}

	/**
	 * Performs the intensity analysis for GCamp data on the designated cell
	 * 
	 * @param cell
	 *            the cell to analyze
	 * @param nT
	 *            number of timepoints
	 */
	public static void analyzeIntensityGCamp(String cell, int nT) {
		WindowManager.closeAllWindows();
		settings.setFolderAndCurrentCell(cell);

		GCampTools.saveDFonF(cell, settings.destGCampROI + settings.currCell + "_rois-segments_crop.zip",
				settings.destForPPT + settings.currCell + "_crop.zip",
				settings.destGCampDataXLS + settings.currCell + "_Segment_Fluo_Data.xls");

		/*
		 * 
		 * RoiManager rm=RoiManager.getInstance(); if(rm==null){ rm=new
		 * RoiManager(); }else{ rm.reset(); } rm.runCommand("Open",
		 * settings.destGCampROI+settings.currCell+"_rois-segments_crop.zip");
		 * 
		 * 
		 * int nRois=rm.getCount(); double[] time = new double[nT]; double[]
		 * baseline = new double[nT]; double[][] fluo = new double[nT][nRois];
		 * // Double is necessary for plots double[][] deltaFOnF = new
		 * double[nT][nRois]; // Double is necessary for plots
		 * 
		 * double[] segmentSurf = new double[nRois];
		 * 
		 * WindowManager.closeAllWindows();
		 * 
		 * ImagePlus[] split = ChannelSplitter.split(new ImagePlus(
		 * settings.destForPPT + settings.currCell + "_crop.zip")); ImagePlus
		 * vesic = split[0]; ImagePlus GCamp = split[1]; split = null;
		 * 
		 * 
		 * //Define baseline: average image intensity GCamp.deleteRoi(); for
		 * (int i = 0; i < nT; i++) { GCamp.setSlice(i); GCamp.deleteRoi();
		 * baseline[i]=GCamp.getStatistics(Measurements.MEAN).mean; }
		 * 
		 * 
		 * for(int i=0; i<rm.getCount(); i++){ GCamp.setRoi(rm.getRoi(i)); Roi
		 * roi = GCamp.getRoi();
		 * GCamp.setCalibration(settings.getCalibration()); segmentSurf[i] =
		 * GCamp.getStatistics(Measurements.AREA).area;
		 * 
		 * //Quantify for (int j = 0; j < nT; j++) { GCamp.setRoi(roi);
		 * GCamp.setSlice(j);
		 * fluo[j][i]=GCamp.getStatistics(Measurements.MEAN).mean; time[j] = j *
		 * settings.calibT;
		 * 
		 * deltaFOnF[j][i] = (fluo[j][i] - baseline[j])/baseline[j]; } }
		 * 
		 * // Saves numerical values ArrayList<String[]> data = new
		 * ArrayList<String[]>(); String[] headerSummary=new String[]{"Segment",
		 * "Area (microns2)"}; String[] headerFlux=new String[2+2*nRois];
		 * headerFlux[0]="Time"; headerFlux[1]="Baseline"; int index=2;
		 * 
		 * for(int i=0; i<nRois; i++){ String roiName=rm.getName(i);
		 * data.add(new String[] {roiName, segmentSurf[i] + ""});
		 * headerFlux[index++]=roiName+"-Fluo"; headerFlux[index++]=roiName+
		 * "-Delta F/F"; }
		 * 
		 * data.add(null); data.add(headerFlux); for (int i = 0; i <
		 * time.length; i++){ String[] line= new String[2+2*nRois];
		 * line[0]=utilities.round(time[i], 3)+""; line[1]=baseline[i]+"";
		 * index=2; for(int j=0; j<nRois; j++){ line[index++]=fluo[i][j] + "";
		 * line[index++]=deltaFOnF[i][j] + ""; } data.add(line); }
		 * utilities.saveAsCSV(headerSummary, data, settings.destGCampDataXLS +
		 * settings.currCell +"_GCamp-Fluo_data.xls");
		 */
		WindowManager.closeAllWindows();
	}

	/**
	 * Requests the user to draw the path over all the images loaded from the
	 * input folder, saves the content of the RoiManager to the ouput folder
	 * 
	 * @param cell
	 *            the current cell to analyze
	 * @param loadRefImgdrawRoi
	 *            true to load a reference image and draw the FRAP Roi
	 * @param in
	 *            the input folder
	 * @param out
	 *            the output folder
	 */
	public static void recordPaths(String cell, boolean loadRefImgdrawRoi, String in, String out) {
		settings.setFolderAndCurrentCell(cell);
		String[] fileNames = new File(in).list();
		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
			rm = new RoiManager();

		for (int i = 0; i < fileNames.length; i++) {
			if (fileNames[i].endsWith(".zip")) {
				ImagePlus overView = null;
				if (loadRefImgdrawRoi) {
					// Opens the reference image, the jpg version with the FRAP
					// zone delineated
					overView = new ImagePlus(settings.destForPPT + "Cell" + cell + "_FRAP_Zone.jpg");
					overView.show();
				}

				// Displays the image on which roads have to be drawn
				ImagePlus ip = new ImagePlus(in + fileNames[i]);
				ip.getProcessor().setColor(Color.white);
				ip.getProcessor().setLineWidth(5);
				ip.show();
				ip.resetDisplayRange();

				// Zoom 300%
				Point location = ip.getCanvas().getLocationOnScreen();
				ip.getCanvas().zoomIn(location.x, location.y);
				ip.getCanvas().zoomIn(location.x, location.y);
				ip.getCanvas().zoomIn(location.x, location.y);

				if (loadRefImgdrawRoi) {
					// Load the region and draw it on the image
					Roi roiOnCrop = RoiDecoder.open(settings.destCell + settings.currCell + "_crop.roi");
					// Stores current Roi's line width
					int w = Line.getWidth();
					Line.setWidth(1);
					roiOnCrop.setStrokeWidth(1);
					ip.getProcessor().draw(roiOnCrop);
					ip.deleteRoi();
					ip.updateAndDraw();
					// Restores Roi's line width
					Line.setWidth(w);
				}

				rm.reset();
				rm.runCommand("Show All");
				IJ.run("Colors...", "foreground=white background=black selection=magenta");
				IJ.setTool("polyline");
				WaitForUserDialog wfud = new WaitForUserDialog(
						"Using the segmented line tool,\ndraw the vesicles' paths ON THE SMALLER IMAGE,\nadd them to the ROI Manager (using the 't' key),\nfinally click on Ok");
				wfud.show();
				if (rm.getCount() >= 1)
					rm.runCommand("Save", out + ip.getTitle().replace(".tif", ".zip"));

				if (loadRefImgdrawRoi) {
					overView.changes = false;
					overView.close();
				}

				ip.changes = false;
				ip.close();

			}
		}
	}

	/**
	 * Draws and save multiple kymos for the input cell
	 * 
	 * @param cell
	 *            the cell to process
	 */
	public static void multipleDrawKymo(String cell) {
		settings.setFolderAndCurrentCell(cell);
		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
			rm = new RoiManager();

		String[] stkProcFiles = filesAndFolders.getFilteredNames(settings.destStkProc, settings.currCell, ".zip");

		if (stkProcFiles.length != 0) {
			ImagePlus ip = new ImagePlus(settings.destStkProc + stkProcFiles[0]);
			// ip = new Duplicator().run(ip, settings.bleachTP,
			// ip.getStackSize());
			ip.setTitle(settings.currCell + "_crop");

			String[] fileNames = new File(settings.destKymosPaths).list();

			for (int i = 0; i < fileNames.length; i++)
				if (fileNames[i].endsWith(".zip"))
					rm.runCommand("open", settings.destKymosPaths + fileNames[i]);

			Roi[] rois = rm.getRoisAsArray();
			for (int i = 0; i < rois.length; i++) {
				ImagePlus kymo = new kymograph(ip, rois[i]).getKymograph((int) rois[i].getStrokeWidth());
				kymo.setTitle(settings.currCell + "_Kymo" + (i + 1));
				new FileSaver(kymo).saveAsZip(settings.destKymosKymos + settings.currCell + "_Kymo" + (i + 1) + ".zip");
				kymo.close();
			}
		}
	}

	/**
	 * Performs the analysis of the kymographs, saves the data and images for
	 * the input cell
	 * 
	 * @param cell
	 *            the cell to process
	 */
	public static void analyseKymo(String cell) {
		settings.setFolderAndCurrentCell(cell);
		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
			rm = new RoiManager();

		String[] stkProcFiles = filesAndFolders.getFilteredNames(settings.destStkProc, settings.currCell, ".zip");

		if (stkProcFiles != null) {
			String stackName = stkProcFiles[0];

			// Open the original stack and removes the pre-bleach sequence
			ImagePlus ip = new ImagePlus(settings.destStkProc + stackName);
			// ip = new Duplicator().run(ip, settings.bleachTP,
			// ip.getStackSize());
			ip.setSlice(ip.getStackSize() / 2);
			ip.resetDisplayRange();
			new ImageConverter(ip).convertToGray8();

			shiftPaths(cell);

			String[] kymos2Analyze = utilities.concatenate(
					filesAndFolders.getFilteredNames(settings.destKymosROIs, settings.currCell, ".zip"),
					filesAndFolders.getFilteredNames(settings.destKymosROIs, settings.currCell, ".roi"));

			if (kymos2Analyze != null) {
				for (int i = 0; i < kymos2Analyze.length; i++) {
					rm.reset();
					rm.runCommand("open", settings.destKymosROIs + kymos2Analyze[i]);

					ImagePlus kymo = new ImagePlus(settings.destKymosKymos + kymos2Analyze[i].replace(".roi", ".zip"));
					kymo.resetDisplayRange();
					new ImageConverter(kymo).convertToGray8();

					analyseKymo ak = new analyseKymo(kymo, rm.getRoisAsArray(), true, settings.speedLimit, true);

					String baseName = kymos2Analyze[i].replace(".roi", "").replace(".zip", "");

					// Saves all results
					utilities.saveAsCSV(ak.getResultsHeader(true), ak.getResults(true),
							settings.destKymosDataXLS + baseName + ".xls");
					utilities.saveAsCSV(ak.getCoordHeader(), ak.getCoord(),
							settings.destKymosDataXLS + "Extrapolated_coordinates_from_" + baseName + ".xls");

					// Creates and saves the image of the kymograph with scale
					// bars
					ImagePlus kymoOverlay = ak.getImage(settings.lineWidth);
					ImagePlus[] channels = ChannelSplitter.split(kymoOverlay);
					ImagePlus[] imgArray = new ImagePlus[4];
					imgArray[0] = channels[0];
					imgArray[1] = channels[1];

					ImageProcessor iproc = kymoOverlay.getProcessor().createProcessor(kymoOverlay.getWidth(),
							kymoOverlay.getHeight());
					iproc.setColor(Color.BLACK);
					iproc.fill();
					utilities.addScaleBar(iproc, settings.scaleMicrons, settings.scaleSeconds,
							settings.getCalibration());
					imgArray[2] = new ImagePlus("Scale bar", iproc);

					kymoOverlay = RGBStackMerge.mergeChannels(imgArray, false);
					kymoOverlay.setTitle(baseName + "-Tracks");
					filesAndFolders.calibrateAndSave(kymoOverlay, settings.destKymosDataImg);

					// Check if the synapses file exists and process (or not)
					if (new File(settings.destSynapses + baseName + "-Synapses.xls").exists()) {
						// Creates and saves the image of the kymograph with the
						// synapses
						RGBStackConverter.convertToRGB(kymoOverlay);
						kymoOverlay.getProcessor().setLineWidth(2);
						kymoOverlay.getProcessor().setColor(Color.WHITE);
						kymoOverlay.draw();

						// Combines both the kymo and synapses views
						ImagePlus synapses = new ImagePlus(settings.destSynapses + baseName + "-Synapses.zip");
						new ImageConverter(synapses).convertToRGB();

						StackCombiner sc = new StackCombiner();
						ImageStack montage = sc.combineVertically(synapses.getStack(), kymoOverlay.getStack());
						ImagePlus montageIP = new ImagePlus(baseName + "-Synapses", montage);
						filesAndFolders.calibrateAndSave(montageIP, settings.destKymosDataImg);

						Polygon pol = utilities
								.synapsesFileToPolygon(settings.destSynapses + baseName + "-Synapses.xls");

						// Draw synapses
						int[] xPoints = pol.xpoints;
						int[] yPoints = pol.ypoints;
						ImageProcessor procMontage = montageIP.getProcessor();
						procMontage.setLineWidth(1);
						int height = montageIP.getHeight();

						montageIP.setCalibration(null);
						for (int j = 0; j < pol.npoints; j++) {
							procMontage.setColor(Color.ORANGE);
							procMontage.drawLine(xPoints[j], yPoints[j] + (int) settings.synapseZone / 2, xPoints[j],
									height);
							procMontage.setColor(Color.RED);
							procMontage.drawOval(xPoints[j] - (int) settings.synapseZone / 2,
									yPoints[j] - (int) settings.synapseZone / 2, settings.synapseZone,
									settings.synapseZone);
						}
						montageIP.updateImage();
						montageIP.setTitle(baseName + "-Synapses_and_Lines");
						filesAndFolders.calibrateAndSave(montageIP, settings.destKymosDataImg);

						montage = null;
						montageIP.changes = false;
						montageIP.close();
					}

					// Close all the kymo related representations
					kymo.changes = false;
					kymo.close();
					kymoOverlay.changes = false;
					kymoOverlay.close();

					// Generates and saves the original image overlaid with the
					// extrapolated coordinates
					ImagePlus extrapolatedStack = ak.reportOnStack(ip, settings.dotSize);
					extrapolatedStack.setTitle(baseName + "-Extrapolated_coordinates");
					filesAndFolders.calibrateAndSave(extrapolatedStack, settings.destKymosDataImg);
					extrapolatedStack.close();
				}
			}
		}
	}

	/**
	 * From the ROIs drawn on the cropped, FRAPed image, shifts the ROIs to be
	 * displayed on the full frame image
	 * 
	 * @param cell
	 *            the cell to process
	 */
	public static void shiftPaths(String cell) {
		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
			rm = new RoiManager();
		rm.reset();

		settings.setFolderAndCurrentCell(cell);

		if (new File(settings.dir + settings.currCell + "-roi.rgn").exists()) {
			Roi roi = new rgnFile(settings.dir, settings.currCell + "-roi.rgn").rois.get(0).getIJRoifromMMRoi();
			Rectangle boundingBox = roi.getBounds();

			String[] kymos2Draw = utilities.concatenate(
					filesAndFolders.getFilteredNames(settings.destKymosPaths, settings.currCell, ".zip"),
					filesAndFolders.getFilteredNames(settings.destKymosPaths, settings.currCell, ".roi"));

			for (int i = 0; i < kymos2Draw.length; i++)
				rm.runCommand("open", settings.destKymosPaths + kymos2Draw[i]);

			for (int i = 0; i < rm.getCount(); i++) {
				Roi tmpRoi = rm.getRoi(0);
				Rectangle tmpBoundingBox = tmpRoi.getBounds();
				tmpRoi.setLocation(boundingBox.x + tmpBoundingBox.x, boundingBox.y + tmpBoundingBox.y);
				rm.select(0);
				rm.runCommand("Delete");
				rm.addRoi(tmpRoi);
			}

			if (rm.getCount() > 0)
				rm.runCommand("Save", settings.destKymosPathsFullImg + settings.currCell + ".zip");
		}
	}

	/**
	 * Detects the synapses, export the data in an XLS file and a composite and
	 * returns the synapses' coordinates as a Polygon
	 * 
	 * @param iproc
	 *            the input ImageProcessor containing the synapses
	 * @param title
	 *            the basename used for outputs
	 * @return the synapses' coordinates as a Polygon
	 */
	public static Polygon detectSynapses(ImageProcessor iproc, String baseName) {
		ImageProcessor iprocOut = iproc.createProcessor(iproc.getWidth(), iproc.getHeight());
		iprocOut.setLineWidth(1);
		iprocOut.setColor(Color.WHITE);

		Polygon maxima = new MaximumFinder().getMaxima(iproc, settings.synapseTolerance, false);

		int[] x = maxima.xpoints;
		int[] y = maxima.ypoints;

		ArrayList<String[]> out = new ArrayList<String[]>();
		out.add(new String[] { maxima.npoints + "", iproc.getWidth() * settings.calibXY + "",
				maxima.npoints / (iproc.getWidth() * settings.calibXY) + "" });
		out.add(new String[] { maxima.npoints != 0 ? "Synapse nb\tx\ty" : "" });
		out.add(null);

		for (int i = 0; i < maxima.npoints; i++) {
			iprocOut.drawOval(x[i] - settings.synapseZone / 2, y[i] - settings.synapseZone / 2, settings.synapseZone,
					settings.synapseZone);
			out.add(new String[] { (i + 1) + "", x[i] + "", y[i] + "" });
		}

		utilities.saveAsCSV(
				new String[] { "Number of synapse", "Section's length (microns)", "Density (nb synapse/micron)" }, out,
				settings.destKymosDataXLS + baseName + "-Synapses_detections.xls");

		ImagePlus synapses = RGBStackMerge.mergeChannels(
				new ImagePlus[] { new ImagePlus("Detections", iprocOut), null, null, new ImagePlus("Synapses", iproc) },
				false);
		synapses.setTitle(baseName + "-Synapses_detection");
		filesAndFolders.calibrateAndSave(synapses, settings.destKymosDataImg);

		return maxima;
	}

	/**
	 * On the input ImagePlus, places a circular zone around the pointed
	 * location, and records coordinates when the user right-clicks
	 * 
	 * @param ip
	 *            input ImagePlus
	 */
	public void pointSynapses(String cell) {
		settings.setFolderAndCurrentCell(cell);

		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
			rm = new RoiManager();

		rm.reset();

		if (new File(settings.destKymosPathsFullImg + settings.currCell + ".zip").exists()) {
			rm.runCommand("open", settings.destKymosPathsFullImg + settings.currCell + ".zip");

			// Open synapses image
			ImagePlus synapseImage = new ImagePlus(
					settings.dir + settings.currCell + "-" + settings.lambdaSynapse + ".tif");

			for (int i = 0; i < rm.getCount(); i++) {
				Straightener stn = new Straightener();
				synapseImage.setRoi(rm.getRoi(i));

				ImagePlus ip = new ImagePlus(settings.currCell + "_Kymo" + (i + 1) + "-Synapses", stn.straightenStack(
						synapseImage, rm.getRoi(i), (int) Math.max(rm.getRoi(i).getStrokeWidth(), 1) * 3));

				// ImagePlus ip = new ImagePlus(settings.currCell + "_Kymo"
				// + (i + 1) + "-Synapses", procSynapses);
				filesAndFolders.calibrateAndSave(ip, settings.destSynapses);

				// When working on a stack of images as synapses representation,
				// sends back a MIP to locate them
				if (ip.getNSlices() > 1)
					ip = viewsGenerator.getMaxProjection(ip, 1, ip.getNSlices());

				new ImageConverter(ip).convertToRGB();

				ip.show();
				IJ.run("In [+]", "");
				IJ.run("In [+]", "");

				synapses = new ArrayList<Point>();
				// Double listener required to listen both the first time the ip
				// is activated (window) and additional times (canvas)
				ip.getWindow().addKeyListener(this);
				ip.getCanvas().addKeyListener(this);
				ip.getCanvas().addMouseListener(this);
				ip.getCanvas().addMouseMotionListener(this);

				WaitForUserDialog wfud = new WaitForUserDialog(
						"Point and click on synapses.\nUse the 9 key to decrease the radius, 0 to increase it.\nOnce done, click on Ok");
				wfud.show();

				ip.deleteRoi();
				ip.getCanvas().removeMouseListener(this);
				ip.getCanvas().removeMouseMotionListener(this);
				ip.getCanvas().removeKeyListener(this);

				ArrayList<String[]> out = new ArrayList<String[]>();
				out.add(new String[] { synapses.size() + "", ip.getWidth() * settings.calibXY + "",
						synapses.size() / (ip.getWidth() * settings.calibXY) + "" });
				out.add(null);
				out.add(new String[] { synapses.size() != 0 ? "Synapse nb\tx\ty" : "" });

				for (int j = 0; j < synapses.size(); j++) {
					Point currPoint = synapses.get(j);
					out.add(new String[] { (j + 1) + "", (int) currPoint.getX() + "", (int) currPoint.getY() + "" });
				}

				ip.setTitle(settings.currCell + "_Kymo" + (i + 1) + "-Localized_Synapses");
				filesAndFolders.calibrateAndSave(ip, settings.destSynapses);
				ip.close();

				utilities.saveAsCSV(
						new String[] { "Number of synapses", "Section's length (microns)",
								"Density (nb synapses/micron)" },
						out, settings.destSynapses + settings.currCell + "_Kymo" + (i + 1) + "-Synapses.xls");

				if (settings.isGCampExperiment)
					analyzeIntensityGCampSynapses(cell, synapses,
							settings.destSynapses + settings.currCell + "_Kymo" + (i + 1) + "-Synapses.zip");
			}

		}
	}

	/**
	 * Extracts the intensity data out of synapses over the the full times
	 * 
	 * @param cell
	 *            the cell being processed
	 * @param synapses
	 *            the list of synapses coordinates
	 * @param pathToImage
	 *            the path to the image on which to perform analysis
	 */
	public static void analyzeIntensityGCampSynapses(String cell, ArrayList<Point> synapses, String pathToImage) {
		RoiManager rm = RoiManager.getInstance();
		if (rm == null) {
			rm = new RoiManager();
		} else {
			rm.reset();
		}

		// Transforms coordinates to ROIs
		for (int i = 0; i < synapses.size(); i++) {
			Roi roi = new OvalRoi((int) (synapses.get(i).getX() - settings.synapseZone / 2),
					(int) (synapses.get(i).getY() - settings.synapseZone / 2), (int) settings.synapseZone,
					(int) settings.synapseZone);
			roi.setName("Synapse_" + (i + 1));
			rm.addRoi(roi);
		}

		String pathToROIs = settings.destGCampDataSynapses + settings.currCell + "_Synapses_ROIs.zip";
		String pathToSave = settings.destGCampDataXLS + settings.currCell + "_GCamp_Synapses_Fluo_Data.xls";

		rm.runCommand("Save", pathToROIs);
		GCampTools.saveDFonF(cell, pathToROIs, pathToImage, pathToSave);

		// Required to restore original ROIs being analysed
		rm.reset();
		rm.runCommand("open", settings.destKymosPathsFullImg + settings.currCell + ".zip");
	}

	/**
	 * Writes into the destination files the headers for allCellsData,
	 * allCellsSummary and allCellsSummaryWS data
	 */
	public static void writeXLSLabels() {
		String string2write = settings.LABELS[0];

		for (int i = 1; i < 6; i++)
			string2write += "\t" + settings.LABELS[i];

		// Write header for allCellsData
		utilities.writeStringToFile(settings.allCellsData, string2write + "\n");

		for (int i = 6; i < settings.LABELS.length; i++)
			string2write += "\t" + settings.LABELS[i];

		// Write header for allCellsSummary
		utilities.writeStringToFile(settings.allCellsSummary, string2write + "\n");

		// Write header for allCellsSummaryWS
		utilities.writeStringToFile(settings.allCellsSummaryWS, string2write + "\n");
	}

	/**
	 * Appends the data to the allCellsData, allCellsSummary and
	 * allCellsSummaryWS files, by reading the files generated for each cell
	 * 
	 * @param cell
	 *            the cell to process
	 */
	public static void writeXLSContent(String cell) {
		settings.setFolderAndCurrentCell(cell);

		String[] filesList = filesAndFolders.getFilteredNames(settings.destKymosDataXLS, settings.currCell + "_Kymo",
				".xls");
		for (int i = 0; i < filesList.length; i++) {
			String[] lines = utilities.readFileAsString(settings.destKymosDataXLS + filesList[i]).split("\n");
			for (int j = 1; j < lines.length; j++) {
				String[] columns = lines[j].split("\t");
				if (columns[0].indexOf("Summary") == -1) {
					String currKymo = columns[0].replace(settings.currCell + "_", "").replace(".tif", "");
					String toAppend = settings.currCell + "\t" + currKymo;
					for (int k = 1; k < columns.length; k++)
						toAppend += "\t" + columns[k];
					utilities.appendStringToFile(settings.allCellsData, toAppend);
				} else {
					String currKymo = columns[0].substring(columns[0].indexOf("_") + 1, columns[0].indexOf(".tif"));
					String toAppend = settings.currCell + "\t" + currKymo;
					for (int k = 1; k < columns.length; k++)
						toAppend += "\t" + columns[k];
					utilities.appendStringToFile(settings.allCellsSummary, toAppend);
					utilities.appendStringToFile(settings.allCellsSummaryWS, toAppend);
				}
			}
		}
	}

	/**
	 * Compiles the flux data for all cells into one file
	 */
	public static void writeXLSFlux() {
		String string2write = "";
		int nbLines = 0;

		for (int i = 0; i < settings.cellsList.length; i++) {
			settings.setFolderAndCurrentCell(settings.cellsList[i]);
			String[] lines = utilities.readFileAsString(settings.destFlux + settings.currCell + "_flux_data.xls")
					.split("\n");
			if (nbLines == 0)
				string2write = "Cell nb\t" + lines[0];
			string2write += "\n" + settings.currCell + "\t" + lines[1];
			nbLines++;
		}
		utilities.writeStringToFile(settings.allCellsFlux, string2write);
	}

	/**
	 * Compiles the synapses data for all cells into one file
	 */
	public static void writeXLSSynapses() {
		String string2write = "";
		int nbLines = 0;

		for (int i = 0; i < settings.cellsList.length; i++) {
			settings.setFolderAndCurrentCell(settings.cellsList[i]);
			String[] synapsesFiles = filesAndFolders.getFilteredNames(settings.destSynapses, settings.currCell, ".xls");
			for (int j = 0; j < synapsesFiles.length; j++) {
				String[] lines = utilities.readFileAsString(settings.destSynapses + synapsesFiles[j]).split("\n");
				if (nbLines == 0 && lines.length != 0)
					string2write = "Cell nb\tImage\t" + lines[0];

				// Add something only if something has to be added
				if (lines.length != 0) {
					string2write += "\n" + settings.currCell + "\t"
							+ synapsesFiles[j].replace(settings.currCell + "_", "").replace("-Synapses.xls", "") + "\t"
							+ lines[1];
					nbLines++;
				}
			}
		}
		if (string2write != "")
			utilities.writeStringToFile(settings.allCellsSynapses, string2write);
	}

	public static void tagAllDataWithSynapsesInfos() {
		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
			rm = new RoiManager();

		String[] allData = utilities.readFileAsString(settings.allCellsData).split("\n");

		allData[0] += "\tMovement_status\tNb_crossed_synapses\tSynapses_nb";

		for (int i = 1; i < allData.length; i++) {
			String[] line = allData[i].split("\t");

			settings.setFolderAndCurrentCell(line[0].replace("Cell", ""));
			String[] roisNames = utilities.concatenate(
					filesAndFolders.getFilteredNames(settings.destKymosROIs, settings.currCell, ".zip"),
					filesAndFolders.getFilteredNames(settings.destKymosROIs, settings.currCell, ".roi"));
			if (roisNames != null) {
				for (int j = 0; j < roisNames.length; j++) {
					rm.reset();
					rm.runCommand("open", settings.destKymosROIs + roisNames[j]);

					String synapsesFile = settings.destSynapses + roisNames[j].replace(".zip", "").replace(".roi", "")
							+ "-Synapses.xls";

					// Required to get the localisation of the central path
					String synapsesImg = synapsesFile.replace(".xls", ".zip");

					if (new File(synapsesFile).exists() && new File(synapsesImg).exists()) {
						Polygon synapses = utilities.synapsesFileToPolygon(synapsesFile);
						if (synapses.npoints != 0) {
							for (int k = 0; k < rm.getCount(); k++) {
								Polygon trail = rm.getRoi(k).getPolygon();

								ImagePlus ip = new ImagePlus(synapsesImg);

								String[][] diagSynapses = synapsesCrossed(synapses, trail, ip.getHeight());

								ip.flush();
								for (int l = 0; l < trail.npoints - 1; l++) {
									if (i < allData.length) {
										double speed = Double.parseDouble(allData[i].split("\t")[5]);
										String movementTag = speed < 0 ? "Inward" : "Outward";
										movementTag = Math.abs(speed) < settings.speedLimit ? "Pause" : movementTag;

										allData[i++] += "\t" + movementTag + "\t" + diagSynapses[l][0] + "\t"
												+ diagSynapses[l][1];
									}
								}
							}
							i--;
						}
					}
				}
			}
		}

		utilities.saveAsCSV(allData, settings.allCellsDataSynapses);
	}

	/**
	 * Based on a kymograph trail, defines for each point of the segment if it
	 * crosses a synapse, as represented by a circle of diameter synapseZone,
	 * counts the number of crossed synapses and their numbers
	 * 
	 * @param synapses
	 *            the synapses' positions as a polygon
	 * @param trail
	 *            the kymograph's trail as a polygon
	 * @return a 2D array, first dimension being the number of the segment,
	 *         second being the number of crossed synapses and their name
	 */
	private static String[][] synapsesCrossed(Polygon synapses, Polygon trail, int trailHeight) {
		String[][] out = new String[trail.npoints][2];

		int[] xSynapses = synapses.xpoints;
		int[] ySynapses = synapses.ypoints;
		int[] xTrail = trail.xpoints;
		int yTrail = (int) (trailHeight / 2.0); // Is the half image height as
												// straighten has been done

		/*
		 * DEBUG int[] tmp=Arrays.copyOf(xSynapses, xSynapses.length);
		 * Arrays.sort(tmp); int[] tmp2=Arrays.copyOf(trail.ypoints,
		 * trail.ypoints.length); Arrays.sort(tmp2);
		 * 
		 * ImagePlus ip=NewImage.createImage("tmp", (int) 1.1*tmp[tmp.length-1],
		 * (int) 1.1*tmp2[tmp2.length-1], 1, 24, NewImage.FILL_BLACK);
		 * 
		 * 
		 * ip.getProcessor().setColor(Color.green);
		 * ip.getProcessor().drawLine(0, yTrail, ip.getWidth(), yTrail);
		 * ip.getProcessor().setColor(Color.white);
		 * 
		 * for (int i = 0; i < trail.npoints-1; i++) { for (int j = 0; j <
		 * synapses.npoints; j++) { ip.getProcessor().drawOval(xSynapses[j]-4,
		 * ySynapses[j]-4, 8, 8); ip.getProcessor().drawLine(xTrail[i],
		 * trail.ypoints[i], xTrail[i+1], trail.ypoints[i+1]); } }
		 */

		for (int i = 0; i < trail.npoints - 1; i++) {
			int nSynapses = 0;
			String synapsesNames = "";

			for (int j = 0; j < synapses.npoints; j++) {
				int leftTrail = Math.min(xTrail[i], xTrail[i + 1]);
				int rightTrail = Math.max(xTrail[i], xTrail[i + 1]);

				boolean isCrossing = Math.abs(ySynapses[j] - yTrail) < settings.synapseZone
						/ 2.0 /**
								 * Shortest distance to trail is smaller than
								 * synapse zone radius
								 **/
						&& (leftTrail - settings.synapseZone / 2.0 < xSynapses[j]
								&& xSynapses[j] < rightTrail + settings.synapseZone
										/ 2.0); /**
												 * Synapse is on the good x
												 * range
												 **/

				if (isCrossing) {
					/*
					 * DEBUG ip.getProcessor().setColor(Color.red);
					 * ip.getProcessor().drawLine(xTrail[i], trail.ypoints[i],
					 * xTrail[i+1], trail.ypoints[i+1]);
					 * ip.getProcessor().drawLine(xTrail[i], yTrail,
					 * xTrail[i+1], yTrail);
					 */
					nSynapses++;
					synapsesNames += (synapsesNames.length() == 0 ? "" : ", ") + "Synapse " + (j + 1);
				}
			}
			out[i][0] = nSynapses + "";
			out[i][1] = synapsesNames;
		}
		/*
		 * DEBUG ip.show(); ip.resetDisplayRange(); ip.updateAndDraw();
		 */
		return out;
	}

	// ----------------------------------OLD
	// VERSION----------------------------------------------
	/**
	 * Based on a kymograph trail, defines for each segment if it crosses a
	 * synapse, counts the number of crossed synapses and their numbers
	 * 
	 * @param synapses
	 *            the synapses' positions as a polygon
	 * @param trail
	 *            the kymograph's trail as a polygon
	 * @return a 2D array, first dimension being the number of the segment,
	 *         second being the number of crossed synapses and their name
	 */
	private static String[][] synapsesCrossedBasedOnSegments(Polygon synapses, Polygon trail) {
		String[][] out = new String[trail.npoints - 1][2];

		int[] xSynapses = synapses.xpoints;
		int[] xTrail = trail.xpoints;

		for (int i = 1; i < trail.npoints; i++) {
			int nSynapses = 0;
			String synapsesNames = "";

			int xMin = Math.min(xTrail[i - 1], xTrail[i]) - settings.synapseZone / 2;
			int xMax = Math.max(xTrail[i - 1], xTrail[i]) + settings.synapseZone / 2;

			for (int j = 0; j < xSynapses.length; j++) {
				if (xSynapses[j] >= xMin && xSynapses[j] <= xMax) {
					nSynapses++;
					synapsesNames += (synapsesNames.length() == 0 ? "" : ", ") + "Synapse " + (j + 1);
				}
			}
			out[i - 1][0] = nSynapses + "";
			out[i - 1][1] = synapsesNames;
		}
		return out;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		ImagePlus ip = WindowManager.getCurrentImage();

		x = ip.getCanvas().offScreenX(e.getX());
		y = ip.getCanvas().offScreenY(e.getY());

		synapses.add(new Point(x, y));
		ip.getProcessor().setColor(Color.RED);
		ip.getProcessor().setLineWidth(2);
		ip.getProcessor().draw(new OvalRoi(x - (int) settings.synapseZone / 2, y - (int) settings.synapseZone / 2,
				settings.synapseZone, settings.synapseZone));
		ip.updateImage();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		ImagePlus ip = WindowManager.getCurrentImage();

		x = ip.getCanvas().offScreenX(e.getX());
		y = ip.getCanvas().offScreenY(e.getY());

		ip.setRoi(new OvalRoi(x - synapseRadius / 2, y - synapseRadius / 2, synapseRadius, synapseRadius));
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == '9')
			synapseRadius--;
		if (e.getKeyChar() == '0')
			synapseRadius++;
		synapseRadius = synapseRadius < 2 ? 2 : synapseRadius;
		WindowManager.getCurrentImage()
				.setRoi(new OvalRoi(x - synapseRadius / 2, y - synapseRadius / 2, synapseRadius, synapseRadius));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	}
}
