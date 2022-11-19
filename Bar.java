package com.wormtrader.bars;
/********************************************************************
* @(#)Bar.java 1.01 20071115
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* Bar.java: A market data bar, consisting of open, high, low, and
* close price for an arbitrary BarSize along with the volume of
* trades.
*
* The prices are represented internally as cents (thus avoiding
* decimals and the associated vagarities of floating point storage
* and calculations). The volumes are represented in 1000's, a
* decision I regret!
* 
* @author Rick Salamone
* @version 1.0 20071115
* @version 1.1 20071201
*    added toString() & Bar(String) to facilitate file IO
* @version 1.2 20081102
*    added Accumulation Distribution calculations
*    added Intrabar Intensity calculations
* @version 2.0 20100110
*    Changed adjust methods to leave the time as the original
*    time bar time is the beginning of the bar to be consistent
*    with historical intraday data.
*    Also removed constructors accepting a String date to ensure
*    more preceise representation of each bar's time.
* @version 2.1 20110628
*    added range() method which simply returns high minus low
* @version 2.2 20120422
*    added methods second(), secondLast(), midpoint(), and
*    agvOpenClose(). These prices are sent to the simulators
*    between the opening & closing prices to simulate the market.
* @version 2.3 20120501
*    added method ohlcHTML() for more readable graph tool tip
* @version 2.4 20120519
*    added static & class methods isConsistent() for validation
* 20120723 rts added mfi()
* 20120905 rts added isUp(), isDown, isDoji()
* 20121115 rts added isInside(Bar prev)
* 20120309 rts added split()
* 20130326 rts added mfiType()
* 20130510 rts added moneyFlow()
*******************************************************/
import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;

public final class Bar extends Close
	{
	// price field choices
	public static final char PRICE_OPEN = 'O';
	public static final char PRICE_HIGH = 'H';
	public static final char PRICE_LOW = 'L';
	public static final char PRICE_CLOSE = 'C';
	public static final char PRICE_TYPICAL = 'T';
	public static final char PRICE_MID = 'M';
	public static final String[] PRICE_FIELDS =
		{
		"Opens",    // PRICE_OPEN
		"Highs",    // PRICE_HIGH
		"Lows",     // PRICE_LOW
		"Closes",   // PRICE_CLOSE
		"Typicals", // PRICE_TYPICAL
		"Midpoints", // PRICE_MID
		};

	public static final int NUM_5MIN_BARS_PER_DAY=78;

//	private volatile long	m_time;			// seconds since 1/1/1970 GMT
	private int		m_open;	
	private int		m_high;			// All prices are in cents
	private int		m_low;
//	private int		m_close;
	private long	m_volume;
	private int		m_count;		// when TRADES historical data is
												// returned, "count" represents the
												// number of trades that occurred
												// during the bar's time period
	private int		m_WAP;			// Weighted Average Price

	public Bar ( long time, int open, int high, int low, int close,
								long volume, int count, int WAP )
		{
		super( time, close );
		m_open = open;
		m_high = high;
		m_low = low;
		m_volume = volume;
		m_count = count;
		m_WAP = WAP;			// Weighted Average Price
		}

	public Bar ( long time, Bar smallerBar )  // when we need to build say
		{                           //  a 15 min barfrom 5 minute bars
		super( time, smallerBar.m_close );
		m_open = smallerBar.m_open;
		m_high = smallerBar.m_high;
		m_low = smallerBar.m_low;
		m_volume = smallerBar.m_volume;
		m_count = smallerBar.m_count;
		m_WAP = smallerBar.m_WAP;			// Weighted Average Price
		}

	public void adjust ( long time, int high, int low, int close,
															long volume, int count )
		{
		if ( high > m_high )	m_high = high;
		if (  low < m_low  ) m_low = low;
		m_WAP = (m_high + m_low) / 2;
		m_close = close;
		m_volume += volume;
		m_count += count;
		}

	public void adjust ( Bar smallerBar )
		{
		if ( smallerBar.m_high > m_high )	m_high = smallerBar.m_high;
		if (  smallerBar.m_low < m_low  ) m_low = smallerBar.m_low;
		m_WAP = (m_high + m_low) / 2;
		m_close = smallerBar.m_close;
		m_volume += smallerBar.m_volume;
		m_count += smallerBar.m_count;
		}

	/**
	* split() Scales the prices int the bar by the split amount. For
	* instance call split(2,1) when a 2 for 1 stock split halves the
	* price, call split(1,2) when a 1 for 2 split doubles the price.
	*/
	public final void split(int denominator, int numerator)
		{
		m_open  = _split(m_open,  denominator, numerator);
		m_high  = _split(m_high,  denominator, numerator);
		m_low   = _split(m_low,   denominator, numerator);
		m_close = _split(m_close, denominator, numerator);
		}

	/**
	* Helper method for split() that splits an individual price and
	* handles rounding.
	*/
	private int _split(int cents, int denominator, int numerator)
		{
		return ((10 * cents + 5) * numerator) / (10 * denominator);
		}

	public final int getPrice ( char field )
		{
		switch ( field )
			{
			default:
			case PRICE_OPEN:    return m_open;
			case PRICE_HIGH:    return m_high;
			case PRICE_LOW:     return m_low;
			case PRICE_CLOSE:   return m_close;
			case PRICE_TYPICAL: return typicalPrice();
			case PRICE_MID:     return midpoint();
			}
		}

	public final int     getOpen()   { return m_open;   }
	public final int     getHigh()   { return m_high;   }
	public final int     getLow()    { return m_low;    }
	public final int     getClose()  { return m_close;  }
	public final long    getVolume() { return m_volume; }
	public final int     getVolK()   { return (int)(m_volume/1000); }
	public final int     getCount()  { return m_count;  }
	public final int     getWAP()    { return m_WAP;    }

	public final boolean isUp()      { return m_close > m_open; }
	public final boolean isDown()    { return m_open > m_close; }
	public final boolean isDoji()    { return m_open == m_close; }

	public final void   setOpen(int x)   { m_open = x;   }
	public final void   setHigh(int x)   { m_high = x;   }
	public final void   setLow(int x)    { m_low = x;    }
	public final void   setClose(int x)  { m_close = x;  }
	public final void   setVolume(long x){ m_volume = x; }
	public final void   setCount(int x)  { m_count = x;  }
	public final void   setWAP(int x)    { m_WAP = x;    }

	public final String toString ( int dateFormat )
		{
		return 	SBDate.format(m_time, dateFormat )
						+ ": " + ohlcString() + ", " + m_volume;
		}

	public final String toString()
		{
		return 	SBDate.toDate(m_time)
		+ ", " + m_open
		+ ", " + m_high
		+ ", " + m_low
		+ ", " + m_close
		+ ", " + m_volume;
		}

	public final String ohlcString()
		{
		return SBFormat.toDollarString(m_open)
				+ ", " + SBFormat.toDollarString(m_high)
				+ ", " + SBFormat.toDollarString(m_low)
				+ ", " + SBFormat.toDollarString(m_close);
		}

	public final String ohlcHTML()
		{
		String openString = SBFormat.toDollarString(m_open);
		return SBFormat.toDollarString(m_high) + "<br>"
		     + ((m_open == m_close)? openString
		        : "<font color=" + ((m_open > m_close)?"RED":"GREEN") + ">"
			      + openString + " - " + SBFormat.toDollarString(m_close) + "</font>")
		     + "<br>" + SBFormat.toDollarString(m_low);
		}

	// Technical Indicators
	public final int change() { return m_close - m_open; }
	public final int range() { return m_high - m_low; }
	/**
	* srange is short for signed range
	*/
	public final int srange()
		{
		return (m_close < m_open)? m_low - m_high : m_high - m_low;
		}

	public final double percentChange()
		{ return 100 * (double)change() / (double)m_open; }

	public final int trueRange( int prevClose )
		{
		int maxHigh = (prevClose > m_high)? prevClose : m_high;
		int minLow = (prevClose < m_low)? prevClose : m_low;
		return maxHigh - minLow;
		}

	/**
	* Used to identify an inside bar which has a higher low than the previous
	* bar's low and a lower high than the previous day's high
	*/
	public final boolean isInside( Bar prev )
		{
		return this.m_high < prev.m_high
		    && this.m_low  > prev.m_low;
		}

	/**
	* Market Facilitation Index (Bill Williams)
	*/
	public final int mfi()
		{
		return (m_volume==0)? 0 : (int)(100000L * (m_high - m_low) / m_volume);
		}

	/**
	* mfiType: Compares the bar's mfi and volume to that of the
	* previous bar and returns FADE, FAKE, SQUAT, or GREEN
	*/
	public static final byte FADE=0;
	public static final byte FAKE=1;
	public static final byte SQUAT=2;
	public static final byte GREEN=3;
	public final byte mfiType(Bar prevBar)
		{
		byte flags = 0;
		if (mfi() > prevBar.mfi()) flags += (byte)1;
		if (m_volume > prevBar.m_volume) flags += (byte)2;
		return flags;
		}

	public final int getAD() // Accumulation Distribution (Japanese Volume)
		{
		long denominator = (m_high != m_low ) ? m_high - m_low : 1;
		return (int)(m_volume * (m_close - m_open) / denominator);
		}

	public final int getII() // Intraday (actually Intrabar) Intensity
		{
		long denominator = (m_high != m_low ) ? m_high - m_low : 1;
		return (int)(m_volume * (2 * m_close - m_high - m_open) / denominator);
		}

	public final int typicalPrice() // Typical Price
		{
		return (m_high + m_low + m_close)/3;
		}

	public final long moneyFlow() // Typical Price
		{
//		int sign = isUp()? 1 : -1;
//		return sign * typicalPrice() * m_volume;
		return typicalPrice() * m_volume;
		}

	public final boolean isConsistent()
		{
		return isConsistent(m_open, m_high, m_low, m_close);
		}

	public static final boolean isConsistent(int open, int high, int low, int close)
		{
		return (open <= high) && (close <= high) && (low <= high)
		    && (open >= low ) && (close >= low );
		}

	/**
	* The following methods support the simulators:
	*  - second() provides a reasonable second price after the open
	*  - secondLast() provides a reasonable price before the close
	*  - avgOpenClose() the avg...() methods are used for prices between
	*  - midpoint()     second() & secondLast()
	*/
	public final int second()     { return (m_open < m_close)? m_low : m_high; }
	public final int secondLast() { return (m_open < m_close)? m_high : m_low; }
	public final int avgOpenClose() { return (m_open + m_close)/2; }
	public final int midpoint() { return (m_high + m_low)/2; }
	}
