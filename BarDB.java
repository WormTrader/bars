package com.wormtrader.bars;
/********************************************************************
* @(#)BarDB.java 1.0 20131111
* Copyright © 2013-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* BarDB: Methods for decoding a bar data String recieved from the
* host into a List of Bar objects.
*
* @author Rick Salamone
* @version 1.00
* 20131111 rts created for M5 data
* 20131111 rts added support for D1 data
*******************************************************/
import com.wormtrader.bars.BarSize;
import com.shanebow.util.SBDate;
import com.wormtrader.bars.Bar;
import java.io.*;
import java.util.List;

public final class BarDB
	{
	/**
	* Decodes M5 data downloaded from the server data base. The
	* format is a caret separated string consisting of the symbol
	* followed by an arbitrary number of date/data pairs. The data
	* consists of the OHLCV values encoded as hex where the OHLC
	* are all 8 bytes long and the V is 16 bytes:
	*   SYM^YYYYMMDD(1)^HexBarData(1)[^YYYYMMDD(2)^HexBarData(2)...]
	* @param List<Bar> the list to which the decoded bars are appended,
	*  Note that the list is not cleared, nor is there any checking (e.g.
	*  that the newly added bars are later date than any existing bars)
	* @return the number of bars added to the list
	*/
	public static int decodeM5(String dat, List<Bar> bars)
		{
		int barsLoaded = 0;
		String[] pieces = dat.split("\\^");
		if (pieces.length < 3) return 0;
		for (int j=1; j < pieces.length; ) {
			long time = SBDate.toTime(pieces[j++] + "  09:30");
			String bytes = pieces[j++];
			for (int i = 0; i < bytes.length(); )
				{
				int op = Integer.parseInt(bytes.substring(i, i+=6), 16);
				int hi = Integer.parseInt(bytes.substring(i, i+=6), 16);
				int lo = Integer.parseInt(bytes.substring(i, i+=6), 16);
				int cl = Integer.parseInt(bytes.substring(i, i+=6), 16);
				long vol = Long.parseLong(bytes.substring(i, i+=10), 16);
				if ( bars.add ( new Bar ( time, op, hi, lo, cl, vol, 0, 0 )))
					++barsLoaded;
				time += 5 * 60; // advance 5 minutes
				}
			}
		return barsLoaded;
		}

	/**
	* Decodes D1 data downloaded from the server data base. The
	* format is a caret separated string consisting of the symbol
	* followed by an arbitrary number of year/data pairs:
	*   SYM^YYYY(1)^HexBarData(1)[^YYYY(2)^HexBarData(2)...]
	* Each HexBarData consists of at most 260 bars encoded as
	* hex characters. Each bar consists of:
	*   - mmdd encoded as 4 hex chars (2 bytes)
	*   - OHLC each encoded as 6 hex chars (3 bytes)
	*   - V encoded as 5 hex chars (10 bytes)
	* @param List<Bar> the list to which the decoded bars are appended,
	*  Note that the list is not cleared, nor is there any checking (e.g.
	*  that the newly added bars are later date than any existing bars)
	* @return the number of bars added to the list
	*/
	public static int decodeD1(String dat, List<Bar> bars)
		{
		int barsLoaded = 0;
		String openTime = "  09:30";
		String[] pieces = dat.split("\\^");
		if (pieces.length < 3) return 0;
		for (int j=1; j < pieces.length; ) {
			int yyyy = Integer.parseInt(pieces[j++]);
			String bytes = pieces[j++];
			for (int i = 0; i < bytes.length(); )
				{
				int yyyymmdd = yyyy * 10000
				       + Integer.parseInt(bytes.substring(i, i+=4), 16);
				int op = Integer.parseInt(bytes.substring(i, i+=6), 16);
				int hi = Integer.parseInt(bytes.substring(i, i+=6), 16);
				int lo = Integer.parseInt(bytes.substring(i, i+=6), 16);
				int cl = Integer.parseInt(bytes.substring(i, i+=6), 16);
				long vol = Long.parseLong(bytes.substring(i, i+=10), 16);
				long time = SBDate.toTime("" + yyyymmdd + openTime);
				if ( bars.add ( new Bar ( time, op, hi, lo, cl, vol, 0, 0 )))
					++barsLoaded;
				}
			}
		return barsLoaded;
		}
	}
