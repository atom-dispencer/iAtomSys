module testing


global State:
  Int16 a = 0
  Int16 b = 0
  Int16 c = 0


type DivByZeroError(Int16 num, Int16 den)


// Every function has exactly one non-null return value.
//  If they don't have a return value, they either aren't achieving anything or they are doing something
//  dangerous which isn't being inspected.


// Note that the type signatures are very expressive! (like Haskell!)
// It tells you the parameters, consumed global state, intermediary stack variables, and all possible return types!
// A function can return one of multiple values (including errors) and all of the possibilities must be handled. 
export divide: Int16 numerator, Int16 denominator -> _ -> Int16, DivByZeroError
  numerator / denominator               // This is effectively an inline function call, so all results must be handled explictly
    | Int16 -> return $
    | DivByZeroError -> return $
  // In this case, any result is just returned
  // The result of the last statement in a function will be returned
  //
  // Note that any errors that may be produced by a line must be *immediately* handled in a | block.


fancyMaths: Int16 num1, Int16 num2, Flags flags -> Int16 x -> Int16, DivByZeroError
  num1 / num2
    | Int16 -> x = $
    | DivideByZeroError -> return $     // The * operator (infix function) cannot receive a DivByZeroError,
                                        //    so the function cannot continue and we must return.
  if flags[0]                           // The flags[0] operation cannot fail if the index is an integer in range.
    return x * 2                        // The * operator cannot fail if both arguments are integers/numbers/valid types.
  else:
    divide(x, num2)
      | Int16 -> return $
      | DivByZeroError -> return x

    // In pseudocode, the previous statement would be roughly:
    // var temp = divide(x, num2)
    // switch typeof(temp) {
    //    case Int16: return temp
    //    case DivByZeroError: return x
    // }


// I've heard it said that global state is effectively an invisible parameter on every functions...
// So why not make it visible instead? It doesn't need a new name, but I think it's a cool idea?
export mathsWithState: State -> Int16 n -> Int16
  n = State.a + State.b     // The + operator cannot fail with two Int16s, so we can set n directly
    
  if n == 0                 // Should this condition short-circuit checks for DivByZeroErrors?
    return State.c * n      // No failure possible
  else:
    State.c / divide(n / 0, 0)          // There are 3 places for DivByZeroError to occur in this line.
      | Int16 -> return $               // Errors on one line are collected into a single error collection statement
      | DivByZeroError -> return x

// A branchless equivalent
export bl_mathsWithState: State -> Int16 n -> Int16
  n = State.a + State.b
  return { 
    n == 0 -> n * State.c 
  } { 
    else -> State.c / divide(n / 0, 0)
    // Here, 'else' is equivalent to 'n != 0', and must be the inverse of all of the conditions before.
      | Int16 -> return $
      | DivByZeroError -> return x
  }
