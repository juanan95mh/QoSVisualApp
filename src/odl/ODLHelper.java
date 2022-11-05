package odl;

import java.awt.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractSet;
import java.util.Base64;
import java.util.Set;
import java.util.TreeSet;

public class ODLHelper {

	public static int flowCounter = 11;/* 0-99 */

	public static String ipController1 = "192.168.91.3:8181";



	
	public static AbstractSet<OpenflowSwitch> ofSwitches;;
	public static String[] goldPrioTags = { "10", "26" };
	public static String[] silverPrioTags = { "10", "27" };
	public static String[] broncePrioTags = { "10", "28" };

	public static boolean minimumEnviroinment() {
		System.out.println("Preparando entorno mínimo...");
		ofSwitches= new TreeSet<OpenflowSwitch>(); 
		//OvSwitch
		//ofSwitches.add(new OpenflowSwitch("openflow:8796757906330", 1/*Host*/, 2/*MPLS*/, 3/*IP*/));
		//ofSwitches.add(new OpenflowSwitch("openflow:8796756574848", 3/*Host*/, 2/*MPLS*/, 1/*IP*/));
		
		
		//Zodiac FX 3 y 4
		ofSwitches.add(new OpenflowSwitch("openflow:123917682138505", 1/*Host*/, 3/*MPLS*/, 2/*IP*/));
		System.out.println("Añadido Z3 tamaño mapa =" + ofSwitches.size());
		ofSwitches.add(new OpenflowSwitch("openflow:123917682138464", 1/*Host*/, 3/*MPLS*/, 2/*IP*/));
		System.out.println("Añadido Z4 tamaño mapa =" + ofSwitches.size());
		//ofSwitches.add(new OpenflowSwitch("openflow:8796757906330", 1/*Host*/, 2/*MPLS*/, 3/*IP*/));
		//ofSwitches.add(new OpenflowSwitch("openflow:8796757906330", 1/*Host*/, 2/*MPLS*/, 3/*IP*/));
		//ofSwitches.add(new OpenflowSwitch("openflow:8796757906330", 1/*Host*/, 2/*MPLS*/, 3/*IP*/));
		//ofSwitches.add(new OpenflowSwitch("openflow:8796757906330", 1/*Host*/, 2/*MPLS*/, 3/*IP*/));

	
		for (OpenflowSwitch switchAux : ofSwitches) {
			
			for (int i = 0; i <= 100; i++) {
				clearFlow(i, 0, switchAux.getName(), ipController1);// Node 1
			}
			
			preparePop(switchAux.getName(), 0, ipController1, switchAux.getMPLSport(),switchAux.gethostport() , "ff:ff:ff:ff:ff:ff");
			prepareDefaultRouting(switchAux.getName(), 1, ipController1, switchAux.gethostport(), switchAux.getIPport());
			prepareDefaultRouting(switchAux.getName(), 2, ipController1, switchAux.getIPport(), switchAux.gethostport());
		}
		
			

		return true;
	}

	public static boolean onMPLSFail() {
		for (OpenflowSwitch switchAux : ofSwitches) {
			for (int i = 11; i <= 200; i++) {
				clearFlow(i, 0, switchAux.getName(), ipController1);// Node 1
			}
		}
		return true;
	}

	public static boolean onIpFail() {
		String[] tags = { "20", "20" };
		
		int flowId = ODLHelper.flowCounter;
		ODLHelper.flowCounter++;
		
		for (OpenflowSwitch switchAux : ofSwitches) {
			setTunnel(ipController1, switchAux.getName(), tags, "", "", "", "", "", "", switchAux.gethostport(), switchAux.getMPLSport(), 20,flowId);
		}
		return true;
	}

	private static void prepareARPRouting(String node, int flowID, String ipController, int portIN, int portOUT) {
		try {
			URL url = new URL("http://" + ipController + "/restconf/config/opendaylight-inventory:nodes/node/" + node
					+ "/table/0/flow/" + flowID);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// Set the request method to POST as required from the API
			String userpass = "admin:admin";
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestMethod("PUT");
			con.setRequestProperty("host", ipController);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Authorization", basicAuth);
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);

			// Getting output stream
			OutputStream os = con.getOutputStream();
			String comando = new String();
			// making RAW XML
			comando = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + "<flow \n"
					+ "    xmlns=\"urn:opendaylight:flow:inventory\">\n" + "    <instructions>\n"
					+ "        <instruction>\n" + "            <order>0</order>\n" + "            <apply-actions>\n"
					+ "                <action>\n" + "                    <output-action>\n"
					+ "                        <output-node-connector>" + portOUT + "</output-node-connector>\n"
					+ "                        <max-length>60</max-length>\n" + "                    </output-action>\n"
					+ "                    <order>3</order>\n" + "                </action>\n" + "                 \n"
					+ "            </apply-actions>\n" + "        </instruction>\n" + "    </instructions>\n"

					+ "    			<id>" + flowID + "	</id>\n" + "    <strict>false</strict>\n"

					+ "   	<match>\n" + "        <in-port>" + node + ":" + portIN + "</in-port>\n"
					+ "        <ethernet-match>\n" + "          <ethernet-type>\n"
					+ "                <type>2056</type>\n" + "            </ethernet-type>"
					+ "        </ethernet-match> \n" + "    </match>\n" + "    <idle-timeout>0</idle-timeout>\n"
					+ "    <cookie>401</cookie>\n" + "    <cookie_mask>255</cookie_mask>\n"
					+ "    <installHw>false</installHw>\n" + "    <hard-timeout>0</hard-timeout>\n"
					+ "    <priority>10</priority>\n" + "    <table_id>0</table_id>\n" + "</flow>";

			os.write(comando.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			System.out.println("prepareARPRouting-> Node=" + node + " FlowId=" + flowID + " || -> respuesta=" + responseCode);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void prepareDefaultRouting(String node, int flowID, String ipController, int portIN, int portOUT) {
		try {
			URL url = new URL("http://" + ipController + "/restconf/config/opendaylight-inventory:nodes/node/" + node
					+ "/table/0/flow/" + flowID);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// Set the request method to POST as required from the API
			String userpass = "admin:admin";
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestMethod("PUT");
			con.setRequestProperty("host", ipController);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Authorization", basicAuth);
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);

			// Getting output stream
			OutputStream os = con.getOutputStream();
			String comando = new String();
			// making RAW XML
			comando = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + "<flow \n"
					+ "    xmlns=\"urn:opendaylight:flow:inventory\">\n" + "    <instructions>\n"
					+ "        <instruction>\n" + "            <order>0</order>\n" + "            <apply-actions>\n"
					+ "                <action>\n" + "                    <output-action>\n"
					+ "                        <output-node-connector>" + portOUT + "</output-node-connector>\n"
					+ "                        <max-length>60</max-length>\n" + "                    </output-action>\n"
					+ "                    <order>3</order>\n" + "                </action>\n" + "                 \n"
					+ "            </apply-actions>\n" + "        </instruction>\n" + "    </instructions>\n"
					+ "    <id>" + flowID + "</id>\n" + "    <strict>false</strict>\n" + "    <match>\n"
					+ "         <in-port>" + node + ":" + portIN + "</in-port>\n"
					/*
					 * + "        <ethernet-match>" + "          <ethernet-type>\n" +
					 * "                <type>2048</type>\n" + "            </ethernet-type>" +
					 * "        </ethernet-match>"
					 */
					+ "    </match>\n" + "    <idle-timeout>0</idle-timeout>\n" + "    <cookie>401</cookie>\n"
					+ "    <cookie_mask>255</cookie_mask>\n" + "    <installHw>false</installHw>\n"
					+ "    <hard-timeout>0</hard-timeout>\n" + "    <priority>10</priority>\n"
					+ "    <table_id>0</table_id>\n" + "</flow>";

			os.write(comando.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
				System.out.println("Flujo aceptado por el controlador");
			} else {
				System.out.println("prepareDefaultRouting  Node=" + node + " FlowId=" + flowID + " || -> respuesta="
						+ responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void preparePop(String node, int flowID, String ipController, int srcPORT, int dstPORT,
			String hwAddr) {
		try {
			URL url = new URL("http://" + ipController + "/restconf/config/opendaylight-inventory:nodes/node/" + node
					+ "/table/0/flow/" + flowID);
			System.out.println("Configuramos POP 1 etiqueta para nodo " + node + " con flowID=" + flowID);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// Set the request method to POST as required from the API
			String userpass = "admin:admin";
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestMethod("PUT");
			con.setRequestProperty("host", ipController);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Authorization", basicAuth);
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);

			// Getting output stream
			OutputStream os = con.getOutputStream();
			String comando = new String();
			// making RAW XML 34887

			comando = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
					+ "<flow	xmlns=\"urn:opendaylight:flow:inventory\">\n" + "	<strict>false</strict>\n"
					+ "	<instructions>\n" + "		<instruction>\n" + "			<order>0</order>\n"
					+ "			<apply-actions>\n" 
					+ "				<action>\n" 
					+ "              <set-dl-dst-action>"
					+ "                 <address>" + hwAddr + "</address> \n" 
					+ "              </set-dl-dst-action>"
					+ "					<order>0</order>\n" 
					+ "				</action>\n" 
					
					+ "				<action>\n" 
					+ "					<pop-mpls-action>\n"
					+ "						<ethernet-type>34887</ethernet-type>\n"
					+ "					</pop-mpls-action>\n" 
					+ "					<order>1</order>\n"
					+ "				</action>\n"
	

					+ "				<action>\n" 
					+ "					<pop-mpls-action>\n"
				    + "						<ethernet-type>2048</ethernet-type>\n"
					+ "					</pop-mpls-action>\n" 
					+ "					<order>2</order>\n"
					+ "				</action>\n" 
					
					+ "				<action>\n"
					+ "					<output-action>\n" 
					+ "						<output-node-connector>" + dstPORT + "</output-node-connector>\n"
					+ "					<max-length>60</max-length>\n"
					+ "					</output-action>\n" 
					+ "					<order>3</order>\n"
					+ "				</action>\n" 
					
					+ "			</apply-actions>\n" 
					+ "		</instruction>\n"
					+ "	</instructions>\n" + "	<table_id>0</table_id>\n" + "	<id>" + flowID + "</id>\n"
					+ "	<cookie_mask>255</cookie_mask>\n" + "	<match>\n" + "		<ethernet-match>\n"
					+ "            <ethernet-type>\n" + "             	<type>34887</type>\n"
					+ "           </ethernet-type>\n" + "        </ethernet-match>\n" + "		<in-port>" + node + ":"
					+ srcPORT + "</in-port>\n" + "	</match>\n" + "	<hard-timeout>0</hard-timeout>\n"
					+ "	<cookie>1114</cookie>\n" + "	<idle-timeout>0</idle-timeout>\n"
					+ "	<priority>100</priority>\n" + "	<barrier>false</barrier>\n" + "</flow>";
			os.write(comando.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_CREATED && responseCode == HttpURLConnection.HTTP_ACCEPTED) {
				System.out.println("Flujo aceptado por el controlador");
			} else {
				System.out.println("preparePOP > Node=" + node + " FlowId=" + flowID + " || -> respuesta=" + responseCode);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean clearFlow(int flow, int table, String node, String ipController) {

		try {
			URL url = new URL("http://" + ipController + "/restconf/config/opendaylight-inventory:nodes/node/" + node
					+ "/table/" + table + "/flow/" + flow);
			// System.out.println("********************************** ODLHelper:clearFlow()
			// table="+ table+"flow=" + flow + "***************************");

			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Set the request method to POST as required from the API
			String userpass = "admin:admin";
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestMethod("DELETE");
			con.setRequestProperty("host", ipController);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Authorization", basicAuth);
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);

			// Getting output stream
			OutputStream os = con.getOutputStream();
			String comando = new String();
			// making RAW XML

			os.write(comando.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_CREATED && responseCode == HttpURLConnection.HTTP_ACCEPTED) {
				System.out.println("********************************** ODLHelper:clearFlow() table=" + table + "flow="
						+ flow + "***************************");
			} else {
				// System.out.println("respuesta=" + responseCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static boolean setTunnel(String ipController, String node, String[] tags, String ipSRC, String ipDST,
			String udpSRC, String udpDST, String tcpSRC, String tcpDST, int inport, int outport, int prio, int flowId) {


		try {
			URL url = new URL("http://" + ipController + "/restconf/config/opendaylight-inventory:nodes/node/" + node
					+ "/table/0/flow/" + flowId);
			System.out.println(
					"*******************NUEVA PETICION Prioridad IP ***********************************************");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			String mplsLabel1 = tags[0];
			String mplsLabel2 = tags[1];

			String stringMatch = "";

			stringMatch = getMatchXML(ipSRC, ipDST, udpSRC, udpDST, tcpSRC, tcpDST);
			System.out.println("String= " + stringMatch);
			// Set the request method to POST as required from the API
			String userpass = "admin:admin";
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestMethod("PUT");
			con.setRequestProperty("host", ipController);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Authorization", basicAuth);
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);

			// Getting output stream
			OutputStream os = con.getOutputStream();
			String comando = new String();
			// making RAW XML

			comando = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
					+ "<flow	xmlns=\"urn:opendaylight:flow:inventory\">\n" + "	<strict>false</strict>\n"
					+ "	<instructions>\n" + "		<instruction>\n" + "			<order>0</order>\n"
					+ "			<apply-actions>\n" 
					+ "				<action>\n" 
					+ "              <set-dl-dst-action>"
					+ "                 <address> ff:ff:ff:ff:ff:ff </address> \n" 
					+ "              </set-dl-dst-action>"
					+ "					<order>0</order>\n" 
					+ "				</action>\n"
					+ "					<action>\n" 
					+ "                    <push-mpls-action>\n"
					+ "                        <ethernet-type>34887</ethernet-type>\n"
					+ "                    </push-mpls-action>\n" 
					+ "                    <order>1</order>\n"
					+ "                </action>\n"

					+ "                <action>\n" 
					+ "                    <set-field>\n"
					+ "                        <protocol-match-fields>\n" 
					+ "                            <mpls-label>"+ mplsLabel1 + "</mpls-label>\n" 
					+ "                        </protocol-match-fields>\n"
					+ "                    </set-field>\n" 
					+ "                    <order>2</order>\n"
					+ "                </action>"
						
					+ "			       <action>\n" 
					+ "                    <push-mpls-action>\n"
					+ "                        <ethernet-type>34887</ethernet-type>\n"
					+ "                    </push-mpls-action>\n" 
					+ "                    <order>3</order>\n"
					+ "                </action>\n"
					
					+ "                <action>\n" 
					+ "                    <set-field>\n"
					+ "                        <protocol-match-fields>\n" 
					+ "                            <mpls-label>"+ mplsLabel2 + "</mpls-label>\n" 
					+ "                        </protocol-match-fields>\n"
					+ "                    </set-field>\n" 
					+ "                    <order>4</order>\n"
					+ "                </action>"
					
					+ "				   <action>\n" 
					+ "					<output-action>\n"
					+ "						<output-node-connector>" + outport + "</output-node-connector>\n"
					+ "						</output-action>\n" 
					+ "					<order>5</order>\n"
					+ "					</action>\n" 
					+ "			</apply-actions>\n" 
					+ "		</instruction>\n"
					+ "	</instructions>\n" 
					+ "	<table_id>0</table_id>\n" 
					+ "	<id>" + flowId + "</id>\n"
					+ "	<cookie_mask>255</cookie_mask>\n" 
					+ "	<match>\n" + "		<in-port>" + node + ":" + inport
					+ "</in-port>\n" + stringMatch + "	</match>\n" + "	<hard-timeout>0</hard-timeout>\n"
					+ "	<cookie>11814</cookie>\n" + "	<idle-timeout>0</idle-timeout>\n"
					+ "	<priority>100</priority>\n" + "	<barrier>false</barrier>\n" + "</flow>";

			os.write(comando.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_OK) {
				System.out.println("SetTunnel > Node=" + node + " FlowId=" + flowId + " || -> respuesta=" + responseCode);
				return true;
			} else {
				System.out.println("SetTunnel > Node=" + node + " FlowId=" + flowId + " || -> respuesta=" + responseCode);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}

	}

	public static void getNodes(String ipController) {
		try {
			URL url = new URL("http://" + ipController + "/restconf/operational/opendaylight-inventory:nodes");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			// Set the Content-Type to "application/xml" as required from the API
			// con.setRequestProperty("Content-Type", "application/xml");

			con.setDoOutput(true);
			String userpass = "admin:admin";
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes());
			con.setRequestProperty("host", ipController);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Authorization", basicAuth);

			InputStream os = con.getInputStream();

			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String getMatchXML(String ipSRC, String ipDST, String udpSRC, String udpDST, String tcpSRC,
			String tcpDST) {

		String stringReturn = "";

		if (ipSRC != "" || ipDST != "" || udpSRC != "" || udpDST != "" || tcpSRC != "" || tcpDST != "") {
			stringReturn = "  <ethernet-match>\n" + "            <ethernet-type>\n"
					+ "                <type>2048</type>\n" + "            </ethernet-type>\n"
					+ "        </ethernet-match>\n";

			if (ipSRC != "") {
				stringReturn += "<ipv4-source>" + ipSRC + "/32</ipv4-source> ";
			}
			if (ipDST != "") {
				stringReturn += "<ipv4-destination>" + ipDST + "/32</ipv4-destination> ";
			}
			if (udpSRC != "") {
				stringReturn += " <udp-source-port>" + udpSRC + "/32</udp-source-port>";
			}

			if (udpDST != "") {
				stringReturn += " <udp-destination-port>" + udpDST + "/32</udp-destination-port>";
			}
			if (tcpSRC != "") {
				stringReturn += " <udp-source-port>" + tcpSRC + "/32</udp-source-port>";
			}

			if (tcpDST != "") {
				stringReturn += " <udp-destination-port>" + tcpDST + "/32</udp-destination-port>";
			}

		}

		return stringReturn;
	}

}