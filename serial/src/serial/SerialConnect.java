package serial;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialConnect implements SerialPortEventListener{

	CommPortIdentifier commPortIdentifier;
	CommPort comPort;
	InputStream in;
	BufferedInputStream bin;
	OutputStream out;

	public SerialConnect() {
	}

	public SerialConnect(String portName) throws Exception{
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
		    
		   } catch (Exception e) {
		    e.printStackTrace();
		   }
		   break;
		  }
		
	}
	
	class Serialwrite implements Runnable{
		
		String data;
		
		public Serialwrite() {
			this.data = ":G11A9\r";
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
	
	public static void main(String[] args) {

		SerialConnect sc = null;

		try {
			sc = new SerialConnect("COM1");
		} catch (Exception e) {
			System.out.println("Connect Fail !");
			e.printStackTrace();
		}

	}

}
