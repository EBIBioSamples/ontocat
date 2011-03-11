package uk.ac.ebi.ontocat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import uk.ac.ebi.ontocat.OntologyService.SearchOptions;

/**
 * The Class CachedServiceDecorator. Decorator adding two caching layers to any
 * requests coming through the OntologyService Layer. All requests are first
 * checked against the first cache with 24h expiry, otherwise the request is
 * passed through to the original provider. If the original provider query
 * fails, the results are returned from the eternal cache if available.
 * <p>
 * Implemented using Java reflection dynamic proxy pattern See the following
 * link for more details http://www.webreference.com/internet/reflection/3.html
 * 
 * @author Tomasz Adamusiak, Niran Abeygunawardena
 */
public class HudsonDecorator implements InvocationHandler {
	// standard invocation
	// see http://www.webreference.com/internet/reflection/4.html
	/** The target. */
	private Object target;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(HudsonDecorator.class);

	private static Cache ServiceCache;
	private static Cache EternalCache;

	/**
	 * Instantiates a new sorted subset decorator.
	 * 
	 * @param obj
	 *            the obj
	 * @param list
	 *            the list
	 */
	private HudsonDecorator(Object obj) {
		target = obj;
		// Initialise the cache singleton
		System.setProperty("net.sf.ehcache.enableShutdownHook", "true");
		CacheManager manager = CacheManager.create(getClass().getResource("ehcache.xml"));
		ServiceCache = manager.getCache("OntologyServiceCache");
		EternalCache = manager.getCache("EternalServiceCache");
	}

	public static void clearCache() {
		CacheManager.getInstance().clearAll();
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
	private static Object createProxy(Object obj) throws OntologyServiceException {
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass()
				.getInterfaces(), new HudsonDecorator(obj));
	}

	public static OntologyService getService(OntologyService os) throws OntologyServiceException {
		return (OntologyService) HudsonDecorator.createProxy(os);
	}

	// Cache all the methods via EHCACHE
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		String cacheKey = method.getName() + ArgsToKey(args);

		// As the very least should return an empty collection
		if (method.getReturnType() == List.class) {
			result = Collections.emptyList();
		}
		if (method.getReturnType() == Map.class) {
			result = Collections.emptyMap();
		}
		if (method.getReturnType() == Set.class) {
			result = Collections.emptySet();
		}

		try {
			System.out
			.println("add the result to cache if it's not there already");
			if (ServiceCache != null && ServiceCache.get(cacheKey) == null) {
				System.out.println("Adding to the cache now.");
				Element el = new Element(cacheKey, method.invoke(target, args));
				System.out.println("The element is " + el);
				ServiceCache.put(el);
				EternalCache.put(el);
			}
			System.out.println("get the result from cache");
			System.out.println(cacheKey);
			System.out.println("that result is " + ServiceCache.get(cacheKey));
			result = ServiceCache.get(cacheKey).getValue();
			System.out.println("got the result from cache");

		} catch (InvocationTargetException e) {
			if (EternalCache != null && EternalCache.get(cacheKey) != null) {
				result = EternalCache.get(cacheKey).getValue();
				log.warn("Accessing eternal cache for " + cacheKey);
			} else {
				throw new OntologyServiceException(e);
			}
		}
		return result;
	}

	private String ArgsToKey(Object[] args) throws OntologyServiceException {
		String s = "";
		if (args == null) {
			return "";
		}
		for (Object arg : args) {
			if (arg instanceof String) {
				s += "|" + (String) arg;
			} else if (arg instanceof OntologyTerm || arg instanceof Ontology) {
				s += "|" + arg.toString();
			} else if (arg instanceof SearchOptions[]) {
				for (SearchOptions o : (SearchOptions[]) arg) {
					s += "|" + o.toString();
				}
			} else {
				log.fatal("Unrecognised parameter in cached call. Could not create key!");
				throw new OntologyServiceException(new Exception(
				"Unrecognised parameter in cached call."));
			}
		}
		return s;
	}

}