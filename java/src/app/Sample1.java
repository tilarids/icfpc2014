package app;

/**
 * Created by san on 7/25/14.
 */
@SuppressWarnings("Convert2MethodRef")
public class Sample1 extends VM {

    static final String map1 =
            "#######################\n" +
            "#..........#..........#\n" +
            "#.###.####.#.####.###.#\n" +
            "#o###.####.#.####.###o#\n" +
            "#.....................#\n" +
            "#.###.#.#######.#.###.#\n" +
            "#.....#....#....#.....#\n" +
            "#####.#### # ####.#####\n" +
            "#   #.#    =    #.#   #\n" +
            "#####.# ### ### #.#####\n" +
            "#    .  # === #  .    #\n" +
            "#####.# ####### #.#####\n" +
            "#   #.#    %    #.#   #\n" +
            "#####.# ####### #.#####\n" +
            "#..........#..........#\n" +
            "#.###.####.#.####.###.#\n" +
            "#o..#......\\......#..o#\n" +
            "###.#.#.#######.#.#.###\n" +
            "#.....#....#....#.....#\n" +
            "#.########.#.########.#\n" +
            "#.....................#\n" +
            "#######################\n";

    public Sample1() {
    }

    @Compiled
    static class SearchItem {

    }

    class OurState {

    }

    @Compiled
    public Tuple<AIState,Function2<AIState, Cons, Tuple<AIState, Integer>>> entryPoint(ListCons<ListCons<Integer>> map, Object undocumented) {
        return new Tuple<>(createInitialState(map), (nextaistate, worldState) -> performMove(nextaistate));
//        int x = location.a;
//        int y = location.b;


//        CT left = getMapItem(map, y, x-1);
//        CT right = getMapItem(map, y, x+1);
//        CT top = getMapItem(map, y-1, x);
//        CT bottom = getMapItem(map, y+1, x);



    }

    @Compiled
    private Tuple<AIState, Integer> performMove(AIState nextaistate) {
        return new Tuple<>(nextaistate, 0);
    }

    @Compiled
    class AIState {

        ParsedStaticMap parsedStaticMap;

        AIState(ParsedStaticMap parsedStaticMap) {
            this.parsedStaticMap = parsedStaticMap;
        }
    }

    @Compiled
    public static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point("+x+","+y+")";
        }
    }

    @Compiled
    class LambdaManState {
        int vitality;
        Point location;
        int direction;
        int lives;
        int score;
    }

    @Compiled
    class WorldState {
        ListCons<ListCons<Integer>> map;
        LambdaManState lambdaManState;
        ListCons<GhostState> ghosts;
        int fruitStatus;
    }

    class GhostState {
        int vitality;
        Point location;
        int direction;
    }

    @Compiled
    class ParsedEdge {
        Point a;
        Point b;
        ListCons<Point> edge;
        int count;

        ParsedEdge(ListCons<Point> edge, int count, Point a, Point b) {
            this.edge = edge;
            this.count = count;
            this.a = a;
            this.b = b;
        }
    }

    @Compiled
    class HorizontalRow {
        int y;
        int x1;
        int x2;
        ParsedEdge dest;

        HorizontalRow(int y, int x1, int x2, ParsedEdge dest) {
            this.y = y;
            this.x1 = x1;
            this.x2 = x2;
            this.dest = dest;
        }
    }

    @Compiled
    class VerticalRow {
        int x;
        int y1;
        int y2;
        ParsedEdge dest;

        VerticalRow(int x, int y1, int y2, ParsedEdge dest) {
            this.x = x;
            this.y1 = y1;
            this.y2 = y2;
            this.dest = dest;
        }
    }

    @Compiled
    class ParsedStaticMap {

        // list of all walkable cells
        ListCons<Point> walkable;

        // list of all junctions (3+ exits)
        ListCons<Point> junctions;

        // edges
        ListCons<ParsedEdge> parsedEdges;
        ListCons<VerticalRow> verticalFinders;
        ListCons<HorizontalRow> horizontalFinders;

        ParsedStaticMap(ListCons<Point> walkable, ListCons<Point> junctions, ListCons<ParsedEdge> parsedEdges, ListCons<VerticalRow> verticalFinders, ListCons<HorizontalRow> horizontalFinders) {
            this.junctions = junctions;
            this.walkable = walkable;
            this.parsedEdges = parsedEdges;
            this.verticalFinders = verticalFinders;
            this.horizontalFinders = horizontalFinders;
        }
    }


    static<A,B> Maybe<Tuple<A,B>> cat_maybe_to_pair(Maybe<A> a, Maybe<B> b) {
        if (a.set != 0 && b.set != 0) {
            return new Maybe<Tuple<A, B>>(new Tuple<A,B>(a.data, b.data), 1);
        } else {
            return new Maybe<Tuple<A, B>>(null, 0);
        }
    }

    @Compiled
    private AIState createInitialState(ListCons<ListCons<Integer>> map) {
        return new AIState(parseStaticMap(map));
    }

    public int isWall(int test) {
        return test == 0 ? 1 : 0;
    }

    public int isWalkable(int test) {
        return test == 0 ? 0 : 1;
    }

    public int isWalkable2(ListCons<ListCons<Integer>> map, Point p) {
        return isWalkable(getMapItem(map, p.y, p.x));
    }

    public int isJunction(ListCons<ListCons<Integer>> map, int x, int y) {
        int a1 = isWalkable(getMapItem(map, y-1, x));
        int a2 = isWalkable(getMapItem(map, y+1, x));
        int a3 = isWalkable(getMapItem(map, y, x-1));
        int a4 = isWalkable(getMapItem(map, y, x+1));
        return a1+a2+a3+a4 > 2 ? 1 : 0;
    }

    public ListCons<ParsedEdge> findExits(ListCons<ListCons<Integer>> map, Point p, ListCons<Point> allJunctions) {
        ListCons<ListCons<Point>> allNeighbourJunctionsPaths = waveFromPointToNearestJunction(map, queue_enqueue(queue_new(), cons(p, null)), allJunctions, cons(p, null), null);
        debug(allNeighbourJunctionsPaths);
        return null;
    }

    private ListCons<ListCons<Point>> waveFromPointToNearestJunction(ListCons<ListCons<Integer>> map, Queue<ListCons<Point>> pointQueue, ListCons<Point> destinations, ListCons<Point> visited, ListCons<ListCons<Point>> acc) {
        ListCons<ListCons<Point>> retval;
        if (queue_isempty(pointQueue)) {
            retval = acc;
        } else {
            Tuple<ListCons<Point>, Queue<ListCons<Point>>> emptier = queue_dequeue(pointQueue);
            ListCons<Point> thisRoute = emptier.a;
            Point weAreHere = head(thisRoute);
            debug(weAreHere);
            ListCons<Point> possibleDestinations =
                    cons(new Point(weAreHere.x + 1, weAreHere.y),
                            cons(new Point(weAreHere.x - 1, weAreHere.y),
                                    cons(new Point(weAreHere.x, weAreHere.y + 1),
                                            cons(new Point(weAreHere.x, weAreHere.y - 1), null))
                            )
                    );
            ListCons<Point> exits = filter(possibleDestinations, (d) -> isWalkable2(map, d) * noneof(visited, (Point v) -> v.x == d.x && v.y == d.y ? 1 : 0));// here * === &&
            ListCons<Point> arriveds = filter(exits, (e) -> any(destinations, (d) -> d.x == e.x && d.y == e.y ? 1 : 0));
            ListCons<Point> continueds = filter(exits, (e) -> noneof(arriveds, (d) -> d.x == e.x && d.y == e.y ? 1 : 0));
            ListCons<ListCons<Point>> exitRoutes = map(continueds, (e) -> cons(e, thisRoute));
            Queue<ListCons<Point>> filledQueue = fold0(exitRoutes, emptier.b, (r, i) -> queue_enqueue(r, i));
            retval = waveFromPointToNearestJunction(map, filledQueue, destinations, concat2_set(visited, exits), concat2_set(acc, map(arriveds, (e) -> cons(e, thisRoute))));
        }
        return retval;
    }

    @Compiled
    private ParsedStaticMap getAllWalkablesInMap(ListCons<ListCons<Integer>> m) {
        ListCons<Point> walkable = concat(mapi(m, 0, (row, rowy) -> cat_maybes(mapi(row, 0, (col, colx) -> isWalkable(col) == 1 ? JUST(new Point(colx, rowy)) : NOTHING()))));
        ListCons<Point> junctions = filter(walkable, (w) -> isJunction(m, w.x, w.y));
        concat(map(junctions, (j) -> findExits(m, j, junctions)));
        debug(junctions);
        debug(length(junctions));
        return new ParsedStaticMap(walkable, junctions, null, null, null);
    }

    @Compiled
    private ParsedStaticMap parseStaticMap(ListCons<ListCons<Integer>> map) {
        ParsedStaticMap allWalkablesInMap = getAllWalkablesInMap(map);
        return allWalkablesInMap;
    }


    @Compiled
    private int test2() {
        Queue queue = queue_enqueue(queue_enqueue(queue_enqueue(queue_new(), 1), 2), 3);
        Tuple<Object, Queue> q1 = queue_dequeue(queue);
        debug(q1.a);
        q1 = queue_dequeue(q1.b);
        debug(q1.a);
        q1 = queue_dequeue(q1.b);
        debug(q1.a);
        return 1;
    }


    public static ListCons<ListCons<Integer>> convertMap(String map) {
        String[] rows = map.split("\n");
        ListCons<ListCons<Integer>> result = null;
        for (int i1 = rows.length - 1; i1 >= 0; i1--) {
            String row = rows[i1];
            ListCons<Integer> lst = null;
            for (int i = row.length() - 1; i >= 0; i--) {
                lst = cons(CT.convertMapCharacter(row.charAt(i)).getValue(), lst);
            }
            result = cons(lst, result);
        }
        return result;
    }

    @Compiled
    public Integer getMapItem(ListCons<ListCons<Integer>> map, int y, int x) {
        return y < 0 || x < 0 ? 0 :
            (Integer)list_item_def((Cons) list_item_def(map, y, 0), x, 0);
    }


    public static void main(String[] args) {
        String theMap = map1;

        int x = -1;
        int y = -1;
        String[] rows = theMap.split("\n");
        for (int yy = rows.length - 1; yy >= 0; yy--) {
            String row = rows[yy];
            for (int ii = row.length() - 1; ii >= 0; ii--) {
                if (row.charAt(ii) == '\\') {
                    x = ii;
                    y = yy;
                }
            }
        }


        ListCons<ListCons<Integer>> lmap = convertMap(theMap);
        Tuple<AIState, Function2<AIState, Cons, Tuple<AIState, Integer>>> initResult = new Sample1().entryPoint(lmap, null);
        while(true) {
            AIState state = initResult.a;
            //initResult.b.apply(state, iterateWorld());
        }
    }

}
