package com.quantum.listeners;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Required Parameters - Global Switch Key - Enable/ disable Create new Jira
 * tasks for failures Switch Key - Enable/Disable Jira Host URL - Eg:
 * https://kulin.atlassian.net Project Key - Eg: TEST Jira Authentication
 * Username & Password XRay Plugin - Client ID & Client Secret Test Execution
 * Key - Eg: TEST-1 Test Key - Eg: Test-4 (This will be configured in the
 * cucumber scenario tags.)
 * 
 * @author kulin sitwala
 *
 */
public class JiraListener implements ITestListener {

	public static final String RESOURCESLOC = "src/main/resources/data/";
	@Override
	public void onTestStart(ITestResult result) {

	}

	@Override
	public void onTestSuccess(ITestResult result) {
		String globalJiraSwitchKey = result.getMethod().getXmlTest().getParameter("globalJiraSwitchKey");
		String projectKey = result.getMethod().getXmlTest().getParameter("projectKey");
		String xrayClientID = result.getMethod().getXmlTest().getParameter("xrayClientID");
		String xrayClientSecret = result.getMethod().getXmlTest().getParameter("xrayClientSecret");
		String xrayHost = result.getMethod().getXmlTest().getParameter("xrayHost");
		String testExecutionKey = result.getMethod().getXmlTest().getParameter("testExecutionKey");
		
		String testKey = "";
		String[] tags = result.getMethod().getGroups();
		for(String tag : tags) {
			if(tag.contains(projectKey + "-") ) {
				testKey = tag.replace("@", "");
			}
		}
		System.out.println("Test Key is - " + testKey);
		
		if (globalJiraSwitchKey.equalsIgnoreCase("true") && !testKey.isEmpty()) {
			String xRayAuthToken = getXRAYAuthorization(xrayHost, xrayClientID, xrayClientSecret);
			postXRayExDetails(xrayHost, xRayAuthToken, testExecutionKey, true, testKey, null, null);
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String scenarioName = result.getMethod().getMethodName();
		String reportURL = QuantumReportiumListener.getReportClient().getReportUrl();
		
		
		String globalJiraSwitchKey = result.getMethod().getXmlTest().getParameter("globalJiraSwitchKey");
		String projectKey = result.getMethod().getXmlTest().getParameter("projectKey");
		String xrayClientID = result.getMethod().getXmlTest().getParameter("xrayClientID");
		String xrayClientSecret = result.getMethod().getXmlTest().getParameter("xrayClientSecret");
		String xrayHost = result.getMethod().getXmlTest().getParameter("xrayHost");
		String testExecutionKey = result.getMethod().getXmlTest().getParameter("testExecutionKey");
		String createNewJiraTasks = result.getMethod().getXmlTest().getParameter("createNewJiraTasks");
		String jiraHostURL = result.getMethod().getXmlTest().getParameter("jiraHostURL");
		String jiraUser = result.getMethod().getXmlTest().getParameter("jiraUser");
		String jiraPassword = result.getMethod().getXmlTest().getParameter("jiraPassword");
		
		String testKey = "";
		String[] tags = result.getMethod().getGroups();
		for(String tag : tags) {
			if(tag.contains(projectKey + "-") ) {
				testKey = tag.replace("@", "");
			}
		}
		System.out.println("Test Key is - " + testKey);
		
		if (globalJiraSwitchKey.equalsIgnoreCase("true") && !testKey.isEmpty()) {
			String xRayAuthToken = getXRAYAuthorization(xrayHost, xrayClientID, xrayClientSecret);
			
			String encodedStringEvidence = "";
			
			
			postXRayExDetails(xrayHost, xRayAuthToken, testExecutionKey, false, testKey, encodedStringEvidence , "failureScreen" + scenarioName);
			if (createNewJiraTasks.equalsIgnoreCase("true")) {
				
				String failureException = "";
				if (result.getThrowable() == null) {
					failureException = "No exception stacktrace";
				} else {
					failureException = ExceptionUtils.getStackTrace(result.getThrowable());
				}

				String taskDesc = "Please check the failed test case - " + scenarioName + " in the ReportURl - "
						+ reportURL + "\n This test case failed with exception - " + failureException;

				createJiraTask(jiraHostURL, jiraUser, jiraPassword, projectKey, taskDesc);
			}
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {

	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

	}

	@Override
	public void onStart(ITestContext context) {

	}

	@Override
	public void onFinish(ITestContext context) {

	}

	private static final int TIMEOUT_MILLIS = 60000;

	public static String makeAPICall(String hostURL, String endPoint, Map<String, String> headers, String body,
			String basicAuthUser, String basicAuthPass) {
		URIBuilder taskUriBuilder;
		try {
			taskUriBuilder = new URIBuilder(hostURL + endPoint);

			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			if (basicAuthUser != null) {
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(basicAuthUser, basicAuthPass);
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			}
			System.out.println("BODY before setting the entity is - " + body);
			StringEntity requestEntity = new StringEntity(body, ContentType.APPLICATION_JSON);

			HttpPost httpPost = new HttpPost(taskUriBuilder.build());
			addRequestHeaders(httpPost, headers);
			httpPost.setEntity(requestEntity);

			HttpResponse response = null;
			HttpClient httpClient = HttpClientBuilder.create()
					.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
					.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(TIMEOUT_MILLIS)
							.setConnectTimeout(TIMEOUT_MILLIS).setConnectionRequestTimeout(TIMEOUT_MILLIS).build())
					.setDefaultCredentialsProvider(credentialsProvider).build();
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			System.out.println("Response of URL - " + hostURL + endPoint + " is response - " + responseString);
			return responseString;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void addRequestHeaders(HttpRequestBase request, Map<String, String> headers) {
		for (String key : headers.keySet()) {
			request.addHeader(key, headers.get(key));
		}
	}

	@SuppressWarnings("unused")
	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


	private static String getXRAYAuthorization(String xrayHost, String xrayClientID, String xrayClientSecret) {
		Map<String, String> xrayHeader = new HashMap<String, String>();
		xrayHeader.put("Content-Type", "application/json");

		String xrayAuthBody = "{ \"client_id\": \"$clientID\",\"client_secret\": \"$clientSecret\" }"
				.replace("$clientID", xrayClientID).replace("$clientSecret", xrayClientSecret);
		// System.out.println(xrayAuthBody);
		String appListResponse = makeAPICall(xrayHost, "/api/v1/authenticate", xrayHeader, xrayAuthBody, null, null);

		System.out.println(appListResponse);

		String xRayAuthToken = appListResponse.replace("\"", "");
		System.out.println(xRayAuthToken);
		return xRayAuthToken;
	}

	@SuppressWarnings({  "deprecation" })
	private static String postXRayExDetails(String xrayHost, String xRayAuthToken, String testExecutionKey, boolean passFailStatus, String testKey, String evidence, String fileName) {
		String updateTestRunResp = "";
		Map<String, String> xrayHeaderBearer = new HashMap<String, String>();
		xrayHeaderBearer.put("Content-Type", "application/json");
		xrayHeaderBearer.put("Authorization", "Bearer " + xRayAuthToken);

		JSONParser parser = new JSONParser();

		Object obj, passObj, failObj;
		try {
			obj = parser.parse(new FileReader(RESOURCESLOC + "xrayTestExUpdate.json"));
			JSONObject jsonObject = (JSONObject) obj;
			
			if(passFailStatus) {
				passObj = parser.parse(new FileReader(RESOURCESLOC + "xrayTestPass.json"));
				JSONObject passJsonObject = (JSONObject) passObj;
				passJsonObject.put("testKey", testKey);
				((JSONArray)jsonObject.get("tests")).add(passJsonObject);
			} else { 
				failObj = parser.parse(new FileReader(RESOURCESLOC + "xrayTestFail.json"));
				JSONObject failJsonObject = (JSONObject) failObj;
				failJsonObject.put("testKey", testKey);
				((JSONArray)jsonObject.get("tests")).add(failJsonObject);
			}
			jsonObject.put("testExecutionKey", testExecutionKey);
			// Code to add body as JSON object

			System.out.println("JSON BODY - " + jsonObject.toString());
			updateTestRunResp = makeAPICall(xrayHost, "/api/v1/import/execution", xrayHeaderBearer,
					jsonObject.toString(), null, null);
			System.out.println("Finished with updating the execution status");
			System.out.println(updateTestRunResp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateTestRunResp;
	}

	@SuppressWarnings({ "deprecation" })
	private static String createJiraTask(String jiraHostURL, String basicAuthUser, String basicAuthPass, String projectKey,
			String taskDesc) {
		String jiraTaskCreateResp = "";
		Map<String, String> jiraHeader = new HashMap<String, String>();
		jiraHeader.put("Content-Type", "application/json");

		String auth = basicAuthUser + ":" + basicAuthPass;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		String authHeader = "Basic " + new String(encodedAuth);
		jiraHeader.put(HttpHeaders.AUTHORIZATION, authHeader);

		// System.out.println(xrayAuthBody);

		JSONParser parser = new JSONParser();

		Object obj;
		try {
			obj = parser.parse(new FileReader(RESOURCESLOC + "jiraNewTask.json"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject fieldsObj = ((JSONObject) jsonObject.get("fields"));
			((JSONObject)fieldsObj.get("project")).put("key", projectKey);
			fieldsObj.put("description", taskDesc);
			
			// Code to add body as JSON object

			System.out.println("JSON BODY - " + jsonObject.toString());
			jiraTaskCreateResp = makeAPICall(jiraHostURL, "/rest/api/2/issue/", jiraHeader, jsonObject.toString(), null,
					null);
			System.out.println("Finished with updating the execution status");
			System.out.println(jiraTaskCreateResp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jiraTaskCreateResp;
	}
}
