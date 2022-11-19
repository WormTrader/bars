package com.wormtrader.bars;
/********************************************************************
* @(#)BarSize.java	1.00 20091021
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* BarSize: Represents the period of a set of bars.
*
* @version 1.00
* @author Rick Salamone
* 20091021 rts created
* 20120322 rts added parse() to support saving configurations
* 20120801 rts added find() to better support saving configurations
* 20121109 rts added backfill choices for sizes < 5 min == 1 day only
* 20130218 rts added friendly names in addion to ib names
* 20130225 rts increased max supported size to weekly
*******************************************************/
public final class BarSize
	implements Comparable<BarSize>
	{
//	public static final BarSize ONE_SEC     = new BarSize( 1, "S1",  "1 sec",    0, 1 );
	public static final BarSize FIVE_SEC    = new BarSize( 2, "S5",  "5 secs",   0, 5 );
//	public static final BarSize FIFTEEN_SEC = new BarSize( 3, "S15", "15 secs",  0, 15 );
//	public static final BarSize THIRTY_SEC  = new BarSize( 4, "S30", "30 secs",  0, 30 );
	public static final BarSize ONE_MIN     = new BarSize( 5, "M1",  "1 min",    0, 60 );
//	public static final BarSize TWO_MIN     = new BarSize( 6, "M2",  "2 mins",   1, 120 );
	public static final BarSize FIVE_MIN    = new BarSize( 7, "M5",  "5 mins",   1, 300 );
	public static final BarSize FIFTEEN_MIN = new BarSize( 8, "M15", "15 mins",  2, 900 );
	public static final BarSize THIRTY_MIN  = new BarSize( 9, "M30", "30 mins",  2, 1800 );
	public static final BarSize ONE_HOUR    = new BarSize(10, "H1",  "1 hour",   2, 3600 );
	public static final BarSize ONE_DAY     = new BarSize(11, "D1",  "1 day",    3, 86400 ); //24 * 60 * 60
	public static final BarSize ONE_WEEK    = new BarSize(12, "W1",  "1 week",   4, 604800 );
//	public static final BarSize ONE_MON     = new BarSize(13, "W4",  "1 month",  4, 2592000 );
//	public static final BarSize THREE_MON   = new BarSize(14, "W13", "3 months", 4, 7889400 );
//	public static final BarSize ONE_YEAR    = new BarSize(15, "Y1",  "1 year",	   4, 31557600 );

	public static final BarSize DEFAULT = BarSize.FIVE_MIN;
	public static final BarSize MINIMUM = BarSize.FIVE_SEC;
	public static final BarSize MAXIMUM = BarSize.ONE_WEEK;

	private static BarSize[] ALL = { FIVE_SEC, ONE_MIN, FIVE_MIN, FIFTEEN_MIN,
	                                 THIRTY_MIN, ONE_HOUR, ONE_DAY, ONE_WEEK };

	static final String[][] BACKFILL_CHOICES =
		{
			{"1 Day"},
			{"1 Day", "2 Days", "3 Days", "4 Days", "5 Days"},
			{"3 Days", "1 Week", "2 Weeks", "3 Weeks", "4 Weeks"},
			{"1 Month", "6 Weeks", "3 Months", "4 Months", "6 Months", "9 Months",
		               "1 Year", "2 Years", "3 Years", "All dates" },
			{"1 Year", "2 Years", "3 Years", "5 years", "All dates" },
		};

	public static BarSize parse(String aSizeString)
		{
		for ( BarSize size : ALL )
			if ( size.m_string.startsWith(aSizeString))
				return size;
		return null;
		}

	public static BarSize find(int aParamValue)
		{
		for ( BarSize size : ALL )
			if ( size.m_paramValue == aParamValue)
				return size;
		return null;
		}

	public final int duration() { return m_duration; }
	public final int paramValue() { return m_paramValue; }
	public final String toString() { return m_string; }
	public final String ibString() { return m_ibstring; }
	public final int compareTo( BarSize other ) { return this.duration() - other.duration(); }
	public final boolean equals( BarSize other ){ return this.duration() == other.duration(); }
	public final boolean isIntraday() { return compareTo(ONE_DAY) < 0; }
	public final String[] backfills() { return BACKFILL_CHOICES[m_backfills]; }

	/****************** Private Implementation **************************/
	private final String m_string;     // friendly name
	private final String m_ibstring;   // specified by InteractiveBrokers
	private final int    m_paramValue; // specified by InteractiveBrokers
	private final int    m_duration;   // in seconds from open to next open
	private final int    m_backfills;  // index into BACKFILL_CHOICES
	private BarSize( int paramValue, String s, String ib, int backfills, int duration )
		{
		m_paramValue = paramValue;
		m_string = s;
		m_ibstring = ib;
		m_backfills = backfills;
		m_duration = duration;
		}
	}
