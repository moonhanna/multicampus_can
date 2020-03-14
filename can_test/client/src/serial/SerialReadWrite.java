package serial;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import msg.Client;
import msg.Client.Sender;

public class SerialReadWrite implements SerialPortEventListener {

	CommPortIdentifier commPortIdentifier;
	CommPort comPort;
	InputStream in;
	BufferedInputStream bin;
	OutputStream out;
	String id;// "00000000";
	String data; // "0000000000000000";
	String msg; // id+data;
	static Client client;
	static Socket socket;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setMsg() {
		this.msg = getId()+getData();
	}
	
	public String getMsg() {
		return this.msg;
	}

	public SerialReadWrite() {
	}

	public SerialReadWrite(String portName) throws Exception {
		commPortIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		System.out.println("Identifier Com Port!");
		connect();
		System.out.println("Connect Com Port!");
		new Thread(new Serialwrite()).start();
		System.out.println("Start Can Network!");
	}

	public void connect() throws Exception {

		if (commPortIdentifier.isCurrentlyOwned()) { // 누군가가 사용하고 있는지
			System.out.println("Port is currently in use...");
		} else {
			comPort = commPortIdentifier.open(this.getClass().getName(), 5000);
			if (comPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) comPort;
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				serialPort.setSerialPortParams(921600, // 통신속도
						SerialPort.DATABITS_8, // 데이터 비트
						SerialPort.STOPBITS_1, // stop 비트
						SerialPort.PARITY_NONE); // 패리티
				in = serialPort.getInputStream();
				bin = new BufferedInputStream(in);
				out = serialPort.getOutputStream();
			} else {
				System.out.println("This port is not SerialPort.");
			}
		}

	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[128];

			try {

				while (bin.available() > 0) {
					int numBytes = bin.read(readBuffer);
				}

				String ss = new String(readBuffer);
				System.out.println("Receive Low Data:" + ss + "||");
				if(ss.charAt(1)=='U') {
					String data =parseData(ss);
					client.sendData(data.substring(0, 8),data.substring(8));
				}
			

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}

	}

	public void send(String msg) {
		new Thread(new Serialwrite(msg)).start();
	}
	
	public void adminSend(String msg) {
		new Thread(new Serialwrite(msg)).start();
	}

	class Serialwrite implements Runnable {

		String data;

		public Serialwrite() {
			this.data = ":G11A9\r";
		}

		public Serialwrite(String msg) {
			this.data = convertData(data);
		}

		public String convertData(String msg) {

			msg = msg.toUpperCase();
			msg = "W28" + msg;

			char c[] = msg.toCharArray();
			int checkSum = 0;

			for (char ch : c) {
				checkSum += ch;
			}

			checkSum = (checkSum & 0xFF);

			String result = ":";
			result += msg + Integer.toHexString(checkSum).toUpperCase() + "\r";

			return result;

		}

		@Override
		public void run() {

			byte[] outData = data.getBytes();

			try {
				out.write(outData);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static String parseData(String ss) {
		StringBuilder sb = new StringBuilder(ss);
		String id= sb.substring(4, 12);
		String txt = sb.substring(26, 28);
		txt=Integer.parseInt(txt,16)+"";
		System.out.println("id:"+id+" "+ "txt : "+txt);
		return id+txt;
	}
	
	
	
   public static void main(String[] args) {

      SerialReadWrite sc = null;
     
      try {
         sc = new SerialReadWrite("COM9");
         client = new Client("ip", 8888);
         client.startClient();
         socket= client.getSocket();
         
         
      } catch (Exception e) {
         System.out.println("Connect Fail !");
         e.printStackTrace();
      }

   }

}