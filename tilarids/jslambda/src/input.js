function foldl(func, acc, list) {
  if (atom(cdr(list))) { 
    return acc 
  } else {
    return foldl(func, func(acc, car(list)), cdr(list))
  } 
} 
function step(state, world, lm_status, ghost_status, fruit_status) {
  return [0, state]; 
}
function id(x, y) { return x; } 
function step_impl(state, tuple4) { 
  return step(state, car(tuple4), car(cdr(tuple4)), car(cdr(cdr(tuple4))), cdr(cdr(cdr(tuple4))))
}
function init() {
    return [3, step_impl]
}
