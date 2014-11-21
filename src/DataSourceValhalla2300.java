import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;


public class DataSourceValhalla2300 extends Thread implements ListenerValhalla2300  {
	public static final boolean debug=false;

	public String gpibHostname;
	public int gpibPort;

	public String dataGSHostname;
	public int dataGSPort;

	public String outPrefix;	
	private long nPackets=0;

	private TCPWriter dataGS;

	/* GUI stuff */
	public static final boolean gui=true;
	protected JLabel labelCurrentValue;
	protected JLabel labelNPackets;




	/* fire it off via TCP/IP */
	public void packetReceivedPower(String line) {
		nPackets++;
		
		if ( null == line )
			return;
		
		line = line.trim();
		if ( line.length() > 1 && ',' == line.charAt(line.length()-1) ) {
			line=line.substring(0,line.length()-1);
		}

		//		System.out.println("# We received (and trimmed) -> '" + line + "'");

		double d;
		
		try { 
			d = Double.parseDouble(line);
		} catch ( Exception e ) {
			System.err.println("# Error parsing double from line '" + line + "'");
			return;
		}

		//		System.out.println("# double value: " + d);

		System.out.println(outPrefix + d);


		if ( gui ) {
			labelCurrentValue.setText(d + " watts");
			labelNPackets.setText( NumberFormat.getNumberInstance(Locale.US).format(nPackets) + " total packets");
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
		
		labelNPackets = new JLabel("Waiting for data...");
		labelNPackets.setFont(new Font("Serif", Font.PLAIN, 64));
		frame.getContentPane().add(labelNPackets);

		//Display the window.
		frame.setLayout(new FlowLayout());
		frame.setMinimumSize(new Dimension(150,200));
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

		/* startup messages to stderr */
		System.err.println("# connecting to Valhalla 2300 via ProLogix GPIB to Ethernet adapter at " + 
				gpibHostname + ":" + gpibPort);
		if ( null != dataGS ) {
			System.err.println("# sending to DataGS at " + dataGSHostname + ":" + dataGSPort);
		}
		System.err.println("# prefixing data with '" + outPrefix + "' before sending to DataGS");


		if ( gui ) {
			setupGUI();
		}


		TCPReaderValhalla2300 v = new TCPReaderValhalla2300(gpibHostname, gpibPort);
		
		if ( null != dataGS ) {
			System.err.print("# opening connection to DataGS host ... ");
			dataGS.start();
			System.err.println("done");
		}

		System.err.print("# opening connection to GPIB .... ");
		v.start();
		
		/* wait for connection */
		while ( ! v.isConnected() ) {
			System.err.print('^');
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/* send initialization string */
		v.sendLine("++addr 11\r\n"); /* instrument address 11 */
		v.sendLine("++auto\r\n");    /* auto read */
		v.sendLine("++llo\r\n");     /* remote operation ... lockout local */
		v.sendLine("V3\r\n");        /* 60 volts */
		v.sendLine("I6\r\n");        /* 20 amps */
		v.sendLine("W4\r\n");        /* 3 phase, 4 wire */
		v.sendLine("T2\r\n");        /* read current watts */

		/* start receiving data */
		v.addPacketListener(this);
		


		
		System.err.println("done");
		




	}

	public static void main(String[] args) throws IOException {
		System.err.println("# Major version: 2014-11-21 (precision)");
		System.err.println("# java.library.path: " + System.getProperty( "java.library.path" ));

		DataSourceValhalla2300 d = new DataSourceValhalla2300();
		d.run(args);
	}




}
