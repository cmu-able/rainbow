#!/usr/bin/perl -w

use lib ".";
use strict;
use Getopt::Std;
use Time::HiRes qw(usleep);

#############################
# Probe Defining Attributes
#############################
my $ALIAS = "dimmer";
my $NAME = "DimmerProbe";
my $DELAY = 15000;
my $REPEATEVERY = 5;
#############################

sub usage() {
  print STDERR << "EOF";

This program dumps current server dimmer to Probe Relay file/socket

 usage: $0 [option] -o file|-k

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]

EOF
  exit;
}

sub determineDimmer () {
#  return `../util/swimcmd.sh get_dimmer`;
  return `echo 0.9`;
#  return "0.9";
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
my $savedDimmer = 0;

while (1) {
  usleep($delay);
  # compute new dimmer status
  my $dimmer = determineDimmer();
  if ($dimmer != $savedDimmer || $repeatCnt == 0) {
    $savedDimmer = $dimmer;  #store new dimmer
    #my $rpt = sprintf("[%s] %f\n", "".localtime(), $dimmer);
    my $rpt = sprintf("%f\n", $dimmer);
    print $rpt;

    if ($repeatCnt == 0) {
      $repeatCnt = $REPEATEVERY;
    }
  } else {
    $repeatCnt--;
  }
}

