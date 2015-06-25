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
		String fileName = "src/main/resources/caro.owl.csv";
		String ontoName = "src/main/resources/caro.owl";
		StringBuilder bldr = new StringBuilder();
		Map<String, String> enDe = new HashMap<String, String>();
		Map<String, String> enEs = new HashMap<String, String>();
		Map<String, String> enIt = new HashMap<String, String>();

		try {
			// duplicate full set of settings of CSV file format
			//			CSVReader reader = new CSVReader(new InputStreamReader(
			//					new FileInputStream(fileName), "UTF-8"), 
			//					';', '\'', 1); // it is not clear what arguments means
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

			try {
				String[] values = reader.readNext();
				while ( values != null ) {
					System.out.println(Arrays.asList(values));					
					String english = values[0];
					String german = values[1];
					String spanish = values[2];
					String italian = values[3];
					if("source label".equalsIgnoreCase(english.trim())){
						values = reader.readNext();
						continue;						
					}
					enDe.put(english.trim(), german.trim());
					enEs.put(english.trim(), spanish.trim());
					enIt.put(english.trim(), italian.trim());
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
		Pattern pattern = Pattern.compile("<rdfs:label rdf:datatype=\"&xsd;string\">(.*)</rdfs:label>");

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

					english = "<rdfs:label xml:lang=\"en\">" + english + "</rdfs:label>";
					german = "<rdfs:label xml:lang=\"de\">" + german + "</rdfs:label>";
					spanish = "<rdfs:label xml:lang=\"es\">" + spanish + "</rdfs:label>";
					italian = "<rdfs:label xml:lang=\"it\">" + italian + "</rdfs:label>";

					bldr.append(english + "\n");
					bldr.append(german + "\n");
					bldr.append(spanish + "\n");
					bldr.append(italian + "\n");

				} else {
					bldr.append(line + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		BasicFileTools.writeFile("src/main/resources/caroOtto.owl", bldr.toString().trim());
	}	

}
