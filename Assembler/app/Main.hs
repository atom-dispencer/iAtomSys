module Main where

import Options.Applicative

data Options = Options
  { file :: String,
    compile :: Bool,
    link :: Bool,
    silent :: Bool,
    verbose :: Bool
  }

optionParser :: Parser Options
optionParser =
  Options
    <$> strOption (long "file" <> short 'f' <> help "The input file or directory for the program")
    <*> switch (long "compile" <> short 'c' <> help "Compile the given file(s)")
    <*> switch (long "link" <> short 'l' <> help "Link the given file(s)")
    <*> switch (long "silent" <> short 's' <> help "Suppress all output messages")
    <*> switch (long "verbose" <> short 'v' <> help "Produce *additional* logging messages")

{- The main function for the assembler and the application's entrypoint. -}
main :: IO ()
main =
  credits >> parseProgramOptions >>= \opts ->
    displayArgs opts >> start opts

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

{- Display the programs arguments. -}
displayArgs :: Options -> IO ()
displayArgs (Options f c l s v) = do
  putStrLn $ "file: " ++ f
  putStrLn $ "compile: " ++ show c
  putStrLn $ "link: " ++ show l
  putStrLn $ "silent: " ++ show s
  putStrLn $ "verbose: " ++ show v

{- Start assembling! -}
start :: Options -> IO ()
start (Options a b c d e) = putStrLn "Doing things..."
