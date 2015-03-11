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

class Client {
	Scanner in = new Scanner(System.in);
	boolean done = false;
	String host;
	Socket socket;
	InputStream is;
	DataInputStream ds;
	DataOutputStream dos;

	int numberFiles;
	String fileName = "";
	int filesize;
	int bytesRead;
	int currentTot = 0;

	boolean StartClient() {
		GetHost();

		try {
			is = socket.getInputStream();
			ds = new DataInputStream(is);
			dos = new DataOutputStream(socket.getOutputStream());

			Que();

			numberFiles = ds.readInt();
			System.out.println("Downloading " + numberFiles + " files.\n");

			for (int i = 0;  i < numberFiles; i++) {
				char c;

				do {
					c = ds.readChar();
					if (c != '|') {
						fileName = fileName + c;

					}
				} while (c != '|');

				filesize = ds.readInt();
				System.out.print("Downloading: " + fileName + " Filesize: ");
				double size = filesize;
				String id = " bytes.";

				if (filesize > 1000 && filesize < 1000000) {
					size = (double)filesize/1000.0;
					id = " KB.";
				} else if (filesize > 1000000 && filesize < 1000000000) {
					size = (double)filesize/1000000.0;
					id = " MB.";
				} else if (filesize > 1000000000) {
					size = (double)filesize/1000000000.0;
					id = " GB.";
				}
				size = round(size, 2);

				System.out.println(size + id);

				byte[] bytearray  = new byte[filesize];
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
				bytesRead = is.read(bytearray, 0, bytearray.length);
				currentTot = bytesRead;
				dos.writeInt(1);
				dos.flush();

				while(currentTot < filesize) {
					bytesRead =
					is.read(bytearray, currentTot, (bytearray.length-currentTot));
					if(bytesRead >= 0) currentTot += bytesRead;
					dos.writeInt(1);
					dos.flush();
				}
				bos.write(bytearray, 0 , currentTot);
				bos.flush();
				bos.close();
				fileName = "";
			}
			socket.close();
			System.out.println("\nFiles received successfully!");
		} catch (IOException e) {
			System.out.println("Error occured while downloading files!");
			e.printStackTrace();
		}
		return true;
	}

	void GetHost() {
		try {
			System.out.print("Please enter the host name or IP adress you want to connect to (to exit loop, enter: cancel): ");
			host = in.nextLine();
			if (host.equalsIgnoreCase("Cancel")) {
				System.out.println("Connection canceled!");
				System.exit(0);
			}
			System.out.println();
			
			socket = new Socket(host,9889);
		} catch (UnknownHostException uhe) {
			System.out.println("The host you entered could not be found, make sure it was spelled correctly.");
			GetHost();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Connected!\n");
	}

	void Que() {
		int number = Integer.MAX_VALUE;
		do {
			try {
				number = ds.readInt();
			} catch (IOException e) {
				System.out.println("Error occured while receiveing que number!");
				e.printStackTrace();
			}
			System.out.println("You are currently number " + number + " in the line.");
		} while (number != 0);
		System.out.println();
	}

	double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}