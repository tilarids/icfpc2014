import Data.Aeson
import qualified Data.ByteString.Lazy as BL
import Data.List.Split
import Data.List
import Data.Maybe
import qualified Data.Map as M

xdecode :: BL.ByteString -> Maybe [[Int]]
xdecode x = decode x

main = do
	x <- BL.readFile "bkg-2-decimated.json"
	let bitLL = fromJust $ xdecode x
	let m = concat $ map (chunksOf 11) $ bitLL
	print m

