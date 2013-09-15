/*
 *  John R. Flynn
 *  "The Fest! 10"
 * 
 */

package com.thefestfl.android12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.thefestfl.android12.R;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

// Activity for the Sunday tab in ByDate

public class DateSun extends ListActivity {
	
	Context mCtx;
	int[] my;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FestDBAdapter festDB = new FestDBAdapter(this);
        festDB.open();
        
        MySchedule ms = new MySchedule(this);
        ms.open();
        
        mCtx = this;
        
        Cursor c = festDB.fetchDateShows(4);
        c.moveToFirst();
        
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        String [] from = new String[] {"show"};
        int[] to = new int[] {R.id.show};
        
        my = new int[c.getCount()];
        
        for(int x = 0; x < c.getCount(); x++){
        	HashMap<String, String> map = new HashMap<String, String>();
      	
        	String show = c.getString(0) + "\n"+ c.getString(2)+"\n" + 
     			   c.getString(3);
        	show = show.replace("#", "'");
        	show = fixTime(show);
        	map.put("show", show);
        	if(!(show.contains("*removed*"))){
        		if(ms.checkShow(c.getInt(4))) my[x] = 1; else my[x] = 0;
        		fillMaps.add(map);
        	}
        	c.moveToNext();
        }
        
        ms.close();
        festDB.close();
        c.close();
        
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
    	        
    	        Cursor c = festDB.fetchDateShows(3);    			
    			c.moveToPosition(position);
    			
    			if(ms.checkShow(c.getInt(4))){
    				int showId = ms.getShowId(c.getInt(4));
    				ms.removeShow(showId);
    				view.setBackgroundColor(Color.BLACK);
    				my[position] = 0;
    			} else {
    				my[position] = 1;
    				view.setBackgroundColor(Color.rgb(51,102,51));
    				ms.addShow(c.getInt(4));
    			
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
