package spellcheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class SpellCheck {
    
    private Map<Integer, TreeSet<String>> wordsByCount;
    private Map<String, Integer> misspellingCount;
    private Map<String, Double> misspellingDegree;
    
    public SpellCheck(Map wordMap) {
        wordsByCount = wordMap;
        misspellingCount = new HashMap<String, Integer>();
        misspellingDegree = new HashMap<String, Double>();
    }
    
    /**
     * @return map of misspellings and the number of times each appears
     */
    public Map<String, Integer> getMisspellings() {
        return misspellingCount;
    }
    
    /**
     * Parses the text by finding how correct the spelling of each word is
     * Finds a suggested percentage grade
     * @param text paragraph to be graded
     * @param byCount if true, every wrong spelling holds the same weight
     *                if false, the weight of each wrong spelling depends on how wrong it is
     * @return suggested grade for the paragraph
     */
    public double suggestedGrade(String text, boolean byCount) {
        String[] words = text.split("[\\s\\d\\p{P}]+");
        int wrongCount = 0;
        int totalCount = 0;
        for (String w : words) {
            if (!w.equals("")) {
                checkWord(w);
            }
        }
        double grade = words.length;
        for (String w : misspellingCount.keySet()) {
            double deducted = 2*misspellingCount.get(w);  //2 points are deducted for each wrong spelling
            if (deducted > 3)
                deducted = 3;
            if (!byCount)
                deducted *= 3*(1.0-misspellingDegree.get(w)); //3 times degree of wrongness is deducted for each
            grade -= deducted;
        }
        return grade * 100.0 / words.length;
    }
    
    /**
     * Parses the text by finding how correct the spelling of each word is
     * Finds a suggested percentage grade, default by count and not degree
     * @param text paragraph to be graded
     * @return suggested grade for the paragraph
     */
    public double suggestedGrade(String text) {
        return suggestedGrade(text, true);
    }
    
    /**
     * Saves into misspellingDegree each misspelled word and its degree of "correctness"
     * Saves into misspellingCount each misspelled word and increments the number of times
     * it appear in the paragraph
     * @param word word in paragraph to be checked against the dictionary
     */
    private void checkWord(String word) {
        if (word.toUpperCase().equals(word))
            return;
        int wordLength = word.length();
        if (misspellingDegree.containsKey(word)) {
            int count = misspellingCount.get(word);
            misspellingCount.put(word, count+1);
        }
        double max_similarity = 0;
        if (wordsByCount.containsKey(wordLength)) {
            for (String s : wordsByCount.get(wordLength)) {
                if (s.equalsIgnoreCase(word))
                    return;
                double temp = calculateSimilarity(word.toLowerCase(), s);
                if (temp > max_similarity)
                    max_similarity = temp;
            }
        }
        misspellingDegree.put(word, max_similarity);
        misspellingCount.put(word, 1);
    }
    
    /**
     * @param s1 a string
     * @param s2 a string
     * @return the degree of similarity between s1 and s2
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.length() == s2.length())
            return similaritySameLength(s1, s2);
        else if (s1.length() > s2.length())
            return similarityDiffLength(s1, s2, 0, s1.length());
        else
            return similarityDiffLength(s2, s1, 0, s2.length());
    }
    
    /**
     * @param s1 a string of same length as s2
     * @param s2 a string of same length as s1
     * @return the degree of similarity between s1 and s2 (specifically, the percentage
     * of letters in each that are in the same position)
     */
    private double similaritySameLength(String s1, String s2) {
        double sim = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) == s2.charAt(i))
                sim++;
        }
        return sim /= s1.length();
    }
    
    /**
     * A recursive method that removes letters from the longer string in order to achieve
     * something as close as possible to the shorter string 
     * @param longer a string of length longer than shorter
     * @param shorter a string of length shorter than longer
     * @param indexRemoved the index of the letter last removed in the previous step
     * @param longest the length of longer in the initial call to the method
     * @return the maximum degree of similarity between s1 and s2 (specifically, if one can be reached
     * by removing letters from the other, and related to how many letters need to be removed)
     */
    private double similarityDiffLength(String longer, String shorter, int indexRemoved, int longest) {
        if (longer.length() == shorter.length())
            return similaritySameLength(longer, shorter);
        double max_sim = 0.0;
        for (int i = indexRemoved; i < longer.length(); i++) {
            double temp_sim = similarityDiffLength(longer.substring(0, i)+longer.substring(i), shorter, i, longest);
            if (temp_sim > max_sim)
                max_sim = temp_sim;
        }
        return max_sim - (longest-longer.length())/(double)longest;
    }
}
