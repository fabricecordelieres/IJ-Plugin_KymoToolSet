import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.SpringLayout;

import java.awt.Dimension;

import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

import settings.checks;
import settings.filesAndFolders;
import settings.gui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import settings.settings;
import steps.RScript;
import steps.analysis;
import steps.preProcessing;
import steps.viewsGenerator;
import utilities.utilities;

import javax.swing.JProgressBar;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class KymoToolSet_ extends JFrame implements PlugIn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5301613363419879326L;
	private JPanel contentPane;
	private JButton step01Button;
	private JButton step02Button;
	private JButton step03Button;
	private JButton step04Button;
	private JButton step05Button;
	private JButton step06Button;
	private JButton step07Button;
	private JButton step08Button;
	private JButton step10Button;
	private JButton step11Button;
	private JButton step12Button;
	public static JProgressBar progressBar;
	public static int progressBarMin=0;
	public static int progressBarMax=0;
	public static int progressBarPosition=0;
	public static String progressBarText="";
	private JButton step09Button;
	private JButton step13Button;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KymoToolSet_ frame = new KymoToolSet_();
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
	public KymoToolSet_() {
		setTitle("KymoToolSet 15/07/16");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 278, 708);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		step01Button = new JButton("Step 1: Create folders and preprocess");
		step01Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step01_createFoldersAndPreprocess();
			}
		});
		step01Button.setIconTextGap(10);
		step01Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_01.jpg")));
		step01Button.setHorizontalAlignment(SwingConstants.LEFT);
		step01Button.setPreferredSize(new Dimension(0, 50));
		step01Button.setSize(new Dimension(0, 50));
		
		step02Button = new JButton("Step 2: Create movies");
		step02Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step02_createMovies();
			}
		});
		step02Button.setIconTextGap(10);
		step02Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_02.jpg")));
		step02Button.setHorizontalAlignment(SwingConstants.LEFT);
		step02Button.setPreferredSize(new Dimension(0, 50));
		step02Button.setSize(new Dimension(0, 50));
		SpringLayout sl_contentPane = new SpringLayout();
		sl_contentPane.putConstraint(SpringLayout.EAST, step01Button, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, step02Button, 0, SpringLayout.SOUTH, step01Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step01Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, step02Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step02Button, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, step01Button, 0, SpringLayout.NORTH, contentPane);
		contentPane.setLayout(sl_contentPane);
		contentPane.add(step01Button);
		contentPane.add(step02Button);
		
		step03Button = new JButton("Step 3: Analyze flux");
		step03Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step03_analyzeFlux();
			}
		});
		step03Button.setIconTextGap(10);
		step03Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_03.jpg")));
		step03Button.setHorizontalAlignment(SwingConstants.LEFT);
		step03Button.setPreferredSize(new Dimension(0, 50));
		step03Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step03Button, 0, SpringLayout.SOUTH, step02Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step03Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step03Button, 0, SpringLayout.EAST, contentPane);
		contentPane.add(step03Button);
		
		step04Button = new JButton("Step 4: Steps 1-3");
		step04Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step04_steps1_3();
			}
		});
		step04Button.setIconTextGap(10);
		step04Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_04.jpg")));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step04Button, 0, SpringLayout.SOUTH, step03Button);
		step04Button.setHorizontalAlignment(SwingConstants.LEFT);
		step04Button.setPreferredSize(new Dimension(0, 50));
		step04Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.WEST, step04Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step04Button, 0, SpringLayout.EAST, contentPane);
		contentPane.add(step04Button);
		
		step05Button = new JButton("Step 5: Generate projections");
		step05Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step05_generateProjections();
			}
		});
		step05Button.setIconTextGap(10);
		step05Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_05.jpg")));
		step05Button.setHorizontalAlignment(SwingConstants.LEFT);
		step05Button.setPreferredSize(new Dimension(0, 50));
		step05Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step05Button, 0, SpringLayout.SOUTH, step04Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step05Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step05Button, 0, SpringLayout.EAST, contentPane);
		contentPane.add(step05Button);
		
		step06Button = new JButton("Step 6: Record main paths");
		step06Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step06_recordMainPaths();
			}
		});
		step06Button.setIconTextGap(10);
		step06Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_06.jpg")));
		step06Button.setHorizontalAlignment(SwingConstants.LEFT);
		step06Button.setPreferredSize(new Dimension(0, 50));
		step06Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step06Button, 0, SpringLayout.SOUTH, step05Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step06Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step06Button, 0, SpringLayout.EAST, step05Button);
		contentPane.add(step06Button);
		
		step07Button = new JButton("Step 7: Create kymographs");
		step07Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step07_createKymographs();
			}
		});
		step07Button.setIconTextGap(10);
		step07Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_07.jpg")));
		step07Button.setHorizontalAlignment(SwingConstants.LEFT);
		step07Button.setPreferredSize(new Dimension(0, 50));
		step07Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step07Button, 0, SpringLayout.SOUTH, step06Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step07Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step07Button, 0, SpringLayout.EAST, contentPane);
		contentPane.add(step07Button);
		
		step08Button = new JButton("Step 8: Record vesicules paths");
		step08Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step08_recordVesiculesPaths();
			}
		});
		step08Button.setIconTextGap(10);
		step08Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_08.jpg")));
		step08Button.setHorizontalAlignment(SwingConstants.LEFT);
		step08Button.setPreferredSize(new Dimension(0, 50));
		step08Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step08Button, 0, SpringLayout.SOUTH, step07Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step08Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step08Button, 0, SpringLayout.EAST, contentPane);
		contentPane.add(step08Button);
		
		step10Button = new JButton("Step 10: Analyze kymographs");
		step10Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step10_analyzeKymographs();
			}
		});
		
		step09Button = new JButton("Step 09: Point synapses");
		sl_contentPane.putConstraint(SpringLayout.NORTH, step10Button, 0, SpringLayout.SOUTH, step09Button);
		sl_contentPane.putConstraint(SpringLayout.NORTH, step09Button, 0, SpringLayout.SOUTH, step08Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step09Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step09Button, 0, SpringLayout.EAST, contentPane);
		step09Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_09.jpg")));
		step09Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step09_pointSynapses();
			}
		});
		step09Button.setSize(new Dimension(0, 50));
		step09Button.setPreferredSize(new Dimension(0, 50));
		step09Button.setIconTextGap(10);
		step09Button.setHorizontalAlignment(SwingConstants.LEFT);
		contentPane.add(step09Button);
		step10Button.setIconTextGap(10);
		step10Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_10.jpg")));
		step10Button.setHorizontalAlignment(SwingConstants.LEFT);
		step10Button.setPreferredSize(new Dimension(0, 50));
		step10Button.setSize(new Dimension(0, 50));
		sl_contentPane.putConstraint(SpringLayout.WEST, step10Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step10Button, 0, SpringLayout.EAST, step03Button);
		contentPane.add(step10Button);
		
		step11Button = new JButton("Step 11: Summarize and graph");
		sl_contentPane.putConstraint(SpringLayout.NORTH, step11Button, 0, SpringLayout.SOUTH, step10Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step11Button, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, step11Button, 0, SpringLayout.EAST, contentPane);
		step11Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step11_summarizeAndGraph();
			}
		});
		step11Button.setIconTextGap(10);
		step11Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_11.jpg")));
		step11Button.setHorizontalAlignment(SwingConstants.LEFT);
		step11Button.setPreferredSize(new Dimension(0, 50));
		step11Button.setSize(new Dimension(0, 50));
		contentPane.add(step11Button);
		
		step12Button = new JButton("Step 12: Generate CPA files");
		step12Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_12.jpg")));
		sl_contentPane.putConstraint(SpringLayout.NORTH, step12Button, 0, SpringLayout.SOUTH, step11Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step12Button, 0, SpringLayout.WEST, step01Button);
		sl_contentPane.putConstraint(SpringLayout.EAST, step12Button, 0, SpringLayout.EAST, contentPane);
		step12Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step12_generateCPAFiles();
			}
		});
		step12Button.setIconTextGap(10);
		step12Button.setHorizontalAlignment(SwingConstants.LEFT);
		step12Button.setPreferredSize(new Dimension(0, 50));
		step12Button.setSize(new Dimension(0, 50));
		contentPane.add(step12Button);
		
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.WEST, progressBar, 0, SpringLayout.WEST, step01Button);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, step01Button);
		progressBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				debugLog();
			}
		});
		
		step13Button = new JButton("Step 13: Egg timer");
		step13Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				step13_eggTimer();
			}
		});
		step13Button.setIcon(new ImageIcon(KymoToolSet_.class.getResource("/ressources/Step_13.jpg")));
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 6, SpringLayout.SOUTH, step13Button);
		sl_contentPane.putConstraint(SpringLayout.NORTH, step13Button, 0, SpringLayout.SOUTH, step12Button);
		sl_contentPane.putConstraint(SpringLayout.WEST, step13Button, 0, SpringLayout.WEST, step01Button);
		sl_contentPane.putConstraint(SpringLayout.EAST, step13Button, 0, SpringLayout.EAST, contentPane);
		step13Button.setSize(new Dimension(0, 50));
		step13Button.setPreferredSize(new Dimension(0, 50));
		step13Button.setIconTextGap(10);
		step13Button.setHorizontalAlignment(SwingConstants.LEFT);
		contentPane.add(step13Button);
		contentPane.add(progressBar);
	}
	
	/**
	 * Creates the folders for the analysis output, asks for the parameters and pre-process the images
	 */
	public static void step01_createFoldersAndPreprocess(){
		if(filesAndFolders.selectAndCreateFolders()){
			if(gui.preprocessGUI()){
				/*
				 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
				 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
				 */
				Thread launch = new Thread(new Runnable(){
			        public void run(){
			        	for(int i=0; i<settings.cellsList.length; i++){
			    			updateProgressBar(0, settings.cellsList.length, i+1, "Pre-processing Cell"+(i+1)+"/"+settings.cellsList.length);
				    		preProcessing.doPreProcessing(settings.cellsList[i]);
				    	}
			        	settings.writeLog();
			        	updateProgressBar(0, 0, 0, "Pre-processing done");
			    	}
				});
				launch.start();
			}
		}
	}
	
	/**
	 * Generates views and movies, ex for PPT presentations
	 */
	public static void step02_createMovies(){
		if(checks.checkAll()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		    			updateProgressBar(0, settings.cellsList.length, i+1, "Movies generation for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		viewsGenerator.generateViews(settings.cellsList[i]);
		    		}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Movies generation done");
		    	}
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Movies generation failed");
		}
	}
	
	/**
	 * Performs the flux analysis
	 */
	public static void step03_analyzeFlux(){
		if(checks.checkAll()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		    			updateProgressBar(0, settings.cellsList.length, i+1, "Flux analysis for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		analysis.analyzeFlux(settings.cellsList[i]);
		    		}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Flux analysis done");
		    	}
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Flux analysis failed");
		}
	}
	
	/**
	 * Performs the 3 previous steps: creates the folders and pre-process data, creates movies,
	 * analyze flux
	 */
	public static void step04_steps1_3(){
		if(filesAndFolders.selectAndCreateFolders() && gui.preprocessGUI()){
			if(settings.isGCampExperiment) checks.checkGCampLength();
			
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		    			updateProgressBar(0, settings.cellsList.length, i+1, "Batch processing Cell"+(i+1)+"/"+settings.cellsList.length);
			    		preProcessing.doPreProcessing(settings.cellsList[i]);
		    			viewsGenerator.generateViews(settings.cellsList[i]);
		    			analysis.analyzeFlux(settings.cellsList[i]);
		    		}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Batch pre-processing done");
		        }
			});
			launch.start();
		}
	}
	
	/**
	 * Performs stacks' projections, based on manual definition of the range of slices to project
	 */
	public static void step05_generateProjections(){
		if(checks.checkWorkingDirectory()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		    			updateProgressBar(0, settings.cellsList.length, i+1, "Projections generation for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		viewsGenerator.doManualProjection(settings.cellsList[i]);
		    		}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Projections generation done");
		    	}
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Projections generation failed");
		}
	}
	
	public static void step06_recordMainPaths(){
		if(checks.checkWorkingDirectory()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		        		updateProgressBar(0, settings.cellsList.length, i+1, "Paths recording for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		filesAndFolders.setFolders(settings.cellsList[i]);
		        		String jpg=settings.destForPPT+settings.currCell+"_FRAP_Zone.jpg";
		        		if(new File(jpg).exists()){
		        			ImagePlus ip=new ImagePlus(jpg);
		        			analysis.recordPaths(settings.cellsList[i], true, settings.destProj, settings.destKymosPaths);
		        			ip.changes=false;
		        			ip.close();
		        		}else{
		        			System.out.println("The file "+jpg+" does not exist.\nDid you use the \"Create movies\" or the \"3 steps\" button ?");
		        		}
		        	}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Paths recording done");
		        }
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Paths recording failed");
		}
	}
	
	public static void step07_createKymographs(){
		if(/*checks.checkFolderAndBleach() &&*/ checks.checkCalib()){
	    	/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		        		updateProgressBar(0, settings.cellsList.length, i+1, "Generating kymos for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		analysis.multipleDrawKymo(settings.cellsList[i]);
		        	}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Kymos generation done");
		        }
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Kymos generation failed");
		}
	}
	
	public static void step08_recordVesiculesPaths(){
		if(checks.checkWorkingDirectory()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		        		updateProgressBar(0, settings.cellsList.length, i+1, "Recording vesic. path for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		analysis.recordPaths(settings.cellsList[i], false, settings.destKymosKymos, settings.destKymosROIs);
		        	}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Vesic. path recording done");
		        }
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Vesic. path recording failed");
		}
	}
	
	public static void step09_pointSynapses(){
		if(checks.checkWorkingDirectory() && checks.checkCalib() && checks.checkLambda() && checks.checkSynapseZone()){
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	for(int i=0; i<settings.cellsList.length; i++){
		        		updateProgressBar(0, settings.cellsList.length, i+1, "Recording synapses' positions for Cell"+(i+1)+"/"+settings.cellsList.length);
			    		analysis.shiftPaths(settings.cellsList[i]);
			    		new analysis().pointSynapses(settings.cellsList[i]);
		        	}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Synapses' positions recording done");
		        }
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Synapses' positions recording failed");
		}
	}
	
	public static void step10_analyzeKymographs(){
		if(checks.checkWorkingDirectory() && /*checks.checkFolderAndBleach() &&*/ checks.checkCalib() && checks.checkLambda() && checks.checkSynapseZone()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
		        public void run(){
		        	
		        	for(int i=0; i<settings.cellsList.length; i++){
		        		updateProgressBar(0, settings.cellsList.length, i+1, "Analysing kymos from Cell"+(i+1)+"/"+settings.cellsList.length);
			    		analysis.analyseKymo(settings.cellsList[i]);
		        	}
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Kymos analysis done");
		        }
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Kymos analysis failed");
		}
	}
	
	public static void step11_summarizeAndGraph(){
		if(checks.checkWorkingDirectory() && checks.checkSynapseZone()){
			/*
			 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
			 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
			 */
			Thread launch = new Thread(new Runnable(){
				public void run(){
		        	analysis.writeXLSLabels();
		        	for(int i=0; i<settings.cellsList.length; i++){
		        		updateProgressBar(0, settings.cellsList.length, i+1, "Pooling data from Cell"+(i+1)+"/"+settings.cellsList.length);
			    		analysis.writeXLSContent(settings.cellsList[i]);
		        	}
		        	analysis.writeXLSFlux();
		        	analysis.writeXLSSynapses();
		        	analysis.tagAllDataWithSynapsesInfos();
		        	settings.writeLog();
		        	updateProgressBar(0, 0, 0, "Pooling data done");
		        	
		        	RScript.writeRScript();
		        	try {
		        		String cmd="R CMD BATCH kymoScript.R";
		        		if(IJ.isMacintosh()) cmd="/usr/local/bin/"+cmd;
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
			});
			launch.start();
		}else{
			updateProgressBar(0, 0, 0, "Pooling data failed");
		}
	}
	
	public static void step12_generateCPAFiles(){
		/*
		 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
		 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
		 */
		Thread launch = new Thread(new Runnable(){
	        public void run(){
	        	
	        }
		});
		launch.start();
	}
	
	public static void step13_eggTimer(){
		/*
		 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
		 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
		 */
		Thread launch = new Thread(new Runnable(){
	        public void run(){
	        	utilities.eggTimer();
	        }
		});
		launch.start();
	}
	
	public static void updateProgressBar(int min, int max, int position, String text){
		progressBarMin=min;
		progressBarMax=max;
		progressBarPosition=position;
		progressBarText=text;
		
		/*
		 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
		 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
		 */
		
		Thread launch = new Thread(new Runnable(){
	        public void run(){
	        	progressBar.setMinimum(progressBarMin);
	        	progressBar.setMaximum(progressBarMax);
	        	progressBar.setValue(progressBarPosition);
	        	progressBar.setStringPainted(true);
	        	progressBar.setString(progressBarText);
	        	progressBar.validate();
	        	progressBar.repaint();
	        }
		});
		launch.start();
	}
	
	/**
	 * Sends the content of all variables to the ImageJ log window
	 */
	public static void debugLog(){
		/*
		 * Creating a new Thread is absolutely required to have Swing and AWT-ImageJ display updated separately.
		 * Otherwise the ImagePlus are only updated once the ActionPerformed Thread is over (at the end of the process)
		 */
		Thread launch = new Thread(new Runnable(){
	        public void run(){
	        	IJ.log(settings.getDebugLog());
	        }
		});
		launch.start();
	}
	
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		main(new String[]{arg});
	}
}
