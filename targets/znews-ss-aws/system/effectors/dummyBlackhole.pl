#!/usr/bin/perl -w

use strict;
use Getopt::Std;

sub usage () {
  print STDERR << "EOF";
  
This effector program is a dummy blackholer.

  usege: $0 [option] -i IP
  
  -i IP: the IP address that should be blackholed
  
EOF
  exit;
}

#get options
my %opts;
getopts ('i:',\%opts) or usage ();
usage () if (!defined($opts{i}));
my $IP = $opts{i}
print "Blackholing $IP...\n";
