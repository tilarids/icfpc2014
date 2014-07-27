package app;

/**
 * Created by san on 7/25/14.
 */
@SuppressWarnings("Convert2MethodRef")
public class Sample1 extends VMExtras {

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
    public Tuple<AIState, Function2<AIState, WorldState, Tuple<AIState, Integer>>> entryPoint(WorldState ws, Object undocumented) {
        return entryFactual(ws);
//        int x = location.a;
//        int y = location.b;


//        CT left = getMapItem(map, y, x-1);
//        CT right = getMapItem(map, y, x+1);
//        CT top = getMapItem(map, y-1, x);
//        CT bottom = getMapItem(map, y+1, x);


    }

    @Compiled
    private Tuple<AIState, Function2<AIState, WorldState, Tuple<AIState, Integer>>> entryFactual(WorldState ws) {
        AIState initialState = createInitialState(ws.map);
        return new Tuple<>(initialState, (nextaistate, worldState) -> performMove(nextaistate, worldState));
    }


    @Compiled
    private ListCons<Point> collectEdgePills(ParsedEdge edge, Point start, ListCons<ListCons<Integer>> map) {
        ListCons<Point> pathOnEdge = dropWhile(edge.edge, (Point p) -> p.x != start.x || p.y != start.y ? 1 : 0);
        return filter(pathOnEdge, (Point p) -> getMapItem(map, p.y, p.x) == CT.PILL ? 1 : 0);
    }

    @Compiled
    private ListCons<Point> collectEdgeGhosts(ParsedEdge edge, Point start, ListCons<ListCons<Integer>> map) {
        ListCons<Point> pathOnEdge = dropWhile(edge.edge, (Point p) -> p.x != start.x || p.y != start.y ? 1 : 0);
        return filter(pathOnEdge, (Point p) -> getMapItem(map, p.y, p.x) == CT.GHOST ? 1 : 0);
    }


    @Compiled
    private ListCons<Point> collectAnyEdgePills(ParsedEdge edge, ListCons<ListCons<Integer>> map) {
        ListCons<Point> pathOnEdge = edge.edge;
        ListCons<Point> rv = filter(pathOnEdge, (Point p) -> getMapItem(map, p.y, p.x) == CT.PILL ? 1 : 0);
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
    }

    @Compiled
    private Tuple<AIState, Integer> performMove(AIState aistate, WorldState worldState) {
        Point location = worldState.lambdaManState.location;
        ListCons<ParsedEdge> edgesForPoint = findEdgesForPoint(aistate, location);
        ListCons<EdgeAndCount> collectedPoints = map(edgesForPoint, (e) -> new EdgeAndCount(e,
                length(collectEdgePills(e, location, worldState.map)),
                length(collectEdgeGhosts(e, location, worldState.map))));
        EdgeAndCount ec = maximum_by(collectedPoints, (EdgeAndCount cp) -> (cp.count - 100 * cp.ghostCount));
        ListCons<Point> pathToWalk = dropWhile(ec.pe.edge, (Point p) -> p.x != location.x || p.y != location.y ? 1 : 0);
        Tuple<AIState, Integer> retval;
        Point newLocation;
        int direction;
        ParsedEdge startEdge;
        if (length(pathToWalk) < 2 || ec.count == 0) { // nothing close to me
//            long l = System.nanoTime();
            startEdge = findBestDistantEdge(edgesForPoint, aistate, worldState);
//            l = System.nanoTime() - l;
//            System.out.println("Search time: "+l);
//            if (l > 1000000) {
//                findBestDistantEdge(edgesForPoint, aistate, worldState);
//            }
            pathToWalk = dropWhile(startEdge.edge, (Point p) -> p.x != location.x || p.y != location.y ? 1 : 0);
            System.out.println("Chosen long way: " + startEdge.toString());
        }
        if (ec.count == 0 && 1 == 2) {
            // ensure that we don't stuck when there is no pills nearby. just go somewhere (and don't go back)
            // branch svg
            direction = isWalkable3(worldState.map, location.x, location.y - 1) == 1 && aistate.lastDirection != Direction.DOWN ?
                    Direction.UP :
                    isWalkable3(worldState.map, location.x + 1, location.y) == 1 && aistate.lastDirection != Direction.LEFT ?
                            Direction.RIGHT :
                            isWalkable3(worldState.map, location.x, location.y + 1) == 1 && aistate.lastDirection != Direction.UP ?
                                    Direction.DOWN :
                                    Direction.LEFT;
            retval = new Tuple<>(new AIState(aistate.parsedStaticMap, direction), direction);
        } else if (length(pathToWalk) >= 2) {
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
        return retval;
    }

    @Compiled
    private ListCons<ListCons<ParsedEdge>> waveFromEdgeToNearestEdges(
            AIState aistate,
            WorldState worldState,
            Queue<ListCons<ParsedEdge>> edgeQueue,
            ListCons<Integer> visited,
            ListCons<ListCons<ParsedEdge>> acc) {
        ListCons<ListCons<ParsedEdge>> retval;
        if (queue_isempty(edgeQueue)) {
            retval = acc;
        } else {
            retval = waveFromEdgeToNearestEdges0(aistate, worldState, edgeQueue, visited, acc);
        }
        return retval;
    }

    @Compiled
    private ListCons<ListCons<ParsedEdge>> waveFromEdgeToNearestEdges0(AIState aistate, WorldState worldState, Queue<ListCons<ParsedEdge>> edgeQueue, ListCons<Integer> visited, ListCons<ListCons<ParsedEdge>> acc) {
        ListCons<ListCons<ParsedEdge>> retval;
        Tuple<ListCons<ParsedEdge>, Queue<ListCons<ParsedEdge>>> reduced = queue_dequeue(edgeQueue);
        ListCons<ParsedEdge> lookingEdge = reduced.a;
        ListCons<ParsedEdge> following = findFollowingEdges(aistate.parsedStaticMap.parsedEdges, lookingEdge);
        following = filter(following, (ParsedEdge f) -> noneof(visited, (v) -> (v == f.edgeNumber) ? 1 : 0));
        ListCons<ParsedEdge> withDots = filter(following, (pe) -> length(collectAnyEdgePills(pe, worldState.map)) > 0 ? 1 : 0);
        ListCons<Integer> nvisited = concat2_set(visited, map(following, (ParsedEdge f) -> f.edgeNumber));
        ListCons<ListCons<ParsedEdge>> newRoutes = map(following, (ParsedEdge next) -> cons(next, lookingEdge));
        Queue<ListCons<ParsedEdge>> newqq = fold0(newRoutes, reduced.b, (Queue<ListCons<ParsedEdge>> qq, ListCons<ParsedEdge> nr) -> queue_enqueue(qq, nr));
        ListCons<ListCons<ParsedEdge>> newAcc = concat2_set(newRoutes, acc);
        retval = length(withDots) > 0? newAcc : waveFromEdgeToNearestEdges(aistate, worldState, newqq, nvisited, newAcc);
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
    private ParsedEdge findBestDistantEdge(ListCons<ParsedEdge> currentEdges, AIState aistate, WorldState worldState) {
        Queue<ListCons<ParsedEdge>> q = queue_new();
        q = fold0(currentEdges, q, (Queue<ListCons<ParsedEdge>> qq, ParsedEdge e) -> queue_enqueue(qq, cons(e, null)));
        ListCons<ListCons<ParsedEdge>> reverseDests = reverse(waveFromEdgeToNearestEdges(aistate, worldState, q, map(currentEdges, (ParsedEdge e) -> e.edgeNumber), null));
        ListCons<ListCons<ParsedEdge>> sortedRoutes = filter(reverseDests, (r) -> (any(r, (r0) -> length(collectAnyEdgePills(r0, worldState.map)) > 0 ? 1 : 0) > 0 && length(r) >= 2) ? 1 : 0);
        ListCons<ParsedEdge> someRoute = head(sortedRoutes);
        ParsedEdge myStart = head(reverse(someRoute));
        return myStart;
    }

    @Compiled
    class AIState {

        ParsedStaticMap parsedStaticMap;
        int lastDirection;


        AIState(ParsedStaticMap parsedStaticMap, int lastDirection) {
            this.parsedStaticMap = parsedStaticMap;
            this.lastDirection = lastDirection;
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
    private AIState createInitialState(ListCons<ListCons<Integer>> map) {
        return new AIState(parseStaticMap(map), 0);
    }

    @Compiled
    public int isWall(int test) {
        return test == 0 ? 1 : 0;
    }

    @Compiled
    public static int isWalkable(int test) {
        int retvla = 77;
        if (test == 0) retvla = 0;
        else retvla = 1;
        return retvla;
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
    public ListCons<ParsedEdge> findNeighbourJunctions(ListCons<ListCons<Integer>> map, Point somePoint, ListCons<Point> allJunctions) {
        ListCons<ListCons<Point>> allNeighbourJunctionsPaths = waveFromPointToNearestJunction(map, queue_enqueue(queue_new(), cons(somePoint, null)), allJunctions, cons(somePoint, null), null);
        return map(allNeighbourJunctionsPaths, (p) -> new ParsedEdge(p, length(p) - 1, head(p), last(p), -1));
    }

    @Compiled
    private ListCons<ListCons<Point>> waveFromPointToNearestJunction(ListCons<ListCons<Integer>> map, Queue<ListCons<Point>> pointQueue, ListCons<Point> destinations, ListCons<Point> visited, ListCons<ListCons<Point>> acc) {
        ListCons<ListCons<Point>> retval;
        if (queue_isempty(pointQueue)) {
            retval = acc;
        } else {
            retval = waveFromPointToNearestJunction0(map, pointQueue, destinations, visited, acc);
        }
        return retval;
    }

    @Compiled
    private ListCons<ListCons<Point>> waveFromPointToNearestJunction0(ListCons<ListCons<Integer>> map, Queue<ListCons<Point>> pointQueue, ListCons<Point> destinations, ListCons<Point> visited, ListCons<ListCons<Point>> acc) {
        ListCons<ListCons<Point>> retval;
        Tuple<ListCons<Point>, Queue<ListCons<Point>>> emptier = queue_dequeue(pointQueue);
        ListCons<Point> thisRoute = emptier.a;
        Point weAreHere = head(thisRoute);
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
        ListCons<ListCons<Point>> toConcat = mapi(m, 0, (row, rowy) -> my_cat_maybes(collectWalkableXY(row, rowy)));
        ListCons<Point> walkable = concat(toConcat);
        ListCons<Point> junctions = filter(walkable, (Point w) -> isJunction(m, w.x, w.y));
        ListCons<ParsedEdge> allParsedEdges = concat(map(junctions, (j) -> findNeighbourJunctions(m, j, junctions)));
        // renumber them.
        allParsedEdges = mapi(allParsedEdges, 0, (ParsedEdge pe, Integer ix) -> new ParsedEdge(pe.edge, pe.count, pe.a, pe.b, ix));
        return new ParsedStaticMap(walkable, junctions, allParsedEdges, null, null);
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
        ParsedStaticMap parsedMap = parseMap(map);
        return parsedMap;
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
        SortedMap<Integer> map = new SortedMap<Integer>(null, 0);
        SortedMap<Integer> new_map = sorted_map_assoc(sorted_map_assoc(sorted_map_assoc(map, 30, 31), 20, 21), 10, 11);
        return sorted_map_get(sorted_map_assoc(new_map, 20, 33), 20, 88);
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
    public Integer ghcstate_read_val(GHCState state, Cons val_cons) {
      Integer val_tag = first(val_cons);
      Integer val = (Integer)second(val_cons);
      if (val_tag > 3) throw new RuntimeException("value tag is invalid");
      return val_tag == 0 ? sorted_map_get(state.regs, val, 0)
          : val_tag == 1 ? sorted_map_get(state.data, sorted_map_get(state.regs, val, 0), 0)
          : val_tag == 2 ? val
          : sorted_map_get(state.data, val, 0);
    }

    @Compiled
    public GHCState ghcstate_write_val(GHCState state, Cons arg_cons, Integer val) {
      Integer arg_tag = first(arg_cons);
      if (arg_tag > 3) throw new RuntimeException("arg tag is invalid");
      if (arg_tag == 2) throw new RuntimeException("arg can't be const");
      return 
            arg_tag == 0 ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, (Integer)second(arg_cons), val), state.data)
          : arg_tag == 1 ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, sorted_map_get(state.regs, (Integer)second(arg_cons), 0), val), state.data)
          : new GHCState(state.ghostState, state.regs, sorted_map_assoc(state.data, (Integer)second(arg_cons), val));
    }
    
    @Compiled
    public GHCState ghcstate_assoc(GHCState state, Cons arg_cons, Cons val_cons) {
      Integer val = ghcstate_read_val(state, val_cons);
      return ghcstate_write_val(state, arg_cons, val);
    }

    @Compiled
    public GHCState processGhostInfoRequest(WorldState world, GHCState state, Integer index, int requestType) {
      GhostState gs = (GhostState)list_item_def(world.ghosts, index, state.ghostState);
      Point startPos = new Point(0, 0); // todo: support
      return 
            4 == requestType ? new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, startPos.x), 1, startPos.y), state.data)
          : 5 == requestType ? new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, gs.location.x), 1, gs.location.y), state.data)
          : new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, gs.vitality), 1, gs.direction), state.data);
    }

    @Compiled
    public GHCState processGhostInt(WorldState world, GHCState state, Integer num, Cons arg) {
      GhostState gs = state.ghostState;
      Integer currentGhostIndex = 0; // todo: support
      return 
            0 == num ? new GHCState(new GhostState(gs.vitality, gs.location, sorted_map_get(state.regs, 0, 0)), state.regs, state.data) 
          : 1 == num ? new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, world.lambdaManState.location.x), 1, world.lambdaManState.location.y), state.data) 
          : 2 == num ? new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, world.lambdaManState.location.x), 1, world.lambdaManState.location.y), state.data) 
          : 3 == num ? new GHCState(gs, sorted_map_assoc(state.regs, 0, currentGhostIndex), state.data) 
          : 4 == num ? processGhostInfoRequest(world, state, sorted_map_get(state.regs, 0, 0), 4) 
          : 5 == num ? processGhostInfoRequest(world, state, sorted_map_get(state.regs, 0, 0), 5) 
          : 6 == num ? processGhostInfoRequest(world, state, sorted_map_get(state.regs, 0, 0), 6) 
          : 7 == num ? new GHCState(gs, sorted_map_assoc(state.regs, 0, getMapItem(world.map, sorted_map_get(state.regs, 0, 0), sorted_map_get(state.regs, 1, 0))), state.data) 
          : state; // 8 is unsupported
    }

    // produce_n f a n = (first (f a)) : iterate f (second (f a)) (n - 1)
    // produce_n f a 0 = (first (f a))
    @Compiled
    public <T> ListCons<T> produce_n(Function1<T,Cons> f, T a, int n) {
      T elem = first(f.apply(a));
      return n == 0 ? cons(elem, null) : cons(elem, produce_n(f, second(f.apply(a)), n - 1));
    }

    @Compiled
    public <T> ListCons<T> zip_with(Function2<T,T,T> f, ListCons<T> x, ListCons<T> y) {
      return 
            x == null ? null 
          : y == null ? null
          : cons (f.apply(head(x), head(y)), zip_with(f, tail(x), tail(y)));
    }

    @Compiled
    public ListCons<Integer> bit_split(Integer x) {
      return produce_n((Integer a) -> cons(a == ((a / 2) * 2) ? 0 : 1, a / 2), x, 7);
    }

    @Compiled
    public Integer emulate_bitop(Integer x, Integer y, Function2<Integer, Integer, Integer> f) {
      ListCons<Integer> bits_x = bit_split(x);
      ListCons<Integer> bits_y = bit_split(y);
      return foldr((Integer elem, Integer acc) -> acc * 2 + elem,
                   0,
                   zip_with(f, bits_x, bits_y));
    }

    @Compiled
    public GHCState ghcstate_bitop(GHCState state, Cons arg_cons, Cons val_cons, int type) {
      Integer arg = ghcstate_read_val(state, arg_cons);
      Integer val = ghcstate_read_val(state, val_cons);
      Integer result = emulate_bitop(arg, val, 
            type == GHCOps.AND ? ((x, y) -> x * y)
          : type == GHCOps.OR ? ((x, y) -> (x + y) > 0 ? 1 : 0)
          : ((x, y) -> x != y ? 1 : 0));
      return ghcstate_write_val(state, arg_cons, result);
    }
    
    @Compiled
    public Integer runGhostStep(SortedMap<Cons> prog, WorldState world, int lev, GHCState state, Cons step) {
      Integer opcode = head(step);
      ListCons<Cons> args = (ListCons<Cons>)tail(step);
      GHCState inc_pc = new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, sorted_map_get(state.regs, 8, 0) + 1), state.data);
      return
            GHCOps.MOV == opcode ? runGhost(prog, world, lev, ghcstate_assoc(inc_pc, first(args), head(second(args))))
          : GHCOps.INC == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) + 1))
          : GHCOps.DEC == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) - 1))
          : GHCOps.ADD == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) + ghcstate_read_val(inc_pc, head(second(args)))))
          : GHCOps.SUB == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) - ghcstate_read_val(inc_pc, head(second(args)))))
          : GHCOps.MUL == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) * ghcstate_read_val(inc_pc, head(second(args)))))
          : GHCOps.DIV == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) / ghcstate_read_val(inc_pc, head(second(args)))))
          : GHCOps.AND == opcode ? runGhost(prog, world, lev, ghcstate_bitop(inc_pc, first(args), head(second(args)), GHCOps.AND))
          // : GHCOps.OR == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) | ghcstate_read_val(inc_pc, head(second(args)))))
          // : GHCOps.XOR == opcode ? runGhost(prog, world, lev, ghcstate_write_val(inc_pc, first(args), ghcstate_read_val(inc_pc, first(args)) ^ ghcstate_read_val(inc_pc, head(second(args)))))
          : GHCOps.JLT == opcode ? runGhost(prog, world, lev,
                  ghcstate_read_val(state, first(tail(args))) < ghcstate_read_val(state, second(tail(args)))
                      ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, (Integer)first(args)), state.data)
                      : state)
          : GHCOps.JEQ == opcode ? runGhost(prog, world, lev,
                  ghcstate_read_val(state, first(tail(args))) == ghcstate_read_val(state, second(tail(args)))
                    ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, (Integer)first(args)), state.data)
                    : state)
          : GHCOps.JGT == opcode ? runGhost(prog, world, lev,
                  ghcstate_read_val(state, first(tail(args))) > ghcstate_read_val(state, second(tail(args))) 
                      ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, (Integer)first(args)), state.data)
                      : state)
          : GHCOps.INT == opcode ? runGhost(prog, world, lev, processGhostInt(world, inc_pc, (Integer)first(args), tail(args)))
          : state.ghostState.direction;
    }
    
    @Compiled
    public Integer runGhost(SortedMap<Cons> prog, WorldState world, int lev, GHCState state) {
      Integer pc = sorted_map_get(state.regs, 8, 0);
      Cons step = sorted_map_get(prog, pc, null);
      return step == null ? state.ghostState.direction 
                          : (lev > 1023 ? state.ghostState.direction : runGhostStep(prog, world, lev + 1, state, step));
    }
    
    @Compiled
    public Integer getGhostDirection(WorldState world, ListCons<Cons> spec) {
        Tuple<Integer, SortedMap<Cons>> prog = 
            fold0(spec,
                  new Tuple<>(0, new SortedMap<Cons>(null, 0)), 
                  (Tuple<Integer, SortedMap<Cons>> init, Cons step) -> new Tuple<>(init.a + 1, sorted_map_assoc(init.b, init.a, step)));
        GhostState ghostState = (GhostState)head(world.ghosts);  // get actual direction and location
        return runGhost(prog.b, 
                        world, 
                        0,
                        new GHCState(ghostState, new SortedMap<Integer>(null, 0), new SortedMap<Integer>(null, 0)));
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
        if (true) {
          System.out.println(new Sample1().bit_split(10) + ":" + new Sample1().bit_split(127));
          System.out.println(new Sample1().emulate_bitop(13, 5, (xx, yy) -> xx * yy));
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

          System.out.println("direction:" + new Sample1().getGhostDirection(worldState, first(spec)));
          System.exit(0);
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

    private static void printMap(String theMap) {
        String[] rows = theMap.split("\n");
        int width = rows[0].length();
        System.out.print("      ");
        for(int x = 0; x<width; x++) {
            if ((x % 2 ) == 0) {
                System.out.print(String.format("%02d", x));
            } else {
                System.out.print("  ");
            }
        }
        System.out.println();
        System.out.print("      ");
        for(int x = 0; x<width; x++) {
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
