/*	$NetBSD: main.c,v 1.5 1996/05/21 21:53:09 mrg Exp $	*/

/*-
 * Copyright (c) 1991, 1993
 *	The Regents of the University of California.  All rights reserved.
 *
 * The game adventure was originally written in Fortran by Will Crowther
 * and Don Woods.  It was later translated to C and enhanced by Jim
 * Gillogly.  This code is derived from software contributed to Berkeley
 * by Jim Gillogly at The Rand Corporation.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package adv32;


class AdvSaveData implements java.io.Serializable
{
	public final static int LAST_LOCATION_INDEX = 140;
	public final static int FIRST_TREASURE_INDEX = 50;
	public final static int LAST_TREASURE_INDEX = 79;
	public final static int LAST_OBJECT_INDEX = 100;
	public final static int FIXED_OBJECT_OFFSET = LAST_OBJECT_INDEX;
	public final static int MAX_VERBS = 35;
	
	// ----------------------------------------------------
	public final static int TOTING = -1;
	
	// ----------------------------------------------------
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
	public int tkk_index;  // TODO: This pointer needs work!!!!!!!!!
	public int turns;
	public int verb;
	public String wd1;
	public String wd2;
	public boolean wzdark;
    public boolean yea;
    public int atloc;
	public int dwarfLoc[] = new int[7];  // dwarf stuff
	public int odloc[] = new int[7];
	public boolean dwarfSeenAtLoc[] = new boolean[7];
	public boolean hinted[] = new boolean[20];
	public int link[] = new int[FIXED_OBJECT_OFFSET + LAST_OBJECT_INDEX + 1];
	public int place[] = new int[LAST_OBJECT_INDEX+1];
	public int fixed[] = new int[LAST_OBJECT_INDEX+1];
	public int prop[] = new int[LAST_OBJECT_INDEX+1];
	public int tk[] = new int[21];
}
