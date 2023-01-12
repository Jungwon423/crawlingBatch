package com.example.demo;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

@SpringBootApplication
public class DemoApplication {
	private final Logger logger = LoggerFactory.getLogger("크롤링 로그");
	private WebDriver driver;

	//Properties 설정
	public static String WEB_DRIVER_ID = "webdriver.chrome.driver";
	public static String WEB_DRIVER_PATH = "C:/chromedriver.exe";
	public static String TARGET_URL = "https://ko.aliexpress.com/campaign/wow/gcp/ae/channel/ae/accelerate/tupr?spm=a2g0o.home.countrygrid.1.472b4430H8ED7a&wh_weex=true&_immersiveMode=true&wx_navbar_hidden=true&wx_navbar_transparent=true&ignoreNavigationBar=true&wx_statusbar_hidden=true&wh_pid=ae%2Fchannel%2Fae%2Fkr_plaza%2FKRfastshipping&productIds=%252C%252C%252C%252C%252C%252C%252C%252C%252C%252C";

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args).getBean(DemoApplication.class).getProductDetails();
	}

	public void getProductLinks() {
		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		options.addArguments("--disable-popup-blocking");       //팝업안띄움
//		options.addArguments("headless");                       //브라우저 안띄움
		options.addArguments("__lang:euc-kr");
		driver = new ChromeDriver(options);
		String baseURL = "https://ko.aliexpress.com/item/";
		int len = baseURL.length();
		Set<String> links = new HashSet<>();
		try {
			driver.get(TARGET_URL);
			var crawlingTime = new Date().getTime();
			while (new Date().getTime() < crawlingTime + 60000) { // 30000 = 30000 millisecond = 30 sec
				logger.info("while문 루프 도는 중");
				((JavascriptExecutor)driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
			}
			Thread.sleep(4000);
			List<WebElement> elements = driver.findElements(By.tagName("a"));
			logger.info("현재 읽어온 a 태그 수 : "+ elements.size());
			for (WebElement element : elements) {
				String link = element.getAttribute("href");
				if (link.length() < len || !(link.substring(0, len).equals(baseURL))) continue; // href가 없으면 continue
				links.add(link);
			}
			logger.info("현재 읽어온 product link 개수 : "+ links.size());
			logger.info("크롤링 종료");
		}
		catch (Exception e){
			logger.error("에러 발생! : " + e);
		}
		logger.info("총 읽어온 product link 개수 : "+ links.size());
	}

	public void getProductDetails() {

		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

		//Driver SetUp
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		options.addArguments("--disable-popup-blocking");       //팝업안띄움
		//options.addArguments("headless");                       //브라우저 안띄움
		options.addArguments("__lang:euc-kr");
		driver = new ChromeDriver(options);
		String baseURL = "https://ko.aliexpress.com/item/";

		List<String> links = new ArrayList<>();
		int len = baseURL.length();
		try {
			driver.get(TARGET_URL);
			Thread.sleep(1000);
			List<WebElement> elements = driver.findElements(By.tagName("a"));
			logger.info(String.valueOf(elements.size()));
			for (WebElement element : elements){
				String link = element.getAttribute("href");
				if (link.length()<len || !(link.substring(0,len).equals(baseURL))) continue;
				links.add(link);
			}
			logger.info("product link 크롤링 완료");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("총 읽어온 product link 개수 : "+ links.size());
		for (String link : links){
			try {
				Product product = new Product();
				product.setLink(link);
				product.setMarketName("AliExpress");
				product.setCurrency("KRW");
				product.setLocale("kr");
				// TODO product.setTax() & product.setShippingFee()
				driver.get(link);
				Thread.sleep(1000);

				// name 크롤링
				WebElement productName = driver.findElement(By.className("product-title-text"));
				product.setName(productName.getText());

				int discountRate;
				String price = "default";
				try { // price, discountRate 크롤링
					WebElement element = driver.findElement(By.className("uniform-banner-box-discounts"));
					List<WebElement> spans = element.findElements(By.tagName("span"));
					price = ToPrice(spans.get(0).getText());
					logger.info(price);
					discountRate = Integer.parseInt(spans.get(1).getText().replaceAll("[^0-9]", ""));
					logger.info(String.valueOf(discountRate));
				}
				catch(Exception e){
					WebElement element = driver.findElement(By.className("product-price-original"));
					List <WebElement> elements = element.findElements(By.tagName("span"));
					price = ToPrice(elements.get(0).getText());
					logger.info(price);
					discountRate = Integer.parseInt(elements.get(1).getText().replaceAll("[^0-9]", ""));
					logger.info(String.valueOf(discountRate));
				}

				try{ // imageUrl 크롤링
					WebElement element = driver.findElement(By.className("video-container"));
					element = element.findElement(By.tagName("img"));
					logger.info(element.getAttribute("src"));
					product.setImageUrl(element.getAttribute("src"));
				}
				catch(Exception e){
					WebElement element = driver.findElement(By.className("image-view-magnifier-wrap"));
					element = element.findElement(By.tagName("img"));
					logger.info(element.getAttribute("src"));
					product.setImageUrl(element.getAttribute("src"));
				}

				// categoryName 크롤링
				WebElement element = driver.findElement(By.className("buy-now-wrap"));
				((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", element);
				Thread.sleep(100);
				WebElement category = driver.findElement(By.className("parent-title"));
				logger.info(category.getText());
				product.setCategoryName(category.getText());

				try { // productDescription 크롤링
					WebElement description = driver.findElement(By.className("product-description"));
					logger.info("productDescription : " +  description.getAttribute("innerHTML"));
				}
				catch(Exception e){
					WebElement description = driver.findElement(By.className("product-overview"));
					logger.info("productDescription : "+ description.getAttribute("innerHTML"));
				}

				if (price.equals("default")) continue;
				product.setPrice(Double.valueOf(price));
				product.setDiscountRate((double) discountRate);
				logger.info("-------------------- 크롤링 결과 --------------------");
				logger.info(product.toString());
				logger.info("-------------------- 크롤링 결과 --------------------");
//
			}
			catch(Exception e){
				e.printStackTrace();
			}

		}
		driver.close();
		driver.quit();
	}

	public String ToPrice(String price){
		String[] Arr = price.split(" ");
		if (Arr.length>2){
			return "NOPE";
		}
		return price.replaceAll("[^0-9]", "");
	}


}
