package as.tra.brayden.androidtools.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import as.tra.brayden.androidtools.BuildConfig;


/**
 * Created by darcy on 2016-10-16.
 */

public abstract class OpenHelper
    extends SQLiteOpenHelper
{
    private static final String TAG = OpenHelper.class.getSimpleName();
    private static final int SCHEMA_VERSION = 1;
    public static final String URI_BASE = "content://"+ BuildConfig.APPLICATION_ID +"/";

    //private static final String DB_NAME = "categories.db";
    //public static final String NAME_TABLE_NAME = "categories";
    //private static final String ID_COLUMN_NAME = "_id";
    //public static final String NAME_COLUMN_NAME = "category";
    //private static Tours instance;

    //public static final boolean CLOSE = false;
    //public static final boolean KEEP = true;

    public enum CloseMode {
        CLOSE, KEEP
    }

    public enum SQLiteDataType {
        INTEGER ("INTEGER"),
        TEXT ("TEXT"),
        REAL ("REAL");

        private final String type;
        private SQLiteDataType(String s) {
            type = s;
        }
        public boolean equalsType(String otherType) {
            return type.equals(otherType);
        }
        public String toString() {
            return this.type;
        }
    }

    public static class ColumnDef {
        public final String name;
        public final SQLiteDataType type;
        public ColumnDef(String name, SQLiteDataType type) {
            this.name = name;
            this.type = type;
        }

        public String toString() { return this.name; }

        public String definition() {
            return this.name + " " + this.type;
        }
    }

    public class NotClosedException extends RuntimeException {
        public NotClosedException(String msg) {
            super(msg);
        }
    }



    protected final Context context;
	protected final String dbName;

    protected final String tableName;
	public final String idName;
	protected final ColumnDef[] columnDefs;
    protected final String[] columnNames;
    //protected final String[] columnTypes;
    protected final String defaultOrder;
	protected final Uri contentUri;

    protected String nameColumn;
    protected CloseMode closeMode;

    private SQLiteDatabase writeDatabase;
    private SQLiteDatabase readDatabase;

    private static final String NAME_TYPE_MISMATCH = "Error: column names doesn't match column types!";

    protected OpenHelper(@NonNull final Context ctx, CloseMode closeMode, final String dbName,
                         String tableName, String idName,
                         ColumnDef[] columnDefs,
                         String defaultOrder)
    {
        super(ctx, dbName, null, SCHEMA_VERSION);

        this.closeMode = closeMode;

        this.context = ctx;
        this.dbName = dbName;
        this.tableName = tableName;
        this.idName = idName;
        this.columnDefs = columnDefs;
        this.columnNames = new String[columnDefs.length];
        for(int i=0; i<columnDefs.length; ++i) {
            this.columnNames[i] = columnDefs[i].name; // substring(0,columnDefs[i].indexOf(" "));
        }


        this.defaultOrder = defaultOrder;
        this.contentUri = Uri.parse(URI_BASE + tableName);

        this.nameColumn = columnNames[0]; // by default, first column (def up until )
    }

    @Override
    public void onConfigure(final SQLiteDatabase db)
    {
        super.onConfigure(db);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true);
            db.setForeignKeyConstraintsEnabled(true);
        }

    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        if(db.isOpen())
            this.useDB(db);

        onCreate();

    }

    public void onCreate()
    {
        String CREATE_TABLE;

        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "  + this.tableName + " ( " +
                            this.idName   + " INTEGER PRIMARY KEY AUTOINCREMENT, ";

        for(int i = 0; i < columnDefs.length; i++) {
            CREATE_TABLE += columnDefs[i].definition();
            if(i+1 != columnDefs.length) CREATE_TABLE += ", ";
        }
        CREATE_TABLE += ")";
        Log.d(TAG, "Running SQL: "+CREATE_TABLE);
        getWritableDatabase().execSQL(CREATE_TABLE);
        //if(closeMode == CloseMode.CLOSE) close();
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db,
                          final int oldVersion,
                          final int newVersion)
    {
    }

    @Override
    public void close() {
        super.close();
        if(this.writeDatabase != null) this.writeDatabase.close();
        if(this.readDatabase != null) this.readDatabase.close();
        Log.d(TAG, this.dbName + " closed!");
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public String nameColumn() {
        return this.nameColumn;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDbName() {
        return dbName;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {

        if(writeDatabase == null || !writeDatabase.isOpen() ) writeDatabase = super.getWritableDatabase();
        return writeDatabase;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {

        //return getWritableDatabase();

        if(readDatabase == null || !readDatabase.isOpen() ) readDatabase = super.getReadableDatabase();
        return readDatabase;


    }

    public OpenHelper useDB(SQLiteDatabase db) {

        if(this.writeDatabase != null && writeDatabase.isOpen()) this.writeDatabase.close();
        if(this.readDatabase != null && readDatabase.isOpen()) this.readDatabase.close();
        writeDatabase = db;
        readDatabase = db;

        return this;
    }

    public long getNumberOfRows()
    {
        return getNumberOfRows(null, null);
    }

    public long getNumberOfRows(String selection,
                                String[] args)
    {
        final long numEntries;

        /*
        final SQLiteDatabase db = getReadableDatabase();

        this.onCreate(db);
        */

        //numEntries = DatabaseUtils.queryNumEntries(db, this.tableName);

        //if(selection == null && args == null) numEntries = DatabaseUtils.queryNumEntries(db, this.tableName);
        //else if(args == null) numEntries = DatabaseUtils.queryNumEntries(db, this.tableName, selection);
        //else
        numEntries = DatabaseUtils.queryNumEntries(getReadableDatabase(), this.tableName, selection, args);
        if(closeMode == CloseMode.CLOSE) this.close();

        return (numEntries);
    }

    public void insert(final ContentValues data, final boolean require) {
        if(require) getWritableDatabase().insertOrThrow(this.tableName, null, data);
        else getWritableDatabase().insert(this.tableName, null, data);

        if(closeMode == CloseMode.CLOSE) close();

    }

    public void insert(final ContentValues data) {
        insert(data, false);
    }
    public void insert(int id, ContentValues data)
    {
        insert(id, data, false);
    }


    public void insert(int id, ContentValues data, final boolean require)
    {
        if(id > 0) data.put("_id", ""+id);
        insert(data, require);
    }

    public void insertJSON(JSONObject object) throws JSONException, SQLiteException
    {
        ContentValues hm = new ContentValues();

        Iterator<String> columns = object.keys();
        while(columns.hasNext()) {
            String key = columns.next();

            if(this.hasColumn(key)) {
                hm.put(key, object.getString(key));
                // Log.d(TAG, "Added " + this.tableName + "." + key + "=" + object.getString(key));
            } else {
               // Log.w(TAG, "Attempted to insert JSON key " + this.tableName + "."+key+" (not found)");

            }

        }
        Log.d(TAG, "Inserting "+hm.size()+" JSON keys to "+this.tableName);
        this.insert(hm, true);

    }

    public void updateJSON(String selection, String[] selectionArgs, JSONObject object ) throws JSONException, SQLiteException {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues hm = new ContentValues();

        Iterator<String> columns = object.keys();
        while(columns.hasNext()) {
            String key = columns.next();

            if(this.hasColumn(key)) {
                hm.put(key, object.getString(key));
                ///Log.d(TAG, "Added " + this.tableName + "." + key + "=" + object.getString(key));
            } else {
               // Log.w(TAG, "Attempted to insert JSON key " + this.tableName + "."+key+" (not found)");

            }

        }
        Log.d(TAG, "Updating "+hm.size()+" JSON keys to "+this.tableName);

        db.update(this.tableName, hm, selection, selectionArgs);
        if(closeMode == CloseMode.CLOSE)
            db.close();
    }

    public void upsertJSON(String selection, String[] selectionArgs, final JSONObject data) throws JSONException, SQLiteException {
        int rows = (int)getNumberOfRows(selection, selectionArgs);
        if(rows < 1) insertJSON(data);
        else         updateJSON(selection, selectionArgs, data);
    }



    public boolean insertIfNotExists(int id, final ContentValues data)
    {
        int rows = (int)getNumberOfRows("_id = ?", new String[] {""+id});
        if(rows < 1) insert(id, data);

        return (rows < 1); // return true if inserted
    }

    public boolean insertIfNotExists(String where, String[] whereArgs,
                                     final ContentValues data)
    {
        int rows = (int)getNumberOfRows(where, whereArgs);
        if(rows < 1) insert(data);

        return (rows < 1); // return true if inserted
    }


    public void update(String selection, String[] selectionArgs, final ContentValues data) {
        SQLiteDatabase db = getWritableDatabase();

        db.update(this.tableName, data, selection, selectionArgs);
        if(closeMode == CloseMode.CLOSE)
            db.close();
    }

    public void update(int id, final ContentValues data) {

        update("_id = ?", new String[] {""+id}, data);
    }



    public void upsert(String selection, String[] selectionArgs, final ContentValues data)
    {
        int rows = (int)getNumberOfRows(selection, selectionArgs);
        if(rows < 1) insert(data);
        else         update(selection, selectionArgs, data);
    }

    public void upsert(int id, final ContentValues data)
    {
        int rows = (int)getNumberOfRows("_id = ?", new String[] {""+id});
        if(rows < 1) insert(id, data);
        else         update(id, data);
    }





    public void deleteTable() {
        SQLiteDatabase db = getWritableDatabase();

        String SQL = "DROP TABLE IF EXISTS "+this.tableName;
                Log.d(TAG, "Executing SQL: " + SQL);
        db.execSQL(SQL);
        if(closeMode == CloseMode.CLOSE)
            db.close();
    }

    public void rebuildTable() {

        this.deleteTable();
        this.onCreate();
        if(closeMode == CloseMode.CLOSE)
            close();
    }

    public void delete(final int id){
       this.delete(this.idName + " = ?", new String[] {""+id});
    }


    public int delete(final String where, final String[] args){
        SQLiteDatabase db = getWritableDatabase();
        int del = db.delete(this.tableName, where, args);
        if(closeMode == CloseMode.CLOSE)
            db.close();
        return del;
    }

    public int getId(final String where, final String[] args) throws Resources.NotFoundException {
        Cursor c = this.getRows(where, args);
        c.moveToFirst();
        try {
            if (c.getInt(0) == 0) throw new Resources.NotFoundException("Row not found!");
        } catch(CursorIndexOutOfBoundsException e) {
            throw new Resources.NotFoundException("Row not found!");
        }
        int i =  c.getInt(c.getColumnIndex(this.idName));
        if(closeMode == CloseMode.CLOSE)
            c.close();
        return i;
    }


    /**
     * Default getRow... for PK
     */
    public Cursor getRow(final int id) { return this.getRow(idName, ""+id); }
    public Cursor getRow(final long id) { return this.getRow(idName, ""+id); }

    /**
     * getRow with a colName and ID
     */
    public Cursor getRow(final String colName, final String value) {
        boolean valid = false;

        Log.d(TAG, "getRow(context, "+colName + ", " + value + ")");

        if(colName.equals(this.idName)) valid = true;
        else {
            List validCols = Arrays.asList(this.columnNames);
            if(validCols.contains(colName)) valid = true;
        }

        if(!valid) {
            Toast.makeText(context, "Invalid column selection: "+colName, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid column selection: "+colName);
            return null;
        }

        Cursor c = getRows(colName + " = ?", new String[] {""+value});
        c.moveToFirst();
        if(closeMode == CloseMode.CLOSE)
            throw new NotClosedException("Unable to autoclose OpenHelper... Cursor open!");
        return c;

    }

    /**
     * Get all rows
     */
    public Cursor getRows()
    {
        return this.getRows(null, null );

    }

    /**
     * Get rows with selection
     *
     */
    public Cursor getRows(String selection) {


        return this.getRows(selection, null );

    }

    /**
     * Get rows with selection & args
     *
     */
    public Cursor getRows(String selection, String[] selectionArgs) {


        return this.getRows(null, selection, selectionArgs, null, null, defaultOrder, null );

    }

    /**
     * Get rows with full query parameters
     *
     */
    public Cursor getRows(String[] cols,
                            String selection,
                            String[] args,
                            String group,
                            String having,
                            String order,
                            String limit)
    {
        final Cursor cursor;

        if(order == null) order = defaultOrder;

        Log.d(TAG, "getRows() selection: "+selection+" ordeR: "+defaultOrder);

        Log.d(TAG,  "Running Q: SELECT "+Arrays.toString(cols) +
                    " FROM "+tableName+
                    (selection == null ? "" : " WHERE "+selection) +
                    (group == null ? "" : " GROUP BY " + group) +
                    (having == null ? "" : " HAVING  " + having) +
                    (order == null ? "" : " ORDER BY " + order) +
                    (limit == null ? "" : " LIMIT " + limit) +
                    (args == null ? "" : "\n("+Arrays.toString(args)+")")
        );


        cursor = getReadableDatabase().query(this.tableName,
                          cols,
                          selection,     // selection, null = *
                          args,     // selection args (String[])
                          group,     // group by
                          having,     // having
                          order,     // order by
                          limit);    // limit

        //context.getContentResolver().
        //cursor.set
        if(context != null) cursor.setNotificationUri(context.getContentResolver(), this.contentUri);
        if(closeMode == CloseMode.CLOSE)
            throw new NotClosedException("Unable to autoclose OpenHelper... Cursor open!");

        return (cursor);
    }

    @NonNull
    public static String cursorCSV(Cursor cursor, CloseMode mode) {
        if(cursor.getCount() == 0) return "";
        String result = "";
        if (cursor.moveToFirst()) {
            for(int i=0; i<cursor.getColumnCount(); ++i) {
                result += cursor.getColumnName(i) + ",";
            }
            result += "\n";
            do {

                for(int i=0; i<cursor.getColumnCount();i++)
                {
                    result += cursor.getString(i) + ",";
                }
                result += "\n";

            } while (cursor.moveToNext());
        }
        if(mode == CloseMode.CLOSE) cursor.close();
        return result;
    }


    @NonNull
    public static JSONObject cursorJSONObject(@NonNull Cursor cursor, CloseMode mode) {
        if(cursor.getCount() == 0) return new JSONObject();
        if(cursor.getPosition() < 0) cursor.moveToFirst();
        JSONObject result = new JSONObject();
        for(int i=0; i<cursor.getColumnCount(); ++i) {
            try {
                result.put(cursor.getColumnName(i), cursor.getString(i));
            } catch (JSONException e) {

            }
        }
        if(mode == CloseMode.CLOSE) cursor.close();
        return result;
    }

    @NonNull
    public static JSONArray cursorJSONArray(@NonNull Cursor cursor, CloseMode mode) {
        if(cursor.getCount() == 0) return new JSONArray();
        cursor.moveToFirst();
        JSONArray result = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                result.put(cursorJSONObject(cursor, CloseMode.KEEP));
            } while (cursor.moveToNext());
        }
        if(mode == CloseMode.CLOSE) cursor.close();
        return result;
    }


    public boolean hasColumn(String column) {
        column = column.toLowerCase();
        for(int i=0; i<columnNames.length; ++i) {
            if(column.equals(columnNames[i].toLowerCase())) return true;
        }
        if(column.equals(idName.toLowerCase())) return true;
        return false;
    }

}
