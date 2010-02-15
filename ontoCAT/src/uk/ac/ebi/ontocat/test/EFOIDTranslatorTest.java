package uk.ac.ebi.ontocat.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.efo.bioportal.EFOIDTranslator;
import uk.ac.ebi.efo.bioportal.IOntologyMapping;
import uk.ac.ebi.ontocat.OntologyServiceException;
import uk.ac.ebi.ontocat.OntologyTerm;
import uk.ac.ebi.ontocat.bioportal.BioportalOntologyService;
import uk.ac.ebi.ontocat.bioportal.xmlbeans.OntologyBean;

/**
 * @author $Id: EFOIDTranslatorTest.java 9019 2009-09-22 12:39:01Z tomasz $
 * 
 */

public class EFOIDTranslatorTest {
	private static final BioportalOntologyService bw = new BioportalOntologyService("tomasz@ebi.ac.uk",
			new EFOIDTranslator());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testMappings() {
		for (IOntologyMapping BPmap : new EFOIDTranslator().getMappings()) {
			try {
				printResults(bw.getTerm(BPmap.getTestCode()));
			} catch (OntologyServiceException e) {
				fail(BPmap.getTestCode() + " NOT FOUND");
			}
		}
	}

	@Test
	public void listResolvableOntologies() throws OntologyServiceException {
		for (IOntologyMapping BPmap : new EFOIDTranslator().getMappings()) {
			try {
				OntologyBean ob = (OntologyBean) bw.getOntology(BPmap.getOntologyAccession());
				printMany(new String[] {
						ob.getMetaAnnotation(), "Preferred name: " + ob.getPreferredNameSlot(),
						"Synonym: " + ob.getSynonymSlot(), "Terminologies: " + ob.getTargetTerminologies()
				});
			} catch (OntologyServiceException e) {
				e.printStackTrace();
				fail(BPmap.getTestCode() + " " + BPmap.getOntologyAccession() + " NOT FOUND");
				
			}
			
		}

	}

	@Test
	public void testSpecial() {
		// bw.searchConceptID("C27291");
		try {
			printResults(bw.getTerm("CHEBI:2365"));
		} catch (OntologyServiceException e) {
			System.out.println(e.getMessage());
		}
	}

	private void printResults(OntologyTerm ontologyTerm) throws OntologyServiceException {
		System.out.println("ID " + ontologyTerm.getAccession());
		System.out.println("LABEL " + ontologyTerm.getLabel());
		System.out.print("SYNONYMS ");
		if (ontologyTerm.getSynonyms() != null) {
			for (String syn : ontologyTerm.getSynonyms()) {
				System.out.print(syn + "\t");
			}
		}
		System.out.println();
		System.out.print("DEFINITON ");
		if (ontologyTerm.getDefinitions() != null) {
			for (String def : ontologyTerm.getDefinitions()) {
				System.out.print(def + "\t");
			}
		}
		System.out.println("SourceUrl " + bw.getQueryURL());

		System.out.println(bw.getSuccessBean().getAccessDate());
		System.out.println(bw.getSuccessBean().getAccessedResource());
		System.out.println();
	}

	private void printMany(String[] arr) {
		for (String str : arr) {
			System.out.println(str);
		}
		System.out.println("***********\n");
	}
}