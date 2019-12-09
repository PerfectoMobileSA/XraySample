package com.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

public class GooglePage  {

	public void launchURL(RemoteWebDriver driver, String url) {
		driver.get(url);
	}

	public void search(RemoteWebDriver driver, String searchKey) {
		WebElement searchBoxElement = driver.findElement(By.name("q"));
		WebElement searchBtnElement = driver.findElement(By.xpath("//input[@aria-label='Google Search']|//button[@aria-label='Google Search']"));
		searchBoxElement.clear();
		searchBoxElement.sendKeys(searchKey);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", searchBtnElement);
	}

	public void verifySearchResults(RemoteWebDriver driver, String result) {
		WebElement searchBoxElement = driver.findElement(By.partialLinkText(result));
		Assert.assertEquals(verifyDisplayed(searchBoxElement), true);
	}
	
	
	
	public boolean verifyDisplayed(WebElement element) {
		try {
			element.isDisplayed();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void selectFirstResultLink(RemoteWebDriver driver) {
		WebElement searchResultLink = driver.findElement(By.className("S3Uucc"));
		searchResultLink.click();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void verifyResultLink(RemoteWebDriver driver, String searchString) {
		Assert.assertEquals(driver.getTitle().contains(searchString.split(" ")[0]), true);
	}


}
