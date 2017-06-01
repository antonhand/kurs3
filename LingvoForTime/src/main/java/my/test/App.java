package my.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import com.github.stagirs.lingvo.morpho.MorphoDictionary;

public class App 
{
	static BufferedReader in;
	static PrintWriter out;
	
	private static void print_norms(String[] norms)
	{
		for(int i = 0; i < norms.length; i++){
			if(i != 0 && norms[i].equals(norms[i-1])){
				continue;
			}
			out.print("<ana lex=\"" + norms[i].toLowerCase() + "\" />");
		}
	}
	
    public static void main( String[] args ) throws IOException
    {
    	in = Files.newBufferedReader(Paths.get(args[0]), Charset.forName("utf-8"));
    	out = new PrintWriter(args[1], "utf-8");
    	String s;
    	out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println("<html><body>");
		out.println();
		out.println("<se>");
    	while((s = in.readLine()) != null){
    		StringTokenizer st = new StringTokenizer(s);
    		while(st.hasMoreTokens()){
    			String stm = st.nextToken();
    			out.print("<w>");
    			MorphoDictionary md = MorphoDictionary.get(stm);
    			md.getRawForms();
    			print_norms(md.getNorms());
    			md.getRawForms();
    			out.print("</w>");
    			out.println();
    		}
    	}
    	out.println("</se>");
    	out.println("</body></html>");
    	out.close();
    	in.close();
    }
}
