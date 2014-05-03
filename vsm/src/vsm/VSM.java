package vsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class VSM {
	public static VectorSpace vs;
	public static void main(String[] args) {
		String inputFolder = args[0];
		String outputFolder= args[1];
		
		SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
		
		String startTime = dateformat.format(Calendar.getInstance().getTime());
		vs = new VectorSpace(inputFolder, outputFolder);
		vs.loadStopwords("C:/Users/Fayz/workspace/VSM/samples/stopwords.txt");
		vs.loadDocuments(inputFolder);
		vs.calculateTfIdf();
		try {
			vs.outputGameMatrix(outputFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String endTime = dateformat.format(Calendar.getInstance().getTime());
		System.out.println("Starting Time: " + startTime);
		System.out.println("Ending Time: " + endTime);
		
		Scanner inputScanner = new Scanner(System.in);
		String in = "";
		while (true) {
			System.out.println("Enter '!' to quit. Input a path to a user review to turn it into a vector.");
			in = inputScanner.nextLine();
			if (in.equals("!"))
				break;
			File userReview = new File(in);
			if(userReview.exists()) {
				readUserReviewFile(userReview);
			} else {
				System.out.println("File does not exist.");
			}
		}
	}
	
	public static void readUserReviewFile(File userReview) {
		Scanner scanner;
		String reviewname = userReview.getName();
		ArrayList<String> games = new ArrayList<String>();
		ArrayList<Integer> scores = new ArrayList<Integer>();
		try {
			scanner = new Scanner(userReview);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] split = line.split("\t");
				String name = normalizeName(split[0]);
				games.add(name);
				scores.add(Integer.parseInt(split[1]));
				
			}
			scanner.close();
			System.out.println(games);
			System.out.println(scores);
			vs.calculateUserVector(reviewname, games, scores);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String normalizeName(String name) {
		String result = name.replaceAll("[^a-zA-Z ]", " ").replaceAll("\\s+", " ").toLowerCase().trim();
		System.out.println(result);
		return result;
	}
}
