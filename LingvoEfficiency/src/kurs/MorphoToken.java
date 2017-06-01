package kurs;

import java.util.Set;

public class MorphoToken {
	private String lexema;
	private Set<String> gramems;
	private String part;

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		if(this.part != null && this.part != "V"){
			return;
		}
		this.part = part;
	}

	public String getLexema() {
		return lexema;
	}
	
	public void setLexema(String lexema) {
		this.lexema = lexema;
	}
	
	public Set<String> getGramems() {
		return gramems;
	}
	
	public void setGramems(Set<String> gramems) {
		this.gramems = gramems;
	}
	
	
}
