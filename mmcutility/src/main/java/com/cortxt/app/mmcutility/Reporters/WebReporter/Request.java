package com.cortxt.app.mmcutility.Reporters.WebReporter;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

public interface Request extends HttpUriRequest {
	public JSONObject serialize() throws JSONException;
	public void deserialize(JSONObject jsonObject) throws JSONException;
}
