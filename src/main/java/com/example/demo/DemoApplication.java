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
		SpringApplication.run(DemoApplication.class, args).getBean(DemoApplication.class).getProductLinks();
	}

	public void getProductLinks() {
		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		options.addArguments("--disable-popup-blocking");       //팝업안띄움
//		options.addArguments("headless");                       //브라우저 안띄움
		options.addArguments("__lang:euc-kr");
		driver = new ChromeDriver(options);
		String comp = "https://ko.aliexpress.com/item/";
		int len = comp.length();
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
				if (link.length() < len || !(link.substring(0, len).equals(comp))) continue; // href가 없으면 뒤로 넘어감
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

	public void test() {

		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

		//Driver SetUp
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		options.addArguments("--disable-popup-blocking");       //팝업안띄움
		//options.addArguments("headless");                       //브라우저 안띄움
		options.addArguments("__lang:euc-kr");
		driver = new ChromeDriver(options);
		String comp = "https://ko.aliexpress.com/item/";

		List<String> links = new ArrayList<>();
		ArrayList<Map<String, Object>> result = new ArrayList<>();
		int len = comp.length();
		try {
			driver.get(TARGET_URL);
			Thread.sleep(4000);
			List<WebElement> elements = driver.findElements(By.tagName("a"));
			System.out.println(elements.size());
			for (WebElement element : elements){
				String link = element.getAttribute("href");
				if (link.length()<len || !(link.substring(0,len).equals(comp))) continue;
				links.add(link);
			}
			System.out.println("finsih!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(links.size());
		for (String link : links){
			try {
				Map<String, Object> data = new HashMap<>();
				data.put("link", link);
				data.put("marketName", "AliExpress");
				data.put("locale", "kr");
				driver.get(link);
				Thread.sleep(1000);
				List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
				WebElement productName = driver.findElement(By.className("product-title-text"));
				System.out.println(link);
				System.out.println(productName.getText());
				data.put("name", productName.getText()); //
				int discountRate = -1;
				String price = "NOPE";
				try { // 가격 할인율
					WebElement element = driver.findElement(By.className("uniform-banner-box-discounts"));
					List<WebElement> spans = element.findElements(By.tagName("span"));
					price = ToPrice(spans.get(0).getText());
					System.out.println(price);
					discountRate = Integer.parseInt(spans.get(1).getText().replaceAll("[^0-9]", ""));
					System.out.println(discountRate);
				}
				catch(Exception e){
					WebElement element = driver.findElement(By.className("product-price-original"));
					List <WebElement> elements = element.findElements(By.tagName("span"));
					price = ToPrice(elements.get(0).getText());
					System.out.println(price);
					discountRate = Integer.parseInt(elements.get(1).getText().replaceAll("[^0-9]", ""));
					System.out.println(discountRate);
				}
				try{ //상품 이미지
					WebElement element = driver.findElement(By.className("video-container"));
					element = element.findElement(By.tagName("img"));
					System.out.println(element.getAttribute("src"));
					data.put("imageUrl", element.getAttribute("src"));
				}
				catch(Exception e){
					WebElement element = driver.findElement(By.className("image-view-magnifier-wrap"));
					element = element.findElement(By.tagName("img"));
					System.out.println(element.getAttribute("src"));
					data.put("imageUrl", element.getAttribute("src"));
				}
				WebElement element = driver.findElement(By.className("buy-now-wrap"));
				((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", element);
				Thread.sleep(100);
				WebElement category = driver.findElement(By.className("parent-title"));
				System.out.println(category.getText());
				data.put("categoryName", category.getText());

				try {
					WebElement description = driver.findElement(By.className("product-description"));
					//System.out.println(description.getAttribute("innerHTML"));
					data.put("productDescription", description.getAttribute("innerHTML"));
				}
				catch(Exception e){
					WebElement description = driver.findElement(By.className("product-overview"));
					//System.out.println(description.getAttribute("innerHTML"));
					data.put("productDescription", description.getAttribute("innerHTML"));
				}
				if (price.equals("NOPE")) continue;
				data.put("price", Integer.parseInt(price));
				data.put("discountRate", discountRate);
				result.add(data);
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
