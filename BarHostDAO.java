package com.wormtrader.bars;
/********************************************************************
* @(#)BarHostDAO.java 1.00 20140208
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* BarHostDAO: An interface that defines IO methods to retrieve historical bars.
* Concrete implementations should be written to access data directly from
* the database, over the network, or via a file.
*
* @author Rick Salamone
* @version 1.00
* 20140208 rts created
* 20140215 rts first implementation done - no optimizations
* 20140504 rts bug fixes srange handles null and 0 for range[0]
*******************************************************/
import com.wormtrader.almanac.DateWalker;
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarSize;
import com.shanebow.util.SBDate;
import com.shanebow.web.host.Post;
import com.shanebow.web.host.PostResponse;
import java.util.List;
import java.util.Vector;

public final class BarHostDAO
	extends BarDAO
	{
	static private final Post fPoster = new Post();
	private static final List<M5Gaps> _gaps = new Vector<M5Gaps>(55);
	static {
//		loadGaps();
		}

	private static void loadGaps() {
		String args = "sym=gaps";
		String app = "apps/cat.php";
		Post poster = new Post();
		PostResponse resp = poster.send(app, args);
		if (resp.dat.isEmpty())
			return;
		String[] pieces = resp.dat.split("\\^");
		_gaps.clear();
		for (int i = 0; i < pieces.length; ) {
			String sym = pieces[i++];
			String packed = pieces[i++];
			M5Gaps gap = new M5Gaps(sym);
			_gaps.add(gap);
			int count = gap.set(packed);
			}
		((Vector)_gaps).trimToSize();
		}

	public final List<String[]> ranges()
		throws Exception
		{
		String args = "sym=*";
		String app = "apps/cat.php";
		PostResponse resp = fPoster.send(app, args);
		if (resp.err != 0)
			throw new Exception("Get Ranges Failed: " + resp.msg);
		if (resp.dat.isEmpty())
			return new Vector<String[]>();
		String[] pieces = resp.dat.split("\\^");
		Vector<String[]> it = new Vector<String[]>(pieces.length/5);
		for (int i = 0; i < pieces.length; ) {
			String[] rec = new String[5];
			for (int j = 0; j < 5; j++)
				rec[j] = pieces[i++]; // sym,d1first,d1last,m5first,m5last
			it.add(rec);
			}
		return it;
		}

	public final List<M5Gaps> gaps() { return _gaps; }

	public final long[] dateRange(String symbol, BarSize barSize) {
		PostResponse resp = fPoster.send("apps/cat.php", "sym="+symbol.toUpperCase());
		if (resp.dat.isEmpty())
			return null;
		int i = barSize.isIntraday()? 1 : 3;
		String[] pieces = resp.dat.split("\\^");
		long[] it = { SBDate.toTime(pieces[i++] + "  09:30"),
		              SBDate.toTime(pieces[i++] + "  16:00") };
		return it;
		}

	public final String[] m5Dates(String aSymbol) {
		PostResponse resp = fPoster.send("apps/cat.php", "sym="+aSymbol.toUpperCase());
		if (resp.dat.isEmpty())
			return null;
		String[] pieces = resp.dat.split("\\^");
		List<String> list = new DateWalker(pieces[3], pieces[4]).list();
		if (pieces.length > 5 && !pieces[5].isEmpty())
			removeGaps(list, pieces[5]);
		return list.toArray(new String[0]);
		}

	private final void removeGaps(List<String> aDates, String aGaps) {
		String[] gaps = aGaps.split(",");
		for (String dateOrRange : gaps)
			if (dateOrRange.length() > 8) { // it's a range
				String[] pieces = dateOrRange.split("-");
				aDates.removeAll(new DateWalker(pieces[0], pieces[1]).list());
				}
			else aDates.remove(dateOrRange); // it's one date
		}

	public final boolean m5Exists (String sym, String yyyymmdd) {
		BarList bars = new BarList();
		int count = requestBars(sym, BarSize.FIVE_MIN, yyyymmdd + yyyymmdd, bars);
		bars.clear();
		return count > 0;
		}

	public final String[] m5Symbols() {
		PostResponse resp = fPoster.send("apps/cat.php", "sym=m5");
		if (resp.dat.isEmpty())
			return null;
		return resp.dat.split("\\^");
		}

	private String srange(long[] times) {
		if (times == null)
			return "00000000" + SBDate.yyyymmdd();
		int[] idates = {(times[0]==0)?0:Integer.parseInt(SBDate.yyyymmdd(times[0])),
		            Integer.parseInt(SBDate.yyyymmdd(times[1]))};
		if ((idates[0] != 0) && SBDate.hhmm(times[0]).compareTo("16:00") >= 0 )
			idates[0]++;
		if ( SBDate.hhmm(times[1]).compareTo("09:30") < 0 )
			idates[1]--;
		return String.format("%08d%8d", idates[0], idates[1]);
		}

	public final int thaw (String sym, BarSize barSize, long[] times, List<Bar> bars) {
		String range = srange(times);
		if (barSize == BarSize.FIVE_MIN || barSize == BarSize.ONE_DAY)
			return requestBars(sym, barSize, range, bars);

		int oldSize = bars.size();
		BarList smallBars = new BarList();
		int numSmall = requestBars(sym, barSize, range, smallBars);
		if (numSmall == 0)
			return oldSize;
		Bar bigBar = null;
		if ( barSize.isIntraday())
			{
			int fiveMinBarsPer = get5MinBarsPer(barSize);
			int i = 0;
			for ( Bar bar5 : smallBars )
				{
				if ((++i == fiveMinBarsPer)
				||  bar5.hhmm().equals("09:30")) {
					bars.add( bigBar = bar5 );
					i = 0;
// System.out.println("Add bar " + bar5.hhmm());
					}
				else
					bigBar.adjust( bar5 );
				}
			}
		else if ( barSize == BarSize.ONE_WEEK)
			{
			int priorDOW = 8;
			for (Bar small : smallBars)
				{
				int dow = small.dow();
				if (dow == 2 || dow < priorDOW) // monday == 2
					bars.add(bigBar = small);
				else
					bigBar.adjust(small);
				priorDOW = dow;
				}
			}
		return bars.size() - oldSize;
		}

	public final int thawM5 (String sym, String yyyymmdd, List<Bar> bars) {
		return requestBars(sym, BarSize.FIVE_MIN, yyyymmdd + yyyymmdd, bars);
		}

	private final int thawM5 (String sym, long[] times, List<Bar> bars) {
		return requestBars(sym, BarSize.FIVE_MIN, srange(times), bars);
		}

	public final int thawD1 (String sym, long[] times, List<Bar> bars) {
		return requestBars(sym, BarSize.ONE_DAY, srange(times), bars);
		}

	private int requestBars(String sym, BarSize barSize, String srange, List<Bar> bars) {
		String args = "bs=" + (barSize.isIntraday()? "m5" : "d1")
		            + "&sym="+sym+"&r=" + srange;
		String app = "apps/thaw.php";
System.out.format("requestBars(%s,%s,%s)\n",sym,barSize.toString(),srange);
		PostResponse resp = fPoster.send(app, args);
		if (resp.err != 0)
{
System.out.format("requestBars(%s,%s,%s): %s\n",sym,barSize.toString(),srange,resp.msg);
			return 0;
}
		return barSize.isIntraday()? BarDB.decodeM5(resp.dat, bars)
		                           : BarDB.decodeD1(resp.dat, bars);
		}

	@Override public String toString() {
		return "BarHostDAO: " + Post._domain;
		}
	}
