package ru.bmstu.compilers.rk

import ru.bmstu.compilers.Grammar

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by ali on 02.03.17.
 */
class RK1_Nikita {
    public static void main(String[] args) {
        def grammar = Grammar.loadFromFile("htmlgrammar")

        // fix grammar
        grammar.rules["TEXT"][1].add(1, " "); // во второе правило добавляем пробел между стрингой и текстом
        grammar.rules["TEXT"][3].add(1, " "); // во второе правило добавляем пробел между стрингой и текстом
        grammar.rules["LINK"][0].add(1, " "); // во второе правило добавляем пробел между стрингой и текстом
        grammar.rules["STRING"] = []
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        alphabet += alphabet.toUpperCase() + "?!.";
        for (char c in alphabet.toCharArray()) {
            grammar.terminals << c.toString();
            grammar.rules["STRING"] << [c.toString()]
            grammar.rules["STRING"] << [c.toString(), "STRING"]
        }

        // program
        List<String> fileContent = Files.readAllLines(Paths.get("rk1/NikInput"));
        File output = new File("rk1/NikOutput");
        PrintWriter writer = new PrintWriter(output);
        writer.print("");
        writer.close();
        for (int i = 1; i < fileContent.size(); i++)
            try {
                grammar.topDownParsing(fileContent[i], false) != null
                output << "Syntax Included\n"
            } catch (Exception e) {
                output << "No Syntax Included\n"
            }
    }
}
