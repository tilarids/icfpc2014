import Data.Aeson
import qualified Data.ByteString.Lazy as BL
import Data.List.Split
import Data.List
import Data.Maybe
import qualified Data.Map as M

xdecode :: BL.ByteString -> Maybe [[Int]]
xdecode x = decode x

sausage size = show . concatMap (chunksOf size) 


main = do
	x <- BL.readFile "bkg-2-decimated.json"
	let bitLL = fromJust $ xdecode x

	writeFile "sausage5.json" $ sausage 5 bitLL
	writeFile "sausage5-transposed.json" $ sausage 5 $ transpose bitLL


