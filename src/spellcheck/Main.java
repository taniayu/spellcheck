package spellcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader bf_dict = new BufferedReader(new FileReader(new File("wordsEn.txt")));
        Map<Integer, TreeSet<String>> dict = new HashMap<Integer, TreeSet<String>>();
        String word = bf_dict.readLine();
        while (word != null) {
            word = word.trim();
            dict.putIfAbsent(word.length(), new TreeSet<String>());
            dict.get(word.length()).add(word);
            word = bf_dict.readLine();
        }
        bf_dict.close();
        
        SpellCheck sp = new SpellCheck(dict);
        
        System.out.println("What is the file name?");
        Scanner input = new Scanner(System.in);
        String paragraphFile = input.nextLine();
        System.out.println("Should all misspelled words be given the same weight? (Y/N)");
        String byCount = input.nextLine();
        input.close();
        String text = "";
        
        BufferedReader bf_text = new BufferedReader(new FileReader(new File(paragraphFile)));
        try {
            text = bf_text.readLine();
            if (!text.matches(".*\\w+.*")) {
                System.out.println("No words in file");
                System.exit(0);
            }
            
            double grade;
            if (byCount.equalsIgnoreCase("Y"))
                grade = sp.suggestedGrade(text, true);
            else
                grade = sp.suggestedGrade(text, false);
            Map<String, Integer> misspellings = sp.getMisspellings();
            if (misspellings.isEmpty())
                System.out.println("Perfect!");
            else {
                int totalWrong = 0;
                for (String s : misspellings.keySet()) {
                    totalWrong += misspellings.get(s);
                }
                System.out.println(misspellings.size() + " distinct misspelling(s)");
                System.out.println(totalWrong + " total misspelled");
            }
            System.out.println("Grade: " + grade);
            bf_text.close();
        } catch (IOException e) {
            System.err.println("File I/O error");
        }
    }

}
