package ghc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

        for (String line : prog) {
            // 1st pass - check if line is code line, assign addr
        }
    }

}
