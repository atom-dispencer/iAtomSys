module Main where

import IASM.Options

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
