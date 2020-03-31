package analysis;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.YesNoCancelDialog;
import ij.measure.Calibration;
import io.KTSdataFileImporter;
import io.dataIO;
import io.imageDataIO;
import io.kymoROITimeCropper;
import io.log;
import io.prismOutput;
import utilities.filesAndFolders;

import javax.swing.JButton;
import javax.swing.SpringLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.awt.event.ActionEvent;

/**
  * GUI.java v1, 5 août 2016
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

/**
 * @author Fabrice P Cordelieres
 *
 */
public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	static filesAndFolders faf=null;
	static boolean isTrp=true;
	static boolean isUncaging=false;
	private JButton btnAnalyseGcamp;
	public static log log=null;
	private JButton btnAnalyseGcampUncaging;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setTitle("KymoToolSet v2 18/05/17");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 288, 263);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JButton btnGenerateImages = new JButton("Generate images");
		btnGenerateImages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateImages();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnGenerateImages, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnGenerateImages, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnGenerateImages, 0, SpringLayout.EAST, contentPane);
		btnGenerateImages.setPreferredSize(new Dimension(117, 29));
		btnGenerateImages.setMinimumSize(new Dimension(117, 29));
		btnGenerateImages.setMaximumSize(new Dimension(117, 29));
		contentPane.add(btnGenerateImages);
		
		JButton btnRecordPaths = new JButton("Record paths");
		btnRecordPaths.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recordPath();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRecordPaths, 0, SpringLayout.SOUTH, btnGenerateImages);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRecordPaths, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRecordPaths, 0, SpringLayout.EAST, contentPane);
		contentPane.add(btnRecordPaths);
		
		JButton btnRecordSegments = new JButton("Record segments/Analyse kymos");
		btnRecordSegments.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recordSegmentsAndAnalyse();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRecordSegments, 0, SpringLayout.SOUTH, btnRecordPaths);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRecordSegments, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRecordSegments, 0, SpringLayout.EAST, contentPane);
		contentPane.add(btnRecordSegments);
		
		JButton btnAnalyseFlux = new JButton("Analyse flux");
		btnAnalyseFlux.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyseFlux();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAnalyseFlux, 0, SpringLayout.SOUTH, btnRecordSegments);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAnalyseFlux, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnAnalyseFlux, 0, SpringLayout.EAST, contentPane);
		contentPane.add(btnAnalyseFlux);
		
		btnAnalyseGcamp = new JButton("Analyse GCaMP/Marker");
		btnAnalyseGcamp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyseGCaMP();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAnalyseGcamp, 0, SpringLayout.SOUTH, btnAnalyseFlux);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAnalyseGcamp, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnAnalyseGcamp, 0, SpringLayout.EAST, contentPane);
		contentPane.add(btnAnalyseGcamp);
		
		JButton btnPullData = new JButton("Pull data");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnPullData, 0, SpringLayout.WEST, btnGenerateImages);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnPullData, 0, SpringLayout.EAST, contentPane);
		btnPullData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pullData();
			}
		});
		
		btnAnalyseGcampUncaging = new JButton("Analyse GCaMP/Uncaging");
		btnAnalyseGcampUncaging.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyseGCaMPUncaging();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAnalyseGcampUncaging, 0, SpringLayout.SOUTH, btnAnalyseGcamp);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnPullData, 0, SpringLayout.SOUTH, btnAnalyseGcampUncaging);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAnalyseGcampUncaging, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnAnalyseGcampUncaging, 0, SpringLayout.EAST, contentPane);
		contentPane.add(btnAnalyseGcampUncaging);
		contentPane.add(btnPullData);
		
		JButton btnTimeFrame = new JButton("Re-analyse data on a timeframe");
		btnTimeFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reAnalyseTimeFrame();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnTimeFrame, 0, SpringLayout.SOUTH, btnPullData);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnTimeFrame, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnTimeFrame, 0, SpringLayout.EAST, btnGenerateImages);
		contentPane.add(btnTimeFrame);
	}
	
	/**
	 * 
	 */
	protected void generateImages() {
		faf=new filesAndFolders();
		if(faf.rootPath!=null && faf.basenames.length>0){
			log=new log(faf.rootPath);
			
			enableButtons();
			
			double xyCalibration=Prefs.get("KTSv2_xyCalibration.double", 0.133);
			double timeCalibration=Prefs.get("KTSv2_timeCalibration.double", 0.1);
			int nbPreBleach=(int) Prefs.get("KTSv2_nbPreBleach.double", 16);
			int minWaveletRadius=(int) Prefs.get("KTSv2_minWaveletRadius.double", 2);
			int maxWaveletRadius=(int) Prefs.get("KTSv2_maxWaveletRadius.double", 5);
			int topHatRadius=(int) Prefs.get("KTSv2_topHatRadius.double", 4);
			int vesiculesWavelength=(int) Prefs.get("KTSv2_vesiculesWavelength.double", 561);
			int secondChannelWavelength=(int) Prefs.get("KTSv2_secondChannelWavelength.double", 491);
			
			if(isUncaging) nbPreBleach=0;
			
			GenericDialog gd=new GenericDialog("Parameters");
			gd.addNumericField("XY calibration (microns)", xyCalibration, 3);
			gd.addNumericField("Time interval (seconds)", timeCalibration, 3);
			gd.addNumericField("Number of pre-bleach frames to remove (!!! SHOULD BE set to 0 FOR uncaging exp. !!!)", nbPreBleach, 0);
			gd.addNumericField("Min radius for DoG (pixels)", minWaveletRadius, 0);
			gd.addNumericField("Max radius for DoG (pixels)", maxWaveletRadius, 0);
			gd.addNumericField("Radius for top-hat (pixels)", topHatRadius, 0);
			gd.addNumericField("Wavelength for vesicules", vesiculesWavelength, 0);
			gd.addNumericField("Wavelength for the second channel", secondChannelWavelength, 0);
			gd.showDialog();
			
			if(gd.wasOKed()){
				Calibration cal=new Calibration();
				cal.pixelWidth=gd.getNextNumber();
				cal.pixelHeight=cal.pixelWidth;
				cal.frameInterval=gd.getNextNumber();
				cal.setUnit("µm");
				cal.setTimeUnit("sec");
				
				nbPreBleach=(int) gd.getNextNumber();
				minWaveletRadius=(int) gd.getNextNumber();
				maxWaveletRadius=(int) gd.getNextNumber();
				topHatRadius=(int) gd.getNextNumber();
				vesiculesWavelength=(int) gd.getNextNumber();
				secondChannelWavelength=(int) gd.getNextNumber();
				
				Prefs.set("KTSv2_xyCalibration.double", cal.pixelWidth);
				Prefs.set("KTSv2_timeCalibration.double", cal.frameInterval);
				Prefs.set("KTSv2_nbPreBleach.double", nbPreBleach);
				Prefs.set("KTSv2_minWaveletRadius.double", minWaveletRadius);
				Prefs.set("KTSv2_maxWaveletRadius.double", maxWaveletRadius);
				Prefs.set("KTSv2_topHatRadius.double", topHatRadius);
				Prefs.set("KTSv2_vesiculesWavelength.double", vesiculesWavelength);
				Prefs.set("KTSv2_secondChannelWavelength.double", secondChannelWavelength);
				
				
				imageDataIO io=new imageDataIO(faf.rootPath, ""+vesiculesWavelength, ""+secondChannelWavelength, cal);
				io.setPreProcessingParameters(minWaveletRadius, maxWaveletRadius, topHatRadius);
				
				log.logInfo("----- Generate images -----");
				log.logInfo("****** Set Parameters *****");
				log.logInfo("XY calibration (microns): "+xyCalibration);
				log.logInfo("Time interval (seconds): "+timeCalibration);
				log.logInfo("Number of pre-bleach frames to remove: "+nbPreBleach);
				log.logInfo("Min radius for wavelet (pixels): "+minWaveletRadius);
				log.logInfo("Max radius for wavelet (pixels): "+maxWaveletRadius);
				log.logInfo("Radius for top-hat (pixels): "+topHatRadius);
				log.logInfo("Wavelength for vesicules: "+vesiculesWavelength);
				log.logInfo("Wavelength for the second channel: "+secondChannelWavelength);
				log.logInfo("****** Set Parameters *****");
				
				Thread launch = new Thread(new Runnable(){
			        public void run(){
			        	for(int i=0; i<faf.basenames.length; i++){
			        		log.logInfo("Processing dataset "+faf.basenames[i]+" ("+(i+1)+"/"+faf.basenames.length+")");
			        		isTrp=io.getType(faf.basenames[i])==imageDataIO.TRP_SYNAPSE;
			        		enableButtons();
							
							int nbPreBleach=(int) Prefs.get("KTSv2_nbPreBleach.double", 16);
							io.saveImages(faf.basenames[i], nbPreBleach);
							
							if(i==faf.basenames.length-1) log.logInfo("----- Generate images: done -----");
						}
			    	}
				});
				launch.start();
			}
		}
	}
	
	private void recordPath() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();
			
			Line.setWidth(21);
			
			log.logInfo("----- Record path -----");
			
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	kymographs k=null;
					
					for(int i=0; i<faf.names.length; i++){
						log.logInfo("Recording path for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
						enableButtons();
						
						k=new kymographs(faf.rootPath, faf.names[i]);
						k.getPath();
						k.createKymograph();
						
						if(i==faf.names.length-1) log.logInfo("----- Record path: done -----");
					}
		    	}
			});
			launch.start();
		}
	}
	
	private void recordSegmentsAndAnalyse() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();
			
			double minSpeed=Prefs.get("KTSv2_minSpeed.double", 0.1);
			int lineWidth=(int) Prefs.get("KTSv2_lineWidth.double", 2);
			
			
			GenericDialog gd=new GenericDialog("Parameters");
			gd.addNumericField("Speed limit for pausing (microns/seconds)", minSpeed, 3);
			gd.addNumericField("Line width for display (pixels)", lineWidth, 0);
			gd.showDialog();
			
			if(gd.wasOKed()){
				Line.setWidth(2);
				minSpeed=gd.getNextNumber();
				lineWidth=(int) gd.getNextNumber();
				
				Prefs.set("KTSv2_minSpeed.double", minSpeed);
				Prefs.set("KTSv2_lineWidth.double", lineWidth);
				
				log.logInfo("----- Record segments/Analyse kymographs -----");
				log.logInfo("****** Set Parameters *****");
				log.logInfo("Speed limit for pausing (microns/seconds): "+minSpeed);
				log.logInfo("Line width for display (pixels): "+lineWidth);
				log.logInfo("****** Set Parameters *****");
						
				Thread launch = new Thread(new Runnable(){
			        public void run(){
			        	kymographs k=null;
						
						for(int i=0; i<faf.names.length; i++){
							log.logInfo("Recording segments and analysing kymographs for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							enableButtons();
							
							double minSpeed=Prefs.get("KTSv2_minSpeed.double", 0.1);
							int lineWidth=(int) Prefs.get("KTSv2_lineWidth.double", 2);
							
							k=new kymographs(faf.rootPath, faf.names[i]);
							k.getSegments();
							k.analyseKymograph(minSpeed, lineWidth);
							
							if(i==faf.names.length-1) log.logInfo("----- Record segments/Analyse kymographs: done -----");
						}
			    	}
				});
				launch.start();
			}
		}
	}
	
	private void analyseFlux() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();
			
			log.logInfo("----- Analyse flux -----");
			
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	flux fl=null;
		        	for(int i=0; i<faf.names.length; i++){
		        		log.logInfo("Analysing flux for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
		        		enableButtons();
						
						fl=new flux(faf.rootPath, faf.names[i]);
						fl.analyseFlux();
						
						if(i==faf.names.length-1) log.logInfo("----- Analyse flux: done -----");
					}
		    	}
			});
			launch.start();
		}
	}
	
	private void analyseGCaMP() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();

			log.logInfo("----- Analyse GCaMP -----");
			
			
			double minSpeed=Prefs.get("KTSv2_minSpeed.double", 0.1);
			
			GenericDialog gd=new GenericDialog("Parameters");
			gd.addNumericField("Speed limit for pausing (microns/seconds)", minSpeed, 3);
			gd.showDialog();
			
			if(gd.wasOKed()){
				minSpeed=gd.getNextNumber();
				Prefs.set("KTSv2_minSpeed.double", minSpeed);
				log.logInfo("****** Set Parameters *****");
				log.logInfo("Speed limit for pausing (microns/seconds): "+minSpeed);
				log.logInfo("****** Set Parameters *****");
			
				Thread launch = new Thread(new Runnable(){
			        public void run(){
			        	GCaMP gc=null;
						for(int i=0; i<faf.names.length; i++){
							log.logInfo("Analysing GCaMP for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							enableButtons();
							
							double minSpeed=Prefs.get("KTSv2_minSpeed.double", 0.1);
							
							gc=new GCaMP(faf.rootPath, faf.names[i]);
							gc.exportGCamPData();
							gc.correlateToSpeed(minSpeed);
							
							gc.separateDirections(GCaMP.IN, minSpeed);
							gc.separateDirections(GCaMP.PAUSE, minSpeed);
							gc.separateDirections(GCaMP.OUT, minSpeed);
							
							if(i==faf.names.length-1) log.logInfo("----- Analyse GCaMP: done -----");
						}
			    	}
				});
				launch.start();
			}
		}
	}
	
	private void analyseGCaMPUncaging() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();

			log.logInfo("----- Analyse GCaMP Uncaging -----");
			
			int nPreImages=(int) Prefs.get("KTSv2_Uncaging_nPreImages.double", 50);
			double preIntervalle=Prefs.get("KTSv2_Uncaging_preIntervalle.double", 0.426);
			double perturbationDuration=Prefs.get("KTSv2_Uncaging_perturbationDuration.double", 0.8);
			int nCycles=(int) Prefs.get("KTSv2_Uncaging_nCycles.double", 10);
			int nPostImages=(int) Prefs.get("KTSv2_Uncaging_nPostImages.double", 5);
			double postIntervalle=Prefs.get("KTSv2_Uncaging_postIntervalle.double", 0.426);
			
			double minSpeed=Prefs.get("KTSv2_Uncaging_minSpeed.double", 0.1);
			
			GenericDialog gd=new GenericDialog("Parameters");
			gd.addMessage("------ Acquisition Parameters ------");
			gd.addNumericField("Number of pre-perturbation images", nPreImages, 0);
			gd.addNumericField("Time interval between pre-perturbation sequences (sec)", preIntervalle, 3);
			gd.addNumericField("Duration of a single perturbation (sec)", perturbationDuration, 3);
			gd.addNumericField("Number of perturbation cycles", nCycles, 0);
			gd.addNumericField("Number of post-perturbation images per cycle", nPostImages, 0);
			gd.addNumericField("Time interval between post-perturbation sequences (sec)", postIntervalle, 3);
			
			gd.addMessage("------ Analysis Parameters ------");
			gd.addNumericField("Speed limit for pausing (microns/seconds)", minSpeed, 3);
			gd.showDialog();
			
			if(gd.wasOKed()){
				nPreImages=(int) gd.getNextNumber();
				preIntervalle=gd.getNextNumber();
				perturbationDuration=gd.getNextNumber();
				nCycles=(int) gd.getNextNumber();
				nPostImages=(int) gd.getNextNumber();
				postIntervalle=gd.getNextNumber();
				minSpeed=gd.getNextNumber();
				
				Prefs.set("KTSv2_Uncaging_nPreImages.double", nPreImages);
				Prefs.set("KTSv2_Uncaging_preIntervalle.double", preIntervalle);
				Prefs.set("KTSv2_Uncaging_perturbationDuration.double", perturbationDuration);
				Prefs.set("KTSv2_Uncaging_nCycles.double", nCycles);
				Prefs.set("KTSv2_Uncaging_nPostImages.double", nPostImages);
				Prefs.set("KTSv2_Uncaging_postIntervalle.double", postIntervalle);
				
				Prefs.set("KTSv2_Uncaging_minSpeed.double", minSpeed);
				
				log.logInfo("****** Set Parameters *****");
				log.logInfo(">>>>>> Acquisition Parameters <<<<<<");
				log.logInfo("Number of pre-perturbation images: "+nPreImages);
				log.logInfo("Time interval between pre-perturbation sequences (sec): "+preIntervalle);
				log.logInfo("Duration of a single perturbation (sec): "+perturbationDuration);
				log.logInfo("Number of perturbation cycles: "+nCycles);
				log.logInfo("Number of post-perturbation images per cycle: "+nPostImages);
				log.logInfo("Time interval between post-perturbation sequences (sec): "+postIntervalle);
				log.logInfo(">>>>>> Analysis Parameters <<<<<<");
				log.logInfo("Speed limit for pausing (microns/seconds): "+minSpeed);
				log.logInfo("****** Set Parameters *****");
			
				Thread launch = new Thread(new Runnable(){
			        public void run(){
			        	GCaMPUncaging gcu=null;
						for(int i=0; i<faf.names.length; i++){
							log.logInfo("Analysing GCaMP Uncaging for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							enableButtons();
							
							int nPreImages=(int) Prefs.get("KTSv2_Uncaging_nPreImages.double", 50);
							double preIntervalle=Prefs.get("KTSv2_Uncaging_preIntervalle.double", 0.426);
							double perturbationDuration=Prefs.get("KTSv2_Uncaging_perturbationDuration.double", 0.8);
							int nCycles=(int) Prefs.get("KTSv2_Uncaging_nCycles.double", 10);
							int nPostImages=(int) Prefs.get("KTSv2_Uncaging_nPostImages.double", 5);
							double postIntervalle=Prefs.get("KTSv2_Uncaging_postIntervalle.double", 0.426);
							
							double minSpeed=Prefs.get("KTSv2_Uncaging_minSpeed.double", 0.1);
							
							
							double[] params=new double[]{nPreImages, preIntervalle, perturbationDuration, nCycles, nPostImages, postIntervalle};
							
							gcu=new GCaMPUncaging(faf.rootPath, faf.names[i], params);
							faf.setName(faf.names[i]);
							
							gcu.moveSynapsesRois(faf.names[i]);
							
							if (i==0) gcu.calculateTime(new ImagePlus(faf.imagePath_crop).getNSlices());
							gcu.exportGCamPUncagingData();
							gcu.correlateToSpeed(minSpeed);
							
							gcu.separateDirections(GCaMP.IN, minSpeed);
							gcu.separateDirections(GCaMP.PAUSE, minSpeed);
							gcu.separateDirections(GCaMP.OUT, minSpeed);
							
							if(i==faf.names.length-1) log.logInfo("----- Analyse GCaMP Uncaging: done -----");
						}
			    	}
				});
				launch.start();
			}
		}
	}
	
	private void pullData() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();
			
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	enableButtons();
		        	pullDataAction();
		    	}
			});
			launch.start();
		}
	}
	
	private void reAnalyseTimeFrame() {
		if(checkFolder() && faf.rootPath!=null && faf.basenames.length>0){
			enableButtons();

			log.logInfo("----- Re-analyse data on a timeframe -----");
			
			int startTimePoint=(int) Prefs.get("KTSv2_ReAnalyse_startTimePoint.double", 0);
			int endTimePoint=(int) Prefs.get("KTSv2_ReAnalyse_endTimePoint.double", 50);
			
			double minSpeed=Prefs.get("KTSv2_minSpeed.double", 0.1);
			int lineWidth=(int) Prefs.get("KTSv2_lineWidth.double", 2);
			
			faf.setName(faf.names[0]);
			
			GenericDialog gd=new GenericDialog("Parameters");
			gd.addMessage("------ Re-analysis Parameters ------");
			gd.addNumericField("Start timepoint", startTimePoint, 0);
			gd.addNumericField("End timepoint", endTimePoint, 0);
			
			gd.addMessage("------ Kymographs analysis Parameters ------");
			gd.addNumericField("Speed limit for pausing (microns/seconds)", minSpeed, 3);
			gd.addNumericField("Line width for display (pixels)", lineWidth, 0);
			
			gd.showDialog();
			
			if(gd.wasOKed()){
				startTimePoint=(int) gd.getNextNumber();
				endTimePoint=(int) gd.getNextNumber();
				
				minSpeed=gd.getNextNumber();
				lineWidth=(int) gd.getNextNumber();
				
				Prefs.set("KTSv2_ReAnalyse_startTimePoint.double", startTimePoint);
				Prefs.set("KTSv2_ReAnalyse_endTimePoint.double", endTimePoint);
				
				Prefs.set("KTSv2_minSpeed.double", minSpeed);
				Prefs.set("KTSv2_lineWidth.double", lineWidth);
				
				
				log.logInfo("****** Set Parameters *****");
				log.logInfo(">>>>>> Re-analysis Parameters <<<<<<");
				log.logInfo("Start timepoint: "+startTimePoint);
				log.logInfo("End timepoint: "+endTimePoint);
				
				log.logInfo("Speed limit for pausing (microns/seconds): "+minSpeed);
				log.logInfo("Line width for display (pixels): "+lineWidth);
				
				log.logInfo(">>>>>> Re-analysis Parameters <<<<<<");
				log.logInfo("****** Set Parameters *****");
				
				
				
				
				
				Thread launch = new Thread(new Runnable(){
			        public void run(){
			        	kymographs k=null;
			        	filesAndFolders fafOut=null;
			        	
			        	for(int i=0; i<faf.names.length; i++){
							enableButtons();
							
							int startTimePoint=(int) Prefs.get("KTSv2_ReAnalyse_startTimePoint.double", 0);
							int endTimePoint=(int) Prefs.get("KTSv2_ReAnalyse_endTimePoint.double", 50);
							
							//Keep track of folders
							fafOut=new filesAndFolders(faf.rootPath+"Re-Analysis_"+startTimePoint+"-"+endTimePoint+File.separator);
							
							faf.setName(faf.names[i]);
							fafOut.setName(faf.names[i]);
							
							//Copy data
							log.logInfo("Copying data from "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							try {
								//Copy images
								new File(fafOut.imagePath).mkdirs();
								Files.copy(Paths.get(faf.imagePath_composite), Paths.get(fafOut.imagePath_composite), StandardCopyOption.REPLACE_EXISTING);
								Files.copy(Paths.get(faf.imagePath_crop), Paths.get(fafOut.imagePath_crop), StandardCopyOption.REPLACE_EXISTING);
								Files.copy(Paths.get(faf.imagePath_crop_proj), Paths.get(fafOut.imagePath_crop_proj), StandardCopyOption.REPLACE_EXISTING);
								Files.copy(Paths.get(faf.imagePath_overview), Paths.get(fafOut.imagePath_overview), StandardCopyOption.REPLACE_EXISTING);
								
								
								//Copy ROIs
								new File(fafOut.kymoPath).mkdirs();
								Files.copy(Paths.get(faf.kymoPath_kymo), Paths.get(fafOut.kymoPath_kymo), StandardCopyOption.REPLACE_EXISTING);
								Files.copy(Paths.get(faf.kymoPath_ROI), Paths.get(fafOut.kymoPath_ROI), StandardCopyOption.REPLACE_EXISTING);
								Files.copy(Paths.get(faf.kymoPath_segments), Paths.get(fafOut.kymoPath_segments), StandardCopyOption.REPLACE_EXISTING);
								
								//Copy GCaMPUngaging data
								if(new File(faf.GCaMPUncagingPath).exists()){
									new File(fafOut.GCaMPUncagingPath).mkdirs();
									Files.copy(Paths.get(faf.GCaMPUncagingPath_rois), Paths.get(fafOut.GCaMPUncagingPath_rois), StandardCopyOption.REPLACE_EXISTING);
								}
								
							} catch (IOException e) {
								log.logInfo("Something went wrong when copying data from "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							}
							
							//Adapt kymoPath_segments
							log.logInfo("Adapting ROIs segments from "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							kymoROITimeCropper krtc=new kymoROITimeCropper(fafOut.kymoPath_segments);
							krtc.timeCropROIs(startTimePoint, endTimePoint);
							krtc.saveCroppedRois(fafOut.kymoPath_segments);
							
							//Analyse kymographs
							log.logInfo("Re-analysing kymographs from "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
							double minSpeed=Prefs.get("KTSv2_minSpeed.double", 0.1);
							int lineWidth=(int) Prefs.get("KTSv2_lineWidth.double", 2);
							
							k=new kymographs(fafOut.rootPath, faf.names[i]);
							k.analyseKymograph(minSpeed, lineWidth);
							
							//Analyse flux
							log.logInfo("Cropping flux data for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
			        		new dataIO(faf.fluxPath_data).cropData(startTimePoint, endTimePoint).writeData(fafOut.fluxPath_data);
							
							//Analyze GCaMP
							if(new File(faf.GCaMPPath).exists()){
								log.logInfo("Cropping GCaMP data for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
								
								new dataIO(faf.GCaMPPath_data).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPPath_data);
								new dataIO(faf.GCaMPPath_data_correl_speed).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPPath_data_correl_speed);
								new dataIO(faf.GCaMPPath_data_correl_speed_IN).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPPath_data_correl_speed_IN);
								new dataIO(faf.GCaMPPath_data_correl_speed_OUT).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPPath_data_correl_speed_OUT);
								new dataIO(faf.GCaMPPath_data_correl_speed_PAUSE).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPPath_data_correl_speed_PAUSE);
							}
							
							//Analyze GCaMP_Uncaging
							if(new File(faf.GCaMPUncagingPath).exists()){
								log.logInfo("Cropping GCaMP Uncaging data for "+faf.names[i]+" ("+(i+1)+"/"+faf.names.length+")");
								
								new dataIO(faf.GCaMPUncagingPath_data).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPUncagingPath_data);
								new dataIO(faf.GCaMPUncagingPath_data_correl_speed).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPUncagingPath_data_correl_speed);
								new dataIO(faf.GCaMPUncagingPath_data_correl_speed_IN).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPUncagingPath_data_correl_speed_IN);
								new dataIO(faf.GCaMPUncagingPath_data_correl_speed_PAUSE).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPUncagingPath_data_correl_speed_PAUSE);
								new dataIO(faf.GCaMPUncagingPath_data_correl_speed_OUT).cropData(startTimePoint, endTimePoint).writeData(fafOut.GCaMPUncagingPath_data_correl_speed_OUT);
							}
							
							if(i==faf.names.length-1) log.logInfo("----- Re-analyse data on a timeframe: done -----");
						}
			        	filesAndFolders fafOri=faf;
			        	filesAndFolders fafOutOri=fafOut;
			        	faf=fafOut;
			        	
			        	pullDataAction();
			        	
			        	faf=fafOri;
			        	fafOut=fafOutOri;
			    	}
				});
				launch.start();
			}
		}
	}
	
	private void enableButtons(){
		//isTrp=faf.basenames[0].toLowerCase().endsWith(".nd");
		isUncaging=new File(faf.rootPath+faf.basenames[0].replace(".nd", "")+"_uncaging.rgn").exists();
		//btnAnalyseGcamp.setEnabled(!isTrp); Finally, decided it should always be one
		btnAnalyseGcampUncaging.setEnabled(isUncaging);
	}
	
	private boolean checkFolder(){
		if(faf==null || faf.rootPath==null){
			faf=new filesAndFolders();
			if(faf.rootPath==null ||faf.rootPath==""){
				return false;
			}else{
				log=new log(faf.rootPath);
				return true;
			}
		}else{
			YesNoCancelDialog ync=new YesNoCancelDialog(null, "Proceed with this folder ?", "Continue with this folder ?\n"+faf.rootPath);
			if(ync.yesPressed()){
				faf=new filesAndFolders(faf.rootPath, null);
				log=new log(faf.rootPath);
				return true;
			}
			if(!ync.cancelPressed() && !ync.yesPressed()){
				faf=new filesAndFolders();
				if(faf.rootPath==null ||faf.rootPath==""){
					return false;
				}else{
					log=new log(faf.rootPath);
					return true;
				}
			}else{
				return false;
			}
		}
	}
	
	private void pullDataAction(){
		log.logInfo("----- Pull data -----");
		
		KTSdataFileImporter kdfi=new KTSdataFileImporter(faf.rootPath);
    	kdfi.merge(faf.rootPath, KTSdataFileImporter.KYMO_COORD);
    	log.logInfo("Kymo_coord.xls generated");
    	
    	kdfi.merge(faf.rootPath, KTSdataFileImporter.KYMO_DATA);
    	log.logInfo("Kymo_data.xls generated");
    	
    	kdfi.merge(faf.rootPath, KTSdataFileImporter.FLUX_DATA);
    	log.logInfo("Flux_data.xls generated");
    	
    	//if(!isTrp){
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_DATA);
        	log.logInfo("GCaMP_data.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_DATA_CORREL_SPEED);
        	log.logInfo("GCaMP_data_correl_speed.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_DATA_CORREL_SPEED_IN);
        	log.logInfo("GCaMP_data_correl_speed_IN.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_DATA_CORREL_SPEED_PAUSE);
        	log.logInfo("GCaMP_data_correl_speed_PAUSE.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_DATA_CORREL_SPEED_OUT);
        	log.logInfo("GCaMP_data_correl_speed_OUT.xls generated");
    	//}    
    	
    	if(isUncaging){
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_UNCAGING_DATA);
        	log.logInfo("GCaMP_Uncaging_data.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_UNCAGING_DATA_CORREL_SPEED);
        	log.logInfo("GCaMP_Uncaging_data_correl_speed.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_UNCAGING_DATA_CORREL_SPEED_IN);
        	log.logInfo("GCaMP_Uncaging_data_correl_speed_IN.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_UNCAGING_DATA_CORREL_SPEED_PAUSE);
        	log.logInfo("GCaMP_Uncaging_data_correl_speed_PAUSE.xls generated");
        	
        	kdfi.merge(faf.rootPath, KTSdataFileImporter.GCAMP_UNCAGING_DATA_CORREL_SPEED_OUT);
        	log.logInfo("GCaMP_Uncaging_data_correl_speed_OUT.xls generated");
    	}
    	
    	savePrismFile();
    	log.logInfo("All_in_one.pzfx generated");
    	
    	log.logInfo("----- Pull data: done -----");
	}
	
	private void savePrismFile(){
		faf.buildPaths();
		String[] pulledDataFiles=new String[]{	faf.pulledDataPath_kymoCoord,
												faf.pulledDataPath_kymoData,
												faf.pulledDataPath_fluxData,
												faf.pulledDataPath_GCaMPData,
												faf.pulledDataPath_GCaMPDataCorrelSpeed,
												faf.pulledDataPath_GCaMPDataCorrelSpeed_IN,
												faf.pulledDataPath_GCaMPDataCorrelSpeed_PAUSE,
												faf.pulledDataPath_GCaMPDataCorrelSpeed_OUT,
												faf.pulledDataPath_GCaMPUncagingData,
												faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed,
												faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed_IN,
												faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed_PAUSE,
												faf.pulledDataPath_GCaMPUncagingDataCorrelSpeed_OUT};
		
		String[] pulledDataTitles=new String[]{	"Kymo coord",
												"Kymo data",
												"Flux data",
												"GCaMP data",
												"GCaMP correl data",
												"GCaMP correl data, inward speeds",
												"GCaMP correl data, pauses",
												"GCaMP correl data, outward speeds",
												"GCaMP Uncaging data",
												"GCaMP Uncaging correl data",
												"GCaMP Uncaging correl data, inward speeds",
												"GCaMP Uncaging correl data, pauses",
												"GCaMP Uncaging correl data, outward speeds"};
		
		prismOutput pzf=new prismOutput();
		dataIO dio=null;
		
		for(int i=0; i<pulledDataFiles.length; i++){
			if(new File(pulledDataFiles[i]).exists()){
				dio=new dataIO(pulledDataFiles[i]);
				pzf.addTable(pulledDataTitles[i], dio.getHeader(), dio.getData());
			}
		}
		
		pzf.write(faf.pulledDataPath_prism);
	}
}
