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

main :: IO ()
main = displayArgs =<< execParser opts
  where
    opts =
      info
        (optionParser <**> helper)
        ( fullDesc
            <> progDesc "Print a little greeting"
            <> header "Hiya! We are testing iasm!"
        )

-- credits :: IO ()
-- credits = putStrln "" >> putStrLn " ~~ iAtomSys Assembler ~~ " >> putStrLn ""

displayArgs :: Options -> IO ()
displayArgs (Options f c l s v) = do
  putStrLn $ "file: " ++ f
  putStrLn $ "compile: " ++ show c
  putStrLn $ "link: " ++ show l
  putStrLn $ "silent: " ++ show s
  putStrLn $ "verbose: " ++ show v
