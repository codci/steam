package webdriver.formverify;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import webdriver.BaseTestParam;
import webdriver.Browser;

import java.io.File;

public class FormVerifyMain extends BaseTestParam {

	@Test
	@Parameters()
	public final void readParams() throws Throwable {
		xTest();
	}

	@Override
	@Parameters()
	public void runTest() {
		try {
			start().join();
		} catch (InterruptedException e) {
			logger.debug(this, e);
		}
	}

	public Thread start() {
		final FormVerifyJFrame frame = new FormVerifyJFrame();
		final Thread thread = new Thread() {
			@Override
			public void run() {
				File classdir = setup();
				String comand;
				String timeoutForCondition = Browser.getTimeoutForCondition();
				Browser.getInstance().setTimeoutForCondition("0");
				while (true) {
					comand = frame.readLine();
					File root = new File(classdir.getAbsolutePath());
					Browser.useCommonDriver(true);
					if (comand.startsWith("save:")) {
						try{
							String dirName = "."+File.separator+"Pages"+File.separator+comand.substring(5); 
							new Saver(dirName,frame).savePages();
						}catch(Exception t){
							logger.debug(this, t);
						}
						continue;
					} else if (comand.startsWith("form:")) {
						new ClassVerifyThread(root).checkForm(comand.substring(5), frame);
						continue;
					} else if (comand.startsWith("method:")) {
						new ClassVerifyThread(root).callMethod(comand.substring(7), frame);
						continue;
					} else if (!comand.isEmpty()) {
						root = new File(classdir.getAbsolutePath()
								+ File.separator
								+ comand.replace(".", File.separator));
					}
					ClassVerifyThread verifyThread = new ClassVerifyThread(root);
					verifyThread.start();
					try {
						verifyThread.join();
					} catch (InterruptedException e) {
						logger.debug(this, e);
					}
					Browser.useCommonDriver(false);
					verifyThread.printNames(frame);
				}
				/*Browser.setTimeoutForCondition(timeoutForCondition);
				Browser.setWaitDialogTimeout(getWaitDialogTimeout);*/
			}

			private File setup() {
				System.out.println("Current dir:"
						+ new File(".").getAbsolutePath());
				File classdir = new File("target" + File.separator
						+ "test-classes");
				if (classdir.exists()) {
					ClassVerifyThread.setClassPath(classdir.getAbsolutePath());
				} else if (new File("test-classes").exists()) {
					classdir = new File("test-classes");
					ClassVerifyThread.setClassPath(classdir.getAbsolutePath());
				} else if (new File("bin").exists()) {
					classdir = new File("bin");
					ClassVerifyThread.setClassPath(classdir.getAbsolutePath());
				} else {
					do {
						frame.println("Enter class directory:");
						String comand = frame.readLine();
						if (classdir.exists()) {
							classdir = new File(comand);
							ClassVerifyThread.setClassPath(classdir
									.getAbsolutePath());
							break;
						}
						frame.println("Class directory wasn't found!");
					} while (true);
				}
				frame.println("classpath:" + classdir.getAbsolutePath());
				return classdir;
			}
		};
		thread.start();
		return thread;
	}

	@Override
	public boolean shouldAnalys() {
		return false;
	}

	@Override
	public void invokeAnalys(Throwable exc, String bodyText) {
        // not necessary yet
	}
}
