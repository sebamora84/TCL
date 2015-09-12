package com.mlstuff.apps.tcl;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;

import com.mlstuff.apps.tcl.R;

public class TCL extends AppCompatActivity {

    private LinkedList<TclItem> _nextList = new LinkedList<TclItem>();
    private LinkedList<TclItem> _prevList = new LinkedList<TclItem>();
    private TclItem _currentItem;
    public int _currentPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcl);

        String uriString = getIntent().getDataString();
        if (uriString != null) {
            SetSharedItem(uriString);
        } else {
            SetNextItem();
        }
        InitializeUI();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tcl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_item_share) {

            ShareCurrentItem();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void InitializeUI() {
        //Assing Buttons Actions
        Button _nextButton = (Button) findViewById(R.id.btnNext);
        _nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SetNextItem();
            }
        });

        Button _prevButton = (Button) findViewById(R.id.btnPrev);
        _prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetPrevItem();
            }
        });

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
    }

    private void SetPrevItem() {
        if (_prevList.isEmpty() || _currentItem == null) {
            return;
        }
        _nextList.addFirst(_currentItem);
        _currentItem = _prevList.removeFirst();
        ShowTclItem(_currentItem);
    }

    private void SetNextItem() {
        if (_nextList.isEmpty() || _currentItem == null) {
            new WebDataRefresh().execute("http://thecodinglove.com/page/" + ++_currentPageIndex);
            return;
        }
        _prevList.addFirst(_currentItem);
        _currentItem = _nextList.removeFirst();
        ShowTclItem(_currentItem);
    }

    private void SetSharedItem(String url) {
        new WebDataRefresh().execute(url, "SingleItem");
    }

    private void ShareCurrentItem() {

        //create the send intent
        Intent shareIntent =
                new Intent(android.content.Intent.ACTION_SEND);

        //set the type
        shareIntent.setType("text/plain");

        //add a subject
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                getString(R.string.app_short_name)+ " - " +  _currentItem.Title);

        //build the body of the message to be shared
        String shareMessage = _currentItem.ItemUrl;

        //add the message
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);

        //start the chooser for sharing
        startActivity(Intent.createChooser(shareIntent, "Share TCL with your friends"));
    }

    private void ShowTclItem(TclItem item) {
        if (item == null)
            return;
        //Set Title
        TextView _txtTitle = (TextView) findViewById(R.id.txtTitle);
        _txtTitle.setText(item.Title);
        //Set Image
        WebView _imgMain = (WebView) findViewById(R.id.webView);
        _imgMain.loadUrl(item.ImageUrl);
    }

    public class WebDataRefresh extends  AsyncTask<Object, TclItem, String> {
        @Override
        protected String doInBackground(Object... params) {

            String url = (String) params[0];
            String html;
            try
            {
                html = RequestHtml(url);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return "Error requesting posts";
            }

            if (params.length > 1 && params[1] == "SingleItem")
            {
                try
                {
                    GetTclSingleItem(html, url);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Error getting post";
                }
            }
            else
            {
                try
                {
                    GetTclItems(html);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Error getting posts";
                }
            }
            return getString(R.string.OkRetriving);
        }

        @Override
        protected void onProgressUpdate(TclItem... items)
        {
            ShowTclItem(items[0]);
        }

        //Executed in the UI thread
        @Override
        protected void onPostExecute(String result) {
            if (result != getString(R.string.OkRetriving)) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        }

        private String RequestHtml(String urlRequested) throws IOException {

            URL url = new URL(urlRequested);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            StringBuilder htmlCode = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                htmlCode.append(line);
            }

            inputStream.close();
            return  htmlCode.toString();
        }

        private void GetTclSingleItem(String html, String url)  {
            TclItem item = WebReader.GetSingleTclItems(html);
            //show it using the ui thread
            publishProgress(item);
        }

        private void GetTclItems(String html) {

            LinkedList<TclItem> tclItems = WebReader.ParseAllTclItems(html);
            ListIterator<TclItem> iterator = tclItems.listIterator();

            if (iterator.hasNext())
                _currentItem = iterator.next();
                //show it using the ui thread
                publishProgress(_currentItem);

            while (iterator.hasNext())
            {
                TclItem item= iterator.next();
                if (!_nextList.contains(item))
                    _nextList.addLast(item);
            }
        }
    }
}




