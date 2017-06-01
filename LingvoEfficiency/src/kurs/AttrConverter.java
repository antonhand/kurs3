package kurs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class AttrConverter {
	
	private Map<String, Set<String>> sourceToMystem;
	private Map<String, Set<String>> lingvoToMystem;
	private Set<String> mystemSet;
	private Set<String> parts;
	
	public Set<String> getParts() {
		return parts;
	}

	public AttrConverter()
	{
		parts = new HashSet<>();
		parts.addAll(Arrays.asList("S","A","V","прич","деепр","NUM","ADV","Other"));
		
		sourceToMystem = new HashMap<>();
		lingvoToMystem = new HashMap<>();
		mystemSet = new HashSet<>();
		BufferedReader in;
		try {
	        in = new BufferedReader(new InputStreamReader(new FileInputStream("сопоставление.csv"), "utf-8"));
			String source, lingvo, s;
			
			while((s = in.readLine()) != null){
				Set<String> mystem = new HashSet<String>();
				StringTokenizer st = new StringTokenizer(s, ",");
				source = st.nextToken();
				lingvo = st.nextToken();
				while(st.hasMoreTokens()){
					String tmp = st.nextToken();
					mystem.add(tmp);
				}
				mystemSet.addAll(mystem);
				sourceToMystem.put(source, mystem);
				lingvoToMystem.put(lingvo, mystem);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isMystemAttr(String s)
	{
		return mystemSet.contains(s);
	}
	
	public Set<String> sourceToMystem(String s)
	{
		return sourceToMystem.get(s);
	}
	
	public Set<String> lingvoToMystem(String s)
	{
		return lingvoToMystem.get(s);
	}
	
	public boolean isPartOfSpeech(String s)
	{
		return parts.contains(s);
	}
	
}
