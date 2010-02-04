/**
 * 
 */
package uk.ac.ebi.ontoapi.bioportal.xmlbeans;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.ontoapi.OntologyTerm;

/**
 * @author Tomasz Adamusiak, Morris Swertz
 * 
 */

public class EntryBean {

	/** The string. */
	private ArrayList<String> strings;

	/** The list. */
	private ArrayList list;

	/** The Unmodifiable collection. */
	private ArrayList UnmodifiableCollection;

	/** The counter. */
	private Integer counter;

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return strings.get(0);
	}

	public String getPath() {
		return strings.get(1);
	}

	/**
	 * Gets the list.
	 * 
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public String[] getList() {
		try {
			if (list != null && list.get(0) instanceof String) {
				return (String[]) list.toArray(new String[0]);
			} else
				return null;
		} catch (ArrayStoreException ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<OntologyTerm> getConceptBeans() {
		if (list.get(0) instanceof ConceptBean) {
			return list;
		} else
			return null;
	}
}