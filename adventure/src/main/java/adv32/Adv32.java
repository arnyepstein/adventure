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

import java.util.ArrayList;
import java.util.List;

public class Adv32 extends Wizard
{

	private boolean isVerbose = false;

    public static void main( String args[] )
    {
		Adv32 adv = new Adv32(  );
		adv.processCommandLine( args );
    }

	public Adv32(  )
	{
	}

	private final static int L_PREAMBLE=0;
	private final static int L_AFTER_NAV =2;
	private final static int L_PREAMBLE_1 =7;
	private final static int L_RESTORED=8;
	private final static int L_EXIT_GAME=9;
	private final static int L_RESURRECT_RESPONSE=10;
	private final static int L_YES_QUERY =33;
	private final static int L_YES_RESPONSE=34;
	private final static int L_PROMPT_OK=2009;
	private final static int L_PROMPT_K=2010;
	private final static int L_PROMPT_SPK=2011;
	private final static int L_NEXT_MOVE=2012;
	private final static int L_USER_INPUT=2600;
	private final static int L_PARSE_USER_INPUT=2608;
	private final static int L_PROCESS_USER_INPUT=2609;

	private final static int L_CHECK_FOR_REALLY_QUIT=4081;
	private final static int L_CHECK_FOR_QUIT=4082;
	private final static int L_CHECK_FOR_ACCEPTABLE=4083;

	private final static int L_ASK_WHAT_OBJ=8000;
	private final static int L_TRY_KILL=9120;
	private final static int L_TRY_KILL_RESPONSE=9122;


	private void processCommandLine(String[] args) {
		String path = null;
		for(String arg : args) {
			if("-v".equals(arg)) {
				isVerbose = true;
				continue;
			}
			if("-dv".equals(arg)) {
				db_dump_vocab = true;
				continue;
			}
			if("-dt".equals(arg)) {
				db_dump_travel = true;
				continue;
			}
			path = arg;
		}
		try {
			this.doGame(path);
		} catch(EmergencyExit e) {
			if(e.isError()) {
				System.out.println(e.getMessage());
			} else {
				System.out.println("Game Over");
			}
		}
	}

	// ===========================================================================
	static public class CrankOutput  {
		public CrankOutput(Type type, int reentryPoint) {
			this.type = type;
			this.reentryPoint = reentryPoint;
		}

		private void setLines(List<String> lines) {
			this.lines = lines;
		}
		public List<String> getLines() {
			return lines;
		}

		enum Type {
			getInput,
			exit
		}
		List<String> lines;
		Type type;
		int reentryPoint;
	}
	// ===========================================================================
	static class CrankInput {
		public CrankInput(int entryPoint, String userInput) {
			this.entryPoint = entryPoint;
			this.userInput = userInput;
		}
		int entryPoint;
		String userInput;
	}
	// ===========================================================================
	static class ListOutputChannel implements AdvIO.OutputChannel {
		List<String> lines = new ArrayList<>();

		@Override
		public void emitOutputToUser(String msg) {
			lines.add(msg);
		}

		public List<String> getLines() {
			return lines;
		}

	}
	// ===========================================================================

	private void doGameX( String path ) {

		init();
		int initialLabel = L_PREAMBLE;

		// TODO: Handle Save and Restore
//		if (false && path != null)    //  Restore file specified
//		{
//			int restoreChoice = restore(path);    //  See what we've got
//			switch (restoreChoice) {
//				case 0:                //  The restore worked fine
//					yea = Start(0);
//					k = 0;
//					unlink(path);    //  Don't re-use the save
//					initialLabel = L_RESTORED;    //  Get where we're going
//				case 1:                //  Couldn't open it
//					exit(0);        //  So give up
//				case 2:                //  Oops -- file was altered
//					rspeak(202);    //  You dissolve
//					exit(0);        //  File could be non-adventure
//			}                    //  So don't unlink it.
//		}
//		else {
//			startup();
//		}
		CrankInput input = new CrankInput(initialLabel, null);
		while(true) {
			// Create a channel object
			ListOutputChannel outputChannel = new ListOutputChannel();
			setOutputChannel(outputChannel);

			CrankOutput output = turnTheCrank(input);
			output.setLines(outputChannel.getLines());
			for(String line : output.getLines()) {
				System.out.println(line);
			}
			switch(output.type) {
				case exit: return;
				case getInput:
				{
					String line = AdvIO.getInputLine();
					input = new CrankInput(output.reentryPoint, line);
				}
			}
		}
	}

	private void doGame( String path ) {

		CrankOutput output = startGame();

		while(true) {
			for(String line : output.getLines()) {
				System.out.println(line);
			}
			switch(output.type) {
				case exit: return;
				case getInput:
				{
					String line = AdvIO.getInputLine();
					output = nextMove(line);
				}
			}
		}
	}

	public CrankOutput startGame() {
		init();
		return nextMove(L_PREAMBLE, null);
	}

	public CrankOutput nextMove(String line) {
		return nextMove(labelForNextMove, line);
	}

	private int labelForNextMove;

	private CrankOutput nextMove(int label, String inputLine) {
		CrankInput input = new CrankInput(label, inputLine);
		// Create a channel object
		ListOutputChannel outputChannel = new ListOutputChannel();
		setOutputChannel(outputChannel);

		CrankOutput output = turnTheCrank(input);
		output.setLines(outputChannel.getLines());
		labelForNextMove = output.reentryPoint;
		return output;
	}


	public CrankOutput turnTheCrank(CrankInput crankInput) {

		MessageList temp_mlist = null;
		int next_label = crankInput.entryPoint;
		// Main Execution Loop
		for (;;)                        //  main command loop (label 2)
		{
			DP("MainLoop: {0}", next_label);
			switch(next_label)
			{
				case L_RESURRECT_RESPONSE:
				{
					gameData.yea = queryState.saidYes();
					gameData.numdie++;
					if (gameData.numdie == gameData.maxdie || !gameData.yea) {
						next_label = done(2);
						continue;
					}
					gameData.place[water]=0;
					gameData.place[oil]=0;
					if (toting(lamp)) gameData.prop[lamp]=0;
					for (int i=FIXED_OBJECT_OFFSET; i>=1; i--)
					{
						if (!toting(i)) continue;
						gameData.k = gameData.oldlc2;
						if (i==lamp) gameData.k = 1;
						drop(i, gameData.k);
					}
					gameData.loc = 3;
					gameData.oldloc = gameData.loc;
					next_label = 2000;
					continue;

				}
				case L_EXIT_GAME:
				{
					return new CrankOutput(CrankOutput.Type.exit, 0);
				}

				case L_PREAMBLE:
					initRandomNumbers();

					gameData.demo = Start(0);
					{next_label=initiateYes(65,1,0, L_PREAMBLE_1); continue;}

				case L_PREAMBLE_1:
//				hinted[3] = yes(65,1,0);
					gameData.hinted[3] = queryState.saidYes();
					gameData.newloc = 1;
					delhit = 0;
					gameData.limit = 330;
					if (gameData.hinted[3])
					{
						gameData.limit = 1000;      //  better batteries if instrucs
					}

				case L_AFTER_NAV:
					if (gameData.newloc <9 && gameData.newloc !=0 && isClosing)
					{
						rspeak(130);    //  if closing leave only by
						gameData.newloc = gameData.loc;     //       main office
						if (!gameData.panic) gameData.clock2 = 15;
						gameData.panic = true;
					}
				{
					int rval=fdwarf();          //  dwarf stuff
					if (rval==99)  {next_label = die(99); continue;}
				}
				case 2000:
					if (gameData.loc ==0) {next_label = die(99); continue;}
//TODO:: Fix This
					temp_mlist = getSText(gameData.loc);
					if ((abb[gameData.loc]% gameData.abbnum)==0 || temp_mlist == null)
						temp_mlist = getLText(gameData.loc);
					if (!forced(gameData.loc) && dark(0))
					{
						if (gameData.wzdark && pct(35))
						{
							next_label = die(99); continue;
						}
						temp_mlist = getRText(16);
					}
				case 2001:
					if (toting(bear)) rspeak(141);  //  2001
					speak(temp_mlist);
					gameData.k = 1;
					if (forced(gameData.loc))
					{next_label=8; continue;}
					if (gameData.loc ==33 && pct(25)&&!isClosing) rspeak(8);
					// FLATTEN
					//if (!dark(0))
					// {
					if (dark(0)) {next_label=L_NEXT_MOVE; continue;}
					abb[gameData.loc]++;
					for (int temp_i = atloc[gameData.loc]; temp_i!=0; temp_i= gameData.link[temp_i])     // 2004
					{
						gameData.obj = temp_i;
						if (gameData.obj >FIXED_OBJECT_OFFSET)
							gameData.obj = gameData.obj - FIXED_OBJECT_OFFSET;
						if (gameData.obj ==steps && toting(nugget))
							continue;
						if (gameData.prop[gameData.obj]<0)
						{
							if (isClosed)
								continue;
							gameData.prop[gameData.obj]=0;
							if (gameData.obj ==rug|| gameData.obj ==chain)
								gameData.prop[gameData.obj]=1;
							gameData.tally--;
							if (gameData.tally == gameData.tally2 && gameData.tally != 0)
								if (gameData.limit >35) gameData.limit = 35;
						}
						{
							int propid = gameData.prop[gameData.obj];   //  2006
							if (gameData.obj ==steps && gameData.loc == gameData.fixed[steps])
								propid = 1;
							pspeak(gameData.obj, propid);
						}
					}                                       //  2008
				{next_label=L_NEXT_MOVE; continue;}
				case L_YES_QUERY:
					return yesQuery(queryState);
				case L_YES_RESPONSE:
				{next_label=yesResponse(queryState, crankInput.userInput); continue;}

				case L_PROMPT_OK:
					gameData.k = 54;                   //  2009
				case L_PROMPT_K:
					gameData.spk = gameData.k;
				case L_PROMPT_SPK:
					rspeak(gameData.spk);
					// ENDFLATTEN }
				case L_NEXT_MOVE:
					gameData.verb = 0;                         //  2012
					gameData.obj = 0;
				case L_USER_INPUT:
				{

					checkhints();                   //  to 2600-2602
					if (isClosed)
					{
						if (gameData.prop[oyster]<0 && toting(oyster))
							pspeak(oyster,1);
						for (int temp_i=1; temp_i<=LAST_OBJECT_INDEX; temp_i++)
						{
							if (toting(temp_i)&& gameData.prop[temp_i]<0)       // 2604
								gameData.prop[temp_i] = -1- gameData.prop[temp_i];
						}
					}
					gameData.wzdark = dark(0);                 //  2605
					if (gameData.knfloc >0 && gameData.knfloc != gameData.loc) gameData.knfloc = 1;
					return new CrankOutput(CrankOutput.Type.getInput, L_PARSE_USER_INPUT);
//				DP("GETIN @loc({0})", loc);
//				getin();
				}

				case L_PARSE_USER_INPUT:
					parseUserInputLine(crankInput.userInput);
					// Fall Through ...
				case L_PROCESS_USER_INPUT:
					if ((gameData.foobar = -gameData.foobar)>0) gameData.foobar = 0;     //  2608
					//  should check here for "magic mode"
					gameData.turns++;
					if (gameData.demo && gameData.turns >=TURNS_IN_A_DEMO_GAME) {next_label=done(1); continue;}
					;      //  to 13000

					if (gameData.verb ==say && wd2!=null) gameData.verb = 0;
					if (gameData.verb ==say)
					{next_label=4090; continue;}
					if (gameData.tally ==0 && gameData.loc >=15 && gameData.loc !=33) gameData.clock1--;
					if (gameData.clock1 ==0)
					{       closing();                      //  to 10000
						{next_label=19999; continue;}
					}
					if (gameData.clock1 <0) gameData.clock2--;
					if (gameData.clock2 ==0)
					{
						caveclose();            //  to 11000
						{next_label=2; continue;} //  back to 2
					}
					if (gameData.prop[lamp]==1) gameData.limit--;
					if (gameData.limit <=30 && here(batter) && gameData.prop[batter]==0
						&& here(lamp))
					{
						rspeak(188);            //  12000
						gameData.prop[batter]=1;
						if (toting(batter)) drop(batter, gameData.loc);
						gameData.limit = gameData.limit + 2500;
						gameData.lmwarn = false;
						{next_label=19999; continue;}
					}
					if (gameData.limit ==0)
					{
						gameData.limit = -1;             //  12400
						gameData.prop[lamp]=0;
						rspeak(184);
						{next_label=19999; continue;}
					}
					if (gameData.limit <0&& gameData.loc <=8)
					{
						rspeak(185);            //  12600
						gameData.gaveup = true;
						{next_label=done(3); continue;}		//  to 20000
					}
					if (gameData.limit <=30)
					{
						if (gameData.lmwarn || !here(lamp)) {next_label=19999; continue;}  // 12200
						gameData.lmwarn = true;
						gameData.spk = 187;
						if (gameData.place[batter]==0) gameData.spk = 183;
						if (gameData.prop[batter]==1) gameData.spk = 189;
						rspeak(gameData.spk);
					}
				case 19999:
					gameData.k = 43;
					if (liqloc(gameData.loc)==water) gameData.k = 70;
					if (weq(wd1,"enter") &&
						(weq(wd2,"strea")||weq(wd2,"water")))
					{next_label=L_PROMPT_K; continue;}
					if (weq(wd1,"enter") && wd2!=null)
					{next_label=2800; continue;}
					if ((!weq(wd1,"water")&&!weq(wd1,"oil"))
						|| (!weq(wd2,"plant")&&!weq(wd2,"door")))
					{next_label=2610; continue;}
					if (at(vocab(wd2,Usage.OBJECT))) wd2 = "pour";

				case 2610:
					if (weq(wd1,"west"))
						if (gameData.iwest++ ==10) rspeak(17);
				case 2630:
				{
					int temp_i = vocab(wd1, Usage.ANY);
					if (temp_i == -1) {
						gameData.spk = 60;                 //  3000
						if (pct(20)) gameData.spk = 61;
						if (pct(20)) gameData.spk = 13;
						rspeak(gameData.spk);
						{
							next_label = L_USER_INPUT;
							continue;
						}
					}
					gameData.k = temp_i % 1000;
					gameData.kq = temp_i / 1000 + 1;
					switch (gameData.kq) {
						case 1: {
							next_label = 8;
							continue;
						}
						case 2: {
							next_label = 5000;
							continue;
						}
						case 3: {
							next_label = 4000;
							continue;
						}
						case 4: {
							next_label = L_PROMPT_K;
							continue;
						}
						default:
							printf("Error 22");
							throw new EmergencyExit();
//						exit(0);
					}
				}
				case L_RESTORED: {
					switch(march())
					{
						case L_AFTER_NAV:
						{next_label=L_AFTER_NAV; continue;} //  back to 2
						case 99:
							switch(die(99))
							{
								case 2000: {next_label=2000; continue;}
								default: bug(111);
							}
						default:
							bug(110);
					}
				}

				case 2800:
					wd1 = wd2;
					wd2=null;
				{next_label=2610; continue;}

				case 4000:
					gameData.verb = gameData.k;
					gameData.spk = actspk[gameData.verb];
					if (wd2!=null && gameData.verb !=say)
					{next_label=2800; continue;}
					if (gameData.verb ==say) gameData.obj = wd2.charAt(0); //TODO::CHECK THIS *wd2;
					if (gameData.obj !=0) {next_label=4090; continue;}
				case 4080:
					switch(gameData.verb)
					{   case 1:                     //  take = 8010
						if (atloc[gameData.loc]==0 || gameData.link[atloc[gameData.loc]] != 0)
						{next_label=L_ASK_WHAT_OBJ; continue;}
						for (int temp_i=1; temp_i<=5; temp_i++)
						{
							if (gameData.dwarfLoc[temp_i]== gameData.loc && gameData.dflag >=2)
							{next_label=L_ASK_WHAT_OBJ; continue;}
						}
						gameData.obj = atloc[gameData.loc];
					{next_label=9010; continue;}
						case 2: case 3: case 9:     //  L_ASK_WHAT_OBJ : drop,say,wave
						case 10: case 16: case 17:  //  calm,rub,toss
						case 19: case 21: case 28:  //  find,feed,break
						case 29:                    //  wake
						{next_label=L_ASK_WHAT_OBJ; continue;}
						case 4: case 6:             //  8040 open,lock
						gameData.spk = 28;
							if (here(clam)) gameData.obj = clam;
							if (here(oyster)) gameData.obj = oyster;
							if (at(door)) gameData.obj = door;
							if (at(grate)) gameData.obj = grate;
							if (gameData.obj !=0 && here(chain)) {next_label=L_ASK_WHAT_OBJ; continue;}
							if (here(chain)) gameData.obj = chain;
							if (gameData.obj ==0) {next_label=L_PROMPT_SPK; continue;}
							else {next_label=9040; continue;}
						case 5: {next_label=L_PROMPT_OK; continue;}         //  nothing
						case 7: {next_label=9070; continue;}         //  on
						case 8: {next_label=9080; continue;}         //  off
						case 11: {next_label=L_ASK_WHAT_OBJ; continue;}        //  walk
						case 12: {next_label=L_TRY_KILL; continue;}        //  kill
						case 13: {next_label=9130; continue;}        //  pour
						case 14:                    //  eat: 8140
							if (!here(food)) {next_label=L_ASK_WHAT_OBJ; continue;}
						{next_label=8142; continue;}
						case 15: {next_label=9150; continue;}        //  drink
						case 18:                    //  quit: 8180
						{next_label=initiateYes(22,54,54, L_CHECK_FOR_QUIT); continue;}
//
						case 20:                    //  invent=8200
							gameData.spk = 98;
							for (int objid=1; objid<=LAST_OBJECT_INDEX; objid++)
							{       if (objid!=bear && toting(objid))
							{       if (gameData.spk ==98) rspeak(99);
								gameData.blklin = false;
								pspeak(objid,-1);
								gameData.blklin = true;
								gameData.spk = 0;
							}
							}
							if (toting(bear)) gameData.spk = 141;
						{next_label=L_PROMPT_SPK; continue;}
						case 22: {next_label=9220; continue;}        //  fill
						case 23: {next_label=9230; continue;}        //  blast
						case 24:                    //  score: 8240
							isScoring =true;
							printf(
								"If you were to quit now, you would score {0} out"
									+" of a possible {1}",
								score(),
								gameData.mxscor
							);
							isScoring =false;
							{next_label=initiateYes(143,54,54, L_CHECK_FOR_REALLY_QUIT); continue;}
						case 25:                    //  foo: 8250
							gameData.k = vocab(wd1, Usage.MAGIC);
							gameData.spk = 42;
							if (gameData.foobar ==1- gameData.k) {next_label=8252; continue;}
							if (gameData.foobar !=0) gameData.spk = 151;
						{next_label=L_PROMPT_SPK; continue;}
						case 26:                    //  brief=8260
							gameData.spk = 156;
							gameData.abbnum = 10000;
							gameData.detail = 3;
						{next_label=L_PROMPT_SPK; continue;}
						case 27:                    //  read=8270
							if (here(magzin)) gameData.obj = magzin;
							if (here(tablet)) gameData.obj = gameData.obj * 100 + tablet;
							if (here(messag)) gameData.obj = gameData.obj * 100 + messag;
							if (isClosed &&toting(oyster)) gameData.obj = oyster;
							if (gameData.obj >FIXED_OBJECT_OFFSET || gameData.obj ==0 || dark(0) )
							{
								{next_label=L_ASK_WHAT_OBJ; continue;}
							}
						{next_label=9270; continue;}
						case 30:                    //  suspend=8300
							gameData.spk = 201;
							if (gameData.demo) {next_label=L_PROMPT_SPK; continue;}
							printf(
								"I can suspend your adventure for you so you "
									+"can resume later, but"
							);
							printf(
								"you will have to wait at least {0} minutes before"
									+" continuing.",
								gameData.latncy
							);
							{next_label=initiateYes(200,54,54, L_CHECK_FOR_ACCEPTABLE); continue;}
						case 31:                    //  hours=8310
							printf(
								"Colossal cave is isClosed 9am-5pm Mon through "
									+"Fri except holidays."
							);
						{next_label=L_NEXT_MOVE; continue;}
						default: bug(23);
					}
				case L_CHECK_FOR_REALLY_QUIT:
					gameData.gaveup = queryState.saidYes();
					if (gameData.gaveup) {next_label=done(2); continue;}
					{next_label=L_NEXT_MOVE; continue;}
				case L_CHECK_FOR_QUIT:
					gameData.gaveup = queryState.saidYes();
					if (gameData.gaveup) {next_label=done(2); continue;}    //  8185
					{next_label=L_NEXT_MOVE; continue;}
				case L_CHECK_FOR_ACCEPTABLE:
					if (!queryState.saidYes()) {next_label=L_NEXT_MOVE; continue;}
					gameData.saved_last_usage = datime();
					// TODO: What do we do here????
//						ciao(path);	          //  Do we quit?
					{next_label= L_AFTER_NAV; continue;} //  Maybe not
				case L_ASK_WHAT_OBJ:
					printf("{0} what?",wd1);
					gameData.obj = 0;
				{next_label=L_USER_INPUT; continue;}
				case 8142:
					dstroy(food);
					gameData.spk = 72;
				{next_label=L_PROMPT_SPK; continue;}
				case 8252:
					gameData.foobar = gameData.k;
					if (gameData.k !=4) {next_label=L_PROMPT_OK; continue;}
					gameData.foobar = 0;
					if (gameData.place[eggs]==plac[eggs]
						||(toting(eggs)&& gameData.loc ==plac[eggs])) {next_label=L_PROMPT_SPK; continue;}
					if (gameData.place[eggs]==0&& gameData.place[troll]==0&& gameData.prop[troll]==0)
						gameData.prop[troll]=1;
					gameData.k = 2;
					if (here(eggs)) gameData.k = 1;
					if (gameData.loc ==plac[eggs]) gameData.k = 0;
					move(eggs,plac[eggs]);
					pspeak(eggs, gameData.k);
				{next_label=L_NEXT_MOVE; continue;}

				case 4090:
					// FLATTEN THIS SWITCH;
					switch(gameData.verb)
					{
						case 1:
						case 2:                     //  drop = 9020
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
						case 8:
						case 9:                     //  off
						case 12:
						case 13:                    //  pour
						case 14:
						case 15:
						case 16:
						case 17:
						case 19:
						case 20:
						case 21:
						case 22:
						case 23:
						case 27:
						case 28:
						case 29:
						{next_label=(9000+ gameData.verb *10); continue;}

						case 10: case 11: case 18:  //  calm, walk, quit
						case 24: case 25: case 26:  //  score, foo, brief
						case 30: case 31:           //  suspend, hours
					{next_label=L_PROMPT_SPK; continue;}
						default:
							bug(24);

					}
				case 9010:	//  take = 9010
					switch(trtake())
					{
						case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
						case 9220: {next_label=9220; continue;}
						case L_PROMPT_OK: {next_label=L_PROMPT_OK; continue;}
						case L_NEXT_MOVE: {next_label=L_NEXT_MOVE; continue;}
						default: bug(102);
					}
				case 9020:	//  drop = 9020
					switch(trdrop())
					{
						case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
						case 19000: {next_label=done(3); continue;}
						case L_NEXT_MOVE: {next_label=L_NEXT_MOVE; continue;}
						default: bug(105);
					}
				case 9030:
					switch(trsay())
					{
						case L_NEXT_MOVE: {next_label=L_NEXT_MOVE; continue;}
						case 2630: {next_label=2630; continue;}
						default: bug(107);
					}
				case 9040: 	//  open, close
				case 9060:
					switch(tropen())
					{   case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
						case L_PROMPT_K: {next_label=L_PROMPT_K; continue;}
						default: bug(106);
					}
				case 9050:	//  nothing
				{next_label=L_PROMPT_OK; continue;}
				case 9070:	//  on   9070
					if (!here(lamp))  {next_label=L_PROMPT_SPK; continue;}
					gameData.spk = 184;
					if (gameData.limit <0) {next_label=L_PROMPT_SPK; continue;}
					gameData.prop[lamp]=1;
					rspeak(39);
					if (gameData.wzdark) {next_label=2000; continue;}
				{next_label=L_NEXT_MOVE; continue;}

				case 9080:	//  off
					if (!here(lamp)) {next_label=L_PROMPT_SPK; continue;}
					gameData.prop[lamp]=0;
					rspeak(40);
					if (dark(0)) rspeak(16);
				{next_label=L_NEXT_MOVE; continue;}

				case 9090:	//  wave
				{
					if ((!toting(gameData.obj))&&(gameData.obj !=rod||!toting(rod2)))
						gameData.spk = 29;
					if (gameData.obj !=rod||!at(fissur)||!toting(gameData.obj)|| isClosing)
					{next_label=L_PROMPT_SPK; continue;}
					gameData.prop[fissur]=1- gameData.prop[fissur];
					pspeak(fissur,2- gameData.prop[fissur]);
					{next_label=L_NEXT_MOVE; continue;}
				}
				case L_TRY_KILL:	//  kill
				{
					int dwarfid;
					for (dwarfid = 1; dwarfid <= 5; dwarfid++)
						if (gameData.dwarfLoc[dwarfid] == gameData.loc && gameData.dflag >= 2) break;
					if (dwarfid == 6) dwarfid = 0;
					if (gameData.obj == 0)                     //  9122
					{
						if (dwarfid != 0) gameData.obj = dwarf;
						if (here(snake)) gameData.obj = gameData.obj * 100 + snake;
						if (at(dragon) && gameData.prop[dragon] == 0)
							gameData.obj = gameData.obj * 100 + dragon;
						if (at(troll)) gameData.obj = gameData.obj * 100 + troll;
						if (here(bear) && gameData.prop[bear] == 0) gameData.obj = gameData.obj * 100 + bear;
						if (gameData.obj > FIXED_OBJECT_OFFSET) {
							next_label = L_ASK_WHAT_OBJ;
							continue;
						}
						if (gameData.obj == 0) {
							if (here(bird) && gameData.verb != vthrow) gameData.obj = bird;
							if (here(clam) || here(oyster)) gameData.obj = 100 * gameData.obj + clam;
							if (gameData.obj > 100) {
								next_label = L_ASK_WHAT_OBJ;
								continue;
							}
						}
					}
					if (gameData.obj == bird)                  //  9124
					{
						gameData.spk = 137;
						if (isClosed) {
							next_label = L_PROMPT_SPK;
							continue;
						}
						dstroy(bird);
						gameData.prop[bird] = 0;
						if (gameData.place[snake] == plac[snake]) gameData.tally2++;
						gameData.spk = 45;
					}
					if (gameData.obj == 0) gameData.spk = 44;             //  9125
					if (gameData.obj == clam || gameData.obj == oyster) gameData.spk = 150;
					if (gameData.obj == snake) gameData.spk = 46;
					if (gameData.obj == dwarf) gameData.spk = 49;
					if (gameData.obj == dwarf && isClosed) {
						next_label = 19000;
						continue;
					}
					if (gameData.obj == dragon) gameData.spk = 147;
					if (gameData.obj == troll) gameData.spk = 157;
					if (gameData.obj == bear) gameData.spk = 165 + (gameData.prop[bear] + 1) / 2;
					if (gameData.obj != dragon || gameData.prop[dragon] != 0) {
						next_label = L_PROMPT_SPK;
						continue;
					}
					rspeak(49);
					gameData.verb = 0;
					gameData.obj = 0;
//				DP("GETIN @loc({0})", loc);
//				getin();
					return new CrankOutput(CrankOutput.Type.getInput, L_TRY_KILL_RESPONSE);
				}
				case L_TRY_KILL_RESPONSE: {
					parseUserInputLine(crankInput.userInput);

					if (!weq(wd1,"y")&&!weq(wd1,"yes")) {next_label=L_PROCESS_USER_INPUT; continue;}
					pspeak(dragon,1);
					gameData.prop[dragon]=2;
					gameData.prop[rug]=0;
					gameData.k = (plac[dragon] + fixd[dragon]) / 2;
					move(dragon+FIXED_OBJECT_OFFSET,-1);
					move(rug+FIXED_OBJECT_OFFSET,0);
					move(dragon, gameData.k);
					move(rug, gameData.k);
					for (gameData.obj = 1; gameData.obj <=LAST_OBJECT_INDEX; gameData.obj++)
						if (gameData.place[gameData.obj]==plac[dragon]|| gameData.place[gameData.obj]==fixd[dragon])
							move(gameData.obj, gameData.k);
					gameData.loc = gameData.k;
					gameData.k = 0;
					{next_label=8; continue;}
				}

				case 9130:	//  pour
					if (gameData.obj ==bottle|| gameData.obj ==0) gameData.obj = liq(0);
					if (gameData.obj ==0) {next_label=L_ASK_WHAT_OBJ; continue;}
					if (!toting(gameData.obj)) {next_label=L_PROMPT_SPK; continue;}
					gameData.spk = 78;
					if (gameData.obj !=oil&& gameData.obj !=water) {next_label=L_PROMPT_SPK; continue;}
					gameData.prop[bottle]=1;
					gameData.place[gameData.obj]=0;
					gameData.spk = 77;
					if (!(at(plant)||at(door))) {next_label=L_PROMPT_SPK; continue;}
					if (at(door))
					{
						gameData.prop[door]=0;   //  9132
						if (gameData.obj ==oil) gameData.prop[door]=1;
						gameData.spk = 113 + gameData.prop[door];
						{next_label=L_PROMPT_SPK; continue;}
					}
					gameData.spk = 112;
					if (gameData.obj !=water) {next_label=L_PROMPT_SPK; continue;}
					pspeak(plant, gameData.prop[plant]+1);
					gameData.prop[plant]=(gameData.prop[plant]+2)% 6;
					gameData.prop[plant2]= gameData.prop[plant]/2;
					gameData.k = 0;
				{next_label=8; continue;}
				case 9140:	//9140 - eat
					if (gameData.obj ==food) {next_label=8142; continue;}
					if (gameData.obj ==bird|| gameData.obj ==snake|| gameData.obj ==clam|| gameData.obj ==oyster
						|| gameData.obj ==dwarf|| gameData.obj ==dragon|| gameData.obj ==troll
						|| gameData.obj ==bear) gameData.spk = 71;
				{next_label=L_PROMPT_SPK; continue;}
				case 9150:	//  9150 - drink
					if (gameData.obj ==0&&liqloc(gameData.loc)!=water&&(liq(0)!=water
						||!here(bottle))) {next_label=L_ASK_WHAT_OBJ; continue;}
					if (gameData.obj !=0&& gameData.obj !=water) gameData.spk = 110;
					if (gameData.spk ==110||liq(0)!=water||!here(bottle))
					{next_label=L_PROMPT_SPK; continue;}
					gameData.prop[bottle]=1;
					gameData.place[water]=0;
					gameData.spk = 74;
				{next_label=L_PROMPT_SPK; continue;}
				case 9160:	//  9160: rub
					if (gameData.obj !=lamp) gameData.spk = 76;
				{next_label=L_PROMPT_SPK; continue;}
				case 9170:	//  9170: throw
					switch(trtoss())
					{
						case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
						case 9020: {next_label=9020; continue;}
						case L_TRY_KILL: {next_label=L_TRY_KILL; continue;}
						case 8: {next_label=8; continue;}
						case 9210: {next_label=9210; continue;}
						default: bug(113);
					}
				case 9190:	//  9190: find
				case 9200:	// , invent
					if (at(gameData.obj)||(liq(0)== gameData.obj &&at(bottle))
						|| gameData.k ==liqloc(gameData.loc)) gameData.spk = 94;
					for (int temp_i=1; temp_i<=5; temp_i++) {
						if (gameData.dwarfLoc[temp_i]== gameData.loc && gameData.dflag >=2&& gameData.obj ==dwarf) gameData.spk = 94;
					}
					if (isClosed) gameData.spk = 138;
					if (toting(gameData.obj)) gameData.spk = 24;
				{next_label=L_PROMPT_SPK; continue;}
				case 9210:	//  feed
					switch(trfeed())
					{   case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
						default: bug(114);
					}
				case 9220:	//  fill
					switch(trfill())
					{   case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
						case L_ASK_WHAT_OBJ: {next_label=L_ASK_WHAT_OBJ; continue;}
						case 9020: {next_label=9020; continue;}
						default: bug(115);
					}
				case 9230:	//  blast
					if (gameData.prop[rod2]<0||!isClosed) {next_label=L_PROMPT_SPK; continue;}
					gameData.bonus = 133;
					if (gameData.loc ==115) gameData.bonus = 134;
					if (here(rod2)) gameData.bonus = 135;
					rspeak(gameData.bonus);
					next_label=done(2);
					continue;
				case 9270:	//  read
					if (dark(0)) {next_label=5190; continue;}
					if (gameData.obj ==magzin) gameData.spk = 190;
					if (gameData.obj ==tablet) gameData.spk = 196;
					if (gameData.obj ==messag) gameData.spk = 191;
					if (gameData.obj ==oyster&& gameData.hinted[2]&&toting(oyster)) gameData.spk = 194;
					if (gameData.obj !=oyster|| gameData.hinted[2]||!toting(oyster)
						||!isClosed) {next_label=L_PROMPT_SPK; continue;}
					{next_label=initiateYes(192,193,54, 9271); continue;}
				case 9271:	//  read
					gameData.hinted[2]=queryState.saidYes();
					{next_label=L_NEXT_MOVE; continue;}
				case 9280:	//  break
					if (gameData.obj ==mirror) gameData.spk = 148;
					if (gameData.obj ==vase&& gameData.prop[vase]==0)
					{
						gameData.spk = 198;
						if (toting(vase)) drop(vase, gameData.loc);
						gameData.prop[vase]=2;
						gameData.fixed[vase]= -1;
						{next_label=L_PROMPT_SPK; continue;}
					}
					if (gameData.obj !=mirror||!isClosed) {next_label=L_PROMPT_SPK; continue;}
					rspeak(197);
					{next_label=done(3); continue;}

				case 9290:	//  wake
					if (gameData.obj !=dwarf||!isClosed) {next_label=L_PROMPT_SPK; continue;}
					rspeak(199);
					{next_label=done(3); continue;}

				// END FLATTEND SWITCH }

				case 5000:
					gameData.obj = gameData.k;
					if (gameData.fixed[gameData.k]!= gameData.loc && !here(gameData.k))
					{
						{next_label=5100; continue;}
					}
				case 5010:
					if (wd2!=null) {next_label=2800; continue;}
					if (gameData.verb !=0) {next_label=4090; continue;}
					printf("What do you want to do with the {0}?",wd1);
				{next_label=L_USER_INPUT; continue;}
				case 5100:
					if (gameData.k !=grate) {next_label=5110; continue;}
					if (gameData.loc ==1|| gameData.loc ==4|| gameData.loc ==7) gameData.k = dprssn;
					if (gameData.loc >9&& gameData.loc <15) gameData.k = entrnc;
					if (gameData.k !=grate) {next_label=8; continue;}
				case 5110:
					if (gameData.k !=dwarf)
					{
						{next_label=5120; continue;}
					}
					for (int temp_i=1; temp_i<=5; temp_i++)
					{
						if (gameData.dwarfLoc[temp_i]== gameData.loc && gameData.dflag >=2)
						{
							{next_label=5010; continue;}
						}
					}
				case 5120:
					if ((liq(0)== gameData.k &&here(bottle))|| gameData.k ==liqloc(gameData.loc)) {next_label=5010; continue;}
					if (gameData.obj !=plant||!at(plant2)|| gameData.prop[plant2]==0) {next_label=5130; continue;}
					gameData.obj = plant2;
				{next_label=5010; continue;}
				case 5130:
					if (gameData.obj !=knife|| gameData.knfloc != gameData.loc) {next_label=5140; continue;}
					gameData.knfloc = -1;
					gameData.spk = 116;
				{next_label=L_PROMPT_SPK; continue;}
				case 5140:
					if (gameData.obj !=rod||!here(rod2)) {next_label=5190; continue;}
					gameData.obj = rod2;
				{next_label=5010; continue;}
				case 5190:
					if ((gameData.verb ==find|| gameData.verb ==invent)&&wd2==null) {next_label=5010; continue;}
					printf("I see no {0} here",wd1);
				{next_label=L_NEXT_MOVE; continue;}
			}
		}
    }
	
	
	// ---------------------------------------------------------------------
	boolean toting(int objj)
	{       
		return gameData.place[objj] == TOTING;
	}
	// ---------------------------------------------------------------------
	boolean here(int objj)
	{       
		return gameData.place[objj]== gameData.loc || toting(objj);
	}
	
	// ---------------------------------------------------------------------
	boolean at(int objj)
	{       
		return gameData.place[objj]== gameData.loc || gameData.fixed[objj]== gameData.loc;
	}
	// ---------------------------------------------------------------------
	int liq2(int pbotl)
	{       
		return((1-pbotl)*water+(pbotl/2)*(water+oil));
	}
	// ---------------------------------------------------------------------
	int liq(int foo)
	{       
		int i= gameData.prop[bottle];
		if (i>-1-i) return(liq2(i));
		else return(liq2(-1-i));
	}
	// ---------------------------------------------------------------------
	int liqloc(int locc)     //  may want to clean this one up a bit 
	{       
		int i=cond[locc]/2;
		int j=((i*2)%8)-5;
		int l=cond[locc]/4;
		l=l%2;
		return(liq2(j*l+1));
	}
	// ---------------------------------------------------------------------
	boolean bitset(int locc, int bitnum)
	{       
		return 0 != (cond[locc] & (1<<bitnum) );
	}
	// ---------------------------------------------------------------------
	boolean forced(int locc)
	{       
		return cond[locc]==2;
	}
	// ---------------------------------------------------------------------
	boolean dark(int foo)
	{       
		return (cond[gameData.loc]%2)==0
				&& (gameData.prop[lamp]==0 || !here(lamp));
	}
	// ---------------------------------------------------------------------
	boolean pct(int n)
	{       
		if (ran(100)<n) return(true);
		return(false);
	}
	// ---------------------------------------------------------------------
	int fdwarf()		//  71 
	{	
		if (gameData.newloc != gameData.loc && !forced(gameData.loc) && !bitset(gameData.loc,3))
		{	
			for (int i=1; i<=5; i++)
			{	
				if (gameData.odloc[i]!= gameData.newloc || !gameData.dwarfSeenAtLoc[i])
					continue;
				gameData.newloc = gameData.loc;
				rspeak(2);
				break;
			}
		}
		gameData.loc = gameData.newloc;			//  74
		if (gameData.loc ==0 || forced(gameData.loc) || bitset(gameData.newloc,3))
			return(2000);
		if (gameData.dflag ==0)
		{	
			if (gameData.loc >=15) gameData.dflag = 1;
			return(2000);
		}
		if (gameData.dflag ==1)		//  6000
		{	
			if (gameData.loc <15||pct(95)) return(2000);
			gameData.dflag = 2;
			for (int i=1; i<=2; i++)
			{	
				int dwarfid = 1+ran(5);
				if (pct(50)&&saved== -1) gameData.dwarfLoc[dwarfid]=0; //  6001
			}
			for (int i=1; i<=5; i++)
			{	
				if (gameData.dwarfLoc[i]== gameData.loc) gameData.dwarfLoc[i]= gameData.daltlc;
				gameData.odloc[i]= gameData.dwarfLoc[i];		//  6002
			}
			rspeak(3);
			drop(axe, gameData.loc);
			return(2000);
		}
		gameData.dtotal = gameData.attack = gameData.stick = 0;			//  6010
		for (int dwarfid=1; dwarfid<=6; dwarfid++)                    /* loop to 6030 */
		{
			if (gameData.dwarfLoc[dwarfid]==0) continue;
			int newdwarfid=1;
			for (TravList travList = getTravel(gameData.dwarfLoc[dwarfid]); travList!=null; travList=travList.next)
			{
				gameData.newloc = travList.tloc;
				if (gameData.newloc >300
					|| gameData.newloc <15
					|| gameData.newloc == gameData.odloc[dwarfid]
					||(newdwarfid>1&& gameData.newloc == gameData.tk[newdwarfid-1])
					||newdwarfid>=20
					|| gameData.newloc == gameData.dwarfLoc[dwarfid]
					||forced(gameData.newloc)
					||(dwarfid==6&&bitset(gameData.newloc,3))
					||travList.conditions==100)
				{
					continue;
				}
				gameData.tk[newdwarfid++]= gameData.newloc;
			}
			gameData.tk[newdwarfid]= gameData.odloc[dwarfid];                 /* 6016 */
			if (newdwarfid>=2) newdwarfid--;
			newdwarfid=1+ran(newdwarfid);
			gameData.odloc[dwarfid]= gameData.dwarfLoc[dwarfid];
			gameData.dwarfLoc[dwarfid]= gameData.tk[newdwarfid];
			gameData.dwarfSeenAtLoc[dwarfid]=(gameData.dwarfSeenAtLoc[dwarfid]&& gameData.loc >=15)||(gameData.dwarfLoc[dwarfid]== gameData.loc || gameData.odloc[dwarfid]== gameData.loc);
			if (!gameData.dwarfSeenAtLoc[dwarfid]) continue;        /* i.e. goto 6030 */
			gameData.dwarfLoc[dwarfid]= gameData.loc;
			if (dwarfid==6)                       /* pirate's spotted him */
			{
				int next_label = 0;
				while(true) 
				{
					switch(next_label)
					{
					case 0:
						if (gameData.loc == gameData.chloc || gameData.prop[chest]>=0) break;
						gameData.k = 0;
						for (newdwarfid=FIRST_TREASURE_INDEX; newdwarfid<=LAST_TREASURE_INDEX; newdwarfid++)      /* loop to 6020 */
						{       
							if (newdwarfid==pyram&&(gameData.loc ==plac[pyram]
								 || gameData.loc ==plac[emrald])) // goto l6020;
							{
							}
							else
							{
								if (toting(newdwarfid)) {next_label = 2; break;};
							}
						//l6020:  
							if (here(newdwarfid)) gameData.k = 1;
							next_label = 1;
						}                              /* 6020 */
						continue;
					case 1: // l6021:  
						if (gameData.tally == gameData.tally2 +1 && gameData.k ==0 && gameData.place[chest]==0
							&&here(lamp) && gameData.prop[lamp]==1) {next_label = 5; continue;};
						if (gameData.odloc[6]!= gameData.dwarfLoc[6]&&pct(20))
							rspeak(127);
						break;       /* to 6030 */
					case 2: // l6022:  
						rspeak(128);
						if (gameData.place[messag]==0) move(chest, gameData.chloc);
						move(messag, gameData.chloc2);
						for (newdwarfid=FIRST_TREASURE_INDEX; newdwarfid<=LAST_TREASURE_INDEX; newdwarfid++)      /* loop to 6023 */
						{       
							if (newdwarfid==pyram && (gameData.loc ==plac[pyram]
								|| gameData.loc ==plac[emrald])) break;
							if (at(newdwarfid)&& gameData.fixed[newdwarfid]==0) carry(newdwarfid, gameData.loc);
							if (toting(newdwarfid)) drop(newdwarfid, gameData.chloc);
						}
					case 4: // l6024:  
						gameData.dwarfLoc[6]= gameData.odloc[6]= gameData.chloc;
						gameData.dwarfSeenAtLoc[6]=false;
						break;
					case 5: // l6025:  
						rspeak(186);
						move(chest, gameData.chloc);
						move(messag, gameData.chloc2);
						{next_label = 4; continue;}
						//goto l6024;
					}
					break;
				}
				continue;
			}
			gameData.dtotal++;                       /* 6027 */
			if (gameData.odloc[dwarfid]!= gameData.dwarfLoc[dwarfid]) continue;
			gameData.attack++;
			if (gameData.knfloc >=0) gameData.knfloc = gameData.loc;
			if (ran(1000)<95*(gameData.dflag -2)) gameData.stick++;
		}                                       /* 6030 */
		if (gameData.dtotal ==0) return(2000);
		if (gameData.dtotal !=1)
		{       
			printf(
				"There are {0} threatening little dwarves "
				+"in the room with you.",
				gameData.dtotal
			);
		}
		else
			rspeak(4);
		if (gameData.attack ==0) return(2000);
		if (gameData.dflag ==2) gameData.dflag = 3;
		if (saved!= -1) gameData.dflag = 20;
		if (gameData.attack !=1)
		{       
			printf("{0} of them throw knives at you!", gameData.attack);
			gameData.k = 6;
		}
		else
		{
			rspeak(5);
			gameData.k = 52;
		}
		if (gameData.stick <=1)                   //  82
		{       
			rspeak(gameData.k + gameData.stick);
			if (gameData.stick ==0) return(2000);
		}
		else
			printf("{0} of them get you!", gameData.stick);  //  83
		gameData.oldlc2 = gameData.loc;
		return(99);
	}
	// ---------------------------------------------------------------------
	//  label 8
	int march()
	{
		TravList tkk = getTravel(gameData.newloc = gameData.loc);
		if (tkk==null)
			bug(26);
		if (gameData.k ==0)
			return(L_AFTER_NAV);
		if (gameData.k ==cave)                            //  40
		{       
			if (gameData.loc <8) rspeak(57);
			if (gameData.loc >=8) rspeak(58);
			return(L_AFTER_NAV);
		}
		if (gameData.k ==look)                            //  30
		{       
			if (gameData.detail++ <3) rspeak(15);
			gameData.wzdark = false;
			abb[gameData.loc]=0;
			return(L_AFTER_NAV);
		}
		if (gameData.k ==back)                            //  20
		{
			tkk = mback(tkk);
			if(tkk == null) {
				return L_AFTER_NAV;
			}
//			switch(mback(tkk))
//			{
//				case L_AFTER_NAV: return(L_AFTER_NAV);
//				case 9: break; // goto l9;
//				default: bug(100);
//			}
		}
		else
		{
			gameData.oldlc2 = gameData.oldloc;
			gameData.oldloc = gameData.loc;
		}
	//l9:
		for (; tkk!=null; tkk=tkk.next)
		{
			if (tkk.tverb==1 || tkk.tverb== gameData.k)
				break;
		}
		if (tkk==null)
		{       
			badmove();
			return(L_AFTER_NAV);
		}
	//l11:    
		while (true)
		{
			int conditionloc=tkk.conditions;                    //  11
			int tloc=tkk.tloc;
			gameData.newloc = conditionloc;                             //  newloc=conditions
			gameData.k = gameData.newloc % 100;                           //  k used for prob
			int next_label  = 0;
			while(true)
			{
				switch(next_label)
				{
				case 0:
					if(!(gameData.newloc <=300)) { next_label = 3; continue;}
					if(!(gameData.newloc <=100)) { next_label = 2; continue;}    //  13
					if(gameData.newloc !=0 && !pct(gameData.newloc)) break; // goto l12;  //  14
				case 1: //		l16:    
					gameData.newloc = tloc;             //  newloc=location
					if (gameData.newloc <=300)
						return(L_AFTER_NAV);
					if (gameData.newloc <=500)
					{
						int spec = specials();
						if(spec == 2 )
						{
							return L_AFTER_NAV;
						}
						else if(spec == 12)
						{
							break;  // goto L12;
						}
						else if(spec == 99)
						{
							return 99;
						}
						else
						{
							bug(101);
						}
					}
					rspeak(gameData.newloc -500);
					gameData.newloc = gameData.loc;
					return(L_AFTER_NAV);
				case 2:
					if (toting(gameData.k)||(gameData.newloc >200&&at(gameData.k)))
							{ next_label = 1; continue;} // goto l16;
					break; // goto l12;
				case 3:
					if (gameData.prop[gameData.k]!=(gameData.newloc /100)-3) //  newloc still conditions
						{ next_label = 1; continue;} // goto l16;
					break;
				}
				break;  // fall thru to L12:
			}
		// l12:    //  alternative to probability move      
			for (; tkk!=null; tkk=tkk.next)
			{
				if (tkk.tloc!=tloc || tkk.conditions!=conditionloc)
					break;
			}
			if (tkk==null)
				bug(25);
			//goto l11;
		}
	}

	// ---------------------------------------------------------------------
	public CrankOutput yesQuery(QueryState state) {
		if(state.isrdata) {
			rspeak(state.question);     //  tell him what we want
		} else {
			mspeak(state.question);     //  tell him what we want
		}
		// Exit and re-enter at L_YES_RESPONSE
		return new CrankOutput(CrankOutput.Type.getInput, L_YES_RESPONSE);
	}
	// ---------------------------------------------------------------------
	public int yesResponse(QueryState state, String input) {
		int message;
		if(input.equals("y") || input.equals("yes")) {
			state.response = true;
			message = state.true_response;
		} else if(input.equals("n") || input.equals("no")) {
			state.response = false;
			message = state.false_response;
		}  else {
			printf("Please answer the question.");
			return L_YES_QUERY;
		}
		if(state.isrdata) {
			rspeak(message);     //  tell him what we want
		} else {
			mspeak(message);     //  tell him what we want
		}
		return state.next_label;
	}
	// ---------------------------------------------------------------------
	public int initiateYes(
		int question,
		int true_response,
		int false_response,
		int next_label)
	{
		return initiateQuery(true, question, true_response, false_response, next_label);
	}
	// ---------------------------------------------------------------------
	public int initiateMYes(
		int question,
		int true_response,
		int false_response,
		int next_label)
	{
		return initiateQuery(false, question, true_response, false_response, next_label);
	}
	// ---------------------------------------------------------------------
	private  int initiateQuery(
		boolean isrdata,
		int question,
		int true_response,
		int false_response,
		int next_label)
	{
		queryState = new QueryState(isrdata, question, true_response, false_response, next_label);
		return L_YES_QUERY;
	}

	// ---------------------------------------------------------------------
	TravList mback(TravList tkk)                                         //  20
	{       
		if (forced(gameData.k = gameData.oldloc)) gameData.k = gameData.oldlc2;         //  k=location
		gameData.oldlc2 = gameData.oldloc;
		gameData.oldloc = gameData.loc;
		TravList tk2=null;
		if (gameData.k == gameData.loc)
		{       
			rspeak(91);
			return(null);
		}
		for (; tkk!=null; tkk=tkk.next)           //  21                   
		{
			int tloc=tkk.tloc;
			if (tloc== gameData.k)
			{
				gameData.k = tkk.tverb;           //  k back to verb
				tkk=getTravel(gameData.loc);
				return(tkk);
			}
			if (tloc<=300)
			{
				TravList jjj = getTravel(gameData.loc);
				if (forced(tloc) && gameData.k ==jjj.tloc) tk2=tkk;
			}
		}
		tkk=tk2;                                //  23                   
		if (tkk!=null)
		{
			gameData.k = tkk.tverb;
			tkk=getTravel(gameData.loc);
			return(tkk);
		}
		rspeak(140);
		return(null);
	}
	// ---------------------------------------------------------------------
	int specials()                                      //  30000                
	{
		switch(gameData.newloc = gameData.newloc - 300)
		{
			case 1:                             //  30100                
				gameData.newloc = 99 + 100 - gameData.loc;
				if (gameData.holdng ==0||(gameData.holdng ==1&&toting(emrald))) return(2);
				gameData.newloc = gameData.loc;
				rspeak(117);
				return(2);
			case 2:                             //  30200                
				drop(emrald, gameData.loc);
				return(12);
			case 3:                             //  to 30300             
				return(trbridge());
			default: bug(29);
		}
		return 0;
	}
	// ---------------------------------------------------------------------
	int trbridge()                                      //  30300                
	{       
		if (gameData.prop[troll]==1)
		{
			pspeak(troll,1);
			gameData.prop[troll]=0;
			move(troll2,0);
			move(troll2+FIXED_OBJECT_OFFSET,0);
			move(troll,plac[troll]);
			move(troll+FIXED_OBJECT_OFFSET,fixd[troll]);
			juggle(chasm);
			gameData.newloc = gameData.loc;
			return(2);
		}
		gameData.newloc = plac[troll] + fixd[troll] - gameData.loc;     //  30310
		if (gameData.prop[troll]==0) gameData.prop[troll]=1;
		if (!toting(bear)) return(2);
		rspeak(162);
		gameData.prop[chasm]=1;
		gameData.prop[troll]=2;
		drop(bear, gameData.newloc);
		gameData.fixed[bear] = -1;
		gameData.prop[bear]=3;
		if (gameData.prop[spices]<0) gameData.tally2++;
		gameData.oldlc2 = gameData.newloc;
		return(99);
	}
	// ---------------------------------------------------------------------
	int badmove()                                       //  20                   
	{
		gameData.spk = 12;
		if (gameData.k >=43 && gameData.k <=50) gameData.spk = 9;
		if (gameData.k ==29|| gameData.k ==30) gameData.spk = 9;
		if (gameData.k ==7|| gameData.k ==36|| gameData.k ==37) gameData.spk = 10;
		if (gameData.k ==11|| gameData.k ==19) gameData.spk = 11;
		if (gameData.verb ==find|| gameData.verb ==invent) gameData.spk = 59;
		if (gameData.k ==62|| gameData.k ==65) gameData.spk = 42;
		if (gameData.k ==17) gameData.spk = 80;
		rspeak(gameData.spk);
		return(2);
	}
	// ---------------------------------------------------------------------
	void bug(int n)
	{
		throw new EmergencyExit(
			"Please tell jim@rand.org that fatal bug "+n+" happened."
		);
	}
	// ---------------------------------------------------------------------
	void checkhints()                                    //  2600 &c
	{       
		for (int hint=4; hint<=hntmax; hint++)
		{       
			if (gameData.hinted[hint])
				continue;
			if (!bitset(gameData.loc,hint))
				hintlc[hint]= -1;
			hintlc[hint]++;
			if (hintlc[hint] < getHint(hint, 1) )
				continue;
			boolean goto_140010 = true;
			
			switch(hint)
			{   
				case 4:     //  40400 
					if (gameData.prop[grate]==0 && !here(keys)) break;
					goto_140010 = false; break;
				case 5:     //  40500 
					if (here(bird)&&toting(rod)&& gameData.obj ==bird) break;
					continue;      //  i.e. goto l40030 
				case 6:     //  40600 
					if (here(snake)&&!here(bird)) break;
					goto_140010 = false; break;
				case 7:     //  40700 
					if (atloc[gameData.loc]==0
						&& atloc[gameData.oldloc]==0
						&& atloc[gameData.oldlc2]==0
						&& gameData.holdng >1) break;
					goto_140010 = false; break;
				case 8:     //  40800 
					if (gameData.prop[emrald]!= -1&& gameData.prop[pyram]== -1) break;
					goto_140010 = false; break;
				case 9:
					break;    //  40900 
				default: bug(27);
			}
			if( goto_140010 )
			{
				// l40010: Goto Target
				hintlc[hint]=0;
				if (!yes( getHint(hint, 3), 0, 54)) continue;
				printf(
					"I am prepared to give you a hint, but it will "
					+ "cost you {0} points.",
					getHint(hint, 2)
				);
				gameData.hinted[hint]=yes(175, getHint(hint, 4),54);
			}
			// l40020: (falls through)
			hintlc[hint]=0;
		}
	}
	// ---------------------------------------------------------------------
	int trsay()                                         //  9030                 
	{   
		int i;
		if (wd2!=null ) wd1 = wd2;
		i=vocab(wd1, Usage.ANY);
		if (i==62||i==65||i==71||i==2025)
		{
			wd2=null;
			gameData.obj = 0;
			return(2630);
		}
		printf("Okay, \"{0}\".",wd2);
		return(L_NEXT_MOVE);
	}
	// ---------------------------------------------------------------------
	int trtake()                                        //  9010                 
	{   
		int i;
		if (toting(gameData.obj)) return(L_PROMPT_SPK);  //  9010
		gameData.spk = 25;
		if (gameData.obj ==plant&& gameData.prop[plant]<=0) gameData.spk = 115;
		if (gameData.obj ==bear&& gameData.prop[bear]==1) gameData.spk = 169;
		if (gameData.obj ==chain&& gameData.prop[bear]!=0) gameData.spk = 170;
		if (gameData.fixed[gameData.obj]!=0) return(L_PROMPT_SPK);
		if (gameData.obj ==water|| gameData.obj ==oil)
		{       
			if (here(bottle)&&liq(0)== gameData.obj)
			{
				gameData.obj = bottle;
				//goto l9017;
			}
			else
			{
				gameData.obj = bottle;
				if (toting(bottle)&& gameData.prop[bottle]==1)
					return(9220);
				if (gameData.prop[bottle]!=1) gameData.spk = 105;
				if (!toting(bottle)) gameData.spk = 104;
				return(L_PROMPT_SPK);
			}
		}
	//l9017:
		if (gameData.holdng >=7)
		{       
			rspeak(92);
			return(L_NEXT_MOVE);
		}
		if (gameData.obj ==bird)
		{       
			if (gameData.prop[bird]==0)	//  goto l9014;
			{
				if (toting(rod))
				{       
					rspeak(26);
					return(L_NEXT_MOVE);
				}
				if (!toting(cage))      //  9013 
				{       
					rspeak(27);
					return(L_NEXT_MOVE);
				}
				gameData.prop[bird]=1;           //  9015
			}
		}
//	l9014:
		if ((gameData.obj ==bird|| gameData.obj ==cage)&& gameData.prop[bird]!=0)
			carry(bird+cage- gameData.obj, gameData.loc);
		carry(gameData.obj, gameData.loc);
		gameData.k = liq(0);
		if (gameData.obj ==bottle && gameData.k !=0) gameData.place[gameData.k] = -1;
		return(L_PROMPT_OK);
	}
	// ---------------------------------------------------------------------
	int dropper()                                       //  9021                 
	{
		gameData.k = liq(0);
		if (gameData.k == gameData.obj) gameData.obj = bottle;
		if (gameData.obj ==bottle&& gameData.k !=0) gameData.place[gameData.k]=0;
		if (gameData.obj ==cage&& gameData.prop[bird]!=0) drop(bird, gameData.loc);
		if (gameData.obj ==bird) gameData.prop[bird]=0;
		drop(gameData.obj, gameData.loc);
		return(L_NEXT_MOVE);
	}
	// ---------------------------------------------------------------------
	int trdrop()                                        //  9020                 
	{
		if (toting(rod2)&& gameData.obj ==rod&&!toting(rod)) gameData.obj = rod2;
		if (!toting(gameData.obj)) return(L_PROMPT_SPK);
		if (gameData.obj ==bird&&here(snake))
		{       
			rspeak(30);
			if (isClosed) return(19000);
			dstroy(snake);
			gameData.prop[snake]=1;
			return(dropper());
		}
		if (gameData.obj ==coins&&here(vend))             //  9024
		{       
			dstroy(coins);
			drop(batter, gameData.loc);
			pspeak(batter,0);
			return(L_NEXT_MOVE);
		}
		if (gameData.obj ==bird&&at(dragon)&& gameData.prop[dragon]==0)     //  9025
		{       
			rspeak(154);
			dstroy(bird);
			gameData.prop[bird]=0;
			if (gameData.place[snake]==plac[snake]) gameData.tally2--;
			return(L_NEXT_MOVE);
		}
		if (gameData.obj ==bear&&at(troll))               //  9026
		{       
			rspeak(163);
			move(troll,0);
			move(troll+100,0);
			move(troll2,plac[troll]);
			move(troll2+100,fixd[troll]);
			juggle(chasm);
			gameData.prop[troll]=2;
			return(dropper());
		}
		if (gameData.obj !=vase|| gameData.loc ==plac[pillow])       //  9027
		{       
			rspeak(54);
			return(dropper());
		}
		gameData.prop[vase]=2;                           //  9028
		if (at(pillow)) gameData.prop[vase]=0;
		pspeak(vase, gameData.prop[vase]+1);
		if (gameData.prop[vase]!=0) gameData.fixed[vase] = -1;
		return(dropper());
	}
	// ---------------------------------------------------------------------
	int tropen()                                        //  9040                 
	{       
		if (gameData.obj ==clam|| gameData.obj ==oyster)
		{
			gameData.k = 0;                            //  9046
			if (gameData.obj ==oyster) gameData.k = 1;
			gameData.spk = 124 + gameData.k;
			if (toting(gameData.obj)) gameData.spk = 120 + gameData.k;
			if (!toting(tridnt)) gameData.spk = 122 + gameData.k;
			if (gameData.verb ==lock) gameData.spk = 61;
			if (gameData.spk !=124) return(L_PROMPT_SPK);
			dstroy(clam);
			drop(oyster, gameData.loc);
			drop(pearl,105);
			return(L_PROMPT_SPK);
		}
		if (gameData.obj ==door) gameData.spk = 111;
		if (gameData.obj ==door&& gameData.prop[door]==1) gameData.spk = 54;
		if (gameData.obj ==cage) gameData.spk = 32;
		if (gameData.obj ==keys) gameData.spk = 55;
		if (gameData.obj ==grate|| gameData.obj ==chain) gameData.spk = 31;
		if (gameData.spk !=31||!here(keys)) return(L_PROMPT_SPK);
		if (gameData.obj ==chain)
		{       
			if (gameData.verb ==lock)
			{
				gameData.spk = 172;                //  9049: lock
				if (gameData.prop[chain]!=0) gameData.spk = 34;
				if (gameData.loc !=plac[chain]) gameData.spk = 173;
				if (gameData.spk !=172) return(L_PROMPT_SPK);
				gameData.prop[chain]=2;
				if (toting(chain)) drop(chain, gameData.loc);
				gameData.fixed[chain]= -1;
				return(L_PROMPT_SPK);
			}
			gameData.spk = 171;
			if (gameData.prop[bear]==0) gameData.spk = 41;
			if (gameData.prop[chain]==0) gameData.spk = 37;
			if (gameData.spk !=171) return(L_PROMPT_SPK);
			gameData.prop[chain]=0;
			gameData.fixed[chain]=0;
			if (gameData.prop[bear]!=3) gameData.prop[bear]=2;
			gameData.fixed[bear]=2- gameData.prop[bear];
			return(L_PROMPT_SPK);
		}
		if (isClosing)
		{
			gameData.k = 130;
			if (!gameData.panic) gameData.clock2 = 15;
			gameData.panic = true;
			return(L_PROMPT_K);
		}
		gameData.k = 34 + gameData.prop[grate];                       //  9043
		gameData.prop[grate]=1;
		if (gameData.verb ==lock) gameData.prop[grate]=0;
		gameData.k = gameData.k + 2 * gameData.prop[grate];
		return(L_PROMPT_K);
	}
//	// ---------------------------------------------------------------------
//	int trkill()                                //  L_TRY_KILL
//	{
//		int dwarfid;
//		for (dwarfid=1; dwarfid<=5; dwarfid++)
//			if (dwarfLoc[dwarfid]==loc&&dflag>=2) break;
//		if (dwarfid==6) dwarfid=0;
//		if (obj==0)                     //  9122
//		{
//			if (dwarfid!=0) obj=dwarf;
//			if (here(snake)) obj=obj*100+snake;
//			if (at(dragon)&&prop[dragon]==0) obj=obj*100+dragon;
//			if (at(troll)) obj=obj*100+troll;
//			if (here(bear)&&prop[bear]==0) obj=obj*100+bear;
//			if (obj>FIXED_OBJECT_OFFSET) return(L_ASK_WHAT_OBJ);
//			if (obj==0)
//			{       if (here(bird)&&verb!=vthrow) obj=bird;
//				if (here(clam)||here(oyster)) obj=100*obj+clam;
//				if (obj>100) return(L_ASK_WHAT_OBJ);
//			}
//		}
//		if (obj==bird)                  //  9124
//		{
//			spk=137;
//			if (isClosed) return(L_PROMPT_SPK);
//			dstroy(bird);
//			prop[bird]=0;
//			if (place[snake]==plac[snake]) tally2++;
//			spk=45;
//		}
//		if (obj==0) spk=44;             //  9125
//		if (obj==clam||obj==oyster) spk=150;
//		if (obj==snake) spk=46;
//		if (obj==dwarf) spk=49;
//		if (obj==dwarf&& isClosed) return(19000);
//		if (obj==dragon) spk=147;
//		if (obj==troll) spk=157;
//		if (obj==bear) spk=165+(prop[bear]+1)/2;
//		if (obj!=dragon||prop[dragon]!=0) return(L_PROMPT_SPK);
//		rspeak(49);
//		verb=0;
//		obj=0;
//
//		DP("GETIN @loc({0})", loc);
//		getin();
//		if (!weq(wd1,"y")&&!weq(wd1,"yes")) return(L_PROCESS_USER_INPUT);
//		pspeak(dragon,1);
//		prop[dragon]=2;
//		prop[rug]=0;
//		k=(plac[dragon]+fixd[dragon])/2;
//		move(dragon+FIXED_OBJECT_OFFSET,-1);
//		move(rug+FIXED_OBJECT_OFFSET,0);
//		move(dragon,k);
//		move(rug,k);
//		for (obj=1; obj<=LAST_OBJECT_INDEX; obj++)
//			if (place[obj]==plac[dragon]||place[obj]==fixd[dragon])
//				move(obj,k);
//		loc=k;
//		k=0;
//		return(8);
//	}

	// ---------------------------------------------------------------------
	int trtoss_exit( int spk, int axe, int loc )
	{
		// l9175:  
		rspeak(spk);
		drop(axe,loc);
		gameData.k = 0;
		return(8);
	}

	// ---------------------------------------------------------------------
	int trtoss()                                //  9170: throw
	{   
		int i;
		if (toting(rod2)&& gameData.obj ==rod&&!toting(rod)) gameData.obj = rod2;
		if (!toting(gameData.obj)) return(L_PROMPT_SPK);
		if (gameData.obj >=FIRST_TREASURE_INDEX && gameData.obj <=LAST_TREASURE_INDEX && at(troll))
		{
			gameData.spk = 159;                        //  9178
			drop(gameData.obj,0);
			move(troll,0);
			move(troll+100,0);
			drop(troll2,plac[troll]);
			drop(troll2+100,fixd[troll]);
			juggle(chasm);
			return(L_PROMPT_SPK);
		}
		if (gameData.obj ==food&&here(bear))
		{
			gameData.obj = bear;                       //  9177
			return(9210);
		}
		if (gameData.obj !=axe) return(9020);
		for (i=1; i<=5; i++)
		{       
			if (gameData.dwarfLoc[i]== gameData.loc)
			{
				gameData.spk = 48;                 //  9172
				if (ran(3)==0||saved!= -1)
					return trtoss_exit(gameData.spk, axe, gameData.loc);
				gameData.dwarfSeenAtLoc[i]=false;
				gameData.dwarfLoc[i]=0;
				gameData.spk = 47;
				gameData.dkill++;
				if (gameData.dkill ==1) gameData.spk = 149;
				return trtoss_exit(gameData.spk, axe, gameData.loc);
				//goto l9175;
			}
		}
		gameData.spk = 152;
		if (at(dragon)&& gameData.prop[dragon]==0)
			return trtoss_exit(gameData.spk, axe, gameData.loc);
			//goto l9175;
		gameData.spk = 158;
		if (at(troll)) 
			return trtoss_exit(gameData.spk, axe, gameData.loc);
			// goto l9175;
		if (here(bear)&& gameData.prop[bear]==0)
		{
			gameData.spk = 164;
			drop(axe, gameData.loc);
			gameData.fixed[axe]= -1;
			gameData.prop[axe]=1;
			juggle(bear);
			return(L_PROMPT_SPK);
		}
		gameData.obj = 0;
		return(L_TRY_KILL);
	}

	// ---------------------------------------------------------------------
	int trfeed()                                        //  9210                 
	{       
		if (gameData.obj ==bird)
		{
			gameData.spk = 100;
			return(L_PROMPT_SPK);
		}
		if (gameData.obj ==snake|| gameData.obj ==dragon|| gameData.obj ==troll)
		{
			gameData.spk = 102;
			if (gameData.obj ==dragon&& gameData.prop[dragon]!=0) gameData.spk = 110;
			if (gameData.obj ==troll) gameData.spk = 182;
			if (gameData.obj !=snake|| isClosed ||!here(bird)) return(L_PROMPT_SPK);
			gameData.spk = 101;
			dstroy(bird);
			gameData.prop[bird]=0;
			gameData.tally2++;
			return(L_PROMPT_SPK);
		}
		if (gameData.obj ==dwarf)
		{       
			if (!here(food)) return(L_PROMPT_SPK);
			gameData.spk = 103;
			gameData.dflag++;
			return(L_PROMPT_SPK);
		}
		if (gameData.obj ==bear)
		{       
			if (gameData.prop[bear]==0) gameData.spk = 102;
			if (gameData.prop[bear]==3) gameData.spk = 110;
			if (!here(food)) return(L_PROMPT_SPK);
			dstroy(food);
			gameData.prop[bear]=1;
			gameData.fixed[axe]=0;
			gameData.prop[axe]=0;
			gameData.spk = 168;
			return(L_PROMPT_SPK);
		}
		gameData.spk = 14;
		return(L_PROMPT_SPK);
	}

	// ---------------------------------------------------------------------
	int trfill()                                        //  9220 
	{       
		if (gameData.obj ==vase)
		{
			gameData.spk = 29;
			if (liqloc(gameData.loc)==0) gameData.spk = 144;
			if (liqloc(gameData.loc)==0||!toting(vase)) return(L_PROMPT_SPK);
			rspeak(145);
			gameData.prop[vase]=2;
			gameData.fixed[vase]= -1;
			return(9020);           //  advent/10 goes to 9024 
		}
		if (gameData.obj !=0&& gameData.obj !=bottle) return(L_PROMPT_SPK);
		if (gameData.obj ==0&&!here(bottle)) return(L_ASK_WHAT_OBJ);
		gameData.spk = 107;
		if (liqloc(gameData.loc)==0) gameData.spk = 106;
		if (liq(0)!=0) gameData.spk = 105;
		if (gameData.spk !=107) return(L_PROMPT_SPK);
		gameData.prop[bottle]=((cond[gameData.loc]%4)/2)*2;
		gameData.k = liq(0);
		if (toting(bottle)) gameData.place[gameData.k]= -1;
		if (gameData.k ==oil) gameData.spk = 108;
		return(L_PROMPT_SPK);
	}

	// ---------------------------------------------------------------------
	int closing()                               //  10000 
	{       
		int i;

		gameData.prop[grate]= gameData.prop[fissur]=0;
		for (i=1; i<=6; i++)
		{
			gameData.dwarfSeenAtLoc[i]=false;
			gameData.dwarfLoc[i]=0;
		}
		move(troll,0);
		move(troll+FIXED_OBJECT_OFFSET,0);
		move(troll2,plac[troll]);
		move(troll2+FIXED_OBJECT_OFFSET,fixd[troll]);
		juggle(chasm);
		if(gameData.prop[bear]!=3) dstroy(bear);
		gameData.prop[chain]=0;
		gameData.fixed[chain]=0;
		gameData.prop[axe]=0;
		gameData.fixed[axe]=0;
		rspeak(129);
		gameData.clock1 = -1;
		isClosing = true;
		return(19999);
	}

	// ---------------------------------------------------------------------
	int caveclose()                             //  11000 
	{       
		int i;
		gameData.prop[bottle]=put(bottle,115,1);
		gameData.prop[plant]=put(plant,115,0);
		gameData.prop[oyster]=put(oyster,115,0);
		gameData.prop[lamp]=put(lamp,115,0);
		gameData.prop[rod]=put(rod,115,0);
		gameData.prop[dwarf]=put(dwarf,115,0);
		gameData.loc = 115;
		gameData.oldloc = 115;
		gameData.newloc = 115;
	
		put(grate,116,0);
		gameData.prop[snake]=put(snake,116,1);
		gameData.prop[bird]=put(bird,116,1);
		gameData.prop[cage]=put(cage,116,0);
		gameData.prop[rod2]=put(rod2,116,0);
		gameData.prop[pillow]=put(pillow,116,0);

		gameData.prop[mirror]=put(mirror,115,0);
		gameData.fixed[mirror]=116;
	
		for (i=1; i<=FIXED_OBJECT_OFFSET; i++)
			if (toting(i)) dstroy(i);
		rspeak(132);
		isClosed = true;
		return(2);
	}

	// ---------------------------------------------------------------------
	int score()   //  sort of like 20000   
	{       
		int scor,i;
		gameData.mxscor = scor = 0;
		for (i=FIRST_TREASURE_INDEX; i<=LAST_TREASURE_INDEX; i++)
		{	
			if (! hasPText(i) ) continue;
			gameData.k = 12;
			if (i==chest) gameData.k = 14;
			if (i>chest) gameData.k = 16;
			if (gameData.prop[i]>=0) scor += 2;
			if (gameData.place[i]==3&& gameData.prop[i]==0) scor += gameData.k -2;
			gameData.mxscor = gameData.mxscor + gameData.k;
		}
		scor += (gameData.maxdie - gameData.numdie)*10;
		gameData.mxscor = gameData.mxscor + gameData.maxdie * 10;
		if (!(isScoring || gameData.gaveup)) scor += 4;
		gameData.mxscor = gameData.mxscor + 4;
		if (gameData.dflag !=0) scor += 25;
		gameData.mxscor = gameData.mxscor + 25;
		if (isClosing) scor += 25;
		gameData.mxscor = gameData.mxscor + 25;
		if (isClosed)
		{       if (gameData.bonus ==0) scor += 10;
			if (gameData.bonus ==135) scor += 25;
			if (gameData.bonus ==134) scor += 30;
			if (gameData.bonus ==133) scor += 45;
		}
		gameData.mxscor = gameData.mxscor + 45;
		if (gameData.place[magzin]==108) scor++;
		gameData.mxscor++;
		scor += 2;
		gameData.mxscor = gameData.mxscor + 2;
		for (i=1; i<=hntmax; i++)
			if (gameData.hinted[i]) scor -= getHint(i, 2);
		return(scor);
	}

	// ---------------------------------------------------------------------
	//  entry=1 means goto 13000 */  /* game is over         
	//  entry=2 means goto 20000 */ /* 3=19000 
	int done(int entry)
	{   
		int score = score();
		if (entry==1) mspeak(1);
		if (entry==3) rspeak(136);
		printf(
			"You scored {0} out of a possible {1} using {2} turns.",
			score,
			gameData.mxscor,
			gameData.turns
		);
		printf("");
		ClassMessage info = null;
		int index = 0;
		
		
		while( null != (info = getLevelInfo( index++ ) ) )
		{
			if( info.max_score < score )
				continue;
			// Found the player's level
			printf( info.message );
			ClassMessage next_level = getLevelInfo( index );
			if( next_level == null )
			{
				printf("To achieve the next higher rating would be a neat trick!");
				printf("Congratulations!!");
			}
			else
			{
				int diff = info.max_score+1-score;
				printf(
					"To achieve the next higher rating, you need {0} more point{1}"
					,diff
					,(diff==1) ? "." : "s."
				);
			}
		}
		return L_EXIT_GAME;
	}

	// ---------------------------------------------------------------------
	//  label 90             
	int die(int entry)
	{
		if (entry != 99)
		{
			rspeak(23);
			gameData.oldlc2 = gameData.loc;
		}
		if (isClosing)                             //  99
		{
			rspeak(131);
			gameData.numdie++;
			return done(2);
		}

		return initiateYes(81+ gameData.numdie *2,82+ gameData.numdie *2,54, L_RESURRECT_RESPONSE);

		// Response processing is at label L_RESURRECT_RESPONSE
	}

	// ======================================================================
	// Stuff from vocab.c
	// ======================================================================
	void dstroy(int object)
	{       
		move(object,0);
	}
	
	void juggle(int object)
	{       
		int i,j;
	
		i= gameData.place[object];
		j= gameData.fixed[object];
		move(object,i);
		move(object+FIXED_OBJECT_OFFSET,j);
	}
	
	
	void move(int object, int where)
	{       
		int from;
	
		if (object<=FIXED_OBJECT_OFFSET)
			from= gameData.place[object];
		else
			from= gameData.fixed[object-FIXED_OBJECT_OFFSET];
		if (from>0 && from<=300) carry(object,from);
		drop(object,where);
	}
	
	
	int put(int object,int where,int pval)
	{       
		move(object,where);
		return(-1-pval);
	}
	
	void carry(int object, int where)
	{       
		int temp;
	
		if (object<=FIXED_OBJECT_OFFSET)
		{       
			if (gameData.place[object]== -1) return;
			gameData.place[object] = -1;
			gameData.holdng++;
		}
		if (atloc[where]==object)
		{       
			atloc[where]= gameData.link[object];
			return;
		}
		for (temp=atloc[where]; gameData.link[temp]!=object; temp= gameData.link[temp]);
		gameData.link[temp]= gameData.link[object];
	}
	
	
	void drop(int object, int where)
	{	
		if (object>FIXED_OBJECT_OFFSET)
			gameData.fixed[object-FIXED_OBJECT_OFFSET]=where;
		else
		{       
			if (gameData.place[object]== -1) gameData.holdng--;
			gameData.place[object]=where;
		}
		if (where<=0) 
			return;
		gameData.link[object]=atloc[where];
		atloc[where]=object;
	}
	// ======================================================================
	// End of Stuff from vocab.c
	// ======================================================================

	
	
	// ---------------------------------------------------------------------
	//  everything for 1st time run  
	private void init()
	{
		loadDataFile();
		linkdata();
		if(db_dump_travel)
		{
			dump_travel();
		}
		
		//TODO WIZARD
		poof();
	}
	// ---------------------------------------------------------------------
	private void linkdata()	//   secondary data manipulation 
	{       
		int i,j;
		// array linkages          
		for (i=1; i<=LAST_LOCATION_INDEX; i++)
		{
			TravList travel = getTravel(i);
			if (hasLText(i) && travel != null)
			{
				if ((travel.tverb)==1) cond[i]=2;
			}
		}
		for (j=LAST_OBJECT_INDEX; j>0; j--)
		{
			if (fixd[j]>0)
			{       
				drop(j+FIXED_OBJECT_OFFSET,fixd[j]);
				drop(j,plac[j]);
			}
		}
		for (j=LAST_OBJECT_INDEX; j>0; j--)
		{
			gameData.fixed[j]=fixd[j];
			if (plac[j]!=0 && fixd[j]<=0)
				drop(j,plac[j]);
		}
		gameData.tally = 0;
		gameData.tally2 = 0;
	
		for (i=FIRST_TREASURE_INDEX; i<=LAST_TREASURE_INDEX; i++)
		{       
			if ( hasPText(i) ) 
			{
				gameData.prop[i] = -1;
			}
			gameData.tally = gameData.tally - gameData.prop[i];
		}
	
		//  define mnemonics 
		keys = vocab("keys", Usage.OBJECT);
		lamp = vocab("lamp", Usage.OBJECT);
		grate = vocab("grate", Usage.OBJECT);
		cage = vocab("cage", Usage.OBJECT);
		rod = vocab("rod", Usage.OBJECT);
		rod2=rod+1;
		steps = vocab("steps", Usage.OBJECT);
		bird = vocab("bird", Usage.OBJECT);
		door = vocab("door", Usage.OBJECT);
		pillow = vocab("pillow", Usage.OBJECT);
		snake = vocab("snake", Usage.OBJECT);
		fissur = vocab("fissur", Usage.OBJECT);
		tablet = vocab("tablet", Usage.OBJECT);
		clam = vocab("clam", Usage.OBJECT);
		oyster = vocab("oyster", Usage.OBJECT);
		magzin = vocab("magaz", Usage.OBJECT);
		dwarf = vocab("dwarf", Usage.OBJECT);
		knife = vocab("knife", Usage.OBJECT);
		food = vocab("food", Usage.OBJECT);
		bottle = vocab("bottle", Usage.OBJECT);
		water = vocab("water", Usage.OBJECT);
		oil = vocab("oil", Usage.OBJECT);
		plant = vocab("plant", Usage.OBJECT);
		plant2=plant+1;
		axe = vocab("axe", Usage.OBJECT);
		mirror = vocab("mirror", Usage.OBJECT);
		dragon = vocab("dragon", Usage.OBJECT);
		chasm = vocab("chasm", Usage.OBJECT);
		troll = vocab("troll", Usage.OBJECT);
		troll2=troll+1;
		bear = vocab("bear", Usage.OBJECT);
		messag = vocab("messag", Usage.OBJECT);
		vend = vocab("vendi", Usage.OBJECT);
		batter = vocab("batter", Usage.OBJECT);
		spices = vocab("spices", Usage.OBJECT);

		nugget = vocab("nugget", Usage.OBJECT);
		coins = vocab("coins", Usage.OBJECT);
		chest = vocab("chest", Usage.OBJECT);
		eggs = vocab("eggs", Usage.OBJECT);
		tridnt = vocab("tride", Usage.OBJECT);
		vase = vocab("vase", Usage.OBJECT);
		emrald = vocab("emera", Usage.OBJECT);
		pyram = vocab("pyram", Usage.OBJECT);
		pearl = vocab("pearl", Usage.OBJECT);
		rug = vocab("rug", Usage.OBJECT);
		chain = vocab("chain", Usage.OBJECT);

		back = vocab("back", Usage.DESTINATION);
		look = vocab("look", Usage.DESTINATION);
		cave = vocab("cave", Usage.DESTINATION);
		vnull = vocab("null", Usage.DESTINATION);
		entrnc = vocab("entra", Usage.DESTINATION);
		dprssn = vocab("depre", Usage.DESTINATION);
		enter = vocab("enter", Usage.DESTINATION);

		pour = vocab("pour", Usage.VERB);
		say = vocab("say", Usage.VERB);
		lock = vocab("lock", Usage.VERB);
		vthrow = vocab("throw", Usage.VERB);
		find = vocab("find", Usage.VERB);
		invent = vocab("inven", Usage.VERB);
	
		//  initialize dwarves 
		gameData.chloc = 114;
		gameData.chloc2 = 140;
		for (i=1; i<=6; i++)
		{
			gameData.dwarfSeenAtLoc[i]=false;
		}
		gameData.dflag = 0;
		gameData.dwarfLoc[1]=19;
		gameData.dwarfLoc[2]=27;
		gameData.dwarfLoc[3]=33;
		gameData.dwarfLoc[4]=44;
		gameData.dwarfLoc[5]=64;
		gameData.dwarfLoc[6]= gameData.chloc;
		gameData.daltlc = 18;
	
		//  random flags & ctrs 
		gameData.turns = 0;
		gameData.lmwarn = false;
		gameData.iwest = 0;
		gameData.knfloc = 0;
		gameData.detail = 0;
		gameData.abbnum = 5;
		for (i=0; i<=4; i++)
		{
			if (hasRText(2*i+81) )
			{
				gameData.maxdie = i + 1;
			}
		}
		gameData.numdie = gameData.holdng = gameData.dkill = gameData.foobar = gameData.bonus = 0;
		gameData.clock1 = 30;
		gameData.clock2 = 50;
		gameData.saved_last_usage = 0;
		isClosing = gameData.panic = isClosed = isScoring = false;
	}
	// ---------------------------------------------------------------------
	private void parseUserInputLine(String line)
	{
		String[] parts = line.split("\\s+", 2);
		switch(parts.length) {
			case 0: wd1 = ""; wd2 = null; break;
			case 1: wd1 = parts[0]; wd2 = null; break;
			case 2: wd1 = parts[0]; wd2 = parts[1]; break;
		}
//		System.out.println("Wd1: "+wd1+", wd2: " + wd2);
	}
	// ---------------------------------------------------------------------
    private boolean weq(String a, String b)
    {
		return b == a
			  || (b != null && b.equals( a ) );
    }
	// ---------------------------------------------------------------------
	void trapdel()       //  come here if he hits a del   
	{	
		delhit++;			//  main checks, treats as QUIT  
		// TODO:: Implement trapdel
		throw new RuntimeException("trapdel Not Yet Implemented");
		//signal(SIGINT,trapdel);		//  catch subsequent DELs        
	}
//	// ---------------------------------------------------------------------
//	private void startup(  )
//	{
//		initRandomNumbers();
//
//		demo = Start(0);
//		//?? srand((int)(time((time_t *)NULL)));	//  random seed
//		//??  srand(371);				/* non-random seed
//		hinted[3] = yes(65,1,0);
//		newloc=1;
//		delhit = 0;
//		limit=330;
//		if (hinted[3])
//		{
//			limit=1000;      //  better batteries if instrucs
//		}
//	}
	// ---------------------------------------------------------------------
    private int restore(String s)
    {
		return 2;
		// TODO:: Implement unlink
		// throw new RuntimeException("Not Yet Implemented");
    }
	// ---------------------------------------------------------------------
    private void unlink(String s)
    {
		// TODO:: Implement unlink
		throw new RuntimeException("Not Yet Implemented");
    }
	// ---------------------------------------------------------------------
	private void DP( String fmt, Object... args )
	{
		if(isVerbose) {
			String msg = sprintf(fmt, (Object[]) args);
			printf("[Adv32] " + msg);
		}
	}
	// ---------------------------------------------------------------------
	// Private Data
	// ---------------------------------------------------------------------
    //
	
}
