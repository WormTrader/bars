package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)SymbolList.java 1.00 20090717
* Copyright © 2007-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* SymbolList: A Vector of symbols loaded from a file.
*
* @author Rick Salamone
* @version 2.00
* 20090717 rts created
* 20120328 rts rewrote to delegate io to TextFile & added saveAs()
* 20120902 rts added sort & addCSV methods
* 20140209 rts storing under usr.dir still no work network
*******************************************************/
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import com.shanebow.util.TextFile;
import java.util.Vector;

public final class SymbolList
	extends Vector<String>
	{
	public static final String FILE_EXT=".sym";

	public static final String DAILY_GRABS = "GrabDaily";
	public static final String INTRADAY_GRABS = "Grab5min";
	public static final String EXCLUDED_GRABS = "GrabExclude";

	public static String filespec(String aName) {
		String path = SBProperties.get("usr.dir") + "/lists/";
		return path + aName + FILE_EXT;
		}

	public static final String[] available()
		{
		String path = SBProperties.get("usr.dir") + "/lists/";
		return SBMisc.fileList( path, FILE_EXT );
		}

/***
	public static void exclude(String symbol)
		{
		SymbolList excludes = new SymbolList(SymbolList.EXCLUDED_GRABS);
		excludes.add(symbol);
		excludes.saveAs(SymbolList.EXCLUDED_GRABS);
		}

	public static void remove(String symbol) {
		SBLog.write(symbol + " deleted");
		String[] symbolLists = SymbolList.available();
		for ( String listName : symbolLists )
			{
			SymbolList list = new SymbolList(listName);
			if ( list.remove(symbol))
				{
				SBLog.write("..." + symbol + " removed from list " + listName);
				list.saveAs(listName);
				}
			}
		}
***/

	public SymbolList() { super(); }

	public SymbolList( String includeFile )
		{
		this();
		include( includeFile );
		trimToSize();
		}

	public SymbolList( String includeFile, String excludeFile )
		{
		this ( includeFile );
		exclude ( excludeFile );
		trimToSize();
		}

	public final void sort() { java.util.Collections.sort(this); }
	public final void saveAs( String aName )
		{
		sort();
		TextFile.freeze(this, filespec(aName), null);
		}

	public void addCSV(String aCSV)
		{
		String[] pieces = aCSV.trim().toUpperCase().split(",");
		for (String sym : pieces)
			{
			sym = sym.trim().toUpperCase();
			if ( !contains(sym))
				super.add(sym);
			}
		}

	public boolean include( String aName )
		{
		return TextFile.thaw(String.class, filespec(aName), this, false);
		}

	public boolean exclude( final String aName )
		{
		Vector<String> excludes = new Vector<String>();
		boolean it = TextFile.thaw(String.class, filespec(aName), excludes, false);
		removeAll(excludes);
		return it;
		}
	}
