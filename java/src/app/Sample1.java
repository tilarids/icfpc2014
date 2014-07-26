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
    public Tuple<AIState,Function2<AIState, WorldState, Tuple<AIState, Integer>>> entryPoint(ListCons<ListCons<Integer>> map, Object undocumented) {
        return new Tuple<>(createInitialState(map), (nextaistate, worldState) -> performMove(nextaistate, worldState));
//        int x = location.a;
//        int y = location.b;


//        CT left = getMapItem(map, y, x-1);
//        CT right = getMapItem(map, y, x+1);
//        CT top = getMapItem(map, y-1, x);
//        CT bottom = getMapItem(map, y+1, x);



    }



    @Compiled
    private ListCons<Point> collectEdgePills(ParsedEdge edge, Point start, ListCons<ListCons<Integer>> map) {
        ListCons<Point> pathOnEdge = dropWhile(edge.edge, (Point p) -> p.x != start.x || p.y != start.y ? 1 : 0);
        return filter(pathOnEdge, (Point p) -> getMapItem(map, p.y, p.x) == 2 ? 1:0);
    }

    @Compiled
    class EdgeAndCount {
        ParsedEdge pe;
        int count;

        EdgeAndCount(ParsedEdge pe, int count) {
            this.pe = pe;
            this.count = count;
        }
    }

    @Compiled
    private Tuple<AIState, Integer> performMove(AIState aistate, WorldState worldState) {
        Point location = worldState.lambdaManState.location;
        ListCons<ParsedEdge> edgesForPoint = findEdgesForPoint(aistate, location);
        ListCons<EdgeAndCount> collectedPoints = map(edgesForPoint, (e) -> new EdgeAndCount(e, length(collectEdgePills(e, location, worldState.map))));
        EdgeAndCount ec = maximum_by(collectedPoints, (EdgeAndCount cp) -> cp.count);
        ListCons<Point> pathToWalk = dropWhile(ec.pe.edge, (Point p) -> p.x != location.x || p.y != location.y ? 1 : 0);
        Point newLocation = head(tail(pathToWalk));
        int direction =
                (newLocation.x > location.x) ? 1 :
                        (newLocation.x < location.x) ? 3 :
                                (newLocation.y < location.y) ? 0 :
                                        2;
        return new Tuple<>(aistate, direction);
    }

    @Compiled
    class AIState {

        ParsedStaticMap parsedStaticMap;
        int dummy;

        AIState(ParsedStaticMap parsedStaticMap, int dummy) {
            this.parsedStaticMap = parsedStaticMap;
            this.dummy = dummy;
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
    public static class LambdaManState {
        int vitality;
        Point location;
        int direction;
        int lives;
        int score;

        public LambdaManState(int vitality, Point location, int direction, int lives, int score) {
            this.vitality = vitality;
            this.location = location;
            this.direction = direction;
            this.lives = lives;
            this.score = score;
        }
    }

    @Compiled
    static class WorldState {
        ListCons<ListCons<Integer>> map;
        LambdaManState lambdaManState;
        ListCons<GhostState> ghosts;
        int fruitStatus;

        WorldState(ListCons<ListCons<Integer>> map, LambdaManState lambdaManState, ListCons<GhostState> ghosts, int fruitStatus) {
            this.map = map;
            this.lambdaManState = lambdaManState;
            this.ghosts = ghosts;
            this.fruitStatus = fruitStatus;
        }
    }

    public static class GhostState {
        int vitality;
        Point location;
        int direction;

        public GhostState(int vitality, Point location, int direction) {
            this.vitality = vitality;
            this.location = location;
            this.direction = direction;
        }
    }

    @Compiled
    class ParsedEdge {
        Point a;
        Point b;
        ListCons<Point> edge;
        int count;
        int edgeNumber;

        ParsedEdge(ListCons<Point> edge, int count, Point a, Point b, int edgeNumber) {
            this.edge = edge;
            this.count = count;
            this.a = a;
            this.b = b;
            this.edgeNumber = edgeNumber;
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


    @Compiled
    static<A,B> Maybe<Tuple<A,B>> cat_maybe_to_pair(Maybe<A> a, Maybe<B> b) {
        return
                a.set != 0 && b.set != 0 ?
                        new Maybe<Tuple<A, B>>(new Tuple<A,B>(a.data, b.data), 1)
                        : new Maybe<Tuple<A, B>>(null, 0);
    }

    @Compiled
    private AIState createInitialState(ListCons<ListCons<Integer>> map) {
        return new AIState(parseStaticMap(map), 0);
    }

    @Compiled
    public int isWall(int test) {
        return test == 0 ? 1 : 0;
    }

    @Compiled
    public static int isWalkable(int test) {
        return test == 0 ? 0 : 1;
    }

    @Compiled
    public static int isWalkable2(ListCons<ListCons<Integer>> map, Point p) {
        return isWalkable(getMapItem(map, p.y, p.x));
    }

    @Compiled
    public int isJunction(ListCons<ListCons<Integer>> map, int x, int y) {
        int a1 = isWalkable(getMapItem(map, y-1, x));
        int a2 = isWalkable(getMapItem(map, y+1, x));
        int a3 = isWalkable(getMapItem(map, y, x-1));
        int a4 = isWalkable(getMapItem(map, y, x+1));
        return a1+a2+a3+a4 > 2 ? 1 : 0;
    }

    @Compiled
    public ListCons<ParsedEdge> findNeighbourJunctions(ListCons<ListCons<Integer>> map, Point somePoint, ListCons<Point> allJunctions) {
        ListCons<ListCons<Point>> allNeighbourJunctionsPaths = waveFromPointToNearestJunction(map, queue_enqueue(queue_new(), cons(somePoint, null)), allJunctions, cons(somePoint, null), null);
        debug(allNeighbourJunctionsPaths);
        return map(allNeighbourJunctionsPaths, (p) -> new ParsedEdge(p, length(p)-1, head(p), last(p), -1));
    }

    @Compiled
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
            ListCons<Point> exits = filter(possibleDestinations, (Point d) -> isWalkable2(map, d) * noneof(visited, (Point v) -> v.x == d.x && v.y == d.y ? 1 : 0));// here * === &&
            ListCons<Point> arriveds = filter(exits, (Point e) -> any(destinations, (Point d) -> d.x == e.x && d.y == e.y ? 1 : 0));
            ListCons<Point> continueds = filter(exits, (Point e) -> noneof(arriveds, (Point d) -> d.x == e.x && d.y == e.y ? 1 : 0));
            ListCons<ListCons<Point>> exitRoutes = map(continueds, (Point e) -> cons(e, thisRoute));
            Queue<ListCons<Point>> filledQueue = fold0(exitRoutes, emptier.b, (r, i) -> queue_enqueue(r, i));
            retval = waveFromPointToNearestJunction(map, filledQueue, destinations, concat2_set(visited, exits), concat2_set(acc, map(arriveds, (e) -> cons(e, thisRoute))));
        }
        return retval;
    }

    @Compiled
    public ListCons<ParsedEdge> findEdgesForPoint(AIState state, Point pos) {
        return filter(state.parsedStaticMap.parsedEdges, (e) -> pointInEdge(pos, e));
    }

    @Compiled
    private Integer pointInEdge(Point pos, ParsedEdge e) {
        return any(e.edge, (Point ep) -> pos.x == ep.x && pos.y == ep.y ? 1 : 0);
    }

    @Compiled
    private ParsedStaticMap parseMap(ListCons<ListCons<Integer>> m) {
        ListCons<Point> walkable = concat(mapi(m, 0, (row, rowy) -> cat_maybes(mapi(row, 0, (col, colx) -> isWalkable(col) == 1 ? JUST(new Point(colx, rowy)) : NOTHING()))));
        ListCons<Point> junctions = filter(walkable, (Point w) -> isJunction(m, w.x, w.y));
        ListCons<ParsedEdge> allParsedEdges = concat(map(junctions, (j) -> findNeighbourJunctions(m, j, junctions)));
        // renumber them.
        allParsedEdges = mapi(allParsedEdges, 0, (ParsedEdge pe, Integer ix) -> new ParsedEdge(pe.edge, pe.count, pe.a, pe.b, ix));
        return new ParsedStaticMap(walkable, junctions, allParsedEdges, null, null);
    }

    @Compiled
    private ParsedStaticMap parseStaticMap(ListCons<ListCons<Integer>> map) {
        ParsedStaticMap parsedMap = parseMap(map);
        return parsedMap;
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


    public static WorldState convertMap(String map) {
        String[] rows = map.split("\n");
        ListCons<ListCons<Integer>> result = null;
        Point human = new Point(0, 0);
        ListCons<GhostState> gs = null;
        for (int i1 = rows.length - 1; i1 >= 0; i1--) {
            String row = rows[i1];
            ListCons<Integer> lst = null;
            for (int i = row.length() - 1; i >= 0; i--) {
                CT charValue = CT.convertMapCharacter(row.charAt(i));
                lst = cons(charValue.getValue(), lst);
                if (charValue == CT.LAMBDA) {
                    human.x = i;
                    human.y = i1;
                }
                if (charValue == CT.GHOST) {
                    gs = cons(new GhostState(0, new Point(i, i1), 0), gs);
                }
            }
            result = cons(lst, result);
        }
        return new WorldState(result, new LambdaManState(100, human, 0, 3, 0), gs, 0);
    }

    @Compiled
    public static Integer getMapItem(ListCons<ListCons<Integer>> map, int y, int x) {
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

        WorldState worldState = convertMap(theMap);
        Tuple<AIState, Function2<AIState, WorldState, Tuple<AIState, Integer>>> initResult = new Sample1().entryPoint(worldState.map, null);
        AIState aistate = initResult.a;
        Function2<AIState, WorldState, Tuple<AIState, Integer>> stepFunction = initResult.b;
        printMap(theMap);
        while(true) {
            Tuple<AIState, Integer> apply = stepFunction.apply(aistate, worldState);
            aistate = apply.a;
            int direction = apply.b;
            int nx = worldState.lambdaManState.location.x, ny = worldState.lambdaManState.location.y;
            switch(direction) {
                case 0: ny--; break;
                case 1: nx++; break;
                case 2: ny++; break;
                case 3: nx--; break;
                default: throw new RuntimeException("Invalid move: "+direction);
            }
            if (isWalkable2(worldState.map, new Point(nx, ny)) == 1) {
                System.out.println("WALKING: "+direction);
                String newMap = replaceMap(theMap, worldState.lambdaManState.location.x, worldState.lambdaManState.location.y,' ');
                newMap = replaceMap(newMap, nx, ny,'\\');
                theMap = newMap;
                worldState = convertMap(theMap);
            } else {
                System.out.println("CANNOT WALK: "+direction);
            }
            printMap(theMap);
        }
    }

    private static void printMap(String theMap) {
        System.out.println(theMap);
    }

    private static String replaceMap(String theMap, int nx, int ny, char c) {
        String[] rows = theMap.split("\n");
        StringBuilder sb = new StringBuilder(rows[ny]);
        sb.setCharAt(nx, c);
        rows[ny] = sb.toString();
        StringBuilder total = new StringBuilder();
        for (String row : rows) {
            total.append(row);
            total.append("\n");
        }
        total.setLength(total.length()-1);
        return total.toString();
    }

}
