/**
  * imagePreProcessing.java v1, 3 août 2016
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
package processing;

import Utilities.kymograph.improveKymo;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.RankFilters;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * This class aims at performing image pre-processing
 * @author Fabrice P Cordelieres
 *
 */
public class imagePreProcessing {
	/**
	 * Performs wavelet transform (a trou) and/or top hat filtering over the input image
	 * @param ip input ImagePlus
	 * @param minWaveletRadius minimum radius of the wavelet transform, or -1 to omit this step
	 * @param maxWaveletRadius maximum radius of the wavelet transform, or -1 to omit this step
	 * @param topHatRadius top-hat radius, or -1 to omit this step
	 * @return the filtered ImagePlus
	 */
	public static ImagePlus waveletAndTopHat(ImagePlus ip, int minWaveletRadius, int maxWaveletRadius, int topHatRadius){
		String title=ip.getTitle();
		
		ImagePlus result=ip.duplicate();
		
		//Wavelet part
		if(minWaveletRadius!=-1 && maxWaveletRadius!=-1) result=new improveKymo(result).getFilteredImage(minWaveletRadius, maxWaveletRadius);
		
		//Top-Hat part
		if(topHatRadius!=-1){
			ImagePlus bkgd=result.duplicate();
		
			RankFilters rf=new RankFilters();
			for(int i=1; i<=bkgd.getStackSize(); i++){
				bkgd.setPosition(i);
				ImageProcessor iproc=bkgd.getProcessor();
				rf.rank(iproc, topHatRadius, RankFilters.MIN);
				rf.rank(iproc, topHatRadius, RankFilters.MAX);
				bkgd.setProcessor(iproc);
				bkgd.updateImage();
			}
			
			//Essential to get the result... Can't directly attribute result to ip
			result=new ImageCalculator().run("Subtract create stack", result, bkgd);
			result.setSlice(result.getNSlices()/2);
			result.resetDisplayRange();
			
		}
		
		ImageConverter.setDoScaling(true);
		new ImageConverter(result).convertToGray16();
		
		result.setTitle(title);
		System.gc();
		
		return result;
	}
}
