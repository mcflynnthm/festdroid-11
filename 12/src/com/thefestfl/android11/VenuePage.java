/*
 *  John R. Flynn
 *  "The Fest! 10"
 * 
 */

package com.thefestfl.android11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

// This Activity generates a ListView of the shows at the venue
// chosen in the ByVenue Activity.

public class VenuePage extends ListActivity {
	
	int[] my;
	Context mCtx;
	String venue;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences("FestBand", MODE_PRIVATE);
        venue = prefs.getString("venue", "8 Seconds");
        setTitle(venue);
        
        mCtx = this;
        
        FestDBAdapter festDB = new FestDBAdapter(this);
        festDB.open();
        
        if (venue.contains("'")) venue = venue.replace("'", "#");
        Cursor c = festDB.fetchVenueShows(venue);
        c.moveToFirst();
        
        MySchedule ms = new MySchedule(this);
        ms.open();        
        
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        String [] from = new String[] {"show"};
        int[] to = new int[] {R.id.show};
        
        my = new int[c.getCount()];
        
        for(int x = 0; x < c.getCount(); x++){
        	HashMap<String, String> map = new HashMap<String, String>();
      	
        	String showday = new String();
        	switch(c.getInt(1)){
        	case 1: showday = "Friday"; break;
        	case 2: showday = "Saturday"; break;
        	case 3: showday = "Sunday"; break;
        	}
        	
        	String bandname = c.getString(0).replace("#", "'");
        	String show = bandname + "\n" + 
        			   showday + " " + fixTime(c.getString(2));
        	map.put("show", show);
        	if(!(show.contains("*removed*"))){
        		if(ms.checkShow(c.getInt(3))) my[x] = 1; else my[x] = 0;
            	fillMaps.add(map);
        	}
        	c.moveToNext();
        }
        
        c.close();
        ms.close();
        festDB.close();
        
        
        ShowAdapter adapter = new ShowAdapter(this, fillMaps, R.layout.list_item, from, to, my);
        
        setListAdapter(adapter);
    	
    	ListView lv = getListView();
    	lv.setTextFilterEnabled(true);
    	
    	lv.setOnItemClickListener(new OnItemClickListener(){
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
    			MySchedule ms = new MySchedule(mCtx);
    			ms.open();
    			
    			FestDBAdapter festDB = new FestDBAdapter(mCtx);
    	        festDB.open();
    	        
    	        Cursor c = festDB.fetchVenueShows(venue);    			
    			c.moveToPosition(position);
    			
    			if(ms.checkShow(c.getInt(3))){
    				int showId = ms.getShowId(c.getInt(3));
    				ms.removeShow(showId);
    				view.setBackgroundColor(Color.BLACK);
    				my[position] = 0;
    			} else {
    				my[position] = 1;
    				view.setBackgroundColor(Color.rgb(51,102,51));
    				ms.addShow(c.getInt(3));
    			
    			}
    			
    			festDB.close();
    			ms.close();
    			c.close();
    		}
    	});
    	
    }
	
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
