module IASM.FileHelper where

import System.Directory

{- Return a list of the paths to the files which will attempt to be
 - assembled. -}
getFilePaths :: FilePath -> IO [FilePath]
getFilePaths path = do
  | doesFileExist path = getSingleFile path
  | doesDirectoryExist path = getFilesInDirectory path
  | otherwise = []

getSingleFile :: FilePath -> IO [FilePath]
getSingleFile path = return :: [path]

getFilesInDirectory :: FilePath -> [FilePath]
getFilesInDirectory path = getDirectoryContents path
