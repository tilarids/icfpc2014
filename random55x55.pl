use warnings;
use strict;
use Data::Dumper;

use GD;
use IO::File;

my $width = 55;
my $height = 55;

my $png = new GD::Image($width, $height);

my $white = $png->colorAllocate(255,255,255);
my $black = $png->colorAllocate(0,0,0);

for (my $x = 0; $x < $width; $x++)
{
	for (my $y = 0; $y < $height; $y++)
	{
		my $cond = rand() * (3025 + 1321) > 1321;
		$png->setPixel($x, $y, $cond ? $black : $white);
	}
}

my $ff = new IO::File 'random55x55.png', 'w';
$ff->binmode(0);
print $ff $png->png();
