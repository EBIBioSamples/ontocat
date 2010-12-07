package uk.ac.ebi.ontocat.virtual;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import uk.ac.ebi.ontocat.OntologyService;
import uk.ac.ebi.ontocat.OntologyServiceException;
import uk.ac.ebi.ontocat.OntologyTerm;
import uk.ac.ebi.ontocat.OntologyService.SearchOptions;
import uk.ac.ebi.ontocat.ols.OlsOntologyService;

/**
 * The Class SortedSubsetDecorator. Decorator adding sorting and subsetting
 * capabilities to an OntologyService. Implemented using Java reflection dynamic
 * proxy pattern See the following link for more details
 * http://www.webreference.com/internet/reflection/3.html
 * 
 * @author Tomasz Adamusiak
 */
@SuppressWarnings("unchecked")
public class CompositeDecorator implements InvocationHandler {
	/** Underlying services */
	private List<OntologyService> ontoServices;

	/** Number of threads to run concurrently */
	private int nThreads = 5;

	public void setNumberOfThreads(int val) {
		nThreads = val;
	}

	public int getNumberOfThreads() {
		return nThreads;
	}

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(CompositeDecorator.class);

	/**
	 * Instantiates a new sorted subset decorator.
	 * 
	 * @param obj
	 *            the obj
	 * @param list
	 *            the list
	 */
	private CompositeDecorator(List<OntologyService> ontoServices) {
		this.ontoServices = ontoServices;
	}

	/**
	 * Creates the proxy.
	 * 
	 * @param obj
	 *            the obj
	 * @param list
	 *            the list
	 * 
	 * @return the object
	 * @throws OntologyServiceException
	 */
	private static Object createProxy(Object obj, List list) throws OntologyServiceException {
		// If an exception if thrown here, the OntologyService
		// interface must have changed and you have to modify
		// the <searchAll> strings below and in the invoke method
		try {
			obj.getClass().getMethod("searchAll", String.class, SearchOptions[].class);
			obj.getClass().getMethod("getOntologies");
			obj.getClass().getMethod("getTermPath", String.class, String.class);
			obj.getClass().getMethod("getTermPath", OntologyTerm.class);
		} catch (Exception e) {
			log.fatal("Signature has changed in proxy pattern!");
			throw new OntologyServiceException(e);
		}
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass()
				.getInterfaces(), new CompositeDecorator(list));
	}

	public static OntologyService getService(List list) throws OntologyServiceException {
		// instantiate OLSService,
		// it's never used but need an instance of OntologyService interface
		// to properly reflect, could use anything else, or instantiate
		// a private type
		return (OntologyService) CompositeDecorator.createProxy(new OlsOntologyService(), list);
	}

	/**
	 * Alternative constructor to simplify access in examples
	 * 
	 * @param list
	 *            list of ontologies to combine
	 * @return the composite service service
	 * @throws OntologyServiceException
	 *             the ontology service exception
	 */
	public static OntologyService getService(OntologyService... list)
			throws OntologyServiceException {
		// instantiate OLSService,
		// it's never used but need an instance of OntologyService interface
		// to properly reflect, could use anything else, or instantiate
		// a private type
		return (OntologyService) CompositeDecorator.createProxy(new OlsOntologyService(), Arrays
				.asList(list));
	}

	// here the magic happens
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		ExecutorService ec = Executors.newFixedThreadPool(nThreads);

		// As the very least should return an empty collection
		if (method.getReturnType() == List.class)
			result = new ArrayList();
		if (method.getReturnType() == Map.class)
			result = new HashMap();
		if (method.getReturnType() == Set.class)
			result = new HashSet();

		// Create tasks
		Collection<InvokeTask> tasks = new ArrayList<InvokeTask>();
		for (OntologyService os : ontoServices) {
			tasks.add(new InvokeTask(os, method, args));
		}

		// Run tasks in parallel

		if (method.getName().equalsIgnoreCase("searchAll")
				|| method.getName().equalsIgnoreCase("getOntologies")) {
			// searchAll or getOntologies so combine results
			for (Future<Object> future : ec.invokeAll(tasks)) {
				try {
					((List) result).addAll((Collection) future.get());
				} catch (ExecutionException e) {
					if (e.getCause() instanceof NoResultsException) {
						log.debug(e.getCause().getMessage());
					} else {
						log.error(e + " " + e.getCause().getMessage());
					}
				}
			}
		} else {
			// Any valid results will do, don't wait for the others
			try {
				result = ec.invokeAny(tasks);
			} catch (ExecutionException e) {
				if (e.getCause() instanceof NoResultsException) {
					log.debug(e.getCause().getMessage());
				} else {
					log.error(e + " " + e.getCause().getMessage());
				}
			}
		}

		// final sort for the levenshtein to kick in
		// and break the internal sorting per ontology source
		if (method.getName().equalsIgnoreCase("searchAll"))
			Collections.sort((List<OntologyTerm>) result);

		ec.shutdown();
		return result;
	}

	private class InvokeTask implements Callable<Object> {
		private Object target;
		private Method method;
		private Object[] args;

		public InvokeTask(Object target, Method method, Object[] args) {
			this.target = target;
			this.method = method;
			this.args = args;
		}

		@Override
		public Object call() throws Exception {
			Object result = method.invoke(target, args);

			// Throw exception here, so invokeAny skips this result
			NoResultsException e = new NoResultsException("No results from " + method.getName()
					+ " in " + target.getClass().getSimpleName());

			if (result == null)
				throw e;
			if (result instanceof Map && ((Map) result).size() == 0)
				throw e;
			if (result instanceof Set && ((Set) result).size() == 0)
				throw e;
			if (result instanceof List) {
				Integer listSize = ((List) result).size();
				String methodName = method.getName();
				if ((listSize == 0)
						|| (methodName.equalsIgnoreCase("getTermPath") && listSize == 1))
					throw e;
			}

			return result;
		}
	}

	public class NoResultsException extends Exception {
		private static final long serialVersionUID = 1L;

		public NoResultsException(String str) {
			super(str);
		}
	}

}