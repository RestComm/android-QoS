package com.cortxt.app.utillib.Utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import java.net.URLDecoder;
//import com.google.firebase.firebase_invites.*;
/**
 * Created by bscheurman on 16-08-30.
 */
public class FirebaseInvite implements GoogleApiClient.OnConnectionFailedListener {

    private FragmentActivity activity;
    private OnResponseListener mOnResponseListener = null;
    public boolean connected = false;
    public boolean invited = false;
    public String url = null;
    public String deepLink = null;
    public String error = null;

    public FirebaseInvite(FragmentActivity activity)
    {
        this.activity = activity;
    }

    public void checkInvite ()
    {
        try {
            // Build GoogleApiClient with AppInvite API for receiving deep links
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .enableAutoManage(activity, null)
                    .addApi(AppInvite.API)
                    .build();

            int bGoogle = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

            if (bGoogle != 0)
            {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "FirebaseInvite", "isGooglePlayServicesAvailable error " + bGoogle, "");
                error = "GooglePlayServices error " + bGoogle;
                mOnResponseListener.onResponse(this);
            }
            // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
            // would automatically launch the deep link if one is found.
            boolean autoLaunchDeepLink = false;
            AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, activity, autoLaunchDeepLink)
                    .setResultCallback(
                            new ResultCallback<AppInviteInvitationResult>() {
                                @Override
                                public void onResult(@NonNull AppInviteInvitationResult result) {
                                    try {
                                        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "FirebaseInvite", "ResultCallback", result.toString());

                                        if (result.getStatus().isSuccess()) {
                                            // Extract deep link from Intent
                                            Intent intent = result.getInvitationIntent();
                                            deepLink = AppInviteReferral.getDeepLink(intent);
                                            url = deepLink;
                                            connected = true;
                                            invited = true;
                                            java.net.URLDecoder decoder = new java.net.URLDecoder();
                                            int pos = deepLink.indexOf ("h");
                                            if (pos > 0)
                                                deepLink = deepLink.substring(pos);
                                            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "FirebaseInvite", "ResultCallback deeplink=", deepLink);

                                            url = decoder.decode(deepLink, "utf8");

                                            mOnResponseListener.onResponse(FirebaseInvite.this);
                                            // Handle the deep link. For example, open the linked
                                            // content, or apply promotional credit to the user's
                                            // account.
                                            // ...
                                        } else {
                                            connected = true;
                                            invited = false;
                                            error = "no invite";
                                            mOnResponseListener.onResponse(FirebaseInvite.this);
                                        }
                                    } catch (Exception e) {
                                        error = e.getMessage();
                                        mOnResponseListener.onResponse(FirebaseInvite.this);
                                    }
                                }
                            });
        }
        catch (Exception e)
        {
            error = e.getMessage();
            mOnResponseListener.onResponse(this);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(activity, "Google Play Services Error: " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
        error = "error " + connectionResult.getErrorCode();
        mOnResponseListener.onResponse(this);
    }

    // set callback to Client application, will call back with success or failure
    public void setOnResponseListener(OnResponseListener listener)
    {
        mOnResponseListener = listener;
        checkInvite ();
    }

    public interface OnResponseListener
    {
        void onResponse (FirebaseInvite locationRequest);
    }
}
