function foo(skip1, skip2, size) {
  while(size) {
    dbug(load_memory(size))
    brk()
    size--
  }
  return 7756;
  insert_vtable(); // don't be afraid to insert tables after return! :)
} 
function init() {
  return init_memory(0, 0, 3, foo)
}

