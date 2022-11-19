package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)DateRangePanel.java 1.00 20120807
* Copyright © 2008-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* DateRangePanel:
*
* @author Rick Salamone
* @version 2.00
* 20080410 rts created
* 20120807 rts decoupled from existing back tester code
* 20130515 rts repackaged under bars & getAvailableDates is protected
*******************************************************/
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.shanebow.ui.SBDateField;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SBRadioPanel;
import com.shanebow.util.SBDate;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;

public class DateRangePanel
	extends JPanel
	{
	private static final String[] DATE_CHOICES =
		{ "All", "2008", "2009", "2010", "2011", "2012" };

	// Date Range Settings
	private SBDateField tfEndDate   = new SBDateField();
	private SBDateField tfStartDate = new SBDateField();
//	private JCheckBox   chkBackfill = new JCheckBox( "Preload standard historical data?", true );
	private final SBRadioPanel<String> group;

	public DateRangePanel()
		{
		super(new GridLayout(2,1));
		group = new SBRadioPanel<String>(1, 0, DATE_CHOICES );
		group.addActionListener( new ActionListener()
			{
			public void actionPerformed( ActionEvent e )
				{
				String cmd = e.getActionCommand();
				if ( cmd.equals( DATE_CHOICES[0] ))
					{
					long[] range = getAvailableDates();
					tfStartDate.setValue((range!=null)? range[0] : 0);
					tfEndDate.setValue((range!=null)? range[1] : SBDate.timeNow());
					}
				else
					{
					tfStartDate.setText( cmd + "0101" );
					tfEndDate.setText( cmd + "1231" );
					}
				}
			});
	//	group.select("All");
		add(group);
		JPanel text = new JPanel(new GridLayout(1,2));
		text.add(tfStartDate);
		text.add(tfEndDate);
		add(text);
//		addRow( "Backfill", chkBackfill );
		}

	public void set(String startYYYYMMDD, String endYYYYMMDD)
		{
		tfStartDate.setText(startYYYYMMDD);
		tfEndDate.setText(endYYYYMMDD);
		}

	public final long[] get()
		{
		long[] dateRange = getDateRange();
		if (dateRange != null
		&&  dateRange[0] > dateRange[1] )
			{
			SBDialog.inputError( "Inconsistent dates" );
			return null;
			}
		return dateRange;
		}

	public final String  getStartDate() { return tfStartDate.getText(); }
	public final String  getEndDate()   { return tfEndDate.getText(); }
	public final long[] getDateRange()
		{
		String yyyymmdd0 = getStartDate();
		if (yyyymmdd0 == null) return null;
		String yyyymmdd1 = getEndDate();
		if (yyyymmdd1 == null) return null;
		long[] range = { SBDate.toTime(yyyymmdd0 + "  09:30"),
		                 SBDate.toTime(yyyymmdd1 + "  16:00") };
		return range;
		}

	public final boolean isDataFor( String symbol, BarSize barSize, long[] reqDates )
		{
		long[] availDates = BarList.dateRange( symbol, barSize );
		if ( availDates == null )
			return SBDialog.inputError ( "No " + barSize + " data found for " + symbol);
		if ( availDates[0] > reqDates[0] || availDates[1] < reqDates[1] )
			{
			String msg = "<html>The following dates are available for <b>" + symbol
			           + ":<br>" + SBDate.yyyymmdd(availDates[0])
			           + " - " + SBDate.yyyymmdd(availDates[1]);
			return SBDialog.inputError ( msg );
			}
		return true;
		}

	protected long[] getAvailableDates() { return null; }
	}
