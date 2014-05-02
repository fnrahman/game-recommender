package vsm;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VSM {
	public static void main(String[] args) {
		String inputFolder = args[0];
		String outputFolder= args[1];
		
		SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
		
		String startTime = dateformat.format(Calendar.getInstance().getTime());
		VectorSpace vs = new VectorSpace(inputFolder, outputFolder);
		vs.loadStopwords("C:/Users/Fayz/workspace/VSM/samples/stopwords.txt");
		vs.loadDocuments(inputFolder);
		vs.calculateTfIdf();
		try {
			vs.outputGameMatrix(outputFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//VectorSpace vs = new VectorSpace("C:/Users/Fayz/workspace/VSM/samples/testreviews", "C:/Users/Fayz/workspace/VSM/samples/output");
		String endTime = dateformat.format(Calendar.getInstance().getTime());
		System.out.println("Starting Time: " + startTime);
		System.out.println("Ending Time: " + endTime);
	}
}
