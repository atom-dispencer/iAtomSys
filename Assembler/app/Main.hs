module Main where

import Options.Applicative

data Mode = ASSEMBLE_ONLY | LINK_ONLY | BOTH deriving (Eq)

instance Show Mode where
  show ASSEMBLE_ONLY = "assemble_only"
  show LINK_ONLY = "link_only"
  show BOTH = "assemble_and_link"

data Verbosity = SILENT | NORMAL | VERBOSE deriving (Eq)

instance Show Verbosity where
  show SILENT = "silent"
  show NORMAL = "normal"
  show VERBOSE = "verbose"

data Options = Options
  { file :: String,
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
    <*> ( flag' ASSEMBLE_ONLY (short 'a' <> long "assemble")
            <|> flag' LINK_ONLY (short 'l' <> long "link")
            <|> pure BOTH
        )
    <*> ( flag' SILENT (short 's' <> long "silent")
            <|> flag' VERBOSE (short 'v' <> long "verbose")
            <|> pure NORMAL
        )

{- The main function for the assembler and the application's entrypoint. -}
main :: IO ()
main =
  credits >> parseProgramOptions >>= \opts -> displayArgs opts >> start opts

{- Display credits, links and licensing information for the application. -}
credits :: IO ()
credits = do
  putStrLn ""
  putStrLn " ~~ iAtomSys Assembler (iasm)                   ~~ "
  putStrLn " ~~ https://github.com/atom-dispencer/iAtomSys/ ~~ "
  putStrLn " ~~ Copyright (c) 2024 - Adam Spencer           ~~ "
  putStrLn ""

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

{- Display the programs arguments (or not, if we're silent ;) ) -}
displayArgs :: Options -> IO ()
displayArgs (Options _ _ SILENT) = return ()
displayArgs (Options f m v) = do
  putStrLn "Parsed arguments: "
  putStrLn $ "file      : " ++ f
  putStrLn $ "mode      : " ++ show m
  putStrLn $ "verbosity : " ++ show v
  putStrLn ""

{- Start assembling! -}
start :: Options -> IO ()
start (Options _ _ _) = putStrLn "Doing things..."
