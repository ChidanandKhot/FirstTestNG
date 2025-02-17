/**
 * 
 */
package pom.com.vilcart.pom.goods;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Reporter;

import util.com.vilcart.util.AngularWait;
import util.com.vilcart.util.CurrentMethod;
import util.com.vilcart.util.TimeStamp;

/**
 * @author win10
 *
 */
public class GoodsTransfer {
	private WebDriver driver;
	private AngularWait aw;
	private JavascriptExecutor js;
	private GoodsTransferCreation gtc;

	@FindBy(xpath = "//*[@id=\"demo-2\"]/input")
	private WebElement searchInput;

	@FindBy(id = "createGoodsTransfer")
	private WebElement createGoodsTransfer;

	@FindBy(xpath = "//*[@id=\"startDate\"]")
	private WebElement startDate;

	@FindBy(xpath = "//*[@id=\"endDate\"]")
	private WebElement endDate;

	@FindBy(xpath = "//button[normalize-space()='Submit']")
	private WebElement submit;

	@FindBy(id = "transferListTuple")
	private List<WebElement> transferListTuples;

	@FindBy(id = "orderListTuple")
	private List<WebElement> orderListTuples;

	public GoodsTransfer(WebDriver driver, AngularWait aw) {
		this.driver = driver;
		this.aw = aw;
		this.js = ((JavascriptExecutor) this.driver);
		PageFactory.initElements(driver, this);
		gtc = new GoodsTransferCreation(driver, aw);
	}

	public void searchInGoodsTransfer(String key) {
		Reporter.log("==>" + CurrentMethod.methodName() + " " + TimeStamp.CurTimeStamp(), true);
		searchInput.clear();
		searchInput.sendKeys(key);
		searchInput.sendKeys(Keys.ENTER);
		aw.waitAllRequest();
	}

	private void getTuplesForCurrentDate() {
		Reporter.log("==>" + CurrentMethod.methodName() + " " + TimeStamp.CurTimeStamp(), true);

		js.executeScript("arguments[0].value = '" + getDate() + "';arguments[0].dispatchEvent(new Event('input'))",
				startDate);

		js.executeScript("arguments[0].value = '" + getDate() + "';arguments[0].dispatchEvent(new Event('input'))",
				endDate);
		submit.click();
		aw.waitAllRequest();
	}

	private String getDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime indiaDateTime = now.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
		return dtf.format(indiaDateTime);
	}

	public void transferGoods(String dc, String vehicle) {
		Reporter.log("==>" + CurrentMethod.methodName() + " " + TimeStamp.CurTimeStamp(), true);
		createGoodsTransfer.click();
		vehicle = gtc.createGoodsTransfer(dc, vehicle);
		getTuplesForCurrentDate();
		searchInGoodsTransfer(vehicle);
		// ToDo verifying with vehicle number should be a challan no which has to pop up in
		// create Goods Transfer.
		for (int i = 0; i < transferListTuples.size(); i++) {
			if (transferListTuples.get(i).findElement(By.xpath(".//td[4]")).getText().equalsIgnoreCase(vehicle)) {
				transferListTuples.get(i).findElement(By.xpath(".//td[11]/div/button[1]")).click();
				aw.waitAllRequest();
				//ToDo check if all order Items are present in the Goods Transfer.
				for (int j = 0; j < gtc.itemsCount; j++) {
					assertThat(orderListTuples.get(j).findElement(By.xpath(".//td[2]")).getText())
							.withFailMessage("Doesn't Tally SKUName " + gtc.data[j][0])
							.isEqualToIgnoringCase(gtc.data[j][0]);
					assertThat(orderListTuples.get(j).findElement(By.xpath(".//td[5]")).getText())
							.withFailMessage("Doesn't Tally SKU Vaiation Name " + gtc.data[j][1])
							.isEqualToIgnoringCase(gtc.data[j][1]);
					assertThat(orderListTuples.get(j).findElement(By.xpath(".//td[4]")).getText())
							.withFailMessage("Doesn't Tally count " + gtc.data[j][2])
							.isEqualToIgnoringCase(gtc.data[j][2]);
					//TODO tally unit price/ GRN Price and Total price.
//					assertThat(orderListTuples.get(j).findElement(By.xpath(".//td[6]")).getText())
//							.withFailMessage("Doesn't Tally receive price " + gtc.data[j][3])
//							.isEqualToIgnoringCase(gtc.data[j][3]);
//					assertThat(Integer.parseInt(
//							orderListTuples.get(j).findElement(By.xpath(".//td[7]")).getText()))
//							.withFailMessage("Doesn't Tally total amount " + gtc.data[j][3])
//							.isEqualTo(Integer.parseInt(gtc.data[j][2])
//									* Integer.parseInt(gtc.data[j][3]));

				}
				break;
			}
			if (i == transferListTuples.size() - 1) {
				assertThat(false).withFailMessage("No tuple with vehicle Number " + vehicle).isEqualTo(true);
			}
		}
	}
}
