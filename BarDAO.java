package com.wormtrader.bars;
/********************************************************************
* @(#)BarDAO.java 1.00 20140208
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* BarDAO: An interface that defines IO methods to retrieve historical bars.
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
import com.shanebow.util.SBLog;
import java.util.List;

public abstract class BarDAO
	{
	public static final int NUM_5MIN_BARS_PER_HALFDAY=42;
	public static final int NUM_5MIN_BARS_PER_FULLDAY=78;
	public static final long[] ALL_DATES = { 0, Long.MAX_VALUE };
	public static final String SLASH = "/";

	public static int get5MinBarsPer(BarSize barSize)
		{
		return barSize.duration() / BarSize.FIVE_MIN.duration();
		}

	public abstract List<String[]> ranges() throws Exception;
	public abstract long[] dateRange( String symbol, BarSize barSize );
	public abstract String[] m5Dates(String aSymbol);
	public abstract boolean m5Exists ( String symbol, String date );
	public abstract String[] m5Symbols();

	public abstract int thaw ( String symbol, BarSize barSize, long[] times, List<Bar> bars );
	public abstract int thawM5 ( String symbol, String date, List<Bar> bars );
	public abstract int thawD1 ( String symbol, long[] dateRange, List<Bar> bars );

	// Logging support
	private final String MODULE = getClass().getSimpleName();
	protected final void log( String fmt, Object... args )
		{
		SBLog.write( MODULE, String.format(fmt, args));
		}
	}
