

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
