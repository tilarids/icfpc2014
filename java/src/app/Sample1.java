package app;

import java.io.IOException;

import static app.SortedMap.*;

/**
 * Created by san on 7/25/14.
 */
@SuppressWarnings("Convert2MethodRef")
public class Sample1 extends VMExtras {

    Boolean IN_JAVA = true;

    static final String map1 = "" +
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
    public Tuple<AIState, Function2<AIState, WorldState, Tuple<AIState, Integer>>> entryPoint(WorldState ws, ListCons<ListCons> ghostSpecs) {
        return entryFactual(ws,ghostSpecs);
    }

    @Compiled
    private Tuple<AIState, Function2<AIState, WorldState, Tuple<AIState, Integer>>> entryFactual(WorldState ws, ListCons<ListCons> ghostSpecs) {
        AIState initialState = createInitialState(ws, ghostSpecs);

        Function2<Integer, Integer, Function1<Integer, Integer>> newRowAccessor = list_item(initialState.parsedStaticMap.mapAccessors, 1);
        Integer oldValue = newRowAccessor.apply(VMExtras.GET_READER, 1).apply(0);

        return new Tuple<>(initialState, (nextaistate, worldState) -> performMove(nextaistate, worldState));
    }

    @Compiled
    private int test01() {
        Function2<Integer, Integer, Function1<Integer, Integer>> accessor = array_256();
        Function1<Integer, Integer> reader101 = accessor.apply(VMExtras.GET_READER, 22);
        Function1<Integer, Integer> writer101 = accessor.apply(VMExtras.GET_WRITER, 22);
        Integer __ = writer101.apply(77);
        Integer value = reader101.apply(0);
        return 0;
    }


    @Compiled
    private ListCons<Tuple<Function1<Integer, Integer>, Point>> collectEdgePills(ParsedEdge edge, Point start, ListCons<ListCons<Integer>> map) {
        ListCons<Tuple<Function1<Integer, Integer>, Point>> pathRemaining = dropWhile(edge.edgeAccess, (Tuple<Function1<Integer, Integer>, Point> t) -> ((Point) t.b).x != start.x || ((Point) t.b).y != start.y ? 1 : 0);
        ListCons<Tuple<Function1<Integer, Integer>, Point>> rv = filter(pathRemaining, (Tuple<Function1<Integer, Integer>, Point> t) -> t.a.apply(0) == CT.PILL ? 1 : 0);
        return rv;
    }

    @Compiled
    private ListCons<Point> collectEdgeGhosts(ParsedEdge edge, Point start, ListCons<ListCons<Integer>> map) {
        ListCons<Point> pathOnEdge = dropWhile(edge.edge, (Point p) -> p.x != start.x || p.y != start.y ? 1 : 0);
        return filter(pathOnEdge, (Point p) -> getMapItem(map, p.y, p.x) == CT.GHOST ? 1 : 0);
    }


    @Compiled
    private ListCons<Tuple<Function1<Integer, Integer>, Point>> collectAnyEdgePills(ParsedEdge edge, ListCons<ListCons<Integer>> map) {
        ListCons<Tuple<Function1<Integer, Integer>, Point>> edgeAccess = edge.edgeAccess;
        ListCons<Tuple<Function1<Integer, Integer>, Point>> rv = filter(edgeAccess, (Tuple<Function1<Integer, Integer>, Point> t) -> t.a.apply(0) == CT.PILL ? 1 : 0);
        return rv;
    }

    @Compiled
    private Integer countAnyEdgePills(ParsedEdge edge) {
        ListCons<Tuple<Function1<Integer, Integer>, Point>> edgeAccess = edge.edgeAccess;
        Integer rv = fold0(edgeAccess, 0, (Integer acc, Tuple<Function1<Integer, Integer>, Point> t) -> acc + (t.a.apply(0) == CT.PILL ? 1 : 0));
        return rv;
    }

    @Compiled
    private Integer countMyEdgePills(ParsedEdge edge, Point start) {
        ListCons<Tuple<Function1<Integer, Integer>, Point>> pathRemaining = dropWhile(edge.edgeAccess, (Tuple<Function1<Integer, Integer>, Point> t) -> ((Point)t.b).x != start.x || ((Point)t.b).y != start.y ? 1 : 0);
        Integer rv = fold0(pathRemaining, 0, (Integer acc, Tuple<Function1<Integer, Integer>, Point> t) -> t.a.apply(0) == CT.PILL ? 1 : 0);
        return rv;
    }

    @Compiled
    class EdgeAndCount {
        ParsedEdge pe;
        int count;
        int ghostCount;

        EdgeAndCount(ParsedEdge pe, int count, int ghostCount) {
            this.pe = pe;
            this.count = count;
            this.ghostCount = ghostCount;
        }

        @Override
        public String toString() {
            return "EdgeAndCount{" +
                    "pe=" + pe +
                    ", count=" + count +
                    ", ghostCount=" + ghostCount +
                    '}';
        }
    }


    @Compiled
    private Tuple<AIState, Integer> performMove(AIState aistate, WorldState worldState) {
        Point location = worldState.lambdaManState.location;
        ListCons<ParsedEdge> edgesForPoint = findEdgesForPoint(aistate, location);
        Tuple<AIState, Integer> retval;
        Point newLocation;
        int direction;
        ParsedEdge startEdge;
//            long l = System.nanoTime();
        startEdge = findBestDistantEdge(edgesForPoint, aistate, worldState);
//            l = System.nanoTime() - l;
//            System.out.println("Search time: "+l);
//            if (l > 1000000) {
//                findBestDistantEdge(edgesForPoint, aistate, worldState);
//            }
        ListCons<Point> pathToWalk = dropWhile(startEdge.edge, (Point p) -> p.x != location.x || p.y != location.y ? 1 : 0);
        System.out.println("Chosen long way: " + startEdge.toString());
        if (length(pathToWalk) >= 2) {
            newLocation = head(tail(pathToWalk));
            direction = (newLocation.x > location.x) ?
                    Direction.RIGHT :
                    (newLocation.x < location.x) ?
                            Direction.LEFT :
                            (newLocation.y < location.y) ?
                                    Direction.UP :
                                    Direction.DOWN;
            retval = new Tuple<>(aistate, direction);
        } else {
            retval = new Tuple<>(aistate, Direction.UP);
        }
        // update map cache, remove pill from it
        int nx = retval.b == Direction.LEFT ? location.x - 1 : retval.b == Direction.RIGHT ? location.x + 1 : location.x;
        int ny = retval.b == Direction.UP ? location.y - 1 : retval.b == Direction.DOWN ? location.y + 1 : location.y;
        AIState a = retval.a;
        Function2<Integer, Integer, Function1<Integer, Integer>> newRowAccessor = list_item(a.parsedStaticMap.mapAccessors, ny);
        Integer oldValue = newRowAccessor.apply(VMExtras.GET_READER, nx).apply(0);
        oldValue = oldValue == CT.PILL ? newRowAccessor.apply(VMExtras.GET_WRITER, nx).apply(CT.SPACE) : oldValue;
        return retval;
    }

    @Compiled
    private ListCons<ListCons<ParsedEdge>> waveFromEdgeToNearestEdges(
            AIState aistate,
            WorldState worldState,
            Queue<ListCons<ParsedEdge>> edgeQueue,
            SortedMap<Integer> visited,
            ListCons<ListCons<ParsedEdge>> acc,
            int edgesWithDotsSoFar) {
        ListCons<ListCons<ParsedEdge>> retval;
        if (queue_isempty(edgeQueue)) {
            retval = acc;
        } else {
            retval = waveFromEdgeToNearestEdges0(aistate, worldState, edgeQueue, visited, acc, edgesWithDotsSoFar);
        }
        return retval;
    }

    @Compiled
    private ListCons<ListCons<ParsedEdge>> waveFromEdgeToNearestEdges0(AIState aistate, WorldState worldState, Queue<ListCons<ParsedEdge>> edgeQueue, SortedMap<Integer> visited, ListCons<ListCons<ParsedEdge>> acc, int edgesWithDotsSoFar) {
        ListCons<ListCons<ParsedEdge>> retval;
        Tuple<ListCons<ParsedEdge>, Queue<ListCons<ParsedEdge>>> reduced = queue_dequeue(edgeQueue);
        ListCons<ParsedEdge> lookingEdge = reduced.a;
        ListCons<ParsedEdge> following = findFollowingEdges(aistate.parsedStaticMap.parsedEdges, lookingEdge);
        following = filter(following, (ParsedEdge f) -> 1 - sorted_map_contains(visited, f.edgeNumber));
        ListCons<ParsedEdge> withDots = filter(following, (pe) -> length(collectAnyEdgePills(pe, worldState.map)) > 0 ? 1 : 0);
        // saving both forward/backward edges to visited
        SortedMap<Integer> nvisited = sorted_map_assoc_all(visited, map(following, (ParsedEdge f) -> new Tuple<>(f.edgeNumber, 0)));
        nvisited = sorted_map_assoc_all(nvisited, map(following, (ParsedEdge f) -> new Tuple<>(f.opposingEdgeNumber, 0)));
        //
        ListCons<ListCons<ParsedEdge>> newRoutes = map(following, (ParsedEdge next) -> cons(next, lookingEdge));
        Queue<ListCons<ParsedEdge>> newqq = fold0(newRoutes, reduced.b, (Queue<ListCons<ParsedEdge>> qq, ListCons<ParsedEdge> nr) -> queue_enqueue(qq, nr));
        ListCons<ListCons<ParsedEdge>> newAcc = concat2_set(newRoutes, acc);
        // found some dots and
        boolean stopCondition = acc != null ? (edgesWithDotsSoFar > 3 && length(acc) > 15 && length(head(newAcc)) > length(head(acc))) : false;
        retval = stopCondition ? acc : waveFromEdgeToNearestEdges(aistate, worldState, newqq, nvisited, newAcc, edgesWithDotsSoFar + length(withDots));
        return retval;
    }

    /**
     * find edges linked to given one
     */
    @Compiled
    private ListCons<ParsedEdge> findFollowingEdges(ListCons<ParsedEdge> parsedEdges, ListCons<ParsedEdge> lookingEdge) {
        return filter(parsedEdges, (ParsedEdge pe) -> pointEquals(pe.a, endingPointOfEdgeRoute(lookingEdge)));
    }

    @Compiled
    private Integer pointEquals(Point a, Point b) {
        return (a.x == b.x && a.y == b.y) ? 1 : 0;
    }

    @Compiled
    private Point endingPointOfEdgeRoute(ListCons<ParsedEdge> lookingEdge) {
        ParsedEdge lastEdge = head(lookingEdge);
        return lastEdge.b;
    }

    @Compiled
    public int countRoutePills(ListCons<ParsedEdge> route) {
        return fold0(route, 0, (acc, pe) -> acc + countAnyEdgePills(pe));
    }

    @Compiled
    private ParsedEdge findBestDistantEdge(ListCons<ParsedEdge> currentEdges, AIState aistate, WorldState worldState) {
        Queue<ListCons<ParsedEdge>> q = queue_new();
        q = fold0(currentEdges, q, (Queue<ListCons<ParsedEdge>> qq, ParsedEdge e) -> queue_enqueue(qq, cons(e, null)));
        ListCons<ListCons<ParsedEdge>> dests = waveFromEdgeToNearestEdges(aistate, worldState, q, sorted_map_assoc_all(new SortedMap<Integer>(null, 0), map(currentEdges, (ParsedEdge e) -> new Tuple<>(e.edgeNumber, 0))), null, 0);
        ListCons<Tuple<ListCons<ParsedEdge>, Integer>> scores = map(dests, (r) -> new Tuple<>(r, 5 * countMyEdgePills(last(r), worldState.lambdaManState.location) + countRoutePills(tail(reverse(r)))));
        Tuple<ListCons<ParsedEdge>, Integer> winningRoute = maximum_by(scores, (Tuple<ListCons<ParsedEdge>, Integer> t) -> t.b);
        if (IN_JAVA) {
//            map(scores, (Tuple<ListCons<ParsedEdge>, Integer> route) -> {
//                System.out.println("Found : "+route.b);
//                printList(reverse(route.a));
//                return 0;
//            });
        }
        ParsedEdge myStart = head(reverse(winningRoute.a));
        return myStart;
    }

    private static int printList(Cons list) {
        if (list == null) return 0;
        System.out.println(list.data);
        if (list.addr instanceof Cons) {
            printList((Cons) list.addr);
        }
        return 0;
    }

    @Compiled
    class AIState {

        ParsedStaticMap parsedStaticMap;
        int lastDirection;
        int tick;
        ListCons<GhostInfo> ghostInfos;

        AIState(ParsedStaticMap parsedStaticMap, int lastDirection, int tick, ListCons<GhostInfo> ghostInfos) {
            this.parsedStaticMap = parsedStaticMap;
            this.lastDirection = lastDirection;
            this.tick = tick;
            this.ghostInfos = ghostInfos;
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
            return "Point(" + x + "," + y + ")";
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
    static class WorldState extends Cons {
        ListCons<ListCons<Integer>> map;
        LambdaManState lambdaManState;
        ListCons<GhostState> ghosts;
        int fruitStatus;

        WorldState(ListCons<ListCons<Integer>> map, LambdaManState lambdaManState, ListCons<GhostState> ghosts, int fruitStatus) {
            super(null, null);
            this.map = map;
            this.lambdaManState = lambdaManState;
            this.ghosts = ghosts;
            this.fruitStatus = fruitStatus;
        }
    }

    @Compiled
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

    // ((12, 16), (-1, (((12, 16), ((11, 16), ((10, 16), 0))), (2, (90, (10, 16))))))

    @Compiled
    class ParsedEdge {
        Point a;
        Point b;
        ListCons<Point> edge;
        ListCons<Tuple<Function1<Integer, Integer>, Point>> edgeAccess;
        int count;
        int edgeNumber;
        int opposingEdgeNumber;

        ParsedEdge(Point a, Point b, ListCons<Point> edge, ListCons<Tuple<Function1<Integer, Integer>, Point>> edgeAccess, int count, int edgeNumber, int opposingEdgeNumber) {
            this.a = a;
            this.b = b;
            this.edge = edge;
            this.edgeAccess = edgeAccess;
            this.count = count;
            this.edgeNumber = edgeNumber;
            this.opposingEdgeNumber = opposingEdgeNumber;
        }

        @Override
        public String toString() {
            return "[Edge: form=" + a + " to=" + b + " count=" + count + " id=" + edgeNumber + "]";
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
        SortedMap<Point> walkable;

        // list of all junctions (3+ exits)
        SortedMap<Point> junctions;

        // edges
        ListCons<ParsedEdge> parsedEdges;
        ListCons<VerticalRow> verticalFinders;
        ListCons<HorizontalRow> horizontalFinders;
        ListCons<Function2<Integer, Integer, Function1<Integer, Integer>>> mapAccessors;


        ParsedStaticMap(SortedMap<Point> walkable, SortedMap<Point> junctions,
                        ListCons<ParsedEdge> parsedEdges, ListCons<VerticalRow> verticalFinders,
                        ListCons<HorizontalRow> horizontalFinders,
                        ListCons<Function2<Integer, Integer, Function1<Integer, Integer>>> mapAccessors) {
            this.junctions = junctions;
            this.walkable = walkable;
            this.parsedEdges = parsedEdges;
            this.verticalFinders = verticalFinders;
            this.horizontalFinders = horizontalFinders;
            this.mapAccessors = mapAccessors;
        }
    }


    @Compiled
    private AIState createInitialState(WorldState ws, ListCons<ListCons> ghostSpecs) {
        ListCons<Integer> ghostNumbers = range_n(length(ws.ghosts));
        return new AIState(parseStaticMap(ws.map), 0, 0,
                zip3_with(((GhostState g, ListCons s, Integer ind) -> new GhostInfo(g.location, s, ind)), ws.ghosts, ghostSpecs, ghostNumbers));
    }

    @Compiled
    public int isWall(int test) {
        return test == CT.WALL ? 1 : 0;
    }

    @Compiled
    public static int isWalkable(int test) {
        int retval = 77;
        if (test == CT.WALL) retval = 0;
        else retval = 1;
        return retval;
    }

    @Compiled
    public static int isWalkable2(ListCons<ListCons<Integer>> map, Point p) {
        return isWalkable(getMapItem(map, p.y, p.x));
    }

    @Compiled
    public static int isWalkable3(ListCons<ListCons<Integer>> map, int x, int y) {
        return isWalkable(getMapItem(map, y, x));
    }


    @Compiled
    public int isJunction(ListCons<ListCons<Integer>> map, int x, int y) {
        int a1 = isWalkable(getMapItem(map, y - 1, x));
        int a2 = isWalkable(getMapItem(map, y + 1, x));
        int a3 = isWalkable(getMapItem(map, y, x - 1));
        int a4 = isWalkable(getMapItem(map, y, x + 1));
        return a1 + a2 + a3 + a4 > 2 ? 1 : 0;
    }

    @Compiled
    public ListCons<ParsedEdge> findNeighbourJunctions(ListCons<ListCons<Integer>> map,
                                                       Point somePoint,
                                                       SortedMap<Point> allJunctions,
                                                       ListCons<Point> allJunctions2,
                                                       ListCons<Function2<Integer, Integer,
                                                               Function1<Integer, Integer>>> accessors) {
        ListCons<ListCons<Point>> allNeighbourJunctionsPaths =
                waveFromPointToNearestJunction(map,
                        queue_enqueue(queue_new(), cons(somePoint, null)),
                        allJunctions,
                        allJunctions2,
                        sorted_map_assoc(new SortedMap<>(null, 0), getPointKey(somePoint), somePoint), null);
        return map(allNeighbourJunctionsPaths, (p) ->
                new ParsedEdge(head(p), last(p), p, makeEdgeAccess(p, accessors), length(p) - 1, -1, -1));
    }


    @Compiled
    private ListCons<Tuple<Function1<Integer, Integer>, Point>> makeEdgeAccess(ListCons<Point> p, ListCons<Function2<Integer, Integer, Function1<Integer, Integer>>> accessors) {
        return zip(map(p, (Point pt) ->
                ((Function2<Integer, Integer, Function1<Integer, Integer>>) list_item(accessors, pt.y)).apply(VMExtras.GET_READER, pt.x)), p);

    }

    @Compiled
    private ListCons<ListCons<Point>> waveFromPointToNearestJunction(ListCons<ListCons<Integer>> map, Queue<ListCons<Point>> pointQueue, SortedMap<Point> destinations, ListCons<Point> destinations2, SortedMap<Point> visited, ListCons<ListCons<Point>> acc) {
        ListCons<ListCons<Point>> retval;
        if (queue_isempty(pointQueue)) {
            retval = acc;
        } else {
            retval = waveFromPointToNearestJunction0(map, pointQueue, destinations, destinations2, visited, acc);
        }
        return retval;
    }

    @Compiled
    private Integer getPointKey(Point pt) {
        return pt.x * 300 + pt.y;
    }

    @Compiled
    private ListCons<Tuple<Integer, Point>> addPointKeyAll(ListCons<Point> in) {
        return map(in, (Point pt) -> new Tuple<>(getPointKey(pt), pt));
    }

    @Compiled
    private ListCons<ListCons<Point>> waveFromPointToNearestJunction0(ListCons<ListCons<Integer>> map, Queue<ListCons<Point>> pointQueue, SortedMap<Point> destinations, ListCons<Point> destinations2, SortedMap<Point> visited, ListCons<ListCons<Point>> acc) {
        ListCons<ListCons<Point>> retval;
        Tuple<ListCons<Point>, Queue<ListCons<Point>>> emptier = queue_dequeue(pointQueue);
        ListCons<Point> thisRoute = emptier.a;
        Point weAreHere = head(thisRoute);
        ListCons<Tuple<Integer, Point>> possibleDestinations = addPointKeyAll(
                cons(new Point(weAreHere.x + 1, weAreHere.y),
                        cons(new Point(weAreHere.x - 1, weAreHere.y),
                                cons(new Point(weAreHere.x, weAreHere.y + 1),
                                        cons(new Point(weAreHere.x, weAreHere.y - 1), null))
                        )
                )
        );
        ListCons<Tuple<Integer, Point>> exits = filter(possibleDestinations, (Tuple<Integer, Point> d) -> isWalkable2(map, d.b) * (1 - sorted_map_contains(visited, d.a)));// here * === &&
        ListCons<Tuple<Integer, Point>> arrivedsList = filter(exits, (Tuple<Integer, Point> e) -> sorted_map_contains(destinations, e.a));
        ListCons<Tuple<Integer, Point>> arrivedsList2 = filter(exits, (Tuple<Integer, Point> e) -> any(destinations2, (d) -> pointEquals(e.b, d)));
        SortedMap<Point> arriveds = sorted_map_assoc_all(new SortedMap<Point>(null, 1), arrivedsList);
        ListCons<Tuple<Integer, Point>> continueds = filter(exits, (Tuple<Integer, Point> e) -> 1 - sorted_map_contains(arriveds, e.a));
        ListCons<ListCons<Point>> exitRoutes = map(continueds, (Tuple<Integer, Point> e) -> cons(e.b, thisRoute));
        Queue<ListCons<Point>> filledQueue = fold0(exitRoutes, emptier.b, (r, i) -> queue_enqueue(r, i));
        ListCons<ListCons<Point>> newacc = concat2_set(acc, map(arrivedsList, (Tuple<Integer, Point> e) -> cons(e.b, thisRoute)));
        if (length(arrivedsList) != length(arrivedsList2)) {
            debug(5000000);
            debug(destinations);
            debug(destinations2);
            debug(arrivedsList);
            debug(arrivedsList2);
            debug(exits);
            breakpoint();
            debug(filter(exits, (Tuple<Integer, Point> e) -> sorted_map_contains(destinations, e.a)));
        }
        retval = waveFromPointToNearestJunction(map, filledQueue, destinations, destinations2, sorted_map_assoc_all(visited, exits), newacc);
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
        debug(4000001);
        ListCons<Function2<Integer, Integer, Function1<Integer, Integer>>> accessors = map(m, (x) -> array_256());
        debug(4000002);
        ListCons<Tuple<ListCons<Integer>, Function2<Integer, Integer, Function1<Integer, Integer>>>> mapAccessors = zip_with((a, b) -> new Tuple<>(a, b), m, accessors);
        debug(4000003);
        ListCons<ListCons<Integer>> __ = map(mapAccessors, (Tuple<ListCons<Integer>, Function2<Integer, Integer, Function1<Integer, Integer>>> t) ->
                        mapi(t.a, 0, (val, ix) -> t.b.apply(VMExtras.GET_WRITER, ix).apply(val))
        );
        debug(4000004);
        ListCons<ListCons<Point>> toConcat = mapi(m, 0, (row, rowy) -> my_cat_maybes(collectWalkableXY(row, rowy)));
        debug(4000005);
        ListCons<Point> walkableList = concat(toConcat);
        ListCons<Point> junctionsList = filter(walkableList, (Point w) -> isJunction(m, w.x, w.y));
        debug(4000006);
        SortedMap<Point> walkable = sorted_map_assoc_all(new SortedMap<Point>(null, 0), addPointKeyAll(walkableList));
        debug(4000007);
        SortedMap<Point> junctions = sorted_map_assoc_all(new SortedMap<Point>(null, 0),
                addPointKeyAll(junctionsList));
        debug(4000008);
        ListCons<ParsedEdge> allParsedEdges = concat(map(junctionsList, (j) -> findNeighbourJunctions(m, j, junctions, junctionsList, accessors)));
        // renumber them.
        debug(4000009);
        ListCons<ParsedEdge> allParsedEdges2 = mapi(allParsedEdges, 0, (ParsedEdge pe, Integer ix) -> new ParsedEdge(pe.a, pe.b, pe.edge, pe.edgeAccess, pe.count, ix, -1));
        debug(4000010);
        ListCons<ParsedEdge> allParsedEdges3 = mapi(allParsedEdges2, 0, (ParsedEdge pe, Integer ix) -> new ParsedEdge(pe.a, pe.b, pe.edge, pe.edgeAccess, pe.count, pe.edgeNumber, edgeNumber(findEdge(pe.b, pe.a, allParsedEdges2))));
        debug(4000011);
        return new ParsedStaticMap(walkable, junctions, allParsedEdges3, null, null, accessors);
    }

    @Compiled
    private ParsedEdge findEdge(Point a, Point b, ListCons<ParsedEdge> edges) {
        return head(filter(edges, (ParsedEdge e) -> pointEquals(e.a, a) * pointEquals(e.b, b)));

    }

    @Compiled
    private Integer edgeNumber(ParsedEdge edge) {
        return edge.edgeNumber;
    }

    @Compiled
    private ListCons<Point> my_cat_maybes(ListCons<Maybe<Point>> maybeListCons) {
        ListCons<Point> rv = cat_maybes(maybeListCons);
        return rv;
    }

    @Compiled
    private ListCons<Maybe<Point>> collectWalkableXY(ListCons<Integer> row, Integer rowy) {
        return mapi(row, 0, (col, colx) -> isWalkable(col) > 0 ? JUST(new Point(colx, rowy)) : NOTHING());
    }

    @Compiled
    private ParsedStaticMap parseStaticMap(ListCons<ListCons<Integer>> map) {
        return parseMap(map);
    }


    @Compiled
    private int test2() {
        Queue queue = queue_enqueue(queue_enqueue(queue_enqueue(queue_new(), 1), 2), 3);
        Tuple<Object, Queue> q1 = queue_dequeue(queue);
        q1 = queue_dequeue(q1.b);
        q1 = queue_dequeue(q1.b);
        return 1;
    }

    @Compiled
    private int test3() {
//        Integer[] test = {23, 2, 11, 3, 2, 34, 1, 17, 2, 3};
//        ListCons<Tuple<Integer, Integer>> arr = null;
//        for (Integer x : test) {
//            arr = cons (new Tuple<>(x, 42), arr);
//        }
        SortedMap<Integer> map = new SortedMap<Integer>(null, 0);
//        SortedMap<Integer> new_map = sorted_map_assoc_all(map, arr);
//        System.out.println("sorted:" + sorted_map_walk(new_map, null, (ListCons<Integer> acc, Tuple<Integer, Integer> x) -> cons(x.a, acc)));

        SortedMap<Integer> new_map2 = sorted_map_assoc(sorted_map_assoc(sorted_map_assoc(map, 30, 31), 20, 21), 10, 11);
        return sorted_map_get(sorted_map_assoc(new_map2, 20, 33), 20, 88);
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
                int charValue = CT.convertMapCharacter(row.charAt(i));
                lst = cons(charValue, lst);
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
        return new WorldState(result, new LambdaManState(100, human, 0, Direction.LEFT, 0), gs, 0);
    }

    @Compiled
    public static Integer getMapItem(ListCons<ListCons<Integer>> map, int y, int x) {
        return y < 0 || x < 0 ? 0 :
                (Integer) list_item_def((Cons) list_item_def(map, y, 0), x, 0);
    }

    @Compiled
    static class GHCState {
        GhostState ghostState;
        SortedMap<Integer> regs;
        SortedMap<Integer> data;

        GHCState(GhostState ghostState, SortedMap<Integer> regs, SortedMap<Integer> data) {
            this.ghostState = ghostState;
            this.regs = regs;
            this.data = data;
        }
    }


    @Compiled
    static class GhostInfo {
        Point initialPoint; //should be used to handle INT 4
        ListCons<Cons> spec;
        int index;

        GhostInfo(Point initialPoint, ListCons<Cons> spec, int index) {
            this.initialPoint = initialPoint;
            this.spec = spec;
            this.index = index;
        }
    }


    public static void main(String[] args) throws Exception {
        if (1 == 1) runInteractiveGCC();


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
        if (false) {
            /*
            new Sample1().test3();
            System.out.println(LambdaGhostEmulator.bit_split(10) + ":" + LambdaGhostEmulator.bit_split(127));
            System.out.println(LambdaGhostEmulator.emulate_bitop(13, 5, (xx, yy) -> xx * yy));
            ListCons<Cons> spec =
                    cons(cons(cons(0, cons(cons(0, 0), cons(cons(2, 1), null))),
                                    cons(cons(13, cons(0, null)), cons(cons(14, null), null))),
                            cons(cons(cons(0, cons(cons(0, 0), cons(cons(2, 1), null))),
                                            cons(cons(13, cons(0, null)), cons(cons(14, null), null))),
                                    cons(cons(cons(0, cons(cons(0, 0), cons(cons(2, 1), null))),
                                                    cons(cons(13, cons(0, null)), cons(cons(14, null), null))),
                                            cons(cons(cons(0, cons(cons(0, 0),
                                                    cons(cons(2, 1), null))), cons(cons(13, cons(0, null)), cons(cons(14, null), null))), null)))
                    );

            System.out.println("direction:" + LambdaGhostEmulator.getGhostDirection(worldState, first(spec)));
            System.exit(0);
            */
        }

        Tuple<AIState, Function2<AIState, WorldState, Tuple<AIState, Integer>>> initResult = new Sample1().entryPoint(worldState, null);
        AIState aistate = initResult.a;
        Function2<AIState, WorldState, Tuple<AIState, Integer>> stepFunction = initResult.b;

        printMap(theMap);
        while (true) {
            Tuple<AIState, Integer> apply = stepFunction.apply(aistate, worldState);
            aistate = apply.a;
            int direction = apply.b;
            int nx = worldState.lambdaManState.location.x, ny = worldState.lambdaManState.location.y;
            switch (direction) {
                case Direction.UP:
                    ny--;
                    break;
                case Direction.RIGHT:
                    nx++;
                    break;
                case Direction.DOWN:
                    ny++;
                    break;
                case Direction.LEFT:
                    nx--;
                    break;
                default:
                    throw new RuntimeException("Invalid move: " + direction);
            }
            if (isWalkable2(worldState.map, new Point(nx, ny)) == 1) {
                System.out.println("WALKING: " + direction);
                String newMap = replaceMap(theMap, worldState.lambdaManState.location.x, worldState.lambdaManState.location.y, ' ');
                newMap = replaceMap(newMap, nx, ny, '\\');
                theMap = newMap;
                worldState = convertMap(theMap);
            } else {
                System.out.println("CANNOT WALK: " + direction);
            }
            printMap(theMap);
        }
    }

    private static void runInteractiveGCC() throws Exception {
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

        GCCEmulator cpu = new GCCEmulator("test.txt", 2);
        cpu.storeInFrame(0, worldState);
        cpu.storeInFrame(1, new GCCEmulator.D(0));

        GCCEmulator.D initialRun = cpu.run(0);
        assert initialRun.tag == GCCEmulator.Tag.Cons;
        GCCEmulator.D aistate = initialRun.cons_p.data;
        GCCEmulator.D stepFun = initialRun.cons_p.addr;

        printMap(theMap);
        while (true) {
            cpu.load(aistate);
            cpu.load(worldState);

            GCCEmulator.D apply = cpu.cont(stepFun, 2);
            assert apply.tag == GCCEmulator.Tag.Cons;


            aistate = apply.cons_p.data;
            GCCEmulator.D direction = apply.cons_p.addr;
            assert direction.tag == GCCEmulator.Tag.Int;
            int nx = worldState.lambdaManState.location.x, ny = worldState.lambdaManState.location.y;
            switch (direction.int_p) {
                case Direction.UP:
                    ny--;
                    break;
                case Direction.RIGHT:
                    nx++;
                    break;
                case Direction.DOWN:
                    ny++;
                    break;
                case Direction.LEFT:
                    nx--;
                    break;
                default:
                    throw new RuntimeException("Invalid move: " + direction);
            }
            if (isWalkable2(worldState.map, new Point(nx, ny)) == 1) {
                System.out.println("WALKING: " + direction);
                String newMap = replaceMap(theMap, worldState.lambdaManState.location.x, worldState.lambdaManState.location.y, ' ');
                newMap = replaceMap(newMap, nx, ny, '\\');
                theMap = newMap;
                worldState = convertMap(theMap);
            } else {
                System.out.println("CANNOT WALK: " + direction);
            }
            printMap(theMap);
        }
    }

    private static void printMap(String theMap) {
        String[] rows = theMap.split("\n");
        int width = rows[0].length();
        System.out.print("      ");
        for (int x = 0; x < width; x++) {
            if ((x % 2) == 0) {
                System.out.print(String.format("%02d", x));
            } else {
                System.out.print("  ");
            }
        }
        System.out.println();
        System.out.print("      ");
        for (int x = 0; x < width; x++) {
            if ((x % 2) != 0) {
                System.out.print(String.format("%02d", x));
            } else {
                System.out.print("  ");
            }
        }
        System.out.println();
        for (int y = 0; y < rows.length; y++) {
            System.out.print(String.format("%3d - ", y));
            String row = rows[y];
            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);
                System.out.print(("" + c) + c);
            }
            System.out.println();
        }
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
        total.setLength(total.length() - 1);
        return total.toString();
    }

}
