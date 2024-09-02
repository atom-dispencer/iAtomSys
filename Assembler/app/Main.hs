module Main where

import Control.Monad (when)
import IASM.Assembler
import IASM.Linker
import IASM.Options
import IASM.Options (parseProgramOptions)

{- The main function for the assembler and the application's entrypoint. -}
main :: IO ()
main = do
  opts <- parseProgramOptions
  credits opts
  assemble opts
  link opts

{- Display credits, links and licensing information for the application. -}
credits :: Options -> IO ()
credits (Options _ _ SILENT) = return ()
credits (Options f m v) = do
  putStrLn ""
  putStrLn " ~~ iAtomSys Assembler (iasm)                   ~~ "
  putStrLn " ~~ https://github.com/atom-dispencer/iAtomSys/ ~~ "
  putStrLn " ~~ Copyright (c) 2024 - Adam Spencer           ~~ "
  putStrLn ""
  putStrLn "Parsed arguments: "
  putStrLn $ "file      : " ++ f
  putStrLn $ "mode      : " ++ show m
  putStrLn $ "verbosity : " ++ show v
  putStrLn ""

getFiles :: String -> IO [String]
getFiles path = return ["", ""]

{- Start assembling! -}
assemble :: Options -> IO ()
assemble opts = do
  when
    (verbosity opts /= SILENT)
    (putStrLn $ "Assembling " ++ file opts ++ "...")
  files <- getFiles (file opts)
  when
    (verbosity opts == VERBOSE)
    (putStrLn $ "Found " ++ show (length files) ++ " files")
  assembleFiles files

link :: Options -> IO ()
link opts = putStrLn "Linking! (just kidding, not really!)"
