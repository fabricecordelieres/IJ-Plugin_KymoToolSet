package steps;

import java.io.File;

import Utilities.kymograph.improveKymo;
import denoise.Denoising;
import denoise.Operations;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.RoiEncoder;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImageCalculator;
import ij.plugin.Resizer;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import imageware.Builder;
import imageware.ImageWare;
import ndFile.ndStackBuilder;
import rgnFile.rgnFile;
import settings.filesAndFolders;
import settings.settings;

/**
 * This class includes all methods to perform pre-processing of the data
 * @author fab
 *
 */
public class preProcessing {

	public static void doPreProcessing(String cell) {
		settings.setFolderAndCurrentCell(cell);
		String title=settings.currCell;
		
		//If GCamp, converts data
		if(settings.isGCampExperiment) convertGCampData(cell);
		
		
		//Load data
		ImagePlus ip=new ImagePlus(settings.dir+settings.currCell+"-"+settings.lambda+".tif");
		ip.show();
		
		//Remove black and pre-bleach images
		int removeCount=settings.bleachTP;
		
		for(int i=1; i<=ip.getNSlices(); i++){
			ip.setSlice(i);
			if(ip.getStatistics().mean==0 || removeCount>0){
				ip.getImageStack().deleteSlice(i);
				i--;
				removeCount--;
			}
		}
		
		//Crop
		rgnFile frapROI=new rgnFile(settings.dir, settings.currCell+"-roi.rgn");
		if(frapROI.rois.size()!=0) ip.setRoi(frapROI.rois.get(0).getIJRoifromMMRoi());
		new Resizer().run("crop");
		ip.setTitle(settings.currCell+"_crop");
		
		//Save the cropped Roi
		Roi roiOnCrop=ip.getRoi();
		if(roiOnCrop!=null) RoiEncoder.save(roiOnCrop, settings.destCell+settings.currCell+"_crop.roi");
		
		filesAndFolders.calibrateAndSave(ip, settings.destCell);
		System.gc();
		
		//Wavelets
		if(settings.impKymo){
			ip=new improveKymo(ip).getFilteredImage(settings.startKymo, settings.stopKymo);
			ip.setTitle(title+"_impKymo"+settings.startKymo+"-"+settings.stopKymo);
			title+="_impKymo"+settings.startKymo+"-"+settings.stopKymo;
			System.gc();
		}
		
		//Denoise
		if(settings.denoise){
			ip=denoise(ip);
			title+="_Denoised"+settings.cycleDen+"-"+settings.frameDen;
		}
		
		//Top-Hat
		if(settings.doTH){
			ip=topHat(ip, (double) settings.radTH);
			title+="_TH"+(int) settings.radTH;
		}
		
		ip.setTitle(title);
		filesAndFolders.calibrateAndSave(ip, settings.destStkProc);
		filesAndFolders.closeAll();
	}
	
	/**
	 * Converts GCamp data to regular experiment format
	 */
	public static void convertGCampData(String cell){
		//Open the nd file, displays the hyperstack
		ndStackBuilder nsb=new ndStackBuilder(settings.oriGCamp, settings.GCampExperimentName+"_Cell"+cell+".nd");
		nsb.setOutputParameters(false, true, true);
		nsb.buildStacks(null);
		ImagePlus ip=WindowManager.getCurrentImage();
		
		//Copy/Paste the ROI
		rgnFile mmRoi=new rgnFile(settings.oriGCamp, settings.GCampExperimentName+"_Cell"+cell+".rgn");
		mmRoi.read();
		
		//Data export to regular format
		new File(settings.dir).mkdir();
		
		ImageStack ip488=ChannelSplitter.getChannel(ip, 2);
		ImageStack ip561=ChannelSplitter.getChannel(ip, 1);
		
		new FileSaver(new ImagePlus("", ip488)).saveAsTiffStack(settings.dir+"Cell"+cell+"-488.tif");
		new FileSaver(new ImagePlus("", ip561)).saveAsTiffStack(settings.dir+"Cell"+cell+"-561.tif");
		mmRoi.write(settings.dir+"Cell"+cell+"-roi.rgn");
		
		//Handles potential memory issues
		mmRoi=null;
		ip.hide();
		ip.flush();
	}
	
	/**
	 * Performs the denoising by calling PureDenoise plugin.
	 * The code is copied/pasted/adapted from the PureDenoise_ class of the plugin
	 * @param ip the input ImagePlus
	 * @return the result of the top-hat filtering, as an ImagePlus
	 */
	public static ImagePlus denoise(ImagePlus ip){
		String title=ip.getTitle();
		//Get sizes
		int Nmin=16;
		
		int nx = ip.getWidth();
        int ny = ip.getHeight();
        int nz = ip.getStackSize();
        
        ImageWare original = Builder.create(ip);
        original = original.convert(ImageWare.DOUBLE);
        //nz = original.getSizeZ();
        
        
        //If needed, extends the image frame
        int nxe = (int) (Math.ceil((double) nx / Nmin) * Nmin);
        int nye = (int) (Math.ceil((double) ny / Nmin) * Nmin);
        int[] Ext = new int[2];
        
        //ip.flush();
        
        if (nxe != nx || nye != ny) {
            original = Operations.symextend2D(original, nxe, nye, Ext);
        } else {
            Ext[0] = 0;
            Ext[1] = 0;
        }
        
        //Initialize the parameters' arrays
        double[] AlphaHat = new double[nz];
        double[] DeltaHat = new double[nz];
        double[] SigmaHat = new double[nz];
        
        Denoising denoising = new Denoising(original, AlphaHat, DeltaHat, SigmaHat, false, settings.cycleDen, settings.frameDen);
        //denoising.setLog(true);
        
        denoising.estimateNoiseParameters();
        
        int NBFRAME = Math.max(1, Math.min(nz, settings.frameDen));
        int CS = Math.max(1, Math.min(10, settings.cycleDen));
        
        denoising.setCycleSpins(CS);
        denoising.setMultiFrame(NBFRAME);
        
        denoising.perform();
        ImageWare output = denoising.getOutput();
        
        //Restores original size, if required
        if (nxe != nx || nye != ny) {
            output = Operations.crop2D(output, nx, ny, Ext);
        }
        
        ImagePlus result=new ImagePlus(title+"_Denoised"+settings.cycleDen+"-"+settings.frameDen, output.buildImageStack());
        output=null;
        
        System.gc();
        return result;
	}
	
	/**
	 * Performs a top-hat filtering and displays the result on the input ImagePlus, using the defined radius
	 * @param ip the input ImagePlus
	 * @param radius the top-hat radius
	 * @return the result of the top-hat filtering, as an ImagePlus
	 */
	public static ImagePlus topHat(ImagePlus ip, double radius){
		String title=ip.getTitle();
		ImagePlus tmp=ip.duplicate();
		
		RankFilters rf=new RankFilters();
		for(int i=1; i<=ip.getStackSize(); i++){
			tmp.setPosition(i);
			ImageProcessor iproc=tmp.getProcessor();
			rf.rank(iproc, radius, RankFilters.MIN);
			rf.rank(iproc, radius, RankFilters.MAX);
			tmp.setProcessor(iproc);
			tmp.updateImage();
		}
		
		//Essential to get the result... Can't directly attribute result to ip
		ImagePlus result=new ImageCalculator().run("Subtract create stack 32-bit", ip, tmp);
		result.setTitle(title+"_TH"+(int)radius);
		
		tmp.changes=false;
		tmp.close();
		
		System.gc();
		return result;
	}
}
