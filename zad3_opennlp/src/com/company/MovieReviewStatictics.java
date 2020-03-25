package com.company;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MovieReviewStatictics {

    public static String TOKENIZER_MODEL = "models/en-token.bin";
    public static String SENTENCE_MODEL = "models/en-sent.bin";
    public static String POS_MODEL = "models/en-pos-maxent.bin";
    public static String LEMMATIZER_DICT = "models/en-lemmatizer.dict";
    public static String NAME_MODEL = "models/en-ner-person.bin";
    public static String PLACES_MODEL = "models/en-ner-location.bin";
    public static String ORGANIZATION_MODEL = "models/en-ner-organization.bin";


    private static final String DOCUMENTS_PATH = "movies/";
    private int _verbCount = 0;
    private int _nounCount = 0;
    private int _adjectiveCount = 0;
    private int _adverbCount = 0;
    private int _totalTokensCount = 0;

    private PrintStream _statisticsWriter;

    private SentenceModel _sentenceModel;
    private TokenizerModel _tokenizerModel;
    private DictionaryLemmatizer _lemmatizer;
    private PorterStemmer _stemmer;
    private POSModel _posModel;
    private TokenNameFinderModel _peopleModel;
    private TokenNameFinderModel _placesModel;
    private TokenNameFinderModel _organizationsModel;

    public static void main(String[] args) {
        MovieReviewStatictics statictics = new MovieReviewStatictics();
        statictics.run();
    }

    private void run() {
        try {
            initModelsStemmerLemmatizer();

            File dir = new File(DOCUMENTS_PATH);
            File[] reviews = dir.listFiles((d, name) -> name.endsWith(".txt"));

            _statisticsWriter = new PrintStream("statistics.txt", "UTF-8");

            Arrays.sort(reviews, Comparator.comparing(File::getName));
            for (File file : reviews)
            {
                System.out.println("Movie: " + file.getName().replace(".txt", ""));
                _statisticsWriter.println("Movie: " + file.getName().replace(".txt", ""));

                String text = new String(Files.readAllBytes(file.toPath()));
                processFile(text);

                _statisticsWriter.println();
            }

            overallStatistics();
            _statisticsWriter.close();

        } catch (IOException ex) {
            Logger.getLogger(MovieReviewStatictics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initModelsStemmerLemmatizer() {
        try {
//         TODO: load all OpenNLP models (+Porter stemmer + lemmatizer)
//         from files (use class variables)
            this._sentenceModel = new SentenceModel(new File(SENTENCE_MODEL));
            this._tokenizerModel = new TokenizerModel(new File(TOKENIZER_MODEL));
            this._posModel = new POSModel(new File(POS_MODEL));
            this._peopleModel = new TokenNameFinderModel(new File(NAME_MODEL));
            this._placesModel = new TokenNameFinderModel(new File(PLACES_MODEL));
            this._organizationsModel = new TokenNameFinderModel(new File(ORGANIZATION_MODEL));

            this._lemmatizer = new DictionaryLemmatizer(new File(LEMMATIZER_DICT));
            this._stemmer = new PorterStemmer();

        } catch (IOException ex) {
            Logger.getLogger(MovieReviewStatictics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processFile(String text) {
        // TODO: process the text to find the following statistics:
        // For each movie derive:

        int noSentences = 0;                   //  number of sentences
        int noTokens = 0;                      //  number of tokens
        int noStemmed = 0;                     //  number of (unique) stemmed forms
        int noWords = 0;                       //  number of (unique) words from a dictionary (lemmatization)
        Span people[] = new Span[] { };        //  people
        Span locations[] = new Span[] { };     //  locations
        Span organizations[] = new Span[] { }; //  organisations

        // TODO + compute the following overall (for all movies) POS tagging statistics:
        //    - percentage number of adverbs (class variable, private int _verbCount = 0)
        //    - percentage number of adjectives (class variable, private int _nounCount = 0)
        //    - percentage number of verbs (class variable, private int _adjectiveCount = 0)
        //    - percentage number of nouns (class variable, private int _adverbCount = 0)
        //    + update _totalTokensCount

        // ------------------------------------------------------------------
        SentenceDetectorME sentence_detector = new SentenceDetectorME(this._sentenceModel);
        TokenizerME tokenizer = new TokenizerME(this._tokenizerModel);
        POSTaggerME postagger = new POSTaggerME(this._posModel);
        NameFinderME people_finder = new NameFinderME(this._peopleModel);
        NameFinderME places_finder = new NameFinderME(this._placesModel);
        NameFinderME organizations_finder = new NameFinderME(this._organizationsModel);

        // TODO derive sentences (update noSentences variable)
        noSentences = sentence_detector.sentDetect(text).length;

        // TODO derive tokens and POS tags from text
        // (update noTokens and _totalTokensCount)
        String[] tokens = tokenizer.tokenize(text);
        String[] pos_tags = postagger.tag(tokens);

        noTokens = tokens.length;
        this._totalTokensCount += tokens.length;

        // TODO perform stemming (use derived tokens)
        // (update noStemmed)
        //for (String token : tokens)
        //{
        // use .toLowerCase().replaceAll("[^a-z0-9]", ""); thereafter, ignore "" tokens
        //}

        Set<String> stems = new HashSet<>();
        for (String token: tokens){
            String stem = this._stemmer.stem(
                    token.toLowerCase().replaceAll("[^a-z0-9]", "")
            );
            stems.add(stem);
        }
        noStemmed = stems.size();


        // TODO perform lemmatization (use derived tokens)
        // (remove "O" from results - non-dictionary forms, update noWords)
        String[] lemmas = this._lemmatizer.lemmatize(tokens, pos_tags);

        ArrayList<String> lemmas_clean = new ArrayList<>(Arrays.asList(lemmas));
        lemmas_clean.removeIf(elem -> elem.equals("O"));

        noWords = lemmas_clean.size();

        // TODO derive people, locations, organisations (use tokens),
        // (update people, locations, organisations lists).
        people = people_finder.find(tokens);
        locations = places_finder.find(tokens);
        organizations = organizations_finder.find(tokens);


        // TODO update overall statistics - use tags and check first letters
        // (see https://www.clips.uantwerpen.be/pages/mbsp-tags; first letter = "V" = verb?)

        for (String tag: pos_tags){
            if (tag.startsWith("V")) _verbCount++;
            if (tag.startsWith("R")) _adverbCount++;
            if (tag.startsWith("N")) _nounCount++;
            if (tag.startsWith("J")) _adjectiveCount++;
        }

        // ------------------------------------------------------------------

        saveResults("Sentences", noSentences);
        saveResults("Tokens", noTokens);
        saveResults("Stemmed forms (unique)", noStemmed);
        saveResults("Words from a dictionary (unique)", noWords);

        saveNamedEntities("People", people, tokens);
        saveNamedEntities("Locations", locations, tokens);
        saveNamedEntities("Organizations", organizations, tokens);

//        saveNamedEntities("People", people, new String[] { });
//        saveNamedEntities("Locations", locations, new String[] { });
//        saveNamedEntities("Organizations", organisations, new String[] { });
    }



    private void saveResults(String feature, int count) {
        String s = feature + ": " + count;
        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void saveNamedEntities(String entityType, Span spans[], String tokens[]) {
        StringBuilder s = new StringBuilder(entityType + ": ");
        for (int sp = 0; sp < spans.length; sp++)
        {
            for (int i = spans[sp].getStart(); i < spans[sp].getEnd(); i++)
            {
                s.append(tokens[i]);
                if (i < spans[sp].getEnd() - 1) s.append(" ");
            }
            if (sp < spans.length - 1) s.append(", ");
        }

        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void overallStatistics() {
        _statisticsWriter.println("---------OVERALL STATISTICS----------");
        DecimalFormat f = new DecimalFormat("#0.00");

        if (_totalTokensCount == 0) _totalTokensCount = 1;
        String verbs = f.format(((double) _verbCount * 100) / _totalTokensCount);
        String nouns = f.format(((double) _nounCount * 100) / _totalTokensCount);
        String adjectives = f.format(((double) _adjectiveCount * 100) / _totalTokensCount);
        String adverbs = f.format(((double) _adverbCount * 100) / _totalTokensCount);

        _statisticsWriter.println("Verbs: " + verbs + "%");
        _statisticsWriter.println("Nouns: " + nouns + "%");
        _statisticsWriter.println("Adjectives: " + adjectives + "%");
        _statisticsWriter.println("Adverbs: " + adverbs + "%");

        System.out.println("---------OVERALL STATISTICS----------");
        System.out.println("Adverbs: " + adverbs + "%");
        System.out.println("Adjectives: " + adjectives + "%");
        System.out.println("Verbs: " + verbs + "%");
        System.out.println("Nouns: " + nouns + "%");
    }

}
