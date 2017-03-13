package de.vetemi.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.vetemi.translation.AbstractTranslator;
import de.vetemi.translation.DictTranslator;
import de.vetemi.translation.TranslationIO;
import de.vetemi.wordprocessing.DictCodeWordProcessor;

/**
 * This class has the control over the whole translation process Each step will
 * be called during process translation
 * 
 * @author Valmir Etemi
 *
 */
public class ProcessHandler {

	/**
	 * Imported translation file as German to English translation map
	 */
	private HashMap<String, String> germanToEnglishTranslationMap;
	/**
	 * Imported translation file as English to German translation map for
	 * cleaning
	 */
	private HashMap<String, String> englishToGermanTranslationMap;
	/**
	 * Imported words which should be translated
	 */
	private HashSet<String> translationSourceSet;
	/**
	 * The output map for all translation (even if no translation available)
	 */
	private HashMap<String, String> translationOutputMap = new HashMap<String, String>();
	
	/**
	 * Contains all already translated words from previous runs
	 */
	private HashMap<String, String> memoryTranslationMap;

	/**
	 * IO component for imports and exports
	 */
	private TranslationIO translationIO;
	/**
	 * Translator component which does the actual translation
	 */
	private AbstractTranslator translator;
	/**
	 * Word processor which does pre and post processing of translation and
	 * source
	 */
	private DictCodeWordProcessor wordProcessor;

	public ProcessHandler() {
		translationIO = new TranslationIO();
		wordProcessor = new DictCodeWordProcessor();
		translator = new DictTranslator(wordProcessor);
	}

	/**
	 * Contains the process workflow
	 */
	public void processTranslationWorkFlow() {
		System.out.println("----------------------------------");
		System.out.println("Start translation process workflow");
		System.out.println("----------------------------------");

		// Step 1: import and configure files
		importFiles();

		// Step 2: clean data
		cleanSourceSet();

		// Step 3: translate
		translate();

		// Step 4: export output
		exportOutput();
		System.out.println("----------------------------------");
		System.out.println("End translation process workflow");
		System.out.println("----------------------------------");
	}

	/**
	 * Calls the IO component and exports output
	 */
	private void exportOutput() {
		System.out.println("Start exporting output");
		translationIO.exportOutput(translationOutputMap);
		translationIO.exportMemoryTranslation(memoryTranslationMap);
		System.out.println("End exporting output");
		System.out.println("----------------------------------");
	}

	/**
	 * Calls the translation component, calls word processor component to clean
	 * both translation and source word. Stores output in map
	 */
	private void translate() {
		System.out.println("Start translating");
		long start = System.currentTimeMillis();

		if (translationOutputMap == null) {
			translationOutputMap = new HashMap<String, String>();
		}

		translator.setTranslationMap(germanToEnglishTranslationMap);
		translator.setAlreadyTranslatedMap(memoryTranslationMap);

		boolean hasTranslation = false;
		List<String> wordParts;
		List<String> translatedWordParts;
		String totalTranslation;

		for (String wordSource : translationSourceSet) {
			System.out.print("Source: " + wordSource + "\t");
			wordParts = wordProcessor.getWordParts(wordSource);
			translatedWordParts = new ArrayList<String>();
			totalTranslation = "";
			for (String wordPart : wordParts) {
								
				// try first without cleaning word
				String translatedWord = translator.translate(wordPart);

				// if nothing found try with cleaned word
				if (translatedWord == null || translatedWord.isEmpty()) {
					String cleanedWort = wordProcessor.cleanWordToTranslate(wordPart);
					if (!cleanedWort.equals(wordPart))
						translatedWord = translator.translate(cleanedWort);
				}

				if (translatedWord == null || translatedWord.isEmpty()) {
					// nothing found
					translatedWordParts.add(wordPart);
				} else {
					translatedWordParts.add(translatedWord);
					hasTranslation = true;
				}
			}
			// put total translated word in map. Could be empty if no
			// translation was found
			if (hasTranslation) {
				totalTranslation = wordProcessor.convertWordToOrigin(translatedWordParts, wordParts, wordSource);
				translationOutputMap.put(wordSource, totalTranslation);
				hasTranslation = false;
				System.out.println("Translation: " + totalTranslation);
			} else {
				translationOutputMap.put(wordSource, "");
				System.out.println("Translation: None");
			}
		}

		long end = System.currentTimeMillis();
		System.out.println("Translator.translate() --> Time: " + (end - start));
		System.out.println("End translating");
		System.out.println("----------------------------------");
	}

	/**
	 * Calls IO component and its import functionality
	 */
	private void importFiles() {
		System.out.println("Start importing files");

		translationIO.importWordSource();
		translationIO.importTranslationMaps();
		translationIO.importMemoryMap();

		translationSourceSet = translationIO.getTranslationSourceSet();
		germanToEnglishTranslationMap = translationIO.getGermanToEnglischTranslationMap();
		englishToGermanTranslationMap = translationIO.getEnglishToGermanTranslationMap();
		memoryTranslationMap = translationIO.getAlreadyFoundTranslationMap();

		System.out.println("Successfully imported translationFile? " + !germanToEnglishTranslationMap.isEmpty());
		System.out.println("Successfully imported word source file? " + !translationSourceSet.isEmpty());
		System.out.println("----------------------------------");
	}

	/**
	 * Cleans the source set. If it is not translatable, then no translation
	 * needed.
	 */
	private void cleanSourceSet() {
		System.out.println("Start cleaning source set");
		// Remove words that contain only english words
		for (Iterator<String> i = translationSourceSet.iterator(); i.hasNext();) {
			String wordSource = i.next();
			if (!isTranslatable(wordSource)) {
				i.remove();
			}
		}
		System.out.println("End cleaning source set");
		System.out.println("----------------------------------");
	}

	/**
	 * The validation function. Takes word source and splits into word parts and
	 * checks for every word part if it is translatable. If at least one part is
	 * translatable, then whole word is translatable
	 * 
	 * @param wordSource
	 * @return true if translatable and false if not
	 */
	private boolean isTranslatable(String wordSource) {
		List<String> wordParts = null;
		boolean isTranslatable = false;

		// this returns at least the wordSource so that List has at least one
		// element
		wordParts = wordProcessor.getWordParts(wordSource);
		for (String word : wordParts) {
			isTranslatable = checkWordIsTranslatable(word);
		}
		return isTranslatable;
	}

	/**
	 * The actual validation functionality. Takes word and tries to match with
	 * English to German translation map in order to find out if it is a English word.
	 * Also checks if word is digit.
	 * 
	 * @param word
	 * @return true if no English word or Digit, otherwise false
	 */
	private boolean checkWordIsTranslatable(String word) {
		if (!englishToGermanTranslationMap.containsKey(word.toLowerCase()) && !word.matches("\\d+")
				&& !englishToGermanTranslationMap.containsKey("to " + word.toLowerCase())) {
			return true;
		}
		return false;
	}

}
