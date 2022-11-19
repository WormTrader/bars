package com.wormtrader.bars;
/********************************************************************
* @(#)BarList.java 1.00 20120615
* Copyright © 2012-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* BarList: Extends Vector<Bar> to enforce that the Bars are stored in
* order, and implements a binary search method for finding a Bar given
* a SBDate style time (i.e. seconds since 1970).
*
* About using binarySearch:
*  - assumes bars are sorted on bar.getTime()
*
* @author Rick Salamone
* @version 1.00
* 20120615 rts created
* 20120821 rts added find(yyyymmdd)
* 20130228 rts added composite()
* 20130301 rts added toString()
* 20130302 rts added ctors to permit specify capacity
* 20130309 rts added split()
* 20130513 rts added back()
* 20131114 rts bug fix composite() was only composing first bar!
* 20131127 rts added fetch to retrieve history into this list
* 20140503 rts added dateRange (instance version)
* 20140504 rts added static getClose from bar file & ranges()
*******************************************************/
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import java.util.Vector;

public final class BarList
	extends Vector<Bar>
	{
	static private BarDAO _dao = new BarHostDAO(); // new BarFileDAO();

	public static void setDAO(BarDAO aDAO) {
		_dao = aDAO;
		SBLog.write("BarList DAO: " + _dao);
		}

	public static final java.util.List<String[]> ranges() throws Exception {
		return _dao.ranges();
		}

	public static final long[] dateRange( String symbol, BarSize barSize ) {
		return _dao.dateRange(symbol, barSize);
		}
	public static final String[] m5Dates(String sym) {
System.out.format("dao.m5Dates(%s)\n", sym);
		return _dao.m5Dates(sym);
		}
	public static final boolean m5Exists ( String symbol, String date ) {
System.out.format("check dao.m5Exists(%s,%s)\n", symbol, date);
		return _dao.m5Exists ( symbol, date );
		}
	public static final String[] m5Symbols() {
System.out.println("dao.m5Symbols()");
		return _dao.m5Symbols();
		}

	/**
	* Constructors
	*/
	public BarList() { super(); }
	public BarList(int aInitialCapacity) { super(aInitialCapacity); }

	public final Bar firstBar() { return get(0); }
	public final Bar lastBar() { return get(size()-1); }

	/**
	* composite() builds one big bar by merging all of the bars in the list
	*/
	public final Bar composite()
		{
		int size = size();
		if (size == 0) return null;

		Bar small = get(0); // firstBar();
		Bar big = new Bar(small.getTime(), small);  // clone the smaller bar
		for (int i = 1; i < size; i++)
			big.adjust(get(i));
		return big;
		}

	public final long firstTime() {
		return (size() > 0)? get(0).getTime(): 0;
		}

	public final long lastTime() {
		int size = size();
		return (size > 0)? get(size-1).getTime() : 0;
		}

	public final long[] dateRange() { return new long[]{firstTime(),lastTime()}; }
/*************
	public int indexOf(Object e)
		{
		return (Object instanceof Bar)? find(((Bar)e).getTime()
		     : (Object instanceof String)? find ((String)e)
		     : null;
		}

	public int lastIndexOf(Object e)
		{
		if (e != null) for ( int i = fLastFilledSlot; i >= 0; i-- )
			if ( e.equals(m_elements[i])) return i;
		return -1;
		}
*************/
	/**
	* Stores the element in the first emply slot. If there
	* are no empty slots, the list is expanded, and the
	* element is placed at the end of the list.
	* @return true - always
	* this return required for java.util.Collection
	public boolean add(E e)
		{
		iAdd(e);
		return true;
		}
	*/

	/**
	* @return the Bar that is x bars back from the end of the list
	* or null if there are not enough bars
	*/
	public final Bar back(int x)
		{
		int index = size() - 1 - x;
		return (index < 0)? null : get(index);
		}

	public final Bar find(String yyyymmdd)
		{
		return find(SBDate.toTime(yyyymmdd + "  09:30"));
		}

	public final Bar find(long aTime)
		{
		int index = binarySearch(aTime);
		return (index < 0)? null : get(index);
		}

	public final int binarySearch(long aTime)
		{
		int first = 0;
		int upto = size();

		while (first < upto)
			{
			int mid = (first + upto) / 2;  // Compute mid point
			// int mid = first + (upto - first) / 2; // avoids int overflow issues
			long midTime = get(mid).getTime();
			if ( aTime < midTime )      upto = mid;      // look in bottom half
			else if ( aTime > midTime ) first = mid + 1; // look in top half
			else return mid;                             // Found: return position
			}
		return -(first + 1);  // Failed to find time
		}

	@Override public final String toString()
		{
		int nbars = size();
		return (nbars == 0)? "no bars"
		                   : "" + nbars + " bars "
		                        + SBDate.yyyymmdd__hhmmss(get(0).getTime()) + " - "
		                        + SBDate.yyyymmdd__hhmmss(get(nbars-1).getTime());
		}

	/**
	* split() Scales all of the bars in the list by the split amount
	* For instance call split(2,1) when a 2 for 1 stock split halves
	* the price or split(1,2) when a 1 for 2 split doubles the price.
	*/
	public final void split(int newQty, int forOldQty)
		{
		for (Bar bar : this)
			bar.split(newQty, forOldQty);
		}

	/**
	* fetch() - appends history bars for the given symbol, BarSize, and
	* date range to this list of bars.
	* @return the number of bars appended
	*/
	public final int fetch(String symbol, BarSize barSize, long[] times) {
		return _dao.thaw(symbol, barSize, times, this);
		}

	/**
	* fetchM5() - appends history bars for the given symbol, BarSize, and
	* date range to this list of bars.
	* @return the number of bars appended
	*/
	public final int fetchM5(String symbol, String yyyymmdd) {
		return _dao.thawM5(symbol, yyyymmdd, this);
		}

	/**
	* fetchD1() - appends daily history bars for the given symbol and date
	* range to this list of bars.
	* @return the number of bars appended
	*/
	public final int fetchD1(String symbol, long[] times) {
		return _dao.thawD1(symbol, times, this);
		}

	public static final int getClose(String aSymbol, String yyyymmdd)
		throws Exception
		{
		long time = SBDate.toTime(yyyymmdd + "  09:30");
		long[] dateRange = { time, time };
		BarList bars = new BarList(1);
		if ( _dao.thawD1(aSymbol, dateRange, bars) < 1)
			throw new Exception("No price data for " + aSymbol + " on " + yyyymmdd);
		return bars.get(0).getClose();
		}
	}
