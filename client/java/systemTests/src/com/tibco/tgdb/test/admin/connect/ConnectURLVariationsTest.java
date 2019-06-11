package com.tibco.tgdb.test.admin.connect;

import org.testng.annotations.Test;

import com.tibco.tgdb.test.lib.TGAdmin;
import com.tibco.tgdb.test.lib.TGAdminException;
import com.tibco.tgdb.test.lib.TGGeneralException;
import com.tibco.tgdb.test.lib.TGInitException;
import com.tibco.tgdb.test.lib.TGServer;
import com.tibco.tgdb.test.utils.ClasspathResource;
import com.tibco.tgdb.test.utils.PipedData;

import bsh.EvalError;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeGroups;


/**
 * Copyright 2018 TIBCO Software Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. A copy of the License is
 * included in the distribution package with this file. You also may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

public class ConnectURLVariationsTest {

	private static TGServer tgServer;
	private static String tgHome = System.getProperty("TGDB_HOME");
	private static String tgWorkingDir = System.getProperty("TGDB_WORKING", tgHome + "/test");

	final private String adminConnectSuccessMsg = "Successfully connected to server";

	/**
	 * Init TG server before test suite
	 * 
	 * @throws Exception
	 */
	@BeforeSuite(description = "Init TG Admin")
	public void initServer() throws Exception {
		TGServer.killAll(); // Clean up everything first
		File initFile = ClasspathResource.getResourceAsFile(this.getClass().getPackage().getName().replaceFirst("\\.[a-z]*$", "").replace('.', '/')
						+ "/initdb.conf", tgWorkingDir + "/initdb.conf");
		tgServer = new TGServer(tgHome);
		try {
			tgServer.init(initFile.getAbsolutePath(), true, 60000);
			System.out.println(tgServer.getBanner());
		} catch (TGInitException ie) {
			System.out.println(ie.getOutput());
			throw ie;
		}
		/*tgServer.setConfigFile(getConfigFile());
		tgServer.start(15000);*/
	}
	
	/**
	 * Start TG Sever before 
	 * 
	 * @throws Exception
	 */
	@BeforeGroups("default")
	public void startServer() throws Exception {
		tgServer.setConfigFile(getConfigFile());
		//tgServer.start(10000);
		tgServer.start(15000);
	}
	
	/**
	 * Stop TG Server
	 * 
	 * @throws Exception
	 */
	@AfterGroups("default")
	public void stopServer() throws Exception {
		tgServer.kill();
		// Backup log file before moving to next test
		File logFile = tgServer.getLogFile();
		File backLogFile = new File(logFile + ".adminconnection");
		Files.copy(logFile.toPath(), backLogFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Start TG Server before port changed test
	 * 
	 * @throws Exception
	 */
	@BeforeGroups("otherPorts")
	public void startServerWithOtherPorts() throws Exception {
		File confFile = ClasspathResource.getResourceAsFile(this.getClass().getPackage().getName().replace('.', '/') + "/diffPorts.conf", tgWorkingDir + "/diffPorts.conf");
		tgServer.setConfigFile(confFile);
		//tgServer.start(10000);
		tgServer.start(15000);
	}
	
	/**
	 * Stop TG Server before port changed test
	 * 
	 * @throws Exception
	 */
	@AfterGroups("otherPorts")
	public void stopServerWithOtherPorts() throws Exception {
		tgServer.kill();
		// Backup log file before moving to next test
		File logFile = tgServer.getLogFile();
		File backLogFile = new File(logFile + ".diffports");
		Files.copy(logFile.toPath(), backLogFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	/************************
	 * 
	 * Test Cases
	 * 
	 ************************/

	/**
	 * testIPv6Connect - Connect TG Admin to TG Server via IPv6
	 * 
	 * @param host
	 * @param port 
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv6Data", 
		  groups = "default",
		  description = "Connect TG Admin to TG Server via IPv6")
	public void testIPv6ConnectWOPort(String host, int port) throws Exception {

		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");

		// Start admin console and connect via IPv6


		String console = "";

		try {
			console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://[" + host + "]", tgServer.getSystemUser(),
					tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv6.log", null, cmdFile.getAbsolutePath(), -1,
					10000);
			Assert.fail("Expected a TGAdminException due to port was not sent");
		} catch (TGAdminException e) {
			Assert.assertFalse(console.contains(adminConnectSuccessMsg),
					"Admin connect to server even when URL doesn't have the port");

		}

	}

	/**
	 * Trying to connect to TGDB-admin without port and host
	 * 
	 * @throws IOException
	 */
	@Test(groups = "default", description = "Trying to connect  TG Admin w/o host and port")
	public void noHostAndPortSpecifiedIPv6() throws IOException {
		File cmdFile = ClasspathResource.getResourceAsFile(this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",tgWorkingDir + "/Connection.cmd");
		// Start admin console and try to connect via IPv6 without host and port

		String console = "";
		try {
			console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://scott@[]", tgServer.getSystemUser(),tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv6.log", null, cmdFile.getAbsolutePath(), -1, 10000);
			Assert.fail("Expected a TGAdminException due to host and port were not sent");
		} catch (TGAdminException e) {
			Assert.assertFalse(console.contains(adminConnectSuccessMsg),"Admin connect to server even when URL doesn't have the port");

		}
	}	

	/**
	 * testIPv4Connect - Trying to connect TG Admin to TG Server via IPv4 without port
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv4Data",
		  groups = "default",
		  description = "Trying to connect TG Admin to TG Server via IPv4 without port")
	public void testIPv4ConnectWOPort(String host, int port) throws Exception {

		File cmdFile = ClasspathResource.getResourceAsFile(this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",tgWorkingDir + "/Connection.cmd");

		// Start admin console and connect via IPv4

		String console = "";
		try {
			console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://" + host, tgServer.getSystemUser(),tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv4.log", null, cmdFile.getAbsolutePath(), -1,10000);
			Assert.fail("Expected a TGAdminException due to wrong connection variation but did not get it");
		} catch (TGAdminException e) {
			Assert.assertFalse(console.contains(adminConnectSuccessMsg),"TGAdmin - Admin could not connect to server tcp://" + host + " with user root");
		}
	}

	
	/**
	 * testIPv4Connect - Trying to connect TG Admin to TG Server via IPv4 without port and host
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv4Data",
		  groups = "default",
		  description = "Trying to connect TG Admin to TG Server via IPv4 without port and host")
	public void testIPv4ConnectWOPortHost(String host, int port) throws Exception {

		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");

		// Start admin console and connect via IPv4
		
		String console = "";
		try { 
			console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://scott@", tgServer.getSystemUser(), tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv4.log", null, cmdFile.getAbsolutePath(), -1, 10000); 
			Assert.fail("Expected a TGAdminException due to wrong connection variation but did not get it");
		} 
		catch (TGAdminException e) {
			Assert.assertFalse(console.contains(adminConnectSuccessMsg), "TGAdmin - Admin could not connect to server tcp://scott@ with user root");
		}
	}
	
	/***
	 * Connection to TG Admin using properties with colon ":"
	 * 
	 * @throws Exception 
	 */
	
	@Test(groups = "default", description = "Trying to connect using an URL with : in the properties")
	public void connectPropsWithColon() throws Exception {
		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");
		String console = "";

		console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://[::1:8223]" , tgServer.getSystemUser(),
					tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv6.log", null, cmdFile.getAbsolutePath(), -1,
					10000);
		
		Assert.assertTrue(console.contains(adminConnectSuccessMsg),"TG Admin did not connect using this URL tcp://[::1:8223]");
	}
	
	/**
	 * testWrongUrlArgument - Try connecting TG Admin to TG Server via IPv4 with wrong url argument
	 * 
	 * @param url
	 * @throws Exception
	 */
	@Test(dataProvider = "wrongUrlData",
		  groups = "default",
		  description = "Try connecting TG Admin to TG Server via IPv4 with wrong url argument")
	public void testWrongUserPwd(String url) throws Exception {


		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");
		String netInt = "";
		boolean windows = (System.getProperty("os.name").contains("Windows"))? true:false;
		
		Enumeration<NetworkInterface> nets =  NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface nif : Collections.list(nets)) {
			if(!nif.getDisplayName().contains("lo"))
				netInt = "%" + nif.getDisplayName();
			
		}
		if(!windows && url.contains("fe80")) {
			 url = url.replace(":8223", netInt + ":8223");
		}
		
		String console = "";
		
		try {
			// Start admin console and connect via IPv4 with wrong url argument
			console = TGAdmin.invoke(tgServer.getHome().toString(), url, tgServer.getSystemUser(), tgServer.getSystemPwd(), tgWorkingDir + "/admin.wrongurl.log", null,
				cmdFile.getAbsolutePath(), -1, 10000);
			System.out.println(console);
			Assert.fail("Expected a TGAdminException due to wrong url argument but did not get it");
		}
		catch(TGAdminException e) { 
			Assert.assertFalse(console.contains(adminConnectSuccessMsg), "Admin did not connect to server with wrong url");
		}
	}
	
	
	
	/**
	 * Trying to connect TG Admin to TG Server via remote
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	@Test(dataProvider = "remoteServers",
		  groups = "default",
		  description = "connecting to a remote server by Ipv4 and IPv6")
	public void connectRemoteServer(String host, String port,String connect) throws Exception {
		String netInt = "";//store the network interface
		String url; //store the url
		boolean expectedPass = (connect.equalsIgnoreCase("true"))?true:false;
		boolean windows = (System.getProperty("os.name").contains("Windows"))? true:false; //Detecting if operating system is windows.
		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");
		
		Enumeration<NetworkInterface> nets =  NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface nif : Collections.list(nets)) {
			if(!nif.getDisplayName().contains("lo"))
				netInt = "%" + nif.getDisplayName();	
		}
		
		String console = "";
		// Creating the URL to connect to server, this depends from the operating system.
		if(windows) {
			 url = (host.length()>11)?"tcp://[" + host + ":" + port + "]": "tcp://" + host + ":" + port ;
		}
		else {
			 url = (host.length()>11)?"tcp://[" + host + netInt + ":" + port + "]": "tcp://" + host + ":" + port ;
		}

//		System.out.println(url);
		try {
		console = TGAdmin.invoke(tgServer.getHome().toString(), url, tgServer.getSystemUser(),
					tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv6.log", null, cmdFile.getAbsolutePath(), -1,
					150000);
		
//			if((!host.equalsIgnoreCase("fe80::797e:c056:c735:5359") & !port.equalsIgnoreCase("8223")) | (!host.equalsIgnoreCase("172.16.1.14") & !port.equalsIgnoreCase("8222"))){
//				Assert.assertTrue(console.contains(adminConnectSuccessMsg), "Expected successful message");
//			}
		if(expectedPass==false) {
			Assert.assertTrue(console.contains(adminConnectSuccessMsg), "Expected failure message");
		}
		
		}catch(Exception e) {
//			if((host.equalsIgnoreCase("fe80::797e:c056:c735:5359") & port.equalsIgnoreCase("8223")) | (host.equalsIgnoreCase("172.16.1.14") & port.equalsIgnoreCase("8222"))){
//				Assert.fail("Expected successful message");
//			}
			if(expectedPass==true) {
				Assert.fail("Expected successful message -> " + url);
			}
			System.out.println("Correct, this should not connect: -> " + url);
			Assert.assertFalse(console.contains(adminConnectSuccessMsg), "TGAdmin - Admin could not connect to server tcp://" + host +"and " + port + "with user root");
		}

			
	}
	
	/**
	 * testIPv4Connect - Trying to connect TG Admin to TG Server via IPv4 with port 8224
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv4Data",
		  groups = "otherPorts",
		  priority = 1,
		  description = "Trying to connect TG Admin to TG Server via IPv4 with port 8224")
	public void testIPv4ConnectWithDiffPort(String host, int port) throws Exception {

		File cmdFile = ClasspathResource.getResourceAsFile(this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",tgWorkingDir + "/Connection.cmd");
		
		// Start admin console and connect via IPv4

		String console = "";
		port = 8224;
		
		console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://" + host + ":" + port, tgServer.getSystemUser(),tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv4.log", 
				null, cmdFile.getAbsolutePath(), -1,10000);
		
		Assert.assertTrue(console.contains(adminConnectSuccessMsg),"TG Admin connect!");

	}
	
	/**
	 * testIPv4Connect - Trying to connect TG Admin to TG Server via IPv4 with port 8224
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv6Data",
		  groups = "otherPorts",
		  priority = 2,
		  description = "Trying to connect TG Admin to TG Server via IPv6 with port 8225")
	public void testIPv6ConnectWithDiffPort(String host, int port) throws Exception {

		File cmdFile = ClasspathResource.getResourceAsFile(this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",tgWorkingDir + "/Connection.cmd");
		String netInt = "";
		String url;
		port = 8225;
		boolean windows = (System.getProperty("os.name").contains("Windows"))? true:false;
		
		Enumeration<NetworkInterface> nets =  NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface nif : Collections.list(nets)) {
			if(!nif.getDisplayName().contains("lo"))
				netInt = "%" + nif.getDisplayName();
			
		}
		
		if(windows) {
			 url = (host.length()>11)?"tcp://[" + host + ":" + port + "]": "tcp://" + host + ":" + port ;
			 System.out.println(url);
		}
		else {
			if(host.contains("0:0:0:0:0:0:0")){
				url = (host.length()>11)?"tcp://[" + host.replace(":10", ":1%lo0") + ":" + port + "]": "tcp://" + host + ":" + port ;
			} 
			else {
				url = (host.length()>11)?"tcp://[" + host + netInt + ":" + port + "]": "tcp://" + host + ":" + port ;
			}
			 System.out.println(url);
		}
		
		// Start admin console and connect via IPv6

		String console = "";
		
		console = TGAdmin.invoke(tgServer.getHome().toString(),url, tgServer.getSystemUser(),tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv4.log", 
				null, cmdFile.getAbsolutePath(), -1,10000);
		
		Assert.assertTrue(console.contains(adminConnectSuccessMsg),"TG Admin connect!");

	}
	

	/************************
	 * 
	 * Data Providers
	 * 
	 ************************/

	/**
	 * Get all IPv6 addresses available on the current machine
	 * 
	 * @throws TGGeneralException
	 * @throws IOException
	 */
	@DataProvider(name = "ipv6Data")
	public Object[][] getIPv6() throws TGGeneralException, IOException {

		// We need to get a new server here to get the port
		// since @DataProvider might run before @BeforeSuite tgServer might not exist
		// yet
		TGServer tgTempServer = new TGServer(tgHome);
		tgTempServer.setConfigFile(getConfigFile());
		int port = tgTempServer.getNetListeners()[1].getPort(); // get port of ipv6 listener

		List<Object[]> urlParams = new ArrayList<Object[]>();
		System.setProperty("java.net.preferIPv6Addresses", "true");

		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

		for (NetworkInterface nif : Collections.list(nets)) {
			if (nif == null)
				continue;
			if (!nif.isUp())
				continue;
			if (nif.isPointToPoint())
				continue;
			if (nif.getName().startsWith("awdl"))
				continue;

			Enumeration<InetAddress> addrs = nif.getInetAddresses();

			while (addrs.hasMoreElements()) {
				InetAddress address = addrs.nextElement();
				if (address instanceof Inet6Address) {
					String tmpAddr = address.getHostAddress();
//					System.out.println(tmpAddr);
					
					tmpAddr = cleanIPv6(tmpAddr);
					urlParams.add(new Object[] { tmpAddr, port });
				}
			}
		}
		return (Object[][]) urlParams.toArray(new Object[urlParams.size()][2]);
	}

	/**
	 * Get all IPv4 addresses available on the current machine
	 * 
	 * @throws TGGeneralException
	 * @throws IOException
	 */
	@DataProvider(name = "ipv4Data")
	public Object[][] getIPv4() throws TGGeneralException, IOException {

		TGServer tgTempServer = new TGServer(tgHome);
		tgTempServer.setConfigFile(getConfigFile());
		int port = tgTempServer.getNetListeners()[0].getPort(); // get port of ipv4 listener

		List<Object[]> urlParams = new ArrayList<Object[]>();
		System.setProperty("java.net.preferIPv6Addresses", "false");

		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

		for (NetworkInterface nif : Collections.list(nets)) {
			if (nif == null)
				continue;
			if (!nif.isUp())
				continue;
			if (nif.isPointToPoint())
				continue;
			if (nif.getName().startsWith("awdl"))
				continue;

			Enumeration<InetAddress> addrs = nif.getInetAddresses();

			while (addrs.hasMoreElements()) {
				InetAddress address = addrs.nextElement();
				if (address instanceof Inet4Address) {
					String tmpAddr = address.getHostAddress();
					urlParams.add(new Object[] {tmpAddr, port});
				}
			}
		}

		return (Object[][]) urlParams.toArray(new Object[urlParams.size()][2]);
	}
	
	
	/**
	 * Get several combinations of wrong urls for --url argument
	 */
	@DataProvider(name = "wrongUrlData")
	public Object[] getUrls() throws IOException, EvalError {
		Object[] data =  PipedData.read(this.getClass().getResourceAsStream("/"+this.getClass().getPackage().getName().replace('.', '/') + "/WrongUrls.data"));
		return data;
	}
	
	@DataProvider(name = "remoteServers")
	public Object[][] getRemoteServer(){
		return new Object[][] {
			//Ipv4 - IPv4 default port - on Windows
			{"172.16.1.14","8222","true"},
			//IPv6 - IPv6 default port on Windows
			{"fe80::797e:c056:c735:5359","8223","true"},
			//Ipv4 - IPv4 default port - On Mac
			{"172.16.3.72","8222","true"},
			//IPv6 - IPv6 default port on Mac
			{"fe80::1c5e:d7a2:daad:62c5","8223","true"},
			//Ipv4 - IPv4 default port - On Linux
			{"172.16.3.42","8222","true"},
			//IPv6 - IPv6 default port on Linux
			{"fe80::47c:487:3811:9814","8223","true"},
			//IPv6 - IPv4 port -  this one should not connect on Windows
			{"fe80::797e:c056:c735:5359","8222","false"},
			////Ipv4 - IPv6 default port -  this one should not connect on Windows
			{"172.16.1.14","8223","false"},
			//Ipv4 - IPv6 default port -  this one should not connect On Mac
			{"172.16.3.72","8223","false"},
			//IPv6 - IPv4 default port -  this one should not connect on Mac
			{"fe80::1c5e:d7a2:daad:62c5","8222","false"},
			//Ipv4 - IPv4 default port - On Linux this one can connect
			{"172.16.3.42","8223","true"},
			//IPv6 - IPv6 default port - on Linux this one should not connect
			{"fe80::47c:487:3811:9814","8222","false"}
		};
			
	}
	

	/************************
	 * 
	 * Cleaners and config methods
	 * 
	 ************************/

	public String cleanIPv6(String ipv6) {
		boolean control = true;
		String newIPv6 = "";
		for (int i = 0; i < ipv6.length(); i++) {
			if (ipv6.contains("%lo")) {
				newIPv6 = ipv6.replace("%lo", "");
				continue;
			}
			if (ipv6.charAt(i) == "%".charAt(0))
				control = false;
			if (ipv6.charAt(i) == ":".charAt(0) && !control)
				control = true;
			if (control)
				newIPv6 += ipv6.charAt(i);
		}
		return newIPv6;

	}

	private File getConfigFile() throws IOException {
		File confFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replaceFirst("\\.[a-z]*$", "").replace('.', '/') + "/tgdb.conf",
				tgWorkingDir + "/tgdb.conf");
		return confFile;
	}

}
