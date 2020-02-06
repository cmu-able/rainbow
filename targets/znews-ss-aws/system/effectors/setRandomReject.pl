#!/usr/bin/perl -w

use strict;
use Getopt::Std;

my $APACHE_WWW_DIR = "/home/owen/project/rainbow/delegate/targets/znews1-d/system/webpages/www5";

sub usage() {
  print STDERR << "EOF";

This effector program affects random reject frequency (0 or mod over seconds).

 usage: $0 [option] -r [0|modval]

  -h        : this (help) message
  -r 0|mod  : the modulus value for request rejection (0 for NO reject)

EOF
  exit;
}

# get options
my %opts;
getopts('hr:', \%opts) or usage();
usage() if ($opts{h} || !defined($opts{r}));

my $modVal = $opts{r};
usage() if ($modVal < 0 && $modVal > 10);

my $file = "reject.mod";
print "Setting reject modulus value to $modVal...";
open(FOUT, ">$APACHE_WWW_DIR/$file");
print FOUT $modVal;
close(FOUT);
print "\nDone.";
