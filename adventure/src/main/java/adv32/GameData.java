package adv32;

import java.io.Serializable;

public class GameData implements Serializable {// ----------------------------------------------------
	public int attack;
	public int abbnum;
	public boolean blklin;
	public int bonus;
	public int chloc;
	public int chloc2;
	public int clock1;
	public int clock2;
	public int daltlc;
	public boolean demo;
	public int detail;
	public int dflag;
	public int dkill;
	public int dtotal;
	public int foobar;
	public boolean gaveup;
	public int holdng;
	public int iwest;
	public int k;
	public int k2;
	public int knfloc;
	public int kq;
	public int latncy;
	public int limit;
	public boolean lmwarn;
	public int loc;
	public int maxdie;
	public int mxscor;
	public int newloc;
	public int numdie;
	public int obj;
	public int oldlc2;
	public int oldloc;
	public boolean panic;
	public int saved_last_usage;
	public boolean scorng;
	public int spk;
	public int stick;
	public int tally;
	public int tally2;
	public int turns;
	public int verb;
	public boolean wzdark;
	public boolean yea;
	public int atloc;
	public int dwarfLoc[] = new int[7];
	public int odloc[] = new int[7];
	public boolean dwarfSeenAtLoc[] = new boolean[7];
	public boolean hinted[] = new boolean[20];
	public int link[];
	public int place[];
	public int fixed[];
	public int prop[];
	public int tk[] = new int[21];

	public GameData() {
		this.link = new int[AdvSaveData.FIXED_OBJECT_OFFSET + AdvSaveData.LAST_OBJECT_INDEX + 1];
		this.place = new int[AdvSaveData.LAST_OBJECT_INDEX + 1];
		this.fixed = new int[AdvSaveData.LAST_OBJECT_INDEX + 1];
		this.prop = new int[AdvSaveData.LAST_OBJECT_INDEX + 1];
	}
}