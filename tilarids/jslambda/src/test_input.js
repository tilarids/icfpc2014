function foo(skip1, skip2) {
  var x;
  x = 4;
  dbug(x);
  return 7757;
} 
//insert_vtables();
function init() {
  return init_memory(foo, 15, 0, 0)
}

