/**
  * CPAFilesGenerator.java v1, 10 juil. 2015
    Fabrice P Cordelieres, fabrice.cordelieres at gmail.com
    
    Copyright (C) 2015 Fabrice P. Cordelieres
  
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
package steps;

import java.io.File;
import java.util.ArrayList;

/**
 * 
 * @author Fabrice P Cordelieres
 *
 */
public class CPAFilesGenerator {
	/** The input directory, the upper level for data to pull **/
	String dir=null;
	
	/** List of all folders within the pointed out directory **/
	ArrayList<String> experiments=new ArrayList<String>();
	
	
	
	
	public CPAFilesGenerator(String dir) {
		this.dir=dir;
		getFoldersList(dir);
	}
	
	
	/**
	 * Retrieved the names of the folders within the input directory
	 * @param dir the input directory
	 * @return the folders' names, as a String ArrayList
	 */
	private ArrayList<String> getFoldersList(String dir){
		ArrayList<String> out=new ArrayList<String>();
		
		String[] filesList=new File(dir).list();
		
		for(int i=0; i<filesList.length; i++) if(new File(dir+filesList[i]).isDirectory()) out.add(filesList[i]);
		
		return out;
		
	}

}
