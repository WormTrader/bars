package com.wormtrader.bars.symbols;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;

public class SymbolListPanel extends JTextArea
	{
	public static final String DOUBLE_CLICK_PROPERTY="double click";
	public static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);
	
	// Instance initialization code:
		{
		setFont(FONT);
		setForeground(Color.BLUE);
		setBackground(new Color(238,238,238));
		setTabSize(6);
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		setMargin(new Insets(4,10,2,10));
		addMouseListener ( new MouseAdapter ()
			{
			public void mouseReleased (MouseEvent e)
				{
				if (e.getClickCount() == 2)// doubleClick ();
					{
					// Get symbol under double click, and tell listeners
					String selected = getSelectedText().trim();
					if ((selected != null) && !selected.isEmpty())
						firePropertyChange(DOUBLE_CLICK_PROPERTY, "", selected);
					}
				}
			});
		}

	public void setTitle( String title )
		{
		setBorder(BorderFactory.createTitledBorder(title));
		}

	public void println ( String msg )
		{
		append( msg + "\n");
		setCaretPosition(getDocument().getLength());
		}

	public void showTop()
		{
		setCaretPosition(0);
		scrollRectToVisible(new Rectangle(0,0,1,1));
		}

	public void clear() { setText(""); }
	}
