/*
 *  John R. Flynn
 *  "The Fest! 10"
 * 
 */

package com.thefestfl.android12;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.thefestfl.android12.R;

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class BandPage extends Activity implements Runnable{

	String name;
	String pic;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.bandpage);
        
        // Open the two databases: FestDB for shows
        // and BandDB for the band info.
        BandDbAdapter bandDb = new BandDbAdapter(this);
        bandDb.open();
        
        FestDBAdapter festDb = new FestDBAdapter(this);
        festDb.open();
        
        MySchedule my = new MySchedule(this);
        my.open();
        
        // Now who were we talking about? Oh, right.
        SharedPreferences prefs = getSharedPreferences("FestBand", MODE_PRIVATE);
        name = prefs.getString("band", "Red City Radioz");
        
        if(name.contains("'")) name = name.replace("'", "#");
        Log.i("name", "*"+name+"*");
        if(bandDb.checkBand(name))Log.i("in there", "true"); else Log.i("in there", "false");
        
        Cursor c = bandDb.fetchBand(name);
        c.moveToFirst();
        
        Cursor b = festDb.fetchShows(name);
        b.moveToFirst();
        
        // Set it up
        String bname, info;
        bname = c.getString(0);
        pic = "http://www.thefestfl.com"+c.getString(1);
        info = c.getString(2);
        
        // Alter the page all dynamic-like
        TextView tv = (TextView)findViewById(R.id.bandName);
        tv.setText(bname);
        
        TextView infov = (TextView)findViewById(R.id.bandInfo);
        infov.setText(info);
        
//        ImageView imgV = (ImageView)findViewById(R.id.bandPic);
//        imgV.setImageBitmap(getPic(pic));
        picHandler();
        
        // Set up the GridView for the shows this band will be playing
        
        String[] shows = null;
        shows = new String[b.getCount()];
        int[] mys = new int[b.getCount()];
        for (int x = 0; x<b.getCount(); x++){
        	
        	if(my.checkShow(b.getInt(3))) mys[x] = 1; else mys[x] = 0;
        	
        	String showday = new String();
        	switch(b.getInt(0)){
        	case 1: showday = "Friday"; break;
        	case 2: showday = "Saturday"; break;
        	case 3: showday = "Sunday"; break;
        	}
        	shows[x] = b.getString(1)+"\n"+showday+" "+b.getString(2);
        	shows[x] = fixTime(shows[x]);
        	b.moveToNext();
        }
        
        c.close();
        b.close();
        my.close();
        bandDb.close();
        festDb.close();
        
        GridView gv = (GridView)findViewById(R.id.bandShows);
        gv.setAdapter(new BandAdapter(this, shows, mys));
        gv.setVerticalSpacing(1);
        gv.setBackgroundColor(Color.WHITE);
        
        gv.setOnItemClickListener(
        	new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					
					Context mCtx = getBaseContext();
					MySchedule ms = new MySchedule(mCtx);
	    			ms.open();
	    			
	    			FestDBAdapter festDB = new FestDBAdapter(mCtx);
	    	        festDB.open();
	    	        
	    	        Cursor c = festDB.fetchShows(name);    			
	    			c.moveToPosition(position);
	    			
	    			if(ms.checkShow(c.getInt(3))){
	    				int showId = ms.getShowId(c.getInt(3));
	    				ms.removeShow(showId);
	    				view.setBackgroundColor(Color.WHITE);
	    			} else {
	    				view.setBackgroundColor(Color.rgb(51,102,51));
	    				ms.addShow(c.getInt(3));
	    			
	    			}
	    			
	    			c.close();
	    			festDB.close();
	    			ms.close();
	    			
				}
        		
        	}
        );
        
        // Add onClickListener to gv that add/removes show as applicable

	}
	
	/*
	 *  Retrieves the band's picture from the Fest website
	 *  Happily, they're already there, and formatted to a
	 *  standard size, 111px x 200px!
	 *  Well, they're there until they decide to remove all of
	 *  last year's Fest stuff :(
	 */
	private Bitmap getPic(String srcUrl){
		Bitmap bitmap = null;
		InputStream in = null;
		
		try{
			in = OpenHttpConnection(srcUrl);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e){
			e.printStackTrace();
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.noband);
		} catch (NullPointerException e){
			e.printStackTrace();
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.noband);
		}
		return bitmap;
		
	}
	
	/*
	 * Opens that HTTP connection for the picture above.
	 */
	private InputStream OpenHttpConnection(String urlString) 
    throws IOException
    {
        InputStream in = null;
        int response = -1;
               
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection))                     
            throw new IOException("Not an HTTP connection");
        
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect(); 

            response = httpConn.getResponseCode();                 
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();                                 
            }                     
        }
        catch (Exception ex)
        {
            throw new IOException("Error connecting");            
        }
        return in;     
    }
	
	public int mProgressStatus = 0;
    public Handler mHandler = new Handler();
    public ProgressDialog mProgress;
    public Bitmap image;
    
	public void picHandler(){
		image = null;
		mProgress = ProgressDialog.show(this, "Loading band", "Please Wait...", false);
        
        // Start lengthy operation in a background thread
        new Thread(this).start();
        
	}
	
	public void run() {
    	
        while (mProgressStatus < 100) {
            image = getPic(pic);
            if(image != null) mProgressStatus = 100;

            // Update the progress bar
            mHandler.post(new Runnable() {
                public void run() {
                    mProgress.setProgress(mProgressStatus);
                }
            });
        }
        handle.sendEmptyMessage(0);
        
    }
	
	private Handler handle = new Handler(){
		
		@Override
		public void handleMessage(Message msg){
			mProgress.dismiss();
	        ImageView imgV = (ImageView)findViewById(R.id.bandPic);
	        imgV.setImageBitmap(image);
		}
	};
	
	public String fixTime(String time){
		String newTime = new String();
		
		newTime = time;
		newTime = newTime.replace("25:","1:");
		newTime = newTime.replace("24:","12:");
		newTime = newTime.replace("23:","11:");
		newTime = newTime.replace("22:","10:");
		newTime = newTime.replace("21:","9:");
		newTime = newTime.replace("20:","8:");
		newTime = newTime.replace("19:","7:");
		newTime = newTime.replace("18:","6:");
		newTime = newTime.replace("17:","5:");
		newTime = newTime.replace("16:","4:");
		newTime = newTime.replace("15:","3:");
		newTime = newTime.replace("14:","2:");
		newTime = newTime.replace("13:","1:");
		
		return newTime;
	}
}
