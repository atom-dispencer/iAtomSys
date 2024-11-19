module Main where

import Control.Monad (filterM, when)
import IASM.Assembler
import IASM.Options
import System.Directory
import System.FilePath

{- The main function for the assembler and the application's entrypoint. -}
main :: IO ()
main = do
  opts <- parseProgramOptions
  credits opts
  assemble opts
  link opts

{- Display credits, links and licensing information for the application. -}
credits :: Options -> IO ()
credits (Options _ _ _ SILENT) = return ()
credits (Options f b m v) = do
  putStrLn ""
  putStrLn " ~~ iAtomSys Assembler (iasm)                   ~~ "
  putStrLn " ~~ https://github.com/atom-dispencer/iAtomSys/ ~~ "
  putStrLn " ~~ Copyright (c) 2024 - Adam Spencer           ~~ "
  putStrLn ""
  putStrLn "Parsed arguments: "
  putStrLn $ "file      : " ++ f
  putStrLn $ "batches   : " ++ show b
  putStrLn $ "mode      : " ++ show m
  putStrLn $ "verbosity : " ++ show v
  putStrLn ""

{- Get a [FilePath] of the files in the given directory, if the given
 - path is a directory, or otherwise the file provided, if it exists.
 - If no such file or directory exists, return an empty list. -}
getFiles :: String -> IO [FilePath]
getFiles path = do
  fileExists <- doesFileExist path
  dirExists <- doesDirectoryExist path
  if fileExists
    then
      return [path]
    else
      if dirExists
        then do
          listDirectory path
        else
          return []

hasFileExtension :: String -> FilePath -> IO Bool
hasFileExtension _ [] = return False
hasFileExtension extension f = do
  dirExists <- doesDirectoryExist f
  return $ takeExtension f == extension && not dirExists

{- Start assembling! -}
assemble :: Options -> IO ()
assemble opts = do
  when
    (verbosity opts /= SILENT)
    (putStrLn $ "Assembling " ++ file opts ++ "...")
  files <- getFiles (file opts)

  filteredFiles <- filterM (hasFileExtension ".iasm") files
  when
    (verbosity opts /= SILENT)
    (putStrLn $ "Filtered " ++ show (length files) ++ " mixed files to " ++ show (length filteredFiles) ++ " .iasm files")

  if null filteredFiles
    then
      putStrLn $ "Cannot continue - no such file or directory: " ++ file opts
    else
      assembleFiles files (batches opts)

link :: Options -> IO ()
link opts = putStrLn "Linking! (just kidding, not really!)"
