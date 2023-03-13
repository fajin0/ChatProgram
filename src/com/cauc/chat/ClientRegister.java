package com.cauc.chat;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class ClientRegister extends JFrame {

	private static final long serialVersionUID = 7926559552859471246L;
	private final int port = 9999;
	private JPanel contentPane;
	private JTextField txtfieldUserName;
	private JTextField txtFieldAge;
	private JTextField txtFieldEmail;
	private JPasswordField passwordFieldPwd;
	private JComboBox<String> comboBoxSex;

	private SSLSocket socket = null;
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientRegister frame = new ClientRegister();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientRegister() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblUserName = new JLabel("\u7528\u6237\u540D");
		lblUserName.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		lblUserName.setBounds(66, 40, 54, 15);
		contentPane.add(lblUserName);

		JLabel lblPwd = new JLabel("\u5BC6\u7801");
		lblPwd.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		lblPwd.setBounds(66, 80, 54, 15);
		contentPane.add(lblPwd);

		JLabel lblSex = new JLabel("\u6027\u522B");
		lblSex.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		lblSex.setBounds(66, 120, 54, 15);
		contentPane.add(lblSex);

		JLabel lblAge = new JLabel("\u5E74\u9F84");
		lblAge.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		lblAge.setBounds(66, 160, 54, 15);
		contentPane.add(lblAge);

		JLabel lblEmail = new JLabel("\u90AE\u7BB1");
		lblEmail.setBounds(66, 200, 54, 15);
		contentPane.add(lblEmail);

		JLabel lblInfo = new JLabel("\u8BF7\u586B\u5199\u4E0B\u5217\u6CE8\u518C\u4FE1\u606F\uFF1A");
		lblInfo.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		lblInfo.setBounds(31, 10, 158, 15);
		contentPane.add(lblInfo);

		txtfieldUserName = new JTextField();
		txtfieldUserName.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		txtfieldUserName.setBounds(156, 38, 100, 21);
		contentPane.add(txtfieldUserName);
		txtfieldUserName.setColumns(10);

		txtFieldAge = new JTextField();
		txtFieldAge.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		txtFieldAge.setColumns(10);
		txtFieldAge.setBounds(156, 158, 100, 21);
		contentPane.add(txtFieldAge);

		txtFieldEmail = new JTextField();
		txtFieldEmail.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		txtFieldEmail.setColumns(10);
		txtFieldEmail.setBounds(156, 198, 100, 21);
		contentPane.add(txtFieldEmail);

		comboBoxSex = new JComboBox<String>();
		comboBoxSex.setModel(new DefaultComboBoxModel<String>(new String[] { "\u5973", "\u7537" }));
		comboBoxSex.setToolTipText("");
		comboBoxSex.setBounds(166, 117, 59, 23);
		contentPane.add(comboBoxSex);

		final JButton btnRegister = new JButton("\u6CE8\u518C");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Security.addProvider(new BouncyCastleProvider());
					String userName = txtfieldUserName.getText();
					String pwd = passwordFieldPwd.getText();
					String sex = (String) comboBoxSex.getSelectedItem();
					String age = txtFieldAge.getText();
					String email = txtFieldEmail.getText();

					UserRegisterMessage userRegisterMessage = new UserRegisterMessage(userName, "", age, sex, email,
							pwd);
					try {
						socket = openSocket();
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());
						synchronized (oos) {
							oos.writeObject(userRegisterMessage);
							oos.flush();
						}

						System.out.println("¿Í»§¶Ë×¢²á£º" + userName + "·¢ËÍ×¢²áÇëÇó!");
						userRegisterStatus status = null;
						synchronized (ois) {
							status = (userRegisterStatus) ois.readObject();
						}
						if (status.isRegisterStatus()) {
							System.out.println("¿Í»§¶Ë×¢²á£º" + userName + "×¢²á³É¹¦");
							userRegisterMessage.print();
							JOptionPane.showMessageDialog(null, "×¢²á³É¹¦");
//							ClientRegister.

						} else {
							JOptionPane.showMessageDialog(null, "×¢²áÊ§°Ü£¬ÓÃ»§ÃûÒÑ´æÔÚ");
							System.out.println("¿Í»§¶Ë×¢²á£º" + userName + "×¢²áÊ§°Ü");
						}
						txtfieldUserName.setEditable(false);
						txtFieldAge.setEditable(false);
						txtFieldEmail.setEditable(false);
						comboBoxSex.setEditable(false);
						passwordFieldPwd.setEditable(false);
						btnRegister.setEnabled(false);
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					oos.close();
					socket.close();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		btnRegister.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		btnRegister.setBounds(156, 230, 93, 23);
		contentPane.add(btnRegister);

		passwordFieldPwd = new JPasswordField();
		passwordFieldPwd.setBounds(156, 78, 100, 21);
		contentPane.add(passwordFieldPwd);

	}

	public SSLSocket openSocket() {
		SSLSocketFactory socketFactory = null;
		try {
			char[] passwdCert = "123456".toCharArray();
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(new FileInputStream("mykeys.keystore"), passwdCert);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(keyStore);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
			socketFactory = sslContext.getSocketFactory();

		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return (SSLSocket) socketFactory.createSocket("localhost", port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
