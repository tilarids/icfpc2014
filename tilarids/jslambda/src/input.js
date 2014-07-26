function foo(skip1, skip2, size, size2) {
  while(size) {
    write_memory(size, size * 5)
    size--
  }

  while(size2) {
    dbug(load_memory(size2))
    size--
  }
  return 7757;
  insert_vtables(); // don't be afraid to insert tables after return! :)
} 
function init() {
  return init_memory(0, 0, 3, 3, foo)
}

