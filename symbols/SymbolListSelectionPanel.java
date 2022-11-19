package com.wormtrader.bars.symbols;
/********************************************************************
* @(#)SymbolListSelectionPanel.java 1.00 2009
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* SymbolListSelectionPanel: Panel for choosing the set of symbols
* that will form the universe for a given operation. Two labeled
* combo boxes facillitate the selection of the files containing
* symbols to include and exclude.
*
* @author Rick Salamone
* @version 1.00
* 20100107 rts did something
* 20110616 rts improved layout
* 20120621 rts getting icons using SBAction.getIcon instead of hardwired
* 20120806 rts added property support to save/recall settings
*******************************************************/
import com.wormtrader.bars.symbols.SymbolList;
import com.shanebow.ui.SBAction;
import com.shanebow.util.SBProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.*;

public final class SymbolListSelectionPanel
	extends JPanel
	{
	private final JComboBox cbInclude = new JComboBox();
	private final JComboBox cbExclude = new JComboBox();
	private final String fKeyPrefix;

	public SymbolListSelectionPanel ( String aPropKeyPrefix )
		{
		super();
		fillComboBoxes();
		fKeyPrefix = aPropKeyPrefix;
		SBProperties props = SBProperties.getInstance();
		String includes = props.getProperty(key("in"), SymbolList.INTRADAY_GRABS);
		String excludes = props.getProperty(key("ex"), SymbolList.EXCLUDED_GRABS);
		add( makePanel( "plus",  cbInclude, "Symbols to include", includes ));
		add( makePanel( "minus", cbExclude, "Symbols to exclude", excludes ));
		}

	private JComponent makePanel(String aIcon, JComboBox aComboBox,
		String aToolTip, String aSelectedItem)
		{
		JPanel it = new JPanel(new BorderLayout());
		it.add( new JLabel(SBAction.getIcon(aIcon)), BorderLayout.WEST);
		it.add( aComboBox, BorderLayout.CENTER );
		aComboBox.setToolTipText( aToolTip );
		aComboBox.setSelectedItem(aSelectedItem);
		it.setToolTipText( aToolTip );
		return it;
		}

	private final void fillComboBoxes()
		{
		String[] fileList = SymbolList.available();
		cbExclude.addItem("---");
		for ( int i = 0; i < fileList.length; i++ )
			{
			cbInclude.addItem(fileList[i]);
			cbExclude.addItem(fileList[i]);
			}
		}

	public SymbolList getSelected()
		{
		String includes = getInclude();
		String excludes = getExclude();
		SBProperties props = SBProperties.getInstance();
		props.setProperty(key("in"), includes);
		props.setProperty(key("ex"), excludes);
		return new SymbolList( includes, excludes);
		}

	public String getInclude() { return (String)cbInclude.getSelectedItem(); }
	public String getExclude() { return (String)cbExclude.getSelectedItem(); }

	private final String key(String aSuffix) { return fKeyPrefix + aSuffix; }
	}
