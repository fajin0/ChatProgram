package com.cauc.chat;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;


public class SaltedHashValue {
	public static void main(String[] args) {
		String hashString = new SaltedHashValue().pwdSaltedHashValue("123123213");
		System.out.println(hashString);
	}
	
	private static String pwdSaltedHashValue(String pwd) {
		// 随机数和计算Hash的digestoutputstream
		Random random = new Random();
		int nounce = random.nextInt();
		byte[] nounceByte = intConvertToByte(nounce);
		
		SecureRandom secureRandom = new SecureRandom();
		byte[] randomByte = new byte[16];
		secureRandom.nextBytes(randomByte);
		
		String data = nounceByte.toString() + pwd;
		
		Security.addProvider(new BouncyCastleProvider());
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SM3");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Hex.toHexString(md.digest(data.getBytes()));

	}
	
	private static byte[] intConvertToByte(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
	}
}
