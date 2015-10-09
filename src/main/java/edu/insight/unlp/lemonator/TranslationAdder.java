package edu.insight.unlp.lemonator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVReader;

public class TranslationAdder {

	public static void main(String[] args) {
		//String fileName = "src/main/resources/caro.owl.csv";
		//String ontoName = "src/main/resources/caro.owl";
		String ontoName = args[0];
		String fileName = args[1];

		StringBuilder bldr = new StringBuilder();
		Map<String, String> enUri = new HashMap<String, String>();
		Map<String, String> enDe = new HashMap<String, String>();
		Map<String, String> enEs = new HashMap<String, String>();
		Map<String, String> enIt = new HashMap<String, String>();
		Map<String, String> enGa = new HashMap<String, String>();

		try {
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			try {
				String[] values = reader.readNext();
				while ( values != null ) {
					System.out.println(Arrays.asList(values));
					String uri = values[0];
					String english = values[1];
					String german = values[2];
					String spanish = values[3];
					String italian = values[4];
					String irish = values[5];
					if("source label".equalsIgnoreCase(english.trim())){
						values = reader.readNext();
						continue;						
					}
					enUri.put(english.trim().toLowerCase(), uri);
					enDe.put(english.trim().toLowerCase(), german.trim());//.toLowerCase());
					enEs.put(english.trim().toLowerCase(), spanish.trim().toLowerCase());
					enIt.put(english.trim().toLowerCase(), italian.trim().toLowerCase());
					enGa.put(english.trim().toLowerCase(), irish.trim().toLowerCase());
					values = reader.readNext();
				}
			} finally {
				// we have to close reader manually
				reader.close();
			}
		} catch (IOException e) {
			// we have to process exceptions when it is not required
			e.printStackTrace();
		}
		System.out.println();
		//Pattern pattern = Pattern.compile("<rdfs:label rdf:datatype=\"&xsd;string\">(.*)</rdfs:label>");

		if(ontoName.endsWith(".owl")){
			Pattern pattern = Pattern.compile("<rdfs:label rdf:datatype=\".*string\">(.*)</rdfs:label>");
			String line = null;
			BufferedReader br = BasicFileTools.getBufferedReaderFile(ontoName);
			try {
				while((line = br.readLine())!=null){
					Matcher matcher = pattern.matcher(line);
					if(matcher.find()){
						String english = matcher.group(1).trim();
						String german = enDe.get(english);
						String spanish = enEs.get(english);
						String italian = enIt.get(english);
						String irish = enGa.get(english);

						english = "<rdfs:label xml:lang=\"en\">" + english + "</rdfs:label>";
						german = "<rdfs:label xml:lang=\"de\">" + german + "</rdfs:label>";
						spanish = "<rdfs:label xml:lang=\"es\">" + spanish + "</rdfs:label>";
						italian = "<rdfs:label xml:lang=\"it\">" + italian + "</rdfs:label>";
						irish = "<rdfs:label xml:lang=\"ga\">" + irish + "</rdfs:label>";

						bldr.append(english + "\n");
						bldr.append(german + "\n");
						bldr.append(spanish + "\n");
						bldr.append(italian + "\n");
						bldr.append(irish + "\n");
					} else {
						bldr.append(line + "\n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//BasicFileTools.writeFile("src/main/resources/caroOtto.owl", bldr.toString().trim());
			BasicFileTools.writeFile(args[2], bldr.toString().trim());
		} else if(ontoName.endsWith(".nt")){
			String onto = BasicFileTools.extractText(ontoName);
			for(String english : enEs.keySet()){
				String uri = enUri.get(english);
				String german = enDe.get(english);
				String spanish = enEs.get(english);
				String italian = enIt.get(english);
				String irish = enGa.get(english);

				//<http://saffron.insight-centre.org/finance/topic/tarp_program>  "TARP program" .
				german = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + german + "\" .";
				spanish = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + spanish + "\" .";
				italian = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + italian + "\" .";
				irish = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + irish + "\" .";

				bldr.append(german + "\n");
				bldr.append(spanish + "\n");
				bldr.append(italian + "\n");
				bldr.append(irish + "\n");
			}
			//BasicFileTools.writeFile("src/main/resources/caroOtto.owl", bldr.toString().trim());
			BasicFileTools.writeFile(args[2], onto + bldr.toString().trim());
		}	
	}
}
