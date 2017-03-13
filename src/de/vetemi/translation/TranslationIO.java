package de.vetemi.translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The IO component for importing and exporting files. Has its files already
 * configured.
 * 
 * @author Valmir Etemi
 */
public class TranslationIO {

	/**
	 * Constant for separating values in the output
	 */
	private final String SEPARATOR = ";";

	/**
	 * File which contains the German to English mapping
	 */
	private File germanEnglischTranslationFile;

	/**
	 * Source file which contains the words to translate
	 */
	private File wordSourceFile;

	/**
	 * Output file which contains the translated words
	 */
	private File wordOutputFile;

	/**
	 * Output files which contains the words which were not able to translate
	 */
	private File wordOutputNotTranslatedFile;

	/**
	 * Already translated files for more efficient access
	 */
	private File memoryTranslationFile;

	/**
	 * The result of importing translation file will be stored in this map as
	 * German to English
	 */
	private HashMap<String, String> germanToEnglischTranslationMap;

	/**
	 * The result of importing translation file will be stored in this map as
	 * English to German
	 */
	private HashMap<String, String> englishToGermanTranslationMap;

	/**
	 * Contains the words to translate after importing
	 */
	private HashSet<String> translationSourceSet;

	/**
	 * Contains already translated words as map
	 */
	private HashMap<String, String> memoryTranslationMap;

	public TranslationIO() {
		super();
		germanToEnglischTranslationMap = new HashMap<String, String>();
		englishToGermanTranslationMap = new HashMap<String, String>();
		memoryTranslationMap = new HashMap<String, String>();
		translationSourceSet = new HashSet<String>();

		germanEnglischTranslationFile = new File("resource/GermanEnglishTranslations.txt");
		wordSourceFile = new File("resource/WordSource.txt");
		wordOutputFile = new File("resource/WordOutput.txt");
		wordOutputNotTranslatedFile = new File("resource/WordOutputNotTranslated.txt");
		memoryTranslationFile = new File("resource/TranslationMemory.txt");
	}

	/**
	 * Imports the words to translate and stores it to the set
	 */
	public void importWordSource() {
		System.out.println("Start importing word source file:" + wordSourceFile.getName());

		try {
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(wordSourceFile), "UTF-8"));

			if (translationSourceSet == null) {
				translationSourceSet = new HashSet<String>();
			}
			while (bReader.ready()) {
				String line = bReader.readLine();
				if (!line.isEmpty()) {
					translationSourceSet.add(line);
				}
			}
			bReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Finished importing word source file. Size:" + translationSourceSet.size());
	}

	/**
	 * Imports the translation files and stores the result in the English to
	 * German map and German to English map
	 */
	public void importTranslationMaps() {
		System.out
				.println("Start importing german english translation file:" + germanEnglischTranslationFile.getName());
		try {
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(germanEnglischTranslationFile), "UTF-8"));
			while (bReader.ready()) {
				String line = bReader.readLine();
				if (!line.isEmpty()) {
					String[] lineSplit = line.split("\t");
					if (lineSplit.length > 1) {
						germanToEnglischTranslationMap.put(lineSplit[0].toLowerCase(), lineSplit[1].toLowerCase());
						englishToGermanTranslationMap.put(lineSplit[1].toLowerCase(), lineSplit[0].toLowerCase());
					}
				}
			}
			bReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println(
				"Finished importing german english translation file. Size:" + germanToEnglischTranslationMap.size());
	}

	/**
	 * Imports the already translated words from previous runs for a more
	 * efficient access.
	 */
	public void importMemoryMap() {
		System.out.println("Start importing already translated words :" + memoryTranslationFile.getName());

		if (memoryTranslationMap == null) {
			memoryTranslationMap = new HashMap<String, String>();
		}

		try {
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(memoryTranslationFile), "UTF-8"));

			while (bReader.ready()) {
				String line = bReader.readLine();
				if (!line.isEmpty()) {
					String[] lineSplit = line.split(SEPARATOR);
					if (lineSplit.length > 1) {
						memoryTranslationMap.put(lineSplit[0], lineSplit[1]);
					} else {
						memoryTranslationMap.put(lineSplit[0], "");
					}
				}
			}
			bReader.close();
		} catch (FileNotFoundException notFoundException) {
			System.out.println("No memory map");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Finished importing already translated words. Size:" + memoryTranslationMap.size());
	}

	/**
	 * Takes the output map and writes result of translation in either
	 * WordOutput.tsv or WordOutputNotTranslated.tsv
	 * 
	 * @param translationOutputMap
	 *            the map which contains the translated words
	 */
	public void exportOutput(HashMap<String, String> translationOutputMap) {
		System.out.println("Start exporting translation output file: " + wordOutputFile.getName() + " and "
				+ wordOutputNotTranslatedFile.getName());

		try {
			if (wordOutputFile.exists()) {
				wordOutputFile.delete();
			}
			wordOutputFile.createNewFile();

			if (wordOutputNotTranslatedFile.exists()) {
				wordOutputNotTranslatedFile.delete();
			}
			wordOutputNotTranslatedFile.createNewFile();

			BufferedWriter bWriterOutput = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(wordOutputFile), "UTF-8"));
			BufferedWriter bWriterNotTranslated = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(wordOutputNotTranslatedFile), "UTF-8"));
			translationOutputMap.forEach((german, english) -> {
				try {
					if (english.isEmpty()) {
						bWriterNotTranslated.write(german);
						bWriterNotTranslated.write(SEPARATOR);
						bWriterNotTranslated.write(english);
						bWriterNotTranslated.newLine();
					} else {
						bWriterOutput.write(german);
						bWriterOutput.write(SEPARATOR);
						bWriterOutput.write(english);
						bWriterOutput.newLine();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			bWriterNotTranslated.close();
			bWriterOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("End exporting translation output file");
	}

	/**
	 * Exports all found translations for a more efficient future access
	 * 
	 * @param memoryTranslations
	 *            Map containing found translation
	 */
	public void exportMemoryTranslation(HashMap<String, String> memoryTranslations) {
		System.out.println("Start exporting already found output file: " + memoryTranslationFile.getName());

		try {
			if (memoryTranslationFile.exists()) {
				memoryTranslationFile.delete();
			}
			memoryTranslationFile.createNewFile();

			BufferedWriter bWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(memoryTranslationFile), "UTF-8"));
			memoryTranslations.forEach((source, translation) -> {
				try {
					bWriter.write(source);
					bWriter.write(SEPARATOR);
					bWriter.write(translation);
					bWriter.newLine();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			bWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("End exporting already translated file");
	}

	public HashMap<String, String> getGermanToEnglischTranslationMap() {
		return germanToEnglischTranslationMap;
	}

	public void setGermanToEnglischTranslationMap(HashMap<String, String> germanToEnglischTranslationMap) {
		this.germanToEnglischTranslationMap = germanToEnglischTranslationMap;
	}

	public HashMap<String, String> getEnglishToGermanTranslationMap() {
		return englishToGermanTranslationMap;
	}

	public void setEnglishToGermanTranslationMap(HashMap<String, String> englishToGermanTranslationMap) {
		this.englishToGermanTranslationMap = englishToGermanTranslationMap;
	}

	public HashSet<String> getTranslationSourceSet() {
		return translationSourceSet;
	}

	public void setTranslationSourceSet(HashSet<String> translationSourceSet) {
		this.translationSourceSet = translationSourceSet;
	}

	public HashMap<String, String> getAlreadyFoundTranslationMap() {
		return memoryTranslationMap;
	}

}
