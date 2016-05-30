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

import java.util.Date;
import java.util.Random;

public class Adv32 extends Wizard
{

	private boolean isVerbose = false;

    public static void main( String args[] )
    {
		Adv32 adv = new Adv32(  );
		adv.processCommandLine( args );
    }

	private Adv32(  )
	{
	}

	private final static int L_NEWGAME=2;
	private final static int L_RESTORED=8;
	private final static int L_PROMPT_OK=2009;
	private final static int L_PROMPT_K=2010;
	private final static int L_PROMPT_SPK=2011;
	private final static int L_NEXT_MOVE=2012;
	private final static int L_USER_INPUT=2600;

	private void processCommandLine(String[] args) {
		String path = null;
		for(String arg : args) {
			if("-v".equals(arg)) {
				isVerbose = true;
				continue;
			}
			path = arg;
		}
		this.doGame( path );
	}
	
    private void doGame( String path )
    {
		DataFile.MsgInfo kk = null;
		int next_label = L_NEWGAME;
		int rval, ll;
		int i;
		
		init();
	
		if( path != null )	//  Restore file specified 
		{
			i = restore(path);	//  See what we've got 
			switch(i)
			{
			case 0:	 			//  The restore worked fine 
				yea = Start(0);
				k=0;
				unlink(path);	//  Don't re-use the save 
				next_label = L_RESTORED;	//  Get where we're going 
			case 1:				//  Couldn't open it 
				exit(0);		//  So give up 
			case 2:				//  Oops -- file was altered 
				rspeak(202);	//  You dissolve 
				exit(0);		//  File could be non-adventure 
			}					//  So don't unlink it. 
		}
		else
		{
			startup();
		}
		// Main Execution Loop
		for (;;)                        //  main command loop (label 2)  
		{
			DP("MainLoop: {0}", next_label);
			switch(next_label)
			{
			case L_NEWGAME:
				if (newloc<9 && newloc!=0 && closng)
				{       
					rspeak(130);    //  if closing leave only by     
					newloc=loc;     //       main office             
					if (!panic) clock2=15;
					panic=TRUE;
				}
		
				rval=fdwarf();          //  dwarf stuff                  
				if (rval==99)
					die(99);
			case 2000:
				if (loc==0)
					die(99);    //  label 2000                   
//TODO:: Fix This				
				kk = getSText(loc);
				if ((abb[loc]%abbnum)==0 || kk == null)
					kk = getLText(loc);
				if (!forced(loc) && dark(0))
				{       
					if (wzdark && pct(35))
					{       
						die(90);
						{next_label=2000; continue;}
					}
					kk = getRText(16);
				}
			case 2001:
				if (toting(bear)) rspeak(141);  //  2001                 
				speak(kk);
				k=1;
				if (forced(loc))
					{next_label=8; continue;}
				if (loc==33 && pct(25)&&!closng) rspeak(8);
				// FLATTEN
				//if (!dark(0))
				// {
				if (dark(0)) {next_label=L_NEXT_MOVE; continue;}
				abb[loc]++;
				for (i=atloc[loc]; i!=0; i=link[i])     // 2004  
				{       
					obj=i;
					if (obj>FIXED_OBJECT_OFFSET)
						obj -= FIXED_OBJECT_OFFSET;
					if (obj==steps && toting(nugget))
						continue;
					if (prop[obj]<0)
					{       
						if (closed)
							continue;
						prop[obj]=0;
						if (obj==rug||obj==chain)
							prop[obj]=1;
						tally--;
						if (tally==tally2 && tally != 0)
							if (limit>35) limit=35;
					}
					ll =  prop[obj];   //  2006         
					if (obj==steps && loc==fixed[steps])
						ll = 1;
					pspeak(obj, ll);
				}                                       //  2008 
				{next_label=L_NEXT_MOVE; continue;}
			
			case L_PROMPT_OK:
				k=54;                   //  2009                 
			case L_PROMPT_K:
				spk=k;
			case L_PROMPT_SPK:
				rspeak(spk);
				// ENDFLATTEN }
			case L_NEXT_MOVE:
				verb=0;                         //  2012                 
				obj=0;
			case L_USER_INPUT:
				checkhints();                   //  to 2600-2602         
				if (closed)
				{       
					if (prop[oyster]<0 && toting(oyster))
						pspeak(oyster,1);
					for (i=1; i<=LAST_OBJECT_INDEX; i++)
					{
						if (toting(i)&&prop[i]<0)       // 2604  
							prop[i] = -1-prop[i];
					}
				}
				wzdark=dark(0);                 //  2605                 
				if (knfloc>0 && knfloc!=loc) knfloc=1;
				DP("GETIN @loc({0})", loc);
				getin();
				if (delhit > 0)		//  user typed a DEL     
				{       
					delhit=0;		//  reset counter
					wd1= "quit";
					wd2 = null;
				}
			case 2608:
				if ((foobar = -foobar)>0) foobar=0;     //  2608         
				//  should check here for "magic mode"                   
				turns++;
				if (demo && turns>=TURNS_IN_A_DEMO_GAME) done(1);      //  to 13000     
		
				if (verb==say && wd2!=null) verb=0;
				if (verb==say)
					{next_label=4090; continue;}
				if (tally==0 && loc>=15 && loc!=33) clock1--;
				if (clock1==0)
				{       closing();                      //  to 10000     
					{next_label=19999; continue;}
				}
				if (clock1<0) clock2--;
				if (clock2==0)
				{       
					caveclose();            //  to 11000             
					{next_label=2; continue;} //  back to 2            
				}
				if (prop[lamp]==1) limit--;
				if (limit<=30 && here(batter) && prop[batter]==0
					&& here(lamp))
				{       
					rspeak(188);            //  12000                
					prop[batter]=1;
					if (toting(batter)) drop(batter,loc);
					limit=limit+2500;
					lmwarn=FALSE;
					{next_label=19999; continue;}
				}
				if (limit==0)
				{       
					limit = -1;             //  12400                
					prop[lamp]=0;
					rspeak(184);
					{next_label=19999; continue;}
				}
				if (limit<0&&loc<=8)
				{       
					rspeak(185);            //  12600                
					gaveup=TRUE;
					done(2);                //  to 20000             
				}
				if (limit<=30)
				{   
					if (lmwarn|| !here(lamp)) {next_label=19999; continue;}  // 12200
					lmwarn=TRUE;
					spk=187;
					if (place[batter]==0) spk=183;
					if (prop[batter]==1) spk=189;
					rspeak(spk);
				}
			case 19999:
				k=43;
				if (liqloc(loc)==water) k=70;
				if (weq(wd1,"enter") &&
					(weq(wd2,"strea")||weq(wd2,"water")))
					{next_label=L_PROMPT_K; continue;}
				if (weq(wd1,"enter") && wd2!=null)
					{next_label=2800; continue;}
				if ((!weq(wd1,"water")&&!weq(wd1,"oil"))
					|| (!weq(wd2,"plant")&&!weq(wd2,"door")))
					{next_label=2610; continue;}
				if (at(vocab(wd2,1))) wd2 = "pour";
		
			case 2610:
				if (weq(wd1,"west"))
				if (++iwest==10) rspeak(17);
			case 2630:
				i=vocab(wd1,-1);
				if (i== -1)
				{       spk=60;                 //  3000         
					if (pct(20)) spk=61;
					if (pct(20)) spk=13;
					rspeak(spk);
					{next_label=L_USER_INPUT; continue;}
				}
				k=i%1000;
				kq=i/1000+1;
				switch(kq)
				{
					case 1: {next_label=8; continue;}
					case 2: {next_label=5000; continue;}
					case 3: {next_label=4000; continue;}
					case 4: {next_label=L_PROMPT_K; continue;}
					default:
					printf("Error 22");
					exit(0);
				}
		
			case L_RESTORED:
				switch(march())
				{   
					case 2:
						{next_label=2; continue;} //  back to 2            
					case 99:
					switch(die(99))
					{   
						case 2000: {next_label=2000; continue;}
						default: bug(111);
					}
					default:
						bug(110);
				}
		
			case 2800:  
				wd1 = wd2;
				wd2=null;
				{next_label=2610; continue;}
		
			case 4000:
				verb=k;
				spk=actspk[verb];
				if (wd2!=null && verb!=say)
					{next_label=2800; continue;}
				if (verb==say) obj= wd2.charAt(0); //TODO::CHECK THIS *wd2;
				if (obj!=0) {next_label=4090; continue;}
			case 4080:
				switch(verb)
				{   case 1:                     //  take = 8010          
						if (atloc[loc]==0 || link[atloc[loc]] != 0)
							{next_label=8000; continue;}
						for (i=1; i<=5; i++)
						{
							if (dloc[i]==loc&&dflag>=2)
								{next_label=8000; continue;}
						}
						obj=atloc[loc];
						{next_label=9010; continue;}
					case 2: case 3: case 9:     //  8000 : drop,say,wave 
					case 10: case 16: case 17:  //  calm,rub,toss        
					case 19: case 21: case 28:  //  find,feed,break      
					case 29:                    //  wake                 
						{next_label=8000; continue;}
					case 4: case 6:             //  8040 open,lock       
						spk=28;
						if (here(clam)) obj=clam;
						if (here(oyster)) obj=oyster;
						if (at(door)) obj=door;
						if (at(grate)) obj=grate;
						if (obj!=0 && here(chain)) {next_label=8000; continue;}
						if (here(chain)) obj=chain;
						if (obj==0) {next_label=L_PROMPT_SPK; continue;}
						{next_label=9040; continue;}
					case 5: {next_label=L_PROMPT_OK; continue;}         //  nothing              
					case 7: {next_label=9070; continue;}         //  on                   
					case 8: {next_label=9080; continue;}         //  off                  
					case 11: {next_label=8000; continue;}        //  walk                 
					case 12: {next_label=9120; continue;}        //  kill                 
					case 13: {next_label=9130; continue;}        //  pour                 
					case 14:                    //  eat: 8140            
						if (!here(food)) {next_label=8000; continue;}
						{next_label=8142; continue;}
					case 15: {next_label=9150; continue;}        //  drink                
					case 18:                    //  quit: 8180           
						gaveup=yes(22,54,54);
						if (gaveup) done(2);    //  8185                 
						{next_label=L_NEXT_MOVE; continue;}
					case 20:                    //  invent=8200          
						spk=98;
						for (i=1; i<=LAST_OBJECT_INDEX; i++)
						{       if (i!=bear && toting(i))
							{       if (spk==98) rspeak(99);
								blklin=FALSE;
								pspeak(i,-1);
								blklin=TRUE;
								spk=0;
							}
						}
						if (toting(bear)) spk=141;
						{next_label=L_PROMPT_SPK; continue;}
					case 22: {next_label=9220; continue;}        //  fill                 
					case 23: {next_label=9230; continue;}        //  blast                
					case 24:                    //  score: 8240          
						scorng=TRUE;
						printf(
							"If you were to quit now, you would score {0} out"
							+" of a possible {1}",
							score(),
							mxscor
						);
						scorng=FALSE;
						gaveup=yes(143,54,54);
						if (gaveup) done(2);
						{next_label=L_NEXT_MOVE; continue;}
					case 25:                    //  foo: 8250            
						k=vocab(wd1,3);
						spk=42;
						if (foobar==1-k) {next_label=8252; continue;}
						if (foobar!=0) spk=151;
						{next_label=L_PROMPT_SPK; continue;}
					case 26:                    //  brief=8260           
						spk=156;
						abbnum=10000;
						detail=3;
						{next_label=L_PROMPT_SPK; continue;}
					case 27:                    //  read=8270            
						if (here(magzin)) obj=magzin;
						if (here(tablet)) obj=obj*100+tablet;
						if (here(messag)) obj=obj*100+messag;
						if (closed&&toting(oyster)) obj=oyster;
						if (obj>FIXED_OBJECT_OFFSET || obj==0 || dark(0) )
						{
							{next_label=8000; continue;}
						}
						{next_label=9270; continue;}
					case 30:                    //  suspend=8300         
						spk=201;
						if (demo) {next_label=L_PROMPT_SPK; continue;}
						printf(
							"I can suspend your adventure for you so you "
							+"can resume later, but"
						);
						printf(
							"you will have to wait at least {0} minutes before"
							+" continuing.",
							latncy
						);
						if (!yes(200,54,54)) {next_label=L_NEXT_MOVE; continue;}
						saved_last_usage = datime();
						ciao(path);	          //  Do we quit? 
						{next_label=2; continue;} //  Maybe not
					case 31:                    //  hours=8310           
						printf(
							"Colossal cave is closed 9am-5pm Mon through "
							+"Fri except holidays."
						);
						{next_label=L_NEXT_MOVE; continue;}
					default: bug(23);
				}
			case 8000:
				printf("{0} what?",wd1);
				obj=0;
				{next_label=L_USER_INPUT; continue;}
			case 8142:
				dstroy(food);
				spk=72;
				{next_label=L_PROMPT_SPK; continue;}
			case 8252:
				foobar=k;
				if (k!=4) {next_label=L_PROMPT_OK; continue;}
				foobar=0;
				if (place[eggs]==plac[eggs]
					||(toting(eggs)&&loc==plac[eggs])) {next_label=L_PROMPT_SPK; continue;}
				if (place[eggs]==0&&place[troll]==0&&prop[troll]==0)
					prop[troll]=1;
				k=2;
				if (here(eggs)) k=1;
				if (loc==plac[eggs]) k=0;
				move(eggs,plac[eggs]);
				pspeak(eggs,k);
				{next_label=L_NEXT_MOVE; continue;}

			case 4090:
			// FLATTEN THIS SWITCH;
				switch(verb)
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
					{next_label=(9000+verb*10); continue;}
					
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
					case 19000: done(3);
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
				spk=184;
				if (limit<0) {next_label=L_PROMPT_SPK; continue;}
				prop[lamp]=1;
				rspeak(39);
				if (wzdark) {next_label=2000; continue;}
				{next_label=L_NEXT_MOVE; continue;}
		
			case 9080:	//  off  
				if (!here(lamp)) {next_label=L_PROMPT_SPK; continue;}
				prop[lamp]=0;
				rspeak(40);
				if (dark(0)) rspeak(16);
				{next_label=L_NEXT_MOVE; continue;}
		
			case 9090:	//  wave                 
				if ((!toting(obj))&&(obj!=rod||!toting(rod2)))
					spk=29;
				if (obj!=rod||!at(fissur)||!toting(obj)||closng)
					{next_label=L_PROMPT_SPK; continue;}
				prop[fissur]=1-prop[fissur];
				pspeak(fissur,2-prop[fissur]);
				{next_label=L_NEXT_MOVE; continue;}
			case 9120:	//  kill
				switch(trkill())
				{   case 8000: {next_label=8000; continue;}
					case 8: {next_label=8; continue;}
					case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
					case 2608: {next_label=2608; continue;}
					case 19000: done(3);
					default: bug(112);
				}
			case 9130:	//  pour
				if (obj==bottle||obj==0) obj=liq(0);
				if (obj==0) {next_label=8000; continue;}
				if (!toting(obj)) {next_label=L_PROMPT_SPK; continue;}
				spk=78;
				if (obj!=oil&&obj!=water) {next_label=L_PROMPT_SPK; continue;}
				prop[bottle]=1;
				place[obj]=0;
				spk=77;
				if (!(at(plant)||at(door))) {next_label=L_PROMPT_SPK; continue;}
				if (at(door))
				{       prop[door]=0;   //  9132                 
					if (obj==oil) prop[door]=1;
					spk=113+prop[door];
					{next_label=L_PROMPT_SPK; continue;}
				}
				spk=112;
				if (obj!=water) {next_label=L_PROMPT_SPK; continue;}
				pspeak(plant,prop[plant]+1);
				prop[plant]=(prop[plant]+2)% 6;
				prop[plant2]=prop[plant]/2;
				k=0;
				{next_label=8; continue;}
			case 9140:	//9140 - eat           
				if (obj==food) {next_label=8142; continue;}
				if (obj==bird||obj==snake||obj==clam||obj==oyster
					||obj==dwarf||obj==dragon||obj==troll
					||obj==bear) spk=71;
				{next_label=L_PROMPT_SPK; continue;}
			case 9150:	//  9150 - drink
				if (obj==0&&liqloc(loc)!=water&&(liq(0)!=water
					||!here(bottle))) {next_label=8000; continue;}
				if (obj!=0&&obj!=water) spk=110;
				if (spk==110||liq(0)!=water||!here(bottle))
					{next_label=L_PROMPT_SPK; continue;}
				prop[bottle]=1;
				place[water]=0;
				spk=74;
				{next_label=L_PROMPT_SPK; continue;}
			case 9160:	//  9160: rub
				if (obj!=lamp) spk=76;
				{next_label=L_PROMPT_SPK; continue;}
			case 9170:	//  9170: throw
				switch(trtoss())
				{   
					case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
					case 9020: {next_label=9020; continue;}
					case 9120: {next_label=9120; continue;}
					case 8: {next_label=8; continue;}
					case 9210: {next_label=9210; continue;}
					default: bug(113);
				}
			case 9190:	//  9190: find
			case 9200:	// , invent
				if (at(obj)||(liq(0)==obj&&at(bottle))
					||k==liqloc(loc)) spk=94;
				for (i=1; i<=5; i++)
					if (dloc[i]==loc&&dflag>=2&&obj==dwarf)
						spk=94;
				if (closed) spk=138;
				if (toting(obj)) spk=24;
				{next_label=L_PROMPT_SPK; continue;}
			case 9210:	//  feed
				switch(trfeed())
				{   case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
					default: bug(114);
				}
			case 9220:	//  fill
				switch(trfill())
				{   case L_PROMPT_SPK: {next_label=L_PROMPT_SPK; continue;}
					case 8000: {next_label=8000; continue;}
					case 9020: {next_label=9020; continue;}
					default: bug(115);
				}
			case 9230:	//  blast
				if (prop[rod2]<0||!closed) {next_label=L_PROMPT_SPK; continue;}
				bonus=133;
				if (loc==115) bonus=134;
				if (here(rod2)) bonus=135;
				rspeak(bonus);
				done(2);
			case 9270:	//  read
				if (dark(0)) {next_label=5190; continue;}
				if (obj==magzin) spk=190;
				if (obj==tablet) spk=196;
				if (obj==messag) spk=191;
				if (obj==oyster&&hinted[2]&&toting(oyster)) spk=194;
				if (obj!=oyster||hinted[2]||!toting(oyster)
					||!closed) {next_label=L_PROMPT_SPK; continue;}
				hinted[2]=yes(192,193,54);
				{next_label=L_NEXT_MOVE; continue;}
			case 9280:	//  break
				if (obj==mirror) spk=148;
				if (obj==vase&&prop[vase]==0)
				{       spk=198;
					if (toting(vase)) drop(vase,loc);
					prop[vase]=2;
					fixed[vase]= -1;
					{next_label=L_PROMPT_SPK; continue;}
				}
				if (obj!=mirror||!closed) {next_label=L_PROMPT_SPK; continue;}
				rspeak(197);
				done(3);
		
			case 9290:	//  wake
				if (obj!=dwarf||!closed) {next_label=L_PROMPT_SPK; continue;}
				rspeak(199);
				done(3);
		
				// END FLATTEND SWITCH }
		
			case 5000:
				obj=k;
				if (fixed[k]!=loc && !here(k))
				{
					{next_label=5100; continue;}
				}
			case 5010:
				if (wd2!=null) {next_label=2800; continue;}
				if (verb!=0) {next_label=4090; continue;}
				printf("What do you want to do with the {0}?",wd1);
				{next_label=L_USER_INPUT; continue;}
			case 5100:  
				if (k!=grate) {next_label=5110; continue;}
				if (loc==1||loc==4||loc==7) k=dprssn;
				if (loc>9&&loc<15) k=entrnc;
				if (k!=grate) {next_label=8; continue;}
			case 5110:
				if (k!=dwarf)
				{
					{next_label=5120; continue;}
				}
				for (i=1; i<=5; i++)
				{
					if (dloc[i]==loc&&dflag>=2)
					{
						{next_label=5010; continue;}
					}
				}
			case 5120:
				if ((liq(0)==k&&here(bottle))||k==liqloc(loc)) {next_label=5010; continue;}
				if (obj!=plant||!at(plant2)||prop[plant2]==0) {next_label=5130; continue;}
				obj=plant2;
				{next_label=5010; continue;}
			case 5130:
				if (obj!=knife||knfloc!=loc) {next_label=5140; continue;}
				knfloc = -1;
				spk=116;
				{next_label=L_PROMPT_SPK; continue;}
			case 5140:
				if (obj!=rod||!here(rod2)) {next_label=5190; continue;}
				obj=rod2;
				{next_label=5010; continue;}
			case 5190:
				if ((verb==find||verb==invent)&&wd2==null) {next_label=5010; continue;}
				printf("I see no {0} here",wd1);
				{next_label=L_NEXT_MOVE; continue;}
			}
		}
    }
	
	
	// ---------------------------------------------------------------------
	boolean toting(int objj)
	{       
		return place[objj] == TOTING;
	}
	// ---------------------------------------------------------------------
	boolean here(int objj)
	{       
		return place[objj]==loc || toting(objj);
	}
	
	// ---------------------------------------------------------------------
	boolean at(int objj)
	{       
		return place[objj]==loc || fixed[objj]==loc;
	}
	// ---------------------------------------------------------------------
	int liq2(int pbotl)
	{       
		return((1-pbotl)*water+(pbotl/2)*(water+oil));
	}
	// ---------------------------------------------------------------------
	int liq(int foo)
	{       
		int i=prop[bottle];
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
		return (cond[loc]%2)==0
				&& (prop[lamp]==0 || !here(lamp));
	}
	// ---------------------------------------------------------------------
	boolean pct(int n)
	{       
		if (ran(100)<n) return(TRUE);
		return(FALSE);
	}
	// ---------------------------------------------------------------------
	int fdwarf()		//  71 
	{	
		int i,j;
		TravList kk;
	
		if (newloc!=loc && !forced(loc) && !bitset(loc,3))
		{	
			for (i=1; i<=5; i++)
			{	
				if (odloc[i]!=newloc || !dseen[i])
					continue;
				newloc=loc;
				rspeak(2);
				break;
			}
		}
		loc=newloc;			//  74 
		if (loc==0 || forced(loc) || bitset(newloc,3))
			return(2000);
		if (dflag==0)
		{	
			if (loc>=15) dflag=1;
			return(2000);
		}
		if (dflag==1)		//  6000 
		{	
			if (loc<15||pct(95)) return(2000);
			dflag=2;
			for (i=1; i<=2; i++)
			{	
				j=1+ran(5);
				if (pct(50)&&saved== -1) dloc[j]=0; //  6001 
			}
			for (i=1; i<=5; i++)
			{	
				if (dloc[i]==loc) dloc[i]=daltlc;
				odloc[i]=dloc[i];		//  6002 
			}
			rspeak(3);
			drop(axe,loc);
			return(2000);
		}
		dtotal=attack=stick=0;			//  6010 
		for (i=1; i<=6; i++)                    /* loop to 6030 */
		{	
			if (dloc[i]==0) continue;
			j=1;
			for (kk=getTravel(dloc[i]); kk!=null; kk=kk.next)
			{	
				newloc=kk.tloc;
				if (newloc>300
					||newloc<15
					||newloc==odloc[i]
					||(j>1&&newloc==tk[j-1])
					||j>=20
					||newloc==dloc[i]
					||forced(newloc)
					||(i==6&&bitset(newloc,3))
					||kk.conditions==100)
				{
					continue;
				}
				tk[j++]=newloc;
			}
			tk[j]=odloc[i];                 /* 6016 */
			if (j>=2) j--;
			j=1+ran(j);
			odloc[i]=dloc[i];
			dloc[i]=tk[j];
			dseen[i]=(dseen[i]&&loc>=15)||(dloc[i]==loc||odloc[i]==loc);
			if (!dseen[i]) continue;        /* i.e. goto 6030 */
			dloc[i]=loc;
			if (i==6)                       /* pirate's spotted him */
			{
				int next_label = 0;
				while(true) 
				{
					switch(next_label)
					{
					case 0:
						if (loc==chloc||prop[chest]>=0) break;
						k=0;
						for (j=FIRST_TREASURE_INDEX; j<=LAST_TREASURE_INDEX; j++)      /* loop to 6020 */
						{       
							if (j==pyram&&(loc==plac[pyram]
								 || loc==plac[emrald])) // goto l6020;
							{
							}
							else
							{
								if (toting(j)) {next_label = 2; break;};
							}
						//l6020:  
							if (here(j)) k=1;
							next_label = 1;
						}                              /* 6020 */
						continue;
					case 1: // l6021:  
						if (tally==tally2+1 && k==0 && place[chest]==0
							&&here(lamp) && prop[lamp]==1) {next_label = 5; continue;};
						if (odloc[6]!=dloc[6]&&pct(20))
							rspeak(127);
						break;       /* to 6030 */
					case 2: // l6022:  
						rspeak(128);
						if (place[messag]==0) move(chest,chloc);
						move(messag,chloc2);
						for (j=FIRST_TREASURE_INDEX; j<=LAST_TREASURE_INDEX; j++)      /* loop to 6023 */
						{       
							if (j==pyram && (loc==plac[pyram]
								|| loc==plac[emrald])) break;
							if (at(j)&&fixed[j]==0) carry(j,loc);
							if (toting(j)) drop(j,chloc);
						}
					case 4: // l6024:  
						dloc[6]=odloc[6]=chloc;
						dseen[6]=FALSE;
						break;
					case 5: // l6025:  
						rspeak(186);
						move(chest,chloc);
						move(messag,chloc2);
						{next_label = 4; continue;}
						//goto l6024;
					}
					break;
				}
				continue;
			}
			dtotal++;                       /* 6027 */
			if (odloc[i]!=dloc[i]) continue;
			attack++;
			if (knfloc>=0) knfloc=loc;
			if (ran(1000)<95*(dflag-2)) stick++;
		}                                       /* 6030 */
		if (dtotal==0) return(2000);
		if (dtotal!=1)
		{       
			printf(
				"There are {0} threatening little dwarves "
				+"in the room with you.",
				dtotal
			);
		}
		else
			rspeak(4);
		if (attack==0) return(2000);
		if (dflag==2) dflag=3;
		if (saved!= -1) dflag=20;
		if (attack!=1)
		{       
			printf("{0} of them throw knives at you!",attack);
			k=6;
		}
		else
		{
			rspeak(5);
			k=52;
		}
		if (stick<=1)                   //  82 
		{       
			rspeak(k+stick);
			if (stick==0) return(2000);
		}
		else
			printf("{0} of them get you!",stick);  //  83 
		oldlc2=loc;
		return(99);
	}
	// ---------------------------------------------------------------------
	//  label 8              
	int march()
	{       
		int ll1,ll2;
	
		if ((tkk=getTravel(newloc=loc))==null)
			bug(26);
		if (k==0)
			return(2);
		if (k==cave)                            //  40                   
		{       
			if (loc<8) rspeak(57);
			if (loc>=8) rspeak(58);
			return(2);
		}
		if (k==look)                            //  30                   
		{       
			if (detail++<3) rspeak(15);
			wzdark=FALSE;
			abb[loc]=0;
			return(2);
		}
		if (k==back)                            //  20                   
		{       
			switch(mback())
			{       
				case 2: return(2);
				case 9: break; // goto l9;
				default: bug(100);
			}
		}
		else
		{
			oldlc2=oldloc;
			oldloc=loc;
		}
	//l9:
		for (; tkk!=null; tkk=tkk.next)
		{
			if (tkk.tverb==1 || tkk.tverb==k)
				break;
		}
		if (tkk==null)
		{       
			badmove();
			return(2);
		}
	//l11:    
		while (true)
		{
			ll1=tkk.conditions;                    //  11                   
			ll2=tkk.tloc;
			newloc=ll1;                             //  newloc=conditions    
			k=newloc%100;                           //  k used for prob
			int next_label  = 0;
			while(true)
			{
				switch(next_label)
				{
				case 0:
					if(!(newloc<=300)) { next_label = 3; continue;}
					if(!(newloc<=100)) { next_label = 2; continue;}    //  13                   
					if(newloc!=0 && !pct(newloc)) break; // goto l12;  //  14   
				case 1: //		l16:    
					newloc=ll2;             //  newloc=location      
					if (newloc<=300)
						return(2);
					if (newloc<=500)
					{
						int spec = specials();
						if(spec == 2 )
						{
							return 2;
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
					rspeak(newloc-500);
					newloc=loc;
					return(2);
				case 2:
					if (toting(k)||(newloc>200&&at(k)))
							{ next_label = 1; continue;} // goto l16;
					break; // goto l12;
				case 3:
					if (prop[k]!=(newloc/100)-3) //  newloc still conditions
						{ next_label = 1; continue;} // goto l16;
					break;
				}
				break;  // fall thru to L12:
			}
		// l12:    //  alternative to probability move      
			for (; tkk!=null; tkk=tkk.next)
			{
				if (tkk.tloc!=ll2 || tkk.conditions!=ll1)
					break;
			}
			if (tkk==null)
				bug(25);
			//goto l11;
		}
	}
	// ---------------------------------------------------------------------
	int mback()                                         //  20                   
	{       
		TravList tk2;
		TravList j;
		int ll;
		
		if (forced(k=oldloc)) k=oldlc2;         //  k=location           
		oldlc2=oldloc;
		oldloc=loc;
		tk2=null;
		if (k==loc)
		{       
			rspeak(91);
			return(2);
		}
		for (; tkk!=null; tkk=tkk.next)           //  21                   
		{       ll=tkk.tloc;
			if (ll==k)
			{       
				k=tkk.tverb;           //  k back to verb       
				tkk=getTravel(loc);
				return(9);
			}
			if (ll<=300)
			{       
				j=getTravel(loc);
				if (forced(ll) && k==j.tloc) tk2=tkk;
			}
		}
		tkk=tk2;                                //  23                   
		if (tkk!=null)
		{       
			k=tkk.tverb;
			tkk=getTravel(loc);
			return(9);
		}
		rspeak(140);
		return(2);
	}
	// ---------------------------------------------------------------------
	int specials()                                      //  30000                
	{
		switch(newloc -= 300)
		{
			case 1:                             //  30100                
				newloc = 99+100-loc;
				if (holdng==0||(holdng==1&&toting(emrald))) return(2);
				newloc=loc;
				rspeak(117);
				return(2);
			case 2:                             //  30200                
				drop(emrald,loc);
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
		if (prop[troll]==1)
		{
			pspeak(troll,1);
			prop[troll]=0;
			move(troll2,0);
			move(troll2+FIXED_OBJECT_OFFSET,0);
			move(troll,plac[troll]);
			move(troll+FIXED_OBJECT_OFFSET,fixd[troll]);
			juggle(chasm);
			newloc=loc;
			return(2);
		}
		newloc=plac[troll]+fixd[troll]-loc;     //  30310                
		if (prop[troll]==0) prop[troll]=1;
		if (!toting(bear)) return(2);
		rspeak(162);
		prop[chasm]=1;
		prop[troll]=2;
		drop(bear,newloc);
		fixed[bear] = -1;
		prop[bear]=3;
		if (prop[spices]<0) tally2++;
		oldlc2=newloc;
		return(99);
	}
	// ---------------------------------------------------------------------
	int badmove()                                       //  20                   
	{
		spk=12;
		if (k>=43 && k<=50) spk=9;
		if (k==29||k==30) spk=9;
		if (k==7||k==36||k==37) spk=10;
		if (k==11||k==19) spk=11;
		if (verb==find||verb==invent) spk=59;
		if (k==62||k==65) spk=42;
		if (k==17) spk=80;
		rspeak(spk);
		return(2);
	}
	// ---------------------------------------------------------------------
	void bug(int n)
	{       
		printf("Please tell jim@rand.org that fatal bug {0} happened.",n);
		exit(0);
	}
	// ---------------------------------------------------------------------
	void checkhints()                                    //  2600 &c              
	{       
		int hint;
		for (hint=4; hint<=hntmax; hint++)
		{       
			if (hinted[hint]) continue;
			if (!bitset(loc,hint)) hintlc[hint]= -1;
			hintlc[hint]++;
			if (hintlc[hint] < getHint(hint, 1) ) continue;
			boolean goto_140010 = true;
			
			switch(hint)
			{   
				case 4:     //  40400 
					if (prop[grate]==0&&!here(keys)) break;
					goto_140010 = false; break;
				case 5:     //  40500 
					if (here(bird)&&toting(rod)&&obj==bird) break;
					continue;      //  i.e. goto l40030 
				case 6:     //  40600 
					if (here(snake)&&!here(bird)) break;
					goto_140010 = false; break;
				case 7:     //  40700 
					if (atloc[loc]==0&&atloc[oldloc]==0
						&& atloc[oldlc2]==0&&holdng>1) break;
					goto_140010 = false; break;
				case 8:     //  40800 
					if (prop[emrald]!= -1&&prop[pyram]== -1) break;
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
				hinted[hint]=yes(175, getHint(hint, 4),54);
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
		i=vocab(wd1,-1);
		if (i==62||i==65||i==71||i==2025)
		{       
			wd2=null;
			obj=0;
			return(2630);
		}
		printf("Okay, \"{0}\".",wd2);
		return(L_NEXT_MOVE);
	}
	// ---------------------------------------------------------------------
	int trtake()                                        //  9010                 
	{   
		int i;
		if (toting(obj)) return(L_PROMPT_SPK);  //  9010 
		spk=25;
		if (obj==plant&&prop[plant]<=0) spk=115;
		if (obj==bear&&prop[bear]==1) spk=169;
		if (obj==chain&&prop[bear]!=0) spk=170;
		if (fixed[obj]!=0) return(L_PROMPT_SPK);
		if (obj==water||obj==oil)
		{       
			if (here(bottle)&&liq(0)==obj)
			{
				obj=bottle;
				//goto l9017;
			}
			else
			{
				obj=bottle;
				if (toting(bottle)&&prop[bottle]==1)
					return(9220);
				if (prop[bottle]!=1) spk=105;
				if (!toting(bottle)) spk=104;
				return(L_PROMPT_SPK);
			}
		}
	//l9017:
		if (holdng>=7)
		{       
			rspeak(92);
			return(L_NEXT_MOVE);
		}
		if (obj==bird)
		{       
			if (prop[bird]==0)	//  goto l9014;
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
				prop[bird]=1;           //  9015 
			}
		}
//	l9014:
		if ((obj==bird||obj==cage)&&prop[bird]!=0)
			carry(bird+cage-obj,loc);
		carry(obj,loc);
		k=liq(0);
		if (obj==bottle && k!=0) place[k] = -1;
		return(L_PROMPT_OK);
	}
	// ---------------------------------------------------------------------
	int dropper()                                       //  9021                 
	{
		k=liq(0);
		if (k==obj) obj=bottle;
		if (obj==bottle&&k!=0) place[k]=0;
		if (obj==cage&&prop[bird]!=0) drop(bird,loc);
		if (obj==bird) prop[bird]=0;
		drop(obj,loc);
		return(L_NEXT_MOVE);
	}
	// ---------------------------------------------------------------------
	int trdrop()                                        //  9020                 
	{
		if (toting(rod2)&&obj==rod&&!toting(rod)) obj=rod2;
		if (!toting(obj)) return(L_PROMPT_SPK);
		if (obj==bird&&here(snake))
		{       
			rspeak(30);
			if (closed) return(19000);
			dstroy(snake);
			prop[snake]=1;
			return(dropper());
		}
		if (obj==coins&&here(vend))             //  9024                 
		{       
			dstroy(coins);
			drop(batter,loc);
			pspeak(batter,0);
			return(L_NEXT_MOVE);
		}
		if (obj==bird&&at(dragon)&&prop[dragon]==0)     //  9025         
		{       
			rspeak(154);
			dstroy(bird);
			prop[bird]=0;
			if (place[snake]==plac[snake]) tally2--;
			return(L_NEXT_MOVE);
		}
		if (obj==bear&&at(troll))               //  9026                 
		{       
			rspeak(163);
			move(troll,0);
			move(troll+100,0);
			move(troll2,plac[troll]);
			move(troll2+100,fixd[troll]);
			juggle(chasm);
			prop[troll]=2;
			return(dropper());
		}
		if (obj!=vase||loc==plac[pillow])       //  9027                 
		{       
			rspeak(54);
			return(dropper());
		}
		prop[vase]=2;                           //  9028                 
		if (at(pillow)) prop[vase]=0;
		pspeak(vase,prop[vase]+1);
		if (prop[vase]!=0) fixed[vase] = -1;
		return(dropper());
	}
	// ---------------------------------------------------------------------
	int tropen()                                        //  9040                 
	{       
		if (obj==clam||obj==oyster)
		{       
			k=0;                            //  9046                 
			if (obj==oyster) k=1;
			spk=124+k;
			if (toting(obj)) spk=120+k;
			if (!toting(tridnt)) spk=122+k;
			if (verb==lock) spk=61;
			if (spk!=124) return(L_PROMPT_SPK);
			dstroy(clam);
			drop(oyster,loc);
			drop(pearl,105);
			return(L_PROMPT_SPK);
		}
		if (obj==door) spk=111;
		if (obj==door&&prop[door]==1) spk=54;
		if (obj==cage) spk=32;
		if (obj==keys) spk=55;
		if (obj==grate||obj==chain) spk=31;
		if (spk!=31||!here(keys)) return(L_PROMPT_SPK);
		if (obj==chain)
		{       
			if (verb==lock)
			{       
				spk=172;                //  9049: lock           
				if (prop[chain]!=0) spk=34;
				if (loc!=plac[chain]) spk=173;
				if (spk!=172) return(L_PROMPT_SPK);
				prop[chain]=2;
				if (toting(chain)) drop(chain,loc);
				fixed[chain]= -1;
				return(L_PROMPT_SPK);
			}
			spk=171;
			if (prop[bear]==0) spk=41;
			if (prop[chain]==0) spk=37;
			if (spk!=171) return(L_PROMPT_SPK);
			prop[chain]=0;
			fixed[chain]=0;
			if (prop[bear]!=3) prop[bear]=2;
			fixed[bear]=2-prop[bear];
			return(L_PROMPT_SPK);
		}
		if (closng)
		{       
			k=130;
			if (!panic) clock2=15;
			panic=TRUE;
			return(L_PROMPT_K);
		}
		k=34+prop[grate];                       //  9043                 
		prop[grate]=1;
		if (verb==lock) prop[grate]=0;
		k=k+2*prop[grate];
		return(L_PROMPT_K);
	}
	// ---------------------------------------------------------------------
	int trkill()                                //  9120                         
	{       
		int i;
		for (i=1; i<=5; i++)
			if (dloc[i]==loc&&dflag>=2) break;
		if (i==6) i=0;
		if (obj==0)                     //  9122                         
		{       
			if (i!=0) obj=dwarf;
			if (here(snake)) obj=obj*100+snake;
			if (at(dragon)&&prop[dragon]==0) obj=obj*100+dragon;
			if (at(troll)) obj=obj*100+troll;
			if (here(bear)&&prop[bear]==0) obj=obj*100+bear;
			if (obj>FIXED_OBJECT_OFFSET) return(8000);
			if (obj==0)
			{       if (here(bird)&&verb!=vthrow) obj=bird;
				if (here(clam)||here(oyster)) obj=100*obj+clam;
				if (obj>100) return(8000);
			}
		}
		if (obj==bird)                  //  9124                         
		{       
			spk=137;
			if (closed) return(L_PROMPT_SPK);
			dstroy(bird);
			prop[bird]=0;
			if (place[snake]==plac[snake]) tally2++;
			spk=45;
		}
		if (obj==0) spk=44;             //  9125                         
		if (obj==clam||obj==oyster) spk=150;
		if (obj==snake) spk=46;
		if (obj==dwarf) spk=49;
		if (obj==dwarf&&closed) return(19000);
		if (obj==dragon) spk=147;
		if (obj==troll) spk=157;
		if (obj==bear) spk=165+(prop[bear]+1)/2;
		if (obj!=dragon||prop[dragon]!=0) return(L_PROMPT_SPK);
		rspeak(49);
		verb=0;
		obj=0;
		
		DP("GETIN @loc({0})", loc);
		getin();
		if (!weq(wd1,"y")&&!weq(wd1,"yes")) return(2608);
		pspeak(dragon,1);
		prop[dragon]=2;
		prop[rug]=0;
		k=(plac[dragon]+fixd[dragon])/2;
		move(dragon+FIXED_OBJECT_OFFSET,-1);
		move(rug+FIXED_OBJECT_OFFSET,0);
		move(dragon,k);
		move(rug,k);
		for (obj=1; obj<=LAST_OBJECT_INDEX; obj++)
			if (place[obj]==plac[dragon]||place[obj]==fixd[dragon])
				move(obj,k);
		loc=k;
		k=0;
		return(8);
	}
	// ---------------------------------------------------------------------
	int trtoss_exit( int spk, int axe, int loc )
	{
		// l9175:  
		rspeak(spk);
		drop(axe,loc);
		k=0;
		return(8);
	}
	int trtoss()                                //  9170: throw                  
	{   
		int i;
		if (toting(rod2)&&obj==rod&&!toting(rod)) obj=rod2;
		if (!toting(obj)) return(L_PROMPT_SPK);
		if (obj>=FIRST_TREASURE_INDEX && obj<=LAST_TREASURE_INDEX && at(troll))
		{       
			spk=159;                        //  9178                 
			drop(obj,0);
			move(troll,0);
			move(troll+100,0);
			drop(troll2,plac[troll]);
			drop(troll2+100,fixd[troll]);
			juggle(chasm);
			return(L_PROMPT_SPK);
		}
		if (obj==food&&here(bear))
		{       
			obj=bear;                       //  9177                 
			return(9210);
		}
		if (obj!=axe) return(9020);
		for (i=1; i<=5; i++)
		{       
			if (dloc[i]==loc)
			{       
				spk=48;                 //  9172                 
				if (ran(3)==0||saved!= -1)
					return trtoss_exit( spk, axe,loc );
				dseen[i]=FALSE;
				dloc[i]=0;
				spk=47;
				dkill++;
				if (dkill==1) spk=149;
				return trtoss_exit( spk, axe,loc );
				//goto l9175;
			}
		}
		spk=152;
		if (at(dragon)&&prop[dragon]==0)
			return trtoss_exit( spk, axe,loc );
			//goto l9175;
		spk=158;
		if (at(troll)) 
			return trtoss_exit( spk, axe,loc );
			// goto l9175;
		if (here(bear)&&prop[bear]==0)
		{       
			spk=164;
			drop(axe,loc);
			fixed[axe]= -1;
			prop[axe]=1;
			juggle(bear);
			return(L_PROMPT_SPK);
		}
		obj=0;
		return(9120);
	}
	// ---------------------------------------------------------------------
	int trfeed()                                        //  9210                 
	{       
		if (obj==bird)
		{       
			spk=100;
			return(L_PROMPT_SPK);
		}
		if (obj==snake||obj==dragon||obj==troll)
		{       
			spk=102;
			if (obj==dragon&&prop[dragon]!=0) spk=110;
			if (obj==troll) spk=182;
			if (obj!=snake||closed||!here(bird)) return(L_PROMPT_SPK);
			spk=101;
			dstroy(bird);
			prop[bird]=0;
			tally2++;
			return(L_PROMPT_SPK);
		}
		if (obj==dwarf)
		{       
			if (!here(food)) return(L_PROMPT_SPK);
			spk=103;
			dflag++;
			return(L_PROMPT_SPK);
		}
		if (obj==bear)
		{       
			if (prop[bear]==0) spk=102;
			if (prop[bear]==3) spk=110;
			if (!here(food)) return(L_PROMPT_SPK);
			dstroy(food);
			prop[bear]=1;
			fixed[axe]=0;
			prop[axe]=0;
			spk=168;
			return(L_PROMPT_SPK);
		}
		spk=14;
		return(L_PROMPT_SPK);
	}
	// ---------------------------------------------------------------------
	int trfill()                                        //  9220 
	{       
		if (obj==vase)
		{       
			spk=29;
			if (liqloc(loc)==0) spk=144;
			if (liqloc(loc)==0||!toting(vase)) return(L_PROMPT_SPK);
			rspeak(145);
			prop[vase]=2;
			fixed[vase]= -1;
			return(9020);           //  advent/10 goes to 9024 
		}
		if (obj!=0&&obj!=bottle) return(L_PROMPT_SPK);
		if (obj==0&&!here(bottle)) return(8000);
		spk=107;
		if (liqloc(loc)==0) spk=106;
		if (liq(0)!=0) spk=105;
		if (spk!=107) return(L_PROMPT_SPK);
		prop[bottle]=((cond[loc]%4)/2)*2;
		k=liq(0);
		if (toting(bottle)) place[k]= -1;
		if (k==oil) spk=108;
		return(L_PROMPT_SPK);
	}
	// ---------------------------------------------------------------------
	int closing()                               //  10000 
	{       
		int i;
	
		prop[grate]=prop[fissur]=0;
		for (i=1; i<=6; i++)
		{       
			dseen[i]=FALSE;
			dloc[i]=0;
		}
		move(troll,0);
		move(troll+FIXED_OBJECT_OFFSET,0);
		move(troll2,plac[troll]);
		move(troll2+FIXED_OBJECT_OFFSET,fixd[troll]);
		juggle(chasm);
		if(prop[bear]!=3) dstroy(bear);
		prop[chain]=0;
		fixed[chain]=0;
		prop[axe]=0;
		fixed[axe]=0;
		rspeak(129);
		clock1 = -1;
		closng=TRUE;
		return(19999);
	}
	// ---------------------------------------------------------------------
	int caveclose()                             //  11000 
	{       
		int i;
		prop[bottle]=put(bottle,115,1);
		prop[plant]=put(plant,115,0);
		prop[oyster]=put(oyster,115,0);
		prop[lamp]=put(lamp,115,0);
		prop[rod]=put(rod,115,0);
		prop[dwarf]=put(dwarf,115,0);
		loc=115;
		oldloc=115;
		newloc=115;
	
		put(grate,116,0);
		prop[snake]=put(snake,116,1);
		prop[bird]=put(bird,116,1);
		prop[cage]=put(cage,116,0);
		prop[rod2]=put(rod2,116,0);
		prop[pillow]=put(pillow,116,0);
	
		prop[mirror]=put(mirror,115,0);
		fixed[mirror]=116;
	
		for (i=1; i<=FIXED_OBJECT_OFFSET; i++)
			if (toting(i)) dstroy(i);
		rspeak(132);
		closed=TRUE;
		return(2);
	}	
	// ---------------------------------------------------------------------
	int score()   //  sort of like 20000   
	{       
		int scor,i;
		mxscor=scor=0;
		for (i=FIRST_TREASURE_INDEX; i<=LAST_TREASURE_INDEX; i++)
		{	
			if (! hasPText(i) ) continue;
			k=12;
			if (i==chest) k=14;
			if (i>chest) k=16;
			if (prop[i]>=0) scor += 2;
			if (place[i]==3&&prop[i]==0) scor += k-2;
			mxscor += k;
		}
		scor += (maxdie-numdie)*10;
		mxscor += maxdie*10;
		if (!(scorng||gaveup)) scor += 4;
		mxscor += 4;
		if (dflag!=0) scor += 25;
		mxscor += 25;
		if (closng) scor += 25;
		mxscor += 25;
		if (closed)
		{       if (bonus==0) scor += 10;
			if (bonus==135) scor += 25;
			if (bonus==134) scor += 30;
			if (bonus==133) scor += 45;
		}
		mxscor += 45;
		if (place[magzin]==108) scor++;
		mxscor++;
		scor += 2;
		mxscor += 2;
		for (i=1; i<=hntmax; i++)
			if (hinted[i]) scor -= getHint(i, 2);
		return(scor);
	}
	// ---------------------------------------------------------------------
	//  entry=1 means goto 13000 */  /* game is over         
	//  entry=2 means goto 20000 */ /* 3=19000 
	void done(int entry)
	{   
		int score = score();
		if (entry==1) mspeak(1);
		if (entry==3) rspeak(136);
		printf(
			"You scored {0} out of a possible {1} using {2} turns.",
			score,
			mxscor,
			turns
		);
		printf("");
		LevelInfo info = null;
		int index = 0;
		
		
		while( null != (info = getLevelInfo( index++ ) ) )
		{
			if( info.max_score < score )
				continue;
			// Found the player's level
			printf( info.message );
			LevelInfo next_level = getLevelInfo( index );
			if( next_level == null )
			{
				printf("To achieve the next higher rating would be a neat trick!");
				printf("Congratulations!!");
			}
			else
			{
				int diff = info.max_score+1-score;
				printf(
					"To achieve the next higher rating, you need {0} more point{2}"
					,diff
					,(diff==1) ? "." : "s."
				);
			}
			exit(0);
		}
		exit(0);
	}
	// ---------------------------------------------------------------------
	//  label 90             
	int die(int entry)
	{
		int i;
		if (entry != 99)
		{       rspeak(23);
			oldlc2=loc;
		}
		if (closng)                             //  99                   
		{       rspeak(131);
			numdie++;
			done(2);
		}
		yea=yes(81+numdie*2,82+numdie*2,54);
		numdie++;
		if (numdie==maxdie || !yea) done(2);
		place[water]=0;
		place[oil]=0;
		if (toting(lamp)) prop[lamp]=0;
		for (i=FIXED_OBJECT_OFFSET; i>=1; i--)
		{       if (!toting(i)) continue;
			k=oldlc2;
			if (i==lamp) k=1;
			drop(i,k);
		}
		loc=3;
		oldloc=loc;
		return(2000);
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
	
		i=place[object];
		j=fixed[object];
		move(object,i);
		move(object+FIXED_OBJECT_OFFSET,j);
	}
	
	
	void move(int object, int where)
	{       
		int from;
	
		if (object<=FIXED_OBJECT_OFFSET)
			from=place[object];
		else
			from=fixed[object-FIXED_OBJECT_OFFSET];
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
			if (place[object]== -1) return;
			place[object] = -1;
			holdng++;
		}
		if (atloc[where]==object)
		{       
			atloc[where]=link[object];
			return;
		}
		for (temp=atloc[where]; link[temp]!=object; temp=link[temp]);
		link[temp]=link[object];
	}
	
	
	void drop(int object, int where)
	{	
		if (object>FIXED_OBJECT_OFFSET)
			fixed[object-FIXED_OBJECT_OFFSET]=where;
		else
		{       
			if (place[object]== -1) holdng--;
			place[object]=where;
		}
		if (where<=0) 
			return;
		link[object]=atloc[where];
		atloc[where]=object;
	}
	// ======================================================================
	// End of Stuff from vocab.c
	// ======================================================================

	
	
	// ---------------------------------------------------------------------
	//  everything for 1st time run  
	private void init()
	{
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
			fixed[j]=fixd[j];
			if (plac[j]!=0 && fixd[j]<=0)
				drop(j,plac[j]);
		}
		tally=0;
		tally2=0;
	
		for (i=FIRST_TREASURE_INDEX; i<=LAST_TREASURE_INDEX; i++)
		{       
			if ( hasPText(i) ) 
			{
				prop[i] = -1;
			}
			tally -= prop[i];
		}
	
		//  define mnemonics 
		keys = vocab("keys", 1);
		lamp = vocab("lamp", 1);
		grate = vocab("grate", 1);
		cage = vocab("cage", 1);
		rod = vocab("rod", 1);
		rod2=rod+1;
		steps = vocab("steps", 1);
		bird = vocab("bird", 1);
		door = vocab("door", 1);
		pillow = vocab("pillow", 1);
		snake = vocab("snake", 1);
		fissur = vocab("fissur", 1);
		tablet = vocab("tablet", 1);
		clam = vocab("clam", 1);
		oyster = vocab("oyster", 1);
		magzin = vocab("magaz", 1);
		dwarf = vocab("dwarf", 1);
		knife = vocab("knife", 1);
		food = vocab("food", 1);
		bottle = vocab("bottle", 1);
		water = vocab("water", 1);
		oil = vocab("oil", 1);
		plant = vocab("plant", 1);
		plant2=plant+1;
		axe = vocab("axe", 1);
		mirror = vocab("mirror", 1);
		dragon = vocab("dragon", 1);
		chasm = vocab("chasm", 1);
		troll = vocab("troll", 1);
		troll2=troll+1;
		bear = vocab("bear", 1);
		messag = vocab("messag", 1);
		vend = vocab("vendi", 1);
		batter = vocab("batter", 1);
		spices = vocab("spices", 1);

		nugget = vocab("nugget", 1);
		coins = vocab("coins", 1);
		chest = vocab("chest", 1);
		eggs = vocab("eggs", 1);
		tridnt = vocab("tride", 1);
		vase = vocab("vase", 1);
		emrald = vocab("emera", 1);
		pyram = vocab("pyram", 1);
		pearl = vocab("pearl", 1);
		rug = vocab("rug", 1);
		chain = vocab("chain", 1);

		back = vocab("back", 0);
		look = vocab("look", 0);
		cave = vocab("cave", 0);
		vnull = vocab("null", 0);
		entrnc = vocab("entra", 0);
		dprssn = vocab("depre", 0);
		enter = vocab("enter", 0);

		pour = vocab("pour", 2);
		say = vocab("say", 2);
		lock = vocab("lock", 2);
		vthrow = vocab("throw", 2);
		find = vocab("find", 2);
		invent = vocab("inven", 2);
	
		//  initialize dwarves 
		chloc=114;
		chloc2=140;
		for (i=1; i<=6; i++)
		{
			dseen[i]=false;
		}
		dflag=0;
		dloc[1]=19;
		dloc[2]=27;
		dloc[3]=33;
		dloc[4]=44;
		dloc[5]=64;
		dloc[6]=chloc;
		daltlc=18;
	
		//  random flags & ctrs 
		turns=0;
		lmwarn=false;
		iwest=0;
		knfloc=0;
		detail=0;
		abbnum=5;
		for (i=0; i<=4; i++)
		{
			if (hasRText(2*i+81) )
			{
				maxdie=i+1;
			}
		}
		numdie=holdng=dkill=foobar=bonus=0;
		clock1=30;
		clock2=50;
		saved_last_usage=0;
		closng=panic=closed=scorng=false;
	}
	// ---------------------------------------------------------------------
	private void getin()
	{
		AdvIO.getin(words);
		wd1 = words[0];
		wd2 = words[1];
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
	// ---------------------------------------------------------------------
	private void startup(  )
	{
		initRandomNumbers();
	
		demo = Start(0);
		//?? srand((int)(time((time_t *)NULL)));	//  random seed 
		//??  srand(371);				/* non-random seed 
		hinted[3] = yes(65,1,0);
		newloc=1;
		delhit = 0;
		limit=330;
		if (hinted[3])
		{
			limit=1000;      //  better batteries if instrucs 
		}
	}	
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
