package steps;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.io.RoiEncoder;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.RGBStackConverter;
import ij.plugin.RoiEnlarger;
import ij.plugin.ZProjector;
import ij.plugin.filter.AVI_Writer;
import ij.plugin.frame.RoiManager;
import ij.process.EllipseFitter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;
import rgnFile.rgnFile;
import settings.filesAndFolders;
import settings.settings;
import utilities.utilities;

/**
 * This class includes all methods to generate image-based output
 * @author fab
 *
 */
public class viewsGenerator {
	
	/**
	 * Performs automatic serial projections of the current image, using 
	 * steps defined by settings.incProj
	 */
	public static void doSerialProjections(){
		ImagePlus ip=WindowManager.getCurrentImage();
		int nZ=ip.getStackSize();
		
		settings.incProj=nZ-1; //nZ-settings.bleachTP+1; //TODO Added to avoid having to make steps
		
		for(int i=1 /*settings.bleachTP+1*/; i<=nZ; i+=settings.incProj){
			ImagePlus out=getMaxProjection(ip, i, i+settings.incProj);
			
			out.setTitle(ip.getTitle()+"_"+i+"-"+i+settings.incProj);
			out.setCalibration(settings.getCalibration());
			filesAndFolders.calibrateAndSave(out, settings.destProj);
			out.flush();
			System.gc();
		}
	}
	
	/**
	 * Performs a manual maximum intensity projection on the first image of the "cell" folder,
	 * asking the user for start/end slices
	 * @param cell the cell to process
	 */
	public static void doManualProjection(String cell){
		settings.setFolderAndCurrentCell(cell);
		String[] list=filesAndFolders.getFilteredNames(settings.destStkProc, settings.currCell, ".zip");
		
		if(list.length>0){
			ImagePlus ip=new ImagePlus(settings.destStkProc+list[0]);
			ip.show();
			
			new WaitForUserDialog("Instructions", "1-Navigate through the stack\n2-Note the top/bottom slice number\n3-Click on Ok\n4-Enter the two values in the following window").show();
			
			GenericDialog gd=new GenericDialog("Projection parameters");
			gd.addSlider("Start_time", 1, ip.getStackSize(), 1/*Math.max(1,  settings.bleachTP+1)*/);
			gd.addSlider("End_time", 1, ip.getStackSize(), ip.getStackSize());
			gd.showDialog();
			
			int start=(int) gd.getNextNumber();
			int end=(int) gd.getNextNumber();
			
			ImagePlus out=getMaxProjection(ip, start, end);
			out.setTitle(list[0].replace(".zip", "")+"_"+start+"-"+end);
			out.setCalibration(ip.getCalibration());
			filesAndFolders.calibrateAndSave(out, settings.destProj);
			filesAndFolders.closeAll();
		}
	}
	
	/**
	 * Generates all views for PowerPoint presentations (jpg, movies etc...)
	 * @param cell the cell to process
	 */
	public static void generateViews(String cell){
		settings.setFolderAndCurrentCell(cell);
		//String img488=settings.currCell+"-488.tif";
		//String img561=settings.currCell+"-561.tif";
		
		String img488=settings.dir+settings.currCell+"-488.tif";
		String img561=settings.dir+settings.currCell+"-561.tif";
		
		//ImagePlus ip488=filesAndFolders.openAndCrop(img488);
		//ImagePlus ip561=filesAndFolders.openAndCrop(img561);
		
		ImagePlus ip488=new ImagePlus(img488);
		ImagePlus ip561=new ImagePlus(img561);
		
		int n488=ip488.getStackSize();
		int n561=ip561.getStackSize();
		
		if(n488<n561) replicate(ip488, n561);
		if(n561<n488) replicate(ip561, n488);
		
		//Generate overlay
		ImagePlus merge=new Concatenator().concatenate(ip561, ip488, false);
		merge=HyperStackConverter.toHyperStack(merge, 2, merge.getStackSize()/2, 1, "xyzct", "composite");
		merge.setTitle(settings.currCell+"_composite");
		filesAndFolders.calibrateAndSave(merge, settings.destForPPT);
		
		//Generate movie for ppt
		ImagePlus mergeRGB=new Duplicator().run(merge);
		RGBStackConverter.convertToRGB(mergeRGB);
		addTimeStamp(mergeRGB);
		mergeRGB.setCalibration(settings.getCalibration());
		IJ.run(mergeRGB, "Scale Bar...", "width=5 height=4 font=14 color=White background=None location=[Lower Right] bold label");
		mergeRGB.getCalibration().fps=48.0;
		AVI_Writer AVIW=new AVI_Writer();
		
		try {
			AVIW.writeImage(mergeRGB, settings.destForPPT+settings.currCell+".avi", AVI_Writer.JPEG_COMPRESSION, 75);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//IJ.run(mergeRGB, "AVI... ", "frame=48 save=["+]");
		mergeRGB.close();
		System.gc();
		
		//Save FRAP zone
		ImagePlus frapZone=new Duplicator().run(merge, 1, 2, 1, 1, 1, 1); //Duplicate the two channels, first time point, first slice
		frapZone.setTitle(settings.currCell+"_FRAP_Zone");
		RGBStackConverter.convertToRGB(frapZone);
		rgnFile frapROIFile=new rgnFile(settings.dir, settings.currCell+"-roi.rgn");
		frapZone.show();
		Roi frapROI=frapROIFile.rois.get(0).getIJRoifromMMRoi();
		frapZone.getProcessor().setColor(Color.WHITE);
		frapZone.getProcessor().draw(frapROI);
		frapZone.setCalibration(settings.getCalibration());
		IJ.run(frapZone, "Scale Bar...", "width=5 height=4 font=14 color=White background=None location=[Lower Right] bold");
		new FileSaver(frapZone).saveAsJpeg(settings.destForPPT+settings.currCell+"_FRAP_Zone.jpg");
		
		if(settings.isGCampExperiment){
			frapZone.getProcessor().setColor(Color.RED);
			frapZone.getProcessor().draw(frapROI);
			frapZone.getProcessor().setColor(Color.WHITE);
			frapZone.setRoi(frapROI);
			saveGCampSegments(frapZone, "");
			RoiManager rm=RoiManager.getInstance();
			if(rm!=null){
				for(int i=0; i<rm.getCount(); i++){
					Roi tmp=rm.getRoi(i);
					frapZone.getProcessor().draw(tmp);
				}
				rm.reset();
			}
			new FileSaver(frapZone).saveAsJpeg(settings.destGCampDataImg+settings.currCell+"_segments.jpg");
		}
		
		frapZone.changes=false;
		frapZone.close();
		System.gc();
		
		//Crop and rotate
		Roi originalRoi=frapROI;
		Rectangle originalBounds=originalRoi.getBounds();
				
		frapROI=RoiEnlarger.enlarge(frapROI, 15);
		merge.setRoi(frapROI);
		Rectangle rect=frapROI.getBounds();
		originalRoi.setLocation(originalBounds.x-rect.x, originalBounds.y-rect.y);
		
		merge=new ImagePlus(settings.currCell+"_crop", merge.getStack().crop((int) rect.getX(), (int) rect.getY(), 0, (int) rect.getWidth(), (int) rect.getHeight(), merge.getStackSize()));
		merge=HyperStackConverter.toHyperStack(merge, 2, merge.getStackSize()/2, 1, "xyczt", "composite");
		merge.setRoi(originalRoi);
		filesAndFolders.calibrateAndSave(merge, settings.destForPPT);
		
		//Saves the FRAP roi on the appropriate location on the image
		RoiEncoder.save(originalRoi, settings.destForPPT+settings.currCell+"_crop.roi");
		
		frapROI.setLocation(0, 0);
		merge.setRoi(originalRoi);
		
		if(settings.isGCampExperiment) saveGCampSegments(merge, "crop");
		merge.setRoi(frapROI);
		
		IJ.run(merge, "Fit Ellipse", "");
		double angle=((EllipseRoi) merge.getRoi()).getFeretValues()[1];
		merge.deleteRoi();
		merge.hide();
		merge=utilities.rotate(merge, angle);
		merge=HyperStackConverter.toHyperStack(merge, 2, merge.getStackSize()/2, 1, "xyczt", "composite");
		merge.setTitle(settings.currCell+"-crop_rotate");
		filesAndFolders.calibrateAndSave(merge, settings.destForPPT);
		
		ImagePlus tmp=ChannelSplitter.split(merge)[0];
		merge.flush();
		tmp.setLut(LUT.createLutFromColor(new Color(254, 254, 254)).createInvertedLut());
		tmp.show();
		tmp.resetDisplayRange();
		tmp.setTitle(settings.currCell+"_crop");
		new ImageConverter(tmp).convertToRGB();
		
		tmp.getCalibration().fps=48.0;
		try {
			AVIW.writeImage(tmp, settings.destForPPT+settings.currCell+"-crop_rotate.avi", AVI_Writer.JPEG_COMPRESSION, 75);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tmp.close();
		System.gc();
	}
	
	/**
	 * Replicates the first image of the stack on the last slice of the stack until 
	 * its size reaches the target size
	 * @param ip the ImagePlus to process
	 * @param targetSize the target number of slices
	 */
	public static void replicate(ImagePlus ip, int targetSize){
		if(ip.getStackSize()<targetSize){
			ip.setSlice(1);
			ImageProcessor iproc=ip.getProcessor().duplicate();
			while(ip.getStackSize()<targetSize){
				ImageStack is=ip.getStack();
				is.addSlice(iproc);
				ip.setStack(is);
			}
		}
	}
	
	/**
	 * Adds timestamps to the slices of the input ImagePlus
	 * @param ip the input ImagePlus the timestamps should be added to
	 */
	public static void addTimeStamp(ImagePlus ip){
		TextRoi tr;
		for(int i=1; i<ip.getStackSize(); i++){
			ip.setSlice(i);
			ImageProcessor iproc=ip.getProcessor();
			iproc.setColor(Color.WHITE);
			
			double time=i*settings.calibT;
			int min=(int) Math.floor(time/60);
			int sec=(int) Math.floor(time-min*60);
			double msec=time-min*60-sec;
			String stamp=IJ.pad(min, 2)+":"+IJ.pad(sec, 2)+"."+((int) (msec*1000));
			tr=new TextRoi(20, 30, stamp, new Font(Font.SANS_SERIF, Font.PLAIN, 18));
			
			tr.drawPixels(iproc);
		}
		
	}
	
	/**
	 * Returns a maximum intensity projection of the input ImagePlus, 
	 * using the input start/end slices, as an ImagePlus
	 * @param in the input ImagePlus
	 * @param start the start slice
	 * @param end the end slice
	 * @return the maximum intensity projection, as an ImagePlus
	 */
	public static ImagePlus getMaxProjection(ImagePlus in, int start, int end){
		ZProjector zp=new ZProjector(in);
		zp.setMethod(ZProjector.MAX_METHOD);
		zp.setStartSlice(start);
		zp.setStopSlice(end);
		zp.doProjection();
		
		return zp.getProjection();
	}
	
	/**
	 * Generates and saves ROIs segmenting the FRAP ROI into sub-ROIs, which length is set under settings.GCampLength
	 * @param ip the ImagePlus carrying the roi to process
	 * @param suffix suffix to be added at the end of the ROI file name
	 */
	private static void saveGCampSegments(ImagePlus ip, String suffix){
		Roi roi=ip.getRoi();
		roi.setName("FRAP_Zone");
		
		RoiManager rm=RoiManager.getInstance();
		if(rm==null){
			rm=new RoiManager();
		}else{
			rm.reset();
		}
		
		rm.addRoi(roi);
		
		double length=settings.GCampLength/settings.calibXY;
		
		if(roi!=null){
			ImageProcessor iproc=ip.getProcessor();
			iproc.setRoi(roi); //Required to get the stats from the Roi
			ImageStatistics is=ImageStatistics.getStatistics(iproc, ImageStatistics.ELLIPSE, new Calibration());
			
			EllipseFitter ef=new EllipseFitter();
			ef.fit(iproc, is);
			
			double major=ef.major/2.0;
			double minor=ef.minor/2.0;
			double angle=2*Math.PI-ef.theta;
			
			
			double xStart=ef.xCenter-major*Math.cos(angle);
			double yStart=ef.yCenter-major*Math.sin(angle);
			
			double onlineX=xStart;
			double onlineY=yStart;
			
			float[] x=new float[4];
			float[] y=new float[4];
			
			for(int i=0; i<Math.ceil(2*major/length); i++){
				if(i==0){
					x[0]=(float) (onlineX+minor*Math.cos(angle-Math.PI/2));
					y[0]=(float) (onlineY+minor*Math.sin(angle-Math.PI/2));
					x[1]=(float) (onlineX+minor*Math.cos(angle+Math.PI/2));
					y[1]=(float) (onlineY+minor*Math.sin(angle+Math.PI/2));
				}else{
					x[0]=x[3];
					y[0]=y[3];
					x[1]=x[2];
					y[1]=y[2];
				}
				
				onlineX=onlineX+length*Math.cos(angle);
				onlineY=onlineY+length*Math.sin(angle);
				
				x[3]=(float) (onlineX+minor*Math.cos(angle-Math.PI/2));
				y[3]=(float) (onlineY+minor*Math.sin(angle-Math.PI/2));
				x[2]=(float) (onlineX+minor*Math.cos(angle+Math.PI/2));
				y[2]=(float) (onlineY+minor*Math.sin(angle+Math.PI/2));
				
				iproc.setColor(Color.white);
				Roi segmentRoi=new PolygonRoi(x, y, PolygonRoi.POLYGON);
				segmentRoi.setName("Segment_"+(i+1));
				
				rm.addRoi(segmentRoi);
				rm.runCommand(ip,"Remove Channel Info");
				rm.runCommand(ip,"Remove Slice Info");
				rm.runCommand(ip,"Remove Frame Info");
				rm.deselect();
				rm.setVisible(true);
			}
			rm.runCommand("Save", settings.destGCampROI+settings.currCell+"_rois-segments"+(suffix!=""?"_"+suffix:"")+".zip");
			ip.setRoi(roi);
		}else{
			throw new IllegalArgumentException("No ROI to analyze");
		}
	}
}
