package com.ire.search;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import com.ire.index.PageParser;
import com.ire.index.ParsingConstants;
import com.ire.index.Stemmer;
import com.ire.index.WikiParser;
import com.ire.sort.ExternalSort;

public class Search {

	
	private static Map<String,Long> offsets;
	private static Stemmer stemmer=new Stemmer();
	private static RandomAccessFile index;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	
	public static void main(String[] args) throws IOException  {
		// TODO Auto-generated method stub
		
		//System.out.print("Start");
		/*Scanner in=new Scanner(System.in);
		int numOfQueries=Integer.parseInt(in.nextLine());
		String[] queryWords= new String[numOfQueries]; 
		for(int i=0; i<numOfQueries ;i++){
			queryWords[i]=stemmer.stemWord( in.nextLine().toLowerCase() );
		}*/
	//	long start = System.currentTimeMillis();
		final String indexFolder=args[0];
		//final String queryFile=args[1];
		String line=null;
		index=new RandomAccessFile(new File(indexFolder,ParsingConstants.INDEX_FILE_NAME), "r");
		
		
		String tokens[]=null;
		offsets = ExternalSort.getOffsets(indexFolder);
		
		//BufferedReader reader=new BufferedReader(new FileReader(new File(queryFile)));
		
		Scanner reader=new Scanner(System.in);
		int numOfQueries=Integer.parseInt(reader.nextLine());
		String[] queryWords= new String[numOfQueries];
		
		/*for(int i=0; i<numOfQueries ;i++){
			queryWords[i]=stemmer.stemWord( in.nextLine().toLowerCase() );
		}*/
		
		/*if( (line=reader.readLine()) != null )
			numberOfQueries=Integer.parseInt(line);
		
		String[] queryWords=new String[(numberOfQueries)];*/
		
		WikiParser.loadStopWords();
		
		//System.out.println(" index folder -"+ indexFolder +" query file- "+queryFile);
		for(int i=0;  i < numOfQueries;i++){
			
			line = reader.nextLine();
			
			if(line != null){
				tokens =line.toLowerCase().split(ParsingConstants.DOC_PARSIGN_REGEX);
				if( tokens.length > 0 ){
					if(!PageParser.stopWords.contains(tokens[0])){
						queryWords[i]=stemmer.stemWord(tokens[0]);
						//queryWords[i]=tokens[0];
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
		//	offsets = ExternalSort.getOffsets(ParsingConstants.OFFSETS_FILE);
			
			
			
			Long lineOffset=null;
			for( String word: queryWords){
				
				if( word != null && (lineOffset = offsets.get(word)) != null){
					index.seek(lineOffset);
					displayWordLine(index.readLine());
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
	
	public static void displayWordLine(String line){
		
		
		String tokens[] = line.split(ParsingConstants.WORD_DELIMITER); // divide word and [doc,count] 
		
		
		String docCounts[] = tokens[1].split(ParsingConstants.DOC_DELIMITER); //divide docs to doc-count 
		
		//TODO: sort result
		StringBuilder result=new StringBuilder(docCounts.length * 9);
		
		Set<Integer> docIds=new TreeSet<Integer>();
		
		
		for(String docCount:docCounts ){
			//System.out.println(docCount.split(ParsingConstants.DOC_COUNT_DELIMITER)[0]); // print document id
			docIds.add(Integer.parseInt(docCount.split(ParsingConstants.DOC_COUNT_DELIMITER)[0]));
		}
		for(Integer docId:docIds){
		     result.append(docId +",");	
		}
		result.delete(result.length() - 1 , result.length() );
		System.out.println(result);
		
	}
}
