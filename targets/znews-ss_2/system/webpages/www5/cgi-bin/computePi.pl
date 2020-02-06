#!/usr/bin/perl

use strict;
use Carp;
use Math::BigFloat;
use Class::Struct qw(struct);
use Data::Dumper;

# With due homage to Mathsoft's site:
#  http://www.mathsoft.com/asolve/constant/pi/pi.html

# Set the global precision.  Faster than passing it everywhichwhere.
my $precision = @ARGV>0?$ARGV[0]:512;

# Set the iteration count.
my $maxIterations = @ARGV>1?$ARGV[1]:10;

# Set the verbose debugging flag.
my $debugVerbose = 0;

# Constants:
my $_1 = Math::BigFloat->new(1);
my $_2 = Math::BigFloat->new(2);
my $_3 = Math::BigFloat->new(3);
my $_4 = Math::BigFloat->new(4);
my $_6 = Math::BigFloat->new(6);
my $_8 = Math::BigFloat->new(8);
my $_239 = Math::BigFloat->new(239);
my $_half = Math::BigFloat->new(0.5);
my $_fifth = Math::BigFloat->new(0.2);
my $_25th = Math::BigFloat->new(0.04);
my $_239th = inv($_239);
my $_57121th = inv($_239*$_239);


# Utility methods:
sub div { 
  my $a = shift;
  my $b = shift;
  return new Math::BigFloat $a->copy()->fdiv($b,$precision);
}
sub inv {  # Inverse (1/x) function.
  my $a = shift;
  return new Math::BigFloat $_1->copy()->fdiv($a,$precision);
}
sub root2 { 
  my $self = shift;
  return new Math::BigFloat $self->copy()->fsqrt($precision);
}
sub root4 { 
  my $self = shift; 
  return root2(root2($self)); 
}
sub pow4 { 
  my $self = shift;
  my $square = round($self*$self);
  return round($square*$square); 
}
sub round {
  my $self = shift;
  return new Math::BigFloat $self->copy()->fround($precision);
}

# Method 4:

# This uses Ramanujan's elliptic integral-based technique to 
# compute Pi monstrously quickly.
#
# This algorithm is neat, since it is quartically convergent -
#   that means that the error goes as 4^(-iteration_count).
#
# In other words, one more step = four times the # of correct digits.

sub RamanujanEI {
  if (@_) {
    my ($a, $z, $factor, $pi) = @{shift()};  # Retrieve the last iteration.

    my $z_4_term = root4($_1 - pow4($z));
    my $z_next = div( $_1-$z_4_term, $_1+$z_4_term );

    my $z_next_plus_1 = $z_next + $_1;

    my $a_next = pow4($z_next_plus_1) * $a -
                 $factor * $z_next * round($z_next_plus_1 + $z_next * $z_next);
    $a_next = round($a_next);
    return [ $a_next, $z_next, round($factor * $_4), inv($a_next) ];

  } else {  # Seed the recurrence
    my $a = $_6-$_4*root2($_2);
    return [ $a, root2($_2)-$_1, $_8, inv($a)];
  }
}



######################################################################
#
#  Main routine:
#
#

my $iterResult = RamanujanEI();
for (my $i=1; $i<$maxIterations; $i++) {
  $iterResult = RamanujanEI($iterResult);
}
my $piValue = ${$iterResult}[-1];
my $piStr = "" . $piValue;

my $CHUNK = 32;
my $i = $CHUNK + 1;
print "Content-type: text/plain\r\n";
print "Content-length: 570\r\n\r\n";
print "Pi_512 :=\n";
print " " . substr($piValue, 0, $i) . "\n";
for (; $i < length($piValue) ; $i+=$CHUNK) {
  print "  " . substr($piValue, $i, $CHUNK) . "\n";
}

