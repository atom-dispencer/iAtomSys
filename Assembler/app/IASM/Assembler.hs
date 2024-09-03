module IASM.Assembler where

import Data.List (unfoldr)

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
