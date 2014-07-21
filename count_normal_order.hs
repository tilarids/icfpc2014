import Data.Aeson
import qualified Data.ByteString.Lazy as BL
import Data.List.Split
import Data.Maybe
import qualified Data.Map as M

xdecode :: BL.ByteString -> Maybe [[Int]]
xdecode x = decode x

main = do
	x <- BL.readFile "bkg-2-decimated.json"
	print $ count $ concat $ map (chunksOf 5) $ fromJust $ xdecode x

count x = foldr f M.empty x where
	f elem = M.insertWith (+) elem 1