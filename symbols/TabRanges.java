package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)TabRanges.java 1.00 2007????
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TabRanges: Lists available historical market data
*
* @author Rick Salamone
* @version 2.00
* 2007???? rts created
* 20140504 rts rewrite to use BarDAO calls
*******************************************************/
import com.wormtrader.bars.BarList;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;

public class TabRanges extends JPanel
	{
	private SymbolListPanel m_log = new SymbolListPanel();

	public TabRanges(PropertyChangeListener pcl)
		{
		this();
		m_log.addPropertyChangeListener(pcl);
		}

	public TabRanges()
		{
		super( new BorderLayout());
		add( new JScrollPane(m_log), BorderLayout.CENTER );
		add( btnPanel(), BorderLayout.SOUTH );
		setBorder(LAF.bevel(5,5));
		}

	private JPanel btnPanel()
		{
		JPanel pBtn = new JPanel();
		pBtn.add(new SBAction("Fetch", 'F', "Get list of available historical data", null) {
			public void action() {fetch();}
			}.makeButton(true));
		return pBtn;
		}

	private String pad(String x)
		{
		int len = x.length();
		for ( int i = len; i < 7; i++ )
			x += ".";
		return x;
		}

	private void fetch()
		{
		m_log.clear();
		m_log.setTitle( "Market History Catalog" );

		try {
			List<String[]> ranges = BarList.ranges();
			for (String[] rec : ranges)
			m_log.println(pad(rec[0]) + rec[1] + " - " + rec[2]
			                    + " " + rec[3] + " - " + rec[4]);
			}
		catch (Exception e) {
			m_log.println("No historical data found!" );
			}
		}
	}
