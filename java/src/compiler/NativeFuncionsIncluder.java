package compiler;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by san on 7/26/14.
 */
public class NativeFuncionsIncluder {

    public static void generate(ArrayList<Compiler.MyMethod> methods, ArrayList<Compiler.Opcode> global) throws IOException {
        File source = new File("../native");
        String[] files = source.list();
        for (String file : files) {
            if (file.endsWith(".gcc")) {
                System.out.println("Assembling function: " + file);
                File src = new File(source, file);
                String body = FileUtils.readFileToString(src);

//                List<String> strings = Arrays.asList(body.split("\n"));
//                strings = assembler(strings);
                String functionName = file.substring(0, file.length()-4);
                Compiler.MyMethod foundMethod = null;
                for (Compiler.MyMethod method : methods) {
                    if (method.name.equals(functionName)) {
                        foundMethod = method;
                        break;
                    }
                }
                if (foundMethod == null) {
                    throw new RuntimeException("Native method for unknown compiled method: "+functionName);
                }
                foundMethod.source = body;
/*
                for (String string : strings) {
                    Compiler.Opcode opcode = new Compiler.Opcode(string);
                    foundMethod.opcodes.add(opcode);
                }
*/
            }
        }
    }

    public static void sampleAssemble() {
        try {
            File file = new File("sampleScript.gcc");
            byte[] arr = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            int rd = fis.read(arr);
            String body = new String(arr, 0, rd);
            List<String> strings = Arrays.asList(body.split("\n"));
            assembler(strings, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> assembler(List<String> lines, int offset) {
        HashMap<String,Integer> labels = new HashMap<>();
        int pc = offset;
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            String line = l.trim();
            if (line.endsWith(":")) {
                String lbl = line.replace(":", "");
                Integer old = labels.put(lbl, pc);
                if (old != null)
                    throw new RuntimeException("Duplicate label, line (" + i + "): " + lbl);
            } else if (line.startsWith(";")) {
                // skip comment
            } else if (line.length() > 0) {
                pc++;
            }
        }
        pc = offset;
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            String line = l.trim();
            if (line.endsWith(":")) {
                lines.set(i, "");
                // System.out.println("; pc="+pc+"  DEF "+line.replace("$","_"));
            } else if (line.startsWith(";")) {
                lines.set(i, "");
                // System.out.println(line);
            } else if (line.length() > 0) {
                for (Map.Entry<String, Integer> stringIntegerEntry : labels.entrySet()) {
                    // skip comment
                    if (line.contains(stringIntegerEntry.getKey())) {
                        line = line.replace(stringIntegerEntry.getKey(), "" + stringIntegerEntry.getValue()); // + " ; ref "+stringIntegerEntry.getKey().replace("$","_");
                    }
                }
                line = line.replace("$pc$", ""+pc);

                while (line.contains("+") && line.contains("$")) {
                    int ix1 = line.indexOf("$");
                    int ix2 = line.indexOf("$", ix1+1);
                    if (ix2 != -1) {
                        String sub = line.substring(ix1+1, ix2);
                        int ix3 = sub.indexOf("+");
                        if (ix3 == -1)
                            throw new IllegalArgumentException("Bad expression in line ("+i+"): "+line);
                        try {
                            int ls = Integer.parseInt(sub.substring(0, ix3).trim());
                            int rs = Integer.parseInt(sub.substring(ix3+1).trim());
                            int total = ls+rs;
                            line = line.substring(0, ix1) + total + line.substring(ix2 + 1);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Bad expression in line ("+i+"): "+line);
                        }
                    }
                }
                int ixx = line.indexOf(";");
                if (ixx != -1) line = line.substring(0, ixx);
                line = "\t"+line;
                lines.set(i, line);
                // System.out.println(lines.get(i));
                pc++;
            } else {
                lines.set(i, "");
                //System.out.println(line);
            }
        }
        return lines;
    }

    public static void main(String[] args) {
        sampleAssemble();
    }
}
