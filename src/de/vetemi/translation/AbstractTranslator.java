package de.vetemi.translation;

import java.util.HashMap;

import de.vetemi.wordprocessing.IWordProcessor;

/**
 * Abstract Translator as basis for different translators.
 * 
 * @author Valmir Etemi
 */
public abstract class AbstractTranslator {

	/**
	 * the word processor the specific translator
	 */
	protected IWordProcessor wordProcessor;

	/**
	 * Contains the imported translation file as German to English
	 */
	protected HashMap<String, String> translationMap;

	/**
	 * Map which contains already translated words as well as words which do not
	 * have a translation. Makes access more efficient
	 */
	protected HashMap<String, String> memoryMap;

	/**
	 * Translates word source and returns if translation available, null if
	 * nothing found first time and empty string if translator has already tried to
	 * translate but nothing found.
	 * 
	 * @param wordSource
	 * @return translation translation, null or empty String
	 */
	public abstract String translate(String wordSource);


	public AbstractTranslator(IWordProcessor wordProcessor) {
		this.wordProcessor = wordProcessor;
		memoryMap = new HashMap<String, String>();
	}
	
	public void setTranslationMap(HashMap<String, String> translationMap) {
		this.translationMap = translationMap;
	}

	public void setAlreadyTranslatedMap(HashMap<String, String> alreadyTranslatedMap) {
		this.memoryMap = alreadyTranslatedMap;
	}

}
