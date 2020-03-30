package utilities;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.CanvasResizer;
import ij.plugin.RoiRotator;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.imageio.ImageIO;

/**
 * This class handles various operations
 * @author fab
 *
 */
public class utilities {
	public static final String MICRO_SIGN="\u00B5"+"m";
	
	/**
	 * Saves a group of data as a tab delimited values file
	 * @param headers the header of the columns
	 * @param values the values to write
	 * @param fullPath the full path, including filename
	 */
	public static void saveAsCSV(String[] headers, ArrayList<String[]> values, String fullPath){
		try {
			FileWriter fw= new FileWriter(fullPath);
			fw.write(stringArrayToDelimitedString("", headers, "\t", ""));
			for(int i=0; i<values.size(); i++) fw.write(stringArrayToDelimitedString("\n", values.get(i), "\t", ""));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves an array of String values to a file
	 * @param data the values to write
	 * @param fullPath the full path, including filename
	 */
	public static void saveAsCSV(String[] data, String fullPath){
		try {
			FileWriter fw= new FileWriter(fullPath);
			for(int i=0; i<data.length; i++) fw.write((i==0?"":"\n")+data[i]);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Transforms an array of String into a single String where start/end and delimiters might be customized
	 * @param startOfString start string
	 * @param stringArray the string array to parse
	 * @param delimiter separator used between the elements of the array
	 * @param endOfString end string
	 * @return
	 */
	public static String stringArrayToDelimitedString(String startOfString, String[] stringArray, String delimiter, String endOfString){
		String out=startOfString;
		if(stringArray!=null) for(int i=0; i<stringArray.length; i++) out+=(i==0?"":delimiter)+stringArray[i];
		
		return out+endOfString;
	}

	/**
	 * Concatenates two arrays of String
	 * @param array1 the first array of String
	 * @param array2 the second array of String
	 * @return a single array of String
	 */
	public static String[] concatenate(String[] array1, String[] array2){
		int length1=array1==null?0:array1.length;
		int length2=array2==null?0:array2.length;
		
		if(length1==0 && length2==0){
			return null;
		}else{
			String[] out=new String[length1+length2];
			
			for(int i=0; i<length1; i++) out[i]=array1[i];
			for(int i=0; i<length2; i++) out[i+length1]=array2[i];
			
			return out;
		}
	}
	
	/**
	 * Draws scale bar, both in time and space, on the input ImageProcessor
	 * @param iproc the ImageProcessor on which the scale bar should be drawn
	 * @param scaleMicrons size of the spatial scale bar
	 * @param scaleSeconds size of the temporal scale bar
	 * @param cal image's calibration
	 */
	public static void addScaleBar(ImageProcessor iproc, double scaleMicrons, double scaleSeconds, Calibration cal){
		String labelSize=scaleMicrons+MICRO_SIGN+"m";
		String labelTime=scaleSeconds+"sec";
		
		Font font=iproc.getFont();
		int fontSize=font.getSize();
		
		int x1=(int) (fontSize*1.25);
		int y1=(int) (fontSize*1.25);
		int x2=(int) (x1+scaleMicrons/cal.pixelWidth);
		int y2=y1;
		int x3=x1;
		int y3=(int) (y1+scaleSeconds/cal.frameInterval);
		
		iproc.setColor(Color.WHITE);
		iproc.drawLine(x1, y1, x2, y2);
		iproc.drawString(labelSize, x1, y1);
		
		iproc.drawLine(x1, y1, x3, y3);
		
		ImageProcessor tmp=iproc.rotateLeft();
		tmp.setColor(Color.WHITE);
		tmp.setFont(font);
		tmp.drawString(labelTime, y1, tmp.getHeight());
		iproc.setIntArray(tmp.rotateRight().getIntArray());
	}
	
	/**
	 * Reads the content of a file and returns it as a String
	 * @param fullPath the fullPath to the file
	 * @return the content of a file as a String
	 */
	public static String readFileAsString(String fullPath){
		String out="";
		
		try {
			BufferedReader br=new  BufferedReader(new FileReader(fullPath));
			String line;
			line = br.readLine();
			while(line!=null){
				out+=line+"\n";
				line=br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	/**
	 * Writes the content of a String into a file
	 * @param fullPath the fullPath to the file
	 * @param string2write the sString to write
	 */
	public static void writeStringToFile(String fullPath, String string2write){
		try {
			FileWriter fr= new FileWriter(fullPath);
			fr.write(string2write);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Appends the content of a String into a file
	 * @param fullPath the fullPath to the file
	 * @param string2write the sString to append
	 */
	public static void appendStringToFile(String fullPath, String string2write){
		try {
			String content=readFileAsString(fullPath);
			FileWriter fw= new FileWriter(fullPath);
			fw.append(content);
			fw.append(string2write);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the current date and time as a String
	 * @return the current date and time as a String
	 */
	public static String getCurrentDateAndTime(){
		Calendar cal=Calendar.getInstance();
		
		return 	 cal.get(Calendar.YEAR)+"/"+pad((cal.get(Calendar.MONTH)+1), 2)+"/"+pad(cal.get(Calendar.DAY_OF_MONTH), 2)+" "
				+pad(cal.get(Calendar.HOUR_OF_DAY), 2)+":"+pad(cal.get(Calendar.MINUTE), 2)+":"+pad(cal.get(Calendar.SECOND), 2);
	}
	
	/**
	 * Pads the input int to the specified number of digits
	 * @param input the integer to pad
	 * @param nDigits the final number of digits
	 * @return a padded version of the input integer
	 */
	public static String pad(int input, int nDigits){
		String out=input+"";
		while(out.length()<nDigits) out="0"+out;
		return out;
	}
	
	/**
	 * Rounds the input double to the specified number of digits
	 * @param time the double to round
	 * @param nDigits the final number of digits
	 * @return a rounded version of the input double
	 */
	public static String round(double time, int nDigits){
		double multiplier=Math.pow(10, nDigits);
		return (((int)Math.round(time*multiplier))/multiplier)+"";
	}
	
	/**
	 * Returns an ImagePlus that is enlarged, if necessary, to accommodate a rotated version by
	 * an input angle of the input ImagePlus
	 * @param in input ImagePlus
	 * @param angle angle of the rotation
	 * @return a rotated version of the input ImagePlus
	 */
	public static ImagePlus rotate(ImagePlus in, double angle){
		Rectangle r=RoiRotator.rotate(new Roi(0, 0, in.getWidth(), in.getHeight()), angle).getBounds();
		if (r.getWidth()<in.getWidth()) r.width=in.getWidth();
        if (r.getHeight()<in.getHeight()) r.height=in.getHeight();
        ImagePlus out=new ImagePlus("", new CanvasResizer().expandStack(in.getImageStack(), (int) r.getWidth(), (int) r.getHeight(), (int) (r.getWidth()-in.getWidth())/2, (int) (r.getHeight()-in.getHeight())/2));
        
        for(int i=1; i<=in.getStackSize(); i++){
        	out.setPosition(i);
        	out.getProcessor().rotate(angle);
        }
        return out;
    }
	
	/**
	 * Reads a synapses' detection file, gets their coordinates and return them as a Polygon
	 * @param fullPath the full path to the synapses detection file
	 * @return a polygon created form the synapses' coordinates
	 */
	public static Polygon synapsesFileToPolygon(String fullPath){
		Polygon p=new Polygon();
		
		try {
			BufferedReader br=new  BufferedReader(new FileReader(fullPath));
			String line="";
			
			//Skips the 4 first lines (infos/headers), keeps the 5th for reading coordinates
			for(int i=0; i<5; i++) line = br.readLine();
			
			while(line!=null){
				String[] coordinates=line.split("\t");
				p.addPoint(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
				line=br.readLine();
			}
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return p;
	}
	
	/**
	 * Implements an egg timer
	 */
	public static void eggTimer(){
		
		String[] cuissons=new String[]{"Oeuf a la coque - 3'", "Oeuf mollet - 5'", "Oeuf dur - 10'", "Autre"};
		
		//GUI
		GenericDialog gd=new GenericDialog("Minuteur à oeuf");
		gd.addRadioButtonGroup("Cuisson", cuissons, 4, 1, cuissons[0]);
		gd.addNumericField("Autre durée (min)", 0.6, 2);
		gd.showDialog();
		
		if(gd.wasOKed()){
		
			String choice=gd.getNextRadioButton();
			int time=0;
			if(choice.equals(cuissons[0])) time=180;
			if(choice.equals(cuissons[1])) time=300;
			if(choice.equals(cuissons[2])) time=600;
			if(choice.equals(cuissons[3])) time=(int) (gd.getNextNumber()*60);
			
			WindowManager.closeAllWindows();
			
			ImagePlus ip=NewImage.createImage("Minuteur à oeuf", 256, 128, 1, 24, NewImage.FILL_BLACK);
			ImageProcessor iproc=ip.getProcessor();
			ip.show();
			
			for(int i=time; i>=0; i--){
				int min=(int) Math.floor(i/60);
				int sec=i-min*60;
				
				if(i>30){
					iproc.setColor(Color.BLACK);
				}else{
					iproc.setColor(Color.RED);
				}
				
				iproc.setRoi(0, 0, ip.getWidth(), ip.getHeight());
				iproc.fill();
				iproc.setFont(new Font("Arial", Font.BOLD, 100));
				iproc.setAntialiasedText(true);
				iproc.setJustification(ImageProcessor.CENTER_JUSTIFY);
				
				if(i>30){
					iproc.setColor(Color.WHITE);
				}else{
					iproc.setColor(Color.GREEN);
				}
				
				iproc.drawString(utilities.pad(min,  2)+":"+utilities.pad(sec,  2), 142, 120);
				ip.updateAndDraw();
				IJ.wait(1000);
			}
			
			ip.close();
			
			//Display of the chick
			ImagePlus chicken=null;
			InputStream is=ClassLoader.getSystemResourceAsStream("ressources/chicken.jpg");
			try {
				BufferedImage img=ImageIO.read(is);
				chicken=new ImagePlus("Time is over !", img);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(chicken !=null){
				chicken.show();
				iproc=chicken.getProcessor();
				iproc.setFont(new Font("Arial", Font.BOLD, 30));
				iproc.setAntialiasedText(true);
				iproc.setJustification(ImageProcessor.CENTER_JUSTIFY);
				
				for(int i=0; i<30; i++){
					Toolkit.getDefaultToolkit().beep();
					IJ.wait(135);
					if(i/2.0==Math.floor(i/2)){
						iproc.setColor(Color.RED);
					}else{
						iproc.setColor(Color.ORANGE);
					}
					iproc.drawString("I'm cooked !!!", 300, 62);
					chicken.updateAndDraw();
				}
				
			}
			
		}
	}
}
