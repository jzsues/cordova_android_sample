/**
 * 
 */
package com.zvidia.android.xmpp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.provider.ProviderManager;

import android.util.Log;

/**
 * @author jiangzm
 * 
 */
public class XmppManager {
	public static final String LOG_TAG = XmppManager.class.getSimpleName();
	private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

	private ExecutorService poolExecutor;

	private String xmppHost = "192.168.1.10";

	private int xmppPort = 5222;

	private String username = "354957031003067";

	private String password = "354957031003067";

	private ConnectionListener connectionListener;

	private PacketListener notificationPacketListener;

	private NotificationIQProvider notificationIQProvider;

	private XMPPConnection connection;

	private boolean running;

	public XMPPConnection getConnection() {
		return connection;
	}

	public void setConnection(XMPPConnection connection) throws Exception {
		this.connection = connection;
	}

	public String getXmppHost() {
		return xmppHost;
	}

	public void setXmppHost(String xmppHost) {
		this.xmppHost = xmppHost;
	}

	public int getXmppPort() {
		return xmppPort;
	}

	public void setXmppPort(int xmppPort) {
		this.xmppPort = xmppPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	public PacketListener getNotificationPacketListener() {
		return notificationPacketListener;
	}

	public void setNotificationPacketListener(PacketListener notificationPacketListener) {
		this.notificationPacketListener = notificationPacketListener;
	}

	public NotificationIQProvider getNotificationIQProvider() {
		return notificationIQProvider;
	}

	public void setNotificationIQProvider(NotificationIQProvider notificationIQProvider) {
		this.notificationIQProvider = notificationIQProvider;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public XmppManager(PacketListener notificationPacketListener) {
		this.connectionListener = new PersistentConnectionListener(this);
		this.notificationPacketListener = notificationPacketListener;
		this.notificationIQProvider = new NotificationIQProvider();
		this.poolExecutor = Executors.newFixedThreadPool(10);
	}

	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	public boolean isAuthenticated() {
		return connection != null && connection.isConnected() && connection.isAuthenticated();
	}

	public void connect(ConnectionCallback callback) {
		ConnectTask connectTask = new ConnectTask(this, callback);
		poolExecutor.submit(connectTask);
	}

	public void disconnect(ConnectionCallback callback) {
		DisconnectTask disconnectTask = new DisconnectTask(this, callback);
		poolExecutor.submit(disconnectTask);
	}

	public void login(ConnectionCallback callback) {
		LoginTask loginTask = new LoginTask(this, callback);
		poolExecutor.submit(loginTask);
	}

	public static class ConnectTask implements Runnable {

		XmppManager xmppManager;

		ConnectionCallback callback;

		public ConnectTask(XmppManager xmppManager, ConnectionCallback callback) {
			super();
			this.xmppManager = xmppManager;
			this.callback = callback;
		}

		@Override
		public void run() {
			if (!xmppManager.isConnected() && !xmppManager.isRunning()) {
				try {
					xmppManager.setRunning(true);
					// Create the configuration for this new connection
					ConnectionConfiguration connConfig = new ConnectionConfiguration(xmppManager.getXmppHost(), xmppManager.getXmppPort());
					connConfig.setSecurityMode(SecurityMode.disabled);
					// connConfig.setSecurityMode(SecurityMode.required);
					connConfig.setSASLAuthenticationEnabled(false);
					connConfig.setCompressionEnabled(false);

					XMPPConnection connection = new XMPPConnection(connConfig);
					xmppManager.setConnection(connection);
					// Connect to the server
					connection.connect();
					// packet provider
					connection.addConnectionListener(xmppManager.getConnectionListener());

					ProviderManager.getInstance().addIQProvider("notification", "androidpn:iq:notification",
							xmppManager.getNotificationIQProvider());
					Log.i(LOG_TAG, "XMPP connected successfully");
					callback.onSuccess();
				} catch (Exception e) {
					Log.e(LOG_TAG, "XMPP connection failed", e);
					callback.onFailed("XMPP connection failed");
				} finally {
					xmppManager.setRunning(false);
				}
			} else {
				Log.w(LOG_TAG, "XMPP is connected or is running");
				callback.onFailed("XMPP is connected or is running");
			}

		}
	}

	public static class DisconnectTask implements Runnable {

		XmppManager xmppManager;

		ConnectionCallback callback;

		public DisconnectTask(XmppManager xmppManager, ConnectionCallback callback) {
			super();
			this.xmppManager = xmppManager;
			this.callback = callback;
		}

		@Override
		public void run() {
			if (xmppManager.isConnected() && !xmppManager.isRunning()) {
				Log.d(LOG_TAG, "terminatePersistentConnection()... run()");
				XMPPConnection connection = xmppManager.getConnection();
				try {
					xmppManager.setRunning(true);
					connection.disconnect();
					connection.removePacketListener(xmppManager.getNotificationPacketListener());
					connection.removeConnectionListener(xmppManager.getConnectionListener());
					callback.onSuccess();
				} catch (Exception e) {
					Log.e(LOG_TAG, "XMPP disconnection failed", e);
					callback.onFailed("XMPP disconnection failed");
				} finally {
					xmppManager.setRunning(false);
				}
			} else {
				Log.w(LOG_TAG, "XMPP is not connected or is running");
				callback.onFailed("XMPP is not connected or is running");
			}
		}
	}

	public static class LoginTask implements Runnable {

		XmppManager xmppManager;

		ConnectionCallback callback;

		public LoginTask(XmppManager xmppManager, ConnectionCallback callback) {
			super();
			this.xmppManager = xmppManager;
			this.callback = callback;
		}

		@Override
		public void run() {
			if (xmppManager.isConnected() && !xmppManager.isAuthenticated() && !xmppManager.isRunning()) {
				XMPPConnection connection = xmppManager.getConnection();
				try {
					xmppManager.setRunning(true);
					xmppManager.getConnection().login(xmppManager.getUsername(), xmppManager.getPassword(), XMPP_RESOURCE_NAME);
					Log.d(LOG_TAG, "Loggedn in successfully");
					PacketFilter packetFilter = new PacketTypeFilter(NotificationIQ.class);
					// packet listener
					PacketListener packetListener = xmppManager.getNotificationPacketListener();
					connection.addPacketListener(packetListener, packetFilter);
					callback.onSuccess();
				} catch (Exception e) {
					Log.e(LOG_TAG, "LoginTask.call()... other error");
					Log.e(LOG_TAG, "Failed to login to xmpp server. Caused by: " + e.getMessage(), e);
					callback.onFailed("LoginTask.call()... other error");
				} finally {
					xmppManager.setRunning(false);
				}
			} else {
				Log.w(LOG_TAG, "XMPP is not connected or is running or is authenticated");
				callback.onFailed("XMPP is not connected or is running or is authenticated");
			}
		}
	}

}
