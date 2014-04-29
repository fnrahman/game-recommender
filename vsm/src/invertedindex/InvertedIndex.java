package invertedindex;

import java.util.HashMap;
import java.util.HashSet;

public class InvertedIndex {
	HashMap<String, HashSet<String>> index;
	
	public InvertedIndex() {
		index = new HashMap<String, HashSet<String>>();
	}
	
	/**
	 * Adds a document to a term in the inverted index.
	 * If the term is not already in the inverted index,
	 * it will be added.
	 * @param term
	 * @param document
	 */
	public void add(String term, String document) {
		if (index.containsKey(term)) {
			index.get(term).add(document);
		} else {
			HashSet<String> documentSet = new HashSet<String>();
			documentSet.add(document);
			index.put(term, documentSet);
		}
	}
	
	/**
	 * Gets a set of documents that contains a given term
	 * @param term
	 * @return
	 */
	public HashSet<String> getDocument(String term) {
		return index.get(term);
	}
	
	public int getTermCount(String term) {
		HashSet<String> doc = index.get(term);
		if (doc != null) {
			return doc.size();
		} else
			return 0;
	}
}
