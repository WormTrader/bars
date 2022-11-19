package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)NavIntradayjava 1.00 2009????
* Copyright (c) 2007-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* NavIntraday: A widget for choosing the date for intraday bar files,
* consisting of a combobox with prev/next buttons.
*
* Override the accept(String aFilename) method to filter the list.
*
* @author Rick Salamone
* @version 1.00
* 2009???? rts created
* 20120511 rts no longer a final class since accept needs overriden
* 20120605 rts reload now selects most recent date by default
* 20140209 rts reload calls m5dates() in prep for dao
* 20140215 rts modified to access the dao
*******************************************************/
import com.wormtrader.bars.BarSize;
import com.wormtrader.bars.BarList;
import com.shanebow.ui.Navigator;
import com.shanebow.util.SBDate;

public class NavIntraday extends Navigator
	{
	{ cbox.setPrototypeDisplayValue("88888888"); }

	/**
	* Override to enforce an absolute earliest date for example
	*/
	public boolean accept(String aDate) { return true; }

	public int setSymbol( String symbol )
		{
		return reload(symbol, getSelectedItem());
		}

	public void setSelectedItem(int ymd) {
		setSelectedItem("" + ymd);
		}

	public int reload(String symbol, String yyyymmdd)
		{
		removeAllItems();
		m_fillingBox = true;
		String[] m5dates = BarList.m5Dates(symbol);
		if (m5dates != null) 
			for (String date : m5dates)
				if (accept(date)) cbox.addItem(date);
		m_fillingBox = false;

		int itemCount = getItemCount();
		if (itemCount > 0)
			{
			if (yyyymmdd != null) setSelectedItem(yyyymmdd);
			else setSelectedIndex(itemCount-1);
			}
		return itemCount;
		}

	public long getSelectedTime(String hhmm)
		{
		String yyyymmdd = getSelectedItem();
		if ( yyyymmdd == null ) return 0;
		return SBDate.toTime( yyyymmdd + "  " + hhmm);
		}
	}
