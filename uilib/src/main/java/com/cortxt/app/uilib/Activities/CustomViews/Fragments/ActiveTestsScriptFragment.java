package com.cortxt.app.uilib.Activities.CustomViews.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cortxt.app.uilib.R;
import com.cortxt.app.utillib.Utils.CommonIntentActionsOld;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by bscheurman on 16-03-31.
 */

public class ActiveTestsScriptFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_PAGE = "ARG_PAGE2";

    private int mPage;
    ActiveTestsPager parent = null;

    public static ActiveTestsScriptFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ActiveTestsScriptFragment fragment = new ActiveTestsScriptFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public void setPager (ActiveTestsPager pager)
    {
        parent = pager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.active_tests_script, container, false);
        Bundle b = getArguments();
        //if(b != null && b.containsKey("highlightItem") && !b.getBoolean("highlightItem")) {
        //    ScalingUtility.getInstance(getActivity()).scaleView(view);
        //}
        initViews(view);
        fillListAdapter();
        commands = new ArrayList<String> ();
        listitems = new ArrayList<String> ();
        return view;
    }

    private void initViews(View v) {
        Spinner spinnerTest = (Spinner) v.findViewById(R.id.activeTestSpinner);
        eventtypeValues = parent.initTestOptions (spinnerTest, this.getActivity());

        Spinner spinner = (Spinner) v.findViewById(R.id.durationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),R.array.LiveStatus_TrackingValues, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(3);


        spinner = (Spinner) v.findViewById(R.id.delaySpinner);
        adapter = ArrayAdapter.createFromResource(this.getActivity(),R.array.testscript_delay_values, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(2);

        spinner = (Spinner) v.findViewById(R.id.delayStartSpinner);
        adapter = ArrayAdapter.createFromResource(this.getActivity(),R.array.testscript_delay_options, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(0);


        // Init the list view for the commands
        mListView = (ListView) v.findViewById(R.id.listView);
        if (mListView == null) {
            throw new RuntimeException(
                    "ListView cannot be null. Please set a valid ListViewId");
        }

        mListView.setOnItemClickListener(new ListOnItemClickListener());

        Button b = (Button) v.findViewById(R.id.addButton);
        b.setOnClickListener(this);
        b = (Button) v.findViewById(R.id.startButton);
        b.setOnClickListener(this);
    }

    private final class ListOnItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> lv, View v, int position, long id) {
            onListItemClick((ListView) lv, v, position, id);
            // String str = ((TextView) arg1).getText().toString();
            // Toast.makeText(getBaseContext(), str,
            // Toast.LENGTH_LONG).show();
            // Intent intent = new Intent(getBaseContext(),
            // your_new_Intent.class);
            // intent.putExtra("list_view_value", str);
            // startActivity(intent);
        }
    }

    private ListView mListView;

    protected ListView getListView() {

        return mListView;
    }


    //protected int getListViewId();

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }

    protected void fillListAdapter ()
    {
        String[] values = new String[] {
                "Add Tests with delays to this list.", "The tests will repeat for a duration.", "The delay between each test can be from", "the start or the end of the test before it"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this.getActivity(), item + " selected", Toast.LENGTH_LONG).show();
    }

    private List<String> commands;
    private List<String> listitems;
    private int[] delayValues = new int[]{0,1,2,3,5,10,15,30};
    private int[] durationValues = new int[] {1, 3, 6, 12, 24, 72, 0};
    private String[] eventtypeValues = null;
    private String[] delaytypeValues = {"predelay","postdelay","nodelay"};

    public void addtestClicked (View view)
    {

        Spinner spinnerTestype = (Spinner) view.findViewById(R.id.activeTestSpinner);
        Spinner spinnerDelay = (Spinner) view.findViewById(R.id.delaySpinner);
        Spinner spinnerDelayStart = (Spinner) view.findViewById(R.id.delayStartSpinner);

        String testtype = (String)spinnerTestype.getSelectedItem();
        int pos = spinnerTestype.getSelectedItemPosition();

        String delay = (String)spinnerDelay.getSelectedItem();
        int delayPos = spinnerDelay.getSelectedItemPosition();
        String delayStart = (String)spinnerDelayStart.getSelectedItem();
        int delayTypePos = spinnerDelayStart.getSelectedItemPosition();
        int delayValue = delayValues[delayPos];

        String item = testtype;
        if (delayValue > 0)
            item += " + " +  delay + " delay after " + delayStart;
        else
            item += " + no delay";

        listitems.add (item);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_list_item_1, listitems);
        setListAdapter(adapter);

        // build the actual commands as well
        String eventtype = eventtypeValues[pos];
        String delayType = delaytypeValues[delayTypePos];

        String command = "{\"mmctype\":\"" + eventtype + "\",\"loop\":1}";
        commands.add (command);
        if (delayValue > 0 && delayTypePos < 2)
        {
            command = "{\"mmctype\":\"" + delayType + "\",\"loop\":" + (delayValue * 60 )+ "}";
            commands.add (command);
        }
    }

    @Override
    public void onClick(View v) {
        View view = this.getView ();
        if (v.getId() == R.id.addButton)
            addtestClicked (view);
        else if (v.getId() == R.id.startButton)
            startClicked (view);
    }

    public void startClicked (View view)
    {
        Spinner spinner = (Spinner) view.findViewById(R.id.durationSpinner);
        int durPos = spinner.getSelectedItemPosition();
        int durationValue = durationValues[durPos] * 5;

        String json = null;
        if (commands.size() > 0)
        {
            json = "{\"commands\":{\"schedule\":{\"dur\":" + durationValue + ",\"commands\":[";
            for (int i=0; i<commands.size(); i++)
            {
                json += commands.get(i);
                if (i<commands.size()-1)
                    json += ",";
            }
            json += "]}}}";
        }

        Intent intent = new Intent(CommonIntentActionsOld.COMMAND);
        intent.putExtra(CommonIntentActionsOld.COMMAND_EXTRA, json);
        intent.putExtra("STARTTIME_EXTRA", System.currentTimeMillis());
        //getActivity().sendBroadcast(intent);
    }
}
