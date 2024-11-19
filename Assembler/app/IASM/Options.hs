module IASM.Options where

import Options.Applicative

{- Whether the assembler should just assemble or link the inputs, or both! -}
data Mode = ASSEMBLE_ONLY | LINK_ONLY | BOTH deriving (Eq)

instance Show Mode where
  show ASSEMBLE_ONLY = "assemble_only"
  show LINK_ONLY = "link_only"
  show BOTH = "assemble_and_link"

{- How many logging messages the assembler should produce.
 - A silent assembler will produce no output at all. -}
data Verbosity = SILENT | NORMAL | VERBOSE deriving (Eq)

instance Show Verbosity where
  show SILENT = "silent"
  show NORMAL = "normal"
  show VERBOSE = "verbose"

data Options = Options
  { file :: String,
    batches :: Int,
    mode :: Mode,
    verbosity :: Verbosity
  }

{- The Options.Applicative Parser for the application's command-line arguments -}
verbosityVerbose :: Options -> Bool
verbosityVerbose = (VERBOSE ==) . verbosity

verbositySilent :: Options -> Bool
verbositySilent = (NORMAL ==) . verbosity

modeAssemble :: Options -> Bool
modeAssemble = (ASSEMBLE_ONLY ==) . mode

modeLink :: Options -> Bool
modeLink = (LINK_ONLY ==) . mode

optionParser :: Parser Options
optionParser =
  Options
    <$> strOption
      ( long "file"
          <> short 'f'
          <> help "The input file or directory for the program."
      )
    <*> option
      auto
      ( long "batches"
          <> short 'b'
          <> help "The desired number of batches (threads) to run in parallel"
          <> showDefault
          <> value 1
      )
    <*> ( flag' ASSEMBLE_ONLY (short 'a' <> long "assemble")
            <|> flag' LINK_ONLY (short 'l' <> long "link")
            <|> pure BOTH
        )
    <*> ( flag' SILENT (short 's' <> long "silent")
            <|> flag' VERBOSE (short 'v' <> long "verbose")
            <|> pure NORMAL
        )

{- Parse the command-line options given to the application, as defined in optionParser above. -}
parseProgramOptions :: IO Options
parseProgramOptions = execParser opts
  where
    opts =
      info
        (optionParser <**> helper)
        ( fullDesc
            <> progDesc "Print a little greeting"
            <> header "Hiya! We are testing iasm!"
        )
