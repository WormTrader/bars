package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)Split.java 1.00 20130316
* Copyright © 2013-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* Split: Represents a stock split as a date, symbol and ratio.
* We track the date as an int in the form yyyymmdd to facilitate
* binary searches.
*
* @author Rick Salamone
* @version 2.00
* 20130316 rts created
* 20140307 rts implements Symbol
*******************************************************/
import com.wormtrader.bars.Symbol;

public final class Split
	implements Symbol, Comparable<Split>
	{
	private final int fYMD;
	private final String fSymbol;
	private final byte[] fFraction = { 0, 0 };

	public Split(int aYMD, String aSymbol, byte numer, byte denom)
		{
		fYMD = aYMD;
		fSymbol = aSymbol;
		fFraction[0] = numer;
		fFraction[1] = denom;
		}

	public Split(String csv)
		{
		String[] pieces = csv.split(",", 4);
		fYMD = Integer.parseInt(pieces[0].trim());
		fSymbol = pieces[1].trim();
		fFraction[0] = Byte.parseByte(pieces[2]);
		fFraction[1] = Byte.parseByte(pieces[3]);
		}

	public String ratio() { return "" + fFraction[0] +":" + fFraction[1]; }
	@Override public String toString()
		{
		return "" + fYMD +"," + fSymbol + "," + fFraction[0] + "," + fFraction[1];
		}

	@Override public int hashCode() { return fYMD; }
	public int ymd() { return fYMD; }
	@Override public final String symbol() { return fSymbol; }
	@Override public boolean equals(Object that) { return this == that; }
	public boolean equals(int aYMD) { return fYMD == aYMD; }
	public byte[] fraction() { return fFraction; }

	@Override final public int compareTo(Split aOther) {
		int symCompare = fSymbol.compareTo(aOther.fSymbol);
		return (symCompare==0)? fYMD - aOther.fYMD : symCompare;
		}
	}
