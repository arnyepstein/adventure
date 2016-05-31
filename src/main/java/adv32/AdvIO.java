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

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

public class AdvIO extends DataFile
{
	public AdvIO( )
	{
	}
	static
	{
		_reader = 
			new BufferedReader(
				new InputStreamReader( System.in )
			);
	}
	// ---------------------------------------------------------------------
	public static String getLine(BufferedReader in) {
		while(true) {
			String line = getAnyLine(in);
			if(line.charAt(0) != '#') {
				return line;
			}
		}
	}
	// ---------------------------------------------------------------------
	public static String getAnyLine(BufferedReader in)
	{
		while( true )
		{
			String line = null;
			try
			{
				line = in.readLine();
				if( line == null )
				{
					throw new IllegalStateException("Unexpected EOF in DataFile");
				}
				line = line.trim();
				// Skip Blank Lines
				if(line.length()>0) {
					return line;
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	// ---------------------------------------------------------------------
	public static char getChar(BufferedReader in)
	{
		char ch = 0;
		String line = getLine( in );
		if( line.length() > 0 )
		{
			ch = line.charAt(0);
		}
		return ch;
	}
	// ---------------------------------------------------------------------
	private static String _getInputLine()
	{
		return getLine(_reader);
	}
	// ---------------------------------------------------------------------
	private static char _getInputChar()
	{
		char ch = 0;
		String line = _getInputLine();
		if( line.length() > 0 )
		{
			ch = line.charAt(0);
		}
		return ch;
	}

	// ---------------------------------------------------------------------
    public static void getin(String words[])		
    {	   
		while(true)
		{	 
			String line = _getInputLine();

			words[0] = words[1] = null;
			int index_of_space = line.indexOf(' ');
			if( index_of_space == -1 )
			{
				words[0] = line;
				line = "";
			}
			else
			{
				words[0] = line.substring(0, index_of_space);
				line = line.substring( index_of_space + 1).trim();
			}
			if( words[0].length() == 0 )
			{
				break;
			}

			index_of_space = line.indexOf(' ');
			if( index_of_space == -1 )
			{
				words[1] = line;
			}
			else
			{
				words[1] = line.substring(0, index_of_space);
				line = line.substring( index_of_space + 1).trim();
			}
			break;
		}

//		if( words[0] != null )
//			System.out.println("Word1: " + words[0]);
//		if( words[1] != null )
//			System.out.println("Word2: " + words[1]);
    }

	// ---------------------------------------------------------------------
	//  confirm irreversible action  
	static boolean confirm(String mesg)
	{	   
		System.out.print( mesg );
		return _getInputChar() == 'y';
	}
	// ---------------------------------------------------------------------
	public boolean yes(int question, int true_response, int false_response)
	{
		boolean result  = true;
		for (;;)
		{
			rspeak(question);	 //  tell him what we want
			char ch = _getInputChar();

			if (ch=='y')
			{
				break;
			}
			if(ch =='n')
			{
				result = false;
				break;
			}
			printf("Please answer the question.");
		}
		rspeak(result ? true_response : false_response );
		return(result);
	}
	// ---------------------------------------------------------------------
	public boolean yesm(int question, int true_response, int false_response)
	{
		boolean result  = true;
		for (;;)
		{
			mspeak(question);	 //  tell him what we want
			char ch = _getInputChar();

			if (ch=='y')
			{
				break;
			}
			if(ch =='n')
			{
				result = false;
				break;
			}
			printf("Please answer the question.");
		}
		mspeak(result ? true_response : false_response );
		return(result);
	}
	// ---------------------------------------------------------------------
	public void initRandomNumbers()
	{
		_random = new Random(new Date().getTime());
	}
	// ---------------------------------------------------------------------
	public int ran(int range)
	{
		return _random.nextInt(range);
	}
	// ---------------------------------------------------------------------
	private static void DP( String fmt, Object... args )
	{
		String msg = sprintf(fmt, (Object[]) args);
		printf("[AdvIO] " + msg);
	}
	// ---------------------------------------------------------------------
	private static BufferedReader _reader;
	public java.util.Random _random;
}
