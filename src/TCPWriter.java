import java.io.*;
import java.net.*;




public class TCPWriter extends Thread {
	protected Boolean connected;
	protected String host;
	protected int port;

	private BufferedReader in;
	private DataOutputStream out;
	private Socket clientSocket=null;
	
	public boolean sendLine(String s) {
		try { 
			out.writeBytes(s);
		} catch ( Exception e ) {
			return false;
		}
		return true;
	}
	

	public boolean isConnected() {
		if ( null == clientSocket )
			return false;
		
		return clientSocket.isConnected();
	}
	
	public void run() {
		System.err.println("-- TCPWriter run() --");
		
		/* connect and open socket */
		try {
			clientSocket = new Socket(host, port);
			connected = clientSocket.isConnected();
			
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new DataOutputStream(clientSocket.getOutputStream());


			while ( clientSocket.isConnected() ) {
				String line=in.readLine();

				if ( null == line )
					break;
				
				System.err.println("# TCPWriter received: " + line);

			}
		} catch ( Exception e ) {
			e.printStackTrace();

		}


		close();
	}


	public TCPWriter(String host, int port) {
		connected=false;
		this.host=host;
		this.port=port;
	}

	public void close() {
		System.err.println("# Closing TCPWriter...");

		try { 
			in.close();
			out.close();
			clientSocket.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		connected=false;
	}
}
