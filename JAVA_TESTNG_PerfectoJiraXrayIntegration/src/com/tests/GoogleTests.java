package com.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.pages.GooglePage;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;

public class GoogleTests {
	 RemoteWebDriver driver;
	    ReportiumClient reportiumClient;

	    //TODO: Set your Perfecto Lab user, password and host.
	    String PERFECTO_HOST        = System.getProperty("np.testHost", "MY_HOST.perfectomobile.com");
	    String PERFECTO_TOKEN       = System.getProperty("np.testToken", "SECURITY_TOKEN");

	    //TODO: Insert your device capabilities at testng.XML file.
	    @Parameters({"platformName" , "model" , "browserName"})
	    @BeforeTest
	    public void beforMethod(String platformName, String model, String browserName) throws MalformedURLException {

	        DesiredCapabilities capabilities = new DesiredCapabilities();
	        capabilities.setCapability("securityToken", PERFECTO_TOKEN);

	        //Old School Credentials Login
	        //capabilities.setCapability("user" , PERFECTO_USER);
	        //capabilities.setCapability("password" , PERFECTO_PASSWORD);

	        capabilities.setCapability("platformName" , platformName);
	        capabilities.setCapability("model" , model);
	        capabilities.setCapability("browserName" , browserName);

	        driver = new RemoteWebDriver(new URL("https://" + PERFECTO_HOST + "/nexperience/perfectomobile/wd/hub") , capabilities);
	        driver.manage().timeouts().implicitlyWait(15 , TimeUnit.SECONDS);
	        
	        //Create Reportium client.
	        reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(
	                        new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
	                        .withProject(new Project("Sample Selenium-Reportium" , "1.0"))
	                        .withContextTags("Regression") //Optional
	                        .withWebDriver(driver) //Optional
	                        .build());
	    }

	@Test(groups = { "@PERFECTO-1" })
	public void googleSearch() {
		GooglePage gp = new GooglePage();

		gp.launchURL(driver, "http://www.google.com");
		gp.search(driver, "Quantum Perfecto");
		gp.verifySearchResults(driver, "Introducing Quantum Framework");
	}
	@Test(groups = { "@PERFECTO-2" })
	public void googleSearchResultLink() {
		GooglePage gp = new GooglePage();

		gp.launchURL(driver, "http://www.google.com");
		gp.search(driver, "Quantum Perfecto");
		gp.verifySearchResults(driver, "Introducing Quantum Framework");
		gp.selectFirstResultLink(driver);
		gp.verifyResultLink(driver, "Quantum Perfecto");
	}

	@SuppressWarnings("Since15")
    @AfterTest
    public void afterMethod(){
        try{
            driver.manage().deleteAllCookies(); //Removes cookies after test.
            driver.quit();
            String reportURL = reportiumClient.getReportUrl();
            System.out.println(reportURL); //Print URL to console

            //TODO: Enable this couple of lines in order to open the browser with the report at the end of the test.
            //if(Desktop.isDesktopSupported())
              //  Desktop.getDesktop().browse(new URI(reportURL));

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
