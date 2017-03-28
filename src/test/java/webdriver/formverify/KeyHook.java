package webdriver.formverify;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import webdriver.Logger;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KeyHook extends Thread{
	private static HHOOK hhk;
	private static LowLevelKeyboardProc keyboardHook;
	private static final Logger logger = Logger.getInstance();

	Saver saver;
	public KeyHook(Saver saver) {
		this.saver = saver;
	}

	@Override
	public void run() {
		final User32 lib = User32.INSTANCE;
		HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
		keyboardHook = new LowLevelKeyboardProc() {
			public LRESULT callback(int nCode, WPARAM wParam,
					KBDLLHOOKSTRUCT info) {
				if (nCode >= 0) {
					switch (wParam.intValue()) {
					case WinUser.WM_KEYUP:
					case WinUser.WM_SYSKEYUP:
						if ("E".equals(KeyEvent
								.getKeyText(info.vkCode))) {
							saver.saveElement();
						} else if ("W".equals(KeyEvent
								.getKeyText(info.vkCode))) {
							saver.savePage();
						} else if ("Q".equals(KeyEvent
								.getKeyText(info.vkCode))) {
							lib.UnhookWindowsHookEx(hhk);
							saver.frame.println("Save process stopped!");
						}
						break;
					}
				}
				return lib.CallNextHookEx(hhk, nCode, wParam,
						info.getPointer());
			}

		};
		hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL,
				keyboardHook, hMod, 0);

		new Thread() {
			public void run() {
				String in;
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(System.in));
				try {
					do {
						in = bufferedReader.readLine();
					} while (!"quit".equals(in));
				} catch (IOException e) {
					logger.debug(this, e);
				}
			}
		}.start();

		// This bit never returns from GetMessage
		int result;
		MSG msg = new MSG();
		while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
			if (result == -1) {
				logger.error("error in get message");
				break;
			} else {
                logger.error("got message");
				lib.TranslateMessage(msg);
				lib.DispatchMessage(msg);
			}
		}
		lib.UnhookWindowsHookEx(hhk);
	}
}
