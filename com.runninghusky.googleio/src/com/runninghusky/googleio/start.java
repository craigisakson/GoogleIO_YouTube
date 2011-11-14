package com.runninghusky.googleio;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

public class start extends Activity implements Runnable {

	private ProgressDialog pd;
	String URL = "http://gdata.youtube.com/feeds/api/videos?start-index=1&max-results=25&caption=true&format=1&v=2&q=";
	public static final String PREFS_NAME = "MyPrefsFile";
	private String searchString;
	private EditText editTextSearch;
	ListView list;
	LazyAdapter adapter;
	List<String> lImageUri = new ArrayList();
	List<String> lTitle = new ArrayList();
	List<String> lUrl = new ArrayList();
	List<String> lAuthor = new ArrayList();
	List<Long> lRating = new ArrayList();
	List<String> lId = new ArrayList();

	protected static final int SHARE = 0;
	protected static final int RATING = 1;

	@Override
	public void onDestroy() {
		adapter.imageLoader.stopThread();
		list.setAdapter(null);
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupStart();
	}

	private void setupStart() {
		list = (ListView) findViewById(R.id.ListViewResults);

		editTextSearch = (EditText) findViewById(R.id.EditTextSearchBar);

		final Button btnSearch = (Button) findViewById(R.id.ButtonSearchVideos);
		btnSearch.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				try {
					resetList();
				} catch (Exception e) {
					Log.d("e", e.toString());
				}
				searchString = (editTextSearch.getText().toString()).replace(
						" ", "+");

				getResults();
			}
		});

	}

	public void getResults() {
		pd = ProgressDialog.show(this, "", "Fetching some results...", true,
				false);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		callWebService();
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			try {
				resultsYouTube();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void callWebService() {
		try {
			String featuredFeed = URL + searchString;
			Log.d("url", URL + searchString);
			URL url = new URL(featuredFeed);

			URLConnection connection;
			connection = url.openConnection();

			HttpURLConnection httpConnection = (HttpURLConnection) connection;

			int responseCode = httpConnection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = httpConnection.getInputStream();

				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();

				Document dom = db.parse(in);
				Element docEle = dom.getDocumentElement();

				NodeList nl = docEle.getElementsByTagName("entry");
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0; i < nl.getLength(); i++) {
						Element entry = (Element) nl.item(i);
						Element title = (Element) entry.getElementsByTagName(
								"title").item(0);
						Element id = (Element) entry.getElementsByTagName("id")
								.item(0);
						Element link = (Element) entry.getElementsByTagName(
								"link").item(0);
						Element mediaThumbNail = (Element) entry
								.getElementsByTagName("media:thumbnail")
								.item(0);
						Element author = (Element) entry.getElementsByTagName(
								"author").item(0);
						Element auth = (Element) author.getElementsByTagName(
								"name").item(0);

						lAuthor.add(auth.getFirstChild().getNodeValue());
						lTitle.add(title.getFirstChild().getNodeValue());
						lUrl.add(link.getAttribute("href"));
						lImageUri.add(mediaThumbNail.getAttribute("url"));
						lId.add(id.getFirstChild().getNodeValue());
						SharedPreferences settings = getSharedPreferences(
								PREFS_NAME, 0);
						lRating.add(settings.getLong(id.getFirstChild()
								.getNodeValue(), 0));

					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
		}

	} // end callWebService()

	private void resultsYouTube() {
		if (lImageUri.size() > 0) {
			String[] mImageUri = (String[]) lImageUri.toArray(new String[0]);
			String[] mTitle = (String[]) lTitle.toArray(new String[0]);
			final String[] mUrl = (String[]) lUrl.toArray(new String[0]);
			String[] mAuthor = (String[]) lAuthor.toArray(new String[0]);
			Long[] mRating = (Long[]) lRating.toArray(new Long[0]);

			adapter = new LazyAdapter(this, mImageUri, mTitle, mUrl, mAuthor,
					mRating);
			if (!adapter.isEmpty()) {
				list.setAdapter(adapter);
			}

			list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenu.ContextMenuInfo menuInfo) {

					menu.setHeaderTitle("Menu");
					menu.add(0, SHARE, 0, "Share");
					menu.add(0, RATING, 0, "Rating");
					// menu.add(0, CONTEXT_SET_WEEK_NOT_COMPLETE, 1,
					// "Mark Week Not Complete");
				}
			});

			list.setOnItemClickListener(new OnItemClickListener() {
				// @Override
				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					long p = list.getItemIdAtPosition(position);
					String youTubeUrl = mUrl[(int) p];
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(youTubeUrl)));
				}
			});
		} else {
			Toast.makeText(this, "No results found...", Toast.LENGTH_LONG)
					.show();
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case SHARE:
			share(menuInfo.id);
			break;
		case RATING:
			setRating(menuInfo.id);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		setupStart();

		return true;
	}

	private void share(long id) {
		final String[] mUrl = (String[]) lUrl.toArray(new String[0]);
		String youTubeUrl = mUrl[(int) id];

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, "Cool YouTube!");
		i.putExtra(Intent.EXTRA_TEXT, youTubeUrl);
		startActivity(Intent.createChooser(i, "Share Video"));
	}

	private void setRating(final long id) {
		final Dialog dialog = new Dialog(start.this);
		dialog.setContentView(R.layout.ratingbardialog);
		dialog.setTitle("Rate this Video");
		dialog.setCancelable(false);

		Button buttonOk = (Button) dialog.findViewById(R.id.ButtonSave);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RatingBar ratingBar = (RatingBar) dialog
						.findViewById(R.id.ratingbar);
				long lngRating = (long) ratingBar.getRating();
				updateRating(lngRating, id);
				resetList();
				getResults();
				dialog.dismiss();
			}
		});
		// now that the dialog is set up, it's time to show it
		dialog.show();
	}

	private void updateRating(long lngRating, long id) {
		final String[] mId = (String[]) lId.toArray(new String[0]);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(mId[(int) id], lngRating);

		// Commit the edits!
		editor.commit();

	}

	private void resetList() {
		lImageUri = new ArrayList();
		lTitle = new ArrayList();
		lUrl = new ArrayList();
		lAuthor = new ArrayList();
		lRating = new ArrayList();
		lId = new ArrayList();

		adapter.imageLoader.clearCache();
		adapter.notifyDataSetChanged();
	}
}