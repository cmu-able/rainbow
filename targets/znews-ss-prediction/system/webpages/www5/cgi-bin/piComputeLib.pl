#!/usr/bin/perl

#map$x+=(-1)**$_*4/(2*$_+1),0..1e6;die$x

use strict;
use Carp;
use Math::BigFloat;
use Class::Struct qw(struct);
use Data::Dumper;

# With due homage to Mathsoft's site:
#  http://www.mathsoft.com/asolve/constant/pi/pi.html

# Set the global precision.  Faster than passing it everywhichwhere.
my $precision = @ARGV>0?$ARGV[0]:10;

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


# Method 1:

# This gives Archimedes' approximation,
#      3+10/71 < Pi < 3+1/7
# ...on the 4th iteration.

sub BorchardtPfaff {
  if (@_) {
    my ($a,$b) = @{shift()};  # Retrieve the last iteration.
    my $a_next = div( $_2*$a*$b, $a+$b );
    my $b_next = root2($a_next*$b);
    return [ round($a_next), round($b_next) ];
  } else {  # Seed the recurrence
    return [ $_2*root2($_3), $_3 ];
  }
}

# Method 2:

# This uses Borwein's AGM method to compute Pi very quickly.
# The 'AGM' is short for the iterated arithmetic-geometric mean;
#   which was looked at by Lagrange and Gauss way back in 1799.
# We basically look at the tuple [a,b] which holds the arithmetic
#   and geometric means of the previous iteration:
#          a[n+1] = (a[n] + b[n])/2    // Arithmetic mean
#          b[n+1] = sqrt(a[n] * b[n])  // Geometric mean
#
# This algorithm is neat, since it is quadratically convergent -
#   that means that the error goes as 2^(-iteration_count).
#
# In other words, one more step = twice the # of correct digits.

sub BorweinAGM {
  if (@_) {
    my ($a,$b,$pi) = @{shift()};  # Retrieve the last iteration.

    my $sqrt_a = root2($a);
    my $inv_sqrt_a = inv($sqrt_a);
    my $b_plus_1 = $b+$_1;    

    my $a_next = $_half * ($sqrt_a+$inv_sqrt_a);
    my $b_next = div( $b*$sqrt_a+$inv_sqrt_a , $b_plus_1 );
    my $pi_next = $pi*div( $a_next+$_1 , $b_next+$_1 );
    return [ round($a_next), round($b_next), round($pi_next) ];

  } else {  # Seed the recurrence
    my $a0 = root2($_2);
    my $pi0 = $_2+root2($_2);
    my $b1 = root2($a0);  # = root4($_2)
    my $a1 = $_half * ($b1 + inv($b1));
    
    return [ round($a1), round($b1), round($pi0 * div($a1 + $_1, $b1 + $_1)) ];
  }
}

# Method 3:

# The arctangent approximation, as determined by Machin (1706).
#      Pi = 4(4 * arctan(1/5) - arctan(1/239)),
# ...where arctan(x) = sum( (-1)^k/(2*k+1)*x^(2*k+1), x=0..infinity).

sub ArctanPi {
  if (@_) {
    my ($k, $atan5, $atan239, $denom5, $denom239, $pi) = @{shift()};  # Retrieve the last iteration.

    $k++;
    my $parity=$k & 1;
    $denom5 = $denom5 * $_25th;

    my $inv_2_k_plus_1='Math::BigFloat'->new(2*$k+1);

    my $term5 = div($denom5, $inv_2_k_plus_1 );
    $denom239 = $denom239 * $_57121th;

    my $term239 = div($denom239, $inv_2_k_plus_1 );
    $atan5 = $parity ? $atan5 - $term5 : $atan5 + $term5;
    $atan239 = $parity ? $atan239 - $term239 : $atan239 + $term239;
    my $pi = $_4*($_4*$atan5 - $atan239);

    return [ $k, round($atan5), round($atan239), 
             round($denom5), round($denom239), round($pi) ];
  } else {  # Seed the recurrence
    my ($k, $atan5, $atan239) = (0, $_fifth, $_239th);
    return [ $k, $atan5, $atan239, $atan5, $atan239, $_4*($_4*$atan5 - $atan239)];
  }
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

my $methods = ["BorchardtPfaff", "ArctanPi", "BorweinAGM", "RamanujanEI" ];

my $longestMethodLen=0;
foreach my $method (@{$methods}) {
  $longestMethodLen = length($method) if length($method) > $longestMethodLen;
}

my $pis = [ BorchardtPfaff(), ArctanPi(), BorweinAGM(), RamanujanEI() ];

for (my $i=0; $i<$maxIterations; $i++) {
  print "Iteration $i:\n";
  if (!$debugVerbose) {
    for (my $j=0; $j<@{$methods}; $j++) {

      # The following piece of line noise is equivalent to the
      #   STL C++, "

      #       piValue = pis[j].last();

      #   ", if the declaration for pis is:

      # std::vector <std::vector <BigFloat> > pis;
      #

      my $methodName = ${$methods}[$j];
      my $piValue = ${${$pis}[$j]}[-1];
      print ${$methods}[$j] . ": " .
            ' ' x ($longestMethodLen - length($methodName)) . 
            $piValue . "\n";
    }
    print "\n";
  }
  print(Dumper($pis)) if $debugVerbose;
  
  last if $i+1 == $maxIterations;

  $pis = [ BorchardtPfaff(${$pis}[0]), 
               ArctanPi(${$pis}[1]), 
               BorweinAGM(${$pis}[2]), 
               RamanujanEI(${$pis}[3]) ];
}

