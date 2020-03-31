/**
  * prismOutput.java v1, 1 sept. 2016
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
import java.util.ArrayList;
import java.util.Date;

/**
 * This class aims at creating the content of a Prism file (only the data/tables part)
 * @author Fabrice P Cordelieres
 *
 */
public class prismOutput {
	
	/** Stores the data contained in each Prism table **/
	ArrayList<String> tables=new ArrayList<String>();
	
	/**
	 * Creates the Prism file header
	 * @return the Prism file header
	 */
	public String getHeader(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?><GraphPadPrismFile PrismXMLVersion=\"5.00\">\n"+
				"<Created>\n"+
				"<OriginalVersion CreatedByProgram=\"GraphPad Prism\" CreatedByVersion=\"0.00.000\" RegisteredTo=\"      \" Login=\"\" DateTime=\""+getFullDate()+"\"></OriginalVersion>\n"+
				"</Created>\n"+
				"<InfoSequence>\n"+
				"<Ref ID=\"Info0\" Selected=\"1\"></Ref>\n"+
				"</InfoSequence>\n"+
				"<Info ID=\"Info0\">\n"+
				"<Title>Project info 1</Title>\n"+
				"<Notes>\n"+
				"</Notes>\n"+
				"<Constant><Name>Experiment Date</Name><Value>"+getDate()+"</Value></Constant>\n"+
				"<Constant><Name>Experiment ID</Name><Value></Value></Constant>\n"+
				"<Constant><Name>Notebook ID</Name><Value></Value></Constant>\n"+
				"<Constant><Name>Project</Name><Value></Value></Constant>\n"+
				"<Constant><Name>Experimenter</Name><Value></Value></Constant>\n"+
				"<Constant><Name>Protocol</Name><Value></Value></Constant>\n"+
				"</Info>\n\n";
	}
	
	/**
	 * Creates the Prism file table sequence
	 * @return the Prism file table sequence
	 */
	public String getTableSequence(){
		String out = "<TableSequence Selected=\"1\">\n";
		for(int i=0; i<tables.size(); i++) out+="\n<Ref ID=\"Table"+i+"\""+(i==0?" Selected=\"1\"":"")+"></Ref>";
		out+="\n</TableSequence>\n";
		
		return out;
	}
	
	/**
	 * Generates a new Prism table from input data
	 * @param titleTable the title of the table
	 * @param titleColumn the columns headers
	 * @param data the numerical data to push
	 */
	public void addTable(String titleTable, ArrayList<String> titleColumn, ArrayList<String[]> data){
		String out=	"<Table ID=\"Table"+tables.size()+"\" XFormat=\"numbers\" YFormat=\"replicates\" Replicates=\"1\" TableType=\"XY\" EVFormat=\"AsteriskAfterNumber\">\n"+
					"<Title>"+titleTable+"</Title>\n";
		
		//Deals with the rows' labels
		String[] columnsStarts=new String[]{"<RowTitlesColumn Width=\"89\">\n", "<XColumn Width=\"81\" Decimals=\"1\" Subcolumns=\"1\">\n", "<YColumn Width=\"183\" Decimals=\"3\" Subcolumns=\"1\">\n"};
		String[] columnsEnds=new String[]{"</RowTitlesColumn>\n", "</XColumn>\n", "</YColumn>\n"};
		
		for(int i=0; i<titleColumn.size(); i++){
			out+=	columnsStarts[i<3?i:2]
					+(i==0?"":"<Title>"+titleColumn.get(i)+"</Title>\n")+
					"<Subcolumn>\n";
			
			for(int j=0; j<data.size(); j++) out+="<d>"+(i<data.get(j).length?data.get(j)[i]:"").replace(".", ",").replace(">", "-").replace("<", "-")+"</d>\n";
			
			out+="</Subcolumn>\n"+columnsEnds[i<3?i:2];
		}
		out+="</Table>\n";
		tables.add(out);
	}
	
	/**
	 * Generates the content of the Prism file and returns it as a String
	 * @return the content of the Prism file and returns it as a String
	 */
	public String getPrismFile(){
		String out=getHeader();
		out+=getTableSequence();
		
		for(int i=0; i<tables.size(); i++) out+=tables.get(i);
		
		out+="</GraphPadPrismFile>";
		
		return out;
	}
	
	/**
	 * Returns current full date formatted for the Prism file ex: 2016-09-01T11:53:49+02:00
	 * @return current full date formatted for the Prism file
	 */
	public static String getFullDate(){
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		String dateString=dateFormat.format(date);
		dateString=dateString.substring(0, dateString.length()-2)+":"+dateString.substring(dateString.length()-2);
        return dateString;
	}
	
	/**
	 * Returns current date, only the date, formatted for the Prism file ex: 2016-09-01
	 * @return current date, only the date, formatted for the Prism file
	 */
	public static String getDate(){
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateString=dateFormat.format(date);
        return dateString;
	}
	
	/**
	 * Writes the prism file
	 * @param path path to which the data should be saved
	 */
	public void write(String path){
		try {
			File f=new File(path);
			if(!new File(f.getParent()).exists()) new File(f.getParent()).mkdirs();
			
			FileWriter out=new FileWriter(f, false);
			out.write(getPrismFile());
			out.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
