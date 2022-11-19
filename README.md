# Bars

The `Bar` class represents a market data bar, consisting of
 open, high, low, and close price for an arbitrary `BarSize`
 along with the volume of trades.

The prices are represented internally as cents (thus avoiding
 decimals and the associated vagarities of floating point storage
 and calculations). The volumes are represented in 1000's, a
 decision I regret!

@author Rick Salamone
@version 1.0 20071115
@version 1.1 20071201
   added toString() & Bar(String) to facilitate file IO
@version 1.2 20081102
   added Accumulation Distribution calculations
   added Intrabar Intensity calculations
@version 2.0 20100110
   Changed adjust methods to leave the time as the original
   time bar time is the beginning of the bar to be consistent
   with historical intraday data.
   Also removed constructors accepting a String date to ensure
   more preceise representation of each bar's time.
@version 2.1 20110628
   added range() method which simply returns high minus low
@version 2.2 20120422
   added methods second(), secondLast(), midpoint(), and
   agvOpenClose(). These prices are sent to the simulators
   between the opening & closing prices to simulate the market.
@version 2.3 20120501
   added method ohlcHTML() for more readable graph tool tip
@version 2.4 20120519
   added static & class methods isConsistent() for validation
20120723 rts added mfi()
20120905 rts added isUp(), isDown, isDoji()
20121115 rts added isInside(Bar prev)
20120309 rts added split()
20130326 rts added mfiType()
20130510 rts added moneyFlow()
