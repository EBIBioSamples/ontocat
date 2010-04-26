package uk.ac.ebi.ontocat.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.ontocat.OntologyService;
import uk.ac.ebi.ontocat.OntologyServiceException;
import uk.ac.ebi.ontocat.OntologyTerm;
import uk.ac.ebi.ontocat.bioportal.BioportalOntologyService;
import uk.ac.ebi.ontocat.file.FileOntologyService;
import uk.ac.ebi.ontocat.ols.OlsOntologyService;
import uk.ac.ebi.ontocat.virtual.CompositeDecorator;

public class CompositeDecoratorTest extends AbstractOntologyServiceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final OntologyService osBP = new BioportalOntologyService();
		final OntologyService osOLS = new OlsOntologyService();
		final FileOntologyService osFile = new FileOntologyService(new URI(
				"http://www.ebi.ac.uk/efo"));
		osFile.setSynonymSlot("alternative_term");

		os = CompositeDecorator.getService(new ArrayList<OntologyService>() {
			{
				add(osOLS);
				add(osBP);
				add(osFile);
			}
		});
		// GO accession
		ONTOLOGY_ACCESSION = "1070";
	}

	@Test
	@Ignore("Tested equals() on OntologyTerm, fixed now")
	public void testHashCollapse() throws OntologyServiceException {
		List<OntologyTerm> l = os.searchAll("thymus");
		log.info("List size " + l.size());
		Set<OntologyTerm> h = new HashSet<OntologyTerm>(l);
		log.info("Hash size " + h.size());
		assertEquals(l.size(), h.size());
		// for (OntologyTerm o : l) log.info(o);
	}

	@Test
	public void testConcurrentRequests() throws OntologyServiceException, URISyntaxException {
		ExecutorService ec = Executors.newFixedThreadPool(50);
		Collection<RequestTask> tasks = new ArrayList<RequestTask>();
		// Create tasks
		for (int i = 0; i < 50; i++) {
			tasks.add(new RequestTask(os));
		}
		try {
			ec.invokeAll(tasks);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class RequestTask implements Callable<Object> {
		private OntologyService os;
		public RequestTask(OntologyService os) {
			this.os = os;
		}

		@Override
		public Object call() throws Exception {
			try {
				os.searchAll("thymus");
			} catch (OntologyServiceException e) {
				log.error(e);
			}
			return null;
		}

	}
}
