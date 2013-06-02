package com.klinker.android.messaging_sliding;

import android.app.*;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.*;
import android.support.v4.app.TaskStackBuilder;
import android.view.*;
import android.widget.*;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.klinker.android.messaging_card.BatchDeleteActivity;
import com.klinker.android.messaging_donate.DeliveredReceiver;
import com.klinker.android.messaging_donate.DisconnectWifi;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.SentReceiver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.transaction.HttpUtils;
import com.android.mms.ui.ImageAttachmentView;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.MMSPart;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.SendReq;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.Profile;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.Telephony;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	public SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public static ViewPager mViewPager;

    public static String deviceType;
	
	public ArrayList<String> inboxNumber, inboxDate, inboxBody, inboxId;
	public ArrayList<Boolean> inboxSent, mms;
	public ArrayList<String> images;
	public ArrayList<String> group;
	public ArrayList<String> msgCount;
	public ArrayList<String> msgRead;

    public static boolean waitToLoad = false;
    public static boolean threadedLoad = true;
	
	public static String myPhoneNumber, myContactId;
	
	public ArrayList<String> contactNames, contactNumbers, contactTypes, threadIds;
	
	public static SlidingMenu menu;
	public boolean firstRun = true;
	public boolean firstContactSearch = true;
	public int contactSearchPosition = 0;
	public boolean refreshMyContact = true;
	
	public static boolean animationOn = false;
	public static int animationReceived = 0;
	public static int animationThread = 0;
	
	public BroadcastReceiver receiver;
	public BroadcastReceiver mmsReceiver;
	public DisconnectWifi discon;
	public WifiInfo currentWifi;
	public boolean currentWifiState;
	
	public static int contactWidth;
	public static String draft = "";
	public boolean jump = true;
	
	public ListView menuLayout;
	public MenuArrayAdapter menuAdapter;
	public static boolean messageRecieved = false;
	public static boolean sentMessage = false;
	public static boolean loadAll = false;
	
	public boolean sendTo = false;
	public String sendMessageTo;
	public String whatToSend = null;
	public boolean fromNotification = false;
	public String sendToThread = null;
	public String sendToMessage;
	
	public SharedPreferences sharedPrefs;
	
	public EditText messageEntry;
	public TextView mTextView;
	public ImageButton sendButton;
	public ImageButton emojiButton;
	public View v;
	public PagerTitleStrip title;
	public View imageAttachBackground;
	public ImageAttachmentView imageAttach;
	public View imageAttachBackground2;
	public ImageAttachmentView imageAttach2;
	public Uri attachedImage;
	public Uri attachedImage2;
	public int attachedPosition;
	
	public Uri capturedPhotoUri;
	public boolean fromCamera = false;
	public boolean multipleAttachments = false;
	
	public Typeface font;

    public static final String GSM_CHARACTERS_REGEX = "^[A-Za-z0-9 \\r\\n@Ł$ĽčéůěňÇŘřĹĺ\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EĆćßÉ!\"#$%&'()*+,\\-./:;<=>?ĄÄÖŃÜ§żäöńüŕ^{}\\\\\\[~\\]|\u20AC]*$";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout") && sharedPrefs.getString("ct_theme_name", "Light Theme").equals("Hangouts Theme"))
        {
            setTheme(R.style.HangoutsTheme);
        }

        getWindow().getDecorView().setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));
		setContentView(R.layout.activity_main);
		setTitle(R.string.app_name_in_app);
		
		getWindow().setBackgroundDrawable(null);
		
//		String version = "";
//
//    	try {
//			version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		if (!sharedPrefs.getString("current_version", "0").equals(version))
//		{
//			final Context context = this;
//
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle(getResources().getString(R.string.changelog_title));
//			builder.setMessage("Version " + version + ":\n\n" +
//                            "- Major rework of settings layout\n" +
//                            "- Added new 1x1 widget with unread counter\n" +
//                            "- More options for notification icons\n" +
//                            "- Layout optimizations\n" +
//                            "- Bug fixes\n\n" +
//                            getResources().getString(R.string.changelog_disclaimers));
//
//			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface arg0, int arg1) {
//					String version = "";
//
//			    	try {
//						version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
//					} catch (NameNotFoundException e) {
//						e.printStackTrace();
//					}
//
//					 Editor prefEdit = sharedPrefs.edit();
//		       		 prefEdit.putString("current_version", version);
//		       		 prefEdit.commit();
//				}
//
//			});
//
//			builder.create().show();
//		}
//
//		try
//      	 {
//      		 PackageManager pm = this.getPackageManager();
//      		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
//      	 } catch (PackageManager.NameNotFoundException e)
//      	 {
//      		if (sharedPrefs.getBoolean("show_theme_dialog", true))
//    		{
//    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//    			builder.setTitle(getResources().getString(R.string.theme_support_title));
//    			builder.setMessage("The theme editor now fully supports the Hangouts UI as well, better time then ever to get on board and start making Sliding Messaging look exactly how you want!\n\n" + getResources().getString(R.string.theme_support));
//
//    			builder.setPositiveButton(R.string.get_it_now, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_theme_dialog", false);
//    		       		 prefEdit.commit();
//
//    					 Intent intent = new Intent(Intent.ACTION_VIEW);
//    		       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
//    		       		 startActivity(intent);
//    				}
//
//    			});
//
//    			builder.setNeutralButton(R.string.remind_me_later, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_theme_dialog", true);
//    		       		 prefEdit.commit();
//    				}
//    			});
//
//    			builder.setNegativeButton(R.string.dont_bother, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_theme_dialog", false);
//    		       		 prefEdit.commit();
//    				}
//    			});
//
//    			builder.create().show();
//    		}
//      	 }
//
//		try
//		{
//			PackageManager pm = this.getPackageManager();
//			pm.getPackageInfo("com.klinker.android.messaging_donate", PackageManager.GET_ACTIVITIES);
//		} catch (Exception e)
//		{
//			if (sharedPrefs.getBoolean("show_pro_dialog", true))
//			{
//				AlertDialog.Builder builder = new AlertDialog.Builder(this);
//				builder.setTitle(getResources().getString(R.string.pro_dialog));
//				builder.setMessage(getResources().getString(R.string.go_pro1) +
//						           getResources().getString(R.string.go_pro2) +
//						           getResources().getString(R.string.go_pro3) +
//						           getResources().getString(R.string.go_pro4));
//
//				builder.setPositiveButton(R.string.get_it_now, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_pro_dialog", false);
//    		       		 prefEdit.commit();
//
//    					 Intent intent = new Intent(Intent.ACTION_VIEW);
//    		       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_donate"));
//    		       		 startActivity(intent);
//    				}
//
//    			});
//
//    			builder.setNeutralButton(R.string.remind_me_later, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_pro_dialog", true);
//    		       		 prefEdit.commit();
//    				}
//    			});
//
//    			builder.setNegativeButton(R.string.dont_bother, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_pro_dialog", false);
//    		       		 prefEdit.commit();
//    				}
//    			});
//
//    			builder.create().show();
//			}
//		}
//
//		try
//		{
//			 PackageManager pm = this.getPackageManager();
//     		 pm.getPackageInfo("com.jb.gosms", PackageManager.GET_ACTIVITIES);
//
//     		if (sharedPrefs.getBoolean("show_go_sms_dialog", true))
//    		{
//    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//    			builder.setTitle(getResources().getString(R.string.go_sms_title));
//    			builder.setMessage(getResources().getString(R.string.go_sms_body));
//
//    			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//    				@Override
//    				public void onClick(DialogInterface arg0, int arg1) {
//    					 Editor prefEdit = sharedPrefs.edit();
//    		       		 prefEdit.putBoolean("show_go_sms_dialog", false);
//    		       		 prefEdit.commit();
//    				}
//
//    			});
//
//    			builder.create().show();
//    		}
//		} catch (Exception e)
//		{
//
//		}
		
		Intent intent = getIntent();
		String action = intent.getAction();
		
		if (action != null)
		{
			if (action.equals(Intent.ACTION_SENDTO))
			{
				sendTo = true;
				
				try
				{
					if (intent.getDataString().startsWith("smsto:"))
					{
						sendMessageTo = Uri.decode(intent.getDataString()).substring("smsto:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
						fromNotification = false;
					} else
					{
						sendMessageTo = Uri.decode(intent.getDataString()).substring("sms:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
						fromNotification = false;
					}
				} catch (Exception e)
				{
					sendMessageTo = intent.getStringExtra("com.klinker.android.OPEN").replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
					fromNotification = true;
				}
			} else if (action.equals(Intent.ACTION_SEND))
			{
				Bundle extras = intent.getExtras();
				
				if (extras.containsKey(Intent.EXTRA_TEXT))
				{
					whatToSend = (String) extras.getCharSequence(Intent.EXTRA_TEXT);
				}
				
				if (extras.containsKey(Intent.EXTRA_STREAM))
				{
					sendTo = true;
					sendMessageTo = "";
					fromNotification = false;
					attachedImage2 = intent.getParcelableExtra(Intent.EXTRA_STREAM);
				}
			}
		} else
		{
			Bundle extras = intent.getExtras();
			
			if (extras != null)
			{
				if (extras.containsKey("com.klinker.android.OPEN_THREAD"))
				{
					sendToThread = extras.getString("com.klinker.android.OPEN_THREAD");
					sendToMessage = extras.getString("com.klinker.android.CURRENT_TEXT");
				}
			}
		}
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			try
			{
				font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", null));
			} catch (Exception e)
			{
				Editor edit = sharedPrefs.edit();
				edit.putBoolean("custom_font", false);
				edit.commit();
			}
		}
		
		if (sharedPrefs.getBoolean("quick_text", false))
		{
			Intent mIntent = new Intent(this, QuickTextService.class);
			this.startService(mIntent);
		} else
		{
			NotificationManager mNotificationManager =
		            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(2);
		}
		
		if (sharedPrefs.getBoolean("override_lang", false))
		{
			String languageToLoad  = "en";
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}
        
        title = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		
		if (sharedPrefs.getString("page_or_menu2", "2").equals("1"))
		{
			title.setTextSpacing(5000);
		}
		
		if (!sharedPrefs.getBoolean("custom_theme", false))
        {
        	if (sharedPrefs.getBoolean("title_text_color", false))
        	{
        		title.setTextColor(getResources().getColor(R.color.black));
        	}
        } else
        {
        	title.setTextColor(sharedPrefs.getInt("ct_titleBarTextColor", getResources().getColor(R.color.white)));
        }
        
        if (!sharedPrefs.getBoolean("title_caps", true))
        {
        	title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        }
		
        if (sharedPrefs.getBoolean("hide_title_bar", true))
        {
        	if (!sharedPrefs.getBoolean("custom_theme", false))
        	{
	        	String titleColor = sharedPrefs.getString("title_color", "blue");
				
				if (titleColor.equals("blue"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_blue));
				} else if (titleColor.equals("orange"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_orange));
				} else if (titleColor.equals("red"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_red));
				} else if (titleColor.equals("green"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_green));
				} else if (titleColor.equals("purple"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_purple));
				} else if (titleColor.equals("grey"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.grey));
				} else if (titleColor.equals("black"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.pitch_black));
				} else if (titleColor.equals("darkgrey"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.darkgrey));
				}
        	} else
        	{
        		title.setBackgroundColor(sharedPrefs.getInt("ct_titleBarColor", getResources().getColor(R.color.holo_blue)));
        	}
        } else
        {
        	title.setVisibility(View.GONE);
        }
        
        menuLayout = new ListView(this);
		
		myPhoneNumber = getMyPhoneNumber();
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(final Context context, Intent intent) {
			    	Bundle extras = intent.getExtras();
			        
			        String body = "";
			        String address = "";
			        String date = "";
			         
			        if ( extras != null )
			        {
			            Object[] smsExtra = (Object[]) extras.get( "pdus" );
			            
			            for ( int i = 0; i < smsExtra.length; ++i )
			            {
			                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
			                 
			                body += sms.getMessageBody().toString();
			                address = sms.getOriginatingAddress();
			                date = sms.getTimestampMillis() + "";
			            }
			        }
			        
			        Calendar cal = Calendar.getInstance();
			        ContentValues values = new ContentValues();
			        values.put("address", address);
			        values.put("body", body);
			        values.put("date", cal.getTimeInMillis() + "");
			        values.put("read", false);
			        values.put("date_sent", date);
			        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
			        
			        if (sharedPrefs.getBoolean("notifications", true))
			        {
				        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	
				        switch (am.getRingerMode()) {
				            case AudioManager.RINGER_MODE_SILENT:
				                break;
				            case AudioManager.RINGER_MODE_VIBRATE:
				            	if (sharedPrefs.getBoolean("vibrate", true))
						        {
						        	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
						        	
						        	if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
						        	{
							        	String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");
							        	
							        	if (vibPat.equals("short"))
							        	{
							        		long[] pattern = {0L, 400L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("long"))
							        	{
							        		long[] pattern = {0L, 800L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("2short"))
							        	{
							        		long[] pattern = {0L, 400L, 100L, 400L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("2long"))
							        	{
							        		long[] pattern = {0L, 800L, 200L, 800L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("3short"))
							        	{
							        		long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("3long"))
							        	{
							        		long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
							        		vibrator.vibrate(pattern, -1);
							        	}
						        	} else
						        	{
						        		try
						        		{
							        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
							        		long[] pattern = new long[vibPat.length];
							        		
							        		for (int i = 0; i < vibPat.length; i++)
							        		{
							        			pattern[i] = Long.parseLong(vibPat[i]);
							        		}
							        		
							        		vibrator.vibrate(pattern, -1);
						        		} catch (Exception e)
						        		{
						        			
						        		}
						        	}
						        }
				            	
				                break;
				            case AudioManager.RINGER_MODE_NORMAL:
				            	try
				            	{
					            	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					            	
					            	try
							        {
							        	notification = (Uri.parse(sharedPrefs.getString("ringtone", "null")));
							        } catch(Exception e)
							        {
							        	notification = (RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
							        }
					            	
					            	Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
					            	r.play();
					            	
					            	if (sharedPrefs.getBoolean("vibrate", true))
							        {
							        	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
							        	
							        	if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
							        	{
								        	String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");
								        	
								        	if (vibPat.equals("short"))
								        	{
								        		long[] pattern = {0L, 400L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("long"))
								        	{
								        		long[] pattern = {0L, 800L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("2short"))
								        	{
								        		long[] pattern = {0L, 400L, 100L, 400L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("2long"))
								        	{
								        		long[] pattern = {0L, 800L, 200L, 800L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("3short"))
								        	{
								        		long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("3long"))
								        	{
								        		long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
								        		vibrator.vibrate(pattern, -1);
								        	}
							        	} else
							        	{
							        		try
							        		{
								        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
								        		long[] pattern = new long[vibPat.length];
								        		
								        		for (int i = 0; i < vibPat.length; i++)
								        		{
								        			pattern[i] = Long.parseLong(vibPat[i]);
								        		}
								        		
								        		vibrator.vibrate(pattern, -1);
							        		} catch (Exception e)
							        		{
							        			
							        		}
							        	}
							        }
				            	} catch (Exception e)
				            	{
				            		
				            	}
				            	
				                break;
				        }
			        }
			        
			        messageRecieved = true;
			        
			        if (draft.equals(""))
			        {
			        	jump = false;
			        } else
			        {
			        	jump = false;
			        }

                    try
                    {
                        if (address.endsWith(inboxNumber.get(mViewPager.getCurrentItem())))
                        {
                            animationReceived = 1;
                            animationThread = mViewPager.getCurrentItem();
                        } else
                        {
                            animationReceived = 2;
                        }
                    } catch (Exception e)
                    {
                        animationReceived = 2;
                    }
			        
		        	refreshViewPager4(address, body, date);
		        	
		        	if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
					{
						final ActionBar ab = getActionBar();
						
						if (group.get(mViewPager.getCurrentItem()).equals("yes"))
						{
							ab.setTitle("Group MMS");
							ab.setSubtitle(null);
						} else
						{
							new Thread(new Runnable() {

								@Override
								public void run() {
									final String title = findContactName(inboxNumber.get(mViewPager.getCurrentItem()), context);
									
									((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
										
										@Override
										public void run() {
											ab.setTitle(title);
										}
								    	
								    });
									
								}
								
							}).start();
							
							Locale sCachedLocale = Locale.getDefault();
							int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
							Editable editable = new SpannableStringBuilder(inboxNumber.get(mViewPager.getCurrentItem()));
							PhoneNumberUtils.formatNumber(editable, sFormatType);
							ab.setSubtitle(editable.toString());
							
							if (ab.getTitle().equals(ab.getSubtitle()))
							{
								ab.setSubtitle(null);
							}
						}
					}
					
					if (sharedPrefs.getBoolean("title_contact_image", false))
			        {
			        	final ActionBar ab = getActionBar();
			        	
			        	new Thread(new Runnable() {

							@Override
							public void run() {
								final Bitmap image = getFacebookPhoto(inboxNumber.get(mViewPager.getCurrentItem()), context);
								final BitmapDrawable image2 = new BitmapDrawable(image);
								
								((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
									
									@Override
									public void run() {
										ab.setIcon(image2);
									}
							    	
							    });
								
							}

			        	}).start();
			        }
					
					Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
					context.sendBroadcast(updateWidget);
					
					abortBroadcast();
		        }
		};
		
		mmsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String currentThread = threadIds.get(mViewPager.getCurrentItem());
				
				refreshViewPager(true);
				
				for (int i = 0; i < threadIds.size(); i++)
				{
					if (currentThread.equals(threadIds.get(i)))
					{
						mViewPager.setCurrentItem(i, false);
						break;
					}
				}
				
			}
			
		};
		
		final float scale = getResources().getDisplayMetrics().density;
		MainActivity.contactWidth = (int) (64 * scale + 0.5f);
		
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

        if (!sharedPrefs.getString("run_as", "sliding").equals("hangout") || !sharedPrefs.getString("ct_theme_name", "Light Theme").equals("Hangouts Theme"))
        {
            ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

            if (sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)) == getResources().getColor(R.color.pitch_black))
            {
                if (!sharedPrefs.getBoolean("hide_title_bar", true))
                {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_blue));
                } else
                {
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.pitch_black)));
                }
            }
        } else
        {
            ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_hangouts));
        }
		
		View v = findViewById(R.id.newMessageGlow);
		v.setVisibility(View.GONE);
		
		setUpSendbar();
	}
	
	public void refreshMessages(boolean totalRefresh)
	{
		inboxSent = new ArrayList<Boolean>();
		inboxNumber = new ArrayList<String>();
		inboxDate = new ArrayList<String>();
		inboxBody = new ArrayList<String>();
		inboxId = new ArrayList<String>();
		threadIds = new ArrayList<String>();
		mms = new ArrayList<Boolean>();
		images = new ArrayList<String>();
		group = new ArrayList<String>();
		msgCount = new ArrayList<String>();
		msgRead = new ArrayList<String>();
		ContentResolver contentResolver = getContentResolver();
		
		if (!sharedPrefs.getBoolean("background_service", false) || totalRefresh)
		{
			try
			{
				String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
				Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
				Cursor query = contentResolver.query(uri, projection, null, null, "date desc");
				
				if (query.moveToFirst())
				{
					do
					{
						threadIds.add(query.getString(query.getColumnIndex("_id")));
						msgCount.add(query.getString(query.getColumnIndex("message_count")));
						msgRead.add(query.getString(query.getColumnIndex("read")));
						
						inboxBody.add(" ");
						
						try
						{
							inboxBody.set(inboxBody.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
						} catch (Exception e)
						{
						}
						
						inboxDate.add(query.getString(query.getColumnIndex("date")));
						
						String[] ids = query.getString(query.getColumnIndex("recipient_ids")).split(" ");
						String numbers = "";
						
						for (int i = 0; i < ids.length; i++)
						{
							try
							{
								if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
								{
									Cursor number = contentResolver.query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);
									
									if (number.moveToFirst())
									{
										numbers += number.getString(number.getColumnIndex("address")).replace("-", "").replace(")", "").replace("(", "").replace(" ", "") + " ";
									} else
									{
										numbers += "0 ";
									}
									
									number.close();
								} else
								{
									
								}
							} catch (Exception e)
							{
								numbers += "0 ";
							}
						}
						
						inboxNumber.add(numbers.trim());
						
						if (ids.length > 1)
						{
							group.add("yes");
						} else
						{
							group.add("no");
						}
					} while (query.moveToNext());
				}
				
				query.close();
			} catch (Exception e)
			{
				
			}
		} else
		{
			readFromFile3(this);
		}
		
		if (inboxNumber.size() > 0)
		{
			messageEntry.setVisibility(View.VISIBLE);
			sendButton.setVisibility(View.VISIBLE);
			v.setVisibility(View.VISIBLE);
			
			if (sharedPrefs.getBoolean("hide_title_bar", true))
			{
				title.setVisibility(View.VISIBLE);
			}
		} else
		{
			messageEntry.setVisibility(View.GONE);
			sendButton.setVisibility(View.GONE);
			v.setVisibility(View.GONE);
			title.setVisibility(View.GONE);
			emojiButton.setVisibility(View.GONE);
			
			getWindow().getDecorView().setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));
		}
		
		if (refreshMyContact)
		{
			String[] mProjection = new String[]
				    {
				        Profile._ID
				    };
	
			Cursor mProfileCursor =
				        getContentResolver().query(
				                Profile.CONTENT_URI,
				                mProjection ,
				                null,
				                null,
				                null);
			
			try
			{
				if (mProfileCursor.moveToFirst())
				{
					myContactId = mProfileCursor.getString(mProfileCursor.getColumnIndex(Profile._ID));
				}
			} catch (Exception e)
			{
				myContactId = myPhoneNumber;
			} finally
			{	
				mProfileCursor.close();
			}

			
			refreshMyContact = false;
		}
	}
	
	public static String findContactName(String number, Context context)
	{
		String name = "";
		
		String origin = number;
		
		if (origin.length() != 0)
		{
			Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
			Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

			if(phonesCursor != null && phonesCursor.moveToFirst()) {
				name = phonesCursor.getString(0);
			} else
			{
				if (!number.equals(""))
				{
					try
					{
						Locale sCachedLocale = Locale.getDefault();
						int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
						Editable editable = new SpannableStringBuilder(number);
						PhoneNumberUtils.formatNumber(editable, sFormatType);
						name = editable.toString();
					} catch (Exception e)
					{
						name = number;
					}
				} else
				{
					name = "No Information";
				}
			}
			
			phonesCursor.close();
		} else
		{			
			if (!number.equals(""))
			{
				try
				{
					Long.parseLong(number.replaceAll("[^0-9]", ""));
					Locale sCachedLocale = Locale.getDefault();
					int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
					Editable editable = new SpannableStringBuilder(number);
					PhoneNumberUtils.formatNumber(editable, sFormatType);
					name = editable.toString();
				} catch (Exception e)
				{
					name = number;
				}
			} else
			{
				name = "No Information";
			}
		}
		
		return name;
	}
	
	public static String findContactId(String number, Context context)
	{
		String name = "";
		
		String origin = number;
		
		if (origin.length() != 0)
		{
			Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
			Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

			if(phonesCursor != null && phonesCursor.moveToFirst()) {
				name = phonesCursor.getString(1);
			} else
			{
				name = "0";
			}
			
			phonesCursor.close();
		} else
		{			
			name = "0";
		}
		
		return name;
	}
	
	public static String loadGroupContacts(String numbers, Context context)
	{
		String names = "";
		String[] number;
		
		try
		{
			number = numbers.split(" ");
		} catch (Exception e)
		{
			return "";
		}
		
		for (int i = 0; i < number.length; i++)
		{				
			try
			{
				String origin = number[i];
				
				Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
				Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");
	
				if(phonesCursor != null && phonesCursor.moveToFirst()) {
					names += ", " + phonesCursor.getString(0);
				} else
				{
					try
					{
						Locale sCachedLocale = Locale.getDefault();
						int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
						Editable editable = new SpannableStringBuilder(number[i]);
						PhoneNumberUtils.formatNumber(editable, sFormatType);
						names += ", " + editable.toString();
					} catch (Exception e)
					{
						names += ", " + number;
					}
				}
				
				phonesCursor.close();
			} catch (IllegalArgumentException e)
			{
				try
				{
					Locale sCachedLocale = Locale.getDefault();
					int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
					Editable editable = new SpannableStringBuilder(number[i]);
					PhoneNumberUtils.formatNumber(editable, sFormatType);
					names += ", " + editable.toString();
				} catch (Exception f)
				{
					names += ", " + number;
				}
			}
		}
		
		try
		{
			return names.substring(2);
		} catch (Exception e)
		{
			return "";
		}
	}
	
	@SuppressWarnings("deprecation")
	public void setUpSendbar()
	{
		mTextView = (TextView) findViewById(R.id.charsRemaining2);
		messageEntry = (EditText) findViewById(R.id.messageEntry);
		sendButton = (ImageButton) findViewById(R.id.sendButton);
		emojiButton = (ImageButton) findViewById(R.id.display_emoji);
		v = findViewById(R.id.view1);
		imageAttachBackground = findViewById(R.id.image_attachment_view_background2);
		imageAttach = (ImageAttachmentView) findViewById(R.id.image_attachment_view);

        deviceType = messageEntry.getTag().toString();

        if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}

        if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            if (!sharedPrefs.getBoolean("keyboard_type", true))
            {
                messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            } else
            {
                messageEntry.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            }
        }
		
		mTextView.setVisibility(View.GONE);

		messageEntry.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));

	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}

	        	String patternStr = "[^" + MainActivity.GSM_CHARACTERS_REGEX + "]";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(s);

				int size = 160;

				if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false))
				{
					size = 70;
				}

	        	int pages = 1;

	        	while (length > size)
	        	{
	        		length-=size;
	        		pages++;
	        	}

	            mTextView.setText(pages + "/" + (size - length));

	            if ((pages == 1 && (size - length) <= 30) || pages != 1)
	            {
	            	mTextView.setVisibility(View.VISIBLE);
	            }

	            if ((pages + "/" + (size - length)).equals("1/31"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if ((pages + "/" + (size - length)).equals("1/160"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if (imageAttach.getVisibility() == View.VISIBLE || group.get(mViewPager.getCurrentItem()).equals("yes"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if (sharedPrefs.getBoolean("send_as_mms", false) && pages >= sharedPrefs.getInt("mms_after", 4))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if (sharedPrefs.getBoolean("send_with_return", false))
	            {
	            	if (messageEntry.getText().toString().endsWith("\n"))
	            	{
	            		messageEntry.setText(messageEntry.getText().toString().substring(0, messageEntry.getText().toString().length() - 1));
	            		sendButton.performClick();
	            	}
	            }
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});

		final Context context = this;
		
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.draft = "";
				
				Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
				context.sendBroadcast(updateWidget);
				
				boolean sendMmsFromLength = false;
				String[] counter = mTextView.getText().toString().split("/");
				
				if (Integer.parseInt(counter[0]) >= sharedPrefs.getInt("mms_after", 4) && sharedPrefs.getBoolean("send_as_mms", false))
				{
					sendMmsFromLength = true;
				}
				
				if (group.get(mViewPager.getCurrentItem()).equals("no") && imageAttach.getVisibility() == View.GONE && sendMmsFromLength == false)
				{
					if (messageEntry.getText().toString().equals(""))
					{
						Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
					} else
					{
						MainActivity.animationOn = true;
						
						if (sharedPrefs.getBoolean("hide_keyboard", false))
						{
							new Thread(new Runnable() {

								@Override
								public void run() {
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									} finally
									{
										InputMethodManager keyboard = (InputMethodManager)
								                getSystemService(Context.INPUT_METHOD_SERVICE);
								        keyboard.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
									}
									
								}
								
							}).start();
						}
						
						String body2 = messageEntry.getText().toString();
						
						if (!sharedPrefs.getString("signature", "").equals(""))
						{
							body2 += "\n" + sharedPrefs.getString("signature", "");
						}
						
						final String body = body2;
						final int position2 = mViewPager.getCurrentItem();
						
						new Thread(new Runnable() {

							@Override
							public void run() {
								
								if (sharedPrefs.getBoolean("delivery_reports", false))
								{
									if (!inboxNumber.get(position2).replaceAll("[^0-9]", "").equals(""))
									{
										String SENT = "SMS_SENT";
								        String DELIVERED = "SMS_DELIVERED";
								 
								        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
								            new Intent(SENT), 0);
								 
								        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
								            new Intent(DELIVERED), 0);
								 
								        //---when the SMS has been sent---
								        context.registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
								                switch (getResultCode())
								                {
								                case Activity.RESULT_OK:
							                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
							                        
							                        if (query.moveToFirst())
							                        {
							                        	String id = query.getString(query.getColumnIndex("_id"));
							                        	ContentValues values = new ContentValues();
							                        	values.put("type", "2");
							                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
							                        	((MainActivity) context).refreshViewPager3();
							                        }
							                        
							                        query.close();
							                        
							                        break;
							                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
							                    	try
							                    	{
							                    		wait(500);
							                    	} catch (Exception e)
							                    	{
							                    		
							                    	}
							                    	
							                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
							                        
							                        if (query.moveToFirst())
							                        {
							                        	String id = query.getString(query.getColumnIndex("_id"));
							                        	ContentValues values = new ContentValues();
							                        	values.put("type", "5");
							                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
							                        	((MainActivity) context).refreshViewPager3();
							                        }
							                        
							                    	NotificationCompat.Builder mBuilder =
							    	                	new NotificationCompat.Builder(context)
							    	                .setSmallIcon(R.drawable.ic_alert)
							    	                .setContentTitle("Error")
							    	                .setContentText("Could not send message");
									    	        
									    	        Intent resultIntent = new Intent(context, MainActivity.class);
									    	
									    	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
									    	        stackBuilder.addParentStack(MainActivity.class);
									    	        stackBuilder.addNextIntent(resultIntent);
									    	        PendingIntent resultPendingIntent =
									    	                stackBuilder.getPendingIntent(
									    	                    0,
									    	                    PendingIntent.FLAG_UPDATE_CURRENT
									    	                );
									    	        
									    	        mBuilder.setContentIntent(resultPendingIntent);
									    	        mBuilder.setAutoCancel(true);
									    	        long[] pattern = {0L, 400L, 100L, 400L};
									    	        mBuilder.setVibrate(pattern);
									    	        mBuilder.setLights(0xFFffffff, 1000, 2000);
									    	        
									    	        try
									    	        {
									    	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
									    	        } catch(Exception e)
									    	        {
									    	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
									    	        }
									    	        
									    	        NotificationManager mNotificationManager =
									    	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
									    	        
									    	        Notification notification = mBuilder.build();
									    	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
									    	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
									    	        mNotificationManager.notify(1, notification);
							                        break;
							                    case SmsManager.RESULT_ERROR_NO_SERVICE:
							                    	try
							                    	{
							                    		wait(500);
							                    	} catch (Exception e)
							                    	{
							                    		
							                    	}
							                    	
							                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
							                        
							                        if (query.moveToFirst())
							                        {
							                        	String id = query.getString(query.getColumnIndex("_id"));
							                        	ContentValues values = new ContentValues();
							                        	values.put("type", "5");
							                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
							                        	((MainActivity) context).refreshViewPager3();
							                        }
							                        
							                        Toast.makeText(context, "No service", 
							                                Toast.LENGTH_SHORT).show();
							                        break;
							                    case SmsManager.RESULT_ERROR_NULL_PDU:
							                    	try
							                    	{
							                    		wait(500);
							                    	} catch (Exception e)
							                    	{
							                    		
							                    	}
							                    	
							                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
							                        
							                        if (query.moveToFirst())
							                        {
							                        	String id = query.getString(query.getColumnIndex("_id"));
							                        	ContentValues values = new ContentValues();
							                        	values.put("type", "5");
							                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
							                        	((MainActivity) context).refreshViewPager3();
							                        }
							                        
							                        Toast.makeText(context, "Null PDU", 
							                                Toast.LENGTH_SHORT).show();
							                        break;
							                    case SmsManager.RESULT_ERROR_RADIO_OFF:
							                    	try
							                    	{
							                    		wait(500);
							                    	} catch (Exception e)
							                    	{
							                    		
							                    	}
							                    	
							                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
							                        
							                        if (query.moveToFirst())
							                        {
							                        	String id = query.getString(query.getColumnIndex("_id"));
							                        	ContentValues values = new ContentValues();
							                        	values.put("type", "5");
							                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
							                        	((MainActivity) context).refreshViewPager3();
							                        }
							                        
							                        Toast.makeText(context, "Radio off", 
							                                Toast.LENGTH_SHORT).show();
							                        break;
							                }
								                
								                context.unregisterReceiver(this);
								            }
								        }, new IntentFilter(SENT));
								 
								        //---when the SMS has been delivered---
								        context.registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
								            	if (sharedPrefs.getString("delivery_options", "2").equals("1"))
								            	{
									                switch (getResultCode())
									                {
									                    case Activity.RESULT_OK:
									                    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
									                    	
									                    	try
									                    	{
									                    		builder.setTitle(loadGroupContacts(inboxNumber.get(position2), context));
									                    	} catch (Exception e)
									                    	{
									                    		
									                    	}
									                    	
									                        builder.setMessage(R.string.message_delivered)
									                               .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
									                                   public void onClick(DialogInterface dialog, int id) {
									                                       dialog.dismiss();
									                                   }
									                               });
									                        
									                        builder.create().show();
									                        
									                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
									                        
									                        if (query.moveToFirst())
									                        {
									                        	String id = query.getString(query.getColumnIndex("_id"));
									                        	ContentValues values = new ContentValues();
									                        	values.put("status", "0");
									                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
									                        	((MainActivity) context).refreshViewPager3();
									                        }
									                        
									                        query.close();
									                        
									                        break;
									                    case Activity.RESULT_CANCELED:
									                    	AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
									                    	
									                    	try
									                    	{
									                    		builder2.setTitle(loadGroupContacts(inboxNumber.get(position2), context));
									                    	} catch (Exception e)
									                    	{
									                    		
									                    	}
									                    	
									                        builder2.setMessage(R.string.message_not_delivered)
									                               .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
									                                   public void onClick(DialogInterface dialog, int id) {
									                                       dialog.dismiss();
									                                   }
									                               });
									                        
									                        builder2.create().show();
									                        
									                        Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
									                        
									                        if (query2.moveToFirst())
									                        {
									                        	String id = query2.getString(query2.getColumnIndex("_id"));
									                        	ContentValues values = new ContentValues();
									                        	values.put("status", "64");
									                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
									                        	((MainActivity) context).refreshViewPager3();
									                        }
									                        
									                        query2.close();
									                        
									                        break;
									                }
								            	} else
								            	{
								            		switch (getResultCode())
									                {
									                    case Activity.RESULT_OK:
									                    	Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
									                    	
									                    	Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
									                        
									                        if (query.moveToFirst())
									                        {
									                        	String id = query.getString(query.getColumnIndex("_id"));
									                        	ContentValues values = new ContentValues();
									                        	values.put("status", "0");
									                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
									                        	((MainActivity) context).refreshViewPager3();
									                        }
									                        
									                        query.close();
								                    		
									                        break;
									                    case Activity.RESULT_CANCELED:
									                    	Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
									                    	
									                    	Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
									                        
									                        if (query2.moveToFirst())
									                        {
									                        	String id = query2.getString(query2.getColumnIndex("_id"));
									                        	ContentValues values = new ContentValues();
									                        	values.put("status", "64");
									                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
									                        	((MainActivity) context).refreshViewPager3();
									                        }
									                        
									                        query2.close();
									                    	
									                        break;
									                }
								            	}
								                
								                context.unregisterReceiver(this);
								            }
								        }, new IntentFilter(DELIVERED));
								        
								        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
								        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
								        
								        String body2 = body;
								        
								        if (sharedPrefs.getBoolean("strip_unicode", false))
								        {
								        	body2 = StripAccents.stripAccents(body2);
								        }
								        
										SmsManager smsManager = SmsManager.getDefault();

										if (sharedPrefs.getBoolean("split_sms", false))
										{
											int length = 160;
											
											String patternStr = "[^\\x20-\\x7E]";
											Pattern pattern = Pattern.compile(patternStr);
											Matcher matcher = pattern.matcher(body2);
											  
											if (matcher.find())
											{
												length = 70;
											}
											
											String[] textToSend = splitByLength(body2, length);
											
											for (int i = 0; i < textToSend.length; i++)
											{
												ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
												
												for (int j = 0; j < parts.size(); j++)
												{
													sPI.add(sentPI);
													dPI.add(deliveredPI);
												}
												
												smsManager.sendMultipartTextMessage(inboxNumber.get(position2), null, parts, sPI, dPI);
											}
										} else
										{
											ArrayList<String> parts = smsManager.divideMessage(body2); 
											
											for (int i = 0; i < parts.size(); i++)
											{
												sPI.add(sentPI);
												dPI.add(deliveredPI);
											}
											
											smsManager.sendMultipartTextMessage(inboxNumber.get(position2), null, parts, sPI, dPI);
										}
									} else
									{
									}
								} else
								{
									if (!inboxNumber.get(position2).replaceAll("[^0-9]", "").equals(""))
									{
										String SENT = "SMS_SENT";
										 
								        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
								            new Intent(SENT), 0);
								 
								        //---when the SMS has been sent---
								        context.registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
								                switch (getResultCode())
								                {
								                    case Activity.RESULT_OK:
								                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "2");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        query.close();
								                        
								                        break;
								                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                    	NotificationCompat.Builder mBuilder =
								    	                	new NotificationCompat.Builder(context)
								    	                .setSmallIcon(R.drawable.ic_alert)
								    	                .setContentTitle("Error")
								    	                .setContentText("Could not send message");
										    	        
										    	        Intent resultIntent = new Intent(context, MainActivity.class);
										    	
										    	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
										    	        stackBuilder.addParentStack(MainActivity.class);
										    	        stackBuilder.addNextIntent(resultIntent);
										    	        PendingIntent resultPendingIntent =
										    	                stackBuilder.getPendingIntent(
										    	                    0,
										    	                    PendingIntent.FLAG_UPDATE_CURRENT
										    	                );
										    	        
										    	        mBuilder.setContentIntent(resultPendingIntent);
										    	        mBuilder.setAutoCancel(true);
										    	        long[] pattern = {0L, 400L, 100L, 400L};
										    	        mBuilder.setVibrate(pattern);
										    	        mBuilder.setLights(0xFFffffff, 1000, 2000);
										    	        
										    	        try
										    	        {
										    	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
										    	        } catch(Exception e)
										    	        {
										    	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
										    	        }
										    	        
										    	        NotificationManager mNotificationManager =
										    	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
										    	        
										    	        Notification notification = mBuilder.build();
										    	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
										    	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
										    	        mNotificationManager.notify(1, notification);
								                        break;
								                    case SmsManager.RESULT_ERROR_NO_SERVICE:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "No service", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                    case SmsManager.RESULT_ERROR_NULL_PDU:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "Null PDU", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                    case SmsManager.RESULT_ERROR_RADIO_OFF:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "Radio off", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                }
								                
								                context.unregisterReceiver(this);
								            }
								        }, new IntentFilter(SENT));
								        
								        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
								        
								        String body2 = body;
								        
								        if (sharedPrefs.getBoolean("strip_unicode", false))
								        {
								        	body2 = StripAccents.stripAccents(body2);
								        }
								        
										SmsManager smsManager = SmsManager.getDefault();

										if (sharedPrefs.getBoolean("split_sms", false))
										{
											int length = 160;
											
											String patternStr = "[^\\x20-\\x7E]";
											Pattern pattern = Pattern.compile(patternStr);
											Matcher matcher = pattern.matcher(body2);
											  
											if (matcher.find())
											{
												length = 70;
											}
											
											String[] textToSend = splitByLength(body2, length);
											
											for (int i = 0; i < textToSend.length; i++)
											{
												ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
												
												for (int j = 0; j < parts.size(); j++)
												{
													sPI.add(sentPI);
												}
												
												smsManager.sendMultipartTextMessage(inboxNumber.get(position2), null, parts, sPI, null);
											}
										} else
										{
											ArrayList<String> parts = smsManager.divideMessage(body2); 
											
											for (int i = 0; i < parts.size(); i++)
											{
												sPI.add(sentPI);
											}
											
											smsManager.sendMultipartTextMessage(inboxNumber.get(position2), null, parts, sPI, null);
										}
									} else
									{
									}
								}
								
								String address = inboxNumber.get(position2);
								String body2 = body;
								
								if (sharedPrefs.getBoolean("strip_unicode", false))
								{
									body2 = StripAccents.stripAccents(body2);
								}
							    
								if (!address.replaceAll("[^0-9]", "").equals(""))
								{
								    final Calendar cal = Calendar.getInstance();
								    ContentValues values = new ContentValues();
								    values.put("address", address);
								    values.put("body", body2); 
								    values.put("date", cal.getTimeInMillis() + "");
								    values.put("read", true);
								    context.getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
									
								    final String address2 = address;
								    
								    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
	
										@Override
										public void run() {
											MainActivity.sentMessage = true;
                                            MainActivity.threadedLoad = false;
								        	refreshViewPager4(address2, StripAccents.stripAccents(body), cal.getTimeInMillis() + "");
								        	mViewPager.setCurrentItem(0);
								        	mTextView.setVisibility(View.GONE);
										}
								    	
								    });
								}
							}
							
						}).start();
						
						messageEntry.setText("");
					}
				} else
				{
					Bitmap b;
					byte[] byteArray;
					
					try
					{
						if (!fromCamera)
						{
							b = decodeFile2(new File(getPath(attachedImage)));
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							b.compress(Bitmap.CompressFormat.PNG, 100, stream);
							byteArray = stream.toByteArray();
						} else
						{
							b = decodeFile2(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							b.compress(Bitmap.CompressFormat.PNG, 100, stream);
							byteArray = stream.toByteArray();
						}
					} catch (Exception e)
					{
						byteArray = null;
					}
					
					String body = messageEntry.getText().toString();
					
					String[] to = ("insert-address-token " + inboxNumber.get(mViewPager.getCurrentItem())).split(" ");

                    if (!sharedPrefs.getBoolean("send_with_stock", false))
                    {
                        if (multipleAttachments == false)
                        {
                            insert(context, to, "", byteArray, body);

                            MMSPart[] parts = new MMSPart[2];

                            if (imageAttach.getVisibility() == View.VISIBLE)
                            {
                                parts[0] = new MMSPart();
                                parts[0].Name = "Image";
                                parts[0].MimeType = "image/png";
                                parts[0].Data = byteArray;

                                if (!body.equals(""))
                                {
                                    parts[1] = new MMSPart();
                                    parts[1].Name = "Text";
                                    parts[1].MimeType = "text/plain";
                                    parts[1].Data = body.getBytes();
                                }
                            } else
                            {
                                parts[0] = new MMSPart();
                                parts[0].Name = "Text";
                                parts[0].MimeType = "text/plain";
                                parts[0].Data = body.getBytes();
                            }

                            sendMMS(inboxNumber.get(mViewPager.getCurrentItem()), parts);
                        } else
                        {
                            ArrayList<byte[]> bytes = new ArrayList<byte[]>();
                            ArrayList<String> mimes = new ArrayList<String>();

                            for (int i = 0; i < AttachMore.data.size(); i++)
                            {
                                bytes.add(AttachMore.data.get(i).Data);
                                mimes.add(AttachMore.data.get(i).MimeType);
                            }

                            insert(context, to, "", bytes, mimes, body);

                            MMSPart part = new MMSPart();
                            part.Name = "Text";
                            part.MimeType = "text/plain";
                            part.Data = body.getBytes();
                            AttachMore.data.add(part);

                            sendMMS(inboxNumber.get(mViewPager.getCurrentItem()), AttachMore.data.toArray(new MMSPart[AttachMore.data.size()]));

                            AttachMore.data = new ArrayList<MMSPart>();
                        }
                    } else
                    {
                        if (multipleAttachments == false)
                        {
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.putExtra("address", inboxNumber.get(mViewPager.getCurrentItem()));
                            sendIntent.putExtra("sms_body", body);
                            sendIntent.putExtra(Intent.EXTRA_STREAM, attachedImage);
                            sendIntent.setType("image/png");
                            startActivity(sendIntent);

                            com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
                        } else
                        {
                            Toast.makeText(context, "Cannot send multiple images through stock", Toast.LENGTH_SHORT).show();
                        }
                    }
					
					messageEntry.setText("");
					imageAttach.setVisibility(false);
					imageAttachBackground.setVisibility(View.GONE);
					
					refreshViewPager4(inboxNumber.get(mViewPager.getCurrentItem()), StripAccents.stripAccents(body), "0");
				}
			}
			
		});
		
		messageEntry.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)messageEntry.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			messageEntry.setLayoutParams(params);
		} else
		{
			emojiButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Insert Emojis");
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View frame = inflater.inflate(R.layout.emoji_frame, null);
					
					final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);
					
					final StickyGridHeadersGridView emojiGrid = (StickyGridHeadersGridView) frame.findViewById(R.id.emojiGrid);
					Button okButton = (Button) frame.findViewById(R.id.emoji_ok);
					
					if (sharedPrefs.getBoolean("emoji_type", true))
					{
						emojiGrid.setAdapter(new EmojiAdapter2(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
					} else
					{
						emojiGrid.setAdapter(new EmojiAdapter(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
					}
					
					builder.setView(frame);
					final AlertDialog dialog = builder.create();
					dialog.show();
					
					okButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (sharedPrefs.getBoolean("emoji_type", true))
							{
								messageEntry.setText(EmojiConverter2.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
								messageEntry.setSelection(messageEntry.getText().length());
							} else
							{
								messageEntry.setText(EmojiConverter.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
								messageEntry.setSelection(messageEntry.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}
		
		mTextView.setTextColor(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		v.setBackgroundColor(sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white)));
		sendButton.setImageResource(R.drawable.ic_action_send_white);
		sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		sendButton.setColorFilter(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		emojiButton.setColorFilter(sharedPrefs.getInt("ct_emojiButtonColor", getResources().getColor(R.color.emoji_button)));
		messageEntry.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black))));
		imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", context.getResources().getColor(R.color.light_silver)));
		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)), Mode.MULTIPLY);
		imageAttach.setBackgroundDrawable(attachBack);
		imageAttachBackground.setVisibility(View.GONE);
		imageAttach.setVisibility(false);
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			mTextView.setTypeface(font);
			messageEntry.setTypeface(font);
		}

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout"))
        {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }
	}
	
	@SuppressWarnings("deprecation")
	public void createMenu()
	{
        if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
            newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));

            if (sharedPrefs.getBoolean("custom_background", false))
            {
                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 1;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background_location", "")).getPath(),options);
                    this.getResources();
                    Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
                    newFragment.getView().setBackgroundDrawable(d);
                } catch (Exception e)
                {

                }
            } else
            {
                newFragment.getView().setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
            }

            newFragment.getListView().setDivider(new ColorDrawable(sharedPrefs.getInt("ct_conversationDividerColor", getResources().getColor(R.color.white))));

            if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true))
            {
                newFragment.getListView().setDividerHeight(1);
            } else
            {
                newFragment.getListView().setDividerHeight(0);
            }
        } else
        {
            if (sharedPrefs.getBoolean("custom_background", false))
            {
                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 1;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background_location", "")).getPath(),options);
                    this.getResources();
                    Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
                    menuLayout.setBackgroundDrawable(d);
                } catch (Exception e)
                {

                }
            } else
            {
                menuLayout.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
            }

            menuAdapter = new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
            menuLayout.setAdapter(menuAdapter);
            menuLayout.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_conversationDividerColor", getResources().getColor(R.color.white))));

            if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true))
            {
                menuLayout.setDividerHeight(1);
            } else
            {
                menuLayout.setDividerHeight(0);
            }
        }
		
		LayoutInflater inflater2 = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View newMessageView = inflater2.inflate(R.layout.new_message_frame, (ViewGroup) this.getWindow().getDecorView(), false);
		
		final TextView mTextView = (TextView) newMessageView.findViewById(R.id.charsRemaining2);
		final EditText mEditText = (EditText) newMessageView.findViewById(R.id.messageEntry2);
		final ImageButton sendButton = (ImageButton) newMessageView.findViewById(R.id.sendButton);
		imageAttachBackground2 = newMessageView.findViewById(R.id.image_attachment_view_background);
		imageAttach2 = (ImageAttachmentView) newMessageView.findViewById(R.id.image_attachment_view);
		
		mTextView.setVisibility(View.GONE);
		mEditText.requestFocus();
		
		mEditText.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));
	        	
	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}
	        	
	        	String patternStr = "[^" + MainActivity.GSM_CHARACTERS_REGEX + "]";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(s);
				
				int size = 160;
				
				if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false))
				{
					size = 70;
				}
	        	
	        	int pages = 1;
	        	
	        	while (length > size)
	        	{
	        		length-=size;
	        		pages++;
	        	}
	        	
	            mTextView.setText(pages + "/" + (size - length));
	            
	            if ((pages == 1 && (size - length) <= 30) || pages != 1)
	            {
	            	mTextView.setVisibility(View.VISIBLE);
	            }
	            
	            if ((pages + "/" + (size - length)).equals("1/31"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if ((pages + "/" + (size - length)).equals("1/160"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if (imageAttach2.getVisibility() == View.VISIBLE)
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if (sharedPrefs.getBoolean("send_as_mms", false) && pages >= sharedPrefs.getInt("mms_after", 4))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if (sharedPrefs.getBoolean("send_with_return", false))
	            {
	            	if (mEditText.getText().toString().endsWith("\n"))
	            	{
	            		mEditText.setText(mEditText.getText().toString().substring(0, mEditText.getText().toString().length() - 1));
	            		sendButton.performClick();
	            	}
	            }
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}
		
		final Context context = (Context) this;
		final EditText contact = (EditText) newMessageView.findViewById(R.id.contactEntry);

		contact.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	if (firstContactSearch)
	        	{
	        		try
	        		{
	        			contactNames = new ArrayList<String>();
	        			contactNumbers = new ArrayList<String>();
	        			contactTypes = new ArrayList<String>();
	        			
	        			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        			String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	        			                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};
	        	
	        			Cursor people = getContentResolver().query(uri, projection, null, null, null);
	        	
	        			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
	        			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
	        	
	        			people.moveToFirst();
	        			do {
	        				int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	        				String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
	        				
	        				if (sharedPrefs.getBoolean("mobile_only", false))
	        				{
	        					if (type == 2)
	        					{
	        						contactNames.add(people.getString(indexName));
	    	        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        						contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        					}
	        				} else
	        				{
	        					contactNames.add(people.getString(indexName));
		        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        					contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        				}
	        			} while (people.moveToNext());
	        			people.close();
	        		} catch (IllegalArgumentException e)
	        		{
	        			
	        		}
	        		
	        		firstContactSearch = false;
	        	}
	        }

	        @SuppressLint("DefaultLocale")
			public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	ArrayList<String> searchedNames = new ArrayList<String>();
	        	ArrayList<String> searchedNumbers = new ArrayList<String>();
	        	ArrayList<String> searchedTypes = new ArrayList<String>();
	        	
	        	String text = contact.getText().toString();
	        	
	        	String[] text2 = text.split("; ");
	        	
	        	text = text2[text2.length-1];
	        	
	        	if (text.startsWith("+"))
	        	{
	        		text = text.substring(1);
	        	}
	        	
	        	Pattern pattern;
	        	
	        	try
	        	{
	        		pattern = Pattern.compile(text.toLowerCase());
	        	} catch (Exception e)
	        	{
	        		pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
	        	}
	        	
			    for (int i = 0; i < contactNames.size(); i++)
			    {
			    	try
			    	{
			    		Long.parseLong(text);
			    		
				        if (text.length() <= contactNumbers.get(i).length())
				        {
				        	Matcher matcher = pattern.matcher(contactNumbers.get(i));
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	} catch (Exception e)
			    	{
			    		if (text.length() <= contactNames.get(i).length())
				        {
			    			Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	}
			    }
	        	
		        ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);
		        ContactSearchArrayAdapter adapter;
		        
		        if (text.length() != 0)
		        {
	        		adapter = new ContactSearchArrayAdapter((Activity)context, searchedNames, searchedNumbers, searchedTypes);
		        } else
		        {
	        		adapter = new ContactSearchArrayAdapter((Activity)context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		        }
	        	
	        	searchView.setAdapter(adapter);
	        	
	        	searchView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);
						
						String[] t1 = contact.getText().toString().split("; ");
						String string = "";
						
						for (int i = 0; i < t1.length - 1; i++)
						{
							string += t1[i] + "; ";
						}
						
						contact.setText(string + view2.getText() + "; ");
						contact.setSelection(contact.getText().toString().length());
						
						if (contact.getText().toString().length() <= 12)
						{
							mEditText.requestFocus();
						}
						
					}
	        		
	        	});
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
				context.sendBroadcast(updateWidget);
				
				if (contact.getText().toString().equals(""))
				{
					Toast.makeText(context, "ERROR: No valid recipients", Toast.LENGTH_SHORT).show();
				} else if (mEditText.getText().toString().equals("") && imageAttach2.getVisibility() == View.GONE)
				{
					Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
				} else
				{
					MainActivity.animationOn = true;
					
					if (sharedPrefs.getBoolean("hide_keyboard", false))
					{
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								} finally
								{
									InputMethodManager keyboard = (InputMethodManager)
							                getSystemService(Context.INPUT_METHOD_SERVICE);
							        keyboard.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
								}
								
							}
							
						}).start();
					}
					
					String[] contacts = contact.getText().toString().split("; ");
					final int contactLength = contacts.length;
					
					boolean sendMmsFromLength = false;
					String[] counter = mTextView.getText().toString().split("/");
					
					if (Integer.parseInt(counter[0]) >= sharedPrefs.getInt("mms_after", 4) && sharedPrefs.getBoolean("send_as_mms", false))
					{
						sendMmsFromLength = true;
					}
					
					if ((imageAttach2.getVisibility() == View.GONE) && (sendMmsFromLength == false) && (contactLength == 1 || (contactLength > 1 && sharedPrefs.getBoolean("group_message", false) == false)))
					{
						for (int i = 0; i < contacts.length; i++)
						{
							String body2 = mEditText.getText().toString();
							
							if (!sharedPrefs.getString("signature", "").equals(""))
							{
								body2 += "\n" + sharedPrefs.getString("signature", "");
							}
							
							final String body = body2;
							final int index = i;
							final String address = contacts[i];
							
							new Thread(new Runnable() {
		
								@Override
								public void run() {
									try
									{
										if(sharedPrefs.getBoolean("delivery_reports", false))
										{
											String SENT = "SMS_SENT";
									        String DELIVERED = "SMS_DELIVERED";
									 
									        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
									            new Intent(SENT), 0);
									 
									        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
									            new Intent(DELIVERED), 0);
									 
									        //---when the SMS has been sent---
									        registerReceiver(new BroadcastReceiver(){
									            @Override
									            public void onReceive(Context arg0, Intent arg1) {
									                switch (getResultCode())
									                {
									                case Activity.RESULT_OK:
								                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "2");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        query.close();
								                        
								                        break;
								                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
								                    	
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                    	NotificationCompat.Builder mBuilder =
								    	                	new NotificationCompat.Builder(context)
								    	                .setSmallIcon(R.drawable.ic_alert)
								    	                .setContentTitle("Error")
								    	                .setContentText("Could not send message");
										    	        
										    	        Intent resultIntent = new Intent(context, MainActivity.class);
										    	
										    	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
										    	        stackBuilder.addParentStack(MainActivity.class);
										    	        stackBuilder.addNextIntent(resultIntent);
										    	        PendingIntent resultPendingIntent =
										    	                stackBuilder.getPendingIntent(
										    	                    0,
										    	                    PendingIntent.FLAG_UPDATE_CURRENT
										    	                );
										    	        
										    	        mBuilder.setContentIntent(resultPendingIntent);
										    	        mBuilder.setAutoCancel(true);
										    	        long[] pattern = {0L, 400L, 100L, 400L};
										    	        mBuilder.setVibrate(pattern);
										    	        mBuilder.setLights(0xFFffffff, 1000, 2000);
										    	        
										    	        try
										    	        {
										    	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
										    	        } catch(Exception e)
										    	        {
										    	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
										    	        }
										    	        
										    	        NotificationManager mNotificationManager =
										    	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
										    	        
										    	        Notification notification = mBuilder.build();
										    	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
										    	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
										    	        mNotificationManager.notify(1, notification);
								                        break;
								                    case SmsManager.RESULT_ERROR_NO_SERVICE:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "No service", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                    case SmsManager.RESULT_ERROR_NULL_PDU:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "Null PDU", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                    case SmsManager.RESULT_ERROR_RADIO_OFF:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "Radio off", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                }
									                
									                context.unregisterReceiver(this);
									            }
									        }, new IntentFilter(SENT));
									 
									        //---when the SMS has been delivered---
									        registerReceiver(new BroadcastReceiver(){
									            @Override
									            public void onReceive(Context arg0, Intent arg1) {
									            	if (sharedPrefs.getString("delivery_options", "2").equals("1"))
									            	{
										                switch (getResultCode())
										                {
										                    case Activity.RESULT_OK:
										                    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
										                        builder.setMessage(R.string.message_delivered)
										                               .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
										                                   public void onClick(DialogInterface dialog, int id) {
										                                       dialog.dismiss();
										                                   }
										                               });
										                        
										                        builder.create().show();
										                        
										                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
										                        
										                        if (query.moveToFirst())
										                        {
										                        	String id = query.getString(query.getColumnIndex("_id"));
										                        	ContentValues values = new ContentValues();
										                        	values.put("status", "0");
										                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
										                        	((MainActivity) context).refreshViewPager3();
										                        }
										                        
										                        query.close();
										                        
										                        break;
										                    case Activity.RESULT_CANCELED:
										                    	AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
										                        builder2.setMessage(R.string.message_not_delivered)
										                               .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
										                                   public void onClick(DialogInterface dialog, int id) {
										                                       dialog.dismiss();
										                                   }
										                               });
										                        
										                        builder2.create().show();
										                        
										                        Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
										                        
										                        if (query2.moveToFirst())
										                        {
										                        	String id = query2.getString(query2.getColumnIndex("_id"));
										                        	ContentValues values = new ContentValues();
										                        	values.put("status", "64");
										                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
										                        	((MainActivity) context).refreshViewPager3();
										                        }
										                        
										                        query2.close();
										                        
										                        break;
										                }
									            	} else
									            	{
									            		switch (getResultCode())
										                {
										                    case Activity.RESULT_OK:
										                    	Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
										                    	
										                    	Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
										                        
										                        if (query.moveToFirst())
										                        {
										                        	String id = query.getString(query.getColumnIndex("_id"));
										                        	ContentValues values = new ContentValues();
										                        	values.put("status", "0");
										                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
										                        	((MainActivity) context).refreshViewPager3();
										                        }
										                        
										                        query.close();
										                        
										                        break;
										                    case Activity.RESULT_CANCELED:
										                    	Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
										                    	
										                    	Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
										                        
										                        if (query2.moveToFirst())
										                        {
										                        	String id = query2.getString(query2.getColumnIndex("_id"));
										                        	ContentValues values = new ContentValues();
										                        	values.put("status", "64");
										                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
										                        	((MainActivity) context).refreshViewPager3();
										                        }
										                        
										                        query2.close();
										                        
										                        break;
										                }
									            	}
									                
									                context.unregisterReceiver(this);
									            }
									        }, new IntentFilter(DELIVERED));
									        
									        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
									        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
									        
									        String body2 = body;
									        
									        if (sharedPrefs.getBoolean("strip_unicode", false))
									        {
									        	body2 = StripAccents.stripAccents(body2);
									        }
									        
											SmsManager smsManager = SmsManager.getDefault();

											if (sharedPrefs.getBoolean("split_sms", false))
											{
												int length = 160;
												
												String patternStr = "[^\\x20-\\x7E]";
												Pattern pattern = Pattern.compile(patternStr);
												Matcher matcher = pattern.matcher(body2);
												  
												if (matcher.find())
												{
													length = 70;
												}
												
												String[] textToSend = splitByLength(body2, length);
												
												for (int i = 0; i < textToSend.length; i++)
												{
													ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
													
													for (int j = 0; j < parts.size(); j++)
													{
														sPI.add(sentPI);
														dPI.add(deliveredPI);
													}
													
													smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
												}
											} else
											{
												ArrayList<String> parts = smsManager.divideMessage(body2); 
												
												for (int i = 0; i < parts.size(); i++)
												{
													sPI.add(sentPI);
													dPI.add(deliveredPI);
												}
												
												smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
											}
										} else
										{
											String SENT = "SMS_SENT";
											 
									        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
									            new Intent(SENT), 0);
									 
									        //---when the SMS has been sent---
									        context.registerReceiver(new BroadcastReceiver(){
									            @Override
									            public void onReceive(Context arg0, Intent arg1) {
									                switch (getResultCode())
									                {
									                case Activity.RESULT_OK:
								                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "2");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        query.close();
								                        
								                        break;
								                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                    	NotificationCompat.Builder mBuilder =
								    	                	new NotificationCompat.Builder(context)
								    	                .setSmallIcon(R.drawable.ic_alert)
								    	                .setContentTitle("Error")
								    	                .setContentText("Could not send message");
										    	        
										    	        Intent resultIntent = new Intent(context, MainActivity.class);
										    	
										    	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
										    	        stackBuilder.addParentStack(MainActivity.class);
										    	        stackBuilder.addNextIntent(resultIntent);
										    	        PendingIntent resultPendingIntent =
										    	                stackBuilder.getPendingIntent(
										    	                    0,
										    	                    PendingIntent.FLAG_UPDATE_CURRENT
										    	                );
										    	        
										    	        mBuilder.setContentIntent(resultPendingIntent);
										    	        mBuilder.setAutoCancel(true);
										    	        long[] pattern = {0L, 400L, 100L, 400L};
										    	        mBuilder.setVibrate(pattern);
										    	        mBuilder.setLights(0xFFffffff, 1000, 2000);
										    	        
										    	        try
										    	        {
										    	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
										    	        } catch(Exception e)
										    	        {
										    	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
										    	        }
										    	        
										    	        NotificationManager mNotificationManager =
										    	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
										    	        
										    	        Notification notification = mBuilder.build();
										    	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
										    	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
										    	        mNotificationManager.notify(1, notification);
								                        break;
								                    case SmsManager.RESULT_ERROR_NO_SERVICE:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "No service", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                    case SmsManager.RESULT_ERROR_NULL_PDU:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "Null PDU", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
								                    case SmsManager.RESULT_ERROR_RADIO_OFF:
								                    	try
								                    	{
								                    		wait(500);
								                    	} catch (Exception e)
								                    	{
								                    		
								                    	}
								                    	
								                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
								                        
								                        if (query.moveToFirst())
								                        {
								                        	String id = query.getString(query.getColumnIndex("_id"));
								                        	ContentValues values = new ContentValues();
								                        	values.put("type", "5");
								                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
								                        	((MainActivity) context).refreshViewPager3();
								                        }
								                        
								                        Toast.makeText(context, "Radio off", 
								                                Toast.LENGTH_SHORT).show();
								                        break;
									                }
									                
									                context.unregisterReceiver(this);
									            }
									        }, new IntentFilter(SENT));
									        
									        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
									        
									        String body2 = body;
									        
									        if (sharedPrefs.getBoolean("strip_unicode", false))
									        {
									        	body2 = StripAccents.stripAccents(body2);
									        }
									        
											SmsManager smsManager = SmsManager.getDefault();

											if (sharedPrefs.getBoolean("split_sms", false))
											{
												int length = 160;
												
												String patternStr = "[^\\x20-\\x7E]";
												Pattern pattern = Pattern.compile(patternStr);
												Matcher matcher = pattern.matcher(body2);
												  
												if (matcher.find())
												{
													length = 70;
												}
												
												String[] textToSend = splitByLength(body2, length);
												
												for (int i = 0; i < textToSend.length; i++)
												{
													ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
													
													for (int j = 0; j < parts.size(); j++)
													{
														sPI.add(sentPI);
													}
													
													smsManager.sendMultipartTextMessage(address, null, parts, sPI, null);
												}
											} else
											{
												ArrayList<String> parts = smsManager.divideMessage(body2); 
												
												for (int i = 0; i < parts.size(); i++)
												{
													sPI.add(sentPI);
												}
												
												smsManager.sendMultipartTextMessage(address, null, parts, sPI, null);
											}
										}
									} catch (NullPointerException e)
									{
										Toast.makeText(context, "Error sending message", Toast.LENGTH_SHORT).show();
									}
									
									String address2 = address;
									String body2 = body;
									
									if (sharedPrefs.getBoolean("strip_unicode", false))
									{
										body2 = StripAccents.stripAccents(body2);
									}
								    
								    Calendar cal = Calendar.getInstance();
								    ContentValues values = new ContentValues();
								    values.put("address", address2); 
								    values.put("body", body2); 
								    values.put("date", cal.getTimeInMillis() + "");
								    values.put("read", true);
								    getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
									
								    if (index == contactLength - 1)
								    {
									    getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
			
											@Override
											public void run() {
                                                MainActivity.threadedLoad = false;
												sentMessage = true;
												refreshViewPager(true);
												mTextView.setVisibility(View.GONE);
											}
									    	
									    });
								    }
								}
								
							}).start();
						}
					} else
					{
						Bitmap b;
						byte[] byteArray;
						
						try
						{
							if (!fromCamera)
							{
								b = decodeFile2(new File(getPath(attachedImage2)));
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								b.compress(Bitmap.CompressFormat.PNG, 100, stream);
								byteArray = stream.toByteArray();
							} else
							{
								b = decodeFile2(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								b.compress(Bitmap.CompressFormat.PNG, 100, stream);
								byteArray = stream.toByteArray();
							}
						} catch (Exception e)
						{
							byteArray = null;
						}
						
						String body = mEditText.getText().toString();
						
						String[] to = ("insert-address-token; " + contact.getText().toString()).split("; ");

                        if (!sharedPrefs.getBoolean("send_with_stock", false))
                        {
                            if (multipleAttachments == false)
                            {
                                insert(context, to, "", byteArray, body);

                                MMSPart[] parts = new MMSPart[2];

                                if (imageAttach2.getVisibility() == View.VISIBLE)
                                {
                                    parts[0] = new MMSPart();
                                    parts[0].Name = "Image";
                                    parts[0].MimeType = "image/png";
                                    parts[0].Data = byteArray;

                                    if (!body.equals(""))
                                    {
                                        parts[1] = new MMSPart();
                                        parts[1].Name = "Text";
                                        parts[1].MimeType = "text/plain";
                                        parts[1].Data = body.getBytes();
                                    }
                                } else
                                {
                                    parts[0] = new MMSPart();
                                    parts[0].Name = "Text";
                                    parts[0].MimeType = "text/plain";
                                    parts[0].Data = body.getBytes();
                                }

                                sendMMS(contact.getText().toString(), parts);
                            } else
                            {
                                ArrayList<byte[]> bytes = new ArrayList<byte[]>();
                                ArrayList<String> mimes = new ArrayList<String>();

                                for (int i = 0; i < AttachMore.data.size(); i++)
                                {
                                    bytes.add(AttachMore.data.get(i).Data);
                                    mimes.add(AttachMore.data.get(i).MimeType);
                                }

                                insert(context, to, "", bytes, mimes, body);

                                MMSPart part = new MMSPart();
                                part.Name = "Text";
                                part.MimeType = "text/plain";
                                part.Data = body.getBytes();
                                AttachMore.data.add(part);

                                sendMMS(contact.getText().toString(), AttachMore.data.toArray(new MMSPart[AttachMore.data.size()]));

                                AttachMore.data = new ArrayList<MMSPart>();
                            }
                        } else
                        {
                            if (multipleAttachments == false)
                            {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra("address", contact.getText().toString().replace(",", ""));
                                sendIntent.putExtra("sms_body", body);
                                sendIntent.putExtra(Intent.EXTRA_STREAM, attachedImage2);
                                sendIntent.setType("image/png");
                                startActivity(sendIntent);

                                com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
                            } else
                            {
                                Toast.makeText(context, "Cannot send multiple images through stock", Toast.LENGTH_SHORT).show();
                            }
                        }
						
						refreshViewPager(true);
					}
					
					contact.setText("");
					mEditText.setText("");
					imageAttach2.setVisibility(false);
					imageAttachBackground2.setVisibility(View.GONE);
					menu.showContent();
					mViewPager.setCurrentItem(0);
				}
			}
			
		});
		
		ImageButton emojiButton = (ImageButton) newMessageView.findViewById(R.id.display_emoji);
		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)mEditText.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mEditText.setLayoutParams(params);
		} else
		{
			emojiButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Insert Emojis");
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View frame = inflater.inflate(R.layout.emoji_frame, null);
					
					final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);
					
					final GridView emojiGrid = (GridView) frame.findViewById(R.id.emojiGrid);
					Button okButton = (Button) frame.findViewById(R.id.emoji_ok);
					
					if (sharedPrefs.getBoolean("emoji_type", true))
					{
						emojiGrid.setAdapter(new EmojiAdapter2(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
					} else
					{
						emojiGrid.setAdapter(new EmojiAdapter(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
					}
					
					builder.setView(frame);
					final AlertDialog dialog = builder.create();
					dialog.show();
					
					okButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (sharedPrefs.getBoolean("emoji_type", true))
							{
								mEditText.setText(EmojiConverter2.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							} else
							{
								mEditText.setText(EmojiConverter.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}
		
		ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);
		
		mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
		
		View v1 = newMessageView.findViewById(R.id.view1);
		View v2 = newMessageView.findViewById(R.id.sentBackground);
		
		mTextView.setTextColor(sharedPrefs.getInt("ct_sentButtonColor", getResources().getColor(R.color.black)));
		v1.setBackgroundColor(sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white)));
		v2.setBackgroundColor(sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white)));
		sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		sendButton.setImageResource(R.drawable.ic_action_send_white);
		sendButton.setColorFilter(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		searchView.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
		emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		emojiButton.setColorFilter(sharedPrefs.getInt("ct_emojiButtonColor", getResources().getColor(R.color.emoji_button)));
		mEditText.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black))));
		contact.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black))));
		
		imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", context.getResources().getColor(R.color.light_silver)));
		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)), Mode.MULTIPLY);
		imageAttach2.setBackgroundDrawable(attachBack);
		imageAttachBackground2.setVisibility(View.GONE);
		imageAttach2.setVisibility(false);
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			mTextView.setTypeface(font);
			mEditText.setTypeface(font);
			contact.setTypeface(font);
		}

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout"))
        {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }
		
		if (sharedPrefs.getBoolean("custom_background", false))
		{
			try
			{
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.inSampleSize = 1;
				Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background_location", "")).getPath(),options);
				this.getResources();
				Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
				searchView.setBackgroundDrawable(d);
			} catch (Exception e)
			{
				
			}
		}
		
		menu = new SlidingMenu(this);

        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            menu.setMode(SlidingMenu.LEFT_RIGHT);
            menu.setShadowDrawable(R.drawable.shadow);
            menu.setSecondaryShadowDrawable(R.drawable.shadowright);
        } else if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            menu.setMode(SlidingMenu.RIGHT);
            menu.setShadowDrawable(R.drawable.shadowright);
        }

        menu.setShadowWidthRes(R.dimen.shadow_width);
        
        if (!sharedPrefs.getBoolean("slide_messages", false))
        {
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
        	    menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            } else if (deviceType.equals("phablet") || deviceType.equals("tablet"))
            {
                menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            }
        } else
        {
        	menu.setBehindOffset(0);
        }
        
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            menu.setMenu(menuLayout);
            menu.setSecondaryMenu(newMessageView);
        } else if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            menu.setMenu(newMessageView);
        }
        
        menu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {

			@Override
			public void onOpened() {
				invalidateOptionsMenu();
				
				if (menu.isSecondaryMenuShowing())
				{
					contact.requestFocus();
				}
				
					ActionBar ab = getActionBar();
					ab.setTitle(R.string.app_name_in_app);
					ab.setSubtitle(null);
					ab.setIcon(R.drawable.ic_launcher);
					
					ab.setDisplayHomeAsUpEnabled(false);
					
					if (menu.isMenuShowing() && !menu.isSecondaryMenuShowing())
					{
						InputMethodManager imm = (InputMethodManager)getSystemService(
							      Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
					}
					
					if (menu.isMenuShowing() && menu.isSecondaryMenuShowing())
					{
						InputMethodManager imm = (InputMethodManager)getSystemService(
							      Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(contact, 0);
					}
			}
        	
        });
        
        menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {

			@Override
			public void onClosed() {
				
				invalidateOptionsMenu();

                if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                {
				    getActionBar().setDisplayHomeAsUpEnabled(true);
                }
				
				if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
				{
					final ActionBar ab = getActionBar();
					
					try
					{
						new Thread(new Runnable() {

							@Override
							public void run() {
								if (inboxNumber.size() != 0)
								{
									final String title = findContactName(inboxNumber.get(mViewPager.getCurrentItem()), context);
									
									((MainActivity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
	
					    				@Override
					    				public void run() {
					    					ab.setTitle(title);

                                            Locale sCachedLocale = Locale.getDefault();
                                            int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                            Editable editable = new SpannableStringBuilder(inboxNumber.get(mViewPager.getCurrentItem()));
                                            PhoneNumberUtils.formatNumber(editable, sFormatType);
                                            ab.setSubtitle(editable.toString());

                                            if (ab.getTitle().equals(ab.getSubtitle()))
                                            {
                                                ab.setSubtitle(null);
                                            }

                                            if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                                            {
                                                ab.setTitle("Group MMS");
                                                ab.setSubtitle(null);
                                            }
					    				}
					    		    	
					    		    });
								}
							}
							
						}).start();
					} catch (Exception e)
					{
						ab.setTitle(R.string.app_name_in_app);
						ab.setIcon(R.drawable.ic_launcher);
					}
				}
				
				if (sharedPrefs.getBoolean("title_contact_image", false))
		        {
		        	final ActionBar ab = getActionBar();
		        	
		        	new Thread(new Runnable() {

						@Override
						public void run() {
                            if (inboxNumber.size() != 0)
                            {
                                Bitmap image = getFacebookPhoto(inboxNumber.get(mViewPager.getCurrentItem()), context);
                                final BitmapDrawable image2 = new BitmapDrawable(image);

                                ((MainActivity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        ab.setIcon(image2);
                                    }

                                });
                            }
							
						}
		        		
		        	}).start();
			    	
			    	if (threadIds.size() == 0)
			    	{
			    		ab.setIcon(R.drawable.ic_launcher);
			    	}
		        }
				
				EditText textEntry = (EditText) findViewById(R.id.messageEntry);
				textEntry.requestFocus();
			}
        
        });
        
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() 
		{
		    public void onPageScrolled(int i, float f, int i2) {
		    	if (!menu.isMenuShowing())
		    	{
		    		menu.showContent();
		    	}
		    }

			@Override
			public void onPageScrollStateChanged(int arg0) {
				if (!menu.isMenuShowing())
		    	{
		    		menu.showContent();
		    	}
			}

			@Override
			public void onPageSelected(int arg0) {
				if (!menu.isMenuShowing())
		    	{
		    		menu.showContent();
		    	}
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						ArrayList<String> newMessages = readFromFile(context);
				        
				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	if (newMessages.get(j).replaceAll("-", "").endsWith(findContactName(inboxNumber.get(mViewPager.getCurrentItem()), context).replace("-", "")))
				        	{
				        		newMessages.remove(j);

				        		int wantedPosition = mViewPager.getCurrentItem();
				        		int firstPosition = menuLayout.getFirstVisiblePosition() - menuLayout.getHeaderViewsCount();
				        		int wantedChild = wantedPosition - firstPosition;

				        		if (wantedChild < 0 || wantedChild >= menuLayout.getChildCount()) {
				        		} else
				        		{
					        		final View item = menuLayout.getChildAt(wantedChild);

					        		((MainActivity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

					    				@Override
					    				public void run() {
					    					item.setBackgroundColor(menuLayout.getDrawingCacheBackgroundColor());
					    				}

					    		    });
				        		}
				        	}
				        }
				        
				        writeToFile(newMessages, context);
				        
				        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

							@Override
							public void run() {
								try
								{
									View row = menuLayout.getChildAt(mViewPager.getCurrentItem());
							        if (!sharedPrefs.getBoolean("custom_background", false))
							        {
							        	row.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
							        }
								} catch (Exception e)
								{
									
								}
							}
					    	
					    });
					}
					
				}).start();
				
				if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
				{
					final ActionBar ab = getActionBar();
					
					if (group.get(mViewPager.getCurrentItem()).equals("yes"))
					{
						ab.setTitle("Group MMS");
						ab.setSubtitle(null);
					} else
					{
						new Thread(new Runnable() {

							@Override
							public void run() {
								final String title = findContactName(inboxNumber.get(mViewPager.getCurrentItem()), context);
								
								((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
									
									@Override
									public void run() {
										ab.setTitle(title);

                                        Locale sCachedLocale = Locale.getDefault();
                                        int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                        Editable editable = new SpannableStringBuilder(inboxNumber.get(mViewPager.getCurrentItem()));
                                        PhoneNumberUtils.formatNumber(editable, sFormatType);
                                        ab.setSubtitle(editable.toString());

                                        if (ab.getTitle().equals(ab.getSubtitle()))
                                        {
                                            ab.setSubtitle(null);
                                        }
									}
							    	
							    });
								
							}
							
						}).start();
					}
				}
				
				if (sharedPrefs.getBoolean("title_contact_image", false))
		        {
		        	final ActionBar ab = getActionBar();
		        	
		        	new Thread(new Runnable() {

						@Override
						public void run() {
							final Bitmap image = getFacebookPhoto(inboxNumber.get(mViewPager.getCurrentItem()), context);
							final BitmapDrawable image2 = new BitmapDrawable(image);
							
							((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
								
								@Override
								public void run() {
									ab.setIcon(image2);
								}
						    	
						    });
							
						}
		        		
		        	}).start();
		        }
				
			}
		});
        
        mViewPager.setOffscreenPageLimit(1);
	}
	
	public Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    try
	    {
		    int width = drawable.getIntrinsicWidth();
		    width = width > 0 ? width : 1;
		    int height = drawable.getIntrinsicHeight();
		    height = height > 0 ? height : 1;
	
		    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		    Canvas canvas = new Canvas(bitmap); 
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);
		    return bitmap;
	    } catch (Exception e)
	    {
	    	if (sharedPrefs.getBoolean("ct_darkContactImage", false))
	    	{
	    		return BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_contact_dark);
	    	} else
	    	{
	    		return BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_contact_picture);
	    	}
	    }
	}
	 
	 public InputStream openDisplayPhoto(long contactId) {
		 Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
	     Cursor cursor = getContentResolver().query(photoUri,
	          new String[] {Contacts.Photo.PHOTO}, null, null, null);
	     if (cursor == null) {
	         return null;
	     }
	     try {
	         if (cursor.moveToFirst()) {
	             byte[] data = cursor.getBlob(0);
	             if (data != null) {
	                 return new ByteArrayInputStream(data);
	             }
	         }
	     } finally {
	         cursor.close();
	     }
	     return null;
	 }
	 
	 private String getMyPhoneNumber(){
		    TelephonyManager mTelephonyMgr;
		    mTelephonyMgr = (TelephonyManager)
		        getSystemService(Context.TELEPHONY_SERVICE); 
		    return mTelephonyMgr.getLine1Number();
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            if (inboxNumber.size() == 0 || MainActivity.menu.isMenuShowing())
            {
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
                menu.getItem(2).setVisible(true);
                menu.getItem(4).setVisible(true);
                menu.getItem(5).setVisible(false);
                menu.getItem(6).setVisible(false);
                menu.getItem(7).setVisible(false);

                if (MainActivity.menu.isSecondaryMenuShowing())
                {
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(true);
                    menu.getItem(2).setVisible(false);
                    menu.getItem(4).setVisible(false);
                    menu.getItem(5).setVisible(true);
                    menu.getItem(6).setVisible(false);
                    menu.getItem(7).setVisible(false);
                }
            } else
            {
                menu.getItem(0).setVisible(true);
                menu.getItem(1).setVisible(true);
                menu.getItem(2).setVisible(true);
                menu.getItem(4).setVisible(false);
                menu.getItem(5).setVisible(true);
                menu.getItem(6).setVisible(true);
                menu.getItem(7).setVisible(true);

                if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                {
                    menu.getItem(7).setVisible(false);
                }
            }
        } else
        {
            if (inboxNumber.size() == 0 || MainActivity.menu.isMenuShowing())
            {
                menu.getItem(0).setVisible(false);
                menu.getItem(2).setVisible(false);
                menu.getItem(4).setVisible(false);
                menu.getItem(6).setVisible(false);
                menu.getItem(7).setVisible(false);
            } else
            {
                menu.getItem(0).setVisible(true);
                menu.getItem(2).setVisible(true);
                menu.getItem(4).setVisible(true);
                menu.getItem(6).setVisible(true);
                menu.getItem(7).setVisible(true);

                if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                {
                    menu.getItem(7).setVisible(false);
                }
            }
        }
		
		if (!sharedPrefs.getBoolean("enable_mms", false))
		{
			menu.getItem(1).setVisible(false);
		}

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout") && sharedPrefs.getString("ct_theme_name", "Light Theme").equals("Hangouts Theme"))
        {
            Drawable callButton = getResources().getDrawable(R.drawable.ic_menu_call);
            callButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(0).setIcon(callButton);

            Drawable attachButton = getResources().getDrawable(R.drawable.ic_attach);
            attachButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(1).setIcon(attachButton);

            Drawable replyButton = getResources().getDrawable(R.drawable.ic_reply);
            replyButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(2).setIcon(replyButton);
        } else
        {
            Drawable callButton = getResources().getDrawable(R.drawable.ic_menu_call);
            callButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(0).setIcon(callButton);

            Drawable attachButton = getResources().getDrawable(R.drawable.ic_attach);
            attachButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(1).setIcon(attachButton);

            Drawable replyButton = getResources().getDrawable(R.drawable.ic_reply);
            replyButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(2).setIcon(replyButton);
        }
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_new_message:
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
	            menu.showSecondaryMenu();
            } else
            {
                menu.showMenu();
            }

	        return true;
	    case R.id.menu_settings:
	    	startActivity(new Intent(this, SettingsPagerActivity.class));
	    	return true;
	    case R.id.menu_about:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle(R.string.menu_about);
	    	String version = "";
	    	
	    	try {
				version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
	    	
			builder.setMessage(this.getResources().getString(R.string.version) + ": " + version +
                    "\n\n" + this.getResources().getString(R.string.about_expanded) + "\n\nŠ 2013 Jacob Klinker");
			
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
	    case android.R.id.home:
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
	    	    menu.showMenu();
            } else
            {

            }

	    	return true;
	    case R.id.menu_attach:
	    	multipleAttachments = false;
	    	AttachMore.data = new ArrayList<MMSPart>();

            boolean newMessage;

            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                newMessage = menu.isSecondaryMenuShowing();
            } else
            {
                newMessage = menu.isMenuShowing();
            }
	    	
	    	if (newMessage)
	    	{
	    		final Context context = this;
	    		
	    		AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
	    		attachBuilder.setItems(R.array.selectImage, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1)
						{
						case 0:
							Intent intent = new Intent();
			                intent.setType("image/*");
			                intent.setAction(Intent.ACTION_GET_CONTENT);
			                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 2);
			                
							break;
						case 1:
							Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							File f = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png");
							capturedPhotoUri = Uri.fromFile(f);
							captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
							startActivityForResult(captureIntent, 4);
							break;
						case 2:
							Intent attachMore = new Intent(context, AttachMore.class);
							startActivityForResult(attachMore, 6);
							break;
						}
						
					}
	    			
	    		});
	    		
	    		attachBuilder.create().show();
	    	} else
	    	{
	    		final Context context = this;
	    		
	    		attachedPosition = mViewPager.getCurrentItem();
	    		
	    		AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
	    		attachBuilder.setItems(R.array.selectImage, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1)
						{
						case 0:
							Intent intent = new Intent();
			                intent.setType("image/*");
			                intent.setAction(Intent.ACTION_GET_CONTENT);
			                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
			                
							break;
						case 1:
							Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							File f = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png");
							capturedPhotoUri = Uri.fromFile(f);
							captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
							startActivityForResult(captureIntent, 3);
							break;
						case 2:
							Intent attachMore = new Intent(context, AttachMore.class);
							startActivityForResult(attachMore, 5);
							break;
						}
						
					}
	    			
	    		});
	    		
	    		attachBuilder.create().show();
	    	}
	    	
	    	return true;
	    case R.id.menu_call:
	    	try
	    	{
		    	Intent callIntent = new Intent(Intent.ACTION_CALL);
		        callIntent.setData(Uri.parse("tel:"+inboxNumber.get(mViewPager.getCurrentItem())));
		        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(callIntent);
	    	} catch (Exception e)
	    	{
	    		Toast.makeText(this, "No contact to call", Toast.LENGTH_SHORT).show();
	    	}
	    	return true;
	    case R.id.menu_delete:
	    	Intent intent = new Intent(this, BatchDeleteActivity.class);
			intent.putExtra("threadIds", threadIds);
			intent.putExtra("inboxNumber", inboxNumber);
			startActivity(intent);
            
	    	return true;
	    case R.id.menu_template:
			AlertDialog.Builder template = new AlertDialog.Builder(this);
			template.setTitle(getResources().getString(R.string.insert_template));
			
			ListView templates = new ListView(this);
			
			TextView footer = new TextView(this);
			footer.setText(getResources().getString(R.string.add_templates));
			int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
			footer.setPadding(scale, scale, scale, scale);
			templates.addFooterView(footer);
			
			final ArrayList<String> text = readFromFile4(this);
			TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, text);
			templates.setAdapter(adapter);
			
			template.setView(templates);
			final AlertDialog templateDialog = template.create();
			templateDialog.show();
			
			templates.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try
					{
                        boolean newMessage;

                        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                        {
                            newMessage = menu.isSecondaryMenuShowing();
                        } else
                        {
                            newMessage = menu.isMenuShowing();
                        }

						if (newMessage)
						{
                            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                            {
							    ((TextView) menu.getSecondaryMenu().findViewById(R.id.messageEntry2)).setText(text.get(arg2));
                            } else
                            {
                                ((TextView) menu.getMenu().findViewById(R.id.messageEntry2)).setText(text.get(arg2));
                            }

							templateDialog.cancel();
						} else
						{
							messageEntry.setText(text.get(arg2));
							messageEntry.setSelection(text.get(arg2).length());
							templateDialog.cancel();
						}
					} catch (Exception e)
					{
						
					}
					
				}
				
			});		
			
			return true;
        case R.id.copy_sender:
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Address", inboxNumber.get(mViewPager.getCurrentItem()));
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, R.string.text_saved, Toast.LENGTH_SHORT).show();
            return true;
        case R.id.delete_conversation:
            final Context context = this;

            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
            deleteDialog.setTitle(getResources().getString(R.string.delete_conversation));
            deleteDialog.setMessage(getResources().getString(R.string.delete_conversation_message));
            deleteDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final ProgressDialog progDialog = new ProgressDialog(context);
                    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progDialog.setMessage(getResources().getString(R.string.deleting));
                    progDialog.show();

                    new Thread(new Runnable(){

                        @Override
                        public void run() {
                            deleteSMS(context, threadIds.get(mViewPager.getCurrentItem()));

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    ((MainActivity)context).refreshViewPager(true);
                                    progDialog.dismiss();

                                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                                    context.sendBroadcast(updateWidget);
                                }

                            });
                        }

                    }).start();
                }
            });
            deleteDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            deleteDialog.create().show();

            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void deleteSMS(Context context, String threadId) {
	    try {
	        	context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), null, null);
	    } catch (Exception e) {
	    	Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
	    }
	}
	
	@SuppressWarnings("deprecation")
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK){  
                final Uri selectedImage = imageReturnedIntent.getData();
                attachedImage = selectedImage;
                fromCamera = false;
                
                imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));
	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
	    		imageAttach.setBackgroundDrawable(attachBack);
	    		imageAttachBackground.setVisibility(View.VISIBLE);
	    		imageAttach.setVisibility(true);
	    		
	    		try
	    		{
	    			imageAttach.setImage("send_image", decodeFile(new File(getPath(selectedImage))));
	    		} catch (Exception e)
	    		{
	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
	    			imageAttach.setVisibility(false);
	    			imageAttachBackground.setVisibility(View.GONE);
	    		}
	    		
	    		final Context context = this;
	    		
	    		Button viewImage = (Button) findViewById(R.id.view_image_button);
	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button);
	    		Button removeImage = (Button) findViewById(R.id.remove_image_button);
	    		
	    		viewImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						context.startActivity(new Intent(Intent.ACTION_VIEW, selectedImage));
						
					}
	    			
	    		});
	    		
	    		replaceImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
		                intent.setType("image/*");
		                intent.setAction(Intent.ACTION_GET_CONTENT);
		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
						
					}
	    			
	    		});
	    		
	    		removeImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						imageAttach.setVisibility(false);
						imageAttachBackground.setVisibility(View.GONE);
						
					}
	    			
	    		});
	    		
	    		MainActivity.menu.showContent();
	    		mViewPager.setCurrentItem(attachedPosition);

            }
        } else if (requestCode == 2)
        {
        	if(resultCode == RESULT_OK){ 
        		final Uri selectedImage = imageReturnedIntent.getData();
        		attachedImage2 = selectedImage;
        		fromCamera = false;
                
                imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
	    		imageAttach2.setBackgroundDrawable(attachBack);
	    		imageAttachBackground2.setVisibility(View.VISIBLE);
	    		imageAttach2.setVisibility(true);
	    		
	    		try
	    		{
	    			imageAttach2.setImage("send_image", decodeFile(new File(getPath(selectedImage))));
	    		} catch (Exception e)
	    		{
	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
	    			imageAttach2.setVisibility(false);
	    			imageAttachBackground2.setVisibility(View.GONE);
	    		}
	    		
	    		final Context context = this;
	    		
	    		Button viewImage = (Button) findViewById(R.id.view_image_button2);
	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button2);
	    		Button removeImage = (Button) findViewById(R.id.remove_image_button2);
	    		
	    		viewImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						context.startActivity(new Intent(Intent.ACTION_VIEW, selectedImage));
						
					}
	    			
	    		});
	    		
	    		replaceImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
		                intent.setType("image/*");
		                intent.setAction(Intent.ACTION_GET_CONTENT);
		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 2);
						
					}
	    			
	    		});
	    		
	    		removeImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						imageAttach2.setVisibility(false);
						imageAttachBackground2.setVisibility(View.GONE);
						
					}
	    			
	    		});
	    		if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                {
	    		    MainActivity.menu.showSecondaryMenu();
                } else
                {
                    MainActivity.menu.showMenu();
                }

            }
        } else if (requestCode == 3)
        {
        	if (resultCode == Activity.RESULT_OK)
        	{
        		getContentResolver().notifyChange(capturedPhotoUri, null);
        		attachedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
        		fromCamera = true;
        		
        		imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground.setVisibility(View.VISIBLE);
 	    		imageAttach.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap image = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), capturedPhotoUri);
 	    			File f = new File(capturedPhotoUri.getPath());
 	    			image = decodeFile(f);
 	    			imageAttach.setImage("send_image", image);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
 	    			imageAttach.setVisibility(false);
 	    			imageAttachBackground.setVisibility(View.GONE);
 	    		}
 	    		
 	    		final Context context = this;
 	    		
 	    		Button viewImage = (Button) findViewById(R.id.view_image_button);
 	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button);
 	    		Button removeImage = (Button) findViewById(R.id.remove_image_button);
 	    		
 	    		viewImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View arg0) {
 						Intent intent = new Intent();
 			            intent.setAction(Intent.ACTION_VIEW);
 			            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")), "image/*");
 						context.startActivity(intent);
 						
 					}
 	    			
 	    		});
 	    		
 	    		replaceImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						Intent intent = new Intent();
 		                intent.setType("image/*");
 		                intent.setAction(Intent.ACTION_GET_CONTENT);
 		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
 						
 					}
 	    			
 	    		});
 	    		
 	    		removeImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						imageAttach.setVisibility(false);
 						imageAttachBackground.setVisibility(View.GONE);
 						
 					}
 	    			
 	    		});
        	}
        } else if (requestCode == 4)
        {
        	if (resultCode == Activity.RESULT_OK)
        	{
        		getContentResolver().notifyChange(capturedPhotoUri, null);
        		attachedImage2 = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
        		fromCamera = true;
        		
        		imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach2.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground2.setVisibility(View.VISIBLE);
 	    		imageAttach2.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap image = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), capturedPhotoUri);
 	    			File f = new File(capturedPhotoUri.getPath());
 	    			image = decodeFile(f);
 	    			imageAttach2.setImage("send_image", image);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
 	    			imageAttach2.setVisibility(false);
 	    			imageAttachBackground2.setVisibility(View.GONE);
 	    		}
 	    		
 	    		final Context context = this;
 	    		
 	    		Button viewImage = (Button) findViewById(R.id.view_image_button2);
 	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button2);
 	    		Button removeImage = (Button) findViewById(R.id.remove_image_button2);
 	    		
 	    		viewImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View arg0) {
 						Intent intent = new Intent();
 			            intent.setAction(Intent.ACTION_VIEW);
 			            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")), "image/*");
 						context.startActivity(intent);
 						
 					}
 	    			
 	    		});
 	    		
 	    		replaceImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						Intent intent = new Intent();
 		                intent.setType("image/*");
 		                intent.setAction(Intent.ACTION_GET_CONTENT);
 		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
 						
 					}
 	    			
 	    		});
 	    		
 	    		removeImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						imageAttach2.setVisibility(false);
 						imageAttachBackground2.setVisibility(View.GONE);
 						
 					}
 	    			
 	    		});
        	}
        } else if (requestCode == 5)
        {
        	if (resultCode == Activity.RESULT_OK)
            {
                multipleAttachments = true;
                
                imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground.setVisibility(View.VISIBLE);
 	    		imageAttach.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
 	    			Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
 	    			imageAttach.setImage("send_image", mutableBitmap);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
 	    			imageAttach.setVisibility(false);
 	    			imageAttachBackground.setVisibility(View.GONE);
 	    		}
                
                final Context context = this;
 	    		
 	    		Button viewImage = (Button) findViewById(R.id.view_image_button);
 	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button);
 	    		Button removeImage = (Button) findViewById(R.id.remove_image_button);
 	    		
 	    		viewImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View arg0) {
 						Intent attachMore = new Intent(context, AttachMore.class);
						startActivityForResult(attachMore, 5);
 						
 					}
 	    			
 	    		});
 	    		
 	    		replaceImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						Intent attachMore = new Intent(context, AttachMore.class);
						startActivityForResult(attachMore, 5);
 						
 					}
 	    			
 	    		});
 	    		
 	    		removeImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						imageAttach.setVisibility(false);
 						imageAttachBackground.setVisibility(View.GONE);
 						multipleAttachments = false;
 						AttachMore.data = new ArrayList<MMSPart>();
 						
 					}
 	    			
 	    		});
            }
        } else if (requestCode == 6)
        {
        	if (resultCode == Activity.RESULT_OK)
            {
                multipleAttachments = true;
                
                imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach2.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground2.setVisibility(View.VISIBLE);
 	    		imageAttach2.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
 	    			Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
 	    			imageAttach2.setImage("send_image", mutableBitmap);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
 	    			imageAttach2.setVisibility(false);
 	    			imageAttachBackground2.setVisibility(View.GONE);
 	    		}
                
                final Context context = this;
 	    		
 	    		Button viewImage = (Button) findViewById(R.id.view_image_button);
 	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button);
 	    		Button removeImage = (Button) findViewById(R.id.remove_image_button);
 	    		
 	    		viewImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View arg0) {
 						Intent attachMore = new Intent(context, AttachMore.class);
						startActivityForResult(attachMore, 6);
 						
 					}
 	    			
 	    		});
 	    		
 	    		replaceImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						Intent attachMore = new Intent(context, AttachMore.class);
						startActivityForResult(attachMore, 6);
 						
 					}
 	    			
 	    		});
 	    		
 	    		removeImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						imageAttach2.setVisibility(false);
 						imageAttachBackground2.setVisibility(View.GONE);
 						multipleAttachments = false;
 						AttachMore.data = new ArrayList<MMSPart>();
 						
 					}
 	    			
 	    		});
            }
        } else
        {
        	
        }
        
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
	}
	
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
		}
	
	private Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=150;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        Bitmap image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	        
	        try
	        {
	        	ExifInterface exif = new ExifInterface(f.getAbsolutePath());
	        	int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
	        	
	        	if (orientation == 6)
	        	{
		        	Matrix matrix = new Matrix();
		        	matrix.postRotate(90);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 3)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(180);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 8)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(2700);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	}
	        } catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        return image;
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	private Bitmap decodeFile2(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
            int REQUIRED_SIZE=300;

            if (!sharedPrefs.getBoolean("limit_attachment_size", true))
            {
                REQUIRED_SIZE = 500;
            }

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        Bitmap image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	        
	        try
	        {
	        	ExifInterface exif = new ExifInterface(f.getPath());
	        	int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
	        	
	        	if (orientation == 6)
	        	{
		        	Matrix matrix = new Matrix();
		        	matrix.postRotate(90);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 3)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(180);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 8)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(2700);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	}
	        } catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        return image;
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	@Override
	public void onBackPressed() {
        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            if (menu.isSecondaryMenuShowing())
            {
                if (!sharedPrefs.getBoolean("open_contact_menu", false))
                {
                    menu.showContent();
                } else
                {
                    menu.showMenu();
                }
            } else
            {
                if (menu.isMenuShowing() && !sharedPrefs.getBoolean("open_contact_menu", false))
                {
                    menu.showContent();
                } else if (!menu.isMenuShowing() && sharedPrefs.getBoolean("open_contact_menu", false))
                {
                    menu.showMenu();
                } else
                {
                    super.onBackPressed();
                }
            }
        } else
        {
            if (menu.isMenuShowing())
            {
                menu.showContent();
            } else
            {
                super.onBackPressed();
            }
        }

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    
	}
	
	@Override
	public void onResumeFragments()
	{
		super.onResumeFragments();
		
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(receiver, filter);
        
        filter = new IntentFilter("com.klinker.android.messaging.NEW_MMS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(mmsReceiver, filter);
        
        String menuOption = sharedPrefs.getString("page_or_menu2", "2");
        
        if (menuOption.equals("2"))
        {
        	menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        } else if (menuOption.equals("1"))
        {
        	menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        } else
        {
        	menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        }
        
        if (imageAttach.getVisibility() == View.VISIBLE)
        {
        	menu.showContent();
        } else if (imageAttach2.getVisibility() == View.VISIBLE)
        {
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
        	    menu.showSecondaryMenu();
            } else
            {
                menu.showMenu();
            }
        }
        
        if (whatToSend != null)
        {
        	messageEntry.setText(whatToSend);
        	whatToSend = null;
        }

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);
	}
	
	@Override
	public void onPause()
	{
		super.onStop();
		
		try
		{
			unregisterReceiver(receiver);
			unregisterReceiver(mmsReceiver);
		} catch (Exception e)
		{
			
		}
		
		ComponentName receiver = new ComponentName(this, SentReceiver.class);
		ComponentName receiver2 = new ComponentName(this, DeliveredReceiver.class);
	    PackageManager pm = this.getPackageManager();

	    pm.setComponentEnabledSetting(receiver,
	            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);
	    
	    pm.setComponentEnabledSetting(receiver2,
	    		PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);
	}
	
	@Override
	public void onStop()
	{
		super.onStop();

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (deviceType.startsWith("phablet"))
        {
            recreate();
        }
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart()
	{
		super.onStart();
		
		ComponentName receiver = new ComponentName(this, SentReceiver.class);
		ComponentName receiver2 = new ComponentName(this, DeliveredReceiver.class);
	    PackageManager pm = this.getPackageManager();

	    pm.setComponentEnabledSetting(receiver,
	            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
	            PackageManager.DONT_KILL_APP);
	    
	    pm.setComponentEnabledSetting(receiver2,
	    		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
	            PackageManager.DONT_KILL_APP);
		
		if (firstRun)
		{
			refreshViewPager(false);
			createMenu();
			firstRun = false;
			
			if (sharedPrefs.getBoolean("open_contact_menu", false) && (deviceType.equals("phone") || deviceType.equals("phablet2")))
			{
				menu.showMenu();
			}
			
			if (sendTo && !fromNotification)
			{
				boolean flag = false;
				
				for (int i = 0; i < inboxNumber.size(); i++)
				{
					if (inboxNumber.get(i).replace("-","").replace("+", "").equals(sendMessageTo.replace("-", "").replace("+1", "")))
					{
						mViewPager.setCurrentItem(i);
						menu.showContent();
						flag = true;
						break;
					}
				}
				
				if (flag == false)
				{
					String name = findContactName(sendMessageTo, this);
					
					for (int i = 0; i < inboxNumber.size(); i++)
					{
						if (findContactName(inboxNumber.get(i), this).equals(name))
						{
							mViewPager.setCurrentItem(i);
							menu.showContent();
							flag = true;
							break;
						}
					}
				}
				
				if (flag == false)
				{
                    View newMessage;

                    if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                    {
                        menu.showSecondaryMenu();
                        newMessage = menu.getSecondaryMenu();
                    } else
                    {
                        menu.showMenu();
                        newMessage = menu.getMenu();
                    }

					EditText contact = (EditText) newMessage.findViewById(R.id.contactEntry);
					contact.setText(sendMessageTo);
					
					if (attachedImage2 != null)
					{
						imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
			    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
			    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
			    		imageAttach2.setBackgroundDrawable(attachBack);
			    		imageAttachBackground2.setVisibility(View.VISIBLE);
			    		imageAttach2.setVisibility(true);
			    		
			    		try
			    		{
			    			imageAttach2.setImage("send_image", decodeFile(new File(getPath(attachedImage2))));
			    		} catch (Exception e)
			    		{
			    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
			    			imageAttach2.setVisibility(false);
			    			imageAttachBackground2.setVisibility(View.GONE);
			    		}
			    		
			    		final Context context = this;
			    		
			    		Button viewImage = (Button) findViewById(R.id.view_image_button2);
			    		Button replaceImage = (Button) findViewById(R.id.replace_image_button2);
			    		Button removeImage = (Button) findViewById(R.id.remove_image_button2);
			    		
			    		viewImage.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								context.startActivity(new Intent(Intent.ACTION_VIEW, attachedImage2));
								
							}
			    			
			    		});
			    		
			    		replaceImage.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent();
				                intent.setType("image/*");
				                intent.setAction(Intent.ACTION_GET_CONTENT);
				                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 2);
								
							}
			    			
			    		});
			    		
			    		removeImage.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								imageAttach2.setVisibility(false);
								imageAttachBackground2.setVisibility(View.GONE);
								
							}
			    			
			    		});
					}
				}
				
				sendTo = false;
			}
			
			if (sendToThread != null)
			{
				for (int i = 0; i < threadIds.size(); i++)
				{
					if (threadIds.get(i).equals(sendToThread))
					{
						mViewPager.setCurrentItem(i);
						sendToThread = null;
						break;
					}
				}
				
				messageEntry.setText(sendToMessage);
				
				try
				{
					messageEntry.setSelection(sendToMessage.length());
				} catch (Exception e)
				{
					
				}
			}
		} else
		{
			if (messageRecieved == true)
			{
				refreshViewPager(false);
				messageRecieved = false;
			}
			
			if (sendTo == true)
			{
				menu.showContent();
			} else
			{
				if (sharedPrefs.getBoolean("open_contact_menu", false) && (imageAttach.getVisibility() != View.VISIBLE && imageAttach2.getVisibility() != View.VISIBLE) && (deviceType.equals("phone") || deviceType.equals("phablet2")))
				{
					menu.showMenu();
				} else if (imageAttach.getVisibility() == View.VISIBLE)
				{
					menu.showContent();
				} else if (imageAttach2.getVisibility() == View.VISIBLE)
				{
                    if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                    {
					    menu.showSecondaryMenu();
                    } else
                    {
                        menu.showMenu();
                    }
				}
			}
		}

		if (fromNotification)
		{
			menu.showContent();
			fromNotification = false;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void refreshViewPager(boolean totalRefresh)
	{
		String threadTitle = "0";
		
		if (!firstRun && inboxNumber.size() != 0)
		{
			threadTitle = findContactName(inboxNumber.get(mViewPager.getCurrentItem()), this);
		}
		
		refreshMessages(totalRefresh);
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getFragmentManager());
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));

        if (sharedPrefs.getBoolean("custom_background2", false))
        {
            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 1;
                Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background2_location", "")).getPath(),options);
                Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
                mViewPager.setBackgroundDrawable(d);
            } catch (Exception e)
            {

            }
        }
		
		if ((messageRecieved && jump) || sentMessage)
		{
			threadTitle = "0";
			sentMessage = false;
		}
		
		NotificationManager mNotificationManager =
	            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);
		mNotificationManager.cancel(2);
		writeToFile2(new ArrayList<String>(), this);
		
		Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
	    this.sendBroadcast(intent);
	    
	    Intent stopRepeating = new Intent(this, NotificationRepeaterService.class);
		PendingIntent pStopRepeating = PendingIntent.getService(this, 0, stopRepeating, 0);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pStopRepeating);
		
		if (!draft.equals(""))
		{
			ClipboardManager clipboard = (ClipboardManager)
			        getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Message Draft", draft);
			clipboard.setPrimaryClip(clip);
		}
		
		if (!firstRun)
		{
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                menuAdapter = new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
                menuLayout.setAdapter(menuAdapter);
            } else
            {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));
            }
		} else
		{
			final Context context = this;
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					ArrayList<String> newMessages = readFromFile(context);
			        
					if (inboxNumber.size() > 0)
					{
                        if (newMessages.size() != 0)
                            newMessages.remove(newMessages.size() - 1);
					} else
					{
						newMessages = new ArrayList<String>();
					}
			        
			        writeToFile(newMessages, context);
					
				}
				
			}).start();
		}
		
		if (threadTitle.equals("0"))
		{
			mViewPager.setCurrentItem(0);
		} else
		{
			final String threadT = threadTitle;
			final Context context = this;
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					boolean flag = false;
					
					for (int i = 0; i < inboxNumber.size(); i++)
					{
						if (threadT.equals(findContactName(inboxNumber.get(i), context)))
						{
							final int index = i;
							
							((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
								
								@Override
								public void run() {
									mViewPager.setCurrentItem(index);
								}
							});
							
							flag = true;
							break;
						}
					}
					
					if (!flag)
					{
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								mViewPager.setCurrentItem(0);
							}
						});
					}
					
				}
				
			}).start();
		}
		
		if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
		{
			final ActionBar ab = getActionBar();
			final Context context = this;
			
			try
			{
				ab.setTitle(findContactName(inboxNumber.get(mViewPager.getCurrentItem()), context));
				
				Locale sCachedLocale = Locale.getDefault();
				int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
				Editable editable = new SpannableStringBuilder(inboxNumber.get(mViewPager.getCurrentItem()));
				PhoneNumberUtils.formatNumber(editable, sFormatType);
				ab.setSubtitle(editable.toString());
				
				if (ab.getTitle().equals(ab.getSubtitle()))
				{
					ab.setSubtitle(null);
				}
				
				if (group.get(mViewPager.getCurrentItem()).equals("yes"))
				{
					ab.setTitle("Group MMS");
					ab.setSubtitle(null);
				}
			} catch (Exception e)
			{
				ab.setTitle(R.string.app_name_in_app);
				ab.setSubtitle(null);
				ab.setIcon(R.drawable.ic_launcher);
			}
		}
        
        if (sharedPrefs.getBoolean("title_contact_image", false))
        {
        	final ActionBar ab = getActionBar();
        	final Context context = this;
        	
        	try
        	{
        		ab.setIcon(new BitmapDrawable(getFacebookPhoto(inboxNumber.get(mViewPager.getCurrentItem()), context)));
        	} catch (Exception e)
        	{
        		
        	}
        }
		
		MainActivity.loadAll = false;
		
		if (MainActivity.animationReceived == 2)
		{
			final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
			
			glow.setVisibility(View.VISIBLE);
			glow.setAlpha((float)1);
			
			Animation fadeIn = new AlphaAnimation(0, (float).9);
			fadeIn.setInterpolator(new DecelerateInterpolator());
			fadeIn.setDuration(1000);
	
			Animation fadeOut = new AlphaAnimation((float).9, 0);
			fadeOut.setInterpolator(new AccelerateInterpolator());
			fadeOut.setStartOffset(1000);
			fadeOut.setDuration(1000);
	
			AnimationSet animation = new AnimationSet(false);
			animation.addAnimation(fadeIn);
			animation.addAnimation(fadeOut);
			
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					glow.setAlpha((float)0);
					
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					
				}
				
			});
			
			glow.startAnimation(animation);
			
			MainActivity.animationReceived = 0;
		} else
		{
			final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
			glow.setAlpha((float)0);
			glow.setVisibility(View.GONE);
		}
		
		if (sharedPrefs.getBoolean("background_service", false))
		{
			new Thread (new Runnable() {

				@Override
				public void run() {
					ArrayList<String> data = new ArrayList<String>();
					
					String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
					Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
					Cursor query = getContentResolver().query(uri, projection, null, null, "date desc");
					
					if (query.moveToFirst())
					{
						do
						{
							data.add(query.getString(query.getColumnIndex("_id")));
							data.add(query.getString(query.getColumnIndex("message_count")));
							data.add(query.getString(query.getColumnIndex("read")));
							
							data.add(" ");
							
							try
							{
								data.set(data.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
							} catch (Exception e)
							{
							}
							
							data.add(query.getString(query.getColumnIndex("date")));
							
							String[] ids = query.getString(query.getColumnIndex("recipient_ids")).split(" ");
							String numbers = "";
							
							for (int i = 0; i < ids.length; i++)
							{
								try
								{
									if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
									{
										Cursor number = getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);
										
										if (number.moveToFirst())
										{
											numbers += number.getString(number.getColumnIndex("address")).replaceAll("-", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "") + " ";
										} else
										{
											numbers += "0 ";
										}
										
										number.close();
									} else
									{
										
									}
								} catch (Exception e)
								{
									numbers += "0 ";
								}
							}
							
							data.add(numbers.trim());
							
							if (ids.length > 1)
							{
								data.add("yes");
							} else
							{
								data.add("no");
							}
						} while (query.moveToNext());
					}
					
					query.close();
					
					writeToFile3(data, getBaseContext());
					
				}
				
			}).start();
		}
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public void refreshViewPager2()
	{
		String threadTitle = "0";
		
		if (!firstRun)
		{
			threadTitle = findContactName(inboxNumber.get(mViewPager.getCurrentItem()), this);
		}
		
		if ((messageRecieved && jump) || sentMessage)
		{
			threadTitle = "0";
			sentMessage = false;
		}
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getFragmentManager());
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		NotificationManager mNotificationManager =
	            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);
		
		Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
	    this.sendBroadcast(intent);
		
		if (!draft.equals(""))
		{
			ClipboardManager clipboard = (ClipboardManager)
			        getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Message Draft", draft);
			clipboard.setPrimaryClip(clip);
		}
		
		if (!firstRun)
		{
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                menuAdapter = new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
                menuLayout.setAdapter(menuAdapter);
            } else
            {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));
            }
		}
		
		if (threadTitle.equals("0"))
		{
			mViewPager.setCurrentItem(0);
		} else
		{
			final String threadT = threadTitle;
			final Context context = this;
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					boolean flag = false;
					
					for (int i = 0; i < inboxNumber.size(); i++)
					{
						if (threadT.equals(findContactName(inboxNumber.get(i), context)))
						{
							final int index = i;
							
							((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
								
								@Override
								public void run() {
									mViewPager.setCurrentItem(index);
								}
							});
							
							flag = true;
							break;
						}
					}
					
					if (!flag)
					{
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								mViewPager.setCurrentItem(0);
							}
						});
					}
					
				}
				
			}).start();
		}
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public void refreshViewPager3()
	{
        MainActivity.threadedLoad = false;
		int position = mViewPager.getCurrentItem();
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getFragmentManager());
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mViewPager.setCurrentItem(position);
		
		if (sharedPrefs.getBoolean("background_service", false))
		{
			new Thread (new Runnable() {

				@Override
				public void run() {
					ArrayList<String> data = new ArrayList<String>();
					
					String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
					Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
					Cursor query = getContentResolver().query(uri, projection, null, null, "date desc");
					
					if (query.moveToFirst())
					{
						do
						{
							data.add(query.getString(query.getColumnIndex("_id")));
							data.add(query.getString(query.getColumnIndex("message_count")));
							data.add(query.getString(query.getColumnIndex("read")));
							
							data.add(" ");
							
							try
							{
								data.set(data.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
							} catch (Exception e)
							{
							}
							
							data.add(query.getString(query.getColumnIndex("date")));
							
							String[] ids = query.getString(query.getColumnIndex("recipient_ids")).split(" ");
							String numbers = "";
							
							for (int i = 0; i < ids.length; i++)
							{
								try
								{
									if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
									{
										Cursor number = getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);
										
										if (number.moveToFirst())
										{
											numbers += number.getString(number.getColumnIndex("address")).replaceAll("-", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "") + " ";
										} else
										{
											numbers += "0 ";
										}
										
										number.close();
									} else
									{
										
									}
								} catch (Exception e)
								{
									numbers += "0 ";
								}
							}
							
							data.add(numbers.trim());
							
							if (ids.length > 1)
							{
								data.add("yes");
							} else
							{
								data.add("no");
							}
						} while (query.moveToNext());
					}
					
					query.close();
					
					writeToFile3(data, getBaseContext());
					
				}
				
			}).start();
		}
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public void refreshViewPager4(String number, String body, String date)
	{
        MainActivity.threadedLoad = false;
		int position = mViewPager.getCurrentItem();
		String currentNumber = inboxNumber.get(position);
		
		boolean flag = false;
		
		for (int i = 0; i < inboxNumber.size(); i++)
		{
			if (number.endsWith(inboxNumber.get(i)))
			{
				inboxBody.add(0, body);
				inboxDate.add(0, date);
				inboxNumber.add(0, inboxNumber.get(i));
				threadIds.add(0, threadIds.get(i));
				group.add(0, group.get(i));
				msgCount.add(0, Integer.parseInt(msgCount.get(i)) + 1 + "");
				msgRead.add(0, "0");
				
				inboxBody.remove(i+1);
				inboxDate.remove(i+1);
				inboxNumber.remove(i+1);
				threadIds.remove(i+1);
				group.remove(i+1);
				msgCount.remove(i+1);
				msgRead.remove(i+1);
				
				flag = true;
				break;
			}
		}
		
		if (flag == true)
		{
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                menuAdapter = new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
                menuLayout.setAdapter(menuAdapter);
            } else
            {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));
            }
			
			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getFragmentManager());
			
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
			
			for (int i = 0; i < inboxNumber.size(); i++)
			{
				if (currentNumber.equals(inboxNumber.get(i)))
				{
					position = i;
					break;
				}
			}
			
			mViewPager.setCurrentItem(position);
			
			final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
			glow.setVisibility(View.VISIBLE);
			
			if (MainActivity.animationReceived == 2)
			{
				glow.setAlpha((float)1);
				glow.setVisibility(View.VISIBLE);
				
				Animation fadeIn = new AlphaAnimation(0, (float).9);
				fadeIn.setInterpolator(new DecelerateInterpolator());
				fadeIn.setDuration(1000);
		
				Animation fadeOut = new AlphaAnimation((float).9, 0);
				fadeOut.setInterpolator(new AccelerateInterpolator());
				fadeOut.setStartOffset(1000);
				fadeOut.setDuration(1000);
		
				AnimationSet animation = new AnimationSet(false);
				animation.addAnimation(fadeIn);
				animation.addAnimation(fadeOut);
				
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation arg0) {
						glow.setAlpha((float)0);
						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						
					}

					@Override
					public void onAnimationStart(Animation animation) {
						
					}
					
				});
				
				glow.startAnimation(animation);
				
				MainActivity.animationReceived = 0;
			} else
			{
				glow.setAlpha((float)0);
				glow.setVisibility(View.GONE);
			}
			
			if (sharedPrefs.getBoolean("background_service", false))
			{
				new Thread (new Runnable() {

					@Override
					public void run() {
						ArrayList<String> data = new ArrayList<String>();
						
						String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
						Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
						Cursor query = getContentResolver().query(uri, projection, null, null, "date desc");
						
						if (query.moveToFirst())
						{
							do
							{
								data.add(query.getString(query.getColumnIndex("_id")));
								data.add(query.getString(query.getColumnIndex("message_count")));
								data.add(query.getString(query.getColumnIndex("read")));
								
								data.add(" ");
								
								try
								{
									data.set(data.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
								} catch (Exception e)
								{
								}
								
								data.add(query.getString(query.getColumnIndex("date")));
								
								String[] ids = query.getString(query.getColumnIndex("recipient_ids")).split(" ");
								String numbers = "";
								
								for (int i = 0; i < ids.length; i++)
								{
									try
									{
										if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
										{
											Cursor number = getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);
											
											if (number.moveToFirst())
											{
												numbers += number.getString(number.getColumnIndex("address")).replaceAll("-", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "") + " ";
											} else
											{
												numbers += "0 ";
											}
											
											number.close();
										} else
										{
											
										}
									} catch (Exception e)
									{
										numbers += "0 ";
									}
								}
								
								data.add(numbers.trim());
								
								if (ids.length > 1)
								{
									data.add("yes");
								} else
								{
									data.add("no");
								}
							} while (query.moveToNext());
						}
						
						query.close();
						
						writeToFile3(data, getBaseContext());
						
					}
					
				}).start();
			}
		} else
		{
			refreshViewPager(true);
		}
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public static Uri insert(Context context, String[] to, String subject, byte[] imageBytes, String text)
	{
	    try
	    {           
	        Uri destUri = Uri.parse("content://mms");

	        // Get thread id
	        Set<String> recipients = new HashSet<String>();
	        recipients.addAll(Arrays.asList(to));
	        long thread_id = Telephony.Threads.getOrCreateThreadId(context, recipients);

	        // Create a dummy sms
	        ContentValues dummyValues = new ContentValues();
	        dummyValues.put("thread_id", thread_id);
	        dummyValues.put("body", "Dummy SMS body.");
	        Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);

	        // Create a new message entry
	        long now = System.currentTimeMillis();
	        ContentValues mmsValues = new ContentValues();
	        mmsValues.put("thread_id", thread_id);
	        mmsValues.put("date", now/1000L);
	        mmsValues.put("msg_box", 4);
	        //mmsValues.put("m_id", System.currentTimeMillis());
	        mmsValues.put("read", true);
	        mmsValues.put("sub", subject);
	        mmsValues.put("sub_cs", 106);
	        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
	        
	        if (imageBytes != null)
	        {
	        	mmsValues.put("exp", imageBytes.length);
	        } else
	        {
	        	mmsValues.put("exp", 0);
	        }
	        
	        mmsValues.put("m_cls", "personal");
	        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
	        mmsValues.put("v", 19);
	        mmsValues.put("pri", 129);
	        mmsValues.put("tr_id", "T"+ Long.toHexString(now));
	        mmsValues.put("resp_st", 128);

	        // Insert message
	        Uri res = context.getContentResolver().insert(destUri, mmsValues);
	        String messageId = res.getLastPathSegment().trim();

	        // Create part
	        if (imageBytes != null)
	        {
	        	createPartImage(context, messageId, imageBytes, "image/png");
	        }
	        
	        createPartText(context, messageId, text);

	        // Create addresses
	        for (String addr : to)
	        {
	            createAddr(context, messageId, addr);
	        }

	        //res = Uri.parse(destUri + "/" + messageId);

	        // Delete dummy sms
	        context.getContentResolver().delete(dummySms, null, null);

	        return res;
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }

	    return null;
	}

	public static Uri insert(Context context, String[] to, String subject, ArrayList<byte[]> imageBytes, ArrayList<String> mimeTypes, String text)
	{
	    try
	    {           
	        Uri destUri = Uri.parse("content://mms");

	        // Get thread id
	        Set<String> recipients = new HashSet<String>();
	        recipients.addAll(Arrays.asList(to));
	        long thread_id = Telephony.Threads.getOrCreateThreadId(context, recipients);

	        // Create a dummy sms
	        ContentValues dummyValues = new ContentValues();
	        dummyValues.put("thread_id", thread_id);
	        dummyValues.put("body", "Dummy SMS body.");
	        Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);

	        // Create a new message entry
	        long now = System.currentTimeMillis();
	        ContentValues mmsValues = new ContentValues();
	        mmsValues.put("thread_id", thread_id);
	        mmsValues.put("date", now/1000L);
	        mmsValues.put("msg_box", 4);
	        //mmsValues.put("m_id", System.currentTimeMillis());
	        mmsValues.put("read", true);
	        mmsValues.put("sub", subject);
	        mmsValues.put("sub_cs", 106);
	        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
	        
	        if (imageBytes != null)
	        {
	        	mmsValues.put("exp", imageBytes.get(0).length);
	        } else
	        {
	        	mmsValues.put("exp", 0);
	        }
	        
	        mmsValues.put("m_cls", "personal");
	        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
	        mmsValues.put("v", 19);
	        mmsValues.put("pri", 129);
	        mmsValues.put("tr_id", "T"+ Long.toHexString(now));
	        mmsValues.put("resp_st", 128);

	        // Insert message
	        Uri res = context.getContentResolver().insert(destUri, mmsValues);
	        String messageId = res.getLastPathSegment().trim();

	        // Create part
	        for (int i = 0; i < imageBytes.size(); i++)
	        {
	        	createPartImage(context, messageId, imageBytes.get(i), mimeTypes.get(i));
	        }
	        
	        createPartText(context, messageId, text);

	        // Create addresses
	        for (String addr : to)
	        {
	            createAddr(context, messageId, addr);
	        }

	        //res = Uri.parse(destUri + "/" + messageId);

	        // Delete dummy sms
	        context.getContentResolver().delete(dummySms, null, null);

	        return res;
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }

	    return null;
	}

	private static Uri createPartImage(Context context, String id, byte[] imageBytes, String mimeType) throws Exception
	{
	    ContentValues mmsPartValue = new ContentValues();
	    mmsPartValue.put("mid", id);
	    mmsPartValue.put("ct", mimeType);
	    mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
	    Uri partUri = Uri.parse("content://mms/" + id + "/part");
	    Uri res = context.getContentResolver().insert(partUri, mmsPartValue);

	    // Add data to part
	    OutputStream os = context.getContentResolver().openOutputStream(res);
	    ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
	    byte[] buffer = new byte[256];
	    for (int len=0; (len=is.read(buffer)) != -1;)
	    {
	        os.write(buffer, 0, len);
	    }
	    os.close();
	    is.close();

	    return res;
	}
	
	private static Uri createPartText(Context context, String id, String text) throws Exception
	{
	    ContentValues mmsPartValue = new ContentValues();
	    mmsPartValue.put("mid", id);
	    mmsPartValue.put("ct", "text/plain");
	    mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
	    mmsPartValue.put("text", text);
	    Uri partUri = Uri.parse("content://mms/" + id + "/part");
	    Uri res = context.getContentResolver().insert(partUri, mmsPartValue);

	    return res;
	}

	private static Uri createAddr(Context context, String id, String addr) throws Exception
	{
	    ContentValues addrValues = new ContentValues();
	    addrValues.put("address", addr);
	    addrValues.put("charset", "106");
	    addrValues.put("type", 151); // TO
	    Uri addrUri = Uri.parse("content://mms/"+ id +"/addr");
	    Uri res = context.getContentResolver().insert(addrUri, addrValues);

	    return res;
	}
	
	public void sendMMS(final String recipient, final MMSPart[] parts)
	{
		if (sharedPrefs.getBoolean("wifi_mms_fix", false))
		{
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			currentWifi = wifi.getConnectionInfo();
			currentWifiState = wifi.isWifiEnabled();
			wifi.disconnect();
			discon = new DisconnectWifi();
			registerReceiver(discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
		}
		
		ConnectivityManager mConnMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		final int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
		
		if (result != 0)
		{
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			BroadcastReceiver receiver = new BroadcastReceiver() {
	
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					
					if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
					{
						return;
					}
					
					@SuppressWarnings("deprecation")
					NetworkInfo mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					
					if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE))
					{
						return;
					}
					
					if (!mNetworkInfo.isConnected())
					{
						return;
					} else
					{
						sendData(recipient, parts);
						
						unregisterReceiver(this);
					}
					
				}
				
			};
			
			registerReceiver(receiver, filter);
		} else
		{
			sendData(recipient, parts);
		}
	}
	
	public void sendData(final String recipient, final MMSPart[] parts)
	{
		final Context context = this;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				final SendReq sendRequest = new SendReq();
				
				String[] recipients = recipient.replace(";", "").split(" ");
				
				for (int i = 0; i < recipients.length; i++)
				{
					final EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(recipients[i]);
					
					if (phoneNumbers != null && phoneNumbers.length > 0)
					{
						sendRequest.addTo(phoneNumbers[0]);
					}
				}
				
				final PduBody pduBody = new PduBody();
				
				if (parts != null)
				{
					for (MMSPart part : parts)
					{
						if (part != null)
						{
							try
							{
								final PduPart partPdu = new PduPart();
								partPdu.setName(part.Name.getBytes());
								partPdu.setContentType(part.MimeType.getBytes());
								partPdu.setData(part.Data);
								pduBody.addPart(partPdu);
							} catch (Exception e)
							{
								
							}
						}
					}
				}
				
				sendRequest.setBody(pduBody);
				
				final PduComposer composer = new PduComposer(context, sendRequest);
				final byte[] bytesToSend = composer.make();
				
				List<APN> apns = new ArrayList<APN>();
				
				try
				{
					APNHelper helper = new APNHelper(context);
					apns = helper.getMMSApns();
					
					final APN apn = apns.get(0);
					
					((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
						
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle("System APNs");
							builder.setMessage("MMSC Url: " + apn.MMSCenterUrl + "\n" +
							                   "MMS Proxy: " + apn.MMSProxy + "\n" +
									           "MMS Port: " + apn.MMSPort + "\n");
							builder.create().show();
						}
					});
					
				} catch (Exception e)
				{
					APN apn = new APN(sharedPrefs.getString("mmsc_url", ""), sharedPrefs.getString("mms_port", ""), sharedPrefs.getString("mms_proxy", ""));
					apns.add(apn);
					
					String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
					apns.get(0).MMSCenterUrl = mmscUrl;
					
					try
					{
						if (sharedPrefs.getBoolean("apn_username_password", false))
						{
							if (!sharedPrefs.getString("apn_username", "").equals("") && !sharedPrefs.getString("apn_username", "").equals(""))
							{
								String mmsc = apns.get(0).MMSCenterUrl;
								String[] parts = mmsc.split("://");
								String newMmsc = parts[0] + "://";
								
								newMmsc += sharedPrefs.getString("apn_username", "") + ":" + sharedPrefs.getString("apn_password", "") + "@";
								
								for (int i = 1; i < parts.length; i++)
								{
									newMmsc += parts[i];
								}
								
								apns.set(0, new APN(newMmsc, apns.get(0).MMSPort, apns.get(0).MMSProxy));
							}
						}
					} catch (Exception f)
					{
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(context, "There may be an error in your username and password settings.", Toast.LENGTH_LONG).show();
							}
						});
					}
				}
				
				try {
					HttpUtils.httpConnection(context, 4444L, apns.get(0).MMSCenterUrl, bytesToSend, HttpUtils.HTTP_POST_METHOD, !TextUtils.isEmpty(apns.get(0).MMSProxy), apns.get(0).MMSProxy, Integer.parseInt(apns.get(0).MMSPort));
				
//					ConnectivityManager mConnMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//					mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
					
					IntentFilter filter = new IntentFilter();
					filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
					BroadcastReceiver receiver = new BroadcastReceiver() {
			
						@Override
						public void onReceive(Context context, Intent intent) {
							Cursor query = context.getContentResolver().query(Uri.parse("content://mms"), new String[] {"_id"}, null, null, "date desc");
							query.moveToFirst();
							String id = query.getString(query.getColumnIndex("_id"));
							query.close();
							
							ContentValues values = new ContentValues();
						    values.put("msg_box", 2);
						    String where = "_id" + " = '" + id + "'";
						    context.getContentResolver().update(Uri.parse("content://mms"), values, where, null);
						    
						    ((MainActivity) context).refreshViewPager3();
						    context.unregisterReceiver(this);
						    
						    if (sharedPrefs.getBoolean("wifi_mms_fix", false))
							{
							    context.unregisterReceiver(discon);
							    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
							    wifi.setWifiEnabled(false);
							    wifi.setWifiEnabled(currentWifiState);
							    Log.v("Reconnect", "" + wifi.reconnect());
							}
						}
						
					};
					
					registerReceiver(receiver, filter);
				} catch (Exception e) {
					
					if (sharedPrefs.getBoolean("wifi_mms_fix", false))
					{
					    context.unregisterReceiver(discon);
					    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					    wifi.setWifiEnabled(false);
					    wifi.setWifiEnabled(currentWifiState);
					    Log.v("Reconnect", "" + wifi.reconnect());
					}
					
					Cursor query = context.getContentResolver().query(Uri.parse("content://mms"), new String[] {"_id"}, null, null, "date desc");
					query.moveToFirst();
					String id = query.getString(query.getColumnIndex("_id"));
					query.close();
					
					ContentValues values = new ContentValues();
				    values.put("msg_box", 5);
				    String where = "_id" + " = '" + id + "'";
				    context.getContentResolver().update(Uri.parse("content://mms"), values, where, null);
				    
					((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
						
						@Override
						public void run() {
							((MainActivity) context).refreshViewPager3();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle(R.string.apn_error_title);
							builder.setMessage(context.getResources().getString(R.string.apn_error_1) + " " +
									           context.getResources().getString(R.string.apn_error_2) + " " +
									           context.getResources().getString(R.string.apn_error_3) + " " +
									           context.getResources().getString(R.string.apn_error_4) + " " +
									           context.getResources().getString(R.string.apn_error_5) +
									           context.getResources().getString(R.string.apn_error_6));
							builder.setNeutralButton(context.getResources().getString(R.string.apn_error_button), new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(context, MmsSettingsActivity.class);
									context.startActivity(intent);
									
								}
								
							});
							
							builder.create().show();
						}
				    	
				    });
				}
				
			}
			
		}).start();
			
	}
	
	public void setContactSearchPosition(int position)
	{
		this.contactSearchPosition = position;
	}
	
	public int getContactSearchPosition()
	{
		return this.contactSearchPosition;
	}
	
	private void writeToFile2(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("notifications.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
	
	private void writeToFile3(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("conversationList.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
	
	public Uri getImageUri(Context inContext, Bitmap inImage) {
		  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		  inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		  String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		  return Uri.parse(path);
		}
	
	private ArrayList<String> readFromFile(Context context) {
		
	      ArrayList<String> ret = new ArrayList<String>();
	      
	      try {
	          InputStream inputStream = context.openFileInput("newMessages.txt");
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null ) {
	          		ret.add(receiveString);
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
	  	
	  	private void writeToFile(ArrayList<String> data, Context context) {
	        try {
	            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("newMessages.txt", Context.MODE_PRIVATE));
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	outputStreamWriter.write(data.get(i) + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}
	  	
	  	private void readFromFile3(Context context) {
		      
		      try {
		          InputStream inputStream = context.openFileInput("conversationList.txt");
		          
		          if ( inputStream != null ) {
		          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		          	String receiveString = "";
		          	
		          	while ( (receiveString = bufferedReader.readLine()) != null ) {
		          		threadIds.add(receiveString);
		          		
		          		receiveString = bufferedReader.readLine();
		          		try
		          		{
		          			Integer.parseInt(receiveString);
			          		msgCount.add(receiveString);
		          		} catch (Exception e)
		          		{
		          			msgCount.add("1");
		          		}
		          		
		          		receiveString = bufferedReader.readLine();
		          		try
		          		{
		          			Integer.parseInt(receiveString);
			          		msgRead.add(receiveString);
		          		} catch (Exception e)
		          		{
		          			msgRead.add("1");
		          		}
						
		          		if ( (receiveString = bufferedReader.readLine()) != null )
		          		{
		          			inboxBody.add(receiveString);
		          		} else
		          		{
		          			inboxBody.add("error");
		          		}
		          		
		          		receiveString = bufferedReader.readLine();
		          		try
		          		{
		          			Long.parseLong(receiveString);
			          		inboxDate.add(receiveString);
		          		} catch (Exception e)
		          		{
		          			Calendar cal = Calendar.getInstance();
		          			inboxDate.add(cal.getTimeInMillis() + "");
		          		}
		          		
		          		if ( (receiveString = bufferedReader.readLine()) != null )
		          		{
		          			inboxNumber.add(receiveString);
		          		} else
		          		{
		          			inboxNumber.add("1");
		          		}
		          		
		          		receiveString = bufferedReader.readLine();
		          		if (receiveString != null)
		          		{
			          		if (receiveString.equals("yes") || receiveString.equals("no"))
			          		{
			          			group.add(receiveString);
			          		} else
			          		{
			          			group.add("no");
			          		}
		          		} else
		          		{
		          			group.add("no");
		          		}
		          	}
		          	
		          	inputStream.close();
		          }
		      }
		      catch (FileNotFoundException e) {
		      	
				} catch (IOException e) {
					
				}
			}
	  	
	  	@SuppressWarnings("resource")
		private ArrayList<String> readFromFile4(Context context) {
			
		      ArrayList<String> ret = new ArrayList<String>();
		      
		      try {
		    	  InputStream inputStream;
		          
		          if (sharedPrefs.getBoolean("save_to_external", true))
		          {
		         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt");
		          } else
		          {
		        	  inputStream = context.openFileInput("templates.txt");
		          }
		          
		          if ( inputStream != null ) {
		          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		          	String receiveString = "";
		          	
		          	while ( (receiveString = bufferedReader.readLine()) != null ) {
		          		ret.add(receiveString);
		          	}
		          	
		          	inputStream.close();
		          }
		      }
		      catch (FileNotFoundException e) {
		      	
				} catch (IOException e) {
					
				}

		      return ret;
			}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {
		
		public ArrayList<String> contact = null;
		
		public SectionsPagerAdapter(android.app.FragmentManager fm) {
			super(fm);
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					ArrayList<String> contacts = new ArrayList<String>();
					
					for (int i = 0; i < inboxNumber.size(); i++)
					{
						contacts.add(loadGroupContacts(inboxNumber.get(i), getBaseContext()));
					}
					
					contact = new ArrayList<String>();
					contact = contacts;
					
				}
				
			}).start();
		}

		@Override
		public DummySectionFragment getItem(int position) {
			DummySectionFragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			boolean[] inboxSents = new boolean[inboxSent.size()];
			boolean[] mmsArray = new boolean[mms.size()];
			
			for (int i = 0; i < inboxSent.size(); i++)
			{
				inboxSents[i] = inboxSent.get(i);
				mmsArray[i] = mms.get(i);
			}
			
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			args.putInt("position", position);
			args.putStringArrayList("numbers", inboxNumber);
			args.putString("myId", myContactId);
			args.putString("myPhone", myPhoneNumber);
			args.putStringArrayList("threadIds", threadIds);
			args.putStringArrayList("group", group);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return inboxNumber.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {	
			String text = "No Messages";

            if (!sharedPrefs.getBoolean("hide_title_bar", true))
            {
                return "";
            }

			if (contact == null)
			{
				if (inboxNumber.size() >= 1)
				{
					if (group.get(position).equals("yes"))
					{
						if (sharedPrefs.getBoolean("title_caps", true))
						{
							text = "GROUP MMS";
						} else
						{
							text = "Group MMS";
						}
					} else
					{
						if (sharedPrefs.getBoolean("title_caps", true))
						{
							if (sharedPrefs.getBoolean("always_show_contact_info", false))
							{
								String[] names = findContactName(inboxNumber.get(position), getBaseContext()).split(" ");
								text = names[0].trim().toUpperCase(Locale.getDefault());
							} else
							{
								text = findContactName(inboxNumber.get(position), getBaseContext()).toUpperCase(Locale.getDefault());
							}
						} else
						{
							if (sharedPrefs.getBoolean("always_show_contact_info", false))
							{
								try
								{
									String[] names = findContactName(inboxNumber.get(position), getBaseContext()).split(" ");
									text = names[0].trim();
								} catch (Exception e)
								{
									text = findContactName(inboxNumber.get(position), getBaseContext());
								}
							} else
							{
								text = findContactName(inboxNumber.get(position), getBaseContext());
							}
						}
					}
				}
				
				return text;
			} else
			{
				try
				{
					if (contact.size() >= 1)
					{
						if (group.get(position).equals("yes"))
						{
							if (sharedPrefs.getBoolean("title_caps", true))
							{
								text = "GROUP MMS";
							} else
							{
								text = "Group MMS";
							}
						} else
						{
							if (sharedPrefs.getBoolean("title_caps", true))
							{
								if (sharedPrefs.getBoolean("always_show_contact_info", false))
								{
									try
									{
										String[] names = contact.get(position).split(" ");
										text = names[0].trim().toUpperCase(Locale.getDefault());
									} catch (Exception e)
									{
										text = contact.get(position).toUpperCase(Locale.getDefault());
									}
								} else
								{
									text = contact.get(position).toUpperCase(Locale.getDefault());
								}
							} else
							{
								if (sharedPrefs.getBoolean("always_show_contact_info", false))
								{
									try
									{
										String[] names = contact.get(position).split(" ");
										text = names[0].trim();
									} catch (Exception e)
									{
										text = contact.get(position);
									}
								} else
								{
									text = contact.get(position);
								}
							}
						}
					}
					
					return text;
				} catch (Exception e)
				{
					if (contact.size() > 0)
					{
						return contact.get(position);
					} else
					{
						return "No Messages";
					}
				}
			}
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends android.app.Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public ArrayList<String> threadIds;
		public int position;
		public ArrayList<String> numbers;
		public ArrayList<String> group;
		public String myId, myPhoneNumber;
		public View view;
		private SharedPreferences sharedPrefs;
		public Context context;
        public Cursor messageQuery;
		
		public DummySectionFragment() {
			
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
		    
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		  super.onConfigurationChanged(newConfig);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
			
			Bundle args = getArguments();
			
			this.position = args.getInt("position");
			this.numbers = args.getStringArrayList("numbers");
			this.myId = args.getString("myId");
			this.myPhoneNumber = args.getString("myPhone");
			this.threadIds = args.getStringArrayList("threadIds");
			this.group = args.getStringArrayList("group");
			
			this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		
		@Override
		public void onAttach(Activity activity)
		{
			super.onAttach(activity);
			context = activity;
		}

        @Override
        public void onDetach()
        {
            super.onDetach();

            try
            {
                messageQuery.close();
            } catch (Exception e)
            {

            }
        }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			view = inflater.inflate(R.layout.message_frame, container, false);
			
			return refreshMessages();
		}		
		
		@SuppressWarnings("deprecation")
		public View refreshMessages()
		{
			final ContentResolver contentResolver = context.getContentResolver();
			
			final TextView groupList = (TextView) view.findViewById(R.id.groupList);
			
			if (group.get(position).equals("yes"))
			{
				new Thread(new Runnable() {

					@Override
					public void run() {
						final String name = loadGroupContacts(numbers.get(position), context);
						
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								groupList.setText(name);
							}
						});
						
					}
					
				}).start();
				
				groupList.setTextColor(getResources().getColor(R.color.white));
				
				if (!sharedPrefs.getBoolean("custom_theme", false))
	        	{
		        	String titleColor = sharedPrefs.getString("title_color", "blue");
					
					if (titleColor.equals("blue"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_blue));
					} else if (titleColor.equals("orange"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_orange));
					} else if (titleColor.equals("red"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_red));
					} else if (titleColor.equals("green"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_green));
					} else if (titleColor.equals("purple"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_purple));
					} else if (titleColor.equals("grey"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.grey));
					} else if (titleColor.equals("black"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.pitch_black));
					} else if (titleColor.equals("darkgrey"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.darkgrey));
					}
	        	} else
	        	{
	        		groupList.setBackgroundColor(sharedPrefs.getInt("ct_titleBarColor", getResources().getColor(R.color.holo_blue)));
	        	}
			} else
			{
				groupList.setHeight(0);
			}
			
			final CustomListView listView = (CustomListView) view.findViewById(R.id.fontListView);

            final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.emptyView);

            if (MainActivity.waitToLoad)
            {
                spinner.setVisibility(View.VISIBLE);
            } else
            {
                spinner.setVisibility(View.GONE);
            }

            if (MainActivity.threadedLoad)
            {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (MainActivity.waitToLoad)
                        {
                            try
                            {
                                Thread.sleep(500);
                            } catch (Exception e)
                            {

                            }

                            MainActivity.waitToLoad = false;
                        }

                        Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadIds.get(position) + "/");
                        String[] projection2;

                        if (sharedPrefs.getBoolean("show_original_timestamp", false))
                        {
                            projection2 = new String[]{"_id", "ct_t", "body", "date", "date_sent", "type", "read", "status", "msg_box"};
                        } else
                        {
                            projection2 = new String[]{"_id", "ct_t", "body", "date", "type", "read", "status", "msg_box"};
                        }

                        messageQuery = contentResolver.query(uri3, projection2, null, null, null);

                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                if (sharedPrefs.getString("run_as", "sliding").equals("sliding"))
                                {
                                    MessageArrayAdapter adapter = new MessageArrayAdapter((Activity) context, myId, numbers.get(position), threadIds.get(position), messageQuery, myPhoneNumber, position);
                                    listView.setAdapter(adapter);
                                    listView.setStackFromBottom(true);
                                    spinner.setVisibility(View.GONE);
                                } else
                                {
                                    com.klinker.android.messaging_hangout.MessageArrayAdapter adapter = new com.klinker.android.messaging_hangout.MessageArrayAdapter((Activity) context, myId, numbers.get(position), threadIds.get(position), messageQuery, myPhoneNumber, position);
                                    listView.setAdapter(adapter);
                                    listView.setStackFromBottom(true);
                                    spinner.setVisibility(View.GONE);
                                }
                            }

                        });

                        try
                        {
                            Thread.sleep(500);
                        } catch (Exception e)
                        {

                        }

                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                try
                                {
                                    listView.setSelection(messageQuery.getCount() - 1);
                                } catch (Exception e)
                                {

                                }
                            }

                        });

                    }

                }).start();
            } else
            {
                Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadIds.get(position) + "/");
                String[] projection2;

                if (sharedPrefs.getBoolean("show_original_timestamp", false))
                {
                    projection2 = new String[]{"_id", "ct_t", "body", "date", "date_sent", "type", "read", "status", "msg_box"};
                } else
                {
                    projection2 = new String[]{"_id", "ct_t", "body", "date", "type", "read", "status", "msg_box"};
                }

                messageQuery = contentResolver.query(uri3, projection2, null, null, null);

                if (sharedPrefs.getString("run_as", "sliding").equals("sliding"))
                {
                    MessageArrayAdapter adapter = new MessageArrayAdapter((Activity) context, myId, numbers.get(position), threadIds.get(position), messageQuery, myPhoneNumber, position);
                    listView.setAdapter(adapter);
                    listView.setStackFromBottom(true);
					spinner.setVisibility (View.GONE);
                } else
                {
                    com.klinker.android.messaging_hangout.MessageArrayAdapter adapter = new com.klinker.android.messaging_hangout.MessageArrayAdapter((Activity) context, myId, numbers.get(position), threadIds.get(position), messageQuery, myPhoneNumber, position);
                    listView.setAdapter(adapter);
                    listView.setStackFromBottom(true);
					spinner.setVisibility (View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.threadedLoad = true;
                    }
                }, 100L);
            }

			listView.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_messageDividerColor", context.getResources().getColor(R.color.light_silver))));
			
			if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true) && sharedPrefs.getString("run_as", "sliding").equals("sliding"))
			{
				listView.setDividerHeight(1);
			} else
			{
				listView.setDividerHeight(0);
			}
			
			return view;
		}
		
		public InputStream openDisplayPhoto(long contactId) {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		     Cursor cursor = getActivity().getContentResolver().query(photoUri,
		          new String[] {Contacts.Photo.PHOTO}, null, null, null);
		     if (cursor == null) {
		         return null;
		     }
		     try {
		         if (cursor.moveToFirst()) {
		             byte[] data = cursor.getBlob(0);
		             if (data != null) {
		                 return new ByteArrayInputStream(data);
		             }
		         }
		     } finally {
		         cursor.close();
		     }
		     return null;
		 }
	}
	
	public static Bitmap getFacebookPhoto(String phoneNumber, Context context) {
		  try
		  {
		    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		    Uri photoUri = null;
		    ContentResolver cr = context.getContentResolver();
		    Cursor contact = cr.query(phoneUri,
		            new String[] { ContactsContract.Contacts._ID }, null, null, null);

		    try
		    {
			    if (contact.moveToFirst()) {
			        long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
			        photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
			        contact.close();
			    }
			    else {
			        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
			        
			        contact.close();
			        return defaultPhoto;
			    }
			    if (photoUri != null) {
			        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
			                cr, photoUri);
			        if (input != null) {
			        	contact.close();
			            return BitmapFactory.decodeStream(input);
			        }
			    } else {
			        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
			        
			        contact.close();
			        return defaultPhoto;
			    }
			    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		        
		        contact.close();
			    return defaultPhoto;
		    } catch (Exception e)
		    {
		        	contact.close();
		        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		    }
		  } catch (Exception e)
		  {
		        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		  }
		}
	
	public static String[] splitByLength(String s, int chunkSize)
  	{
  		int arraySize = (int) Math.ceil((double) s.length() / chunkSize);

  	    String[] returnArray = new String[arraySize];

  	    int index = 0;
  	    for(int i = 0; i < s.length(); i = i+chunkSize)
  	    {
  	        if(s.length() - i < chunkSize)
  	        {
  	            returnArray[index++] = s.substring(i);
  	        } 
  	        else
  	        {
  	            returnArray[index++] = s.substring(i, i+chunkSize);
  	        }
  	    }

  	    return returnArray;
  	}
}