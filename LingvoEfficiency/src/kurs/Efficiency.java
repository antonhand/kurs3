package kurs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.github.stagirs.lingvo.morpho.MorphoDictionary;
import com.github.stagirs.lingvo.morpho.model.Attr;
import com.github.stagirs.lingvo.morpho.model.Form;

public class Efficiency {
	
	private final static boolean DEBUG_LINGVO = true;
	private final static boolean DEBUG_MYSTEM = true;
	
	private static int countLex = 0;
	private static Map<String, Integer> countAll;
	private static Map<String, Integer> countCorrLingvoLex;
	private static Map<String, Integer> countCorrMystemLex;
	private static Map<String, Double> countCorrLingvoGr;
	private static Map<String, Double> countCorrMystemGr;
	private static AttrConverter attr = new AttrConverter();
	private static Process process;
	
	private static void LingvoCorrectPercent(String word, MorphoToken correct)
	{
		List<String> notAcc = new ArrayList<String>();
		MorphoDictionary md = MorphoDictionary.get(word);
		String[] norms = md.getNorms();
    	Form[] forms = md.getRawForms();
    	double cor = 0;
    	int corL = 0;
    	for(int i = 0; i < norms.length; i++){
    		if(!correct.getGramems().contains("V") && !norms[i].toLowerCase().equals(correct.getLexema())){
    			continue;
    		}
    		if(norms[i].toLowerCase().equals(correct.getLexema())){
    			corL = 1;
    		}
    		List<Attr> attrs = forms[i].getAttrs();
    		Set<String> gr = new HashSet<String>();
    		
    		for(Attr a : attrs){
    			if(attr.lingvoToMystem(a.getDescription()) != null){
        			gr.addAll(attr.lingvoToMystem(a.getDescription()));
        		}
    		}
    		
			if(correct.getGramems().equals(gr)){
    			cor = 1;
    			break;
			} else if(correct.getGramems().containsAll(gr)){
				double tmp = (correct.getGramems().size() - gr.size()) * 1. / correct.getGramems().size();
				if(tmp > cor){
					cor = tmp;
				}
				if(DEBUG_LINGVO){
					notAcc.add("частично верно: " + correct.getLexema() + " " + correct.getGramems().toString() + " " + norms[i] + " " + gr.toString());
				}
			} else if(DEBUG_LINGVO){
				notAcc.add("неверно: " + correct.getLexema() + " " + correct.getGramems().toString() + " " + norms[i] + " " + gr.toString());
			}
    	}
		if(DEBUG_LINGVO) {
			if (cor < 1) {
				System.out.println("LINGVO:");
				System.out.println(word + " " + correct.getGramems().toString());
				for(String na : notAcc){
					System.out.println(na);
				}
				System.out.println();
			}
		}
    	countCorrLingvoGr.put(correct.getPart(), countCorrLingvoGr.get(correct.getPart()).doubleValue() + cor);
    	countCorrLingvoLex.put(correct.getPart(), countCorrLingvoLex.get(correct.getPart()).intValue() + corL);
	}
	
	private static void MystemCorrectPercent(String word, MorphoToken correct)
	{
		List<String> notAcc = new ArrayList<String>();
		double cor = 0;
        int corL = 0;
		try {
	        if(process == null){
	        	process = new ProcessBuilder("D:\\Курсовая\\mystem\\mystem.exe","-ni", "--format=xml"/*, "-e", "cp1251"*/).start();
	        }
	        
	        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
	       
	        PrintWriter pw = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "utf-8"));
	        pw.println(word);
	        pw.println(word);
	        pw.flush();
	        Thread.sleep(0, 500);
	        String s = br.readLine();
	        while(!s.contains("<w>")){
	        	 s = br.readLine();
	        }
            byte[] bytes = s.getBytes(Charset.forName("utf-8"));

            InputStream stringStream = new ByteArrayInputStream(bytes);
	        		
	        XMLStreamReader xmlr = XMLInputFactory.newInstance().createXMLStreamReader(stringStream);
	  
	        while(xmlr.hasNext()) {
	            if(xmlr.isStartElement() && xmlr.getName().toString() == "ana"){
	            	if(xmlr.getAttributeValue(0).toLowerCase().equals(correct.getLexema())){
	            		corL = 1;
	            	} else if(!correct.getGramems().contains("V")){
	            		xmlr.next();
	        			continue;
	        		}
	            	
	        		Set<String> gr = new HashSet<String>();
	        		StringTokenizer st = new StringTokenizer(xmlr.getAttributeValue(xmlr.getAttributeCount() - 1), ",=");
	        		while(st.hasMoreTokens()){
	        			String gram = st.nextToken();
	        			if(attr.isMystemAttr(gram)){
	        				gr.add(gram);
	        				
	        			}
	        		}
	        		
	        		if(gr.contains("APRO") || gr.contains("ANUM")){
    					gr.add("A");
    					if(!gr.contains("сокр")){
    						gr.add("полн");
    					}
	        		}
	        		
        			if(correct.getGramems().equals(gr)){
        				cor = 1;
	        			break;
        			} else if(correct.getGramems().containsAll(gr)){
        				double tmp = (correct.getGramems().size() - gr.size()) * 1. / correct.getGramems().size();
        				if(tmp > cor){
        					cor = tmp;
        				}
        				if(DEBUG_MYSTEM){
        					notAcc.add("частично верно: " + correct.getLexema() + " " + correct.getGramems().toString() + " " + gr.toString());
        				}
        			} else if(DEBUG_MYSTEM) {
	        			notAcc.add("неверно: " + correct.getLexema() + " " + correct.getGramems().toString() + " " + gr.toString());
	        		}
	            }
	            xmlr.next();
	        }
        } catch (XMLStreamException | FactoryConfigurationError | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		if(DEBUG_MYSTEM) {
			if (cor < 1) {
				System.out.println("MYSTEM:");
				System.out.println(word + " " + correct.getGramems().toString());
				for(String na : notAcc){
					System.out.println(na);
				}
				System.out.println();
			}
		}

    	countCorrMystemGr.put(correct.getPart(), countCorrMystemGr.get(correct.getPart()).doubleValue() + cor);
    	countCorrMystemLex.put(correct.getPart(), countCorrMystemLex.get(correct.getPart()).intValue() + corL);
	}

	public static void main(String[] args) throws IOException {
		countAll = new HashMap<>();
		countCorrLingvoLex = new HashMap<>();
		countCorrMystemLex = new HashMap<>();
		countCorrLingvoGr = new HashMap<>();
		countCorrMystemGr = new HashMap<>();
		for(String i : attr.getParts()){
			countAll.put(i, 0);
			countCorrLingvoLex.put(i, 0);
			countCorrMystemLex.put(i, 0);
			countCorrLingvoGr.put(i, 0.);
			countCorrMystemGr.put(i, 0.);
		}
		
		String source = "annot.opcorpora.no_ambig.xml";		
		try {
			XMLStreamReader xmlr = XMLInputFactory.newInstance().createXMLStreamReader(source, new FileInputStream(source));
			while (xmlr.hasNext()) {
                
                if(xmlr.isStartElement() && xmlr.getName().toString() == "tfr"){
                	String word = xmlr.getAttributeValue(1).replace('ё', 'е');
                	xmlr.next();
                	xmlr.next();
                	MorphoToken correct = new MorphoToken();
                	correct.setLexema(xmlr.getAttributeValue(1).toLowerCase().replace('ё', 'е'));
                	xmlr.next();
                	Set<String> gramems = new HashSet<String>();
                	while(xmlr.isStartElement() && xmlr.getName().toString() == "g"){
                		Set<String> stm = attr.sourceToMystem(xmlr.getAttributeValue(0));
                		if(stm != null){
                			gramems.addAll(stm);
                		}
                		xmlr.next(); xmlr.next();
                	}
                	
                	
                	
                	if(gramems.isEmpty() /*|| gramems.contains("V")*/){
                		continue;
                	}
                	
                	for(String i : attr.getParts()){
                		//System.out.println(i);
        				//if(stm.contains("прич")) System.out.println("kk");
        				if(gramems.contains(i)){
        					correct.setPart(i);
        					//System.out.println("d " + i);
        				}
        			}
                	//System.out.println();
                	if(correct.getPart() == null){
                		correct.setPart("Other");
                	}
                	
                	countLex++;
                	countAll.put(correct.getPart(), countAll.get(correct.getPart()).intValue() + 1);
                	correct.setGramems(gramems);
                	//System.out.print(word + " ");
                	LingvoCorrectPercent(word, correct);
                	
                	MystemCorrectPercent(word, correct);
                }
                xmlr.next();
			}
		} catch (FileNotFoundException | XMLStreamException | FactoryConfigurationError e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Лемматизация");
		System.out.println("\tLingvo\tMystem");
		for(String i : attr.getParts()){
			System.out.println(i + "\t" + new BigDecimal(countCorrLingvoLex.get(i) * 100.0 / countAll.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue() + "%\t" + new BigDecimal(countCorrMystemLex.get(i) * 100.0 / countAll.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
		}
		System.out.println();
		System.out.println("\tLingvo\tMystem");
		for(String i : attr.getParts()){
			System.out.println(i + "\t" + countCorrLingvoLex.get(i)  + "\t" + countCorrMystemLex.get(i));
		}
		
		System.out.println();
		System.out.println("Разбор");
		System.out.println("\tLingvo\tMystem");
		for(String i : attr.getParts()){
			System.out.println(i + "\t" + new BigDecimal(countCorrLingvoGr.get(i) * 100.0 / countAll.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue() + "%\t" + new BigDecimal(countCorrMystemGr.get(i) * 100.0 / countAll.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
		}
		System.out.println();
		System.out.println("\tLingvo\tMystem");
		for(String i : attr.getParts()){
			System.out.println(i + "\t" +  new BigDecimal(countCorrLingvoGr.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue()  + "\t" + new BigDecimal(countCorrMystemGr.get(i)).setScale(2, RoundingMode.HALF_UP).doubleValue());
		}
		
		System.out.println("Total words: " + countLex);
	}
	
}
