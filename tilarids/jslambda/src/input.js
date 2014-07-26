function step(state, world, n, m) {
  return [state, state+1];
} 
function length(l) {
  if (atom(car(l))) {
    return 2;
  } else {
    return 1 + length(car(l));
  }
}
function step_impl(state, tuple4) {
  return init_memory(step, 10, state, car(tuple4), length(car(tuple4)) - 1, length(car(car(tuple4))) - 1)
}
//insert_vtables(); 
function init() {
  return [0, step_impl]

}

