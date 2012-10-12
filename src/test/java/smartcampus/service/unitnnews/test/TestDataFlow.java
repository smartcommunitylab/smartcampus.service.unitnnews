package smartcampus.service.unitnnews.test;

import it.sayservice.platform.servicebus.test.DataFlowTestHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import smartcampus.service.unitnnews.data.message.Unitnnews.NewsEntry;
import smartcampus.service.unitnnews.impl.GetOperaNewsDataFlow;

public class TestDataFlow extends TestCase {

	public void test() {
		try {
			Map<String, Object> emptyPars = new HashMap<String, Object>();
			
			Map<String, Object> pars = new HashMap<String, Object>();
//			pars.put("baseurl", "http://www.unisport.tn.it");
//			pars.put("variableurl", "");
//			pars.put("source", "unisport");			
			// pars.put("baseurl", "http://www.unitn.it");
			// pars.put("variableurl", "ateneo");
			// pars.put("source", "ateneo");
			
			DataFlowTestHelper helper = new DataFlowTestHelper();
			Map<String, Object> out = helper.executeDataFlow("smartcampus.service.unitnnews", "GetOperaNews", new GetOperaNewsDataFlow(), emptyPars);
//			Map<String, Object> out = helper.executeDataFlow("smartcampus.service.unitnnews", "GetUnitnNews", new GetUnitnNewsDataFlow(), pars);
//			Map<String, Object> out = helper.executeDataFlow("smartcampus.service.unitnnews", "GetCiscaNews", new GetCiscaNewsDataFlow(), emptyPars);
			for (NewsEntry news : (List<NewsEntry>)out.get("data")) {
				System.out.println(news.getTitle());
				System.out.println(news.getContent());
				System.out.println("----");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
