package com.wormtrader.bars;
/********************************************************************
* @(#)BarFile.java 1.00 2007
* Copyright © 2007-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* BarFile: DAO for historical data stored in bar files.
*
* @author Rick Salamone
* @version 1.00, 2007???? rts created
* 20100814 rts
* 20110516 rts reading root from properties file
* 20120328 rts added delete() which copies 1 day file to dead subfolder
* 20121012 rts thaw checks for BarSize.ONE_MIN (and if so returns 0)
* 20130218 rts modified for changes to BarSize names
* 20130218 rts decoupled file writing to admin package
* 20130225 rts added support for weekly bar size
* 20130304 rts fixed weekly bar size thaw all dates
* 20130328 rts added getClose(symbol, yyyymmdd) for MTM calculations
* 20140214 rts m5dates sorts values & streamlined dateRange() method
* 20140504 rts moved getClose(symbol, yyyymmdd) to BarList
* 20140524 rts added getD1(symbol, yyyymmdd)
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.shanebow.web.host.HostFile;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import com.shanebow.util.TextFile;
import java.io.File;
import java.io.DataInputStream;
import java.util.List;
import java.util.Vector;

public final class BarFile
	{
private static boolean DEBUG=false;
	public static final String BARFILE_ROOT_KEY="tw.bar.file.root";
	public static final int NUM_5MIN_BARS_PER_HALFDAY=42;
	public static final int NUM_5MIN_BARS_PER_FULLDAY=78;

	public static final long[] ALL_DATES = { 0, Long.MAX_VALUE };
	public static final String SLASH = "/";
	private static String ROOT_DIR;
	public static final String EXTENSION = ".bar";
	public static final Vector<M5Gaps> _gaps = new Vector<M5Gaps>(55);
	static
		{
		ROOT_DIR = SBProperties.get(BARFILE_ROOT_KEY);
		SBLog.write ( "BarFile Root: " + ROOT_DIR );
		TextFile.thaw( M5Gaps.class, gapsFilespec(), _gaps, false );
		_gaps.trimToSize();
		}

	public static String gapsFilespec() {
		return new File(ROOT_DIR, "m5gaps.csv").getPath();
		}

	public static List<M5Gaps> gaps() { return _gaps; }

	private BarFile() {}

	public static File file( String symbol, BarSize barSize, String date )
		{
		return new File(fileSpec(symbol, barSize, date));
		}

	public static final String getRoot() { return ROOT_DIR; }

	public static final String fileSpec ( String symbol, BarSize barSize, String date )
		{
		if ((symbol == null) || symbol.trim().isEmpty())
			return null;
		if ( barSize.equals(BarSize.ONE_DAY))
			return ROOT_DIR + barSize + SLASH + symbol + EXTENSION;
		if ((date == null) || (date.trim().length() != 8))
			return null;
		return ROOT_DIR + barSize + SLASH + symbol + SLASH + date + EXTENSION;
		}

	public static String[] d1Symbols() {
		return SBMisc.fileList( BarFile.path(null, BarSize.ONE_DAY), EXTENSION);
		}

	public static String[] m5Symbols() {
		return SBMisc.fileList( BarFile.path(null, BarSize.FIVE_MIN), "");
		}

	public static String[] m5Dates(String aSymbol) {
		String[] dates = SBMisc.fileList(path(aSymbol, BarSize.FIVE_MIN), EXTENSION);
		if (dates != null)
			java.util.Arrays.sort(dates);
		return dates;
		}

	public static String root() { return ROOT_DIR; }
	public static String path ( String symbol, BarSize barSize )
		{
		String it = ROOT_DIR + barSize;
		if ( barSize.isIntraday() && (symbol != null) && !symbol.trim().isEmpty())
			it += SLASH + symbol.toUpperCase();
		return it;
		}

	public static boolean exists ( String symbol, BarSize barSize, String date )
		{
		if (ROOT_DIR.startsWith("http:"))
			{
			System.out.println("skip exists on " + symbol + ", " + barSize + ", " + date);
			return true;
			}
		String filespec = fileSpec(symbol, barSize, date);
		return ( filespec == null ) ? false : (new File(filespec)).exists();
		}

	private static final DataInputStream getDataInputStream(String filespec)
		throws Exception
		{
		return HostFile.dataInputStream(filespec);
		}

	public static int[] d1IRange(String symbol) {
		long[] range = d1Range(symbol);
		if (range == null) return null;
		int[] it = { Integer.parseInt(SBDate.yyyymmdd(range[0]), 10),
		             Integer.parseInt(SBDate.yyyymmdd(range[1]), 10) };
		return it;
		}

	private static long[] d1Range(String symbol) {
		String filespec = fileSpec( symbol, BarSize.ONE_DAY, null );
		DataInputStream dis = null;
		try
			{
			long[] range = {0, 0};
			dis = getDataInputStream(filespec);
			range[0] = range[1] = dis.readLong();
			dis.skipBytes(24); // skip over OHLCV
			while ( dis.available() > 0 ) {
				range[1] = dis.readLong();
				dis.skipBytes(24); // skip over OHLCV
				}
			return range;
			}
		catch (Exception e) {
			return null;
			}
		finally {
			try { if ( dis != null ) dis.close(); }
			catch (Exception ignore) {}
			}
		}

	public static long[] dateRange( String symbol, BarSize barSize )
		{
		if ( barSize.isIntraday())
			{
			String[] dates = m5Dates(symbol);
			if (dates == null || dates.length == 0)
				return null;
			long[] range = {0, 0};
			range[0] = SBDate.toTime(dates[0] + "  09:30");
			range[1] = SBDate.toTime(dates[dates.length-1] + "  16:00");
			return range;
			}
		else return d1Range(symbol);
		}

	public static int thaw ( String symbol, BarSize barSize, long[] times, List<Bar> bars )
		{
		if ( barSize.equals(BarSize.ONE_DAY))
			{
			if ((times == null)
			||  ((times[0] == 0) && (times[1] >= SBDate.today)))
				return thaw( symbol, BarSize.ONE_DAY, (String)null, bars ); // read entire file
			else return thawD1 ( symbol, times, bars );
			}
		else if ( barSize.equals(BarSize.FIVE_MIN))
			{
/**********
			long date = startTime(times);
if (DEBUG)
SBLog.format( " range(%s - %s) start %s", hhmm(times[0]),
hhmm(times[1]), hhmm(date));
**********/
			for ( long date = startTime(times); date <= times[1]; date = SBDate.nextWeekDay(date))
				{
				thawM5 ( symbol, SBDate.yyyymmdd(date), bars );
				if (DEBUG)
					SBLog.format( "  thaw(%s,M5,%s) %s", symbol, SBDate.yyyymmdd(date), SBDate.mmddyy_hhmm(date));
				}
			return bars.size();
			}
		else if ( barSize.equals(BarSize.ONE_MIN)) return 0;
		else if ( barSize.isIntraday()) // intraday > 5 minutes
			{
			int fiveMinBarsPer = get5MinBarsPer(barSize);
			BarList fiveMinBars = new BarList();
			for ( long time = startTime(times); time <= times[1]; time = SBDate.nextWeekDay(time))
				{
				if (DEBUG)
					SBLog.format( " thaw(%s,M5,%s) - to build", symbol, SBDate.yyyymmdd(time));
				if ( thawM5 ( symbol, SBDate.yyyymmdd(time), fiveMinBars ) < 1 )
					continue;
				Bar bigBar = null;
				for ( int i = 0; i < fiveMinBars.size(); i++ )
					{
					Bar bar5 = fiveMinBars.get(i);
					if ((i % fiveMinBarsPer) == 0 )
						bars.add( bigBar = bar5 );
					else
						bigBar.adjust( bar5 );
					}
				fiveMinBars.clear();
				}
			return bars.size();
			}
		else
			{
//if (times == null) System.out.println("1w null range"); else
//System.out.println("1w range " + times[0] + " - " + times[1]);
//System.out.format( " range(%s - %s)\n", SBDate.yyyymmdd(times[0]), SBDate.yyyymmdd(times[1]));
			BarList dailyBars;
			if ((times == null)
			||  ((times[0] == 0) && (times[1] >= SBDate.today)))
				{
				dailyBars = allDailies(symbol);
				}
			else
				{
				if (times[0] != 0)
					times[0] = SBDate.startOfWeek(times[0]);
				thawD1 ( symbol, times, dailyBars = new BarList());
				}
			if (dailyBars.size() == 0) return 0;
			long cutoff = SBDate.startOfWeek(dailyBars.get(0).getTime());
			Bar weeklyBar = null;

			for ( Bar dailyBar : dailyBars )
				{
				if (dailyBar.getTime() >= cutoff)
					{
					cutoff = SBDate.addDays(cutoff, 7); // advance the cutoff date
					bars.add( weeklyBar = dailyBar );   // & start a new bar
					}
				else
					weeklyBar.adjust( dailyBar );
				}
			dailyBars.clear();
			return bars.size();
			}
		}

	public static BarList allDailies(String symbol)
		{
		BarList allDailies = new BarList();
		thaw( fileSpec(symbol, BarSize.ONE_DAY, null), allDailies);
		return allDailies;
		}

	private static long startTime( long[] times )
		{
		long date = (times[0] >= SBDate.close(times[0])) ?
			               SBDate.nextWeekDay(times[0]) : times[0];
		return SBDate.open(date);
		}

	private static String hhmm( long time )
		{
		return (time == 0)? "0" : SBDate.mmddyy_hhmm(time);
		}

	private static int get5MinBarsPer(BarSize barSize)
		{
		return barSize.duration() / BarSize.FIVE_MIN.duration();
		}

	public static int thaw ( String symbol, BarSize barSize, String date, List<Bar> bars )
		{
		return thaw(fileSpec(symbol, barSize, date), bars);
		}

	public static int thawM5 ( String symbol, String date, List<Bar> bars )
		{
		return thaw(fileSpec(symbol, BarSize.FIVE_MIN, date), bars);
		}

	/**
	* Reads the entire file into the list
	* used privately and by OrphanList
	*/
	public static int thaw ( String filespec, List<Bar> bars )
		{
		int barsLoaded = 0;
		DataInputStream dis = null;
long prev = 0;
		try
			{
			dis = getDataInputStream((filespec));
			while ( dis.available() > 0 )
				{
				long time = dis.readLong(); // 8 bytes
				int op = dis.readInt();
				int hi = dis.readInt();
				int lo = dis.readInt();
				int cl = dis.readInt();
				long vol = dis.readLong(); // 8 bytes - total 1 bar = 24 bytes/bar = 1872 bytes/day
if (time <= prev) {
SBLog.write("thaw skipped " + SBDate.yyyymmdd(time) + " in " + filespec);
continue;
}
prev = time;
				if ( bars.add ( new Bar ( time, op, hi, lo, cl, vol, 0, 0 )))
					++barsLoaded;
				}
			}
		catch (Exception e) {}
		finally { try { dis.close(); } catch (Exception ignore) {}}
		return barsLoaded;
		}

	/**
	* Reads the entire file into the list
	*/
	public static int thawD1 ( String symbol, long[] dateRange, List<Bar> bars )
		{
		String filespec = fileSpec( symbol, BarSize.ONE_DAY, null );
		int barsLoaded = 0;
		DataInputStream dis = null;
		try
			{
			dis = getDataInputStream(filespec);
			while ( dis.available() > 0 )
				{
				long time = dis.readLong();
				if ( time > dateRange[1] )
					break;
				int op = dis.readInt();
				int hi = dis.readInt();
				int lo = dis.readInt();
				int cl = dis.readInt();
				long vol = dis.readLong();
				if ( time < dateRange[0] )
					continue;
				if ( bars.add ( new Bar ( time, op, hi, lo, cl, vol, 0, 0 )))
					++barsLoaded;
				}
			dis.close();
			dis = null;
			}
		catch (Exception e)
			{
			try { if ( dis != null ) dis.close(); }
			catch (Exception ex) {}
			}
		return barsLoaded;
		}

	public static final Bar getD1(String symbol, String yyyymmdd)
		throws Exception
		{
		long time = com.shanebow.util.SBDate.toTime(yyyymmdd + "  09:30");
		long[] dateRange = { time, time };
		BarList bars = new BarList(1);
		if ( BarFile.thawD1(symbol, dateRange, bars) < 1)
			throw new Exception("No price data for " + symbol + " on " + yyyymmdd);
		return bars.get(0);
		}
	}
