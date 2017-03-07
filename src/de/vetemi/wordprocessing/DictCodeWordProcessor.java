package de.vetemi.wordprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word processor which is based on dict's (dict.cc) translation file and the
 * program code to translate
 * 
 * @author Valmir Etemi
 */
public class DictCodeWordProcessor implements IWordProcessor {

	private final String CAMELTITELCASE_DELIMITER = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
	private final String HYPHEN_DELIMITER = "-";
	private final String UNDERSCORE_DELIMITER = "_";

	/**
	 * Contains all pre defined delimiters
	 */
	private List<String> delimiterList;
	
	
	/**
	 * Contains the cleaning regex rules
	 */
	private List<String> removeRegexRules;
	
	/**
	 * Contains the umlauts regex rules for German
	 */
	private Map<String, String> umlautsRegexRules;

	public DictCodeWordProcessor() {
		super();
		configureRemoveRegexRules();
		configureUmlautsRegexRules();
		configureDelimiters();
	}

	public List<String> getWordParts(String wordSource) {
		String[] wordParts;
		if (wordSource.contains(UNDERSCORE_DELIMITER)) {
			wordParts = wordSource.split(UNDERSCORE_DELIMITER);
		} else if (wordSource.contains(HYPHEN_DELIMITER)) {
			wordParts = wordSource.split(HYPHEN_DELIMITER);
		} else {
			wordParts = wordSource.split(CAMELTITELCASE_DELIMITER);
		}
		return Arrays.asList(wordParts);
	}

	public String convertWordToOrigin(List<String> target, List<String> originWordParts, String originWord) {
		// get delimiter of origin
		Delimiter delimiter = getDelimiter(originWord);
		switch (delimiter) {
		case CAMELTITLECASE:
			return convertWordWithDelimiter(target, originWordParts, CAMELTITELCASE_DELIMITER);
		case HYPHEN:
			return convertWordWithDelimiter(target, originWordParts, HYPHEN_DELIMITER);
		case UNDERSCORE:
			return convertWordWithDelimiter(target, originWordParts, UNDERSCORE_DELIMITER);
		case NODELIMITER:
		default:
			return convertWordWithDelimiter(target, originWordParts, "");
		}
	}

	/**
	 * Does the actual conversion of the target word parts according to the
	 * given delimiter and the origin word parts
	 * 
	 * @param target
	 *            translated word parts to convert
	 * @param origin
	 *            the origin word parts
	 * @param delimiter
	 *            Delimiter which can be hyphen, underscore or TitleCamelCase
	 * @return the converted word
	 */
	private String convertWordWithDelimiter(List<String> target, List<String> origin, String delimiter) {
		// Here comes the preparation of the translated word according to it's
		// origin
		String totalConvertedWord = "";
		for (int i = 0; i < target.size(); i++) {

			char[] originChars = origin.get(i).toCharArray();
			if (Character.isUpperCase(originChars[0])) {
				if (target.get(i).toCharArray().length > 1) {
					totalConvertedWord += target.get(i).substring(0, 1).toUpperCase() + target.get(i).substring(1);
				} else {
					totalConvertedWord += target.get(i).substring(0, 1).toUpperCase();
				}
			} else {
				totalConvertedWord += target.get(i);
			}
			if ((delimiter == HYPHEN_DELIMITER || delimiter == UNDERSCORE_DELIMITER) && i < (target.size() - 1)) {
				totalConvertedWord += delimiter;
			}
		}
		return totalConvertedWord;
	}

	@Override
	public String cleanWordToTranslate(String wordToClean) {
		// Clean german Umlauts: ae -> Ã¤; ue -> Ã¼; oe -> Ã¶ for dict
		// dictionary
		String cleanedWord = wordToClean;

		for (Map.Entry<String, String> entry : umlautsRegexRules.entrySet()) {
			cleanedWord = cleanedWord.replaceAll(entry.getKey(), entry.getValue());
		}
		return cleanedWord;
	}

	@Override
	public String cleanTranslation(String sourceWord) {
		String wordToClean = sourceWord.toLowerCase();
		for (String rule : removeRegexRules) {
			wordToClean = wordToClean.replaceAll(rule, "");
		}
		return wordToClean;
	}

	public String[] getHyphenDelimited(String wordSource) {
		String[] wordParts = wordSource.split(HYPHEN_DELIMITER);
		if (wordParts.length > 1) {
			return wordParts;
		}
		return null;
	}

	public String[] getCamelTitelCaseDelimited(String wordSource) {
		String[] wordParts = wordSource.split(CAMELTITELCASE_DELIMITER);
		if (wordParts.length > 1) {
			return wordParts;
		}
		return null;
	}

	public String[] getUnderscoreDelimited(String wordSource) {
		String[] wordParts = wordSource.split(UNDERSCORE_DELIMITER);
		if (wordParts.length > 1) {
			return wordParts;
		}
		return null;
	}

	/**
	 * Determines the delimiter of a string. Returns Delimiter.NONDELIMITER if
	 * single word
	 * 
	 * @param origin
	 * @return
	 */
	private Delimiter getDelimiter(String origin) {
		if (origin.contains(HYPHEN_DELIMITER)) {
			return Delimiter.HYPHEN;
		} else if (origin.contains(UNDERSCORE_DELIMITER)) {
			return Delimiter.UNDERSCORE;
		} else if (origin.split(CAMELTITELCASE_DELIMITER).length > 1) {
			return Delimiter.CAMELTITLECASE;
		}
		return Delimiter.NODELIMITER;
	}

	/**
	 * Configures the regex rules for cleaning words
	 */
	private void configureRemoveRegexRules() {
		removeRegexRules = new ArrayList<String>();
		removeRegexRules.add("\\[.*\\]");
		removeRegexRules.add("\\(.*\\)");
		removeRegexRules.add("\\<.*\\>");
		removeRegexRules.add("\\{.*\\}");
		removeRegexRules.add("/");
		removeRegexRules.add("sth.");
		removeRegexRules.add("sb.");
		removeRegexRules.add("the ");
	}

	/**
	 * Configures the regex rules for cleaning german words
	 */
	private void configureUmlautsRegexRules() {
		umlautsRegexRules = new HashMap<String, String>();
		umlautsRegexRules.put("ae", "Ä");
		umlautsRegexRules.put("ue", "Ü");
		umlautsRegexRules.put("oe", "Ö");
	}

	/**
	 * Configure the current delimiters CamelTitelCase, Hyphen, Underscore
	 */
	private void configureDelimiters() {
		delimiterList = new ArrayList<String>();
		delimiterList.add(CAMELTITELCASE_DELIMITER);
		delimiterList.add(HYPHEN_DELIMITER);
		delimiterList.add(UNDERSCORE_DELIMITER);
	}
}
