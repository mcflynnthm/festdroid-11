package com.thefestfl.android12;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.xmlpull.v1.XmlPullParserException;

import com.thefestfl.android12.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SchedThur extends ListActivity implements OnItemClickListener {
	Context mCtx;
	String[] shows;
	int[] pos;
	
	public void onResume(){
		super.onResume();
		onCreate(new Bundle());
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		MySchedule ms = new MySchedule(this);
		ms.open();
		
		mCtx = this;
		
		FestDBAdapter fest = new FestDBAdapter(this);
		fest.open();
		
		Cursor c = ms.getSched();
		c.moveToFirst();
		shows = new String[c.getCount()];

		int count = 0;
		
		// Here I need to not just make an array of Strings, but something ordered
		// by the date-time of the show. Ugh.
		for(int x = 0; x < c.getCount(); x++){
			Cursor f = fest.fetchShowById(c.getInt(0));
			f.moveToFirst();
			shows[count] = f.getString(0);
			if(shows[count].contains("#")) shows[count] = shows[count].replace("#", "'");
			
        	String showday = new String();
			int day = f.getInt(1);
			switch(day){
			case 1: showday = "Thursday"; break;
			case 2: showday = "Friday"; break;
			case 3: showday = "Saturday"; break;
			case 4: showday = "Sunday"; break;
			}
        	
			if(showday.equals("Thursday")){
				
				shows[count] = c.getInt(1)+"<"+shows [count]+"\n"+f.getString(2) +"\n"+showday+" "+f.getString(3); 
				shows[count] = shows[count].replace("#", "'");
				count++;
				
			}
			c.moveToNext();
		}
		
		c.close();
		fest.close();
		ms.close();
		
		String[] newShows = new String[count];
		for(int x = 0; x < count; x++){
			newShows[x] = shows[x];
		}
		pos = new int[count];
		
		shows = newShows;
		
		Arrays.sort(shows, new Comparator<String>(){
			@Override
			public int compare (String entry1, String entry2){
				int day1, day2;
				if(entry1.contains("Friday")) day1 = 2;
				else if (entry1.contains("Thursday")) day1 = 1;
				else if (entry1.contains("Saturday")) day1 = 3;
//				else if (entry1.contains("Mon ")) day1 = 4;
				else day1 = 4;
				
				if(entry2.contains("Friday")) day2 = 2;
				else if (entry2.contains("Saturday")) day2 = 3;
				else if (entry2.contains("Thursday")) day2 = 1;
//				else if (entry2.contains("Mon ")) day2 = 4;
				else day2 = 4;
				
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
		
		for(int x = 0; x < pos.length; x++){
			int temp = shows[x].indexOf("<");
			pos[x] = Integer.parseInt(shows[x].substring(0, temp));
			shows[x] = shows[x].substring(temp+1, shows[x].length());
			shows[x] = fixTime(shows[x]);
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, shows));
    	
    	ListView lv = getListView();
    	lv.setTextFilterEnabled(true);
    	
    	lv.setOnItemClickListener(this);
		
	}
	
	int posi;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		
		posi = position;
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	
		        	//ListView lv = getListView();
		    		
		    		MySchedule ms = new MySchedule(mCtx);
		    		ms.open();
		    		
		    		ms.removeShow(pos[posi]);
		    		String[] newShows = new String[shows.length - 1];
		    		int [] newpos = new int[pos.length - 1];
		    		int x = 0, y = 0;
		    		while (x < shows.length){
		    			if (x != posi) {
		    				newShows[y] = shows[x];
		    				newpos[y] = pos[x];
		    				y++;
		    			}
		    			x++;
		    		}
		    		shows = newShows;
		    		pos = newpos;
		    		
		    		ms.close();
		    		
		    		setListAdapter(new ArrayAdapter<String>(mCtx, R.layout.list_item, shows));
		        	
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Remove show").setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
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
