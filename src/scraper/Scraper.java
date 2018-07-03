package scraper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.*;

public class Scraper {
	
	private void initUI() {
		JLabel header = new JLabel();
		header.setText("<html>GUI Scraper by Clayton Chu <div style='background-color: blue; width: 70px; height: 30px;'></div></html>");
		header.setFont(new Font("Lato", Font.PLAIN, 35));
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
		
		JPanel headerBox = new JPanel(new BorderLayout());
		headerBox.add(header, BorderLayout.NORTH);
		
		JFrame frame = new JFrame();
		frame.add(headerBox);
		frame.setVisible(true);
	}
	
	Scraper(){
		initUI();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String cmd = "bash -c 'curl -L http://www.google.com'";
		Runtime run = Runtime.getRuntime();
		Process pr = run.exec(cmd);
		pr.waitFor();
		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		while((line = buf.readLine()) != null)
			System.out.println(line);
		
		new Scraper();
	}
	
}
