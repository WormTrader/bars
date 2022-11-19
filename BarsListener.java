package com.wormtrader.bars;
/********************************************************************
* @(#)BarsListener.java 1.00 20071105
* Copyright © 2007 - 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* BarsListener: Interface implemented by classes that wish to be notified
* of realtime and/or historical bars from the broker.
*
* @version 1.20 03/24/08 - added barError()
* @version 1.20 12/03/07 - added historyDone()
* @version 1.10 12/02/07 - made historical & realtime orthoginal
* @version 1.00 11/05/07
* @author Rick Salamone
*******************************************************/
import com.wormtrader.bars.Bar;

public interface BarsListener
	{
	public void historyBar(	Bar bar );
	public void historyDone();					// Historical data request completed
	public void realtimeBar( Bar bar );
	public void barError ( int errorCode, String errorMsg );
	}
