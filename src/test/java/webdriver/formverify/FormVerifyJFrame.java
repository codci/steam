package webdriver.formverify;

import webdriver.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FormVerifyJFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4295620929799554310L;
    private static final Logger logger = Logger.getInstance();
    private static String ENTER = "Enter";
	static JButton enterButton;
	static JButton clearButton;
	public static final JTextArea output = new JTextArea(15, 50);
	public static final JTextField input = new JTextField(20);
	static JPanel panel;
	String cmd;
	transient Object monitor;

	public FormVerifyJFrame() {
		super("Test");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setOpaque(true);
		ButtonListener buttonListener = new ButtonListener();
		output.setWrapStyleWord(true);
		output.setEditable(false);
		JScrollPane scroller = new JScrollPane(output);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel inputpanel = new JPanel();
		inputpanel.setLayout(new FlowLayout());
		enterButton = new JButton("Enter");
		enterButton.setActionCommand(ENTER);
		enterButton.addActionListener(buttonListener);

		clearButton = new JButton("Clear");
		clearButton.setActionCommand(ENTER);
		clearButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				output.setText("");
			}
		});

		input.setActionCommand(ENTER);
		input.addActionListener(buttonListener);
		panel.add(scroller, BorderLayout.CENTER);
		inputpanel.add(input);
		inputpanel.add(enterButton);
		inputpanel.add(clearButton);
		panel.add(inputpanel, BorderLayout.SOUTH);
		this.getContentPane().add(BorderLayout.CENTER, panel);
		this.pack();
		this.setLocationByPlatform(true);
		// Center of screen
		this.setVisible(true);
		this.setResizable(true);
		input.requestFocus();
	}

	private class ButtonListener implements ActionListener {

		public void actionPerformed(final ActionEvent ev) {
			cmd = input.getText();
			if(monitor!=null){
				synchronized (monitor) {
					monitor.notifyAll();
				}
			}
			input.setText("");
			input.requestFocus();
		}
	}
	
	public synchronized String readLine(){
		try {
			monitor = this;
			this.wait();
		} catch (InterruptedException e) {
            logger.debug(this, e);
			monitor = null;
		}
		return cmd;
	}
	
	public void println(String text){
		output.append(text);
		output.append("\n");
	}
}
