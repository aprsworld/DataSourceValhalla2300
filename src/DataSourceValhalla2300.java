import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

import javax.swing.*;


public class DataSourceValhalla2300 implements ListenerValhalla2300 {
	public static final boolean debug=false;

	public String gpibHostname;
	public int gpibPort;
	public String outPrefix;
	
	/* GUI stuff */
	public static final boolean gui=true;
	protected JLabel labelCurrentValue;




	/* fire it off via TCP/IP */
	public void packetReceivedPower(String line) {
		line = line.trim();
		if ( ',' == line.charAt(line.length()-1) ) {
			line=line.substring(0,line.length()-1);
		}

		//		System.out.println("# We received (and trimmed) -> '" + line + "'");

		double d = Double.parseDouble(line);

		//		System.out.println("# double value: " + d);

		System.out.println(outPrefix + d);
		
		if ( gui ) {
			labelCurrentValue.setText(d + " watts");
		}
	}


	protected void setupGUI() {
		JFrame frame = new JFrame("DataSourceValhalla2300 for " + gpibHostname + ":" + gpibPort);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add the ubiquitous "Hello World" label.
		labelCurrentValue = new JLabel("Waiting for data...");
		labelCurrentValue.setFont(new Font("Serif", Font.PLAIN, 64));
		frame.getContentPane().add(labelCurrentValue);

		//Display the window.
		frame.setMinimumSize(new Dimension(400,150));
		frame.pack();
		
		frame.setVisible(true);
	}

	public void run(String[] args) throws IOException {
		gpibHostname="192.168.10.243";
		gpibPort=1234;
		outPrefix="W";


		if ( args.length < 3 ) {
			System.err.println("usage: DataSourceValhalla2300 OUTPUT_PREFIX GPIB_HOSTNAME GPIB_PORT");
			System.exit(1);
		}

		if ( args.length >= 3 ) {
			outPrefix=args[0];
			gpibHostname=args[1];
			gpibPort=Integer.parseInt(args[2]);
		}

		System.err.println("# connecting to Valhalla 2300 via ProLogix GPIB to Ethernet adapter at " + 
				gpibHostname + ":" + gpibPort);
		System.err.println("# prefixing data with '" + outPrefix + "' before sending to DataGS");


		if ( gui ) {
			setupGUI();
		}


		TCPReaderValhalla2300 v = new TCPReaderValhalla2300(gpibHostname, gpibPort);
		v.addPacketListener(this);
		v.run();

	}

	public static void main(String[] args) throws IOException {
		System.err.println("# Major version: 2014-11-21 (precision)");
		System.err.println("# java.library.path: " + System.getProperty( "java.library.path" ));

		DataSourceValhalla2300 d = new DataSourceValhalla2300();
		d.run(args);
	}




}
