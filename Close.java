package com.wormtrader.bars;
/********************************************************************
* @(#)Close.java 1.01 20071201
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Close: Market Data Price At a given time. This class is extended
* by Bar to provide open, high, low, and volume numbers in addition
* to just the close.
*
* @version 1.0 11/15/07
* @author Rick Salamone
*
* 20120519 rts added setTime() for fixing inconsistencies
* 20130407 rts added dow() to support queries
*******************************************************/
import com.shanebow.util.SBDate;

public class Close
	{
	protected long m_time;			// seconds since 1/1/1970 GMT
	public int  m_close;

	public Close ( long time, int cents )
		{
		m_time = time;		// seconds since 1/1/1970 GMT
		m_close = cents;
		}

	protected Close() { m_time = 0; }

	public final int    getPrice()  { return m_close;  }
	public final void   setPrice(int x)  { m_close = x;  }
	public final long   getTime()   { return m_time; }
	public final String getDate()   { return SBDate.toDate(m_time); }

	public final void   setTime(long aTime)  { m_time = aTime;  }
	public final String hhmm() { return SBDate.hhmm(m_time); }
	public final String yyyymmdd() { return SBDate.yyyymmdd(m_time); }
	/** Sunday == 1, Saturday == 7 */
	public final int   dow() { return SBDate.dayOfWeek(m_time); }

	public String toString ( int dateFormat )
		{
		return 	SBDate.format(m_time, dateFormat ) + ", " + m_close;
		}

	public String toString ()
		{
		return 	SBDate.toDate(m_time) + ", " + m_close;
		}
	}
