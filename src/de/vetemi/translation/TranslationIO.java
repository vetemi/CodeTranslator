package de.vetemi.translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
	private File alreadyTranslatedFile;

	/**
	 * The result of importing translation file will be stored in this map as German to English
	 */
	private HashMap<String, String> germanToEnglischTranslationMap;
	
	/**
	 * The result of importing translation file will be stored in this map as English to German
	 */
	private HashMap<String, String> englishToGermanTranslationMap;
	
	/**
	 * Contains the words to translate after importing
	 */
	private HashSet<String> translationSourceSet;
	
	/**
	 * Contains already translated words as map
	 */
	private HashMap<String, String> alreadyTranslatedMap;

	public TranslationIO() {
		super();
		germanToEnglischTranslationMap = new HashMap<String, String>();
		englishToGermanTranslationMap = new HashMap<String, String>();
		alreadyTranslatedMap = new HashMap<String,String>();
		translationSourceSet = new HashSet<String>();

		germanEnglischTranslationFile = new File("resource/GermanEnglishTranslations.txt");
		wordSourceFile = new File("resource/WordSource.txt");
		wordOutputFile = new File("resource/WordOutput.tsv");
		wordOutputNotTranslatedFile = new File("resource/WordOutputNotTranslated.tsv");
		alreadyTranslatedFile = new File("resource/AlreadyTranslated.tsv");
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
	 * Imports the translation files and stores the result in the English to German map and German to English map
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
	 * Imports the already translated words from previous runs for a more efficient access.
	 */
	public void importAlreadyFoundTranslations() {
		System.out.println("Start importing already translated words :" + alreadyTranslatedFile.getName());

		try {
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(alreadyTranslatedFile), "UTF-8"));

			if (alreadyTranslatedMap == null) {
				alreadyTranslatedMap = new HashMap<String,String>();
			}
			while (bReader.ready()) {
				String line = bReader.readLine();
				if (!line.isEmpty()) {
					String[] lineSplit = line.split("\t");
					if (lineSplit.length > 1) {
						alreadyTranslatedMap.put(lineSplit[0], lineSplit[1]);
					}
				}
			}
			bReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Finished importing already translated words. Size:" + alreadyTranslatedMap.size());
	}

	/**
	 * Takes the output map and writes result of translation in either WordOutput.tsv or WordOutputNotTranslated.tsv
	 * @param translationOutputMap the map which contains the translated words
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
						bWriterNotTranslated.write("\t");
						bWriterNotTranslated.write(english);
						bWriterNotTranslated.newLine();
					} else {
						bWriterOutput.write(german);
						bWriterOutput.write("\t");
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
	 * @param alreadyTranslated Map containing found translation
	 */
	public void exportAlreadyFoundTranslations(HashMap<String,String> alreadyTranslated) {
		System.out.println("Start exporting already found output file: " + alreadyTranslatedFile.getName());

		try {
			if (alreadyTranslatedFile.exists()) {
				alreadyTranslatedFile.delete();
			}
			alreadyTranslatedFile.createNewFile();

			BufferedWriter bWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(alreadyTranslatedFile), "UTF-8"));
			alreadyTranslated.forEach((source, translation) -> {
				try {
					bWriter.write(source);
					bWriter.write("\t");
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
		return alreadyTranslatedMap;
	}

}
