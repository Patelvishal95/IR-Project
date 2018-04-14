
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.*;

import java.io.*;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stemmer {

    private int MaxWordLength = 50;
    private Dictionary dic;
    private MorphologicalProcessor morph;
    private boolean IsInitialized = false;
    public HashMap AllWords = null;

    public Stemmer() {
        AllWords = new HashMap();

        try {
            JWNL.initialize(new FileInputStream("properties.xml"));
            dic = Dictionary.getInstance();
//             IndexWord word= dic.getIndexWord(POS.VERB,"run");
//         Synset[] senses = word.getSenses();
//         for (int i = 0; i < senses.length; i++) {
//          System.out.println(word + ": " + senses[i].getGloss());
//            morph = dic.getMorphologicalProcessor();
//            // ((AbstractCachingDictionary)dic).
//            //	setCacheCapacity (10000);
//            IsInitialized = true;
//        }
        morph = dic.getMorphologicalProcessor();
        IsInitialized=true;
        } catch (FileNotFoundException e) {
            System.out.println( "Error initializing Stemmer:JWNLproperties.xml not found" );
		}
		catch (JWNLException e) {
            System.out.println("Error initializing Stemmer: " + e.toString());
        }
    }   
    public void Unload ()
	{ 
		dic.close();
		Dictionary.uninstall();
		JWNL.shutdown();
	}
    /* stems a word with wordnet
	 * @param word word to stem
	 * @return the stemmed word or null if it was not found in WordNet
	 */
	public String StemWordWithWordNet ( String word )
	{
		if ( !IsInitialized )
			return word;
		if ( word == null ) return null;
		if ( morph == null ) morph = dic.getMorphologicalProcessor();
		
		IndexWord w;
		try
		{
			w = morph.lookupBaseForm( POS.VERB, word );
			if ( w != null )
				return w.getLemma().toString ();
			w = morph.lookupBaseForm( POS.NOUN, word );
			if ( w != null )
				return w.getLemma().toString();
			w = morph.lookupBaseForm( POS.ADJECTIVE, word );
			if ( w != null )
				return w.getLemma().toString();
			w = morph.lookupBaseForm( POS.ADVERB, word );
			if ( w != null )
				return w.getLemma().toString();
		} 
		catch ( JWNLException e )
		{
		}
		return null;
	}
        /**
	 * Stem a single word
	 * tries to look up the word in the AllWords HashMap
	 * If the word is not found it is stemmed with WordNet
	 * and put into AllWords
	 * 
	 * @param word word to be stemmed
	 * @return stemmed word
	 */
	public String Stem( String word )
	{
		// check if we already know the word
		String stemmedword = (String) AllWords.get( word );
		if ( stemmedword != null )
			return stemmedword; // return it if we already know it
		
			// unknown word: try to stem it
			stemmedword = StemWordWithWordNet (word);
		
		if ( stemmedword != null )
		{
			// word was recognized and stemmed with wordnet:
			// add it to hashmap and return the stemmed word
			AllWords.put( word, stemmedword );
			return stemmedword;
		}
		// word could not be stemmed by wordnet, 
		// thus it is no correct english word
		// just add it to the list of known words so 
		// we won't have to look it up again
		AllWords.put( word, word );
		return word;
	}
        /**
	 * performs Stem on each element in the given Vector
	 * 
	 */
	public Vector Stem ( Vector words )
	{
		if ( !IsInitialized )
			return words;
		
		for ( int i = 0; i < words.size(); i++ )
		{
			words.set( i, Stem( (String)words.get( i ) ) );
		}
		return words;		
	}

}
