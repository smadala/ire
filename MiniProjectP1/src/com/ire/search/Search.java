package com.ire.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ire.index.PageParser;
import com.ire.index.ParsingConstants;
import com.ire.index.Stemmer;
import com.ire.index.WikiParser;
import com.ire.sort.ExternalSort;

public class Search {

	
	//private static Map<String,Long> offsets;
	private static Stemmer stemmer=new Stemmer();
	private static List<RandomAccessFile> primayIndexeFiles;
	private static List<RandomAccessFile> indexFiles;
	private static List<TreeMap<String,Long>> secIndexes;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void loadSecondryIndexes(String indexFolder) throws Exception{
		String tokens[];
		TreeMap<String,Long> secIndex;
		secIndexes=new ArrayList<TreeMap<String,Long>>();
		primayIndexeFiles=new ArrayList<RandomAccessFile>();
		indexFiles=new ArrayList<RandomAccessFile>();
		File auxFile=null;
		for(int i=0;i<ParsingConstants.NUM_OF_INDEXFILES+1;i++){
			
			BufferedReader reader=new BufferedReader(new FileReader(new File(indexFolder,i+ParsingConstants.SECONDRY_SUFFIX)));
			secIndex=new TreeMap<String,Long>();
			for(String line; (line = reader.readLine())!= null; ){
				tokens=line.split(ParsingConstants.wordDelimiter);
				if(tokens.length != 2)
					continue;
				secIndex.put(tokens[0], Long.parseLong(tokens[1]));
			}
			secIndexes.add(secIndex);
			
			auxFile=new File(indexFolder,i+ParsingConstants.OFFSET_SUFFIX);
			primayIndexeFiles.add( new RandomAccessFile(auxFile.getAbsoluteFile(), "r"));
			
			auxFile=new File(indexFolder,i+ParsingConstants.INDEX_SUFFIX);
			indexFiles.add( new RandomAccessFile(auxFile.getAbsoluteFile(), "r"));
		}
	}
	
	public static void main(String[] args) throws Exception  {
		// TODO Auto-generated method stub
		
		
		long start = System.currentTimeMillis();
		final String indexFolder=args[0];
		loadSecondryIndexes(indexFolder);
		WikiParser.loadStopWords();
		final String queryFile=args[1];
		String line=null;
		
		
		String tokens[]=null;
		
		BufferedReader reader=new BufferedReader(new FileReader(new File(queryFile)));
		
		//Scanner reader=new Scanner(System.in);
		int numOfQueries=Integer.parseInt(reader.readLine());
		String[] queryWords= new String[numOfQueries];
		
		for(int i=0;  i < numOfQueries;i++){
			
			line = reader.readLine();
			
			if(line != null){
				tokens =line.toLowerCase().split(ParsingConstants.DOC_PARSIGN_REGEX);
				if( tokens.length > 0 ){
					if(!PageParser.stopWords.contains(tokens[0])){
						queryWords[i]=stemmer.stemWord(tokens[0]);
			//			queryWords[i]=tokens[0];
						//System.out.println(line);
						continue;
					}
				}
			}
			queryWords[i]=null;
			//System.out.println(line);
		}
		
		 printResults(queryWords);
		
		//System.out.print( System.currentTimeMillis() - start  +" Exit");
	}
	public static void printResults(String[] queryWords){
		try {
			
			String postingList=null;
			for( String word: queryWords){
				
				if( word != null && (postingList = getPostingList(word)) != null){
					displayWordLine(postingList);
					continue;
				}
				//print empty line
				
				System.out.println();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getPostingList(String word) throws IOException{
		
		int filePrefix=word.charAt(0) - 'a';
		if(filePrefix < 0){
			filePrefix=ParsingConstants.TITLES_FILE_PREFIX;
		}
		TreeMap<String,Long> secIndex=secIndexes.get(filePrefix);
		Entry<String,Long> entry = secIndex.floorEntry(word);
		if(entry == null)
			return null;
		long startOffset=entry.getValue(),offset=-1;
		RandomAccessFile primaryIndex=primayIndexeFiles.get(filePrefix),indexFile;
		
		primaryIndex.seek(startOffset);
		String line,tokens[];
		int diff;
		
		while((line = primaryIndex.readLine()) != null){
			tokens=line.split(ParsingConstants.wordDelimiter);
			if(tokens.length != 2)
				continue;
			diff=word.compareTo(tokens[0]);
			if(diff > 0)
				continue;
			else if(diff == 0)
				offset = Long.parseLong(tokens[1]);
			else
				offset = -1;
			break;
		}
		if(offset == -1){
			return null;
		}else{
			indexFile=indexFiles.get(filePrefix);
			indexFile.seek(offset);
			return indexFile.readLine();
		}
	}
	
	//word#idf=docid-freq:weight;
	
	public static void getRank(List<String> postingLists){
		List<QueryWord> queryWords=new ArrayList<QueryWord>();
		QueryWord queryWord;
		int idfStartPos=0,idfEndPos=0;
		double idf=0;
		for(String postingList:postingLists){
			queryWord=new QueryWord();
			
			idfStartPos = postingList.indexOf(ParsingConstants.WORD_IDF_DELIMITER);
			queryWord.setWord(postingList.substring(0, idfStartPos));
			
			idfEndPos =postingList.indexOf(ParsingConstants.CHAR_WORD_DELIMITER, idfStartPos);
			idf=Double.parseDouble(postingList.substring(idfStartPos+1, idfEndPos));// +1 for ignore #
			queryWord.setIdf(idf);
			
			queryWord.setDocIds(postingList.substring(idfEndPos+1)); // +1 for ignore = 
		}
		Collections.sort(queryWords, QueryWord.SORT_BY_IDF);
		for(QueryWord qWord:queryWords){
			
		}
	}
	public static void displayWordLine(String line){
		
		String prev="1",cur;
		String tokens[] = line.split(ParsingConstants.WORD_DELIMITER); // divide word and [doc,count] 
		
		
		String docCounts[] = tokens[1].split(ParsingConstants.DOC_DELIMITER); //divide docs to doc-count 
		
		//TODO: sort result
		StringBuilder result=new StringBuilder(docCounts.length * 9);
		
		Set<Integer> docIds=new TreeSet<Integer>();
		
		
		for(String docCount:docCounts ){
			//System.out.println(docCount.split(ParsingConstants.DOC_COUNT_DELIMITER)[0]); // print document id
			cur=docCount.split(ParsingConstants.DOC_COUNT_DELIMITER)[0];
			/*if(docCounts.length > 20)
			System.out.println(cur);
			if(cur.compareTo(prev) <= 0){
				System.out.println("Error at"+ cur + "       "+ prev);
			}*/
			docIds.add(Integer.parseInt(cur));
		}
		/*if(docCounts.length > 20)
		System.out.println("\n\n\n");*/
		for(Integer docId:docIds){
		     result.append(docId +",");	
		}
		result.delete(result.length() - 1 , result.length() );
		System.out.println(result);
	}
}
