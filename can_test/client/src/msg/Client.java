package msg;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import serial.SerialReadWrite;

public class Client {
	String ss;
	 Socket socket;
	Sender sender;
	String vel;
	boolean aflag = true;

	public Client() {
	}
	
	public Socket getSocket() {
		return this.socket;
	}

	public Client(String address, int port) throws IOException {
		try {
			socket = new Socket(address, port);

		} catch (Exception e) {
			while (true) {
				System.out.println("Retry..");
				try {
					Thread.sleep(1000);
					socket = new Socket(address, port);
					break;
				} catch (Exception e1) {
					// e1.printStackTrace();
				}
			}
		}

		System.out.println("Connected Server:" + address);

		sender = new Sender(socket);

		Msg msg = new Msg("Milktea", null, null);
		sender.setMsg(msg);
		new Thread(sender).start();

		new Receiver(socket).start();
	}
	
	
	public String getSs() {
		return this.ss;
	}

	public void setSs(String ss) {
		this.ss = ss;
	}

	class Receiver extends Thread {
		InputStream is;
		ObjectInputStream ois;

		public Receiver(Socket socket) throws IOException {
			is = socket.getInputStream();
			ois = new ObjectInputStream(is);
		}

		@Override
		public void run() {
			while (ois != null) {
				Msg msg = null;
				try {
					msg = (Msg) ois.readObject();

					if (msg.getIps() == null || msg.getIps().size() == 0) {
						System.out.println(msg.getId() + ":" + msg.getTxt());
					} else {
						ArrayList<String> list = msg.getIps();
						System.out.println(list);
					}

					if (msg.getId().equals("Admin")) {

						if (msg.getTxt().equals("stop")) {
							vel = "0";
							aflag = false;
						}

						else if (msg.getTxt().equals("start")) {
							aflag = true;
						}
						vel = msg.getTxt();
						System.out.println("vel : " + vel);

						msg.setId("Milktea");
						sender.setMsg(msg);
						// System.out.println("Receiver : "+ msg.getTxt());
						new Thread(sender).start();

					}

				} catch (Exception e) {
					System.out.println("Server Die");
					break;
				}
			}

			try {
				if (ois != null) {
					ois.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public void sendData(String id,String msg) throws IOException {
		sender.setMsg(new Msg(id,msg,"/"));
		new Thread(sender).start();
	}

	public class Sender implements Runnable {

		OutputStream os;
		ObjectOutputStream oos;
		Msg msg;

		public Sender(Socket socket) throws IOException {
			os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
		}

		public void setMsg(Msg msg) {

			this.msg = msg;
		}

		@Override
		public void run() {
			if (oos != null) {
				try {

					oos.writeObject(msg);
					System.out.println("Sender: " + msg.getTxt());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void startClient() {

		while (true) {

			try {
				Msg msg = new Msg("Milktea", "0", null);
				if (aflag) {
					vel = Math.floor(Math.random() * 30 + 60) + "";
					msg.setTxt(vel);
				}
				System.out.println("vel: " + vel);

				sender.setMsg(msg);
				new Thread(sender).start();
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;

			}

		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("End Client.");

	}


}
