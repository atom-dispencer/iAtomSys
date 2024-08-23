defmodule IAtomSysAssembler.CLI do
  def main(args) do
    IO.puts("")
    IO.puts(" ~~ iAtomSys Assembler (Elixir) ~~")
    IO.puts("")
    {opts, _, _} = OptionParser.parse(args, switches: [file: :string], aliases: [f: :file])
    IO.inspect(opts)

    # Read input directories/files
    # Create linkable object for each input file
    # Link objects
  end
end
