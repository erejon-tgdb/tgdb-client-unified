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

import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

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
		File initFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replaceFirst("\\.[a-z]*$", "").replace('.', '/')
						+ "/initdb.conf",
				tgWorkingDir + "/initdb.conf");
		tgServer = new TGServer(tgHome);
		try {
			tgServer.init(initFile.getAbsolutePath(), true, 60000);
		} catch (TGInitException ie) {
			System.out.println(ie.getOutput());
			throw ie;
		}
		System.out.println(tgServer.getBanner());
		tgServer.setConfigFile(getConfigFile());
		tgServer.start(15000);
	}

	/**
	 * Kill TG server after suite
	 * 
	 * @throws Exception
	 */
	@AfterSuite
	public void killServer() throws Exception {
		tgServer.kill();
		// Backup log file before moving to next test
		File logFile = tgServer.getLogFile();
		File backLogFile = new File(logFile + ".adminconnection");
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
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv6Data", description = "Connect TG Admin to TG Server via IPv6")
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

	/***
	 * Trying to connect to TGDB-admin without port and host
	 * 
	 * @throws IOException
	 */

	@Test(description = "Trying to connect  TG Admin w/o host and port")
	public void noHostAndPortSpecifiedIPv6() throws IOException {
		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");
		// Start admin console and try to connect via IPv6 without host and port

		String console = "";
		try {
			console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://scott@[]", tgServer.getSystemUser(),
					tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv6.log", null, cmdFile.getAbsolutePath(), -1,
					10000);
			Assert.fail("Expected a TGAdminException due to host and port were not sent");
		} catch (TGAdminException e) {
			Assert.assertFalse(console.contains(adminConnectSuccessMsg),
					"Admin connect to server even when URL doesn't have the port");

		}
	}


	/**
	 * testIPv4Connect - Trying to connect TG Admin to TG Server via IPv4 without
	 * port
	 * 
	 * @throws Exception
	 */
	@Test(dataProvider = "ipv4Data", description = "Trying to connect TG Admin to TG Server via IPv4 without port")
	public void testIPv4Connect(String host) throws Exception {

		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");

		// Start admin console and connect via IPv6
		// Sneha: Earlier call for invoke was incorrect as we need to use host and port
		// passed to the method in order to connect
		// String console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://" +
		// host, tgServer.getSystemUser(), tgServer.getSystemPwd(), tgWorkingDir +
		// "/admin.ipv4.log", null, cmdFile.getAbsolutePath(), -1, 10000);
		String console = "";
		try {
			console = TGAdmin.invoke(tgServer.getHome().toString(), "tcp://" + host, tgServer.getSystemUser(),
					tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv4.log", null, cmdFile.getAbsolutePath(), -1,
					10000);
			Assert.fail("Expected a TGAdminException due to wrong connection variation but did not get it");
		} catch (TGAdminException e) {
			Assert.assertFalse(console.contains(adminConnectSuccessMsg),
					"TGAdmin - Admin could not connect to server tcp://" + host + " with user root");
		}
		// System.out.println(console);

		// Assert.assertTrue(console.contains(adminConnectSuccessMsg), "Admin did not
		// connect to server");
	}

	
	/***
	 * Connection to TG Admin using properties with colon ":"
	 * @throws Exception 
	 *
	 */
	
	@Test(description = "Trying to connect using an URL with : in the properties")
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
	
	
	
	
	@Test(dataProvider = "remoteServer",
		  description = "connecting to a remote server by Ipv4 and IPv6")
	public void connectRemoteServer(String host, String port) throws Exception {
		File cmdFile = ClasspathResource.getResourceAsFile(
				this.getClass().getPackage().getName().replace('.', '/') + "/Connection.cmd",
				tgWorkingDir + "/Connection.cmd");


		String console = "";
		String url = (host.length()>11)?"tcp://[" + host + ":" + port + "]": "tcp://" + host + ":" + port ;
		System.out.println(url);
		try {
		console = TGAdmin.invoke(tgServer.getHome().toString(), url, tgServer.getSystemUser(),
					tgServer.getSystemPwd(), tgWorkingDir + "/admin.ipv6.log", null, cmdFile.getAbsolutePath(), -1,
					150000);
			
			if((!host.equalsIgnoreCase("fe80::797e:c056:c735:5359") & !port.equalsIgnoreCase("8223")) | (!host.equalsIgnoreCase("172.16.1.14") & !port.equalsIgnoreCase("8222"))){
				Assert.assertTrue(console.contains(adminConnectSuccessMsg), "Expected successful message");
			}
		}catch(Exception e) {
			System.out.println("Correct, this should not connect");
			Assert.assertFalse(console.contains(adminConnectSuccessMsg), "TGAdmin - Admin could not connect to server tcp://" + host +"and " + port + "with user root");
		}

				
			
			
		
			
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
					// Sneha: Keeping the portion of IPV6 address after % sign as well,as
					// testIPv6Connet test fails on MACOSX without it.
					// This change needs to be tested on other Platforms.
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

		// We need to get a new server here to get the port
		// since @DataProvider might run before @BeforeSuite tgServer might not exist
		// yet
		TGServer tgTempServer = new TGServer(tgHome);
		tgTempServer.setConfigFile(getConfigFile());
		// int port = tgTempServer.getNetListeners()[0].getPort(); // get port of ipv6
		// listener

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
					// Sneha: Keeping the portion of IPV6 address after % sign as well,as
					// testIPv6Connet test fails on MACOSX without it.
					// This change needs to be tested on other Platforms.
					String tmpAddr = address.getHostAddress();
					urlParams.add(new Object[] { tmpAddr });
				}
			}
		}

		return (Object[][]) urlParams.toArray(new Object[urlParams.size()][2]);
	}
	
	
	
	
	/**
	 * Get several combinations of remote addresses
	 */
	@DataProvider(name = "remoteServer")
	public Object[][] getUsers() throws IOException, EvalError {
		Object[][] data =  PipedData.read(this.getClass().getResourceAsStream("/"+this.getClass().getPackage().getName().replace('.', '/') + "/remoteServer.data"));
		return data;
	}
	
	
//	
//	
//	/**
//	 * Get several combinations of wrong user and pwd 
//	 */
//	@DataProvider(name = "x")
//	public Object[][] getUsers() throws IOException, EvalError {
//		Object[][] data =  PipedData.read(this.getClass().getResourceAsStream("/"+this.getClass().getPackage().getName().replace('.', '/') + "/WrongUsers.data"));
//		return data;
//	}

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
