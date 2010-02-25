package uk.ac.ebi.ontocat;

/**
 * @author Tomasz Adamusiak, Morris Swertz
 * @version $Id$
 */
public class OntologyServiceException extends Exception
{
	private static final long serialVersionUID = -1620042517905054570L;

	public OntologyServiceException(String str)
	{
		super(str);
	}

	public OntologyServiceException(Exception e)
	{
		super(e);
	}
}
