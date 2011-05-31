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
package uk.ac.ebi.ontocat.examples;

import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ebi.ontocat.OntologyService;
import uk.ac.ebi.ontocat.OntologyServiceException;
import uk.ac.ebi.ontocat.OntologyTerm;
import uk.ac.ebi.ontocat.file.ReasonedFileOntologyService;

public class Sandbox {

	private static final Logger log = Logger.getLogger(Sandbox.class);

	/**
	 * @param args
	 * @throws URISyntaxException
	 * @throws OntologyServiceException
	 * @throws UnknownOptionException
	 * @throws IllegalOptionValueException
	 */
	public static void main(String[] args) throws URISyntaxException,
	OntologyServiceException, IllegalOptionValueException,
	UnknownOptionException {

		OntologyService os = new ReasonedFileOntologyService(
				new File(
				"C:\\work\\EFO\\EFO on bar\\ExperimentalFactorOntology\\ExFactorInOWL\\releasecandidate\\efo_release_candidate.owl")
				.toURI(), "efo");
		Set<OntologyTerm> organisms = os.getAllChildren("efo", "OBI_0100026");
		log.info("Organism has " + organisms.size()
				+ " children");

		for (OntologyTerm t : organisms) {
			if (!t.getAccession().contains("NCBITaxon")) {
				System.out.println(t.getLabel() + "\t"
						+ t.getAccession());
				for (String syn : os.getSynonyms(t)) {
					System.out.println(syn + "\t"
							+ t.getAccession());
				}
			}
		}

	}

	public static String getURI(OntologyTerm term) {
		return "http://purl.org/obo/owl/CL#" + term.getAccession();
		// return ((ConceptBean) term).getFullId();
	}
}
