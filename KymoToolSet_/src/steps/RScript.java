package steps;

import ij.IJ;

import utilities.utilities;
import settings.settings;

/**
 * This class handles generation, saving and execution of the R analysis Script
 * @author fab
 *
 */
public class RScript {
	
	public static String getRScript(){
		String analysedOn="Date of analysis: "+utilities.getCurrentDateAndTime();
		
		return	"setwd(\""+ settings.destGlobalData.replace("\\", "/")+"\");\n"
	
				+ "# FLUX DATA\n"
				+ "pdf(file=\""+settings.allCellsFlux.replace("\\", "/").replace(".xls", ".pdf")+"\", paper=\"a4r\");\n"
				+ "allFluxData<-read.csv(\""+settings.allCellsFlux.replace("\\", "/")+"\", header=TRUE, sep=\"\\t\", dec=\".\", na.string=\"NaN\", stringsAsFactors=FALSE);\n\n"
				+ "par(mfrow=c(2,3), oma=c(0,0,2,0));\n"
				+ "barplot(as.numeric(allFluxData$Mean.nb.of.vesicules), names.arg=allFluxData$Cell.nb, ylab=\"Mean nb vesic.\");\n"
				+ "barplot(as.numeric(allFluxData$Min.nb.of.vesicules), names.arg=allFluxData$Cell.nb, ylab=\"Min. nb vesic.\");\n"
				+ "barplot(as.numeric(allFluxData$Max.nb.of.vesicules), names.arg=allFluxData$Cell.nb, ylab=\"Max. nb vesic.\");\n"
				+ "barplot(as.numeric(allFluxData$Mean.vesicules..density..nb.microns2.), names.arg=allFluxData$Cell.nb, ylab=\"Mean nb vesic./micron2\");\n"
				+ "barplot(as.numeric(allFluxData$Min.vesicules..density..nb.microns2.), names.arg=allFluxData$Cell.nb, ylab=\"Min. nb vesic./micron2\");\n"
				+ "barplot(as.numeric(allFluxData$Max.vesicules..density..nb.microns2.), names.arg=allFluxData$Cell.nb, ylab=\"Max. nb vesic./micron2\");\n"
				+ "par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Flux Data - "+analysedOn+"\", outer=TRUE);\n"
				+"dev.off();\n\n"
	
	
	
				+ "# ALL DATA\n"
				+ "pdf(file=\""+settings.allCellsData.replace("\\", "/").replace(".xls", ".pdf")+"\", paper=\"a4r\");\n"
				+ "allData<-read.csv(\""+settings.allCellsData.replace("\\", "/")+"\", header=TRUE, sep=\"\t\", dec=\".\", na.string=\"NaN\", stringsAsFactors=FALSE);\n\n"
				
				+"par(mfrow=c(2, 3), oma=c(0,0,2,0));\n"
				+"hist(as.numeric(allData$Ttl.Time..sec.), main=\"Total time\", xlab=\"Total time (sec)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(allData$Cum.Dist..micron.), main=\"Cumulative distance\", xlab=\"Cumulative distance (micron)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(allData$Mean.Speed..micron.per.sec.), main=\"Mean speed\", xlab=\"Mean speed (micron/sec)\", ylab=\"Nb. of values\");\n"
				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" All Data - "+analysedOn+"\", outer=TRUE);\n"
		 
				+"dev.off();\n\n"
	
	
	
				+"# SUMMARY DATA\n"
				+"pdf(file=\""+settings.allCellsSummary.replace(".xls", ".pdf").replace("\\", "/")+"\", paper=\"a4r\");\n"
				+"summaryData<-read.csv(\""+settings.allCellsSummary.replace("\\", "/")+"\", header=TRUE, sep=\"\\t\", dec=\".\", na.string=\"NaN\", stringsAsFactors=FALSE);\n\n"
				
				+"par(mfrow=c(2,3), oma=c(0,0,2,0));\n"
				+"hist(as.numeric(summaryData$Ttl.Time..sec.), main=\"Total time\", xlab=\"Total time (sec)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Cum.Dist..micron.), main=\"Cumulative distance\", xlab=\"Distance (micron)\", ylab=\"Nb. of values\");\n"
				+"plot.new();\n"
				+"hist(as.numeric(summaryData$Mean.Speed..micron.per.sec.), main=\"Mean speed\", xlab=\"Speed (micron/sec)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Mean.Speed.In..micron.per.sec.), main=\"Mean speed inward\", xlab=\"Speed(micron/sec)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Mean.Speed.Out..micron.per.sec.), main=\"Mean speed outward\", xlab=\"Speed (micron/sec)\", ylab=\"Nb. of values\");\n\n"
				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Summary Data, page 1/6 - "+analysedOn+"\", outer=TRUE, cex.main=1);\n\n"
	
				+"par(mfrow=c(2, 2), oma=c(0,0,2,0));\n"
				+"hist(as.numeric(summaryData$Cum.Dist.In..micron.), main=\"Cum. dist. inward\", xlab=\"Distance (micron)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Cum.Dist.Out..micron.), main=\"Cum. dist. outward\", xlab=\"Distance (micron)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Min.Dist.Start.End..micron.), main=\"Start-end dist.\", xlab=\"Distance(micron)\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Persistence), main=\"Persistence\", xlab=\"Persistence\", ylab=\"Nb. of values\");\n\n"
				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Summary Data, page 2/6 - "+analysedOn+"\", outer=TRUE, cex.main=1);\n\n"
				
				+"par(mfrow=c(2,3), oma=c(0,0,2,0));\n"
				+"hist(as.numeric(summaryData$Freq.In.Out..sec.1.), main=\"Inward > Outward\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Freq.In.Pause..sec.1.), main=\"Inward > Pause\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Freq.Out.In..sec.1.), main=\"Outward > Inward\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Freq.Out.Pause..sec.1.), main=\"Outward > Pause\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Freq.Pause.In..sec.1.), main=\"Pause > Inward\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$Freq.Pause.Out..sec.1.), main=\"Pause > Outward\", xlab=\"% of time\", ylab=\"Nb. of values\");\n\n"
				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Summary Data, page 3/6 - "+analysedOn+"\", outer=TRUE, cex.main=1);\n\n"
				
				+"par(mfrow=c(2,3), oma=c(0,0,2,0));\n"
				+"hist(as.numeric(summaryData$X..Time.In), main=\"% Pause while inward\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$X..Time.Out), main=\"% Pause while outward\", xlab=\"% of time\", ylab=\"Nb. of values\");\n"
				+"hist(as.numeric(summaryData$X..Time.Pause), main=\"% Time spent in pause\", xlab=\"% of time\", ylab=\"Nb. of values\");\n\n"
				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Summary Data, page 4/6 - "+analysedOn+"\", outer=TRUE, cex.main=1);\n"
	
				+"par(mfrow=c(2,2), oma=c(0,0,2,0));\n"
				+"plot(as.numeric(summaryData$Cum.Dist..micron.), summaryData$Mean.Speed..micron.per.sec., xlab=\"Cumulative distance (microns)\", ylab=\"Mean speed (micron/sec)\");\n"
				+"plot(as.numeric(summaryData$Min.Dist.Start.End..micron.), summaryData$Mean.Speed..micron.per.sec., xlab=\"Start-end dist. (microns)\", ylab=\"Mean speed (micron/sec)\");\n"
				+"plot(as.numeric(summaryData$Cum.Dist.In..micron.), summaryData$Mean.Speed.In..micron.per.sec., xlab=\"Cum. dist. inward (microns)\", ylab=\"Mean speed inward (micron/sec)\");\n"
				+"plot(as.numeric(summaryData$Cum.Dist.Out..micron.), summaryData$Mean.Speed.Out..micron.per.sec., xlab=\"Cum. dist. outward (microns)\", ylab=\"Mean speed outward(micron/sec)\");\n"
				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Summary Data, page 5/6 - "+analysedOn+"\", outer=TRUE, cex.main=1);\n"
	
				
				+ "# ALL DATA+SYNAPSES\n"
				+ "allDataSynapses<-read.csv(\""+settings.allCellsDataSynapses.replace("\\", "/")+"\", header=TRUE, sep=\"\t\", dec=\".\", na.string=\"NaN\", stringsAsFactors=FALSE);\n"
				+ "inward<-allDataSynapses[allDataSynapses$Movement_status==\"Inward\",];\n"
				+ "outward<-allDataSynapses[allDataSynapses$Movement_status==\"Outward\",];\n"
				+ "pause<-allDataSynapses[allDataSynapses$Movement_status==\"Pause\",];\n"

				+ "par(mfrow=c(2, 3), oma=c(0,0,2,0));\n"
				+ "hist(as.numeric(inward$Nb_crossed_synapses), main=\"Inward\", xlab=\"Nb. of synapses crossed\", ylab=\"Nb. of values\");\n"
				+ "hist(as.numeric(outward$Nb_crossed_synapses), main=\"Outward\", xlab=\"Nb. of synapses crossed\", ylab=\"Nb. of values\");\n"
				+ "hist(as.numeric(pause$Nb_crossed_synapses), main=\"Pause\", xlab=\"Nb. of synapses crossed\", ylab=\"Nb. of values\");\n"

				+ "plot(as.numeric(allDataSynapses$Nb_crossed_synapses), as.numeric(allDataSynapses$Ttl.Time..sec.), xlab=\"Nb. of synapses crossed\", ylab=\"Travel time for the segment (sec)\");\n"
				+ "plot(as.numeric(allDataSynapses$Nb_crossed_synapses), as.numeric(allDataSynapses$Cum.Dist..micron.), xlab=\"Nb. of synapses crossed\", ylab=\"Distance travelled for the segment (micron)\");\n"
				+ "plot(as.numeric(allDataSynapses$Nb_crossed_synapses), as.numeric(allDataSynapses$Mean.Speed..micron.per.sec.), xlab=\"Nb. of synapses crossed\", ylab=\"Mean speed of the segment (micron/sec)\");\n"

				+"par(mfrow=c(1,1));\n"
				+"title(main=\""+settings.expName+" Summary Data, page 6/6 - "+analysedOn+"\", outer=TRUE, cex.main=1);\n"
				
				+"dev.off();\n"
				+"write.table(summary(summaryData), \""+settings.expName+"_summaryData.xls\", sep=\"\\t\", dec=\".\", na=\"NaN\", eol=\"\\n\");\n";
	}
	
	public static void writeRScript(){
		String script=getRScript();
		utilities.writeStringToFile(IJ.getDirectory("imageJ")+"kymoScript.R", script);
		utilities.writeStringToFile(settings.destGlobalData+settings.expName+".R", script);
	}
}
