package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Server extends JFrame {
	private static final long serialVersionUID = -8314928059934802143L;
	private SSLServerSocket serverSocket;
	private final static int PORT = 9999;
	private final UserManager userManager = new UserManager();
	final DefaultTableModel onlineUsersDtm = new DefaultTableModel();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private UserDerby userDerby = new UserDerby();

	private final JPanel contentPane;
	private final JTable tableOnlineUsers;
	private final JTextPane textPaneMsgRecord;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Server frame = new Server();
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
	public Server() {
		setTitle("\u670D\u52A1\u5668");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 561, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPaneNorth = new JSplitPane();
		splitPaneNorth.setResizeWeight(0.5);
		contentPane.add(splitPaneNorth, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setPreferredSize(new Dimension(100, 300));
		scrollPaneMsgRecord.setViewportBorder(
				new TitledBorder(null, "\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneNorth.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setPreferredSize(new Dimension(100, 100));
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setPreferredSize(new Dimension(100, 300));
		splitPaneNorth.setRightComponent(scrollPaneOnlineUsers);

		onlineUsersDtm.addColumn("用户名");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("端口");
		onlineUsersDtm.addColumn("登录时间");
		tableOnlineUsers = new JTable(onlineUsersDtm);
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // 让JTable充满它的容器
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton btnStart = new JButton("\u542F\u52A8");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverSocket = openSocket();
				String msgRecord = dateFormat.format(new Date()) + " 服务器启动成功" + "\r\n";
				addMsgRecord(msgRecord, Color.red, 12, false, false);
				new Thread() {
					@Override
					public void run() {
						while (true) {
							try {
								SSLSocket socket = (SSLSocket) serverSocket.accept();
								UserHandler userHandler = new UserHandler(socket);
								new Thread(userHandler).start();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
				}.start();
				btnStart.setEnabled(false);
			}
		});
		panelSouth.add(btnStart);
	}

	public SSLServerSocket openSocket() {
		SSLServerSocketFactory serverSocketFactory = null;
		try {
			char[] passwdCert = "123456".toCharArray();
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(new FileInputStream("mykeys.keystore"), passwdCert);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, passwdCert);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			serverSocketFactory = sslContext.getServerSocketFactory();

		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException
				| IOException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		}
		try {
			return (SSLServerSocket) serverSocketFactory.createServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void addMsgRecord(final String msgRecord, Color msgColor, int fontSize, boolean isItalic,
			boolean isUnderline) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Document docs = textPaneMsgRecord.getDocument();
				try {
					docs.insertString(docs.getLength(), msgRecord, attrset);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	class UserHandler implements Runnable {
		private final Socket currentUserSocket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		public UserHandler(Socket currentUserSocket) {
			this.currentUserSocket = currentUserSocket;
			try {
				ois = new ObjectInputStream(currentUserSocket.getInputStream());
				oos = new ObjectOutputStream(currentUserSocket.getOutputStream());
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					Message msg = (Message) ois.readObject();
					String date = dateFormat.format(new Date());
					if (msg instanceof UserStateMessage) {
						processUserStateMessage((UserStateMessage) msg);
						System.out.println(date + "获取用户" + msg.getSrcUser() + "状态信息");
					} else if (msg instanceof ChatMessage) {
						processChatMessage((ChatMessage) msg);
						System.out.println(date + "获取用户" + msg.getSrcUser() + "消息信息");
					} else if (msg instanceof UserRegisterMessage) {
						processRegisterMessage((UserRegisterMessage) msg);
						System.out.println(date + "获取用户" + msg.getSrcUser() + "注册请求");
					} else if (msg instanceof UserLoginMessage) {
						processLoginMessage((UserLoginMessage) msg);
						System.out.println(date + "获取用户" + msg.getSrcUser() + "登录请求");
					} else if (msg instanceof FileMessage) {
						processFileMessage((FileMessage) msg);
						System.out.println("服务器收到");
					} else if (msg instanceof SendFileMessage) {
						processSendFileMessage((SendFileMessage) msg);
					} else {
						System.err.println("用户发出错误信息");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("客户端退出");
				} else {
					System.out.println("客户端退出");
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (currentUserSocket != null) {
					try {
						currentUserSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void transferMsgToOtherUsers(Message msg) {
			String[] users = userManager.getAllUsers();
			for (String user : users) {
				if (userManager.getUserSocket(user) != currentUserSocket) {
					try {
						ObjectOutputStream o = userManager.getUserOos(user);
						synchronized (o) {
							o.writeObject(msg);
							o.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();

			if (msg.isUserOnline()) {
				if (userManager.hasUser(srcUser)) {
					System.err.println("用户重复登录");
					return;
				}
				String[] users = userManager.getAllUsers();
				try {
					for (String user : users) {
						UserStateMessage userStateMessage = new UserStateMessage(user, srcUser, true);
						synchronized (oos) {
							oos.writeObject(userStateMessage);
							oos.flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				transferMsgToOtherUsers(msg);
				onlineUsersDtm.addRow(new Object[] { srcUser, currentUserSocket.getInetAddress().getHostAddress(),
						currentUserSocket.getPort(), dateFormat.format(new Date()) });
				userManager.addUser(srcUser, currentUserSocket, oos, ois);
				String ip = currentUserSocket.getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "上线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
			} else {
				if (!userManager.hasUser(srcUser)) {
					System.err.println("用户未发送登录消息就发送了下线消息");
					return;
				}
				String ip = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "下线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				userManager.removeUser(srcUser);
				for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
						onlineUsersDtm.removeRow(i);
					}
				}
				transferMsgToOtherUsers(msg);
			}
		}

		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (userManager.hasUser(srcUser)) {
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "说: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
				if (msg.isPubChatMessage()) {
					transferMsgToOtherUsers(msg);
				} else {
					ObjectOutputStream o = userManager.getUserOos(dstUser);
					synchronized (o) {
						try {
							o.writeObject(msg);
							o.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				System.err.println("用启未发送上线消息就直接发送了聊天消息");
				return;
			}
		}

		private void processRegisterMessage(UserRegisterMessage userRegisterMsg) {
			try {
				Security.addProvider(new BouncyCastleProvider());
				SecureRandom secureRandom = new SecureRandom();
				byte[] salt = new byte[32];
				secureRandom.nextBytes(salt);
				MessageDigest md = MessageDigest.getInstance("SM3");
				md.update(userRegisterMsg.getPasswd().getBytes());
				md.update(salt);
				System.out.println(userRegisterMsg.getPasswd());
				userDerby = new UserDerby();
				if (userDerby.insertUser(userRegisterMsg.getSrcUser(), userRegisterMsg.getAge(),
						userRegisterMsg.getSex(), userRegisterMsg.getEmail(), md.digest(), salt)) {
					synchronized (oos) {
						oos.writeObject(new userRegisterStatus(userRegisterMsg.getSrcUser(), "", true));
						oos.flush();
					}
					String msgRecord = dateFormat.format(new Date()) + "新用户注册成功：" + userRegisterMsg.getSrcUser()
							+ "\r\n";
					addMsgRecord(msgRecord, Color.blue, 12, false, false);
				} else {
					synchronized (oos) {
						oos.writeObject(new userRegisterStatus(userRegisterMsg.getSrcUser(), "", false));
						oos.flush();
					}

					String msgRecord = dateFormat.format(new Date()) + "新用户注册失败：" + userRegisterMsg.getSrcUser()
							+ "\r\n";
					addMsgRecord(msgRecord, Color.blue, 12, false, false);

				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void processLoginMessage(UserLoginMessage userLoginMsg) {
			try {

				userDerby = new UserDerby();
				if (userDerby.checkUserPwd(userLoginMsg) && !userManager.hasUser(userLoginMsg.getSrcUser())) {
					synchronized (oos) {
						oos.writeObject(new UserLoginStatus(userLoginMsg.getSrcUser(), "", true));
						oos.flush();
					}
					String msgRecord = dateFormat.format(new Date()) + "允许用户" + userLoginMsg.getSrcUser() + "登录\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
				} else {
					if (userManager.hasUser(userLoginMsg.getSrcUser())) {
						synchronized (oos) {
							oos.writeObject(
									new UserLoginStatus(userLoginMsg.getSrcUser(), "", false, "duplicateLogin"));
							oos.flush();
						}
					} else {
						synchronized (oos) {
							oos.writeObject(new UserLoginStatus(userLoginMsg.getSrcUser(), "", false));
							oos.flush();
						}
					}
					String msgRecord = dateFormat.format(new Date()) + "拒绝用户" + userLoginMsg.getSrcUser() + "登录\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void processSendFileMessage(SendFileMessage msg) {
			String dstUser = msg.getDstUser();
			try {
				ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
				synchronized (objectOutputStream) {
					objectOutputStream.writeObject(msg);
					objectOutputStream.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void processFileMessage(FileMessage msg) {
			String dstUser = msg.getDstUser();
			try {
				ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
				synchronized (objectOutputStream) {
					objectOutputStream.writeObject(msg);
					objectOutputStream.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}

class UserManager {
	private final Hashtable<String, User> onLineUsers;

	public UserManager() {
		onLineUsers = new Hashtable<String, User>();
	}

	public boolean hasUser(String userName) {
		return onLineUsers.containsKey(userName);
	}

	public boolean isEmpty() {
		return onLineUsers.isEmpty();
	}

	public ObjectOutputStream getUserOos(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOos();
		}
		return null;
	}

	public ObjectInputStream getUserOis(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOis();
		}
		return null;
	}

	public Socket getUserSocket(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getSocket();
		}
		return null;
	}

	public boolean addUser(String userName, Socket userSocket) {
		if ((userName != null) && (userSocket != null)) {
			onLineUsers.put(userName, new User(userSocket));
			return true;
		}
		return false;
	}

	public boolean addUser(String userName, Socket userSocket, ObjectOutputStream oos, ObjectInputStream ios) {
		if ((userName != null) && (userSocket != null) && (oos != null) && (ios != null)) {
			onLineUsers.put(userName, new User(userSocket, oos, ios));
			return true;
		}
		return false;
	}

	public boolean removeUser(String userName) {
		if (hasUser(userName)) {
			onLineUsers.remove(userName);
			return true;
		}
		return false;
	}

	public String[] getAllUsers() {
		String[] users = new String[onLineUsers.size()];
		int i = 0;
		for (String userName : onLineUsers.keySet()) {
			users[i++] = userName;
		}
		return users;
	}

	public int getOnlineUserCount() {
		return onLineUsers.size();
	}
}

class User {
	private final Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Date logonTime;

	public User(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois, Date logonTime) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		this.logonTime = logonTime;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public Date getLogonTime() {
		return logonTime;
	}

}
