/*function foo(skip1, skip2) {
  var x;
  x = 4;
  dbug(x);
  return 7757;
} */
function length(l) {
  dbug(l);
  if (atom(cdr(l))) {
    return 2;
  } else {
    return length(cdr(l)) + 1;
  }
}

// insert_vtables();
function init() {
  return length([1,2,3,4,5,0]);
  // return init_memory(foo, 15, 0, 0)
}

