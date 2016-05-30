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

public class DataFile extends AdvGameData
{
	
	private BufferedReader _in;
	// ---------------------------------------------------------------------
	static class LevelInfo
	{
		int max_score;
		String message;
		public LevelInfo( int max_score, String message )
		{
			this.max_score = max_score;
			this.message = message;
		}
	}
	// ---------------------------------------------------------------------
	static class MsgInfo
	{
		int first_string_number = -1;
		int last_string_number = -1;
	}
	// ---------------------------------------------------------------------
	private static class RMsgInfo
	{
		String description_string = null;
		ArrayList<MsgInfo> info_list = new ArrayList<MsgInfo>(4);
	}
	// ---------------------------------------------------------------------

	public DataFile()
	{
		
		rdata();
	}
	// ---------------------------------------------------------------------
	public void rspeak( int msg_id )
	{
		_speak( _rtext, msg_id );
	}
	// ---------------------------------------------------------------------
	public boolean hasRText( int msg_id )
	{
		return _hasText( _rtext, msg_id );
	}
	// ---------------------------------------------------------------------
	public boolean hasPText( int msg_id )
	{
		return 
			(msg_id < _ptext.size())
			&& _ptext.get(msg_id).description_string != null;
	}
	// ---------------------------------------------------------------------
	public boolean hasLText( int msg_id )
	{
		return _hasText( _ltext, msg_id );
	}
	// ---------------------------------------------------------------------
	private boolean _hasText( ArrayList<MsgInfo> list, int msg_id )
	{
		if( msg_id < list.size() )
		{
			MsgInfo info = list.get( msg_id );
			if( info != null && info.first_string_number != -1 )
			{
				return true;
			}
		}
		return false;
	}
	// ---------------------------------------------------------------------
	public MsgInfo getMText( int msg_id )
	{
		return _getText( _mtext, msg_id );
	}
	// ---------------------------------------------------------------------
	public MsgInfo getLText( int msg_id )
	{
		return _getText( _ltext, msg_id );
	}
	// ---------------------------------------------------------------------
	public MsgInfo getRText( int msg_id )
	{
		return _getText( _rtext, msg_id );
	}
	// ---------------------------------------------------------------------
	public MsgInfo getSText( int msg_id )
	{
		return _getText( _stext, msg_id );
	}
	// ---------------------------------------------------------------------
	private MsgInfo _getText( ArrayList<MsgInfo> list, int msg_id )
	{
		if( msg_id < list.size() )
		{
			MsgInfo info = list.get( msg_id );
			if( info != null && info.first_string_number != -1 )
			{
				return info;
			}
		}
		return null;
	}
	// ---------------------------------------------------------------------
	public void mspeak( int msg_id )
	{
		_speak( _mtext, msg_id );
	}
	// ---------------------------------------------------------------------
	public void lspeak( int msg_id )
	{
		_speak( _ltext, msg_id );
	}
	// ---------------------------------------------------------------------
	public void pspeak( int msg_id, int skip )
	{
		_speak( _ptext.get(msg_id).info_list, skip );
	}
	// ---------------------------------------------------------------------
	public void speak( MsgInfo info )
	{
		//DP("Speak (from {0} to {1})", info.first_string_number, info.last_string_number);
		for( int ix = info.first_string_number;
			 ix > 0 && ix <= info.last_string_number;
			 ++ix )
		{
			System.out.println( _strings.get(ix) );
		}
	}
	// ---------------------------------------------------------------------
	public TravList getTravel( int where )
	{
		if( where >=0 && where < _travel.size() )
		{
			return _travel.get( where );
		}
		return null;
		//throw new RuntimeException("Invalid input to getTravel: " + where );
	}
	// ---------------------------------------------------------------------
	public int getHint( int hintid, int which )
	{
		if( hintid >=0 && hintid < _hints.size() )
		{
			return _hints.get( hintid )[which];
		}
		throw new RuntimeException("Invalid input to getHint: " + hintid + ", " + which );
	}
	// ---------------------------------------------------------------------
	public LevelInfo getLevelInfo( int index )
	{
		return ( index < _ctext.size() ) ? _ctext.get(index) : null;
	}
	// ---------------------------------------------------------------------
	public int vocab( String s, int type )
	{
		// Lookup
		String key = s;
		if( s.length() > 5 )
		{
			key = s.substring(0, 5);
		}
		key = key.toLowerCase();
		Integer value = _vocab.get( key );
		if( value == null )
		{
			if(type == -1)
			{
				if( db_dump_vocab )
				{
					printf("Vocab({0}, -1): -1", key);
				}
				return -1;
			}
			throw new RuntimeException("Unable to find '" + s + "' in vocab");
		}
		
		int val = value.intValue();
		int vtype = val/1000;
		int vval = val%1000;
		if( db_dump_vocab )
		{
			printf("Vocab({0}, {1}): t={2}, val={3}", key, type, vtype, vval);
		}
		
		if( type == -1 )
		{
			return val;
		}
		if( vtype == type )
		{
			return vval;
		}
		throw new RuntimeException(
			"Found '"+s+"' with value "+val+", looking for type " + type
		);
	}
	// ---------------------------------------------------------------------
	public static String sprintf( String fmt, Object... args )
	{
		String msg = fmt;
		if( args != null && args.length >0 )
		{
			msg = MessageFormat.format( fmt, (Object[])args );
		}
		return msg;
	}
	// ---------------------------------------------------------------------
	public static void printf( String fmt, Object... args )
	{
		String msg = fmt;
		if( args != null && args.length >0 )
		{
			msg = MessageFormat.format( fmt, (Object[])args );
		}
		System.out.println( msg );
	}
	// ---------------------------------------------------------------------
	public void exit(int id)
	{
		System.exit(id);
	}
	// ---------------------------------------------------------------------
	private void rdata()   // "read" data from virtual file
	{
		// DP("Enter RDATA()");
		_in = new BufferedReader(
				new InputStreamReader(
					ClassLoader.getSystemResourceAsStream( "glorkz" )
				)
			);
		while( true )
		{
			// Calculate the Section Number
			_getLine();
			int section = _getNumber();
			//DP( "Section Number: {0}", section );
			switch(section)
			{
				case 0:			 //  finished reading database	
					return;
				case 1:			 //  long form descriptions	   
					rdesc(_ltext);
					break;
				case 2:			 //  short form descriptions	  
					rdesc(_stext);
					break;
				case 3:			 //  travel table	
					rtrav();
					break;
				case 4:			 //  vocabulary				   
					rvoc();
					break;
				case 5:			 //  object descriptions		  
					rdescp();
					break;
				case 6:			 //  arbitrary messages		   
					rdesc(_rtext);
					break;
				case 7:			 //  object locations			 
					rlocs();
					break;
				case 8:			 //  action defaults			  
					rdflt();
					break;
				case 9:			 //  liquid assets				
					rliq();
					break;
				case 10:			//  class messages			   
					rdescc();
					break;
				case 11:			//  hints						
					rhints();
					break;
				case 12:			//  magic messages			   
					rdesc(_mtext);
					break;
				default:
					DP(
						"Invalid data section number({0}) from line: {1}", 
						section,
						_curline
					);
					break;
			}
		}
	}
	// ---------------------------------------------------------------------
	void rdesc(ArrayList<MsgInfo> list)
	{
		while( true )
		{
			_getLine();
			int msgno = _getNumber();
			if( msgno < 0 )
			{
				break;
			}
			_skipWS();
			String msg = _curline.substring( _curindex );
			_addMessage( list, msgno, msg );
			//DP("Msg#({0}), ({1})", msgno, msg );
		}
	}
	// ---------------------------------------------------------------------
	void rdescc()
	{
		while( true )
		{
			_getLine();
			int score = _getNumber();
			if( score < 0 )
			{
				break;
			}
			_skipWS();
			String msg = _curline.substring( _curindex );
			LevelInfo info = new LevelInfo( score, msg );
			_ctext.add( info );
			//DP("Msg#({0}), ({1})", msgno, msg );
		}
	}
	// ---------------------------------------------------------------------
	void rdescp()
	{
		int outer_msgno = -1;
		RMsgInfo rminfo = null;
		ArrayList<MsgInfo> list = null;
		while( true )
		{
			_getLine();
			int msgno = _getNumber();
			if( msgno < 0 )
			{
				break;
			}
			_skipWS();
			String msg = _curline.substring( _curindex );
			
			if( msgno==0 || msgno >= 100 )
			{
				_addMessage( rminfo.info_list, msgno/100, msg );
				//DP("Msg#({0}.{1}), ({2})", outer_msgno, msgno/100, msg );
			}
			else
			{
				outer_msgno = msgno;
				while( outer_msgno >= _ptext.size() )
				{
					_ptext.add( new RMsgInfo() );
				}
				rminfo = _ptext.get( outer_msgno );
				rminfo.description_string = msg;
			}
		}
	}
	// ---------------------------------------------------------------------
	void rtrav()
	{
		TravList t = null;
		int entries = 0;
		int oldloc= -1;
		while(true)			//  get another line			 
		{	   
			_getLine();
			int locc = _getNumber();

			if (locc != oldloc && oldloc>=0) //  end of entry 
			{
				t.next = null;	//  terminate the old entry	  
				//DP("{0}:{1} entries", oldloc, entries );
			//	   twrite(oldloc);								 
			}
			if (locc== -1)
			{
				break;
			}
			if (locc != oldloc)		//  getting a new entry		 
			{	
				while( locc >= _travel.size() )
				{
					_travel.add( new TravList() );
				}
				t  = _travel.get( locc );
				
				//DP("New travel list for {0}",locc);		
				entries = 0;
				oldloc = locc;
			}
			// Get the newloc number
			int v1 = _getNumber();
			int m = v1/1000;
			int n = v1%1000;
			_skipWS();	   
			while (_curindex < _curlength )	 //  only do one line at a time   
			{
				if (0 != (entries++) )
				{
					t.next = new TravList();
					t = t.next;
				}
				t.tverb= _getNumber();	//  get verb from the file	   
				t.tloc = n;	  			//  table entry mod 1000		 
				t.conditions = m;		//  table entry / 1000	
				_skipWS();	   
				//DP("entry {0} for {1}", entries, locc );	   
			}
		}
	}
	// ---------------------------------------------------------------------
	void rvoc()
	{
		if( db_dump_travel )
		{
			_verbname = new String[LAST_VERB_ID+1];
			_objectname = new String[LAST_OBJECT_INDEX+1];
		}
		Integer value = new Integer(2);
		while( true )
		{
			_getLine();
			int id = _getNumber();
			if( id == -1 )
			{
				break;
			}
			String name = _getToken();
			if( id != value.intValue() )
			{
				value = new Integer(id);
			}
			if( db_dump_travel )
			{
				if(id <= LAST_VERB_ID )
				{
					_verbname[id] = name;
				}
				else if( (id/1000) == 1 )
				{
					_objectname[id-1000] = name;
				}
			}
			
			//DP("Vocab; {0}={1}", value.intValue(), name );
			
			_vocab.put( name, value );
		}
	}
	// ---------------------------------------------------------------------
	void rlocs()
	{
		while( true )
		{
			_getLine();
			int objid = _getNumber();
			if( objid == -1 )
			{
				break;
			}
			plac[objid] = _getNumber();
			_skipWS();
			if( _curindex < _curlength )
			{
				fixd[objid] = _getNumber();
			}
			else
			{
				fixd[objid] = 0;
			}
			//DP("Loc for Obj[{0}]: plac({1}), fixd({2})", objid, plac[objid], fixd[objid] );
		}
	}
	// ---------------------------------------------------------------------
	void rdflt()
	{
		while( true )
		{
			_getLine();
			int verbid = _getNumber();
			if( verbid == -1 )
			{
				break;
			}
			actspk[verbid] = _getNumber();
			//DP("ActSpk for Verb[{0}]: msgid({1})", verbid, actspk[verbid]);
			//rspeak( actspk[verbid] );
		}
	}
	// ---------------------------------------------------------------------
	void rliq()
	{
		while( true )
		{
			_getLine();
			int bitnum = _getNumber();
			if( bitnum == -1 )
			{
				break;
			}
			int flag = 1<<bitnum;
			do
			{
				cond[ _getNumber()] |= flag;
				_skipWS();
			}
			while( _curindex < _curlength );
		}
		for( int i=0; i<cond.length; i++ )
		{
			//DP("cond[{0}] = 0x{1}", i, Integer.toHexString(cond[i]) );
		}
		
	}
	// ---------------------------------------------------------------------
	void rhints()
	{
		while( true )
		{
			_getLine();
			int hintnum = _getNumber();
			if( hintnum == -1 )
			{
				break;
			}
			while( hintnum >= _hints.size() )
			{
				_hints.add( new int[5] );
			}
			int hints[] = _hints.get( hintnum );
			int ix = 0;
			do
			{
				hints[ ix++ ] = _getNumber();
				_skipWS();
			}
			while( _curindex < _curlength );
			//DP("hint[{0}] = {1} {2} {3} {4} {5}",  hintnum, hints[0], hints[1], hints[2], hints[3], hints[4] );
		}
	}
	// ---------------------------------------------------------------------
	void dump_travel()
	{
		StringBuilder sb = null;
		for(int it=1; it<_travel.size(); it++)
		{
			TravList t = getTravel(it);
			if( t == null )
			{
				continue;
			}
			
			// Info for Travel from Location[it].
			int desc_id = -1;
			if( it < _stext.size() )
			{
				desc_id = _stext.get(it).first_string_number;
			}
			if(desc_id == -1)
			{
				desc_id = _ltext.get(it).first_string_number;
			}
			String desc = (desc_id != -1) ? _strings.get(desc_id) : "????";
			printf("[{0}, cond({1})]: {2}", it, Integer.toHexString(cond[it]),desc);
			for (int oix = 0; oix<=LAST_OBJECT_INDEX; oix++)
			{
				if (fixed[oix] == it)
				{
					printf(" FIXD: {0}", _objectname[oix]);
				}
				if (place[oix] == it)
				{
					printf(" PLAC: {0}", _objectname[oix]);
				}
			}
			
			
			int prev_to_loc = -1;
			for( TravList tl = t; tl != null; tl = tl.next)
			{
				int cur_to_loc = tl.tloc;
				if( cur_to_loc != prev_to_loc )
				{
					if( prev_to_loc != -1 )
					{
						System.out.println(sb.toString());
					}
					sb = new StringBuilder();
					prev_to_loc = cur_to_loc;
					// New Location
					sb.append( "  To get to [" )
						.append(cur_to_loc).append(']');
					if( tl.conditions != 0 )
					{
						sb.append(" with cond(")
							.append(tl.conditions).append(')');
					}
					sb.append(" say:")
						.append(_getVerbName(tl.tverb));
				}
				else
				{
					sb.append('/').append(_getVerbName(tl.tverb));
				}
			}
			System.out.println(sb.toString());
		}
	}
	
	// ---------------------------------------------------------------------
	private String _getVerbName(int id)
	{
		String name = null;
		if( id < _verbname.length )
		{
			name = _verbname[id];
		}
		return (name == null )
					? ("verb[" + id + "]")
					: name;
			
		
	}
	// ---------------------------------------------------------------------
	private void _skipWS ()
	{
		while( _curindex < _curlength 
			   && ( _curline.charAt(_curindex ) == '\t' 
			   || _curline.charAt(_curindex ) == ' ' ) )
		{
			++_curindex;
		}
	}
	// ---------------------------------------------------------------------
	private void _spanToWS()
	{
		while( _curindex < _curlength 
			   && _curline.charAt(_curindex ) != '\t' 
			   && _curline.charAt(_curindex ) != ' ' )
		{
			++_curindex;
		}
	}
	// ---------------------------------------------------------------------
	private String _getToken ()
	{
		_skipWS(  );
		int first = _curindex;
		_spanToWS();
		//DP("Token; {0}:{1}={2}", _curline, first, _curindex );
		String token = _curline.substring( first, _curindex );
		return token;
	}
	// ---------------------------------------------------------------------
	private int atoi(String num_text)
	{
		return Integer.parseInt( num_text );
	}
	// ---------------------------------------------------------------------
	private int _getNumber()
	{
		String num_text = _getToken();
		return Integer.parseInt( num_text );
	}
	// ---------------------------------------------------------------------
	private void _getLine()
	{
		do
		{
			_curline =  AdvIO.getLine( _in );
			_curlength = _curline == null ? 0 : _curline.length();
			_curindex = 0;
		}
		while( _curlength > 0 && _curline.charAt(0) == '#' );
	}
	// ---------------------------------------------------------------------
	private void _speak( ArrayList<MsgInfo> list, int msg_id )
	{
		MsgInfo info = list.get( msg_id );
		if( info == null )
		{
			throw new RuntimeException( "No Message Number: " + msg_id );
		}
		speak( info );				
	}
	// ---------------------------------------------------------------------
	private void _addMessage( ArrayList<MsgInfo> list, int msg_id, String msg )
	{
		// Save the String
		int string_number = _addString( msg );

		while( msg_id >= list.size() )
		{
			list.add( new MsgInfo() );
		}
		MsgInfo info = list.get( msg_id );
		if( info.first_string_number == -1 )
		{
			info.first_string_number = string_number;
		}
		info.last_string_number = string_number;
	}
	// ---------------------------------------------------------------------
	private int _addString( String msg )
	{
		// Save the String
		int string_number = _strings.size();
		_strings.add( msg );
		return string_number;
	}
	// ---------------------------------------------------------------------
	private static void DP( String fmt, Object... args )
	{
		String msg = sprintf(fmt, (Object[]) args);
		printf("[DataFile] " + msg);
	}
	// ---------------------------------------------------------------------
	private String _curline;
	private int _curindex;
	private int _curlength;
	private ArrayList<String> _strings = new ArrayList<String>();
	private ArrayList<MsgInfo> _ltext = new ArrayList<MsgInfo>();
	private ArrayList<MsgInfo> _stext = new ArrayList<MsgInfo>();
	private ArrayList<RMsgInfo> _ptext = new ArrayList<RMsgInfo>();
	private ArrayList<MsgInfo> _rtext = new ArrayList<MsgInfo>();
	private ArrayList<LevelInfo> _ctext = new ArrayList<LevelInfo>();
	private ArrayList<MsgInfo> _mtext = new ArrayList<MsgInfo>();
	private ArrayList<TravList> _travel = new ArrayList<TravList>();
	private HashMap<String, Integer> _vocab = new HashMap<String, Integer>();
	
	private final static int LAST_VERB_ID = 77;
	private String _verbname[];
	private String _objectname[];
	
	// various condition bits
	public int cond[] = new int[LAST_LOCATION_INDEX+1];
	private ArrayList<int[]> _hints = new ArrayList<int[]>();
	
	// DEBUG Configuration
	static boolean db_dump_travel = 
		System.getProperty("dump.travel") != null;
	static boolean db_dump_vocab = 
		System.getProperty("dump.vocab") != null;

}
