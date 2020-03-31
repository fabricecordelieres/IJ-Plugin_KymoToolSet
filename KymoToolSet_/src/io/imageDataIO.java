/**
  * inputOutput.java v1, 1 août 2016
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

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;

import analysis.GUI;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.RGBStackConverter;
import ij.plugin.RoiEnlarger;
import ij.plugin.ZProjector;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ndFile.ndFile;
import processing.imagePreProcessing;
import rgnFile.rgnFile;
import utilities.filesAndFolders;

/**
 * This file handles multiple type of input files, generating same outputs: 1-A
 * composite of the full frame image. 2-A cropped version if the full frame,
 * relative to the FRAP zone. To ease structures' delineation, the FRAP zone is
 * enlarged by 20 pixels prior to cropping.
 * 
 * @author Fabrice P Cordelieres
 *
 */
public class imageDataIO {
	/** Folder where the files are stored **/
	String dir = "";

	/** Part of the name relative to the vesicules' wavelength **/
	String waveVesicules = "";

	/** Part of the name relative to the second channel's wavelength **/
	String waveSecondChannel = "";
	
	/** Calibration for the data **/
	Calibration cal=new Calibration();

	/** Experiment's type **/
	int type = -1;
	
	/** Top-Hat filtering radius **/
	int topHatRadius=-1;
	
	/** Minimum wavelet radius **/
	int minWaveletRadius=-1;
	
	/** Maximum wavelet radius **/
	int maxWaveletRadius=-1;
	
	/** Enlarge value to be used to define the analysis region **/
	public static final int ENLARGE=20;		

	/** Type: none **/
	public static final int TRP_NONE = -1;

	/** Type: transport and synapses **/
	public static final int TRP_SYNAPSE = 0;

	/** Type: transport and GCaMP **/
	public static final int TRP_GCaMP = 1;
	
	/** Type: transport and GCaMP+uncaging **/
	public static final int TRP_GCaMP_UNCAGING = 2;

	/**
	 * Generates a new input/output object, based on an input directory and part
	 * of a filename for vesicules
	 * 
	 * @param dir the input directory
	 * @param waveVesicles the wavelength for vesicules
	 */
	public imageDataIO(String dir, String waveVesicules, String waveSecondChannel, Calibration cal) {
		this.dir = dir;
		if(!dir.endsWith(File.separator)) this.dir=this.dir+File.separator;
		this.waveVesicules = waveVesicules;
		this.waveSecondChannel=waveSecondChannel;
		this.cal=cal;
	}

	/** 
	 * Tries to identify the type of experiment, based on the files:
	 * @param basename the basename of the cell to analyze (CellXX-wave.tif for TRP_SYNAPSE, basename_wXXSPI waveYYY.TIF for a TRP_GCaMP)
	 * @return TRP_SYNAPSE (0) for transport and synapses, TRP_GCaMP (1) for transport and GCaMP, TRP_NONE (-1) otherwise
	 */
	public int getType(String basename){
		basename=basename.replace(".tif", "").replace(".TIF", "").replace(".nd", "").replace(".ND", "");
		
		//Check if this is regular transport+synapses experiment
		File testTrp_Synapse=new File(dir+basename+"-"+waveVesicules+".tif");
		
		//Check if this is regular transport+GCaMP experiment
		File testTrp_GCaMP=new File(dir+basename+".nd");
		
		//Check if this is regular transport+GCaMP+uncaging experiment
		File testTrp_GCaMP_uncaging=new File(dir+basename+"_uncaging.rgn");
		
		if(testTrp_Synapse.exists()) return TRP_SYNAPSE;
		if(testTrp_GCaMP.exists() && !testTrp_GCaMP_uncaging.exists()) return TRP_GCaMP;
		if(testTrp_GCaMP.exists() && testTrp_GCaMP_uncaging.exists()) return TRP_GCaMP_UNCAGING;
		
		return TRP_NONE;
	}
	
	/**
	 * Sets the preprocessing parameters to be applied to the vesicules' image
	 * @param topHatRadius top-hat filtering radius
	 * @param minWaveletRadius minimum wavelet radius
	 * @param maxWaveletRadius maximum wavelet radius
	 */
	public void setPreProcessingParameters(int minWaveletRadius, int maxWaveletRadius, int topHatRadius){
		this.minWaveletRadius=minWaveletRadius;
		this.maxWaveletRadius=maxWaveletRadius;
		this.topHatRadius=topHatRadius;
	}
	
	/**
	 * Create an ImagePlus from the input basename, assuming the wavename for vesicules and the directory to have been defined earlier
	 * @param basename should be in the form CellXX for TRP_SYNAPSE experiments, the name of the nd file for TRP_GCaMP experiments
	 * @param nPreBleachImg number of pre-bleach images
	 * @return the ImagePlus if the process went well, a null pointer exception otherwise
	 */
	public ImagePlus buildComposite(String basename, int nPreBleachImg){
		basename=basename.replace(".tif", "").replace(".TIF", "").replace(".nd", "").replace(".ND", "").replace(".rgn", "").replace(".RGN", "");
		type=getType(basename);
		
		ImagePlus ip=null;
		
		switch(type){
			case TRP_SYNAPSE: ip=buildComposite_Trp_Synapse(basename); break;
			case TRP_GCaMP: ip=buildComposite_Trp_GCaMP(basename); break;
			case TRP_GCaMP_UNCAGING: ip=buildComposite_Trp_GCaMP(basename); break;
			default: GUI.log.logInfo("Unrecognised experiment type: "+basename);
		}
		
		return imageCleanUp(ip, nPreBleachImg);
	}
	
	/**
	 * Suppresses all black slices as well as the pre-bleach timepoints
	 * @param ip the ImagePlus to process
	 * @param nPreBleachImg the number of pre-bleach images
	 * @return a filtered ImagePlus
	 */
	public ImagePlus imageCleanUp(ImagePlus ip, int nPreBleachImg){
		if(ip!=null){
			Roi frapROI=ip.getRoi();
			String title=ip.getTitle();
			int nSlices=ip.getNSlices();
			int nChannels=ip.getNChannels();
			int startSlice=nPreBleachImg+1;
			ip.deleteRoi();
			
			for(int i=nPreBleachImg+1; i<=nSlices; i++){
				ip.setZ(i);
				double mean=1.0;
				for(int j=1; j<=nChannels; j++){
					ip.setC(j);
					mean*=ip.getStatistics().mean;
				}
				if(mean==0.0) startSlice=i+1;
			}
			
			ip=new Duplicator().run(ip, 1, nChannels, startSlice, nSlices, 1, 1);
					
			ip.setRoi(frapROI);
			ip.setTitle(title);
		}
		
		return ip;
	}
	
	/**
	 * Builds a composite, considering the dataset to be a TRP_SYNAPSE experiment
	 * NB the 2 expected waves are 488 and 561
	 * @param basename
	 * @return
	 */
	public ImagePlus buildComposite_Trp_Synapse(String basename){
		basename=basename.replace(".tif", "").replace(".TIF", "");
		
		String vesiculesPath=dir+basename+"-"+waveVesicules+".tif";
		String synapsesPath=dir+basename+"-"+waveSecondChannel+".tif";
		String roiFRAPPath=dir+basename+"-ROI.rgn";
		
		if(!new File(vesiculesPath).exists()) GUI.log.logInfo("Vesicules' image does not exist: "+vesiculesPath+" check the wavelength you've entered and try again");
		if(!new File(synapsesPath).exists()) GUI.log.logInfo("Synapses' image does not exist: "+synapsesPath+ " check the wavelength you've entered and try again");
		if(!new File(roiFRAPPath).exists()) GUI.log.logInfo("FRAP Roi does not exist: "+roiFRAPPath);
		
		ImagePlus merge=null;
		if(new File(vesiculesPath).exists() && new File(synapsesPath).exists() && new File(roiFRAPPath).exists()){
			ImagePlus vesicules=new ImagePlus(vesiculesPath);
			ImagePlus synapses=new ImagePlus(synapsesPath);
			
			addSlices(vesicules, synapses.getNSlices());
			addSlices(synapses, vesicules.getNSlices());
			
			//Merge the two stacks
			merge=new Concatenator().concatenate(vesicules, synapses, false);
			merge=HyperStackConverter.toHyperStack(merge, 2, merge.getStackSize()/2, 1, "xyzct", "composite");
			
			//Open the ROI
			rgnFile roi=new rgnFile(dir, basename+"-ROI.rgn");
			if(roi.nRois<1) throw new NullPointerException("The FRAP Roi file is empty");
			merge.setRoi(roi.rois.get(0).getIJRoifromMMRoi());
			
			//Modifies the title
			merge.setTitle(basename);
			
			//Calibrates the image
			merge.setCalibration(cal);
		}
		
		return merge;
	}
	
	/**
	 * Builds a composite, considering the dataset to be a TRP_SYNAPSE experiment
	 * NB the 2 expected waves are 491 and 561
	 * @param basename
	 * @return
	 */
	public ImagePlus buildComposite_Trp_GCaMP(String basename){
		basename=basename.replace(".nd", "").replace(".ND", "");
		
		String ndPath=dir+basename+".nd";
		String roiFRAPPath=dir+basename+".rgn";
		
		if(!new File(ndPath).exists()) GUI.log.logInfo("The nd file does not exist: "+ndPath);
		if(!new File(roiFRAPPath).exists()) GUI.log.logInfo("FRAP Roi does not exist: "+roiFRAPPath);
		
		ImagePlus merge=null;
		if(new File(ndPath).exists() && new File(roiFRAPPath).exists()){
			ndFile nd=new ndFile(dir, basename+".nd");
			
			String[] waveNames=nd.getWaveNames();
			String waveNameVesicules="";
			String waveNameSecondChannel="";
			for(int i=0; i<waveNames.length; i++){
				if(waveNames[i].contains(waveVesicules)) waveNameVesicules=waveNames[i];
				if(waveNames[i].contains(waveSecondChannel)) waveNameSecondChannel=waveNames[i];
			}
			
			ImagePlus vesicules=buildWaveStack(nd, waveNameVesicules);
			ImagePlus synapses=buildWaveStack(nd, waveNameSecondChannel);
			
			//Merge the two stacks
			merge=new Concatenator().concatenate(vesicules, synapses, false);
			merge=HyperStackConverter.toHyperStack(merge, 2, merge.getStackSize()/2, 1, "xyzct", "composite");
					
			//Open the ROI
			rgnFile roi=new rgnFile(dir, basename+".rgn");
			if(roi.nRois<1) GUI.log.logInfo("The FRAP Roi file is empty in folder "+dir+", file "+basename+".rgn");
			merge.setRoi(roi.rois.get(0).getIJRoifromMMRoi());
					
			//Modifies the title
			merge.setTitle(basename);
			
			//Calibrates the image
			merge.setCalibration(cal);
		}
				
		return merge;
	}
	
	/**
	 * Build a stack representing a single channel from a nd serie
	 * @param nd the nd file from which informations should be extracted
	 * @param wave the channel to extract
	 * @return and image, as an ImagePlus
	 */
	public ImagePlus buildWaveStack(ndFile nd, String wave){
		String basename=nd.BaseName;
		String dir=nd.Directory;
		int timePoints=nd.NTimePoints;
		
		//Retrieves the wave number
		String[] waves=nd.getWaveNames();
		int waveNb=0;
		for(int i=0; i<waves.length; i++) if(waves[i]==wave) waveNb=i+1;
		
		ImagePlus out=null;
		ImageStack outStack=null;
		ImagePlus ip=null;
		for(int i=1; i<=timePoints; i++){
			String currImg=basename+"_w"+waveNb+wave+"_t"+i+".TIF";
			
			if(!new File(dir+currImg).exists()) GUI.log.logInfo("This file does not exist: "+currImg+".\nCheck the wavelength settings you've entered in the dialog box.");
			ip=new ImagePlus(dir+currImg);
			
			if(out==null){
				out=ip;
			}else{
				outStack=out.getImageStack();
				outStack.addSlice(ip.getProcessor());
				out=new ImagePlus(wave, outStack);
				outStack=null;
			}
			ip.close();
		}
		
		return out;
	}
	
	/**
	 * Export the original data and the crop version as a composite, to which is overlaid the ROI
	 * @param basename the basename for the dataset
	 * @param nPreBleachImg number of pre-bleach images
	 * @param saveCrop true if the cropped version has to be saved
	 */
	public void saveImages(String basename, int nPreBleachImg){
		ImagePlus ip=buildComposite(basename, nPreBleachImg);
		
		if(ip!=null){
			String name="";
			
			switch(type){
				case TRP_SYNAPSE: basename=basename.replace(".tif", "").replace(".TIF", ""); name=basename; break; 
				case TRP_GCaMP: basename=basename.replace(".nd", "").replace(".ND", ""); name="Cell"+basename.substring(basename.toLowerCase().indexOf("_cell")+5).replace(" ", ""); break;
				case TRP_GCaMP_UNCAGING: basename=basename.replace(".nd", "").replace(".ND", ""); name="Cell"+basename.substring(basename.toLowerCase().indexOf("_cell")+5).replace(" ", ""); break;
				default: GUI.log.logInfo("Unrecognised experiment type: "+basename);
			}
			
			if(type==TRP_SYNAPSE || type==TRP_GCaMP || type==TRP_GCaMP_UNCAGING){
				filesAndFolders faf=new filesAndFolders(dir, name);
				if(!new File(faf.imagePath).exists()) new File(faf.imagePath).mkdirs();
				
				//Save the composite
				resetHyperstackDisplayRange(ip);
				ip.setTitle(name);
				new FileSaver(ip).saveAsZip(faf.imagePath_composite);
				
				//Save the first image as jpg
				saveFirstSliceAsJpg(ip, faf.imagePath, faf.name);
				
				//Save the cropped version of the hyperstack
				ImagePlus cropped=saveCroppedStack(ip, faf.imagePath, faf.name);
				
				//Performs the MIP of the cropped stack
				saveProjectionOfCroppedStack(cropped, faf.imagePath, faf.name);
				
				//For TRP_GCaMP_UNCAGING experiments, saves the shifted synapses ROIs
				if(type==TRP_GCaMP_UNCAGING){
					if(!new File(faf.GCaMPUncagingPath).exists()) new File(faf.GCaMPUncagingPath).mkdirs();
					ImagePlus imageForSynapses=new ImagePlus(faf.imagePath_composite);
					saveUncagingRois(imageForSynapses, basename, faf.imagePath, faf.GCaMPUncagingPath_rois);
					imageForSynapses.close();
				}
				
				cropped.close();
				ip.close();
			}
		}else{
			GUI.log.logInfo("The composite could not be built (basename: "+basename+", nPreBleachImg: "+nPreBleachImg+")");			
		}
		
		System.gc();
	}
	
	/**
	 * Saves the first slice of an hyperstack as a jpg, including the ROI and the scale bar
	 * @param ip the input ImagePlus
	 * @param outPath the output path
	 * @param name the output file name
	 */
	public void saveFirstSliceAsJpg(ImagePlus ip, String outPath, String name){
		name=name.replace(".jpg", "").replace(".JPG", "");
		Roi frapROI=ip.getRoi();
		ip.deleteRoi();
		ImagePlus jpg=new Duplicator().run(ip, 1, ip.getNChannels(), 1, 1, 1, 1);
		resetHyperstackDisplayRange(jpg);
		RGBStackConverter.convertToRGB(jpg);
		ip.setRoi(frapROI);
		jpg.restoreRoi();
		jpg.getProcessor().setColor(Color.WHITE);
		jpg.getProcessor().draw(frapROI);
		jpg.setCalibration(cal);
		IJ.run(jpg, "Scale Bar...", "width=5 height=4 font=14 color=White background=None location=[Lower Right] bold");
		
		new FileSaver(jpg).saveAsJpeg(outPath+name+".jpg");
		jpg.close();
		System.gc();
	}
	
	/**
	 * Saves a cropped version of the hyperstack, including the ROI
	 * @param ip the input ImagePlus
	 * @param outPath the output path
	 * @param name the output file name
	 * @return the cropped stack as an ImagePlus
	 */
	public ImagePlus saveCroppedStack(ImagePlus ip, String outPath, String name){
		Roi frapROI=ip.getRoi();
		if(frapROI==null) GUI.log.logInfo("No ROI found on the image: "+name);
		Rectangle boundingBox=frapROI.getBounds();
		
		Roi enlargedFrapROI=RoiEnlarger.enlarge(frapROI, ENLARGE);
		Rectangle boundingBoxEnlarged=enlargedFrapROI.getBounds();
		
		ImagePlus out=new ImagePlus(name+"_crop", ip.getImageStack().crop(boundingBoxEnlarged.x, boundingBoxEnlarged.y, 0, boundingBoxEnlarged.width, boundingBoxEnlarged.height, ip.getStackSize()));
		out=HyperStackConverter.toHyperStack(out, 2, ip.getStackSize()/2, 1, "xyczt", "composite");
		
		//Optional: performs pre-processing
		if(topHatRadius!=-1 || (minWaveletRadius!=-1 && maxWaveletRadius!=-1)){
			ImagePlus[] tmp=ChannelSplitter.split(out);
			tmp[0]=imagePreProcessing.waveletAndTopHat(tmp[0], minWaveletRadius, maxWaveletRadius, topHatRadius);
			int nChannels=tmp.length;
			out=new Concatenator().concatenate(tmp, false);
			out=HyperStackConverter.toHyperStack(out, nChannels, out.getStackSize()/nChannels, 1, "xyzct", "composite");
		}
				
		frapROI.setLocation(boundingBox.x-boundingBoxEnlarged.x, boundingBox.y-boundingBoxEnlarged.y);
		out.setRoi(frapROI);
		out.setCalibration(cal);
		
		resetHyperstackDisplayRange(out);
		out.setTitle(name+"_crop");
		new FileSaver(out).saveAsZip(outPath+name+"_crop.zip");
		
		return out;
	}
	
	/**
	 * Saves the shifted version of the synapses' Roi in case of uncaging experiments
	 * @param ip the input ImagePlus containing the FRAP Roi
	 * @param basename the dataset's basename
	 * @param inPath the input directory
	 * @param outPath the full output path, including the file-name
	 */
	public void saveUncagingRois(ImagePlus ip, String basename, String inPath, String outPath){
		Roi frapROI=ip.getRoi();
		if(frapROI==null) GUI.log.logInfo("No ROI found on the image: "+ip.getTitle());
		
		Roi enlargedFrapROI=RoiEnlarger.enlarge(frapROI, ENLARGE);
		Rectangle boundingBoxEnlarged=enlargedFrapROI.getBounds();
		
		if(getType(basename)==TRP_GCaMP_UNCAGING){
	 		rgnFile synapses_uncaging_MM=new rgnFile(dir, basename.replace(".nd", "")+"_uncaging.rgn");
	 		if(synapses_uncaging_MM.nRois==0){
	 			GUI.log.logInfo("No ROI found in rgn file: "+basename.replace(".nd", "")+"_uncaging.rgn");
	 		}else{
	 			RoiManager rm=RoiManager.getInstance();
	 			if(rm==null){
	 				rm=new RoiManager();
	 			}else{
	 				rm.reset();
	 			}
	 			
	 			for(int i=0; i<synapses_uncaging_MM.nRois; i++){
	 				Roi currRoi=synapses_uncaging_MM.rois.get(i).getIJRoifromMMRoi();
	 				Rectangle boundingBoxSynapse=currRoi.getBounds();
	 				currRoi.setLocation(boundingBoxSynapse.x-boundingBoxEnlarged.x, boundingBoxSynapse.y-boundingBoxEnlarged.y);
	 				currRoi.setName("Synapse_"+(i+1));
	 				rm.addRoi(currRoi);
	 			}
	 			rm.runCommand("Save", outPath);
	 		}
	 	}
	}
	
	/**
	 * Saves the projection of the cropped hyperstack, including the ROI
	 * @param ip the input ImagePlus
	 * @param outPath the output path
	 * @param name the output file name
	 */
	public void saveProjectionOfCroppedStack(ImagePlus ip, String outPath, String name){
		Roi frapROI=ip.getRoi();
		if(frapROI==null) GUI.log.logInfo("No ROI found on the image: "+name);
		
		ZProjector zproj=new ZProjector(ip);
		zproj.setStartSlice(1);
		zproj.setStopSlice(ip.getNSlices());
		zproj.setMethod(ZProjector.MAX_METHOD);
		zproj.doHyperStackProjection(true);
		ImagePlus out=zproj.getProjection();
		
		out.setRoi(frapROI);
		out.setCalibration(cal);
		
		resetHyperstackDisplayRange(out);
		out.setTitle(name+"_crop_proj");
		new FileSaver(out).saveAsZip(outPath+name+"_crop_proj.zip");
		out.close();
		System.gc();
	}
	
	
	/**
	 * Adds slices at the end of the stack by replicating the last slice, until the nImages parameters is reached for stack's size
	 * @param ip the ImagePlus to modify
	 * @param nImages the targetted number of images
	 */
	public void addSlices(ImagePlus ip, int nImages){
		int numberOfSlicesToAdd=nImages-ip.getNSlices();
		ip.setSlice(ip.getNSlices());
		
		//Get the last image of the stack, keeps it into buffer
		ImageProcessor iproc=ip.getProcessor();
		
		//Get ImageStack and adds the slices at its end
		ImageStack is=ip.getImageStack();
		for(int i=0; i<numberOfSlicesToAdd; i++) is.addSlice(iproc);
		
		ip.setStack(is);
	}
	
	/**
	 * Resets the min max range of the input hyperstack
	 * @param ip the input hyperstack
	 */
	public void resetHyperstackDisplayRange(ImagePlus ip){
		ip.setZ(ip.getNSlices()/2);
		for(int i=1; i<=ip.getNChannels(); i++){
			ip.setC(i);
			ip.resetDisplayRange();
		}
	}
}
