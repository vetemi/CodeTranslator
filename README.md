# CodeTranslator
Translator which for German code. Will create semicolon-separated output for not translated and translated terms. Is based on dict.cc translations data, which must be downloaded here:
http://www1.dict.cc/translation_file_request.php?l=e 

Has kind of a "memory". I.e. it stores already found translations in a map for a more efficient access. This file will be exported afterwards as txt file and imported before a translation run. Make sure that you delete the file if you optimize the translator. Otherwise, it will take the stored translations which might be not correct.

# Licence
Under GNU GPL (http://www.gnu.org/licenses/gpl-3.0.en.html)
All used third part libraries refers to their own licences

# TODOs
- Make more efficient by storing prepared (stemmed, lemmatized, etc.) translations in the translation map
- Use Lemmatizer and Stemmer for words to translate
- Use spell-checking logic for finding "not correct spelled" words.
- Test cases

Valmir Etemi