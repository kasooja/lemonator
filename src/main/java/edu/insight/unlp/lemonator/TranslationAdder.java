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
		//String fileName = "src/main/resources/caro/caro.csv";
		//String ontoName = "src/main/resources/caro/caro.owl";
		String ontoName = args[0];
		String fileName = args[1];

		StringBuilder bldr = new StringBuilder();
		Map<String, String> enUri = new HashMap<String, String>();

		Map<String, String> enDeExp = new HashMap<String, String>();
		Map<String, String> enDeMac = new HashMap<String, String>();

		Map<String, String> enEsExp = new HashMap<String, String>();
		Map<String, String> enEsMac = new HashMap<String, String>();

		Map<String, String> enItExp = new HashMap<String, String>();
		Map<String, String> enItMac = new HashMap<String, String>();

		Map<String, String> enGaExp = new HashMap<String, String>();
		Map<String, String> enGaMac = new HashMap<String, String>();

		Map<String, String> enSlExp = new HashMap<String, String>();
		Map<String, String> enSlMac = new HashMap<String, String>();

		Map<String, String> enCsExp = new HashMap<String, String>();
		Map<String, String> enCsMac = new HashMap<String, String>();

		try {
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			try {
				String[] values = reader.readNext();
				while ( values != null ) {
					int i = 0;
					
					System.out.println(Arrays.asList(values));
					String uri = values[i++];
					String predicate = values[i++];
					String english = values[i++];

					String germanExpert = values[i++];
					String germanMachine = values[i++];

					String spanishExpert = values[i++];
					String spanishMachine = values[i++];

					String italianExpert = values[i++];
					String italianMachine = values[i++];

					String irishExpert = values[i++];
					String irishMachine = values[i++];

					String sloveneExpert = values[i++];
					String sloveneMachine = values[i++];

					String czechExpert = values[i++];
					String czechMachine = values[i++];

					if("source label".equalsIgnoreCase(english.trim())){
						values = reader.readNext();
						continue;						
					}

					enUri.put(english.trim().toLowerCase(), uri);

					enDeExp.put(english.trim().toLowerCase(), germanExpert.trim());//no lowercasing in case of german
					enDeMac.put(english.trim().toLowerCase(), germanMachine.trim());

					enEsExp.put(english.trim().toLowerCase(), spanishExpert.trim().toLowerCase());
					enEsMac.put(english.trim().toLowerCase(), spanishMachine.trim().toLowerCase());

					enItExp.put(english.trim().toLowerCase(), italianExpert.trim().toLowerCase());
					enItMac.put(english.trim().toLowerCase(), italianMachine.trim().toLowerCase());

					enGaExp.put(english.trim().toLowerCase(), spanishExpert.trim().toLowerCase());
					enGaMac.put(english.trim().toLowerCase(), spanishMachine.trim().toLowerCase());

					enSlExp.put(english.trim().toLowerCase(), sloveneExpert.trim().toLowerCase());
					enSlMac.put(english.trim().toLowerCase(), sloveneMachine.trim().toLowerCase());

					enCsExp.put(english.trim().toLowerCase(), czechExpert.trim().toLowerCase());
					enCsMac.put(english.trim().toLowerCase(), czechMachine.trim().toLowerCase());

					values = reader.readNext();
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
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

						String germanExpert = enDeExp.get(english).trim();
						String germanMachine = enDeMac.get(english).trim();

						String spanishExpert = enEsExp.get(english).trim();
						String spanishMachine = enEsMac.get(english).trim();

						String italianExpert = enItExp.get(english).trim();
						String italianMachine = enItMac.get(english).trim();

						String irishExpert = enGaExp.get(english).trim();
						String irishMachine = enGaMac.get(english).trim();

						String sloveneExpert = enSlExp.get(english).trim();
						String sloveneMachine = enSlMac.get(english).trim();

						String czechExpert = enCsExp.get(english).trim();
						String czechMachine = enCsMac.get(english).trim();

						String german = !germanExpert.equalsIgnoreCase("") ? germanExpert : germanMachine;
						String spanish = !spanishExpert.equalsIgnoreCase("") ? spanishExpert : spanishMachine;
						String italian = !italianExpert.equalsIgnoreCase("") ? italianExpert : italianMachine;
						String irish = !irishExpert.equalsIgnoreCase("") ? irishExpert : irishMachine;
						String slovene = !sloveneExpert.equalsIgnoreCase("") ? sloveneExpert : sloveneMachine;
						String czech = !czechExpert.equalsIgnoreCase("") ? czechExpert : czechMachine;

						english = "<rdfs:label xml:lang=\"en\">" + english + "</rdfs:label>";
						german = "<rdfs:label xml:lang=\"de\">" + german + "</rdfs:label>";
						spanish = "<rdfs:label xml:lang=\"es\">" + spanish + "</rdfs:label>";
						italian = "<rdfs:label xml:lang=\"it\">" + italian + "</rdfs:label>";
						irish = "<rdfs:label xml:lang=\"ga\">" + irish + "</rdfs:label>";
						slovene = "<rdfs:label xml:lang=\"sl\">" + slovene + "</rdfs:label>";
						czech = "<rdfs:label xml:lang=\"cs\">" + czech + "</rdfs:label>";

						bldr.append(english + "\n");
						bldr.append(german + "\n");
						bldr.append(spanish + "\n");
						bldr.append(italian + "\n");
						bldr.append(irish + "\n");
						bldr.append(slovene + "\n");
						bldr.append(czech + "\n");
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
			for(String english : enEsExp.keySet()){
				String uri = enUri.get(english);

				String germanExpert = enDeExp.get(english).trim();
				String germanMachine = enDeMac.get(english).trim();

				String spanishExpert = enEsExp.get(english).trim();
				String spanishMachine = enEsMac.get(english).trim();

				String italianExpert = enItExp.get(english).trim();
				String italianMachine = enItMac.get(english).trim();

				String irishExpert = enGaExp.get(english).trim();
				String irishMachine = enGaMac.get(english).trim();

				String sloveneExpert = enSlExp.get(english).trim();
				String sloveneMachine = enSlMac.get(english).trim();

				String czechExpert = enCsExp.get(english).trim();
				String czechMachine = enCsMac.get(english).trim();

				String german = !germanExpert.equalsIgnoreCase("") ? germanExpert : germanMachine;
				String spanish = !spanishExpert.equalsIgnoreCase("") ? spanishExpert : spanishMachine;
				String italian = !italianExpert.equalsIgnoreCase("") ? italianExpert : italianMachine;
				String irish = !irishExpert.equalsIgnoreCase("") ? irishExpert : irishMachine;
				String slovene = !sloveneExpert.equalsIgnoreCase("") ? sloveneExpert : sloveneMachine;
				String czech = !czechExpert.equalsIgnoreCase("") ? czechExpert : czechMachine;


				//<http://saffron.insight-centre.org/finance/topic/tarp_program>  "TARP program" .
				german = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + german + "\" .";
				spanish = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + spanish + "\" .";
				italian = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + italian + "\" .";
				irish = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + irish + "\" .";
				slovene = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + slovene + "\" .";
				czech = "<" + uri + ">" + " " +  "<http://www.w3.org/2000/01/rdf-schema#label>" + " \"" + czech + "\" .";


				bldr.append(german + "\n");
				bldr.append(spanish + "\n");
				bldr.append(italian + "\n");
				bldr.append(irish + "\n");
				bldr.append(slovene + "\n");
				bldr.append(czech + "\n");
			}
			//BasicFileTools.writeFile("src/main/resources/caroOtto.owl", bldr.toString().trim());
			BasicFileTools.writeFile(args[2], onto + bldr.toString().trim());
		}	
	}
}
