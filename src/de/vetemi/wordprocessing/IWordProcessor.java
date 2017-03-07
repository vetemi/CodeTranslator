package de.vetemi.wordprocessing;

import java.util.List;

/**
 * Interface for specific word processors based on different translation bases
 * 
 * @author Valmir Etemi
 */
public interface IWordProcessor {

	/**
	 * Splits String into word parts and returns them. May use different
	 * delimiters
	 * 
	 * @param wordSource
	 *            the String which should be split into parts
	 * @return List of Strings containing the split parts
	 */
	public List<String> getWordParts(String wordSource);

	/**
	 * Converts the translated word parts as a whole String according to the
	 * delimiter and the word parts of the origin
	 * 
	 * @param target
	 *            The target word parts which should be converted
	 * @param originWordParts
	 *            According to the origin parts the target will be converted
	 * @param originWord
	 *            Used to get the delimiter
	 * @return The converted word parts as whole String
	 */
	public String convertWordToOrigin(List<String> target, List<String> originWordParts, String originWord);

	/**
	 * Cleans the word to translate (source word)
	 * 
	 * @param wordToClean
	 *            source word
	 * @return the cleaned word
	 */
	public String cleanWordToTranslate(String wordToClean);

	/**
	 * Cleans the translated word
	 * 
	 * @param translationValue
	 *            the translated word
	 * @return the cleaned word
	 */
	public String cleanTranslation(String translationValue);

}
