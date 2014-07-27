package app;

import static app.VM.*;
import static app.Sample1.*;
import static app.SortedMap.*;

/**
 * Created by lenovo on 27.07.2014.
 */
public class LambdaGhostEmulator {

    @Compiled
    public static Integer ghcstate_read_val(GHCState state, Cons val_cons) {
        Integer val_tag = first(val_cons);
        Integer val = (Integer) second(val_cons);
        if (val_tag > 3) throw new RuntimeException("value tag is invalid");
        return val_tag == 0 ? sorted_map_get(state.regs, val, 0)
                : val_tag == 1 ? sorted_map_get(state.data, sorted_map_get(state.regs, val, 0), 0)
                : val_tag == 2 ? val
                : sorted_map_get(state.data, val, 0);
    }

    @Compiled
    public static GHCState ghcstate_write_val(GHCState state, Cons arg_cons, Integer val) {
        Integer arg_tag = first(arg_cons);
        if (arg_tag > 3) throw new RuntimeException("arg tag is invalid");
        if (arg_tag == 2) throw new RuntimeException("arg can't be const");
        return
                arg_tag == 0 ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, (Integer) second(arg_cons), val), state.data)
                        : arg_tag == 1 ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, sorted_map_get(state.regs, (Integer) second(arg_cons), 0), val), state.data)
                        : new GHCState(state.ghostState, state.regs, sorted_map_assoc(state.data, (Integer) second(arg_cons), val));
    }

    @Compiled
    public static GHCState ghcstate_assoc(GHCState state, Cons arg_cons, Cons val_cons) {
        Integer val = ghcstate_read_val(state, val_cons);
        return ghcstate_write_val(state, arg_cons, val);
    }

    @Compiled
    public static GHCState processGhostInfoRequest(WorldState world, GHCState state, Integer index, int requestType) {
        GhostState gs = (GhostState) list_item_def(world.ghosts, index, state.ghostState);
        Point startPos = new Point(0, 0); // todo: support
        return
                4 == requestType ? new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, startPos.x), 1, startPos.y), state.data)
                        : 5 == requestType ? new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, gs.location.x), 1, gs.location.y), state.data)
                        : new GHCState(gs, sorted_map_assoc(sorted_map_assoc(state.regs, 0, gs.vitality), 1, gs.direction), state.data);
    }

    @Compiled
    public static GHCState processGhostInt(WorldState world, GHCState state, Integer num, Cons arg) {
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
    public static<T> ListCons<T> produce_n(Function1<T, Cons> f, T a, int n) {
        T elem = first(f.apply(a));
        return n == 0 ? cons(elem, null) : cons(elem, produce_n(f, second(f.apply(a)), n - 1));
    }

    @Compiled
    public static ListCons<Integer> bit_split(Integer x) {
        return produce_n((Integer a) -> cons(a == ((a / 2) * 2) ? 0 : 1, a / 2), x, 7);
    }

    @Compiled
    public static Integer emulate_bitop(Integer x, Integer y, Function2<Integer, Integer, Integer> f) {
        ListCons<Integer> bits_x = bit_split(x);
        ListCons<Integer> bits_y = bit_split(y);
        return foldr((Integer elem, Integer acc) -> acc * 2 + elem,
                0,
                zip_with(f, bits_x, bits_y));
    }

    @Compiled
    public static GHCState ghcstate_bitop(GHCState state, Cons arg_cons, Cons val_cons, int type) {
        Integer arg = ghcstate_read_val(state, arg_cons);
        Integer val = ghcstate_read_val(state, val_cons);
        Integer result = emulate_bitop(arg, val,
                type == GHCOps.AND ? ((x, y) -> x * y)
                        : type == GHCOps.OR ? ((x, y) -> (x + y) > 0 ? 1 : 0)
                        : ((x, y) -> x != y ? 1 : 0));
        return ghcstate_write_val(state, arg_cons, result);
    }

    @Compiled
    public static Integer runGhostStep(SortedMap<Cons> prog, WorldState world, int lev, GHCState state, Cons step) {
        Integer opcode = head(step);
        ListCons<Cons> args = (ListCons<Cons>) tail(step);
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
                        : GHCOps.OR == opcode ? runGhost(prog, world, lev, ghcstate_bitop(inc_pc, first(args), head(second(args)), GHCOps.OR))
                        : GHCOps.XOR == opcode ? runGhost(prog, world, lev, ghcstate_bitop(inc_pc, first(args), head(second(args)), GHCOps.XOR))
                        : GHCOps.JLT == opcode ? runGhost(prog, world, lev,
                        ghcstate_read_val(state, first(tail(args))) < ghcstate_read_val(state, second(tail(args)))
                                ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, (Integer) first(args)), state.data)
                                : state)
                        : GHCOps.JEQ == opcode ? runGhost(prog, world, lev,
                        ghcstate_read_val(state, first(tail(args))) == ghcstate_read_val(state, second(tail(args)))
                                ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, (Integer) first(args)), state.data)
                                : state)
                        : GHCOps.JGT == opcode ? runGhost(prog, world, lev,
                        ghcstate_read_val(state, first(tail(args))) > ghcstate_read_val(state, second(tail(args)))
                                ? new GHCState(state.ghostState, sorted_map_assoc(state.regs, 8, (Integer) first(args)), state.data)
                                : state)
                        : GHCOps.INT == opcode ? runGhost(prog, world, lev, processGhostInt(world, inc_pc, (Integer) first(args), tail(args)))
                        : state.ghostState.direction;
    }

    @Compiled
    public static Integer runGhost(SortedMap<Cons> prog, WorldState world, int lev, GHCState state) {
        Integer pc = sorted_map_get(state.regs, 8, 0);
        Cons step = sorted_map_get(prog, pc, null);
        return step == null ? state.ghostState.direction
                : (lev > 1023 ? state.ghostState.direction : runGhostStep(prog, world, lev + 1, state, step));
    }

    @Compiled
    public static Integer getGhostDirection(WorldState world, ListCons<Cons> spec) {
        VM.Tuple<Integer, SortedMap<Cons>> prog =
                fold0(spec,
                        new VM.Tuple<>(0, new SortedMap<Cons>(null, 0)),
                        (VM.Tuple<Integer, SortedMap<Cons>> init, Cons step) -> new VM.Tuple<>(init.a + 1, sorted_map_assoc(init.b, init.a, step)));
        GhostState ghostState = (GhostState) head(world.ghosts);
        return runGhost(prog.b,
                world,
                0,
                new GHCState(ghostState, new SortedMap<Integer>(null, 0), new SortedMap<Integer>(null, 0)));
    }
}
