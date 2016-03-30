package com.cortxt.com.mmcextension.VQ;

import android.os.Handler;

import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.ICallbacks;
import com.cortxt.app.mmcutility.Utils.TimeSeries;

public class VQManager {

public static final String TAG = VQManager.class.getSimpleName();

	public static final String KEY_SETTINGS_USE_BLUETOOTH_REC = "KEY_SETTINGS_USE_BLUETOOTH_REC";
	public static final String KEY_SETTINGS_BLUETOOTH_DEVICE = "KEY_SETTINGS_USE_BLUETOOTH_DEVICE";
	public static final String KEY_SETTINGS_VQ_LANG = "KEY_SETTINGS_VQ_LANG";
	public static final String KEY_SETTINGS_VQ_VOICE = "KEY_SETTINGS_VQ_VOICE";

	public static final String VOICETEST_INPROGRESS = "KEY_VOICETEST_INPROGRESS";
	public static final String VOICETEST_FILENAME = "KEY_VOICETEST_FILENAME";
	public static final String VIDEOTEST_INPROGRESS = "KEY_VIDEOTEST_INPROGRESS";
	public static final String VQ_CONFIG = "KEY_VQ_CONFIG";
	public static final String VOICETEST_SERVICE = "KEY_VOICETEST_SERVICE";

	public static TimeSeries<Integer> audioTimeSeries = null;

	public VQManager(ICallbacks owner, PhoneState phoneState) {
	}
	public void setHandler (Handler han)
	{
	}
	public static VQManager getInstance() {
		return null;
	}
	public void killTest () {
	}
	/**
	 * Runs a speed test in a background thread and reports the results if network is not wifi
	 * @param trigger to flag how this test was triggered
	 */
	public void runTest(int trigger) {
	}
}
