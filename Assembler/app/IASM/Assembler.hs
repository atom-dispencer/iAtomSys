module IASM.Assembler where

assembleFiles :: [FilePath] -> IO ()
assembleFiles files = do
  putStrLn $ "Assembling " ++ show (length files) ++ " files..."
