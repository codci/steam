package webdriver.formverify;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import webdriver.Browser;
import webdriver.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Saver {
	static final boolean useId = true;
	static final Logger logger = Logger.getInstance();
	enum XPathTypes {
		NoNumber, ClassAndName_Or_Number, Full
	}

	String dirName;
	int step = 0;
	FormVerifyJFrame frame;

	public Saver(String dirName, FormVerifyJFrame frame) {
		this.dirName = dirName;
		new File(dirName).mkdirs();
		this.frame = frame;
	}

	public void savePages() {
		try {
			new KeyHook(this).start();
			frame.println("Save process started");
			frame.println("Press 'e' to save current element");
			frame.println("Press 'w' to save current window");
			frame.println("Press 'q' to qiut");
		} catch (Exception e) {
			logger.debug(this, e);
		}
	}

	void savePage() {
		try {
			String fileName = dirName + File.separator + step + ".html";
			PrintStream out = new PrintStream(new FileOutputStream(fileName),
					true, "UTF-8");
			out.print(Browser.getInstance().getDriver().getPageSource());
			out.close();
			frame.println("Saved:" + fileName);
			step++;
		} catch (Exception e) {
			logger.debug(this, e);
		}
	}

	public void saveElement() {
		JavascriptExecutor js = (JavascriptExecutor) Browser.getDriver();
		Point location = MouseInfo.getPointerInfo().getLocation();
		org.openqa.selenium.Point browserPos = Browser.getDriver().manage()
				.window().getPosition();
		WebElement body = Browser.getDriver().findElement(By.xpath(".//body"));
		int x = location.x - browserPos.x - body.getLocation().x, y = location.y
				- browserPos.y - body.getLocation().y;
		frame.println("point x:" + x + " y:" + y);
		WebElement el = (WebElement) js.executeScript(
				"return document.elementFromPoint(arguments[0], arguments[1])",
				x, y);
		js.executeScript("arguments[0].style.border='3px solid red'", el);
		String res = genXPath(js, el, XPathTypes.NoNumber, true);
		if (!check(res)) {
			String res2 = genXPath(js, el, XPathTypes.ClassAndName_Or_Number,
					true);
			if (check(res2)) {
				frame.println(optimize(res2).replace("[slh]","/"));
				return;
			}
			res2 = genXPath(js, el, XPathTypes.Full, true);
			if (check(res2)) {
				frame.println(optimize(res2).replace("[slh]","/"));
				return;
			}
			frame.println("not valid: " + res.replace("[slh]","/"));
			return;
		}
		frame.println(optimize(res).replace("[slh]","/"));
	}

	private String genXPath(JavascriptExecutor js, WebElement el,
			XPathTypes type, boolean text) {
		StringBuffer sb = new StringBuffer();
		sb.append("    	function getPathTo(element) {\n");
		if(useId){
			sb.append("    	    if (element.id!=='')\n");
			sb.append("    	        return 'id(\"'+element.id+'\")';\n");
		}
		sb.append("    	    if (element===document.body)\n");
		sb.append("    	        return element.tagName.toLowerCase();\n");
		sb.append("    	    var ix= 0;\n");
		sb.append("    	    var siblings= element.parentNode.childNodes;\n");
		sb.append("    	    for (var i= 0; i<siblings.length; i++) {\n");
		sb.append("    	        var sibling= siblings[i];\n");
		sb.append("    	        if (sibling===element){\n");
		sb.append("    	        var attr = ''");
		sb.append("+(element.hasAttribute('class')?'[contains(@class,\"'+element.getAttribute('class')+'\")]':'')");
		sb.append("+(element.hasAttribute('name')?'[contains(@name,\"'+element.getAttribute('name')+'\")]':'');\n");
		sb.append("    	            return getPathTo(element.parentNode)+'/'+element.tagName.toLowerCase()");
		if (type == XPathTypes.Full) {
			sb.append("+'['+(ix+1)+']'+attr");
		} else if (type == XPathTypes.ClassAndName_Or_Number) {
			sb.append("+(attr==''?'['+(ix+1)+']':attr)");
		} else {
			sb.append("+attr");
		}
		sb.append(";}\n");
		sb.append("    	        if (sibling.nodeType===1 && sibling.tagName===element.tagName)\n");
		sb.append("    	            ix++;\n");
		sb.append("    }}\n");
		sb.append("return getPathTo(arguments[0]);");
		String res = "";
		try {
			res = (String) js.executeScript(sb.toString(), el);
			if(!res.startsWith("id")){
				res = "//"+res;
			}
			if (text && el.findElements(By.xpath("./*")).isEmpty()
					&& !"".equals(el.getText())) {
				res = res
						+ "[contains(.,'" + el.getText().replace("/", "[slh]") + "')]";
			}
		} catch (Exception e) {
			logger.debug(this, e);
			frame.println("error");
			return "";
		}
		return res;
	}

	private boolean check(String res) {
		List<WebElement> list = Browser.getDriver().findElementsByXPath(res.replace("[slh]","/"));
		if (list.isEmpty()) {
			return false;
		}
		if (list.size() > 1) {
			return false;
		}
		return true;
	}

	private String optimize(String res) {
		try {
			LinkedList<String> list = new LinkedList<String>(Arrays.asList(res
					.split("/")));
			String symple = "//" + list.getLast();
			if (check(symple)) {
				return symple;
			}
			int index = 1;
			while (index < list.size() - 1) {
				String parent = "";
				String tmp = list.get(index);
				list.remove(index);
				parent = list.get(index - 1);
				if (!parent.endsWith("/")) {
					list.set(index - 1, parent + "/");
				}
				if (!check(join(list))) {
					list.add(index, tmp);
					list.set(index - 1, parent);
					index++;
				}
			}
			return join(list);
		} catch (Exception e) {
			logger.debug(this, e);
			return res;
		}
	}

	private String join(List<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String string : list) {
			sb.append(string).append("/");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
