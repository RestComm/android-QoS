package org.restcomm.app.mmcextension.VQ;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;

import android.content.Context;

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
