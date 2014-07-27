package lispparser;

import sun.util.calendar.CalendarUtils;

/**
 * Created by lenovo on 27.07.2014.
 */
public class BinarySearchGenerator {

    private static final String LAMBDA_PROLOGUE = "$create_accessor_256$:\n" +
            "    LDF  $accessor256$\n" +
            "    RTN\n" +
            "\n" +
            "$accessor256$:\n" +
            "\n" +
            "    LD  0   0       ; opcode\n" +
            "    LDC 100         ; read?\n" +
            "    CEQ\n";


    public static void main(String[] args) {
//        System.out.println(generateBinarySearch(16, new FakeGenerator()));
        System.out.println(generateLambdaBinarySearch(5));
    }


    static String generateLambdaBinarySearch(int value) {
        StringBuilder sb = new StringBuilder();
        LambdaGenerator read = new LambdaGenerator("read", 2);
        LambdaGenerator write = new LambdaGenerator("write", 4);

        sb.append(LAMBDA_PROLOGUE);
        sb.append(" TSEL  " + read.getLabel(0, value) + " " + write.getLabel(0, value));
        sb.append("\r\n\r\n");
        sb.append(generateBinarySearch(value, read));
        sb.append("\r\n\r\n");
        sb.append(generateBinarySearch(value, write));
        return sb.toString();
    }

    static interface Generator {
        boolean generateLeaf();

        String getLabel(int from, int to);

        String getJgt(int val, String lessLabel, String greaterLabel);

        String getLeaf(int val);
    }


    static class LambdaGenerator implements Generator {
        private final String labelPrefix;
        private final int accessorShift;

        LambdaGenerator(String labelPrefix, int accessorShift) {
            this.labelPrefix = labelPrefix;
            this.accessorShift = accessorShift;
        }

        @Override
        public String getLabel(int from, int to) {
            if (from == to) {
                return "$$" + labelPrefix + from + "$+" + accessorShift + "$";
            }
            return "$" + labelPrefix + from + "_" + to + "$";
        }

        @Override
        public String getJgt(int val, String lessLabel, String greaterLabel) {
            StringBuilder buf = new StringBuilder();
            buf.append("    LD 0 1 ; ix").append("\r\n");
            buf.append("    LDC " + val).append("\r\n");
            buf.append("    CGT ").append("\r\n");
            buf.append("    TSEL ").append(lessLabel).append(" ").append(greaterLabel).append("\r\n");
            return buf.toString();
        }

        @Override
        public String getLeaf(int val) {
            return "";
        }

        @Override
        public boolean generateLeaf() {
            return false;
        }
    }

    public static String generateBinarySearch(int max, Generator generator) {
        StringBuilder buf = new StringBuilder();
        generateBinarySearch(buf, 0, max, generator);
        return buf.toString();
    }


    private static void generateBinarySearch(StringBuilder buf, int from, int to, Generator generator) {
        if (generator.generateLeaf() || (from != to))
            buf.append(generator.getLabel(from, to)).append(":\r\n");
        if (from == to) {
            generateLeaf(buf, from, generator);
            return;
        } else if (from + 1 == to) {
            buf.append(generator.getJgt(from, generator.getLabel(from, from), generator.getLabel(to, to))).append("\r\n");

            generateLeaf(buf, from, generator);
            generateLeaf(buf, to, generator);
            return;
        }
        int mid = (from + to) / 2;
        buf.append(generator.getJgt(mid, generator.getLabel(from, mid), generator.getLabel(mid + 1, to))).append("\r\n");
        generateBinarySearch(buf, from, mid, generator);
        generateBinarySearch(buf, mid + 1, to, generator);
    }

    private static void generateLeaf(StringBuilder buf, int value, Generator generator) {
        if (generator.generateLeaf()) {
            buf.append(generator.getLabel(value, value)).append("\r\n");
            buf.append(generator.getLeaf(value)).append("\r\n");
        }
    }


    static int pow2Inv(int val) {
        int cnt = 0;
        int p = 1;
        while (p <= val) {
            p *= 2;
            cnt++;
        }
        return cnt;
    }

    private static class FakeGenerator implements Generator {
        @Override
        public boolean generateLeaf() {
            return true;
        }

        @Override
        public String getLabel(int from, int to) {
            return "LBL_" + from + "_" + to + ":";
        }

        @Override
        public String getJgt(int val, String lessLabel, String greaterLabel) {
            return "if value < " + val + " then goto " + lessLabel + " else goto " + greaterLabel;
        }

        @Override
        public String getLeaf(int val) {
            return "return " + val;
        }
    }
}
