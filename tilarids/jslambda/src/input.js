function foo(size) {
  while(size) {
    dbug(size)
    size--
  }
  return 1233;
} 
function init() {
  return foo(5)
}

