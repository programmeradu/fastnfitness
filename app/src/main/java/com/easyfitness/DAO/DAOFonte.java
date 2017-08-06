package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DateGraphData;
import com.easyfitness.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOFonte extends DAOBase {

	// Contacts table name
	public static final String TABLE_NAME = "EFfontes";

	public static final String KEY = "_id";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String MACHINE = "machine";
	public static final String SERIE = "serie";
	public static final String REPETITION = "repetition";
	public static final String POIDS = "poids";
	public static final String UNIT = "unit"; // 0:kg 1:lbs
	public static final String NOTES = "notes";
	public static final String PROFIL_KEY = "profil_id";
	public static final String MACHINE_KEY = "machine_id";

	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE
			+ " DATE, " + MACHINE + " TEXT, " + SERIE + " INTEGER, "
			+ REPETITION + " INTEGER, " + POIDS + " INTEGER, " + PROFIL_KEY
			+ " INTEGER, " + UNIT + " INTEGER, " + NOTES + " TEXT, " + MACHINE_KEY + " INTEGER," + TIME + " TEXT);";
	public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
			+ TABLE_NAME + ";";
	public static final int SUM_FCT = 0;
	public static final int MAX1_FCT = 1;
	public static final int MAX5_FCT = 2;
    public static final int NBSERIE_FCT = 3;
    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + MACHINE + "," + SERIE + "," + REPETITION + "," + POIDS + "," + UNIT + "," + PROFIL_KEY + "," + NOTES + "," + MACHINE_KEY + "," + TIME;
    private Profil mProfil = null;
    private Cursor mCursor = null;
	private Context mContext = null;

	public DAOFonte(Context context) {
			super(context);
			mContext = context;
	}
	
	public void setProfil (Profil pProfil)
	{
		mProfil = pProfil;
	}

	/**
	 * @param pDate Date
	 * @param pMachine Machine name
	 * Le Record a ajouter a la base
	 */
	public long addRecord(Date pDate, String pMachine, int pSerie, int pRepetition, int pPoids, Profil pProfil, int pUnit, String pNote, String pTime) {

		ContentValues value = new ContentValues();
		long new_id = -1;
		long machine_key = -1;
		
		//Test is Machine exists. If not create it. 
		DAOMachine lDAOMachine = new DAOMachine(mContext);
		if ( ! lDAOMachine.machineExists(pMachine) )  {
			machine_key = lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_FONTE, "");
		} else {
			machine_key = lDAOMachine.getMachine(pMachine).getId();
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);

		value.put(DAOFonte.DATE, dateFormat.format(pDate));
		value.put(DAOFonte.MACHINE, pMachine);
		value.put(DAOFonte.SERIE, pSerie);
		value.put(DAOFonte.REPETITION, pRepetition);
		value.put(DAOFonte.POIDS, pPoids);
		value.put(DAOFonte.PROFIL_KEY, pProfil.getId());
		value.put(DAOFonte.UNIT, pUnit);
		value.put(DAOFonte.NOTES, pNote);
		value.put(DAOFonte.MACHINE_KEY, machine_key);
		value.put(DAOFonte.TIME, pTime);
		
		SQLiteDatabase db = open();
		new_id = db.insert(DAOFonte.TABLE_NAME, null, value);		
		close();
		
		return new_id;
	}

	
	// Getting single value
	public Fonte getRecord(long id) {
		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;
		mCursor = db.query(TABLE_NAME, new String[] { KEY, DATE, MACHINE,
				SERIE, REPETITION, POIDS, UNIT, PROFIL_KEY, NOTES, MACHINE_KEY, TIME }, KEY + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0)
			mCursor.moveToFirst();
		else 
			return null;

		Date date;
		try {
			date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor
					.getString(1));
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date();
		}
		
		//Get Profil
		DAOProfil lDAOProfil = new DAOProfil(mContext);
		Profil lProfil = lDAOProfil.getProfil(mCursor.getLong(7));

		Fonte value = new Fonte(date, mCursor.getString(2), 
				mCursor.getInt(3), 
				mCursor.getInt(4), 
				mCursor.getInt(5),
				lProfil,
				mCursor.getInt(6),
				mCursor.getString(8), 
				mCursor.getLong(9),
				mCursor.getString(10));

		value.setId(mCursor.getLong(0));
		// return value
		close();
		return value;
	}

	// Getting All Records
	private List<Fonte> getRecordsList(String pRequest) {
		List<Fonte> valueList = new ArrayList<Fonte>();
		SQLiteDatabase db = this.getReadableDatabase();
		// Select All Query
		String selectQuery = pRequest;

		mCursor = null;
		mCursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (mCursor.moveToFirst() && mCursor.getCount()>0) {
			do {
				//Get Date
				Date date;
				try {
					date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
							.parse(mCursor.getString(1));
				} catch (ParseException e) {

					e.printStackTrace();
					date = new Date();
				}
				
				//Get Profil
				DAOProfil lDAOProfil = new DAOProfil(mContext);
				Profil lProfil = lDAOProfil.getProfil(mCursor.getLong(7));
				
				long machine_key = -1;
				
				//Test is Machine exists. If not create it. 
				DAOMachine lDAOMachine = new DAOMachine(mContext);
				if (mCursor.getString(9)==null)  {
					machine_key = lDAOMachine.addMachine(mCursor.getString(2), "", DAOMachine.TYPE_FONTE, "");
				} else {
					machine_key = mCursor.getLong(9);
				}
				
				Fonte value = new Fonte(date, mCursor.getString(2), 
						mCursor.getInt(3), 
						mCursor.getInt(4), 
						mCursor.getInt(5), 
						lProfil,
						mCursor.getInt(6),
						mCursor.getString(8),
						machine_key,
						mCursor.getString(10));
				
				value.setId(mCursor.getLong(0));

				// Adding value to list
				valueList.add(value);
			} while (mCursor.moveToNext());
		}
		// return value list
		return valueList;
	}

	public Cursor GetCursor() {
		return mCursor;
	}

	// Getting All Records
	public List<Fonte> getAllRecords() {
		// Select All Query
		String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " ORDER BY "
				+ DATE + " DESC," + KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}
	
	// Getting All Records
	public List<Fonte> getAllRecordsByProfil(Profil pProfil) {
		return getAllRecordsByProfil(pProfil, -1);
	}
	
	public List<Fonte> getAllRecordsByProfil(Profil pProfil, int pNbRecords) {
		String mTop;
		if (pNbRecords == -1) mTop = "";
		else mTop = " LIMIT " + pNbRecords;
		 
		
		// Select All Query
		String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME + 
				" WHERE " + PROFIL_KEY + "=" + pProfil.getId() + 
				" ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop ;

		// Return value list
		return getRecordsList(selectQuery);
	}

	// Getting Filtered records
	public List<Fonte> getFilteredRecords(Profil pProfil, String pMachine, String pDate) {

		boolean lfilterMachine = true;
		boolean lfilterDate = true;
		String selectQuery = null;

		if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString()))
		{
			lfilterMachine = false;
		}

		if (pDate == null || pDate.isEmpty() || pDate.equals(mContext.getResources().getText(R.string.all).toString())) 
		{
			lfilterDate = false;
		}

		if (lfilterMachine && lfilterDate) {
			selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME 
					+ " WHERE " + MACHINE + "=\"" + pMachine 
					+ "\" AND " + DATE + "=\"" + pDate 
					+ "\" AND " + PROFIL_KEY + "=" + pProfil.getId() 
					+ " ORDER BY " + DATE + " DESC," + KEY + " DESC";
		} else if (!lfilterMachine && lfilterDate) {
			selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME 
					+ " WHERE " + DATE + "=\"" + pDate 
					+ "\" AND " + PROFIL_KEY + "=" + pProfil.getId()
					+ " ORDER BY " + DATE + " DESC," + KEY + " DESC";
		} else if (lfilterMachine && !lfilterDate) {
			selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME 
					+ " WHERE " + MACHINE	+ "=\"" + pMachine 
					+ "\" AND " + PROFIL_KEY + "=" + pProfil.getId()
					+ " ORDER BY " + DATE + " DESC," + KEY + " DESC";
		} else if (!lfilterMachine && !lfilterDate) {
			selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME 
					+ " WHERE " + PROFIL_KEY + "=" + pProfil.getId()
					+ " ORDER BY " + DATE + " DESC," + KEY + " DESC";
		}

		// return value list
		return getRecordsList(selectQuery);
	}

	// Getting Function records
	public List<DateGraphData> getFunctionRecords(Profil pProfil, String pMachine,
			int pFunction) {

		String selectQuery = null;

		// TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
		if (pFunction == DAOFonte.SUM_FCT) {
			selectQuery = "SELECT SUM(" + SERIE + "*" + REPETITION + "*"
					+ POIDS + "), " + DATE + " FROM " + TABLE_NAME 
					+ " WHERE "	+ MACHINE + "=\"" + pMachine + "\"" 
					+ " AND " + PROFIL_KEY + "=" + pProfil.getId()
					+ " GROUP BY " + DATE
					+ " ORDER BY date(" + DATE + ") ASC";
		} else if (pFunction == DAOFonte.MAX5_FCT) {
			selectQuery = "SELECT MAX(" + POIDS + ") , " + DATE + " FROM "
					+ TABLE_NAME 
					+ " WHERE " + MACHINE + "=\"" + pMachine + "\""
					+ " AND " + REPETITION + ">=5" 
					+ " AND " + PROFIL_KEY + "=" + pProfil.getId()
					+ " GROUP BY " + DATE 
					+ " ORDER BY date(" + DATE	+ ") ASC";
		} else if (pFunction == DAOFonte.MAX1_FCT) {
			selectQuery = "SELECT MAX(" + POIDS + ") , " + DATE + " FROM "
					+ TABLE_NAME 
					+ " WHERE " + MACHINE + "=\"" + pMachine + "\""
					+ " AND " + REPETITION + ">=1" 
					+ " AND " + PROFIL_KEY + "=" + pProfil.getId()
					+ " GROUP BY " + DATE 
					+ " ORDER BY date(" + DATE	+ ") ASC";
        } else if (pFunction == DAOFonte.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + MACHINE + "=\"" + pMachine + "\""
                    + " AND " + PROFIL_KEY + "=" + pProfil.getId()
                    + " GROUP BY " + DATE
                    + " ORDER BY date(" + DATE + ") ASC";
        }
        // case "MEAN" : selectQuery = "SELECT SUM("+ SERIE + "*" + REPETITION +
		// "*" + POIDS +") FROM " + TABLE_NAME + " WHERE " + MACHINE + "=\"" +
		// pMachine + "\" AND " + DATE + "=\"" + pDate + "\" ORDER BY " + KEY +
		// " DESC";
		// break;

		// Formation de tableau de valeur
		List<DateGraphData> valueList = new ArrayList<DateGraphData>();
		SQLiteDatabase db = this.getReadableDatabase();
		
		mCursor = null;
		mCursor = db.rawQuery(selectQuery, null);

		double i = 0;

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			do {
				Date date;
				try {
					date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
							.parse(mCursor.getString(1));
				} catch (ParseException e) {
					e.printStackTrace();
					date = new Date();
				}

				DateGraphData value = new DateGraphData(date.getTime(), mCursor.getDouble(0));

				// Adding value to list
				valueList.add(value);
			} while (mCursor.moveToNext());
		}

		// return value list
		return valueList;
	}
	
	// Getting All Machines
	public String[] getAllMachines(Profil pProfil) {
		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;

		// Select All Machines
		//String selectQuery = "SELECT DISTINCT  " + DAOMachine.TABLE_NAME +'.' + DAOMachine.NAME + " FROM "
		///		+ TABLE_NAME  + " INNER JOIN " + DAOMachine.TABLE_NAME + " ON " + DAOMachine.TABLE_NAME + '.' + DAOMachine.KEY + "=" + DAOFonte.TABLE_NAME + '.' + DAOFonte.MACHINE_KEY + " WHERE " + PROFIL_KEY + "=" + pProfil.getId() + " ORDER BY " + DAOMachine.NAME + " ASC";
		String selectQuery = "SELECT DISTINCT " + MACHINE + " FROM "
				+ TABLE_NAME + "  WHERE " + PROFIL_KEY + "=" + pProfil.getId() + " ORDER BY " + MACHINE + " ASC";
		mCursor = db.rawQuery(selectQuery, null);

		int size = mCursor.getCount();

		String[] valueList = new String[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			int i = 0;
			do {
				String value = new String(mCursor.getString(0));
				valueList[i] = value;
				i++;
			} while (mCursor.moveToNext());
		}
		close();
		// return value list
		return valueList;
	}

	// Getting All Machines
	public String[] getAllMachines() {
		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;

		// Select All Machines
		String selectQuery = "SELECT DISTINCT  " + MACHINE + " FROM "
				+ TABLE_NAME + " ORDER BY " + MACHINE + " ASC";
		mCursor = db.rawQuery(selectQuery, null);

		int size = mCursor.getCount();

		String[] valueList = new String[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			int i = 0;
			do {
				String value = new String(mCursor.getString(0));
				valueList[i] = value;
				i++;
			} while (mCursor.moveToNext());
		}
		close();
		// return value list
		return valueList;
	}

	// Getting All Dates
	public Date[] getAllDates(Profil pProfil) {
		
		SQLiteDatabase db = this.getReadableDatabase();

		mCursor = null;

		// Select All Machines
		String selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME
				+ " WHERE " + PROFIL_KEY + "=" + pProfil.getId()
				+ " ORDER BY " + DATE + " DESC";
		mCursor = db.rawQuery(selectQuery, null);
		int size = mCursor.getCount();

		Date[] valueList = new Date[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			do {
				int i = 0;
				Date date;
				try {
					date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
							.parse(mCursor.getString(1));
				} catch (ParseException e) {
					e.printStackTrace();
					date = new Date();
				}
				valueList[i] = date;
				i++;
			} while (mCursor.moveToNext());
		}
		
		close();

		// return value list
		return valueList;
	}

	// Getting All Dates
	public String[] getAllDatesAsString(Profil pProfil) {

		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;

		// Select All Machines
		String selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME
				+ " WHERE " + PROFIL_KEY + "=" + pProfil.getId()
				+ " ORDER BY " + DATE + " DESC";
		mCursor = db.rawQuery(selectQuery, null);

		int size = mCursor.getCount();

		String[] valueList = new String[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			int i = 0;
			do {
				String value = new String(mCursor.getString(0));
				valueList[i] = value;
				i++;
			} while (mCursor.moveToNext());
		}
		
		close();

		// return value list
		return valueList;
	}

	// Get all record for one Machine
	public List<Fonte> getAllRecordByMachines(Profil pProfil, String pMachines) {
		return getAllRecordByMachines(pProfil, pMachines, -1);
	}
	
	public List<Fonte> getAllRecordByMachines(Profil pProfil, String pMachines, int pNbRecords) {
		String mTop;
		if (pNbRecords == -1) mTop = "";
		else mTop = " LIMIT " + pNbRecords;
		
		// Select All Query
		String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME 
				+ " WHERE " + MACHINE + "=\"" + pMachines + "\""
				+ " AND " + PROFIL_KEY + "=" + pProfil.getId()
				+ " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;

		// return value list
		return getRecordsList(selectQuery);
	}

	// Get all record for one Date
	public List<Fonte> getAllRecordByDate(Profil pProfil, Date pDate) {
		// return value list
		return getAllRecordByDate(pProfil, pDate.toString());
	}

	// Get all record for one Date
	public List<Fonte> getAllRecordByDate(Profil pProfil, String pDate) {
		// Select All Query
		String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME 
				+ " WHERE " + DATE + "=\"" + pDate + "\"" 
				+ " AND " + PROFIL_KEY + "=" + pProfil.getId()
				+ " ORDER BY " + DATE + " DESC," + KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}

	// Getting last record
	public Fonte getLastRecord(Profil pProfil) {

		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;
		Fonte lReturn = null;

		// Select All Machines
		String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME 
				+ " WHERE " + PROFIL_KEY + "=" + pProfil.getId();
		mCursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		mCursor.moveToFirst();
		try {
			long value = mCursor.getLong(0);
			lReturn = this.getRecord(value);
		} catch (NumberFormatException e) {
			//Date date = new Date();
			lReturn = null; // Return une valeur
		}
		
		close();

		// return value list
		return lReturn;
	}

	// Updating single value
	public int updateRecord(Fonte m) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues value = new ContentValues();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
		value.put(DAOFonte.DATE, dateFormat.format(m.getDate()));		
		value.put(DAOFonte.MACHINE, m.getMachine());
		value.put(DAOFonte.SERIE, m.getSerie());
		value.put(DAOFonte.REPETITION, m.getRepetition());
		value.put(DAOFonte.POIDS, m.getPoids());
		value.put(DAOFonte.UNIT, m.getUnit());
		value.put(DAOFonte.NOTES, m.getNote());		
		value.put(DAOFonte.PROFIL_KEY, m.getProfilKey());

		// updating row
		return db.update(TABLE_NAME, value, KEY + " = ?",
				new String[] { String.valueOf(m.getId()) });
	}

	// Deleting single Record
	public void deleteRecord(Fonte m) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY + " = ?",
				new String[] { String.valueOf(m.getId()) });
		db.close();
	}

	// Deleting single Record
	public void deleteRecord(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY + " = ?", new String[] { String.valueOf(id) });
		db.close();
	}

    // Getting Profils Count
    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        	
        int value = cursor.getCount();
        cursor.close();
        close();
        
        // return count
        return value;
    }
    
    
    public void closeCursor() {
    	if ( mCursor!=null) mCursor.close();
    }
    
    public void closeAll() {
    	if ( mCursor!=null) mCursor.close();
        close();
    }

	public void populate() {
		// DBORecord(long id, Date pDate, String pMachine, int pSerie, int
		// pRepetition, int pPoids)
		Date date = new Date();
		int poids = 10;

		for (int i = 1; i <= 5; i++) {
			String machine = "Biceps";
			date.setDate(date.getDay() + i * 10);
			addRecord(date, machine, i * 2, 10 + i, poids * i, mProfil, 0, "", "12:34:56");
		}

		date = new Date();
		poids = 12;

		for (int i = 1; i <= 5; i++) {
			String machine = "Dev Couche";
			date.setDate(date.getDay() + i * 10);
			addRecord(date, machine, i * 2, 10 + i, poids * i, mProfil, 0, "", "12:34:56");
		}
	}

}
