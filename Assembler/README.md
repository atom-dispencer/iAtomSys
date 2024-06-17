# iAtomSys Assembler
| Language | Tools | Dependencies |
| -------- | ----- | ------------ |
| Elixir   |       |              |

## Why Elixir?
Because I'm curious about the functional paradigm.
I've already made an assembler before (see my C# implemention in [**Crimson-RFASM**](https://github.com/atom-dispencer/Crimson-RFASM)), so I can focus on learning the language without having to solve so many other problems at the same time.
Compiliation and assembly are (unlike linking) highly parallelisable because files are broadly independent of one-another - a perfect task for a highly concurrent language!


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

