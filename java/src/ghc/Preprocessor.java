package ghc;

import sun.plugin.dom.exception.InvalidStateException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: brox
 * Since: 2014-07-26
 */

public class Preprocessor {

    public static void main(String[] args) {

        List<String> prog = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream("src/ghc/example.ghc");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                prog.add(strLine);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(GHCCode.process(prog, false));

    }


    static class GHCCode {
        private final Map<String, Integer> labelPos = new HashMap<>();
        private List<GHCInstruction> instructions = new ArrayList<>();

        public GHCCode(List<String> src) {
            for (String line : src) {
                // 1st pass - check if line is code line, assign addr
                String trimmed = line.trim();
                // skip empty lines
                if (trimmed.length() == 0)
                    continue;
                instructions.add(parseInstruction(trimmed));
            }

            calcLabelPositions();
        }

        private void calcLabelPositions() {
            int pos = 0;
            for (GHCInstruction instr : this.instructions) {
                if (instr instanceof LabelInstruction) {
                    LabelInstruction label = (LabelInstruction) instr;
                    String labelName = label.getLabel();
                    if (labelPos.containsKey(labelName))
                        throw new InvalidStateException("Duplicated label '" + label + "'");
                    labelPos.put(labelName, pos);
                }
                pos += instr.getInstrCount();
            }
        }

        public String generateAsm(boolean annotateWithLineNumbers) {
            StringBuilder sb = new StringBuilder();
            int pos = 0;
            String commentForNextInstr = null;
            for (GHCInstruction instr : this.instructions) {
                String str = instr.toAsmString(this);
                if (commentForNextInstr != null) {
//                    int comment = str.lastIndexOf(';');
//                    int lastInstr = str.lastIndexOf('\n');
//                    if ((comment == -1) || (lastInstr > comment))
//                        str += " ; ";
                    str += " ;" + commentForNextInstr;
                }
                sb.append(str);
                commentForNextInstr = instr.getCommentForNextInst();
                if (sb.charAt(sb.length() - 1) != '\n')
                    sb.append("\r\n");

                if (annotateWithLineNumbers && instr.getInstrCount() > 0)
                    sb.insert(sb.length() - 2, " ; #" + pos);

                pos += instr.getInstrCount();
            }
            return sb.toString();
        }

        public static String process(List<String> src, boolean annotateWithLineNumbers) {
            return new GHCCode(src).generateAsm(annotateWithLineNumbers);
        }

        public int getLabelPosition(String label) {
            Integer pos = labelPos.get(label);
            if (pos == null)
                throw new InvalidStateException("Unknown label '" + label + "'");
            return pos;
        }

        public String replaceLabelsWithAddr(String asm) {
            String prev;
            String next = asm;
            boolean hasComment = (asm.indexOf(';') != -1);
            for (Map.Entry<String, Integer> p : labelPos.entrySet()) {
                prev = next;
                next = prev.replace(p.getKey(), p.getValue().toString());
                if (!prev.equals(next)) {
                    if (!hasComment)
                        next += " ; ";
                    next += " used label = '" + p.getKey() + "'.";
                }
            }
            return next;
        }


        private GHCInstruction parseInstruction(String srcLine) {
            if (srcLine.contains(":")) {
                return new LabelInstruction(srcLine.substring(0, srcLine.indexOf(':')));
            } else if (srcLine.startsWith("call ")) {
                return new CallInstruction(srcLine.substring("call ".length()).trim());
            } else if (srcLine.startsWith("ret")) {
                return new ReturnInstruction();
            } else
                return new SimpleGHCInstruction(srcLine);

        }
    }

    static interface GHCInstruction {
        int getInstrCount();

        String toAsmString(GHCCode code);

        String getCommentForNextInst();
    }

    static class SimpleGHCInstruction implements GHCInstruction {
        private final String instruction;

        SimpleGHCInstruction(String instruction) {
            this.instruction = instruction;
        }

        @Override
        public String toAsmString(GHCCode code) {
            return code.replaceLabelsWithAddr(this.instruction);
        }

        @Override
        public int getInstrCount() {
            return 1;
        }

        @Override
        public String getCommentForNextInst() {
            return null;
        }
    }


    static class LabelInstruction implements GHCInstruction {
        private final String label;

        LabelInstruction(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public int getInstrCount() {
            return 0;
        }

        @Override
        public String toAsmString(GHCCode code) {
            return "";
        }

        @Override
        public String getCommentForNextInst() {
            return "label = '" + label + "'";
        }
    }

    static class CallInstruction implements GHCInstruction {
        private final String label;

        CallInstruction(String label) {
            this.label = label;
        }

        @Override
        public int getInstrCount() {
            return 2;
        }

        @Override
        public String toAsmString(GHCCode code) {
            StringBuilder sb = new StringBuilder();
            sb.append("mov h, pc").append("; call '" + label + "'").append("\r\n");
            sb.append("mov pc, ").append(code.getLabelPosition(label)).append("\r\n");
            return sb.toString();
        }

        @Override
        public String getCommentForNextInst() {
            return null;
        }
    }

    static class ReturnInstruction implements GHCInstruction {

        ReturnInstruction() {
        }

        @Override
        public int getInstrCount() {
            return 2;
        }

        @Override
        public String toAsmString(GHCCode code) {
            StringBuilder sb = new StringBuilder();
            sb.append("add h, 2").append("; return  from function").append("\r\n");
            sb.append("mov pc, h").append("\r\n");
            return sb.toString();
        }

        @Override
        public String getCommentForNextInst() {
            return null;
        }
    }

}
