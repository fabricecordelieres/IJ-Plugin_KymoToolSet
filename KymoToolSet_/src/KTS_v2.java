import analysis.GUI;
import ij.IJ;
import ij.plugin.PlugIn;

/**
  * test.java v1, 1 août 2016
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
public class KTS_v2 implements PlugIn{

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		//Required to have proper export of xls files
		IJ.run("Input/Output...", "jpeg=85 gif=-1 file=.xls save_column");
		
		//Check for plugins and run
		Class<?> kymoToolBox=null;
		Class<?> MMCompanion=null;
		
		try {
			kymoToolBox = Class.forName("Analyse_Kymo");
			MMCompanion = Class.forName("plugins.ndFile.buildNdStack");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(kymoToolBox!=null && MMCompanion!=null){
			new GUI().setVisible(true);
		}else{
			String message="Please install first the following plugin(s):\n";
			if(kymoToolBox==null) message+="KymoToolBox";
			if(kymoToolBox==null && MMCompanion==null) message+=" and ";
			if(MMCompanion==null) message+="MMCompanion";
			IJ.error(message);
		}
	}

}
