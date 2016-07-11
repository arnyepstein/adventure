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
	public interface OutputChannel {
		void emitOutputToUser(String msg);
	}
	// ===========================================================================
	static class MessageListSet extends ArrayList<MessageList> {
		public MessageListSet() { super(256); }
		public MessageListSet(int initialCapacity) { super(initialCapacity); }

		public void addMessageToList( int listNumber, String message ) {
			this.ensureCapacity(listNumber+1);
			while(size() <= listNumber) {
				super.add(null);
			}

			MessageList list = this.get(listNumber);
			if(list == null) {
				list = new MessageList(message);
				set(listNumber, list);
			} else {
				list.add(message);
			}
		}

		@Override
		public MessageList get(int index) {
			return (index >=0 && index < size()) ? super.get(index) : null;
		}

	}
	// ===========================================================================
	static class MessageList extends ArrayList<String> {
		public MessageList(String message) {
			super(4);
			this.add( message );
		}
	}
	// ===========================================================================
	static class ClassMessage
	{
		int max_score;
		String message;
		public ClassMessage(int max_score, String message )
		{
			this.max_score = max_score;
			this.message = message;
		}
	}
	// ===========================================================================
	static class MsgInfo
	{
		int first_string_number = -1;
		int last_string_number = -1;
	}
	// ===========================================================================
	private static class ObjectDescriptors
	{
		String description_string = null;
		MessageListSet messageSet =  new MessageListSet(4);
		int place;
		int fixedLoc;

		ObjectDescriptors(String description) {
			this.description_string = description;
		}
		public MessageList getList(int index) {
			return messageSet.get(index);
		}
		public void addMessageToList(int listNumber, String message) {
			messageSet.addMessageToList(listNumber, message);
		}
	}
	// ---------------------------------------------------------------------
	private OutputChannel outputChannel;

	public DataFile()
	{
	}
	// ---------------------------------------------------------------------
	public void setOutputChannel(OutputChannel outputChannel) {
		this.outputChannel = outputChannel;
	}
	// ---------------------------------------------------------------------
	public void rspeak( int msg_id )
	{
		_speak(randomDescriptionSet, msg_id );
	}
	// ---------------------------------------------------------------------
	public boolean hasRText( int msg_id )
	{
		return _hasText(randomDescriptionSet, msg_id );
	}
	// ---------------------------------------------------------------------
	public boolean hasPText( int msg_id )
	{
		return msg_id >=0 && msg_id < objectDescriptorsList.size() && null != objectDescriptorsList.get(msg_id);
	}
	// ---------------------------------------------------------------------
	public boolean hasLText( int msg_id )
	{
		return _hasText(longDescriptionSet, msg_id );
	}
	// ---------------------------------------------------------------------
	private boolean _hasText( MessageListSet messageSet, int msg_id )
	{
		return null != messageSet.get(msg_id);
	}
	// ---------------------------------------------------------------------
	public MessageList getMText( int msg_id )
	{
		return magicDescriptionSet.get(msg_id);
	}
	// ---------------------------------------------------------------------
	public MessageList getLText( int msg_id )
	{
		return longDescriptionSet.get(msg_id);
	}
	// ---------------------------------------------------------------------
	public MessageList getRText( int msg_id )
	{
		return randomDescriptionSet.get(msg_id);
	}
	// ---------------------------------------------------------------------
	public MessageList getSText( int msg_id )
	{
		return shortDescriptionSet.get(msg_id);
	}
	// ---------------------------------------------------------------------
	public void mspeak( int msg_id )
	{
		_speak(magicDescriptionSet, msg_id );
	}
	// ---------------------------------------------------------------------
	public void lspeak( int msg_id )
	{
		_speak(longDescriptionSet, msg_id );
	}
	// ---------------------------------------------------------------------
	public void pspeak( int propid, int msgid )
	{
		ObjectDescriptors descriptor = objectDescriptorsList.get(propid);
		if(msgid == -1) {
			emitOutputToUser( descriptor.description_string );
		} else {
			_speak(descriptor.messageSet, msgid);
		}
	}
	// ---------------------------------------------------------------------
	public void speak( MessageList list )
	{
		if(list != null) {
			for (String s : list) {
				emitOutputToUser( s );
			}
		}
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
	public ClassMessage getLevelInfo(int index )
	{
		return ( index < classMessageList.size() ) ? classMessageList.get(index) : null;
	}
	// ===========================================================================
	public static class VocabEntry {
		public VocabEntry(String name, int entryId) {
			usage = Usage.fromValue(entryId/1000);
			this.entryId = entryId;
			this.id = entryId % 1000;
			addName(name);
		}
		public int getEntryId() {
			return entryId;
		}
		public int getId() {
			return id;
		}
		public void addName(String name) {
			names.add(name);
		}
		int entryId;
		int id;
		Usage usage;
		List<String> names = new ArrayList<>(4);
	}
	// ---------------------------------------------------------------------
	enum Usage {
		DESTINATION,
		OBJECT,
		VERB,
		MAGIC,
		ANY;

		public static Usage fromValue(int value) {
			return values()[value];
		}

	}
	// ---------------------------------------------------------------------
	public VocabEntry vocabEntry( String s, Usage usage )
	{
		// Lookup
		String key = s;
		if( s.length() > 5 )
		{
			key = s.substring(0, 5);
		}
		key = key.toLowerCase();

		// Each map entry is a list of interpretations based on usage.
		List<VocabEntry> list = _vocab.get( key );
		VocabEntry entry = null;
		if(list != null) {
			for(VocabEntry e : list) {
				if(usage == Usage.ANY || usage == e.usage) {
					entry = e;
					break;
				}
			}
		}
		if( entry == null )
		{
			if(usage == Usage.ANY)
			{
				if( db_dump_vocab )
				{
					printf("Vocab({0}, -1): -1", key);
				}
				return null;
			}
			throw new RuntimeException("Unable to find '" + s + "' in vocab");
		}
		
		if( db_dump_vocab )
		{
			printf("Vocab({0}, {1}): t={2}, val={3}", key, usage, entry.usage, entry.getEntryId());
		}
		return entry;
	}
	// ---------------------------------------------------------------------
	public int vocab( String s, Usage usage ) {
		VocabEntry entry = vocabEntry(s, usage);
		if (entry == null) {
			return -1;
		}
		if( usage == Usage.ANY )
		{
			return entry.entryId;
		}
		if( usage == entry.usage ) {
			return entry.id;
		}
		throw new RuntimeException(
			"Found '"+s+"' with value "+entry.getEntryId()+" and usage "+entry.usage +", but looking for type " + usage
		);

	}
	// ---------------------------------------------------------------------
	public String sprintf( String fmt, Object... args )
	{
		String msg = fmt;
		if( args != null && args.length >0 )
		{
			msg = MessageFormat.format( fmt, (Object[])args );
		}
		return msg;
	}
	// ---------------------------------------------------------------------
	public void printf( String fmt, Object... args )
	{
		String msg = fmt;
		if( args != null && args.length >0 )
		{
			msg = MessageFormat.format( fmt, (Object[])args );
		}
		emitOutputToUser( msg );
	}
	// ---------------------------------------------------------------------
	public void emitOutputToUser(String msg) {
		if(outputChannel != null) {
			outputChannel.emitOutputToUser(msg);
		} else {
			System.out.println( msg );
		}
	}
	// ---------------------------------------------------------------------
	public void exit(int id)
	{
		System.exit(id);
	}
	// ---------------------------------------------------------------------
	public void loadDataFile()   // "read" data from virtual file
	{
		// DP("Enter RDATA()");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
					this.getClass().getClassLoader().getResourceAsStream( "glorkz" )
				)
			);

		int curline = 0;
		while( true )
		{
			// Calculate the Section Number
			String line;
			do {
				line = AdvIO.getAnyLine(reader);
				++curline;
				if (line == null) {
					System.out.println("Unexpected EOF from Configuration Data");
					System.exit(3);
				}
			} while(line.length() == 0 || line.charAt(0) == '#');

			int section = atoi(line);
			//DP( "Section Number: {0}", section );
			switch(section)
			{
				case 0:			 //  finished reading database	
					return;
				case 1:			 //  long form descriptions	   
					loadDescriptions(reader, longDescriptionSet);
					break;
				case 2:			 //  short form descriptions	  
					loadDescriptions(reader, shortDescriptionSet);
					break;
				case 3:			 //  travel table	
					loadTravelData(reader);
					break;
				case 4:			 //  vocabulary				   
					loadVocabulary(reader);
					break;
				case 5:			 //  object descriptions		  
					loadObjectDescriptions(reader);
					break;
				case 6:			 //  arbitrary messages		   
					loadDescriptions(reader, randomDescriptionSet);
					break;
				case 7:			 //  object locations			 
					loadObjectLocations(reader);
					break;
				case 8:			 //  action defaults			  
					loadActionDefaults(reader);
					break;
				case 9:			 //  liquid assets				
					loadLiquidAssets(reader);
					break;
				case 10:			//  class messages			   
					loadClassMessages(reader);
					break;
				case 11:			//  hints						
					loadHints(reader);
					break;
				case 12:			//  magic messages			   
					loadDescriptions(reader, magicDescriptionSet);
					break;
				default:
					DP(
						"Invalid data section number({0}) from line: {1}", 
						section,
						curline
					);
					break;
			}
		}
	}
	private final static String SPLIT_PATTERN = "[\\t ]";
	// ---------------------------------------------------------------------
	void loadDescriptions(BufferedReader reader, MessageListSet listSet)
	{
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN,2);
			int listNumber = Integer.valueOf(parts[0]);
			if(listNumber<0) {
				break;
			}
			String msg = parts[1];
			listSet.addMessageToList(listNumber, msg);
		}
	}
	// ---------------------------------------------------------------------
	void loadClassMessages(BufferedReader reader)
	{
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN,2);
			int score = Integer.valueOf(parts[0]);
			if(score<0) {
				break;
			}
			String msg = parts[1];

			ClassMessage info = new ClassMessage( score, msg );
			classMessageList.add( info );
			//DP("Msg#({0}), ({1})", msgno, msg );
		}
	}
	// ---------------------------------------------------------------------
	void loadObjectDescriptions(BufferedReader reader)
	{
		int outer_msgno = -1;
		ObjectDescriptors rminfo = null;
		ArrayList<MsgInfo> list = null;
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN,2);
			int listNumber = Integer.valueOf(parts[0]);
			if(listNumber<0) {
				break;
			}
			String msg = parts[1];

			if( listNumber>0 && listNumber < 100 )
			{
				// Begin a new Object Descriptor
				outer_msgno = listNumber;
				while( outer_msgno >= objectDescriptorsList.size() )
				{
					objectDescriptorsList.add( new ObjectDescriptors(null) );
				}
				rminfo = new ObjectDescriptors(msg);
				objectDescriptorsList.set(outer_msgno, rminfo);
			}
			else
			{
				rminfo.addMessageToList(listNumber/100, msg);
			}
		}
	}
	// ---------------------------------------------------------------------
	void loadTravelData(BufferedReader reader)
	{
		int entries = 0;
		int oldloc= -1;
		while(true)			//  get another line			 
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN);
			int locc = Integer.valueOf(parts[0]);
			if(locc == -1) {
				break;
			}

			while(navConfigs.size() < (locc+1)) {
				navConfigs.add(new NavConfig(navConfigs.size()));
			}
			NavConfig navConfig = navConfigs.get(locc);

			// Get the newloc number
			int v1 = atoi(parts[1]);
			int conditions = v1/1000;
			int destLoc = v1%1000;

			NavConfigEntry navConfigEntry = new NavConfigEntry();
			navConfigEntry.conditions = conditions;
			navConfigEntry.destLoc = destLoc;
			if(navConfig.first == null) {
				navConfig.first = navConfigEntry;
			} else {
				navConfig.last.next = navConfigEntry;
			}
			navConfig.last = navConfigEntry;

			for(int ord=2; ord<parts.length; ord++)
			{
				int verbId = atoi(parts[ord]);
				navConfigEntry.verbIdSet.set(verbId);
			}
		}
	}
	// ---------------------------------------------------------------------
	void loadVocabulary(BufferedReader reader)
	{
		if( db_dump_travel )
		{
			_verbname = new String[LAST_VERB_ID+1];
			_objectname = new String[LAST_OBJECT_INDEX+1];
		}
		List<VocabEntry> vocabIndex = new ArrayList<>(4000);
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN);
			int id = atoi(parts[0]);
			if( id == -1 )
			{
				break;
			}
			String name = parts[1];
			while(vocabIndex.size() < (id+1)) {
				vocabIndex.add(null);
			}

			VocabEntry entry = vocabIndex.get(id);
			if(entry == null) {
				entry = new VocabEntry(name, id);
				vocabIndex.set(id, entry);
			} else {
				entry.addName(name);
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

		}
		// Now we have all of the entries, create a map based on their names
		// A name may refer to multiple entries with different semantics
		for(VocabEntry entry : vocabIndex) {
			if(entry == null) continue;
			for(String name : entry.names) {
				List<VocabEntry> list = _vocab.get(name);
				if(list == null) {
					list = new ArrayList<>();
					_vocab.put(name, list);
				}
				list.add(entry);
			}
		}
	}
	// ---------------------------------------------------------------------
	void loadObjectLocations(BufferedReader reader)
	{
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN);
			int objid = atoi(parts[0]);
			if( objid == -1 )
			{
				break;
			}
			ObjectDescriptors desc = objectDescriptorsList.get(objid);
			int place = atoi(parts[1]);
			int value = (parts.length == 3) ? atoi(parts[2]) : 0;

			desc.place = place;
			desc.fixedLoc = value;

			plac[objid] = place;
			fixd[objid] = value;
			//DP("Loc for Obj[{0}]: plac({1}), fixd({2})", objid, plac[objid], fixd[objid] );
		}
	}
	// ---------------------------------------------------------------------
	void loadActionDefaults(BufferedReader reader)
	{
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN);

			int verbid = atoi(parts[0]);
			if( verbid == -1 )
			{
				break;
			}
			actspk[verbid] = atoi(parts[1]);
			//DP("ActSpk for Verb[{0}]: msgid({1})", verbid, actspk[verbid]);
			//rspeak( actspk[verbid] );
		}
	}
	// ---------------------------------------------------------------------
	void loadLiquidAssets(BufferedReader reader)
	{
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN);
			int bitnum = atoi(parts[0]);
			if( bitnum == -1 )
			{
				break;
			}
			int flag = 1<<bitnum;
			for(int ord=1; ord<parts.length; ord++)
			{
				cond[ atoi(parts[ord])] |= flag;
			}
		}
		for( int i=0; i<cond.length; i++ )
		{
			//DP("cond[{0}] = 0x{1}", i, Integer.toHexString(cond[i]) );
		}
		
	}
	// ---------------------------------------------------------------------
	void loadHints(BufferedReader reader)
	{
		while( true )
		{
			String line = AdvIO.getLine(reader);
			String parts[] = line.split(SPLIT_PATTERN);
			int hintnum = atoi(parts[0]);
			if( hintnum == -1 )
			{
				break;
			}
			while( hintnum >= _hints.size() )
			{
				_hints.add( new int[5] );
			}
			int hints[] = _hints.get( hintnum );
			for(int ord=1; ord<parts.length; ord++)
			{
				hints[ ord-1 ] = atoi(parts[ord]);
			}
			//DP("hint[{0}] = {1} {2} {3} {4} {5}",  hintnum, hints[0], hints[1], hints[2], hints[3], hints[4] );
		}
	}
	// ---------------------------------------------------------------------
	void dump_travel()
	{
		for(NavConfig config : navConfigs) {
			// Info for Travel from Location[it].
			if(config == null) {
				continue;
			}

			int sourceLoc = config.sourceLoc;
			String desc = getPlaceDescription(sourceLoc);
			printf("[{0}, cond({1})]: {2}", sourceLoc, Integer.toHexString(cond[sourceLoc]),desc);
			for (int oix = 0; oix<=LAST_OBJECT_INDEX; oix++)
			{
				if (gameData.fixed[oix] == sourceLoc)
				{
					printf(" FIXD: {0}", _objectname[oix]);
				}
				if (gameData.place[oix] == sourceLoc)
				{
					printf(" PLAC: {0}", _objectname[oix]);
				}
			}



			StringBuilder sb = new StringBuilder();
			for(NavConfigEntry entry = config.first; entry != null; entry = entry.next) {
				sb.setLength(0);

				// New Location
				sb.append( "  To get to [" )
					.append(getPlaceDescription(entry.destLoc)).append(']');
				if( entry.conditions != 0 ) {
					sb.append(" with cond(")
						.append(entry.conditions).append(')');
				}
				String prefix = "";
				sb.append(" say:");
				int val = 0;
				while(-1 != (val = entry.verbIdSet.nextSetBit(val+1))) {
					sb.append(prefix).append(_getVerbName(val));
					prefix = " or ";
				}
			}
			System.out.println(sb.toString());
		}
	}

	String getPlaceDescription(int placeId) {
		MessageList messageList = getMessageListForPlace(placeId);
		return (messageList != null) ? messageList.get(0) : "????";
	}
	// ---------------------------------------------------------------------
	private MessageList getMessageListForPlace(int placeId) {
		MessageList messageList = shortDescriptionSet.get(placeId);
		if(messageList == null)
		{
			messageList = longDescriptionSet.get(placeId);
		}
		return messageList;
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
	private int atoi(String num_text)
	{
		return Integer.parseInt( num_text );
	}
	// ---------------------------------------------------------------------
	private void _speak( MessageListSet listSet, int msg_id )
	{
		if( msg_id < listSet.size() ) {
			speak(listSet.get(msg_id));
		}
	}
	// ---------------------------------------------------------------------
	private void DP( String fmt, Object... args )
	{
//		String msg = sprintf(fmt, (Object[]) args);
//		printf("[DataFile] " + msg);
	}
	// ---------------------------------------------------------------------
	private MessageListSet longDescriptionSet = new MessageListSet();
	private MessageListSet shortDescriptionSet = new MessageListSet();
	private MessageListSet randomDescriptionSet = new MessageListSet();
	private MessageListSet magicDescriptionSet = new MessageListSet();
	private ArrayList<ObjectDescriptors> objectDescriptorsList = new ArrayList<ObjectDescriptors>();
	private ArrayList<ClassMessage> classMessageList = new ArrayList<ClassMessage>();
	private HashMap<String, List<VocabEntry>> _vocab = new HashMap<>();
	
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
