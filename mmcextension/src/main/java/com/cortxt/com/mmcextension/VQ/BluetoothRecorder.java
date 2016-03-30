package com.cortxt.com.mmcextension.VQ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;

import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.com.mmcextension.R;

public class BluetoothRecorder   {

	public BluetoothHeadset headset;
	Context context;
	private static BluetoothRecorder mInstance;
	public static final String TAG = BluetoothRecorder.class.getSimpleName();
	public boolean isRecording = false, isDownloading = false, isComplete = false;
	public static UUID BT_UUID = UUID.fromString("00001101-bbad-1000-8000-00805f9b34fb");

	public BluetoothRecorder(Context _context) {

	}

	public List<BluetoothDevice> getDevices ()
	{
		return null;
	}
	
	public boolean isConnected ()
	{
		return false;
	}
	public void startDiscovery ()
	{
	}
	public void cancelDiscovery ()
	{

	}
	
	public void testVQDevice ()
	{
	}

	public static BluetoothRecorder getInstance(Context service) {
		if (mInstance == null) {
			mInstance = new BluetoothRecorder(service);
		}
		return mInstance;
	}

	public void disconnect ()
	{

	}

}
