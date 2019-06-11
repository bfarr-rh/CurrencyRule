package com.redhat.currencyvalidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
/*import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.drools.compiler.kproject.ReleaseIdImpl;*/
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import com.myspace.currencyrules.CurrencyData;
import com.myspace.currencyrules.CurrentCheckResult;


public class RulesBean {
	static Map<String, String> currencyMap = new HashMap<String, String>(500);
	static String FORMAT = "{ \"TransId\": \"%s\":, \"Currency\" : \"%s\", \"Amount\" : %d }";
	
	final static String G = "com.myspace";
    final static String A = "currencyRules";
    final static String V = "1.0.0-SNAPSHOT";
    static final String drlFile = "rules.drl";

/*    
 * The following are meant fo KieScaner
 */
    /*
    private KieServices kieServices;
    private ReleaseId releaseId;
    private KieContainer kContainer;
    private KieScanner kScanner;
	
    
    public RulesBean() {
    	kieServices = KieServices.Factory.get();
    	releaseId = kieServices.newReleaseId( G, A, V );
    	kContainer = kieServices.newKieContainer( releaseId );
    	kScanner = kieServices.newKieScanner( kContainer );

    	// Start the KieScanner polling the Maven repository every 10 seconds
    	kScanner.start( 10000L );
    	System.out.println("KieScanner Initialised...");
    }*/
    
    KieFileSystem kFileSystem;
    KieBuilder kBuilder;
    KieModule kModule;
    KieContainer kContainer;
    
    public RulesBean() {
        KieServices kieServices = KieServices.Factory.get();
 
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource(drlFile));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
 
        kContainer = kieServices.newKieContainer(kieModule.getReleaseId());
    }

    
	public void setCurrencyMap(String content) {
		JSONArray array = new JSONArray(content);
		Map<String, String> map = new HashMap<String, String>(500);
		for (int i = 0; i < array.length(); i++) {  
		     JSONObject obj = array.getJSONObject(i);

		     map.put( obj.get("AlphabeticCode").toString(), obj.get("Currency").toString());

		}
		for (Entry<String, String> entry : currencyMap.entrySet()) {
		    System.out.println(entry.getKey() + ", " + entry.getValue());
		}
		System.out.println("No. of Entries: " + currencyMap.size());
		currencyMap = map;
	}
	
	public String validateCurrency(String content) {
//		System.out.println(content);
		JSONObject json = new JSONObject(content);
		CurrencyData data = new CurrencyData(json.get("TransId").toString(), json.get("Currency").toString());
		CurrentCheckResult result = new CurrentCheckResult(true);
		
        KieSession kieSession = kContainer.newKieSession();
        kieSession.setGlobal("currencyMap", currencyMap);
        kieSession.setGlobal("result", result);
        kieSession.insert(data);
        kieSession.fireAllRules();
        kieSession.dispose();

		return String.format("{ \"valid\" : %d, \"currency\" : \"%s\" }", ((result.isValid())? 1: 0), data.getCurrency());
	}

	public String convertToJson(String data) {
		StringTokenizer st = new StringTokenizer(data.replaceAll("=", ":").replaceAll(" ", ""), "{:,}");
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		int i = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
//	        System.out.println(token); 
	        sb.append('"').append(token).append('"');
			if (i % 2 == 0) {
				sb.append(" : ");
			}
			else {
				if (i > 0) sb.append(", ");
			}
			i++;
	     }  
		sb.append('}');
		return sb.toString().replaceAll(", }", "}");
	}
	
    public static void main(String[] args) {
    	RulesBean bean = new RulesBean();
    	System.out.println(bean.convertToJson("{TransId=X00006, Currency=XYZ, Amount=600}"));
    }
}
