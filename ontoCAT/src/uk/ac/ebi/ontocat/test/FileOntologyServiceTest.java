package uk.ac.ebi.ontocat.test;

import java.net.URI;

import org.junit.BeforeClass;

import uk.ac.ebi.ontocat.file.FieldDescriptor;
import uk.ac.ebi.ontocat.file.FileOntologyService;

public class FileOntologyServiceTest extends OntologyServiceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		os = new FileOntologyService(new URI("http://www.ebi.ac.uk/efo"),
				new FieldDescriptor("alternative_term", "definition", "label"));
		TERM_ACCESSION1 = "EFO_0000318";
		TERM_ACCESSION2 = "EFO_0002343";
	}
}
