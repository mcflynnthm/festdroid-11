/*
 *  John R. Flynn
 *  "The Fest! 10"
 * 
 */

package com.thefestfl.android11;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Adapter for working with the Fest (individual shows)
// DB table. Similar to, but not quite exactly, BandDbAdapter

public class FestDBAdapter {
	
//	private static final String TAG = "FestDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase FestDb;

	static final String KEY_ID = "_id";
	static final String KEY_NAME = "name";
	static final String KEY_DATE = "date";
	static final String KEY_DAY = "day";
	static final String KEY_DURATION = "length";
	static final String KEY_TIME = "time";
	static final String KEY_VENUE = "venue";
	static final String KEY_ACOUSTIC = "acoustic";
	
	private static final String FESTDB_CREATE =
        "create table if not exists fest (" + 
        "_id integer primary key autoincrement, " +
        "name text not null, " + 
//		"date integer not null, " +
        "day integer not null, " +
//		"duration text not null, " +
        "time text not null, " +
        "venue text not null " +
//        "acoustic text not null" +
        ");";
	
	private static final String DATABASE_NAME = "data";
    private static final String FESTDB_TABLE = "fest";
    private static final int DATABASE_VERSION = 2;
    
    private final  Context mCtx;
    
    // Subclass for helping out with FestDB
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

    	DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);    
        }
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(FESTDB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
		public void onOpen(SQLiteDatabase db){
			db.execSQL(FESTDB_CREATE);
		}
    }
    
    public FestDBAdapter(Context c){
    	this.mCtx = c;
    }
    
    // Opens the database
    public FestDBAdapter open() throws SQLException{
    	mDbHelper = new DatabaseHelper(mCtx);
    	FestDb = mDbHelper.getWritableDatabase();

    	return this;
    }
    
    // Closes the database
    public void close(){
    	mDbHelper.close();
    }
    
    // Adds a show to the database. Shoulda called it "addShow" but oh well?
    public long addBand(String name, Date date, int length, String venue, boolean acoustic){
    	ContentValues init = new ContentValues();
    	init.put(KEY_NAME, name);
//    	init.put(KEY_DATE, date.toString());
    	init.put(KEY_DURATION, length);
    	init.put(KEY_VENUE, venue);
//    	if(acoustic) { init.put(KEY_ACOUSTIC, 1); } else {init.put(KEY_ACOUSTIC, 0);}
    	
    	return FestDb.insert(FESTDB_TABLE, null, init);
    }
    
    // Same as previous, but with the cv already assembled
    public long addBand(ContentValues cv){
    	
    	
    	return FestDb.insert(FESTDB_TABLE, null, cv);
    }
    
    // returns all the bands playing, ordered by date of their show
    public Cursor fetchBands(){
    	return FestDb.query(FESTDB_TABLE, new String[] {KEY_NAME}, null, null, null, null, KEY_DAY);
    }
    
    // returns the entire DB, more or less
    public Cursor fetchAllShows(){
    	return FestDb.query(FESTDB_TABLE, new String[] {KEY_NAME, KEY_DAY, KEY_VENUE, KEY_TIME}, null, null, null, null, null);
    }
    
    // returns all the shows on a given day
    public Cursor fetchDateShows(int day){
    	
    	return FestDb.query(FESTDB_TABLE, new String[] {KEY_NAME, KEY_DAY, KEY_VENUE, KEY_TIME, KEY_ID}, KEY_DAY+" = '"+day+"'", null, null, null, KEY_TIME+" ASC");
    }
    
    // returns all the shows a particular band is playing
    public Cursor fetchShows(String name){
    	return FestDb.query(FESTDB_TABLE, new String[] {KEY_DAY, KEY_VENUE, KEY_TIME, KEY_ID}, KEY_NAME+"='"+name+"'", null, null, null, null);
    }
    
    // returns the shows at a given venue
    public Cursor fetchVenueShows(String venue){
    	return FestDb.query(FESTDB_TABLE, new String[] {KEY_NAME, KEY_DAY, KEY_TIME, KEY_ID}, KEY_VENUE+"='"+venue+"'", null, null, null, KEY_DAY+", " +KEY_TIME+" ASC");
    }
    
    // returns the show based on the row ID (mostly for mySchedule)
    public Cursor fetchShowById(int id){
    	return FestDb.query(FESTDB_TABLE, new String[] {KEY_NAME, KEY_DAY, KEY_VENUE, KEY_TIME, KEY_ID}, KEY_ID+"='"+id+"'", null, null, null, null);
    }
    
    // Another Armageddon method
    public void destroyDB(){
    	FestDb.execSQL("DROP TABLE fest;");
    }
    
    public void createDB(){
    	FestDb.execSQL(FESTDB_CREATE);
    }
}
