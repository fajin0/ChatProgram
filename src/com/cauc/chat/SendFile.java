package com.cauc.chat;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SwingWorker;

import javax.swing.JProgressBar;
import javax.swing.JLabel;

public class SendFile extends JFrame{
	private static final long serialVersionUID = 6592386607649676163L;
	private JTextField textFieldFilePath;
	private SSLSocket sslsocket;
	private long size;
	private JProgressBar progressBar;
	private JLabel lblSpeed;
	
	
	public SendFile(String drcUser, String filePath, int port, FileInputStream fis, long fileLength) {
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 431, 415);
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		panel.setToolTipText("给" + drcUser + "的发送进度");
		
		textFieldFilePath = new JTextField();
		textFieldFilePath.setText("<dynamic>");
		textFieldFilePath.setToolTipText("");
		textFieldFilePath.setBounds(14, 74, 385, 26);
		panel.add(textFieldFilePath);
		textFieldFilePath.setColumns(10);
		textFieldFilePath.setText(filePath);
		textFieldFilePath.setEditable(false);
		
		progressBar = new JProgressBar();
		progressBar.setToolTipText("");
		progressBar.setBounds(51, 153, 304, 20);
		panel.add(progressBar);
		
		lblSpeed = new JLabel("", JLabel.CENTER);
		lblSpeed.setBounds(51, 125, 61, 16);
		panel.add(lblSpeed);
		lblSpeed.setVisible(false);
		
	    try {
	    	char[] passwdCert = "123456".toCharArray();
			SSLContext sslContext = null;
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(new FileInputStream("mykeys.keystore"), passwdCert);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(keyStore);
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
			SSLSocketFactory factory=sslContext.getSocketFactory();
			sslsocket=(SSLSocket)factory.createSocket("localhost",port);
			
			OutputStream os = sslsocket.getOutputStream();
			UpdateProgressBar updateProgressBar = new UpdateProgressBar(os, fis);
			updateProgressBar.execute();
			size = fileLength;
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	class UpdateProgressBar extends SwingWorker<String, String> {
		private OutputStream os;
		private FileInputStream fis;
		public UpdateProgressBar(OutputStream os, FileInputStream fis) {
			this.os = os;
			this.fis = fis;
		}
		@Override		
		protected String doInBackground() throws Exception{ 
			int n = 0;
			int sum = 0;
			System.out.println("文件总长度：" + size);
			byte[] buffer = new byte[1024];
	        while ((n = fis.read(buffer)) != -1) {             
	        	 os.write(buffer,0,n);
	        	 os.flush();
	             sum = sum + n;
	             publish("已发送:"+ sum / 1024 + "KB/" + size / 1024 + "KB" + '\n');
	         }
	         if(n == -1) {
	        	 os.close();
	        	 sslsocket.close();
	             fis.close();
	             System.out.println("关闭连接");
	             setVisible(false);
	         }
			return null;   
		}
		
		@Override
		protected void process(List<String> chunks) { 
			for (String string : chunks) {
				System.out.println(string);
				string = string.replace(":", " ");
				string = string.replace("KB", " ");
				String[] submit = new String[10];
				submit = string.split(" ");
				float persent = Float.parseFloat(submit[1]) / (float) (size / 1024);
				progressBar.setVisible(true);
				progressBar.setValue((int) (persent * 100));
				lblSpeed.setText("传输进度:" + (int) (persent * 100) + "%");
				lblSpeed.setVisible(true);
			}
		}
	}
}


