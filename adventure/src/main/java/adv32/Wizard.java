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

public class Wizard extends AdvIO
{
	public Wizard( )
	{
	}

	int datime()
	{   
		int now = (int) (new Date().getTime() / 60000L);
		return now;
	}
	
	
	// ---------------------------------------------------------------------
	public void poof()
	{
		this.gameData.latncy = 45;
	}
	// ---------------------------------------------------------------------
	public boolean Start(int ignored)
	{
		int delay = datime() - this.gameData.saved_last_usage;
		if (delay >= this.gameData.latncy)
		{
			this.gameData.saved_last_usage = -1;
			return false;
		}
		printf(
			"This adventure was suspended a mere {0} minute{1} ago.",
			delay,
			delay == 1 ? "" : "s"
		);
		if (delay <= this.gameData.latncy /3)
		{
			mspeak(2);
			exit(0);
		}
		mspeak(8);
		if (!wizard())
		{       
			mspeak(9);
			exit(0);
		}
		this.gameData.saved_last_usage = -1;
		return false;
	}
	// ---------------------------------------------------------------------
	
	// not as complex as advent/10 (for now)
	private boolean wizard()
	{
		String words[] = new String[2];
		if (! yesm(16,0,7))
		{
			return false;
		}
		mspeak(17);
		getin( words );
		if (!weq( words[0], _magic) )
		{       
			mspeak(20);
			return false;
		}
		mspeak(19);
		return true;
	}
	// ---------------------------------------------------------------------
    private boolean weq(String a, String b)
    {
		return b == a
			  || (b != null && b.equals( a ) );
    }
	// ---------------------------------------------------------------------
	void ciao(String cmdfile)
	{
		// TODO:: Implement this!
	/*
		char *c;
		int outfd, size;
		char fname[80], buf[512];
		long filesize;
	
		printf("What would you like to call the saved version?\n");
		for (c=fname;; c++)
			if ((*c=getchar())=='\n') break;
		*c=0;
		if (save(fname) != 0) return;           // Save failed
		printf("To resume, say \"adventure %s\".\n", fname);
		printf("\"With these rooms I might now have been familiarly acquainted.\"\n");
		exit(0);
	*/
	}
	// ---------------------------------------------------------------------
	private void DP( String fmt, Object... args )
	{
//		String msg = sprintf(fmt, (Object[]) args);
//		printf("[Wizard] " + msg);
	}
	// ---------------------------------------------------------------------
	private String _magic = "dwarf";
	
}
