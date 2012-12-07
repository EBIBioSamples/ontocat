/**
 * Copyright (c) 2010 - 2011 European Molecular Biology Laboratory and University of Groningen
 *
 * Contact: ontocat-users@lists.sourceforge.net
 *
 * This file is part of OntoCAT
 *
 * OntoCAT is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * OntoCAT is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with OntoCAT. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ebi.ontocat;

import static org.junit.Assert.assertNotSame;

import java.net.URI;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.ontocat.file.FileOntologyService;
import uk.ac.ebi.ontocat.special.AbstractOntologyServiceTest;

public class FileOntologyServiceTest extends AbstractOntologyServiceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ONTOLOGY_ACCESSION = "GOslim";
		TERM_ACCESSION = "GO_0005694";

		os = new FileOntologyService(
				new URI(
				"http://www.geneontology.org/GO_slims/goslim_generic.obo"),
				ONTOLOGY_ACCESSION);


	}

	@Test
	public final void testGetAllTerms() throws OntologyServiceException {
		printCurrentTest();
		Set<OntologyTerm> set = os.getAllTerms(ONTOLOGY_ACCESSION);
		assertNotSame("Empty set returned!", 0, set.size());
		println(set.size());
	}
}
