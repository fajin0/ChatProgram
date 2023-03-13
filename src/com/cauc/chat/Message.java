package com.cauc.chat;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1705703883214597078L;
	private String srcUser;
	private String dstUser;

	public Message(String srcUser, String dstUser) {
		this.srcUser = srcUser;
		this.dstUser = dstUser;
	}

	public String getSrcUser() {
		return srcUser;
	}

	public void setSrcUser(String srcUser) {
		this.srcUser = srcUser;
	}

	public String getDstUser() {
		return dstUser;
	}

	public void setDstUser(String dstUser) {
		this.dstUser = dstUser;
	}
}

class ChatMessage extends Message {

	private static final long serialVersionUID = -2524099131367593960L;
	private String msgContent;

	public ChatMessage(String srcUser, String dstUser, String msgContent) {
		super(srcUser, dstUser);
		this.msgContent = msgContent;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public boolean isPubChatMessage() {
		return getDstUser().equals("");
	}
}

class UserStateMessage extends Message {

	private static final long serialVersionUID = 5321645244646135843L;
	private boolean userOnline;

	public UserStateMessage(String srcUser, String dstUser, boolean userOnline) {
		super(srcUser, dstUser);
		this.userOnline = userOnline;
	}

	public boolean isUserOnline() {
		return userOnline;
	}

	public boolean isUserOffline() {
		return !userOnline;
	}

	public void setUserOnline(boolean userOnline) {
		this.userOnline = userOnline;
	}

	public boolean isPubUserStateMessage() {
		return getDstUser().equals("");
	}
}

class UserRegisterMessage extends Message {
	private static final long serialVersionUID = -7184522146250820441L;
	private String age;
	private String sex;
	private String email;
	private String passwd;

	public UserRegisterMessage(String srcUser, String dstUser, String age, String sex, String email, String passwd) {
		super(srcUser, dstUser);
		this.age = age;
		this.sex = sex;
		this.email = email;
		this.passwd = passwd;
	}

	public String getAge() {
		return age;
	}

	public String getSex() {
		return sex;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswd() {
		return passwd;
	}

	public void print() {
		System.out.println("’À∫≈£∫" + getSrcUser());
		System.out.println("–‘±£∫" + getAge());
		System.out.println("ƒÍ¡‰£∫" + getSex());
		System.out.println("” œ‰£∫" + getEmail());
	}

}

class UserLoginMessage extends Message {
	private static final long serialVersionUID = 4550783985506829467L;
	private String pwd;

	public UserLoginMessage(String srcUser, String dstUser, String pwd) {
		super(srcUser, dstUser);
		this.pwd = pwd;
	}

	public String getPwd() {
		return pwd;
	}
}

class userRegisterStatus extends Message {
	private static final long serialVersionUID = 4678383176985458070L;
	private boolean registerStatus;

	public userRegisterStatus(String srcUser, String dstUser, boolean registerStatus) {
		super(srcUser, dstUser);
		this.registerStatus = registerStatus;
	}

	public boolean isRegisterStatus() {
		return registerStatus;
	}

}

class UserLoginStatus extends Message {
	private static final long serialVersionUID = -1738045447389496878L;
	private boolean loginStatus;
	private String state;

	public UserLoginStatus(String srcUser, String dstUser, boolean loginStatus) {
		super(srcUser, dstUser);
		this.loginStatus = loginStatus;
		this.state = " ";
	}

	public UserLoginStatus(String srcUser, String dstUser, boolean loginStatus, String state) {
		super(srcUser, dstUser);
		this.loginStatus = loginStatus;
		this.state = state;
	}

	public boolean isLogined() {
		return loginStatus;
	}

	public String getStateString() {
		return state;
	}

	public void setStateString(String state) {
		this.state = state;
	}

}

class FileMessage extends Message {
	private static final long serialVersionUID = -3974713751957827313L;
	private String filename;
	private long filelength;

	public FileMessage(String srcUser, String dstUser, String filename) {
		super(srcUser, dstUser);
		this.filename = filename;
	}

	public FileMessage(String srcUser, String dstUser, String filename, long filelength) {
		super(srcUser, dstUser);
		this.filename = filename;
		this.filelength = filelength;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilelength() {
		return filelength;
	}

	public void setFilelength(long filelength) {
		this.filelength = filelength;
	}

}

class SendFileMessage extends Message {
	private static final long serialVersionUID = -7520702639779538290L;
	private boolean accept;
	private int port;

	public SendFileMessage(String srcUser, String dstUser) {
		super(srcUser, dstUser);
	}

	public SendFileMessage(String srcUser, String dstUser, boolean accept, int port) {
		super(srcUser, dstUser);
		this.accept = accept;
		this.port = port;
	}

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
