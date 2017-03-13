package de.vetemi.translation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.danielnaber.jwordsplitter.AbstractWordSplitter;
import de.danielnaber.jwordsplitter.GermanWordSplitter;
import de.vetemi.wordprocessing.IWordProcessor;

/**
 * Translator for German code terms based on the dict translation file.
 * 
 * @author Valmir Etemi
 * 
 */
public class DictTranslator extends AbstractTranslator {

	/**
	 * Splitter to decompound German word composition, e.g. "Donaudampfschiff"
	 * -> "Donau","Dampf","Schiff"
	 */
	AbstractWordSplitter compositionSplitter;

	public DictTranslator(IWordProcessor wordProcessor) {
		super(wordProcessor);

		try {
			compositionSplitter = new GermanWordSplitter(true);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/** Constant for the maximum of translations to found per word */
	private final int MAX_FIND_TRANS = 40;
	/**
	 * Constant for the minimum a word to translate or translation itself must
	 * have
	 */
	private final int MIN_WORD_TO_TRANS_LENGTH = 3;
	/**
	 * Constant for the maximum of words one single translation can contain
	 * (avoid quotes)
	 */
	private final int MAX_WORDS_TRANS_CONTAINS = 4;

	@Override
	public String translate(String wordSource) {

		if (!isValid(wordSource)) {
			return null;
		}

		// try whole word without decomposition
		String totalTranslation = translateWord(wordSource);
		if (!totalTranslation.isEmpty()) {
			return totalTranslation;
		}

		// Decompose German compounded words
		List<String> decompoundedWords = compositionSplitter.splitWord(wordSource);
		boolean hasTranslation = false;

		for (String word : decompoundedWords) {
			String translatedWord = translateWord(word);
			if (translatedWord.isEmpty()) {
				totalTranslation += "N/A";
			} else {
				totalTranslation += translatedWord;
				hasTranslation = true;
			}
		}
		// Save translation
		if (hasTranslation) {
			return totalTranslation;
		}
		return null;
	}

	/**
	 * Does the actual translation. Uses lemmatization to find the translation
	 * for the base word. Collects the count for every found translation and
	 * returns the value with the highest count. Assumption is that highest
	 * count is best translation
	 * 
	 * @param word
	 *            the word to translate
	 * @return the translation with highest frequency in the translation map
	 */
	private String translateWord(String word) {

		// absolute word as lower case
		String absoluteWord = word.toLowerCase();

		// check if this word has been already translated
		String translatedString = memoryMap.get(absoluteWord);
		if (translatedString != null) {
			return translatedString;
		}

		HashMap<String, Integer> foundTranslations = new HashMap<String, Integer>();

		// iterate through whole translation map until enough translations are found
		for (Map.Entry<String, String> translation : translationMap.entrySet()) {
			if (foundTranslations.size() <= MAX_FIND_TRANS) {

				// Clean and split key to different word parts in order to
				// have a better check
				String cleanedKey = wordProcessor.cleanTranslation(translation.getKey());
				List<String> keyWords = Arrays.asList(cleanedKey.split("[-\\s]"));

				// translation found
				if (keyWords.contains(absoluteWord)) {

					// clean translation
					String cleanedValue = wordProcessor.cleanTranslation(translation.getValue());

					// collect all words in a translation and count
					// frequency
					String[] split = cleanedValue.split("[-\\s]");
					if (split.length <= MAX_WORDS_TRANS_CONTAINS) {
						for (String splitWord : split) {
							if (!splitWord.isEmpty() && (splitWord.toCharArray().length >= MIN_WORD_TO_TRANS_LENGTH)) {
								Integer counter = foundTranslations.get(splitWord);
								if (counter == null) {
									counter = 1;
								} else {
									counter++;
								}
								foundTranslations.put(splitWord, counter);
							}
						}
					}
				}
			} else {
				break;
			}
		}

		// Check if translation has been found
		if (!foundTranslations.isEmpty()) {
			Map.Entry<String, Integer> bestTranslation = foundTranslations.entrySet().iterator().next();
			// Iterate through found translations and return the best value
			for (Map.Entry<String, Integer> currentTranslation : foundTranslations.entrySet()) {
				if (bestTranslation.getValue() < currentTranslation.getValue()) {
					bestTranslation = currentTranslation;
				}
			}
			memoryMap.put(absoluteWord, bestTranslation.getKey());
			return bestTranslation.getKey();
		} else {
			memoryMap.put(absoluteWord, "");
			return "";
		}

	}

	/**
	 * Returns true if word has minimum length and is not a digit
	 * 
	 * @param word
	 *            to check
	 * @return true if valid and false if not valid
	 */
	private boolean isValid(String word) {
		if (word.toCharArray().length > MIN_WORD_TO_TRANS_LENGTH && !word.matches(".*\\d+.*")) {
			return true;
		}
		return false;
	}
}
