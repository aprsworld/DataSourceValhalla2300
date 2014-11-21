import java.io.*;
import java.net.*;
import java.util.Vector;



public class TCPReaderValhalla2300 extends Thread {
	protected Boolean connected;
	protected Vector<ListenerValhalla2300> packetListeners;
	protected String host;
	protected int port;

	private BufferedReader inFromGPIB;
	private DataOutputStream outToGPIB;
	private Socket clientSocket;

	public boolean isConnected() {
		if ( null == clientSocket )
			return false;
		
		return clientSocket.isConnected();
	}

	public void addPacketListener(ListenerValhalla2300 b) {
		packetListeners.add(b);
	}

	public boolean sendLine(String s) {
		if ( null == clientSocket ) 
			return false;
		
		
		while ( ! clientSocket.isConnected() ) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.err.println("# TCPReaderValhalla2300 sendLine('" + s + "')");
		try {
			outToGPIB.writeBytes(s);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}


		return true;
	}

	public void run() {

		/* connect and open socket */
		try {
			clientSocket = new Socket(host, port);


			//			System.err.println(clientSocket.toString());
			//			System.err.println("Connected? " + clientSocket.isConnected());

			inFromGPIB = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToGPIB = new DataOutputStream(clientSocket.getOutputStream());


			while ( clientSocket.isConnected() ) {
				String line=inFromGPIB.readLine();

				if ( null == line ) {
					break;
				}
				
				System.err.println("# TCPReaderValhalla2300 received: " + line);

				/* send packet to listeners */
				for ( int i=0 ; i<packetListeners.size(); i++ ) {
					packetListeners.elementAt(i).packetReceivedPower(line);
				}

			}
		} catch ( Exception e ) {
			e.printStackTrace();

		}


		close();
	}


	public TCPReaderValhalla2300(String host, int port) {
		packetListeners = new Vector<ListenerValhalla2300>();
		connected=false;
		this.host=host;
		this.port=port;
	}

	public void close() {
		System.err.println("# Closing TCPReader...");

		try { 
			inFromGPIB.close();
			outToGPIB.close();
			clientSocket.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		connected=false;
	}
}
