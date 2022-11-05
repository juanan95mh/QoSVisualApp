package odl;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class QoSVisualAPP {

	public static void main(String[] args) {

		// Tabla donde pintaremos los flujos añadidos
		JTable flowTable = new JTable();

		DefaultTableModel model = new DefaultTableModel(0, 0);
		String header[] = new String[] { "FlowId", "Criterio", "Calidad", "Etiquetas MPLS asociadas" };
		model.setColumnIdentifiers(header);
		flowTable.setModel(model);

		// Ventana
		// IP************************************************************************************************************************************************************
		JFrame ipFrame = new JFrame("IP");
		ipFrame.setSize(300, 150);

		ipFrame.setLayout(new GridLayout(1, 1));
		JPanel panelIP = new JPanel();
		panelIP.setLayout(new FlowLayout());
		JTextField textfieldIP = new JTextField("192.168.140.2", 15);
		JTextField textfieldIPtags = new JTextField(
				"(Rellenar solo en manual)Introduce las etiquetas separadas por comas...", 15);

		JComboBox<String> jComboBox1IP;
		jComboBox1IP = new JComboBox<>();
		jComboBox1IP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "IP origen", "IP destino" }));

		JComboBox<String> jComboBox2IP;
		jComboBox2IP = new JComboBox<>();
		jComboBox2IP.setModel(new javax.swing.DefaultComboBoxModel<>(
				new String[] { "Calidad Oro", "Calidad Plata", "Calidad Bronce", "Calidad Manual" }));

		JButton botonIP = new JButton("OK");
		botonIP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] tags = { "n/a", "n/a" };

				System.out.println("QoSVisualAPP:main Prioridad IP pulsada");
				if (jComboBox2IP.getSelectedItem() == "Calidad Oro") {
					System.out.println("QoSVisualAPP:Calidad ORO");
					tags = ODLHelper.goldPrioTags;
				} else if (jComboBox2IP.getSelectedItem() == "Calidad Plata") {
					System.out.println("QoSVisualAPP:Calidad Plata");
					tags = ODLHelper.silverPrioTags;
				} else if (jComboBox2IP.getSelectedItem() == "Calidad Bronce") {
					System.out.println("QoSVisualAPP:Calidad Bronce");
					tags = ODLHelper.broncePrioTags;
				} else {
					tags = textfieldIPtags.toString().split(",");
				}

				String ipSRC = "";
				String ipDST = "";

				if (jComboBox1IP.getSelectedItem().toString() == "IP origen") {
					ipSRC = textfieldIP.getText();
				} else {
					if (jComboBox1IP.getSelectedItem().toString() == "IP destino") {
						ipDST = textfieldIP.getText();
					}
				}
				
				int flowId = ODLHelper.flowCounter;
				ODLHelper.flowCounter++;
		
				
				boolean flowOk=true; 

				for (OpenflowSwitch switchAux : ODLHelper.ofSwitches) {
					flowOk = ODLHelper.setTunnel(ODLHelper.ipController1, switchAux.getName(), tags, ipSRC, ipDST, "", "","", "", switchAux.gethostport(),switchAux.getMPLSport(), 100,flowId) && flowOk  ;
				}

				
				if (flowOk == true) {
					model.addRow(new String[] { Integer.toString(flowId),
							jComboBox1IP.getSelectedItem().toString() + " = " + textfieldIP.getText(),
							jComboBox2IP.getSelectedItem().toString(), tags[0] + " , " + tags[1] });
				}else {
					System.out.println("SetTunnel fallido por lo que borramos todos los flujos");
					for (OpenflowSwitch switchAux : ODLHelper.ofSwitches) {
						ODLHelper.clearFlow(flowId, 0, switchAux.getName(), ODLHelper.ipController1);	
					}
				}
				
				
			}
		});
		panelIP.add(jComboBox1IP);
		panelIP.add(jComboBox2IP);
		panelIP.add(textfieldIP);
		panelIP.add(textfieldIPtags);
		panelIP.add(botonIP);
		ipFrame.getContentPane().add(panelIP);
		// fin IP frame
		// *********************************************************************************************************************************************************

		// Ventana
		// Port**********************************************************************************************************************************************************
		JFrame portFrame = new JFrame("Port");
		portFrame.setSize(400, 130);
		// ipFrame.setResizable(false);
		ipFrame.setLayout(new GridLayout(1, 1));
		JPanel panelPort = new JPanel();
		panelIP.setLayout(new FlowLayout());
		JTextField textfieldPort1 = new JTextField("8080", 15);
		JTextField textfieldPort2 = new JTextField(
				"(Rellenar solo en manual)Introduce las etiquetas separadas por comas...", 15);

		JComboBox<String> jComboBox1Port;
		jComboBox1Port = new JComboBox<>();
		jComboBox1Port
				.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Puerto origen", "Puerto destino" }));

		JComboBox<String> jComboBox2Port;
		jComboBox2Port = new JComboBox<>();
		jComboBox2Port.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UDP", "TCP" }));

		JComboBox<String> jComboBox3Port;
		jComboBox3Port = new JComboBox<>();
		jComboBox3Port.setModel(new javax.swing.DefaultComboBoxModel<>(
				new String[] { "Calidad Oro", "Calidad Plata", "Calidad Bronce", "Calidad Manual" }));

		JButton botonPortOk = new JButton("OK");
		botonPortOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] tags = { "n/a", "n/a" };
				model.addRow(new String[] {
						jComboBox1Port.getSelectedItem().toString() + " " + jComboBox2Port.getSelectedItem() + " = "
								+ textfieldPort1.getText(),
						jComboBox3Port.getSelectedItem().toString(), tags[0] + " , " + tags[1] });
			}
		});
		panelPort.add(jComboBox1Port);
		panelPort.add(jComboBox2Port);
		panelPort.add(jComboBox3Port);
		panelPort.add(textfieldPort1);
		panelPort.add(textfieldPort2);
		panelPort.add(botonPortOk);
		portFrame.getContentPane().add(panelPort);
		// fin Port frame
		// *******************************************************************************************************************************************************

		// Ventana
		// Service*******************************************************************************************************************************************************
		JFrame serviceFrame = new JFrame("Service");
		serviceFrame.setSize(400, 130);
		// ipFrame.setResizable(false);
		ipFrame.setLayout(new GridLayout(1, 1));
		JPanel panelService = new JPanel();
		panelIP.setLayout(new FlowLayout());

		JTextField textfieldService1 = new JTextField(
				"(Rellenar solo en manual)Introduce las etiquetas separadas por comas...", 15);

		JComboBox<String> jComboBox1Service;
		jComboBox1Service = new JComboBox<>();
		jComboBox1Service.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ssh", "ftp", "sip" }));

		JComboBox<String> jComboBox2Service;
		jComboBox2Service = new JComboBox<>();
		jComboBox2Service.setModel(new javax.swing.DefaultComboBoxModel<>(
				new String[] { "Calidad Oro", "Calidad Plata", "Calidad Bronce", "Calidad Manual" }));

		JButton botonServiceOk = new JButton("OK");
		botonServiceOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] tags = { "n/a", "n/a" };
				model.addRow(new String[] { "Servicio " + jComboBox1Service.getSelectedItem().toString(),
						jComboBox2Service.getSelectedItem().toString(), tags[0] + " , " + tags[1] });

			}
		});
		panelService.add(jComboBox1Service);
		panelService.add(jComboBox2Service);
		panelService.add(textfieldService1);
		panelService.add(botonServiceOk);
		serviceFrame.getContentPane().add(panelService);
		// fin Service frame
		// ***************************************************************************************************************************************************

		// Ventana dscp
		// mapping*******************************************************************************************************************************************************
		JFrame dscpFrame = new JFrame("DSCP");
		dscpFrame.setSize(400, 130);
		// ipFrame.setResizable(false);
		ipFrame.setLayout(new GridLayout(1, 1));
		JPanel panelDSCP = new JPanel();
		panelDSCP.setLayout(new FlowLayout());

		JTextField textfieldDSCP = new JTextField(
				"(Rellenar solo en manual)Introduce las etiquetas separadas por comas...", 15);

		JComboBox<String> jComboBox1DSCP;
		jComboBox1DSCP = new JComboBox<>();
		jComboBox1DSCP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Network Control", "Telephony" ,"Signaling" , 
				"Multimedia Conferencing" , 
				"Real-Time Interactive" , 
				"Multimedia Streaming" , 
				"Broadcast Video" , 
				"Low-Latency Data" , 
				"OAM" , 
				"High-Throughput Data" , 
				"Standard" }));

		JComboBox<String> jComboBox2DSCP;
		jComboBox2DSCP = new JComboBox<>();
		jComboBox2DSCP.setModel(new javax.swing.DefaultComboBoxModel<>(
				new String[] { "Calidad Oro", "Calidad Plata", "Calidad Bronce", "Calidad Manual" }));

		JButton botonDSCPOk = new JButton("OK");
		botonDSCPOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] tags = { "n/a", "n/a" };
				model.addRow(new String[] { "Servicio " + jComboBox1DSCP.getSelectedItem().toString(),
						jComboBox2DSCP.getSelectedItem().toString(), tags[0] + " , " + tags[1] });

			}
		});
		panelDSCP.add(jComboBox1DSCP);
		panelDSCP.add(jComboBox2DSCP);
		panelDSCP.add(textfieldDSCP);
		panelDSCP.add(botonDSCPOk);
		dscpFrame.getContentPane().add(panelDSCP);
		// fin DSCP mapping frame
		// ***************************************************************************************************************************************************

		// Ventana
		// DELETE************************************************************************************************************************************************************
		JFrame deleteFrame = new JFrame("Delete");
		deleteFrame.setSize(300, 90);

		deleteFrame.setLayout(new GridLayout(1, 1));
		JPanel panelDelete = new JPanel();
		panelDelete.setLayout(new FlowLayout());
		JTextField textfieldDelete = new JTextField("Borrar criterio con ID...", 15);

		JButton botonDelete = new JButton("OK");
		botonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int flowId = Integer.parseInt(textfieldDelete.getText());
				for (OpenflowSwitch switchAux : ODLHelper.ofSwitches) {
					ODLHelper.clearFlow(flowId,0/*table*/, switchAux.getName(), ODLHelper.ipController1);
				}
				System.out.println("QoSVisualAPP:main Delete pulsada");

				// Borrar de la tabla los flujos encontrados con prioridad 11 flowId
				int column = model.findColumn("FlowId");
				int row = -1;
				for (int i = 0; i < model.getRowCount(); i++) {
					if (model.getValueAt(i, column).toString().equals( textfieldDelete.getText())) {
						row = i;
					}
				}
				if (row >= 0) {
					model.removeRow(row);
				}
			}
		});
		panelDelete.add(textfieldDelete);
		panelDelete.add(botonDelete);
		deleteFrame.getContentPane().add(panelDelete);
		// fin delete frame
		// *********************************************************************************************************************************************************

		JFrame mainFrame = new JFrame("SD-WAN QoS App");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(750, 300);
		// mainFrame.setResizable(false);
		GridLayout experimentLayout = new GridLayout(1, 1);
		mainFrame.setLayout(experimentLayout);
		JPanel panelPrincipal = new JPanel();

		panelPrincipal.setLayout(new FlowLayout());

		// Boton IP
		JButton botonVentanaIP = new JButton("Prioridad de IP");
		botonVentanaIP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ipFrame.setVisible(true);
			}
		});
		panelPrincipal.add(botonVentanaIP);

		// Boton prioridad por puerto
		JButton botonVentanaPuerto = new JButton("Prioridad por puerto");
		botonVentanaPuerto.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				portFrame.setVisible(true);
			}
		});
		panelPrincipal.add(botonVentanaPuerto);

		// Boton prioridad servicios predefinidos
		JButton botonVentanaServicio = new JButton("Prioridad por servicio");
		botonVentanaServicio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				serviceFrame.setVisible(true);
			}
		});
		panelPrincipal.add(botonVentanaServicio);

		// Activar DSCP maping
		JButton botonVentanaDSCP = new JButton("Activar mapeo DSCP");
		// botonIP.addActionListener(};
		botonVentanaDSCP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dscpFrame.setVisible(true);
			}
		});
		panelPrincipal.add(botonVentanaDSCP);

		// Botón delete
		JButton botonDel = new JButton("Delete flow");
		// botonIP.addActionListener(};
		botonDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteFrame.setVisible(true);
			}
		});
		panelPrincipal.add(botonDel);

		// Botón delete
		JButton botonClear = new JButton("Clear flows");
		// botonIP.addActionListener(};
		botonClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i=11; i<65 ;i++ ) {

					for (OpenflowSwitch switchAux : ODLHelper.ofSwitches) {
						ODLHelper.clearFlow(i,0, switchAux.getName(), ODLHelper.ipController1);
					}
					
					int column = model.findColumn("FlowId");
					int row = -1;
					for (int j = 0; j < model.getRowCount(); j++) {
						if ( Integer.parseInt(model.getValueAt(j, column).toString()) == i ) {
							row = j;
							System.out.println("Encontrado elemento para eliminar");
						}
					}
					if (row >= 0) {
						model.removeRow(row);
					}
				}
			}
		}
				);
		panelPrincipal.add(botonClear);
		
		mainFrame.getContentPane().add(panelPrincipal);
		mainFrame.getContentPane().add(new JScrollPane(flowTable));
		mainFrame.setVisible(true);
		
		ODLHelper.minimumEnviroinment();

	}

}