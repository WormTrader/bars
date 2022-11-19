package com.wormtrader.bars;
/********************************************************************
* @(#)BarFileDAO.java 1.00 20140208
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* BarFileDAO: An interface that defines IO methods to retrieve historical bars.
* Concrete implementations should be written to access data directly from
* the database, over the network, or via a file.
*
* @author Rick Salamone
* @version 1.00
* 20140208 rts created
* 20140504 rts added ranges()
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarSize;
import com.shanebow.util.SBDate;
import java.util.List;
import java.util.Vector;

public final class BarFileDAO
	extends BarDAO
	{
	public final long[] dateRange(String symbol, BarSize barSize) {
		return BarFile.dateRange(symbol, barSize);
		}

	public final String[] m5Dates(String aSymbol) {
		return BarFile.m5Dates(aSymbol);
		}

	public final List<String[]> ranges()
		{
		String path = BarFile.path( null, BarSize.ONE_DAY );
		String[] fileList = com.shanebow.util.SBMisc.fileList( path, BarFile.EXTENSION );
		Vector<String[]> it = new Vector<String[]>(fileList.length);

		for (String sym : fileList) {
			String[] rec = new String[5];
			rec[0] = sym;
			long[] range = BarFile.dateRange( sym, BarSize.ONE_DAY );
			rec[1] = SBDate.yyyymmdd(range[0]);
			rec[2] = SBDate.yyyymmdd(range[1]);
			range = BarFile.dateRange( sym, BarSize.FIVE_MIN );
			if (range == null)
				rec[3] = rec[4] = "";
			else {
				rec[3] = SBDate.yyyymmdd(range[0]);
				rec[4] = SBDate.yyyymmdd(range[1]);
				}
			it.add(rec);
			}
		return it;
		}

	public final boolean m5Exists (String symbol, String date) {
		return BarFile.exists (symbol, BarSize.FIVE_MIN, date);
		}

	public final String[] m5Symbols() {
		return BarFile.m5Symbols();
		}

	public final int thaw (String symbol, BarSize barSize, long[] times, List<Bar> bars) {
		return BarFile.thaw(symbol, barSize, times, bars);
		}

	public final int thawM5 (String symbol, String date, List<Bar> bars) {
		return BarFile.thawM5 (symbol, date, bars);
		}

	public final int thawD1 (String symbol, long[] dateRange, List<Bar> bars) {
		return BarFile.thawD1 (symbol, dateRange, bars);
		}

	@Override public String toString() {
		return "BarFileDAO: " + BarFile.getRoot();
		}
	}
