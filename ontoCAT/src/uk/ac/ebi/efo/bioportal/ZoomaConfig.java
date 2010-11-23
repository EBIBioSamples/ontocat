package uk.ac.ebi.efo.bioportal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ebi.ontocat.Ontology;
import uk.ac.ebi.ontocat.OntologyService;
import uk.ac.ebi.ontocat.bioportal.BioportalOntologyService;
import uk.ac.ebi.ontocat.ols.OlsOntologyService;

public class ZoomaConfig {
	private static final Logger log = Logger.getLogger(ZoomaConfig.class);

	public static void main(String[] args) throws Exception {
		List<String> acceptList = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			{
				add("Human disease");
				add("Chemical entities of biological interest");
				add("Cell type");
				add("Human Phenotype Ontology");
				add("Foundational Model of Anatomy");
				add("Mouse gross anatomy and development");
				add("Mouse adult gross anatomy");
				add("Mammalian phenotype");
				add("Drosophila development");
				add("Drosophila gross anatomy");
				add("Mosquito gross anatomy");
				add("Zebrafish anatomy and development");
				add("Malaria Ontology");
				add("Plant growth and developmental stage");
				add("Plant structure");
				add("C. elegans development");
				add("C. elegans gross anatomy");
				add("C. elegans phenotype");
				add("NCI Thesaurus");
				add("International Classification of Diseases");
				add("NCBI organismal classification");
				add("Medical Subject Headings");
				add("SNOMED Clinical Terms");
				add("Ontology for Biomedical Investigations");
			}
		};

		Map<String,String> acceptMap = new HashMap<String,String>();
		
		// Map accessions to labels
		OntologyService os = new BioportalOntologyService();
		for (Ontology o : os.getOntologies()){
			for (String acc : acceptList){
				if (o.getLabel().equals(acc)){
					if (acceptMap.containsKey(o.getLabel())){
						log.warn("Already found " + o.getLabel() + " at "
								+ acceptMap.get(o.getLabel()));
					} else {
						acceptMap.put(o.getLabel(), o.getOntologyAccession());
					}

				}
			}		
		}

		// produce config
		int c = 0;
		for (String acc : acceptList) {
			c++;
			if (acceptMap.containsKey(acc)) {
				// System.out.println(os.getOntology(acceptMap.get(acc)).getLabel());
				System.out.println(os.getOntology(acceptMap.get(acc)).getAbbreviation() + "_acc"
						+ c
						+ "=" + acceptMap.get(acc));
			} else {
				log.error(acc + " left unmapped");
			}
		}

		// get rejected
		c = 0;
		for (Ontology o : os.getOntologies()) {
			if (!acceptMap.containsValue(o.getOntologyAccession())) {
				c++;
				System.out.println("rej" + c + "="
						+ o.getOntologyAccession());
			}
		}
		os = new OlsOntologyService();
		for (Ontology o : os.getOntologies()) {
			c++;
			System.out.println("rej" + c + "=" + o.getOntologyAccession());
		}

	}
}
