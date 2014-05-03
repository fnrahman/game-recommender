package vsm;

import invertedindex.InvertedIndex;


import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class VectorSpace {
	public HashSet<String> stopwords;
	public HashMap<String, Multiset<String>> documents;
	public InvertedIndex invertedIndex;
	public HashMap<String, HashMap<String, Double>> tfidfDocuments;
	public TreeSet<String> vocabulary;
	public String inputFolderPath;
	public String outputFolderPath;
	
	public VectorSpace(String inputPath, String outputPath) {
		documents = new HashMap<String, Multiset<String>>();
		invertedIndex = new InvertedIndex();
		vocabulary = new TreeSet<String>();
		inputFolderPath = inputPath;
		outputFolderPath = outputPath;
	}
	
	public void calculateTfIdf() {
		tfidfDocuments = new HashMap<String, HashMap<String, Double>>();
		for (String docName : documents.keySet()) {
			HashMap<String, Double> tfidfs = new HashMap<String, Double>();
			Multiset<String> doc = documents.get(docName);
			for (String term : doc.elementSet()) {
				double tf = (double)doc.count(term); // (double)doc.size();
				double termcount = (double)invertedIndex.getTermCount(term);
				double temp = (double)documents.size() / termcount;
				double idf = Math.log(temp);
				double tfidf = tf*idf;
				if (tfidf > 0.0) {
					tfidfs.put(term, tfidf);
				}
			}
			tfidfDocuments.put(docName, tfidfs);
			System.out.println(docName);
			//System.out.println(tfidfs);
		}
	}
	
	public void loadDocuments(String inputPath) {
		File inputFolder = new File(inputPath);
		File[] reviewFolders = inputFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}	
		});
		
		for (int i=0; i<reviewFolders.length; ++i) {
			File folder = reviewFolders[i];
			System.out.println("Working on Folder: " + folder.getName());
			File[] reviews = folder.listFiles();
			for (File reviewFile : reviews) {
				try {
					makeDocumentVector(reviewFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public HashSet<String> loadStopwords(String path) {
		HashSet<String> stopwords = new HashSet<String>();
		try {
			Scanner scanner = new Scanner(new FileReader(path));
			while (scanner.hasNext()) {
				String next = scanner.next();
				//System.out.println(next);
				stopwords.add(next);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.stopwords = stopwords;
		return stopwords;
	}
	
	public Multiset<String> makeDocumentVector(File reviewFile) throws Exception {
		Porter stemmer = new Porter();
		Multiset<String> document;
		String docName = standardizeFilename(reviewFile.getName());
		
		
		if (documents.containsKey(docName)) {
			document = documents.get(docName);
		} else {
			document = HashMultiset.create();
			documents.put(docName, document);
		}
		Scanner scanner = new Scanner(reviewFile);
		
		while (scanner.hasNext()) {
			String word = scanner.next().toLowerCase();
			if (!stopwords.contains(word)) {
				String stem = stemmer.stripAffixes(word);
				vocabulary.add(stem);
				
				document.add(stem);
				invertedIndex.add(stem, docName);
			}
		}
		//System.out.println("Number of words: " + document.size());
		//System.out.println(document.toString());
		
		return document;

	}
	
	public String standardizeFilename(String filename) {
		return filename.replace(".txt", "").replace('_', ' ').trim();
	}
	
	public void outputGameMatrix(String outputFolder) throws IOException {
		// Check if output folder exists. If not, create it
		File folder = new File(outputFolder);
		if (!folder.exists())
			folder.mkdir();
		
		// Create the file that lists all features/vocabulary
		FileWriter vocabFw = new FileWriter(new File(outputFolder,"vocabulary.txt"));
		for (String term : vocabulary) {
			vocabFw.write(term + "\n");
		}
		vocabFw.close();
		
		// Create the file that lists all games.
		TreeSet<String> games = new TreeSet<String>(documents.keySet());
		FileWriter gameFw = new FileWriter(new File(outputFolder, "games.txt"));
		for (String game : games) {
			gameFw.write(game + "\n");
		}
		gameFw.close();
		
		// Create the vector space matrix, with games as rows and features as columns
		FileWriter matrixFw = new FileWriter(new File(outputFolder, "matrix.txt"));
		int gameNum = 1;
		for (String game : games) {
			HashMap<String, Double> gameDoc = tfidfDocuments.get(game);
			int termNum = 1;
			for (String term : vocabulary) {
				if (gameDoc.containsKey(term)) {
					StringBuilder sb = new StringBuilder();
					sb.append(gameNum);
					sb.append(' ');
					sb.append(termNum);
					sb.append(' ');
					sb.append(gameDoc.get(term));
					sb.append('\n');
					matrixFw.write(sb.toString());
				}
				termNum++;
			}
			gameNum++;
			
		}
		matrixFw.write(String.format("%s %s 0.0", games.size(), vocabulary.size()));
		matrixFw.close();
	}
	
	public void calculateUserVector(String outputName, ArrayList<String> games, ArrayList<Integer> scores) {
		HashMap<String, Double> userDoc = new HashMap<String, Double>();
		for (int i=0; i<games.size(); ++i) {
			String game = games.get(i);
			double score = scores.get(i)/100.0;
			if (tfidfDocuments.containsKey(game)) {
				HashMap<String, Double> doc = tfidfDocuments.get(game);
				System.out.println("DOC: " + doc.toString());
				for (String term : doc.keySet()) {
					Double docScore = doc.get(term);
					if (userDoc.containsKey(term))
						userDoc.put(term, userDoc.get(term) + (docScore*score));
					else
						userDoc.put(term, docScore);
				}
			}
 		}
		System.out.println("USERDOC: " + userDoc.toString());
		
		FileWriter userFw;
		try {
			userFw = new FileWriter(new File(outputFolderPath, outputName));
			int termNum = 1;
			for (String term : vocabulary) {
				if (userDoc.containsKey(term)) {
					StringBuilder sb = new StringBuilder();
					sb.append("1 ");
					sb.append(termNum);
					sb.append(' ');
					sb.append(userDoc.get(term));
					sb.append('\n');
					userFw.write(sb.toString());
				}
				termNum++;
			}
			userFw.write(String.format("1 %s 0.0", vocabulary.size()));
			userFw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
