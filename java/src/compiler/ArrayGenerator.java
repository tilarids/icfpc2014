package compiler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by san on 7/26/14.
 */
public class ArrayGenerator {

    public static void main(String[] args) {

    }

    public static void generate(ArrayList<Compiler.MyMethod> methods) {
    }

    public ArrayList<String> assembler(ArrayList<String> lines) {
        HashMap<String,Integer> labels = new HashMap<>();
        int pc = 0;
        for (String l : lines) {
            String line = l.trim();
            if (line.endsWith(":")) {
                labels.put(line.replace(":", ""), pc);
            } else if (line.startsWith(";")) {
                // skip comment
            } else if (line.length() > 0) {
                pc++;
            }
        }
        return null;
    }
}
