package de.vetemi.translation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.vetemi.wordprocessing.IWordProcessor;

/**
 * Translator for German code terms based on the dict translation file.
 * 
 * @author Valmir Etemi
 * 
 */
public class DictTranslator extends AbstractTranslator {

	public DictTranslator(IWordProcessor wordProcessor) {
		super(wordProcessor);
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

	public String translate(String word) {
		HashMap<String, Integer> foundTranslations = new HashMap<String, Integer>();

		if (!isValid(word)) {
			return null;
		}

		// all saved as lower case
		String wordToTranslate = word.toLowerCase();

		// check if this word has been already translated
		String translatedString = alreadyTranslated.get(wordToTranslate);
		if (translatedString != null) {
			return translatedString;
		}

		// iterate through whole translation map.
		for (Map.Entry<String, String> translation : translationMap.entrySet()) {
			if (foundTranslations.size() <= MAX_FIND_TRANS) {
				// try with absolute word
				// TODO try with stemmed word
				// TODO try with lemmatized word
				
				// Clean and split key to different word parts in order to have a better check
				String cleanedKey = wordProcessor.cleanTranslation(translation.getKey());
				List<String> keyWords = Arrays.asList(cleanedKey.split("[-\\s]"));
				
				// translation found
				if (keyWords.contains(wordToTranslate)) {

					// clean translation
					String cleanedValue = wordProcessor.cleanTranslation(translation.getValue());

					// collect all words in a translation and count frequency
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
			// store word to translate to already translated map for efficient lookup
			alreadyTranslated.put(wordToTranslate, bestTranslation.getKey());
			return bestTranslation.getKey();
		}
		// store word to translate to already translated map for efficient lookup
		alreadyTranslated.put(wordToTranslate, "");
		return null;
	}

	/**
	 * Returns true if word has minimum length and is not a digit
	 * @param word to check
	 * @return true if valid and false if not valid
	 */
	private boolean isValid(String word) {
		if (word.toCharArray().length > MIN_WORD_TO_TRANS_LENGTH && !word.matches(".*\\d+.*")) {
			return true;
		}
		return false;
	}
}
