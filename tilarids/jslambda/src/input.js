/*function foldl(func, acc, list) {
  if (atom(cdr(list))) { 
    return acc 
  } else {
    return foldl(func, func(acc, car(list)), cdr(list))
  } 
}*/
/*function mod(x, y) {
  if (x > y) {
    return mod(x - y, y)
  } else {
    return x
  }
}

function step(state, world, lm_status, ghost_status, fruit_status) {
  return [0, state + 1]; 
}/*
function queue_new() {
  return [0, 0];
}
function queue_enqueue(q, v) {
  return [car(q), v, cdr(q)];
}
function queue_isempty(q) {
  if (atom(car(q)) * atom(cdr(q))) {
    if (car(q) + cdr(q)) {
      return 0;
    } else {
      return 1;
    }
  } else {
    return 0;
  }
}*/
/*function reverse(c) {
  if (atom(c)) {
    return c;
  } else {
    return [reverse(cdr(c)), car(c)];
  }
}

function queue_dequeue(q) {
  if (atom(car(q)) * (0 == car(q))) {
    if (atom(cdr(q)) * (0 == cdr(q))) {
      return [0, 0];
    } else {
      return queue_dequeue([reverse(cdr(q), 0)]);
    }
  } else {
    return [car(car(q)), cdr(car(q)), cdr(q)];
  }
}
*/
//function id(x, y) { return x; } 
/*function step_impl(state, tuple4) { 
  return step(state, car(tuple4), car(cdr(tuple4)), car(cdr(cdr(tuple4))), cdr(cdr(cdr(tuple4))))
}*/
function imperative_step(state, x11, x12, x21, x22) {
  return [0, [x11, x12, x21, x22]];
}
function new_step(state, world) {
  return imperative_step(state, car(car(world)), car(cdr(car(world))), car(cdr(world)), car(cdr(cdr(world))))
} 
function new_step_impl(state, tuple4) {
  return new_step(state, car(tuple4))
}
function init() {
//  return 42;
//  return mod(154,3)
  return [0, new_step_impl]
}
