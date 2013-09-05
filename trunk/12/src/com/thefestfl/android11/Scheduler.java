package com.thefestfl.android11;

import java.util.Arrays;
import java.util.Comparator;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

public class Scheduler extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.schedule);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Schedule.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("all").setIndicator("All",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, SchedFri.class);
	    spec = tabHost.newTabSpec("friday").setIndicator("Friday",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SchedSat.class);
	    spec = tabHost.newTabSpec("saturday").setIndicator("Saturday",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, SchedSun.class);
	    spec = tabHost.newTabSpec("sunday").setIndicator("Sunday",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
    	// show menu when menu button is pressed
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.share_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Do some things when menu items pressed.
    	
    	switch(item.getItemId()){
    	case R.id.menuShare: 
    		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    		emailIntent.setType("message/rfc822");
    		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My FEST 11 Schedule!");
    		// build Body here from MySchedule
    		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, makeEmailSchedule());
    		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		try{
    			startActivity(Intent.createChooser(emailIntent, "Email Schedule..."));
    		} catch (android.content.ActivityNotFoundException ex) {
    			Toast.makeText(this, "No email client available", Toast.LENGTH_SHORT).show();
    		}
    		break;
    	}
    	
    	return true;
    }
    
    public String makeEmailSchedule(){
    	String body = new String();
    	String[] shows;
    	int[] pos;
    	
    	MySchedule ms = new MySchedule(this);
		ms.open();
		
		FestDBAdapter fest = new FestDBAdapter(this);
		fest.open();
		
		Cursor c = ms.getSched();
		c.moveToFirst();
		shows = new String[c.getCount()];
		pos = new int[c.getCount()];
		
		// Here I need to not just make an array of Strings, but something ordered
		// by the date-time of the show. Ugh.
		for(int x = 0; x< c.getCount(); x++){
			Cursor f = fest.fetchShowById(c.getInt(0));
			f.moveToFirst();
			shows[x] = f.getString(0);
			if(shows[x].contains("#")) shows[x] = shows[x].replace("#", "'");
			
			String showday = new String();
			int day = f.getInt(1);
			switch(day){
			case 1: showday = "Friday"; break;
			case 2: showday = "Saturday"; break;
			case 3: showday = "Sunday"; break;
			}
			shows[x] = shows[x]+" |"+f.getString(2) +" | "+showday+" "+f.getString(3); 
			c.moveToNext();
			shows[x] = shows[x].replace("#", "'");
		}

		c.close();
		fest.close();
		ms.close();
		
		Arrays.sort(shows, new Comparator<String>(){
			@Override
			public int compare (String entry1, String entry2){
				int day1, day2;
				if(entry1.contains("Friday")) day1 = 1;
				else if (entry1.contains("Saturday")) day1 = 2;
//				else if (entry1.contains("Mon ")) day1 = 4;
				else day1 = 3;
				
				if(entry2.contains("Friday")) day2 = 1;
				else if (entry2.contains("Saturday")) day2 = 2;
//				else if (entry2.contains("Mon ")) day2 = 4;
				else day2 = 3;
				
				if (day1 < day2) return -1; else if (day2 < day1) return 1; else {
					
					
					int hyp1 = entry1.indexOf("-"), hyp2 = entry2.indexOf("-");
					// 00:00XM - 99:99XM
					// 87654321012345678
					int hr1 = Integer.parseInt(entry1.substring(hyp1-9, hyp1-7));
					int hr2 = Integer.parseInt(entry2.substring(hyp2-9, hyp2-7));
					
					if (hr1 > hr2) return 1; else if (hr2 > hr1) return -1; else {
						int min1 = Integer.parseInt(entry1.substring(hyp1-6, hyp1-4));
						int min2 = Integer.parseInt(entry2.substring(hyp2-6, hyp2-4));
						if (min1 > min2) return 1; else return -1;
					}
					
				}
//				return 0;
			}
		});
		
		for(int x = 0; x < shows.length; x++){
			shows[x] = fixTime(shows[x]);
			body = body+"\n"+shows[x];
		}
    	
    	return body;
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
