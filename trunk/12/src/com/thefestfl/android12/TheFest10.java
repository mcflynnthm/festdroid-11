/*
 *  John R. Flynn
 *  
 *  "The Fest! 10"
 * 
 */

package com.thefestfl.android12;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.thefestfl.android12.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

// The main Activity. Contains the initial UI and contains the
// method for updating the databases as necessary.

public class TheFest10 extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	
	Context mCtx;
	
	// I think we all know what onCreate does, right?
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mCtx = this;
        SharedPreferences prefs = getSharedPreferences("Fest12Band", MODE_PRIVATE);
		
        boolean runOnce = prefs.getBoolean("runOnce", false);
        MySchedule ms = new MySchedule(this);
        ms.open();
        
		if (!runOnce)
			pBar();
		else
			try {
				updateDB();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		Button band = (Button)findViewById(R.id.mainByBand);
        Button date = (Button)findViewById(R.id.mainByDate);
        Button venue = (Button)findViewById(R.id.mainByVenue);
        Button sched = (Button)findViewById(R.id.mainMySched);
        
        band.setOnClickListener(this);
        date.setOnClickListener(this);
        venue.setOnClickListener(this);
        sched.setOnClickListener(this);
        
        
        ms.close();
        
    }
	
	// Figures out what button you're cliking. Does this
	// REALLY need to be commented?
	public void onClick (View v){
		String text = "";
		
		switch(v.getId()){
		case R.id.mainByBand:{
	    	Intent i = new Intent(this, ByBand.class);
	    	startActivity(i);
	    	break;
		}
		case R.id.mainByDate:{
			Intent i = new Intent(this, ByDate.class);
	    	startActivity(i);
	    	break;
		}
		case R.id.mainByVenue:{
			Intent i = new Intent(this, ByVenue.class);
	    	startActivity(i);
	    	break;
		}
		case R.id.mainMySched:{
			Intent i = new Intent(this, Scheduler.class);
	    	startActivity(i);
	    	break;
		}
		default: text = "wtf you doin son"; break;
		}
	
		if ( text != "") {
			Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
    
	static final String NAME = "name";
	static final String BAND = "band";
	static final String PHOTO = "photo";
	static final String MP3 = "mp3";
	static final String DESC = "description";
	static final String SONG_URL = "songURL";
	static final String SONG_NAME = "songName";
	static final String DATE = "date";
	static final String DURATION = "length";
	static final String VENUE = "venue";
	static final String ACOUSTIC = "acoustic";
	static final String DAY = "day";
	static final String TIME = "time";
	
    // Parses the XML file and rebuilds the database
	// The XML is updated so new bands are added at the bottom
	// so as to not disturb existing shows in MySchedule
	public int ParseXML(String XmlUrl) throws XmlPullParserException, IOException{
        
		URL xUrl;
		String text;
        
    	try{
    		xUrl = new URL(XmlUrl);
    	} catch (MalformedURLException e) {
    		Log.e("ParseXML", "Something wrong with the band XML URL");
    		xUrl = null;
    	}
        
        XmlPullParserFactory fac;
        fac = XmlPullParserFactory.newInstance();
		
    	XmlPullParser parser = fac.newPullParser();
    	
    	if (xUrl != null){
    		try {
				parser.setInput(new InputStreamReader(xUrl.openStream()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("XML error", "openStream failed; using built-in XML.");
				parser = getResources().getXml(R.xml.bands);
			}
    	}else{
    		Log.e("URL error", "Url didn't translate correctly; using built-in XML");
    		parser = getResources().getXml(R.xml.bands);
    	}

    	ContentValues cv = new ContentValues();
    	ContentValues bcv = new ContentValues();
    	
    	SharedPreferences prefs = getSharedPreferences("Fest12Band", MODE_PRIVATE);
    	boolean runOnce = prefs.getBoolean("runOnce", false);
    	
    	FestDBAdapter festDB = new FestDBAdapter(this);
    	festDB.open();
    	if (runOnce) festDB.destroyDB();
    	festDB.createDB();
    	
    	BandDbAdapter bandDB = new BandDbAdapter(this);
    	bandDB.open();
    	if (runOnce) bandDB.destroyDB();
    	bandDB.createDB();
    	
    	try {
    	    int eventType = parser.getEventType();
    	    String name = null;
    	    while (eventType != XmlPullParser.END_DOCUMENT) {
    	        

    	        switch (eventType){
    	            case XmlPullParser.START_TAG:
    	                name = parser.getName().toLowerCase();
    	                break;
    	            case XmlPullParser.TEXT:
    	            	text = parser.getText();
    	            	text = text.replace("'", "#");
    	            	if(text.matches("^\\s*$")) {
    	            	    break;
    	            	}
    	            	text = text.replace("&", "and");
    	            	if (name.equals(NAME)){
//    	            		cv.put(NAME, text);
    	            		bcv.put(NAME, text);
    	            	} else if (name.equals(DESC)){
    	            		if(parser.getText().equals(null)) bcv.put(DESC, "n/a"); else bcv.put(DESC, text);
    	            	} else if (name.equals(PHOTO)){
    	            		bcv.put(PHOTO, text);
    	            	}
    	            	break;
    	            case XmlPullParser.END_TAG:
    	                
    	            	name = parser.getName();
    	            	
    	            	if(name.equals("band")){
    	            		if(!bandDB.checkBand(bcv.getAsString(NAME))){
    	            			bandDB.addBand(bcv);
    	            		}
    	            		bcv = new ContentValues();
    	            	} else if (name.equals("show")){
//    	            		festDB.addBand(cv);
    	            	}
    	            	if (name.equals("shows")) cv = new ContentValues();
    	                break;
    	        }

    	        eventType = parser.next();
    	    }
    	}
    	catch (XmlPullParserException e) {
    		Log.e(e.getMessage(), "Shit's fucked");
    	    throw new RuntimeException("Cannot parse XML");
    	}
    	catch (IOException e) {
    	    throw new RuntimeException("Cannot parse XML");
    	}
    	finally {
    	    bandDB.close();
//    	    festDB.close();
    	}
    	
    	/*
    	 * Create the DB of shows from my home cooked XML. Yikes.
    	 */
    	
    	try{
    		xUrl = new URL("http://johnrflynn.com/fest12/shows.xml");
    	} catch (MalformedURLException e){
    		Log.e("ParseXML", "Something wrong with the show XML URL");
    		xUrl = null;
    	}
    	
        fac = XmlPullParserFactory.newInstance();
		parser = fac.newPullParser();
    	
    	if (xUrl != null){
    		try {
				parser.setInput(new InputStreamReader(xUrl.openStream()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("XML error", "openStream failed; using built-in XML.");
				parser = getResources().getXml(R.xml.shows);
			}
    	}else{
    		Log.e("URL error", "Url didn't translate correctly; using built-in XML");
    		parser = getResources().getXml(R.xml.shows);
    	}
    	
    	try {
    	    int eventType = parser.getEventType();
    	    String name = null;
    	    while (eventType != XmlPullParser.END_DOCUMENT) {
    	        

    	        switch (eventType){
    	            case XmlPullParser.START_TAG:
    	                name = parser.getName().toLowerCase();
    	                break;
    	            case XmlPullParser.TEXT:
    	            	text = parser.getText();
    	            	text = text.replace("'", "#");
    	            	if(text.matches("^\\s*$")) {
    	            	    break;
    	            	}
    	            	text = text.replace("&", "and");
    	            	if (name.equals("show")){
    	            		cv = new ContentValues();
    	            	} else if (name.equals(BAND)){
    	            		cv.put(NAME, text);
    	            	} else if (name.equals(TIME)){
    	            		text = parser.getText();
    	            		String starttime, endtime;
    	            		starttime = text.substring(0, text.indexOf("-"));
    	            		endtime = text.substring(text.indexOf("-")+1, text.length());
    	            		if(starttime.contains("am")){
    	            			starttime = starttime.replace("12:","24:");
    	            			starttime = starttime.replace("1:","25:");
    	            			//starttime = starttime.replace("2:","26:");
    	            		} else if (starttime.contains("pm")){
    	            			if(starttime.indexOf(":") == 1){
    	            				if(starttime.contains("1:")){
    	            					starttime = starttime.replace("1:","13:");
    	            				} else if(starttime.contains("2:")){
    	            					starttime = starttime.replace("2:","14:");
    	            				} else if(starttime.contains("3:")){
    	            					starttime = starttime.replace("3:","15:");
    	            				}else if(starttime.contains("4:")){
    	            					starttime = starttime.replace("4:","16:");
    	            				}else if(starttime.contains("5:")){
    	            					starttime = starttime.replace("5:","17:");
    	            				}else if(starttime.contains("6:")){
    	            					starttime = starttime.replace("6:","18:");
    	            				}else if(starttime.contains("7:")){
    	            					starttime = starttime.replace("7:","19:");
    	            				}else if(starttime.contains("8:")){
    	            					starttime = starttime.replace("8:","20:");
    	            				}else if(starttime.contains("9:")){
    	            					starttime = starttime.replace("9:","21:");
    	            				}
    	            				
    	            			} else {
    	            				starttime = starttime.replace("11:","23:");
        	            			starttime = starttime.replace("10:","22:");
        	            			
    	            			}
    	            			
    	            		}
    	            		if(endtime.contains("am")){
    	            			endtime = endtime.replace("12:","24:");
    	            			endtime = endtime.replace("1:","25:");
    	            			//endtime = endtime.replace("2:","26:");
    	            		} else if (endtime.contains("pm")) {
    	            			if (endtime.indexOf(":") == 2){
    	            				if(endtime.contains("1:")){
    	            					endtime = endtime.replace("1:","13:");
    	            				} else if(endtime.contains("2:")){
    	            					endtime = endtime.replace("2:","14:");
    	            				} else if(endtime.contains("3:")){
    	            					endtime = endtime.replace("3:","15:");
    	            				}else if(endtime.contains("4:")){
    	            					endtime = endtime.replace("4:","16:");
    	            				}else if(endtime.contains("5:")){
    	            					endtime = endtime.replace("5:","17:");
    	            				}else if(endtime.contains("6:")){
    	            					endtime = endtime.replace("6:","18:");
    	            				}else if(endtime.contains("7:")){
    	            					endtime = endtime.replace("7:","19:");
    	            				}else if(endtime.contains("8:")){
    	            					endtime = endtime.replace("8:","20:");
    	            				}else if(endtime.contains("9:")){
    	            					endtime = endtime.replace("9:","21:");
    	            				}
    	            			} else {
    	            				endtime = endtime.replace("11:","23:");
        	            			endtime = endtime.replace("10:","22:");
    	            			}
    	            			
    	            			
    	            		}
    	            		text = starttime + " - " + endtime;
    	            		cv.put(TIME, text);
    	            	} else if (name.equals(VENUE)){
    	            		cv.put(VENUE, text);
    	            	} else if (name.equals(DAY)){
    	            		cv.put(DAY, text);
    	            	}
    	            	break;
    	            case XmlPullParser.END_TAG:
    	                
    	            	name = parser.getName();
    	            	
    	            	if (name.equals("show")){
    	            		festDB.addBand(cv);
    	            	}
    	            	if (name.equals("shows")) cv = new ContentValues();
    	                break;
    	        }

    	        eventType = parser.next();
    	    }
    	}
    	catch (XmlPullParserException e) {
    		Log.e(e.getMessage(), "Shit's fucked");
    	    throw new RuntimeException("Cannot parse XML");
    	}
    	catch (IOException e) {
    	    throw new RuntimeException("Cannot parse XML");
    	}
    	finally {
    	    festDB.close();
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	String name = null;
    	int ver = 1;
    	
    	fac = XmlPullParserFactory.newInstance();
    	parser = fac.newPullParser();
    	
    	try {
    	    // Create a URL for the desired page
//    		URL url = new URL("http://robmcw.com/thefest11/thefest.xml");
    	    URL url = new URL("http://johnrflynn.com/fest12/thefest.xml");
    		
    	    parser.setInput(new InputStreamReader(url.openStream()));

    	    // Read all the text returned by the server
    	    int eventType = parser.getEventType();
    	    
    	    while (eventType != XmlPullParser.END_DOCUMENT) {
    	        
    	        switch (eventType){
    	        case XmlPullParser.START_TAG: name = parser.getName(); break;
    	        case XmlPullParser.TEXT:
    	        	if (name.equals("version")) ver = Integer.parseInt(parser.getText());
    	        	break;
    	        case XmlPullParser.END_TAG: name = ""; break;
    	        }
    	        eventType = parser.next();
    	    }
    	} catch (MalformedURLException e) {
    	} catch (IOException e) {
    	}
    	
    	
    	prefs = getSharedPreferences("Fest12Band", MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("runOnce", true);
		editor.putString("version", ""+ver);
		editor.commit();
    	
		return 100;
    }
    
    // Called by the menu button to check for a update
	// to the XML file
	public void updateDB() throws XmlPullParserException, IOException{
    	
    	String name = null;
	    int ver = 0;
    	
    	XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
    	XmlPullParser parser = fac.newPullParser();
    	
    	try {
    	    // Create a URL for the desired page
    		
//    		URL url = new URL("http://robmcw.com/thefest11/thefest.xml");
    	    URL url = new URL("http://johnrflynn.com/fest12/thefest.xml");
    		
    	    parser.setInput(new InputStreamReader(url.openStream()));

    	    // Read all the text returned by the server
    	    int eventType = parser.getEventType();
    	    
    	    while (eventType != XmlPullParser.END_DOCUMENT) {
    	        
    	        switch (eventType){
    	        case XmlPullParser.START_TAG: name = parser.getName(); break;
    	        case XmlPullParser.TEXT:
    	        	if (name.equals("version")) ver = Integer.parseInt(parser.getText());
    	        	break;
    	        case XmlPullParser.END_TAG: name = ""; break;
    	        
    	        
    	        }
    	        eventType = parser.next();
    	    }
    	} catch (MalformedURLException e) {
    	} catch (IOException e) {
    	}
    	
    	SharedPreferences prefs = getSharedPreferences("Fest12Band", MODE_PRIVATE);
    	int localver = Integer.parseInt(prefs.getString("version", "0"));
    	Log.i("Local", ""+localver);
    	Log.i("Remote", ""+ver);
    	
    	if(localver >= ver){
    		Log.i("UpdateDB","Already up to date");
    		Toast toast = Toast.makeText(this, "Schedule up to date", Toast.LENGTH_SHORT);
    		toast.show();
    	} else {
    		Log.i("UpdateDB","Updating");
    		pBar();
    	}
    	
    }
	
	public int mProgressStatus = 0;
    public Handler mHandler = new Handler();
    public ProgressDialog mProgress;
	
    /*
     *  Wrapper function for the Progress Dialog while updating the database
     */
	public void pBar(){

		mProgress = ProgressDialog.show(this, "Updating Database", "Please Wait...", false);
        
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
        	
            public void run() {
            	
                while (mProgressStatus < 100) {
                    try {
						mProgressStatus = ParseXML("http://johnrflynn.com/fest12/bands.xml");
//						mProgressStatus = ParseXML("http://robmcw.com/thefest11/bands.xml");

					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                }
                mProgress.dismiss();
                
            }
        }).start();
	}
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	// show menu when menu button is pressed
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Do some things when menu items pressed.
    	
    	String message = "";
    	switch(item.getItemId()){
    	case R.id.menuUpdate: try {
				updateDB();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} break;
    	case R.id.menuMap: Intent i = new Intent(this, FestMap.class);
    		startActivity(i);
    		break;
    	}
    	
    	if(!message.equals("")){
    		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
    		toast.show();
    	}
    	
    	return true;
    }
    
}