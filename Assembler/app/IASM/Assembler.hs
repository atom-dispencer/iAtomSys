module IASM.Assembler where

import Data.List (unfoldr)

type Token = String

type Statement = String

assembleFiles :: [FilePath] -> Int -> IO ()
assembleFiles files batchCount = do
  putStrLn $ "Assembling " ++ show (length files) ++ " files..."
  putStrLn $ "Created " ++ show (length fileBatches) ++ " batches of up to " ++ show batchSize ++ " files"
  mapM_ (mapM_ print) fileBatches
  where
    -- TODO May need to do ceiling division here?
    batchSize = length files `div` batchCount
    fileBatches = splitListEvenly batchSize files

splitListEvenly :: Int -> [a] -> [[a]]
splitListEvenly chunkSize = unfoldr split
  where
    split [] = Nothing
    split xs = Just (splitAt chunkSize xs)

assembleSingle :: FilePath -> IO ()
assembleSingle path = do
  content <- readFile path
  let bytes = compile . preprocess . parse . tokenise . lines $ content
  writeToObject bytes

{- Convert a list of lines to a list of Tokens representing discrete atoms of information
 - within the compilation target. -}
tokenise :: [String] -> [Token]
tokenise fileLines = fileLines

{- Group the given list of Tokens into coherent Statements, for example 'MOV IDK TBH*' -}
parse :: [Token] -> [Statement]
parse tokens = tokens

{- Perform preprocessing steps on the given list of statements, such as applying macros.
 - Macros are a text-replacement mechanism (like in C/C++) and are therefore not shared
 - between source files, and hence can be consumed without linking. -}
preprocess :: [Statement] -> [Statement]
preprocess statements = statements

{- Convert the given statements into bytes (or rather int16's). The informatio to do this
 - may be incomplete, so space may be reserved for linking. -}
compile :: [Statement] -> String
compile statements = "nothing here yet..."

{- Write a compiled (but not linked) iASM object (.oasm) back to the disk. -}
writeToObject :: String -> IO ()
writeToObject bytes = writeFile "output.txt" bytes
