package com.cauc.chat;

import java.io.File;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;



public class UserDerby {
	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	String dbName = "USER";
	String connectionURL = "jdbc:derby:" + dbName + ";create=true";
	Connection conn;
	private Message userRegisterMsg;
	
	public static void main(String[] args) {
		UserDerby userDerby = new UserDerby();
		//userDerby.deleteUser("bbb");
		userDerby.showAllUsers();
	}
	public UserDerby() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(driver + " loaded. ");
			String createString = "create table USERTABLE " // 表名
					+ "(USERNAME varchar(20) primary key not null, " // 用户名
					+ "HASHEDPWD char(32) for bit data, "// 口令的HASH值
					+ "SALT char(32) for bit data," //盐值
					+ "AGE varchar(20)," //年龄
					+ "SEX varchar(20)," //性别
					+ "EMAIL varchar(20))"; // 邮箱
			
			//DriverManager.setLogWriter(new PrintWriter(new File("aaa.txt")));
			conn = DriverManager.getConnection(connectionURL);
			Statement s = conn.createStatement();//可以执行SQL语句，connection,statement,resultset用完都要关
			if (!checkTable(conn)) {
				System.out.println(" . . . . creating table USERTABLE");
				s.execute(createString);
			}
			s.close();
			System.out.println("连接数据库");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean insertUser(String userName,String age, 
			String sex, String email, byte[] passwd,byte[] salt) throws NoSuchAlgorithmException {
		try {
			//先查看用户是否已经存在
			PreparedStatement selectStatement = conn.prepareStatement(
					"select * from USERTABLE where USERNAME=?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			selectStatement.setString(1, userName);
			ResultSet rs = selectStatement.executeQuery();
			rs.last();
			int n = rs.getRow();
			selectStatement.close();
			rs.close();
			if(n == 0) {
				//计算盐值和Hash
				PreparedStatement insertStatement = conn
						.prepareStatement("insert into USERTABLE values (?,?,?,?,?,?)");
				insertStatement.setString(1, userName);
				insertStatement.setBytes(2, passwd);
				insertStatement.setBytes(3, salt);
				insertStatement.setString(4, age);
				insertStatement.setString(5, sex);
				insertStatement.setString(6, email);
				insertStatement.executeUpdate();
				insertStatement.close();
				System.out.println("数据库信息：新注册用户：" + userName);
				
				return true;
			}
			else {
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkUserPwd(UserLoginMessage userLoginMsg) {
		try {
			PreparedStatement selectstStatement = conn.prepareStatement(
					"select HASHEDPWD,SALT from USERTABLE where USERNAME=?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			selectstStatement.setString(1, userLoginMsg.getSrcUser());
			ResultSet resultSet = selectstStatement.executeQuery();
			resultSet.last();
			int row = resultSet.getRow();
			if(row == 0) {
				System.out.println("数据库信息：不存在用户" + userLoginMsg.getSrcUser());
				return false;
			}else {
				byte[] salt = resultSet.getBytes("SALT");
				byte[] hashedpwd = resultSet.getBytes("HASHEDPWD");
				selectstStatement.close();
				resultSet.close();
				
				Security.addProvider(new BouncyCastleProvider());
				MessageDigest md = MessageDigest.getInstance("SM3");
				md.update(userLoginMsg.getPwd().getBytes());
				md.update(salt);
				byte[] userPwd = md.digest();
				if (Arrays.equals(hashedpwd, userPwd)) {
					return true;
				} else {
					return false;
				}
			}
			
		} catch (NoSuchAlgorithmException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean deleteUser(String userName) {
			try {
				PreparedStatement psDelete = conn
						.prepareStatement("delete from USERTABLE where USERNAME=?");
				psDelete.setString(1, userName);
				int n = psDelete.executeUpdate();
				psDelete.close();
				if (n > 0) {
					System.out.println("成功删除用户" + userName);
					return true;
				} else {
					System.out.println("删除用户" + userName + "失败");
					return false;
				}
			} catch (SQLException e) {
			}
		return false;
	}
	
	public void showAllUsers() {
		String printLine = "  ______________当前所有注册用户______________";
		try {
			Statement s = conn.createStatement();
			// Select all records in the USERTABLE table
			ResultSet users = s
					.executeQuery("select USERNAME, HASHEDPWD, SALT from USERTABLE");

			// Loop through the ResultSet and print the data
			System.out.println(printLine);
			//users.getString(1):取这个表的第一个字段
			while (users.next()) {
				System.out.println("User-Name: " + users.getString("USERNAME") //用户名
						+ " Hashed-Pasword: " + Base64.getEncoder().encodeToString(users.getBytes("HASHEDPWD"))//口令HASH值的BASE64编码
				+ " Salt: " + Base64.getEncoder().encodeToString(users.getBytes("SALT")));
			}
			System.out.println(printLine);
			// Close the resultSet
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean checkTable(Connection conTst) throws SQLException {
		try {
			Statement s = conTst.createStatement();
			s.execute("update USERTABLE set USERNAME= 'TEST' where 1=3");
		} catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			// System.out.println("  Utils GOT:  " + theError);
			/** If table exists will get - WARNING 02000: No row was found **/
			if (theError.equals("42X05")) // Table does not exist
			{
				return false;
			} else if (theError.equals("42X14") || theError.equals("42821")) {
				System.out
						.println("checkTable: Incorrect table definition. Drop table USERTABLE and rerun this program");
				throw sqle;
			} else {
				System.out.println("checkTable: Unhandled SQLException");
				throw sqle;
			}
		}
		return true;
	}
	public void shutdownDatabase() {
		/***
		 * In embedded mode, an application should shut down Derby. Shutdown
		 * throws the XJ015 exception to confirm success.
		 ***/
		if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
			boolean gotSQLExc = false;
			try {
				conn.close();
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database did not shut down normally");
			} else {
				System.out.println("Database shut down normally");
			}
		}
	}
}
