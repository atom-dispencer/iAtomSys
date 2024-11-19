import:
  if
  do
  integer
  boolean
  console
  math
  array

primeSieve: Int16 max -> _ -> Ok, OutOfBounds

  // Any function calling this function must prove that this is true
  require:
    max > 0

  max / 2
  ~ DivideByZeroError
  | Float16
    math.floor($)
    | Int16
      highest_tester = $
      array.new(Boolean, max + 1, True)
      ~ NegativeLength
      | Array
        sieve = $

        // Find the primes
        while n < highest_tester {
          while x <= array.length(sieve) - 1 { 
            x = i * n
            array.set(sieve, x, False)
            | OutOfBounds 
              console.printerr($)
            | Ok
              do.nothing()
          }
          
          n = n + 1
        }

        // Print the primes
        m = 0
        {
          x = sieve[m]
          if.equals(x, True)
          | True
            console.println("Found prime: " + m)
            | IOError
              do.nothing()
            console.maybeprint("Found prime: " + m)
            // Functions that only return Ok don't have to be handled.
            // This is important for terminating the tree!!
          | False

          m++;
          @escape if m >= array.length(sieve)
        }

export main: _ -> _ -> Ok, IOError
  get_user_int()
  | Int16
    input = $
    primeSieve(input)
  | IOError
    return $







primeSieve: Int16 max -> _ -> Ok

  // Any function calling this function must prove that this is true
  require:
    max > 0

  max / 2
  | Float16 -> math.floor($)
    | Int16 -> highest_tester = $
  // | DivideByZeroError -> return $
  // Doesn't need to be handled because 2 != 0

  array.new(max + 1)
  | Array -> sieve = $
  // | NegativeLength -> return $
  // Also doesn't need handling

  n = 2
  {
    {
      x = i * n
      array.set(sieve, x, False)
      | OutOfBounds // How to handle this?
      | Ok
      
      @head x <= len(sieve) - 1
      @here else
    }
    
    n = n + 1
    // jump   (::head * n<highest_tester) + (::here * n>=highest_tester)
    @head n < highest_tester
    @here else
  }

export main: _ -> _ -> _
  {
    get_user_int()
  }
