/**
 * 
 */
package org.apache.cordova.plugin;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.zvidia.android.xmpp.ConnectionCallback;
import com.zvidia.android.xmpp.NotificationIQ;
import com.zvidia.android.xmpp.XmppManager;

/**
 * @author jiangzm
 * 
 */
public class MessageListener extends CordovaPlugin implements PacketListener {
	public static final String LOG_TAG = MessageListener.class.getSimpleName();

	public static final String ACTION_CONNECT = "connect";
	public static final String ACTION_LOGIN = "login";
	public static final String ACTION_DISCONNECT = "disconnect";

	CallbackContext messageCallbackContext;

	XmppManager xmppManager;

	public MessageListener() {
		super();
		this.xmppManager = new XmppManager(this);
	}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals(ACTION_CONNECT)) {
			xmppManager.connect(new ConnectionCallback() {
				@Override
				public void onSuccess() {
					callbackContext.success();
				}

				@Override
				public void onFailed(String message) {
					callbackContext.error(message);
				}
			});
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
			return true;
		} else if (action.equals(ACTION_LOGIN)) {
			if (messageCallbackContext != null) {
				callbackContext.error("Message listener connection already login.");
				return true;
			}
			messageCallbackContext = callbackContext;
			xmppManager.login(new ConnectionCallback() {
				@Override
				public void onSuccess() {
					keepCallbackSuccess(ACTION_LOGIN);
				}

				@Override
				public void onFailed(String message) {
					keepCallbackError(ACTION_LOGIN);
				}
			});
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			messageCallbackContext.sendPluginResult(pluginResult);
			return true;
		} else if (action.equals(ACTION_DISCONNECT)) {
			xmppManager.disconnect(new ConnectionCallback() {
				@Override
				public void onSuccess() {

					// callback in
					// JS side
					messageCallbackContext = null;
					callbackContext.success();
				}

				@Override
				public void onFailed(String message) {
					callbackContext.error(message);
				}
			});
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
			return true;

		}
		return false;
	}

	/**
	 * Create a new plugin result and send it back to JavaScript
	 * 
	 * @param connection
	 *            the network info to set as navigator.connection
	 */
	private void sendUpdate(JSONObject info, boolean keepCallback) {
		if (this.messageCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, info);
			result.setKeepCallback(keepCallback);
			this.messageCallbackContext.sendPluginResult(result);
		}
	}

	private void keepCallbackError(String type) {
		JSONObject info = new JSONObject();
		try {
			info.put("result", "error");
			info.put("type", type);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		sendUpdate(info, true);
	}

	private void keepCallbackSuccess(String type) {
		JSONObject info = new JSONObject();
		try {
			info.put("result", "success");
			info.put("type", type);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		sendUpdate(info, true);
	}

	@Override
	public void processPacket(Packet packet) {
		Log.d(LOG_TAG, "NotificationPacketListener.processPacket()...");
		Log.d(LOG_TAG, "packet.toXML()=" + packet.toXML());

		if (packet instanceof NotificationIQ) {
			NotificationIQ notification = (NotificationIQ) packet;

			if (notification.getChildElementXML().contains("androidpn:iq:notification")) {
				String notificationId = notification.getId();
				String notificationApiKey = notification.getApiKey();
				String notificationTitle = notification.getTitle();
				String notificationMessage = notification.getMessage();
				// String notificationTicker = notification.getTicker();
				String notificationUri = notification.getUri();
				Log.d(LOG_TAG, "processed Packet,message:" + notificationMessage);

				try {
					JSONObject info = new JSONObject();
					info.put("notificationId", notificationId);
					info.put("notificationApiKey", notificationApiKey);
					info.put("notificationTitle", notificationTitle);
					info.put("notificationUri", notificationUri);
					sendUpdate(info, true); // release status
				} catch (JSONException e) {
					Log.e(LOG_TAG, e.getMessage(), e);
				}
			}
		}

	}
}
