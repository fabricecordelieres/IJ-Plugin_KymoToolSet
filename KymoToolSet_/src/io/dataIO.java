/**
  * dataImporter.java v1, 5 août 2016
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import analysis.GUI;

/**
 * This files handles import of numerical data as xls files
 * @author Fabrice P Cordelieres
 *
 */
public class dataIO {
	/** The root path to the file to read **/
	public String path=null;
	
	/** The name of the file to read **/
	public String filename=null;
	
	/** Columns headers **/
	public ArrayList<String> header=null;
	
	/** Core data **/
	public ArrayList<String[]> data=null;
	
	
	/**
	 * Builds a new empty data Importer
	 */
	public dataIO(){}
	
	/**
	 * Builds the new data importer, based on a header and data
	 * @param fullPath the full path
	 */
	public dataIO(String fullPath){
		File f=new File(fullPath);
		path=f.getParent()+File.separator;
		filename=f.getName();
		
		readData();
	}
	
	/**
	 * Builds the new data importer, based on a path and filename
	 * @param path the parent path to the file
	 * @param filename the filename
	 */
	public dataIO(String path, String filename){
		this.path=path;
		this.filename=filename;
		readData();
	}
	
	/**
	 * Builds the new data importer, based on a header and data
	 * @param header header as an ArrayList<String>
	 * @param data data as an ArrayList<String[]>
	 */
	public dataIO(ArrayList<String> header, ArrayList<String[]> data){
		this.header=header;
		this.data=data;
	}

	/**
	 * Returns the header data as an ArrayList of Strings
	 * @return the header data as an ArrayList of Strings
	 */
	public ArrayList<String> getHeader(){
		return header;
	}
	
	/**
	 * Adds empty column at the input position, and shift all data right. Also takes care of the header
	 * @param position the position at which to ass empty header/data
	 */
	public void addEmptyColumn(int position){
		if(position<header.size()){
			header.add(position, "");
			for(int i=0; i<data.size(); i++){
				String[] currLine=data.get(i);
				String[] newLine=new String[currLine.length+1];
				
				for(int j=0; j<position; j++) newLine[j]=currLine[j];
				newLine[position]="";
				for(int j=position; j<currLine.length; j++) newLine[j+1]=currLine[j];
				
				data.set(i, newLine);
			}
		}
	}
	
	
	/**
	 * Returns the data as 1D String array
	 * @return the data as 1D String array
	 */
	public String[] getHeaderAsStringArray(){
		String[] out=null;
		
		int nCols=header.size();
		
		if(nCols!=0){
			out=new String[nCols];
			
			for(int i=0; i<nCols; i++) out[i]=header.get(i);
		}else{
			GUI.log.logInfo("No header found in "+path+filename);
		}
		
		return out;
	}
	
	/**
	 * Returns the header as a String, tab delimited
	 * @return the data as a String, tab delimited
	 */
	public String getHeaderAsString(){
		String out="";
		
		int nCols=header.size();
		
		if(nCols!=0){
			out+=header.get(0);
			
			for(int i=0; i<nCols; i++) out+="\t"+header.get(i);
		}else{
			GUI.log.logInfo("No header found in "+path+filename);
		}
		
		return out;
	}
	
	/**
	 * Returns the data as an ArrayList of String array
	 * @return the data as an ArrayList of String array
	 */
	public ArrayList<String[]> getData(){
		return data;
	}
	
	/**
	 * Returns the data as 2D String array (first index: row, second index: column)
	 * @return the data as 2D String array (first index: row, second index: column)
	 */
	public String[][] getDataAsStringArray(){
		String[][] out=null;
		
		int nRows=data.size();
		
		if(nRows!=0){
			int nCols=data.get(0).length;
			out=new String[nRows][nCols];
			
			for(int i=0; i<nRows; i++) out[i]=data.get(i);
		}else{
			GUI.log.logInfo("No data found in "+path+filename);
		}
		
		return out;
	}
	
	/**
	 * Reads the data
	 */
	public void readData(){
		try {
			header=new ArrayList<String>();
			data=new ArrayList<String[]>();
			File f=new File(path+filename);
			
			if(f.exists()){
				FileReader fr=new FileReader(f);
				BufferedReader br=new BufferedReader(fr);
				
				//Reads header
				String line=br.readLine();
				if(!line.isEmpty()){
					String[] tmp=line.split("\t");
					for(int i=0; i<tmp.length; i++) header.add(tmp[i]);
				}
				
				//Prepares next line reading
				line=br.readLine();
				
				//Reads data
				while(line!=null){
					data.add(line.split("\t"));
					line=br.readLine();
				}
				
				br.close();
				fr.close();
			}else{
				GUI.log.logInfo("Could not find file "+path+filename);
			}
		} catch (IOException e) {
			GUI.log.logInfo("Could not read infos from "+path+filename);
			e.printStackTrace();
		}
	}
	
	/**
	 * Return a dataIO containing only line between startLine and endLine
	 * @param startLine the start data line
	 * @param endLine the end data line
	 * @return a dataIO object
	 */
	public dataIO cropData(int startLine, int endLine){
		ArrayList<String[]> dataTmp=new ArrayList<String[]>();
		ArrayList<String> headerTmp=header;
		
		startLine=Math.min(Math.max(0, startLine), data.size()-1);
		endLine=Math.max(0, Math.min(endLine, data.size()-1));
		
		for(int i=startLine; i<=endLine; i++) dataTmp.add(data.get(i));
		
		return new dataIO(headerTmp, dataTmp);
	}
	
	/**
	 * Writes the data to the input path
	 * @param path path to which the data should be saved
	 */
	public void writeData(String path){
		try {
			File f=new File(path);
			if(!new File(f.getParent()).exists()) new File(f.getParent()).mkdirs();
			
			FileWriter out=new FileWriter(f, false);
			
			String line="";
			//Write header
			if(header.size()>0){
				line=header.get(0);
				for(int i=1; i<header.size(); i++) line+="\t"+header.get(i);
				out.write(line);
			}
			
			//Write data
			if(data.size()>0){
				for(int i=0; i<data.size(); i++){
					line="\n";
					for(int j=0; j<data.get(i).length; j++) line+=(j==0?"":"\t")+data.get(i)[j];
					out.write(line);
				}
			}
			out.close();		
		} catch (IOException e) {
			GUI.log.logInfo("Could not start logging infos in "+path);
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString(){
		String out="";
		
		if(header!=null && data!=null){
			for(int i=0; i<header.size(); i++) out+=(i!=0?"\t":"")+header.get(i);
			
			for(int i=0; i<data.size(); i++){
				String[] line=data.get(i);
				if(line.length!=0) out+="\n";
				for(int j=0; j<line.length; j++) out+=(j!=0?"\t":"")+line[j];
			}
		}else{
			GUI.log.logInfo("No data found in "+path+filename);
		}
		
		return out;
	}
}
