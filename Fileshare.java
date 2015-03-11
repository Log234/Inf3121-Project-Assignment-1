import java.net.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.text.*;
import java.security.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.interfaces.*;
import javax.crypto.spec.*;

//Files
import Server.java;
import Client.java;

public class Fileshare {
	public static void main(String[] args) {
		String result;
		boolean done = false;
		boolean send = false;
		Scanner in = new Scanner(System.in);

		while (done == false) {
			System.out.print("Do you wish to send or receive files (S/R)? ");
			result = in.nextLine();
			System.out.println();

			if (result.equalsIgnoreCase("S")) {
				send = true;
				done = true;

			} else if (result.equalsIgnoreCase("R")) {
				send = false;
				done = true;

			} else {
				System.out.println("Invalid answer, try again!");
			}
		}
		while (done == false) {
			System.out.print("Are you going to host the transfer (Y/N)? ");
			result = in.nextLine();
			System.out.println();

			if (result.equalsIgnoreCase("Y")) {
				Server server = new Server();
				done = server.StartServer();

			} else if (result.equalsIgnoreCase("N")) {
				Client client = new Client();
				done = client.StartClient();

			} else {
				System.out.println("Invalid answer, try again!");
			}

		}

	}
}





class Encryption {
	Cipher aesCipher;
	SecretKey aesKey;

	String EncryptKey(String input) {
		DateFormat df = new SimpleDateFormat("dd;MM;yy;HH;mm");
		Calendar calobj = Calendar.getInstance();

		String sKey = (String) aesKey.getEncoded();
		char[] key = sKey.toCharArray();
		int[] iKey = new int[key.length];

		for (int i = 0; i < iKey.length; i++) {
			iKey[i] = (int) key[i];
		}

		for (int i = iKey.length-1; i < -1; i--) {
			if (i != 0) {
				key[i] = (char) ((((iKey[i]*iKey[i-1]) + 4)-74)*2);

			} else {
				String solution = df.format(calobj.getTime());
				char[] cSolution = solution.toCharArray();
				int[] iSolution = new int[cSolution.length];

				for (int j = 0; j < iSolution.length; j++) {
					iSolution[j] = (int) cSolution[j];
				}

				int value = iSolution[0] + iSolution[1] - iSolution[2] + iSolution[3] - iSolution[4];
				key[0] = (char) ((((iKey[0]*value) + 4)-74)*2);
			}
		}
		return new String(key);
	}

	String Encrypt(String input) {
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
		byte[] Coded;
		char[] Coded2;

		Coded = aesCipher.doFinal(input.getBytes());
		Coded2 = new char[Coded.length];

		for(int i=0;i<Coded.length;i++)
			Coded2[i] = (char)Coded[i];

		return new String(Coded2);

	}

	int Encrypt(int input) {
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
		byte[] toCode = ByteBuffer.allocate(4).putInt(input).array();
		byte[] Coded = aesCipher.doFinal(toCode);
		return ByteBuffer.wrap(Coded).getInt();

	}

	byte[] Encrypt(byte[] input) {
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
		return aesCipher.doFinal(input);

	}

	void DecryptKey(String input) {
		DateFormat df = new SimpleDateFormat("dd;MM;yy;HH;mm");
		Calendar calobj = Calendar.getInstance();
		String solution = df.format(calobj.getTime());

		char[] key = input.toCharArray();
		int[] iKey = new int[key.length];
		for (int i = 0; i < iKey.length; i++) {
			iKey[i] = (int) key[i];
		}

		for (int i = 0; i < iKey.length; i++) {
			if (i != 0) {
				key[i] = (char) ((((iKey[i]/2) +74) -4)/iKey[i-1]);

			} else {
				char[] cSolution = solution.toCharArray();
				int[] iSolution = new int[cSolution.length];

				for (int j = 0; j < iSolution.length; j++) {
					iSolution[j] = (int) cSolution[j];
				}

				int value = iSolution[0] + iSolution[1] - iSolution[2] + iSolution[3] - iSolution[4];

				key[0] = (char) ((((iKey[i]/2) +74) -4)/value);
			}
		}
		String result =  new String(key);

		aesKey = new SecretKeySpec(result.getBytes(), "AES");
	}

	String Decrypt(String input) {
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
		byte[] Coded;
		char[] Coded2;

		Coded = aesCipher.doFinal(input.getBytes());
		Coded2 = new char[Coded.length];
		
		for(int i=0;i<Coded.length;i++)
			Coded2[i] = (char)Coded[i];

		return new String(Coded2);

	}

	int Decrypt(int input) {
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
		byte[] toCode = ByteBuffer.allocate(4).putInt(input).array();
		byte[] Coded = aesCipher.doFinal(toCode);
		return ByteBuffer.wrap(Coded).getInt();

	}

	byte[] Decrypt(byte[] input) {
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
		return aesCipher.doFinal(input);

	}
}