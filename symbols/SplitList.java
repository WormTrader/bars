package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)SplitList.java 1.00 20130308
* Copyright © 2013-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* SplitList: Represents a list of stock splits. Has methods for searching
* for a Split and persisting the list.
*
* @author Rick Salamone
* @version 2.00
* 20130315 rts created
* 20130604 rts data file stored in history root (either web or local)
*******************************************************/
import com.shanebow.web.host.HostFile;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import com.shanebow.util.TextFile;
import javax.swing.JTable;
import javax.swing.table.*;
import java.util.List;
import java.util.Vector;

public final class SplitList
	extends AbstractTableModel
	{
	public static final int COL_DATE = 0;
	public static final int COL_SYMBOL = 1;
	public static final int COL_RATIO = 2;
	private static final String MASTER="splits.csv";
	private static final Vector<Split> _master = new Vector<Split>(200);
	private static String filespec(String fname)
		{
		String dir = SBProperties.get("tw.bar.file.root");
		return new java.io.File(dir, fname).getPath();
		}

	static // public int load( String filespec )
		{
		String filespec = filespec(MASTER);
		try
			{
			log ("Loading splits from " + filespec);
			TextFile.thaw( Split.class, HostFile.bufferedReader(filespec), _master, false );
			_master.trimToSize();
			log ( "loaded: %d splits", _master.size());
			}
		catch (Exception e)
			{
			System.err.println(filespec + " Error: " + e.toString());
			}
		}

	static final String[] columnNames = { "Date", "Symbol", "Ratio" };

	private static final int[] colWidths = { 40, 30, 30 };
	private void initColumns( JTable table )
		{
		for ( int c = getColumnCount(); c-- > 0; )
			{
			TableColumn column = table.getColumnModel().getColumn(c);
			column.setPreferredWidth(colWidths[c]);
			}
		}

	public int getColumnCount() { return columnNames.length; }
	public String  getColumnName(int c) { return columnNames[c]; }
	public int getRowCount() { return _master.size(); }
	public boolean isCellEditable(int r, int c) { return false; }

	public Object getValueAt( int r, int c )
		{
		Split split = _master.get(r);
		switch (c)
			{
			case COL_DATE:   return split.ymd();
			case COL_SYMBOL: return split.symbol();
			case COL_RATIO:  return split.ratio();
			}
		return null;
		}

	public static List<Split> list() { return _master; }

	protected static void log(String fmt, Object... args) {
		com.shanebow.util.SBLog.format(fmt, args); }
	}
