package com.klinker.android.messaging_sliding;

import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactSearchArrayAdapter2 extends ArrayAdapter<String> {
	  private final Activity context;
	  private final ArrayList<String> names, numbers, types;
	  private SharedPreferences sharedPrefs;

	  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	    public TextView text3;
	  }

	  public ContactSearchArrayAdapter2(Activity context, ArrayList<String> names, ArrayList<String> numbers, ArrayList<String> types) {
	    super(context, R.layout.contact_search, names);
	    this.context = context;
	    this.names = names;
	    this.numbers = numbers;
	    this.types = types;
	    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    if (rowView == null) {
	      LayoutInflater inflater = context.getLayoutInflater();
	      rowView = inflater.inflate(R.layout.contact_search, null);
	      
	      rowView.setBackgroundColor(context.getResources().getColor(R.color.black));
	      
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.text = (TextView) rowView.findViewById(R.id.conversationCount);
	      viewHolder.text2 = (TextView) rowView.findViewById(R.id.receivedMessage);
	      viewHolder.text3 = (TextView) rowView.findViewById(R.id.receivedDate);
	      
	      viewHolder.text.setTextColor(context.getResources().getColor(R.color.white));
	      viewHolder.text2.setTextColor(context.getResources().getColor(R.color.white));
	      viewHolder.text3.setTextColor(context.getResources().getColor(R.color.white));
	      
	      if (sharedPrefs.getBoolean("custom_font", false))
	      {
	    	  viewHolder.text.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text2.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text3.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	      }
	      
	      rowView.setTag(viewHolder);
	    }

	    ViewHolder holder = (ViewHolder) rowView.getTag();
	    holder.text.setText(names.get(position));
	    holder.text2.setText(numbers.get(position));
	    holder.text3.setText(types.get(position));
	    
	    return rowView;
	  }
	} 
