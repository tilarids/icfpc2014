use warnings;
use strict;
use JSON;
use Data::Dumper;

use GD;
use IO::File;

my $f = new IO::File $ARGV[0];
my $j = <$f>;
my @rows = @{decode_json(qq({"foo":$j}))->{foo}};

my $width = scalar(@rows);
my $height = scalar(@{$rows[0]});
print "Height = $height\n";

my $png = new GD::Image($width, $height);

my $white = $png->colorAllocate(255,255,255);
my $black = $png->colorAllocate(0,0,0);

for (my $x = 0; $x < $width; $x++)
{
	for (my $y = 0; $y < $height; $y++)
	{
		my $cols = $rows[$x];
		my $pix = $rows[$x][$y];
		$png->setPixel($x, $y, $pix == 0 ? $black : $white);
	}
}

my $ff = new IO::File $ARGV[0].'.png', 'w';
$ff->binmode(0);
print $ff $png->png();


