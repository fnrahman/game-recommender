package vsm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Scanner;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class VectorSpace {
	public HashSet<String> stopwords;
	public HashSet<String> documents;
	
	public VectorSpace() {
		stopwords = loadStopwords("C:/Users/Fayz/workspace/VSM/samples/stopwords.txt");
		documents = new HashSet<String>();
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
		System.out.println(stopwords.size());
		return stopwords;
	}
	
	public Multiset<String> makeDocumentVector(String path) {
		Porter stemmer = new Porter();
		Multiset<String> document = HashMultiset.create();
		try {
			File f = new File(path);
			String name = standardizeFilename(f.getName());
			System.out.println(name);
			Scanner scanner = new Scanner(f);
			
			while (scanner.hasNext()) {
				String word = scanner.next().toLowerCase();
				if (!stopwords.contains(word)) {
					String stem = stemmer.stripAffixes(word);
					document.add(stem);
				}
			}
			System.out.println("Number of words: " + document.size());
			System.out.println(document.toString());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
		
		
	}
	
	public String standardizeFilename(String filename) {
		return filename.replace(".txt", "").replace('_', ' ').trim();
	}
}
