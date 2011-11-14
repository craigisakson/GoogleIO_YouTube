package com.runninghusky.googleio;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {

	private Activity activity;
	private String[] data, title, author;
	private Long[] rating;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public LazyAdapter(Activity a, String[] d, String[] t, String[] u,
			String[] au, Long[] r) {
		activity = a;
		data = d;
		title = t;
		author = au;
		rating = r;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return data.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {
		public TextView text, author, rate;
		public ImageView image;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.row, null);
			holder = new ViewHolder();
			holder.text = (TextView) vi.findViewById(R.id.title);
			holder.rate = (TextView) vi.findViewById(R.id.rate);
			holder.author = (TextView) vi.findViewById(R.id.author);
			// holder.pos = (TextView) vi.findViewById(R.id.pos);
			holder.image = (ImageView) vi.findViewById(R.id.uri);
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		holder.text.setText(title[position]);
		holder.image.setTag(data[position]);
		holder.author.setText("Author:  " + author[position]);
		holder.rate
				.setText("Caption Rating: " + Long.valueOf(rating[position]));
		// holder.pos.setText(String.valueOf(position));
		imageLoader.DisplayImage(data[position], activity, holder.image);
		return vi;
	}
}