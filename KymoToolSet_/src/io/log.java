/**
  * log.java v1, 2 août 2016
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ij.IJ;

/**
 * This class handles log operations
 * @author Fabrice P Cordelieres
 *
 */
public class log {
	String path=null;
	String name=null;
	
	/**
	 * Build a new log file in the path folder, named after the provided title
	 * @param path path where the file should be saved
	 * @param name name of the file
	 */
	public log(String path, String name){
		this.path=path.endsWith(File.separator)?path:path+File.separator;
		this.name=name.toLowerCase().endsWith(".txt")?name:name+".txt";
	}
	
	/**
	 * Build a new log file in the path folder, named after the current date
	 * @param path path where the file should be saved
	 * @param name name of the file
	 */
	public log(String path){
		this(path, new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+"_KymoToolSet");
	}
	
	/**
	 * Logs into the file the current date/time, followed by the input info
	 * @param info the info to log
	 */
	public void logInfo(String info){
		FileWriter log=null;
		try {
			File f=new File(path+name);
			log=new FileWriter(f, f.exists());
		} catch (IOException e) {
			System.out.println("Could not start logging infos in "+path+", "+name);
			e.printStackTrace();
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd (HH:mm:ss)");
		Date date = new Date();
		try {
			IJ.log(dateFormat.format(date)+": "+info);
			log.write(dateFormat.format(date)+": "+info+"\n");
		} catch (IOException e) {
			System.out.println("Could not log infos in log file");
			e.printStackTrace();
		}
		
		try {
			log.close();
		} catch (IOException e) {
			System.out.println("Could not close log file");
			e.printStackTrace();
		}
	}
}
