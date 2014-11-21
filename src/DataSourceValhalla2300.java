import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.*;


public class DataSourceValhalla2300 extends Thread implements ListenerValhalla2300  {
	public static final boolean debug=false;

	public String gpibHostname;
	public int gpibPort;

	public String dataGSHostname;
	public int dataGSPort;

	public String outPrefix;

	private TCPWriter dataGS;

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

		/* send to DataGS ... kill program if we can't */
		if ( null != dataGS && dataGS.isConnected() ) {
			if ( ! dataGS.sendLine(outPrefix + d + "\n") ) {

				System.err.println("# dataGS was not able to send. Killing program.");
				System.exit(2);
			}
		}
	}


	protected void setupGUI() {
		JFrame frame = new JFrame("DataSourceValhalla2300 for " + gpibHostname + ":" + gpibPort);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		labelCurrentValue = new JLabel("Waiting for data...");
		labelCurrentValue.setFont(new Font("Serif", Font.PLAIN, 64));
		frame.getContentPane().add(labelCurrentValue);

		//Display the window.
		frame.setLayout(new FlowLayout());
		frame.setMinimumSize(new Dimension(400,150));
		frame.pack();

		frame.setVisible(true);
	}

	public void run(String[] args) throws IOException {
		gpibHostname="192.168.10.243";
		gpibPort=1234;
		outPrefix="W";
		dataGS=null;

		if ( args.length < 3 ) {
			System.err.println("usage: DataSourceValhalla2300 OUTPUT_PREFIX GPIB_HOSTNAME GPIB_PORT DATAGS_HOSTNAME DATAGS_PORT");
			System.exit(1);
		}

		if ( args.length >= 3 ) {
			outPrefix=args[0];
			gpibHostname=args[1];
			gpibPort=Integer.parseInt(args[2]);
		}
		if ( args.length >= 5 ) {
			dataGSHostname=args[3];
			dataGSPort=Integer.parseInt(args[4]);
			dataGS = new TCPWriter(dataGSHostname, dataGSPort);
		}

		System.err.println("# connecting to Valhalla 2300 via ProLogix GPIB to Ethernet adapter at " + 
				gpibHostname + ":" + gpibPort);
		System.err.println("# sending to DataGS at " + dataGSHostname + ":" + dataGSPort);
		System.err.println("# prefixing data with '" + outPrefix + "' before sending to DataGS");


		if ( gui ) {
			setupGUI();
		}


		TCPReaderValhalla2300 v = new TCPReaderValhalla2300(gpibHostname, gpibPort);
		v.addPacketListener(this);

		if ( null != dataGS ) {
			System.err.print("# opening connection to DataGS host ... ");
			dataGS.start();
			System.err.println("done");
		}

		System.err.print("# opening connection to GPIB .... ");
		v.run();
		System.err.println("done");
		




	}

	public static void main(String[] args) throws IOException {
		System.err.println("# Major version: 2014-11-21 (precision)");
		System.err.println("# java.library.path: " + System.getProperty( "java.library.path" ));

		DataSourceValhalla2300 d = new DataSourceValhalla2300();
		d.run(args);
	}




}
