/**
  * kymoGraphs.java v1, 3 août 2016
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

import java.awt.Color;
import java.io.File;

import KymoButler.KymoButlerIO;
import KymoButler.KymoButlerResponseParser;
import Utilities.kymograph.analyseKymo;
import Utilities.kymograph.buildLUT;
import Utilities.kymograph.kymograph;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import utilities.filesAndFolders;

/**
 * This class aims at dealing with kymographs handeling
 * @author Fabrice P Cordelieres
 *
 */
public class kymographs {
	/** Stores the path to the files and folders to be used **/
	filesAndFolders faf=null;
	
	
	/**
	 * Sets the different paths for file to be used when dealing with kymographs
	 * @param rootPath the root path
	 * @param name the cell name
	 */
	public kymographs(String rootPath, String name){
		faf=new filesAndFolders(rootPath, name);
	}
	
	
	/**
	 * Interacts with the user to get the path along which the vesicules are moving
	 */
	public void getPath(){
		if(new File(faf.imagePath_overview).exists() && new File(faf.imagePath_crop_proj).exists()){
			ImagePlus ipOverview=new ImagePlus(faf.imagePath_overview);
			ipOverview.show();
			ImagePlus ipProj=new CompositeImage(new ImagePlus(faf.imagePath_crop_proj));
			ipProj.deleteRoi();
			ipProj.setDisplayMode(IJ.COMPOSITE);
			ipProj.show();
			
			RoiManager rm=RoiManager.getInstance();
			if(rm==null){
				rm=new RoiManager();
				rm.setVisible(true);
			}else{
				rm.reset();
				rm.setVisible(false);
				rm.setVisible(true);
			}
			
			IJ.setTool("polyline");
			
			WaitForUserDialog wfud = new WaitForUserDialog(
					"Using the segmented line tool,\n"
					+ "draw the vesicles' paths ON THE SMALLER IMAGE,\n"
					+ "from the cell body towards the periphery,\n"
					+ "adjust the line width by double-clicking on the drawing tool,\n"
					+ "then click on Ok");
			wfud.show();
			
			Roi roi=ipProj.getRoi();
			if(roi!=null){
				new File(faf.kymoPath).mkdirs();
				RoiEncoder.save(roi, faf.kymoPath_ROI);
			}
			ipOverview.close();
			ipProj.close();
			System.gc();
		}
	}
	
	/**
	 *Opens the user defined path along which the kymograph will be drawn and creates the kymograph
	 */
	public void createKymograph(){
		if(new File(faf.kymoPath_ROI).exists() && new File(faf.imagePath_crop).exists()){
			Roi roi=RoiDecoder.open(faf.kymoPath_ROI);
			int stroke=(int) roi.getStrokeWidth();
			ImagePlus[] cropImage=ChannelSplitter.split(new ImagePlus(faf.imagePath_crop));
			ImagePlus[] kymographs=new ImagePlus[cropImage.length+1];
			
			//Retrieves the kymograph, using the roi's stroke width for vesicules' image or the max dimensions of the image otherwise
			for(int i=0; i<cropImage.length; i++){
				//float originalRoiWidth=roi.getStrokeWidth();
				//if(i>0) roi.setStrokeWidth(Math.max(cropImage[i].getWidth(), cropImage[i].getHeight()));
				kymographs[i]=new kymograph(cropImage[i], roi).getKymograph(stroke);
				//roi.setStrokeWidth(originalRoiWidth);
			}
			
			//Retrieve the kymograph for synapses by blackening the roi, then extending its width to 5 times its former value
			for(int i=1; i<=cropImage[cropImage.length-1].getNSlices(); i++){
				cropImage[cropImage.length-1].setSlice(i);
				cropImage[cropImage.length-1].getProcessor().setColor(Color.BLACK);
				cropImage[cropImage.length-1].getProcessor().draw(roi);
			}
			kymographs[cropImage.length]=new kymograph(cropImage[cropImage.length-1], roi).getKymograph(stroke*5);
			roi.setStrokeWidth(stroke);
			
			//Required to transfer the file info elements containing the original coordinates of the path
			String infoString=(String) kymographs[0].getProperty("Info");
			
			ImagePlus out=HyperStackConverter.toHyperStack(new Concatenator().concatenate(kymographs, false), kymographs.length, 1, 1);
			out.setProperty("Info", infoString);
			out.setTitle(faf.name+"_kymo");
			
			//Reset the display for each channel
			for(int i=1; i<=out.getNChannels(); i++){
				out.setC(i);
				out.resetDisplayRange();
			}
			
			new FileSaver(out).saveAsZip(faf.kymoPath_kymo);
			
			roi=null;
			cropImage=null;
			kymographs=null;
			out.close();
			System.gc();
		}
	}
	
	/**
	 * Interacts with the user to get the segments along which the vesicules are moving
	 */
	public void getSegmentsManually(){
		if(new File(faf.kymoPath_kymo).exists()){
			RoiManager rm=RoiManager.getInstance();
			if(rm==null){
				rm=new RoiManager();
				rm.setVisible(true);
			}else{
				rm.reset();
				rm.setVisible(false);
				rm.setVisible(true);
			}
			
			ImagePlus kymograph=new CompositeImage(new ImagePlus(faf.kymoPath_kymo));
			kymograph.setDisplayMode(IJ.GRAYSCALE);
			kymograph.show();
			
			IJ.setTool("polyline");
			WaitForUserDialog wfud = new WaitForUserDialog(
					"Using the segmented line tool,\n"
					+ "draw each single vesicle' path, from top to bottom,\n"
					+ "add in turn each path to the ROI Manager by pressing 't',\n"
					+ "then click on Ok");
			wfud.show();
			
			if(rm.getCount()>0){
				//Saves the segments
				rm.runCommand("Save", faf.kymoPath_segments);
				rm.reset();
			}
			
			kymograph.close();
			System.gc();
		}
	}
	
	/**
	 * Automatically get the segments along which the vesicules are moving using KymoButler
	 * @param URL KymoButler API URL, as a String
	 * @param threshold detection threshold for segments, as a float
	 * @param minimumSize minimum number of overall traveled pixels to consider the segment, as a float
	 * @param minimumFrames minimum number of frames composing the segment to consider it, as a float
	 */
	public void getSegmentsUsingKymoButler(String URL, float threshold, float minimumSize, float minimumFrames){
		if(new File(faf.kymoPath_kymo).exists()){
			RoiManager rm=RoiManager.getRoiManager();
			rm.reset();
			
			ImagePlus kymograph=new CompositeImage(new ImagePlus(faf.kymoPath_kymo));
			kymograph.setC(1);
			
			kymograph=new ImagePlus("Kymograph", kymograph.getChannelProcessor());
			kymograph.resetDisplayRange();
			
			KymoButlerIO kbio=new KymoButlerIO();
			kbio.setKymograph(kymograph);
			kbio.setURL(URL);
			kbio.setThreshold(threshold);
			kbio.setMinimumSize(minimumSize);
			kbio.setMinimumFrames(minimumFrames);
			
			String response=kbio.getAnalysisResults();
			
			boolean debug=Prefs.get("KymoButler_debug.boolean", false); //Check the debug box in KymoButler4IJ to activate saving the JSON files
			
			if(response==null) {
				IJ.log("Process cancelled, either by server or by user");
			}else {
				if(KymoButlerResponseParser.isJSON(response)){
					KymoButlerResponseParser pkr=new KymoButlerResponseParser(response);
					pkr.pushRoisToRoiManager(true);
				}else {
					IJ.log("The response doesn't seem to be properly formatted");
				}
				if(debug) kbio.saveAnalysisResults(faf.kymoPath+faf.name+"_KymoButler.JSON");
			}
			
			if(rm.getCount()>0){
				//Saves the segments
				rm.runCommand("Save", faf.kymoPath_segments);
				rm.reset();
			}
			
			kymograph.close();
			System.gc();
		}
	}
	
	/**
	 * Requests the user to draw the segments along which the vesicules are moving, performs the analysis and saves the data
	 * @param minSpeed the minimum speed to be considered for movement
	 * @param lineWidth line width for output
	 */
	public void analyseKymograph(double minSpeed, int lineWidth){
		if(new File(faf.kymoPath_kymo).exists() && new File(faf.kymoPath_segments).exists()){
			RoiManager rm=RoiManager.getInstance();
			if(rm==null){
				rm=new RoiManager();
				rm.setVisible(true);
			}else{
				rm.reset();
				rm.setVisible(false);
				rm.setVisible(true);
			}
			rm.runCommand("Open", faf.kymoPath_segments);
			
			if(rm.getCount()>0){
				//Open the kymograph
				ImagePlus kymograph=new CompositeImage(new ImagePlus(faf.kymoPath_kymo));
				
				String infoString=(String) kymograph.getProperty("Info");
				ImagePlus[] kymographArray=ChannelSplitter.split(kymograph);
				kymograph.close();
				kymographArray[0].setProperty("Info", infoString);
				kymographArray[0].setTitle(faf.name);
				
				analyseKymo ak=new analyseKymo(kymographArray[0], false, minSpeed, true);
				
				//Saves the numerical data
				ak.logResults(true);
				ResultsTable rt=ResultsTable.getResultsTable();
				IJ.selectWindow("Results");
				rt.save(faf.kymoPath_data);
				IJ.run("Close");
				
				//Saves the extrapolated coordinates
				ak.logCoord();
				IJ.selectWindow("Extrapolated coordinates from "+faf.name);
				IJ.saveAs("Results", faf.kymoPath_coord);
				IJ.run("Close");
				
				
				//Saves the annotated kymograph
				ImagePlus segments=ChannelSplitter.split(ak.getImage(lineWidth))[0];
				ImageConverter.setDoScaling(true);
				new ImageConverter(segments).convertToGray32();
				
				ImagePlus[] outArray=new ImagePlus[kymographArray.length+1];
				for(int i=0; i<kymographArray.length; i++) outArray[i]=kymographArray[i];
				
				outArray[kymographArray.length]=segments;
				kymographArray=null;
				segments.close();
				
				CompositeImage annotatedKymo=(CompositeImage) HyperStackConverter.toHyperStack(new Concatenator().concatenate(outArray, false), outArray.length, 1, 1);
				annotatedKymo.setC(annotatedKymo.getNChannels());
				annotatedKymo.setChannelLut(new buildLUT().getLUT(buildLUT.KYMO));
				annotatedKymo.setProperty("Info", infoString);
				annotatedKymo.resetDisplayRanges();
				annotatedKymo.setTitle(faf.name+"_kymo_annotated");
				
				new FileSaver(annotatedKymo).saveAsZip(faf.kymoPath_annotated);
				
				annotatedKymo.close();
				rm.reset();
			}
			System.gc();
		}
	}
}
