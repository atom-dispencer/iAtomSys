module IASM.FileHelper where

import System.Directory

{- Return a list of the paths to the files which will attempt to be
 - assembled. -}
getFilePaths :: FilePath -> IO [FilePath]
getFilePaths path =
  do
      fileExists <- doesFileExist path
      directoryExists <- doesDirectoryExist path
    if fileExists && directoryExists
      then []
      else if fileExists then getSingleFile path else if directoryExists then getFilesInDirectory path else []

getSingleFile :: FilePath -> [FilePath]
getSingleFile path = [path]

getFilesInDirectory :: FilePath -> [FilePath]
getFilesInDirectory path = getDirectoryContents path
