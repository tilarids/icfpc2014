function new_step(state, world, size) {
  while(size) {
    dbug(load_memory(size))
    size--
  }
  insert_vtable();
} 
function new_step_impl(state, tuple4) {
  return init_memory(state, car(tuple4), 5, new_step)
}
function init() {
  return [0, new_step_impl]
}

