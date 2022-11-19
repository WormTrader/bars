package com.wormtrader.bars;
/********************************************************************
* @(#)M5Gaps.java 1.00 20140220
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* M5Gaps: A single symbol along with the dates for which we
* are missing the M5 historical price data for whatever reason.
*
* @author Rick Salamone
* @version 1.00
* 20140220 rts created
* 20140329 rts added inRange()
*******************************************************/
import com.wormtrader.almanac.DateWalker;
import com.shanebow.util.SBDate;

public final class M5Gaps
	implements Comparable<M5Gaps>
	{
	public static final String SEPARATOR = ",";

	private final String fSymbol;
	private String fPacked;
	private int fCount;

	public M5Gaps( String csv )
		{
		String[] split = csv.split(SEPARATOR,2);
		fSymbol = split[0].trim();
		set ((split.length > 1)? split[1] : "");
		}

	/**
	* MUST return inverse of the ctor that accepts a csv String for TextFile.save()
	*/
	@Override public final String toString() {
		return fSymbol + SEPARATOR + fPacked;
		}

	public String symbol() { return fSymbol; }
	public int    size() { return fCount; }

	/**
	* Accepts a csv consisting of yyyymmdd formatted dates and
	* yyyymmdd-yyyymmdd date ranges (inclusive)
	* Do not include the symbol!
	* For instance "20060101,20060203-20060507,20071224"
	*/
	public int set(String csv) {
		fPacked = csv.trim();

		// just count the number missing for now
		if (fPacked.isEmpty())
			return fCount = 0;

		String[] pieces = fPacked.split(SEPARATOR);
		fCount = pieces.length;
		for (String dateOrRange : pieces)
			if (dateOrRange.length() > 8) { // it's a range yyyymmdd-yyyymmdd
				String[] srange = dateOrRange.split("-");
				long[] range = { SBDate.toTime(srange[0]), SBDate.toTime(srange[1]) };
				fCount += new DateWalker(range).size() - 1;
				}
		return fCount;
		}		

	public boolean includes(String yyyymmdd) {
		String[] pieces = fPacked.split(SEPARATOR);
		for (String dateOrRange : pieces) {
			if (dateOrRange.startsWith(yyyymmdd))
				return true;
			else	if ((dateOrRange.length() > 8) && inRange(dateOrRange, yyyymmdd))
				return true;
			}
		return false;
		}

	private final boolean inRange(String range, String target) {
		String[] srange = range.split("-");
		return srange[0].compareTo(target) <= 0
		    && srange[1].compareTo(target) >= 0;
		}

	public String packed() { return fPacked; }

	@Override public int compareTo(M5Gaps other) {
		return fSymbol.compareTo(other.fSymbol);
		}

	@Override public boolean equals(Object aOther)
		{
		if (aOther == null)
			return false;
		if (aOther instanceof M5Gaps)
			return ((M5Gaps)aOther).fSymbol.equals(this.fSymbol);
		return equals(aOther.toString());
		}

	public final boolean equals(String aOther) {
		return fSymbol.equalsIgnoreCase(aOther);
		}
	}
