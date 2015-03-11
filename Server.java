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

import Fileshare.java;

class Server {
	Encryption en = new Encryption();
	Scanner in = new Scanner(System.in);
	ServerSocket serverSocket;
	ArrayList<Socket> clients = new ArrayList<Socket>();
	File[] files;
	int fileNr = 0;

	boolean StartServer() {
		try {
			serverSocket = new ServerSocket(9889);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		InitEncrypt(en);
		ServerMenu();
		GetFiles();
		SendFiles();
		return true;
	}

	void ServerMenu() {
		String hostName = "ERROR";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Couldn't find host name");
			e.printStackTrace();
		}
		System.out.println("If on the same network: " + hostName);
		System.out.println("If connecting remote: ");
		System.out.println();
		System.out.print("How many clients are you expecting? ");
		int nr = in.nextInt();
		System.out.println();
		System.out.println("Waiting for clients to connect.");

		for (int i = clients.size(); i < nr; i++) {
			Socket connection = new Socket();
			try {
				connection = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("Connection failed!");
				e.printStackTrace();
			}
			clients.add(connection);
			System.out.println(connection.getPort() + " connected! (" + (i+1) + "/" + nr + ")");
		}
	}

	void GetFiles() {
		boolean done = false;
		File folder = new File(System.getProperty("user.dir"));
		files = folder.listFiles();

		System.out.println("\nCurrently selected files: ");
		System.out.println("\nCurrently selected files: ");

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				System.out.println(files[i].getName());
				fileNr++;
			}
		}
		System.out.println();
	}

	void SendFiles() {
		try {
			for (int i = 0; i < clients.size(); i++) {
				NotifyQue();
				System.out.println("Preparing file transfer to " + clients.get(i).getPort());
				OutputStream os = clients.get(i).getOutputStream();
				DataOutputStream ds = new DataOutputStream(os);
				ds.writeInt(fileNr);
				DataInputStream dis = new DataInputStream(clients.get(i).getInputStream());

				for (int j = 0; j < files.length; j++) {
					if (files[j].isFile()) {
						ds.writeChars(files[j].getName() + "|");
						ds.writeInt((int)files[j].length());

						byte[] bytearray = new byte[(int) files[j].length()];
						BufferedInputStream bin = new BufferedInputStream(new FileInputStream(files[j]));
						bin.read(bytearray, 0, bytearray.length);
						System.out.println(bytearray.length);

						System.out.println("Sending " + files[j].getName() + " to " + clients.get(i).getPort());
						int currentTot = 0;
						if (bytearray.length > 64000) {
							int rounds = bytearray.length/64000;
							for (int k = 0; k < rounds; k++) {
								os.write(bytearray, currentTot, 64000);
								currentTot += 64000;
								os.flush();
								if (dis.readInt() != 1) {
									System.exit(1);
								}
							}
						}
						os.write(bytearray, currentTot, bytearray.length-currentTot);
						os.flush();
						if (dis.readInt() != 1) {
							System.exit(1);
						}
					}
				}
				System.out.println("File transfer to " + clients.get(i).getPort() + " complete!\n");
				clients.get(i).close();
			}
			System.out.println("File transfer complete");
		} catch (IOException e) {
			System.out.println("Error occured while sending files!");
			e.printStackTrace();
		}
	}

	void NotifyQue() {
		try {
			for (int i = 0; i < clients.size(); i++) {
				OutputStream os = clients.get(i).getOutputStream();
				DataOutputStream ds = new DataOutputStream(os);
				ds.writeInt(i);
			}
		} catch (IOException e) {
			System.out.println("Error occured while updating the que!");
			e.printStackTrace();
		}
	}

	void InitEncrypt(Encryption en) {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			en.aesCipher = Cipher.getInstance("AES");
			en.aesKey = keyGen.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}