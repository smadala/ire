package com.ire.index;
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
	
	
	private PageParser pageParser=new PageParser();

	public WikiSAXHandler(){
		requiredElements=new HashMap<String,Boolean>();
		requiredElements.put(ParsingConstants.ELE_TITLE,true);
		requiredElements.put(ParsingConstants.ELE_ID, false);
		requiredElements.put(ParsingConstants.ELE_TEXT, true);
	}

	@Override
	public void startDocument() throws SAXException {
		//System.out.println("Start of Doc...");
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("End of Doc...");
		
		try {
			pageParser.dumpAllWords();
			pageParser.mergeSubIndexFiles();
			ExternalSort.createOffsetsFile(ParsingConstants.absoluteIndexFilePath, ParsingConstants.indexFileDir);
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
		if(parse){
			if(qName.equals(ParsingConstants.ELE_ID)){
				requiredElements.put(ParsingConstants.ELE_ID, false);
			}else if(qName.equals(ParsingConstants.ELE_TEXT)){
				try {
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
			page.getText().append(ch,start,length);
		}
		else if(currentElement == CurrentElement.TITLE){
			page.getTitle().append(ch, start, length);
		}else if(currentElement == CurrentElement.ID){
			String id=new String(ch,start,length);
			page.setId(id);
		}
	}
}
