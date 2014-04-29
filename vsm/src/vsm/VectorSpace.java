package vsm;

import invertedindex.InvertedIndex;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	
	public VectorSpace() {
		stopwords = loadStopwords("C:/Users/Fayz/workspace/VSM/samples/stopwords.txt");
		documents = new HashMap<String, Multiset<String>>();
		invertedIndex = new InvertedIndex();
		vocabulary = new TreeSet<String>();
		//String[] reviewFolders = new String[] { "C:/Users/Fayz/workspace/VSM/samples/destructoid", "C:/Users/Fayz/workspace/VSM/samples/polygon" };
		String[] reviewFolders = new String[] { "C:/Users/Fayz/workspace/VSM/samples/test" };
		loadDocuments(reviewFolders);
//		for (String s : reviewFolders) {
//			System.out.println(s);
//		}
		calculateTfIdf();
		System.out.println(vocabulary);
		try {
			outputGameMatrix("C:/Users/Fayz/workspace/VSM/samples/output");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				double idf = Math.log(temp)/Math.log(2.0);
				tfidfs.put(term, tf*idf);
			}
			tfidfDocuments.put(docName, tfidfs);
			System.out.println(docName);
			System.out.println(tfidfs);
		}
	}
	
	public void loadDocuments(String[] reviewFolders) {
		for (int i=0; i<reviewFolders.length; ++i) {
			File folder = new File(reviewFolders[i]);
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
		return stopwords;
	}
	
	public Multiset<String> makeDocumentVector(File reviewFile) throws Exception {
		Porter stemmer = new Porter();
		
		Multiset<String> document;

		String docName = standardizeFilename(reviewFile.getName());
		System.out.println(docName);
		
		
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
		System.out.println("Number of words: " + document.size());
		System.out.println(document.toString());
		
		return document;

	}
	
	public String standardizeFilename(String filename) {
		return filename.replace(".txt", "").replace('_', ' ').trim();
	}
	
	public void outputGameMatrix(String outputFolder) throws IOException {
		File folder = new File(outputFolder);
		if (!folder.exists())
			folder.mkdir();
		FileWriter vocabFw = new FileWriter(new File(outputFolder,"vocabulary"));
		for (String term : vocabulary) {
			vocabFw.write(term + "\n");
		}
		vocabFw.close();
		
		TreeSet<String> games = new TreeSet<String>(documents.keySet());
		
		FileWriter gameFw = new FileWriter(new File(outputFolder, "games"));
		for (String game : games) {
			gameFw.write(game + "\n");
		}
		gameFw.close();
		
		FileWriter matrixFw = new FileWriter(new File(outputFolder, "matrix"));
		for (String game : games) {
			StringBuilder sb = new StringBuilder();
			HashMap<String, Double> gameDoc = tfidfDocuments.get(game);
			for (String term : vocabulary) {
				if (!gameDoc.containsKey(term)) {
					sb.append("0\t");
				} else {
					sb.append(gameDoc.get(term));
					sb.append('\t');
				}
			}
			sb.append('\n');
			matrixFw.write(sb.toString());
		}
		matrixFw.close();
	}
}
