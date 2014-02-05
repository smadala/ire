package com.ire.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class WikiParser {

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		System.out.println(new Date().toString());
		ParsingConstants.startTime=System.currentTimeMillis();
		ParsingConstants.lastDump=System.currentTimeMillis();
		try {
			buildIndex(args[0],args[1]);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void buildIndex(String corpusFile,String indexDirectory) throws IOException, ParserConfigurationException, SAXException{
		  loadStopWords();
		  SAXParserFactory saxParserFactory=SAXParserFactory.newInstance();
		  SAXParser saxParser = saxParserFactory.newSAXParser();
		  ParsingConstants.indexFileDir=indexDirectory;
		  saxParser.parse(corpusFile, new WikiSAXHandler());
		  System.out.println(new Date().toString() );
	}
	public static void loadStopWords() throws IOException{
		
		BufferedReader br=new BufferedReader(new FileReader(new File(ParsingConstants.STOP_WORD_FILE)));
		
		String line;
		PageParser.stopWords=new HashSet<String>();
		
		while( (line = br.readLine()) != null){
			  String tokens[]=line.toLowerCase().split(ParsingConstants.STOP_WORD_DELIMITER);
			  for(String token:tokens){
				  PageParser.stopWords.add(token);
			  }
		}
		if(br != null)
			br.close();
	}
}
