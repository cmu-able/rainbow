#!/usr/bin/perl -w

use lib ".";
use strict;
use Getopt::Std;
use Time::HiRes qw(usleep);

#############################
# Probe Defining Attributes
#############################
my $DELAY = 15000;
my $REPEATEVERY = 5;
#############################

sub usage() {
  print STDERR << "EOF";

This program periodically dumps output from a swimsim command

 usage: $0 [option] command

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]

EOF
  exit;
}

sub probeSystem () {
  my $response = `../util/swimcmd.sh @ARGV`;
  if ($response =~ /^error/) {
      $response = "-1\n";
  }
  return $response;
}

# get options
my %opts;
getopts('hd:', \%opts) or usage();
usage() if ($opts{h});

my $delay = $DELAY;
if ($opts{d}) {
  $delay = $opts{d};
}
$delay *= 1000;

my $repeatCnt = $REPEATEVERY;
my $savedValue = "";

while (1) {
  usleep($delay);
  my $value = probeSystem();
  if ($value ne $savedValue || $repeatCnt == 0) {
    $savedValue = $value;  #store new value
    print $value;

    if ($repeatCnt == 0) {
      $repeatCnt = $REPEATEVERY;
    }
  } else {
    $repeatCnt--;
  }
}

