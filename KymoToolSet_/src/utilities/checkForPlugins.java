/**
  * checkForPlugins.java v1, 1 août 2016
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
package utilities;

import analysis.GUI;

/**
 * This class aims at checking for the presence of specific plugins
 * @author Fabrice P Cordelieres
 *
 */
public class checkForPlugins {
	/**
	 * Checks if the MM companion plugin is installed
	 * @return true if the plugin is installed, false otherwise
	 */
	public static boolean checkForMMCompanion(){
		try{
			Class.forName("ndFile.ndFile");
			GUI.log.logInfo("The Metamorph Companion Plugin is installed");
			return true;
		}catch(ClassNotFoundException e){
			GUI.log.logInfo("The Metamorph Companion Plugin is not installed");
			return false;
		}
	}
	
	/**
	 * Checks if the Pure Denoise plugin is installed
	 * @return true if the plugin is installed, false otherwise
	 */
	public static boolean checkForPureDenoise(){
		try{
			Class.forName("denoise.Denoising");
			GUI.log.logInfo("The Pure Denoise Plugin is installed");
			return true;
		}catch(ClassNotFoundException e){
			GUI.log.logInfo("The Pure Denoise Plugin is not installed");
			return false;
		}
	}
}
