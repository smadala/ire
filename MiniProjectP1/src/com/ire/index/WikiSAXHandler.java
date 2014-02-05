package com.ire.index;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ire.sort.ExternalSort;

public class WikiSAXHandler extends DefaultHandler{
	public enum CurrentElement{
		TITLE,ID,TEXT
	}
	
	
	private Map<String,Boolean> requiredElements;
	private boolean parse=false;
	
	private CurrentElement currentElement;
	
	private WikiPage page;
	private static enum TextFields{
		INFOBOX("{{infobox "),
		EXTERNAL_LINKS("external links"),
		TEXT("text"),
		CATEGORY("[[category:");
		private String pattern;
		private TextFields(String pattern){
			this.pattern=pattern;
		}
		
	}
	
	private PageParser pageParser=new PageParser();

	public WikiSAXHandler(){
		requiredElements=new HashMap<String,Boolean>();
		requiredElements.put(ParsingConstants.ELE_TITLE,true);
		requiredElements.put(ParsingConstants.ELE_ID, false);
		requiredElements.put(ParsingConstants.ELE_TEXT, true);
	}

	@Override
	public void startDocument() throws SAXException {
		
		//Titles File
		ParsingConstants.titleFile=new File(ParsingConstants.indexFileDir, 
				ParsingConstants.TITLES_FILE_PREFIX+ParsingConstants.INDEX_SUFFIX);
		ParsingConstants.titleFile=new File(ParsingConstants.indexFileDir, 
				ParsingConstants.TITLES_FILE_PREFIX+ParsingConstants.INDEX_SUFFIX);
		try {
			ParsingConstants.titleIndexWriter=new BufferedWriter(new FileWriter(ParsingConstants.titleFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Start of Doc...");
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("End of Doc...");
		
		try {
			pageParser.dumpAllWords();
			pageParser.mergeSubIndexFiles();
			ParsingConstants.indexFiles.add(ParsingConstants.titleFile.getAbsolutePath());
			for(int i=0;i<ParsingConstants.NUM_OF_INDEXFILES+1;i++){
				ExternalSort.createOffsetsFile(ParsingConstants.indexFiles.get(i), ParsingConstants.indexFileDir,i);
			}
			/*ExternalSort.createOffsetsFile(ParsingConstants.titleFile.getAbsolutePath(),
					ParsingConstants.indexFileDir,ParsingConstants.TITLES_FILE_PREFIX);*/
			ParsingConstants.titleIndexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		qName=qName.toLowerCase();
		if(requiredElements.get(qName) != null && requiredElements.get(qName) ){
			
			if(qName.equals(ParsingConstants.ELE_TITLE)){
				requiredElements.put(ParsingConstants.ELE_ID, true);
				currentElement = CurrentElement.TITLE;
				page=new WikiPage();
				countOfIBCurl=0;
				prevField=TextFields.TEXT;
				curField=TextFields.TEXT;
				infoboxDone=false;
				
			}else if(qName.equals(ParsingConstants.ELE_ID)){
				currentElement = CurrentElement.ID;
			}else{
				currentElement = CurrentElement.TEXT;
			}
			parse=true;		
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		qName=qName.toLowerCase();
//		System.out.println("end "+qName);
		if(parse){
			if(qName.equals(ParsingConstants.ELE_ID)){
				requiredElements.put(ParsingConstants.ELE_ID, false);
			}else if(qName.equals(ParsingConstants.ELE_TEXT)){
				try {
					//System.out.println(page.getInfoBox());
					//System.out.println(page.getCategory());
					//System.out.println(page.getExternalLinks());
					pageParser.parse(page);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*for(String token:wordCount.keySet()){
					System.out.println(token +":d"+id+"-"+wordCount.get(token));
				}*/
			}
			parse=false;
	
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		if(!parse)
			return;
		
		if( currentElement == CurrentElement.TEXT){
			//page.getText().append(ch,start,length);
			divideFields(ch, start, length);
		}
		else if(currentElement == CurrentElement.TITLE){
			page.getTitle().append(ch, start, length);
		}else if(currentElement == CurrentElement.ID){
			String id=new String(ch,start,length);
			page.setId(id);
		}
	}
	
	private int countOfIBCurl=0;
	private TextFields prevField;
	private TextFields curField;
	private boolean infoboxDone;
	

	private void divideFields(char[] ch, int start, int length){
		int i=start;
		/*INFOBOX("{{Infobox "),
		EXTERNAL_LINKS("==External links=="),
		CATEGORY("[[Category:");*/
		boolean match=false;
		int matchIndex=0;
		
		if(curField == TextFields.INFOBOX || curField == TextFields.CATEGORY){
			if(curField == TextFields.INFOBOX){ //match curl braces for infobox
				for(;i<start+length;i++){
					if( ch[i] == '{')
						countOfIBCurl++;
					else if(ch[i] == '}')
						countOfIBCurl--;
					if(countOfIBCurl == 0){
						addStringToPrevField(ch, start, i-start+1, curField);
						curField=TextFields.TEXT;
						infoboxDone=true;
						divideFields(ch, i , length-(i - start));
						return;
					}
				}
				
			}else if(curField == TextFields.CATEGORY){
				for(;i<start+length;i++){
					if( ch[i] == '[')
						countOfIBCurl++;
					else if(ch[i] == ']')
						countOfIBCurl--;
					if(countOfIBCurl == 0){
						addStringToPrevField(ch, start, i-start+1, curField);
						curField=TextFields.TEXT;
						divideFields(ch, i , length-(i - start));
						return;
					}
				}
			}
			addStringToPrevField(ch, start, length, curField);
			return;
		}
		for(; i<start+length ; i++ ){
			matchIndex=i;
			if(  !infoboxDone && ch[i] == '{'){
				match=isMatch(ch, start, length,i, TextFields.INFOBOX.pattern);
				if(match){
					prevField=curField;
					curField=TextFields.INFOBOX;
					countOfIBCurl=0;
				}
			}else if(curField != TextFields.EXTERNAL_LINKS &&ch[i] == '='){
				match=isExternalLink(ch, start, length, i, TextFields.EXTERNAL_LINKS.pattern);
				if(match){
					prevField=curField;
					curField=TextFields.EXTERNAL_LINKS;
				}
				
			}else if( ch[i] == '['){
				match=isMatch(ch, start, length,i, TextFields.CATEGORY.pattern);
				if(match){
					prevField=curField;
					curField=TextFields.CATEGORY;
					countOfIBCurl=0;
				}
			}
			if(match){
				break;
			}
		}
		if(match){
			addStringToPrevField(ch, start, matchIndex-start, prevField);
			divideFields(ch, matchIndex, length-(matchIndex - start));
		}else{
			addStringToPrevField(ch, start, length, curField);
		}
	}
	
	private void addStringToPrevField(char[] ch,int start,int length,TextFields field ){
		switch(field){
			case TEXT:	page.getText().append(ch, start, length);
						break;
			case INFOBOX: page.getInfoBox().append(ch,start,length);
						  break;
			case CATEGORY: page.getCategory().append(ch,start,length);
							break;
			case EXTERNAL_LINKS: page.getExternalLinks().append(ch,start,length);
							break;
		}
		
	}
	
	private boolean isMatch(char ch[], int start, int length ,int firstCharPos, String  pattern){
		
			int j=0;
			while( firstCharPos+j < start+length  &&
					j < pattern.length() && 
					Character.toLowerCase(ch[firstCharPos+j]) == pattern.charAt(j) ){
				j++;
			}
			if( j == pattern.length())
				return true;
			return false;
	}
	private boolean isExternalLink(char ch[], int start, int length ,int firstCharPos, String  pattern){
		
		int auxFirst=firstCharPos;
		//only two equals should be there
		while( auxFirst < start+length && ch[auxFirst] == '=' ){
			auxFirst++;
		}
		if(auxFirst == start+length || auxFirst - firstCharPos != 2)
			return false;
		while( auxFirst < start+length && ch[auxFirst] == ' ') auxFirst++;
		
		return isMatch(ch, start, length, auxFirst, pattern);
	}
}
