use warnings;
use strict;
use GD;
use JSON;
my $image = GD::Image->newFromPng('bkg-2.png');

my @cols;

my ($w, $h) = $image->getBounds();

for (my $y = 0; $y < $h; $y++)
{
	my @row;
	for (my $x = 0; $x < $w; $x++)
	{
		push @row, $image->getPixel($x, $y);
	}
	push @cols, \@row;
}


print encode_json({ image => \@cols });