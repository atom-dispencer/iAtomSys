# iAtomSys Assembler
| Language | Tools | Dependencies |
| -------- | ----- | ------------ |
| Elixir   |       |              |

## Why Elixir?
Is Elixir the 'best' language for the job?
Nope!
But I'm curious about functional programming, soooo...

I've made an assembler before (see my C# implemention in [**Crimson-RFASM**](https://github.com/atom-dispencer/Crimson-RFASM)), so I can focus on learning the language without having to solve so many other problems at the same time.

And who knows!
Elixir is great at handling lots of lightweight parallel tasks, perhaps like assembling many lines of code??
I'm sure it'll be fine... ;)


### Building and Running
This project uses **mix** and **escript** to produce a binary which can be run on a machine with Erlang/OTP installed:

```
mix escript.build
escript iatomsys_assembler
```


## Installation

If [available in Hex](https://hex.pm/docs/publish), the package can be installed
by adding `iatomsys_assembler` to your list of dependencies in `mix.exs`:

```elixir
def deps do
  [
    {:iatomsys_assembler, "~> 0.1.0"}
  ]
end
```

Documentation can be generated with [ExDoc](https://github.com/elixir-lang/ex_doc)
and published on [HexDocs](https://hexdocs.pm). Once published, the docs can
be found at <https://hexdocs.pm/iatomsys_assembler>.

