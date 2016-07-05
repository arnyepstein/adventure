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


public class AdvGameData extends AdvSaveData
{
	public final static int TURNS_IN_A_DEMO_GAME=50;// How short is a demo game?

	// ---------------------------------------------------------------------
	public static final class TravList
	{
		TravList next;	//  ptr to next list entry	   
		int conditions;	//  m in writeup (newloc / 1000) 
		int tloc;		//  n in writeup (newloc % 1000) 
		int tverb;		//  the verb that takes you there
		int index;		// Used for Save and Restore (java version only)
	}
	// ---------------------------------------------------------------------
	public static final class QueryState {

		boolean isrdata;
		int question;
		int true_response;
		int false_response;
		int next_label;
		boolean response;

		QueryState(
			boolean isrdata,
			int question,
			int true_response,
			int false_response,
			int next_label)
		{
			this.false_response = false_response;
			this.true_response = true_response;
			this.isrdata = isrdata;
			this.next_label = next_label;
			this.question = question;
		}
		public boolean saidYes() {
			return response;
		}
	}

	// ---------------------------------------------------------------------
	TravList tkk;
	
	public int keys;
	public int lamp;
	public int grate;
	public int cage;
	public int rod;
	public int rod2;
	public int steps;
	public int bird;
	public int door;
	public int pillow;
	public int snake;
	public int fissur;
	public int tablet;
	public int clam;
	public int oyster;
	public int magzin;
	public int dwarf;
	public int knife;
	public int food;
	public int bottle;
	public int water;
	public int oil;
	public int plant;
	public int plant2;
	public int axe;
	public int mirror;
	public int dragon;
	public int chasm;
	public int troll;
	public int troll2;
	public int bear;
	public int messag;
	public int vend;
	public int batter;

	public int nugget;
	public int coins;
	public int chest;
	public int eggs;
	public int tridnt;
	public int vase;
	public int emrald;
	public int pyram;
	public int pearl;
	public int rug;
	public int chain;
	public int spices;
	public int back;
	public int look;
	public int cave;
	public int vnull;
	public int entrnc;
	public int dprssn;
	public int enter;

	public int pour;
	public int say;
	public int lock;
	public int vthrow;
	public int find;
	public int invent;
    //
	public int saved;
	
	// various flags & counters
	public boolean isClosing;
	public boolean isClosed;
	public boolean isScoring;
	//
	//
	public QueryState queryState;
	public int delhit;
	public int hntmax;
	public int hintlc[] = new int[20];
	public String wd1 = null;
	public String wd2 = null;
	public int abb[] = new int[LAST_LOCATION_INDEX+1];
	public int atloc[] = new int[LAST_LOCATION_INDEX+1];
	// Initial Object Placement
	public int plac[] = new int[LAST_OBJECT_INDEX+1];
	public int fixd[] = new int[LAST_OBJECT_INDEX+1];
	// rspeak for verb[n]
    public int actspk[] = new int[MAX_VERBS+1];
}
