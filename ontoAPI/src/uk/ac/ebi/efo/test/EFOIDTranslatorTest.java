package uk.ac.ebi.efo.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import plugin.OntologyBrowser.OntologyServiceException;
import plugin.OntologyBrowser.OntologyTermExt;
import uk.ac.ebi.efo.bioportal.BioportalMapping;
import uk.ac.ebi.efo.bioportal.BioportalService;
import uk.ac.ebi.efo.bioportal.EFOIDTranslator;
import uk.ac.ebi.efo.bioportal.xmlbeans.OntologyBean;

/**
 * @author $Id: EFOIDTranslatorTest.java 9019 2009-09-22 12:39:01Z tomasz $
 * 
 */

public class EFOIDTranslatorTest {
	private static final BioportalService bw = new BioportalService("tomasz@ebi.ac.uk", new EFOIDTranslator());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testMappings() {
		for (BioportalMapping BPmap : new EFOIDTranslator().getMappings()) {
			try {
				printResults(bw.getTerm(BPmap.getTestCode()));
			} catch (OntologyServiceException e) {
				System.out.println(BPmap.getTestCode() + " NOT FOUND");
				fail();
			}
		}
	}

	@Test
	public void listResolvableOntologies() throws OntologyServiceException {
		for (BioportalMapping BPmap : new EFOIDTranslator().getMappings()) {
			OntologyBean ob = (OntologyBean) bw.getOntologyDescription(BPmap.getOntologyID());
			printMany(new String[] {
					ob.getMetaAnnotation(), "Preferred name: " + ob.getPreferredNameSlot(),
					"Synonym: " + ob.getSynonymSlot(), "Terminologies: " + ob.getTargetTerminologies()
			});
		}

	}

	@Test
	public void testSpecial() {
		// bw.searchConceptID("NCI C3235");
		try {
			printResults(bw.getTerm("SNOMEDCT_2005_01_31:45410002"));
		} catch (OntologyServiceException e) {
			System.out.println(e.getMessage());
		}
	}

	private void printResults(OntologyTermExt ontologyTerm) throws OntologyServiceException {
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