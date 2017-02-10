package com.example.huimin_zhou.Huimin_Zhou_FitRunner;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.DatabaseSource;
import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.ExerciseEntry;
import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.MySQLOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Lucidity on 17/1/17.
 */

public class HistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<ExerciseEntry>> {
    DatabaseSource mDBSource;
    HistoryListAdapter mAdapter;
    ArrayList<ExerciseEntry> mEntries;
    Context mContext;
    public LoaderManager mLoaderManager;
    boolean onCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initial
        mContext = getActivity();
        mDBSource = new DatabaseSource(mContext);
        mLoaderManager = getActivity().getLoaderManager();

        mAdapter = new HistoryListAdapter(mContext);
        mLoaderManager.initLoader(1, null, this).forceLoad();
        setListAdapter(mAdapter);
        onCreated = true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        // set click listener to involve activity with more details
        ListView mListView = getListView();
        AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExerciseEntry entry = mEntries.get(position);
                switch (entry.getInputType()) {
                    case "Manual Input":
                        Intent intent = new Intent(getActivity(), HistoryEntryActivity.class);
                        Bundle bundle = new Bundle();
                        Log.d("history", String.valueOf(entry.getId()));
                        bundle.putLong(MySQLOpenHelper.ID, entry.getId());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;
                    case "GPS":
                        intent = new Intent(getActivity(), MapActivity.class);
                        bundle = new Bundle();
                        bundle.putBoolean(Global.KEY_ISSERVICE, false);
                        bundle.putBoolean(Global.KEY_ISMILE, Parser.isMile(getActivity()));
                        bundle.putLong(MySQLOpenHelper.ID, entry.getId());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;
                    default:
                        intent = new Intent(getActivity(), HistoryEntryActivity.class);
                        bundle = new Bundle();
                        bundle.putLong(MySQLOpenHelper.ID, entry.getId());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;
                }
            }
        };
        mListView.setOnItemClickListener(mListener);
    }

    // update adapter
    public void updateAdapter(Context context) {
        this.mContext = context;
        if (this.mDBSource == null) {
            this.mDBSource = new DatabaseSource(context);
            this.mAdapter = new HistoryListAdapter(context);
            mLoaderManager = ((Activity)context).getLoaderManager();
            setListAdapter(this.mAdapter);
        }

        if (onCreated) {
            onCreated = false;
        } else {
            mLoaderManager.initLoader(1, null, this).forceLoad();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-query in case the data base has changed.
        updateAdapter(mContext);
    }

    @Override
    public Loader<ArrayList<ExerciseEntry>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<ArrayList<ExerciseEntry>>(mContext) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }
            @Override
            public ArrayList<ExerciseEntry> loadInBackground() {
                DatabaseSource dbSource = new DatabaseSource(mContext);
                dbSource.open();
                ArrayList<ExerciseEntry> arrayList = dbSource.queryAll();
                dbSource.close();
                return arrayList;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<ExerciseEntry>> loader, ArrayList<ExerciseEntry> data) {
        mEntries = data;
        mAdapter.clear();
        mAdapter.addAll(mEntries);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ExerciseEntry>> loader) {
        mAdapter.clear();
        mAdapter.addAll(mEntries);
        mAdapter.notifyDataSetChanged();
    }

    public class HistoryListAdapter extends ArrayAdapter<ExerciseEntry> {
        public HistoryListAdapter(Context context) {
            super(context, R.layout.layout_historyentry);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = convertView;
            if (convertView == null) {
                view = inflater.inflate(R.layout.layout_historyentry, parent, false);
            }
            // set two lines in a row
            ExerciseEntry entry = getItem(position);
            TextView textView1 = (TextView) view.findViewById(R.id.history_text1);
            TextView textView2 = (TextView) view.findViewById(R.id.history_text2);

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MMM dd yyyy");
            String line1 = entry.getInputType() + ": " + entry.getActivityType() + ", "
                    + dateFormat.format(entry.getDate().getTime());
            String line2 = Parser.parseDistance(mContext, entry.getDistance()) +", ";
            line2 += Parser.parseSecond(entry.getDuration());

            textView1.setText(line1);
            textView2.setText(line2);
            return view;
        }
    }
}
