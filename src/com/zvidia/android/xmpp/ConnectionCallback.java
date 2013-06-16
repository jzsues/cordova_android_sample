/**
 * 
 */
package com.zvidia.android.xmpp;

/**
 * @author jiangzm
 * 
 */
public interface ConnectionCallback {
	public void onSuccess();

	public void onFailed(String message);
}
