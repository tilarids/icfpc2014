package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkopiychenko on 7/25/14.
 */
public class LambdaMap {

    List<List<CT>> map;

    public LambdaMap(String s){
        String[] rows = s.split("\n");
        map = new ArrayList<List<CT>>();
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            List<CT> lst = new ArrayList<>();
            for (int j = 0; j < row.length(); j++) {
                lst.add(CT.convertMapCharacter(row.charAt(i)));
            }
            map.add(lst);
        }
    }

    public LambdaMap(Cons map){
        //TO DO
    }

    public Cons toCons(){
        Cons result = null;
        for (int i1 = map.size() - 1; i1 >= 0; i1--) {
            List<CT> row = map.get(i1);
            Cons lst = null;
            for (int i = row.size() - 1; i >= 0; i--) {
                lst = VM.cons(row.get(i), lst);
            }
            result = VM.cons(lst, result);
        }
        return result;
    }

    public int height() {
        return map.size();
    }

    //No empty maps...
    public int width() {
        return map.get(0).size();
    }
}
