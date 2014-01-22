package com.ire.index;

public class WikiPage {
	
	private StringBuffer title;
	private String id;
	private StringBuffer text;
	
	public WikiPage(){
		title=new StringBuffer(32);
		text=new StringBuffer(4096);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public StringBuffer getTitle() {
		return title;
	}
	public void setTitle(StringBuffer title) {
		this.title = title;
	}
	public StringBuffer getText() {
		return text;
	}
	public void setText(StringBuffer text) {
		this.text = text;
	}
	
	
}
