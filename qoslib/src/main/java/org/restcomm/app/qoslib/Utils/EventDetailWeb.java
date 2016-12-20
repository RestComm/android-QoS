/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.qoslib.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.restcomm.app.utillib.DataObjects.EventType;
import org.restcomm.app.utillib.Reporters.ReportManager;
import org.restcomm.app.utillib.Utils.Global;
import org.restcomm.app.utillib.Utils.LoggerUtil;
import org.restcomm.app.utillib.Utils.ScalingUtility;
import org.restcomm.app.utillib.Utils.ShareInviteTask;
import org.restcomm.app.utillib.Utils.TaskHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import com.restcomm.app.qoslib.R;
/**
 * Activity that shows details of an event. It must be passed an integer event id in the Intent, with the key
 * {@link EventDetailWeb#EXTRA_EVENT_ID}
 *
 * @author brad scheurman
 *
 */
public class EventDetailWeb extends Activity {
    public static final String EXTRA_EVENT_ID = "eventId";

    TextView mdropcallText;
    Button dropButton;
    public static final String TAG = EventDetailWeb.class.getSimpleName();
    private static HashMap<String, String> mEvent;
    private AsyncTask<Void, Void, HashMap<String, String>> mGetEventInfoTask;
    private static RelativeLayout scalingLayout = null;
    private AsyncTask<Void, Void, Long> mConfirmEventTask;
    private Handler mHandler = new Handler();
    private int eventId;
    private WebView webview;
    private String webviewurl;
    private ProgressBar busyIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Enable the drawing of the whole document for Lollipop to get the whole WebView
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            WebView.enableSlowWholeDocumentDraw();
        }
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.call_details_web, null, false);
        ScalingUtility.getInstance(this).scaleView(view);
        setContentView(view);

        String eventname = getString(R.string.dashboard_eventdetail);
        if (getIntent().hasExtra("eventName")) {
            eventname = getIntent().getStringExtra("eventName");
        }
        TextView headerText = (TextView) view.findViewById(R.id.actionbartitle);
        headerText.setText(eventname);

        scalingLayout = (RelativeLayout) findViewById(R.id.scallingWrapepr);
        busyIndicator = (ProgressBar) view.findViewById(R.id.busyIndicator);
        busyIndicator.setVisibility(View.VISIBLE);

        webview = (WebView) findViewById(R.id.webView);

        if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
            eventId = getIntent().getIntExtra(EXTRA_EVENT_ID, -1);

            if (getIntent().hasExtra("url"))
            {
                webviewurl = getIntent().getStringExtra("url");
            }
            /**
             * This task gets the event info from the ReportManager, and looks up the address of the event location
             */
            mGetEventInfoTask = new AsyncTask<Void, Void, HashMap<String, String>>() {
                private static final String KEY_ADDRESS = "address";

                @Override
                protected HashMap<String, String> doInBackground(Void... params) {
                    if (webviewurl == null) {
                        ReportManager reportManager = ReportManager.getInstance(getApplicationContext());
                        HashMap<String, String> event = reportManager.getEventDetails(eventId);
                        mEvent = event;
                        return event;
                    }
                    return null;
                }

                private void showEventInfo() {

                }


                @Override
                protected void onPostExecute(final HashMap<String, String> event) {

                    // load webview with website showing this event details
                    if (event == null && webviewurl == null)
                        return;

                    if (event != null) {
                        String eventid = event.get(ReportManager.EventKeys.EVENTID);
                        int eventtype = Integer.parseInt(event.get(ReportManager.EventKeys.TYPE));
                        if (eventid.length() > 0) {
                            ReportManager reportManager = ReportManager.getInstance(getApplicationContext());

                            double fLat = 0, fLng = 0;
                            if (event.get(ReportManager.EventKeys.LONGITUDE) != null) {
                                fLat = Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE));
                                fLng = Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE));
                            }
                            long timeStamp = Long.parseLong(event.get(ReportManager.EventKeys.TIMESTAMP));
                            Date dateValue = new Date(timeStamp);

                            SimpleDateFormat sd = new SimpleDateFormat("MMM dd yyyy");
                            Calendar calendar = new GregorianCalendar(dateValue.getYear(), dateValue.getMonth(), dateValue.getDate());
                            calendar.add(Calendar.DATE, -1);
                            String startdate = sd.format(calendar.getTime());
                            calendar.add(Calendar.DATE, 3);
                            String enddate = sd.format(calendar.getTime());

                            int carrierID = 0;
                            if (event.get(ReportManager.EventKeys.OPERATOR_ID).length() > 0)
                                carrierID = Integer.parseInt(event.get(ReportManager.EventKeys.OPERATOR_ID));
                            else
                                carrierID = reportManager.getCurrentCarrier().ID;

                            String appurl = Global.getApiUrl(EventDetailWeb.this);
                            String servername = "app";
                            if (appurl.indexOf("dev.") >= 0)
                                servername = "devmynetwork";

                            String linkMN = "https://" + servername + ".mymobilecoverage.com/MyNetwork/simpleshare.html?userID=" + Global.getUserID(EventDetailWeb.this) + "&type=" + eventtype + "&carrierID=" + carrierID;
                            linkMN += "&id=" + eventid + "&eventsmode=evt&limit=20000&startdate=" + startdate + "&enddate=" + enddate;
                            linkMN += "&filternames=all";
                            linkMN += "&zoom=14&lat=" + fLat + "&lng=" + fLng + "&expand=1&mob=1";
                            linkMN += "&apiKey=" + Global.getApiKey(EventDetailWeb.this);
                            //linkMN += "&evtname=Floor " + samplingEvent.getEventIndex() + " sampling";

                            webviewurl = linkMN;
                        }
                    }
                    webview.setWebChromeClient(new WebChromeClient() {});
                    if (Build.VERSION.SDK_INT >= 19) {
                        webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    }
                    webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                    //webview.getSettings().setLoadWithOverviewMode(true);
                    //webview.getSettings().setUseWideViewPort(true);
                    webview.getSettings().setJavaScriptEnabled(true);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            webview.setWebContentsDebuggingEnabled(true);
//                        }

                    webview.loadUrl(webviewurl);

                    webview.setWebViewClient(new WebViewClient() {
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            //Toast.makeText(owner, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "runWebpageTest", "onError " + errorCode + " " + description);
                            busyIndicator.setVisibility(View.GONE);

                        }

                        public void onPageFinished(WebView view, String url) {
                            busyIndicator.setVisibility(View.GONE);
                        }

                        public void onPageStarted(WebView view, String url, Bitmap favicon) {

                        }
                    });

                }

            };
            TaskHelper.execute(mGetEventInfoTask);

        } else {
            onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGetEventInfoTask != null) {
            mGetEventInfoTask.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share) {

            // int eventType = Integer.parseInt(mEvent.get(EventKeys.TYPE));
            EventType eventType = EventType.get(Integer.parseInt(mEvent.get(ReportManager.EventKeys.TYPE)));
            int markerResource = eventType.getImageResource();
            String message;
            String subject;
            if (mEvent == null)
                return true;

            if (eventType != null) {
                int evtstr = eventType.getEventString();

                message = getString(R.string.sharemessage_eventdetail_event);
                subject = getString(R.string.sharemessagesubject_eventdetail_event);
                String strEvent = getString(evtstr);
                message = String.format(message, strEvent);
                subject = String.format(subject, strEvent);

            } else {
                message = "";
                subject = "";
            }

            Bitmap screenshot = capture(webview);
            TaskHelper.execute(new ShareInviteTask(EventDetailWeb.this, message, subject, webview, eventId, screenshot));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //	@Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public void backActionClicked(View button) {
        this.finish();
    }

    public void shareClicked(View button) {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String message = "";
                String subject = "";
                if (mEvent == null)
                    return;

                // int eventType = Integer.parseInt(mEvent.get(EventKeys.TYPE));
                EventType eventType = EventType.get(Integer.parseInt(mEvent.get(ReportManager.EventKeys.TYPE)));

                if (eventType != null) {
                    int evtstr = eventType.getEventString();

                    message = getString(R.string.sharemessage_eventdetail_event);
                    subject = getString(R.string.sharemessagesubject_eventdetail_event);
                    String strEvent = getString(evtstr);
                    message = String.format(message, strEvent);
                    subject = String.format(subject, strEvent);

                } else {
                    message = "";
                    subject = "";
                }
                Bitmap screenshot = capture(webview);
                TaskHelper.execute(
                        new ShareInviteTask(EventDetailWeb.this, message, subject, webview, eventId, screenshot));

            }
        }, 500);
    }

    public Bitmap capture(WebView webView) {
        WebView lObjWebView = webView;
        lObjWebView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //lObjWebView.layout(0, 0, lObjWebView.getMeasuredWidth(), lObjWebView.getMeasuredHeight());
        Bitmap bm = Bitmap.createBitmap(lObjWebView.getMeasuredWidth(), lObjWebView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas bigcanvas = new Canvas(bm);
        Paint paint = new Paint();
        int iHeight = bm.getHeight();
        bigcanvas.drawBitmap(bm, 0, iHeight, paint);
        lObjWebView.draw(bigcanvas);
        return bm;
    }
}
