package smartcampus.service.unitnnews.test;

import it.sayservice.platform.core.bus.common.exception.PersistenceException;
import it.sayservice.platform.core.bus.service.handler.BusServiceHandler;
import it.sayservice.platform.core.bus.service.persistence.PersistenceEngine;
import it.sayservice.platform.core.common.exception.EntityNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.protobuf.InvalidProtocolBufferException;

public class TestDataFlow extends TestCase {
	public void testRun() throws PersistenceException, EntityNotFoundException, InvalidProtocolBufferException {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "/test-context.xml", "/applicationContext.xml" });
		assertNotNull(context);
		PersistenceEngine persistenceEngine = (PersistenceEngine) context.getBean("busPersistenceEngineDAO");
		assertNotNull(persistenceEngine);

		BusServiceHandler busServiceHandler = (BusServiceHandler) context.getBean("busServiceHandler");
		assertNotNull(busServiceHandler);
		busServiceHandler.start();

//		System.out.println("Waiting...");
//		Scanner in = new Scanner(System.in);
//		in.nextLine();

		Map<String, Object> emptyPars = new HashMap<String, Object>();
		
		Map<String, Object> pars = new HashMap<String, Object>();
//		pars.put("baseurl", "http://www.unitn.it");
//		pars.put("variableurl", "ateneo");
//		pars.put("source", "ateneo");
		pars.put("baseurl", "http://www.unisport.tn.it");
		pars.put("variableurl", "");
		pars.put("source", "unisport");
		
		Object object = null;
		try {
//			object = busServiceHandler.invokeService("smartcampus.service.unitnnews", "GetOperaNews", emptyPars, null);
//			System.out.println(object);
			object = busServiceHandler.invokeService("smartcampus.service.unitnnews", "GetUnitnNews", pars, null);
			System.out.println(object);
//			object = busServiceHandler.invokeService("smartcampus.service.unitnnews", "GetCiscaNews", emptyPars, null);
//			System.out.println(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Waiting...");
		Scanner in = new Scanner(System.in);
		in.nextLine();		
		
	}

}
