#!/usr/bin/perl -w

use lib ".";
use strict;
use Getopt::Std;
use Time::HiRes qw(usleep);
use Rainbow::Probe qw(location openFile openSocket cleanup shouldTerminate
		      register announce deregister);

#############################
# Probe Defining Attributes
#############################
my $ALIAS = "load";
my $NAME = "LoadProbe";
my $DELAY = 2500;
#############################


sub usage() {
  print STDERR << "EOF";

This program dumps current CPU usage to the Probe Relay file or socket

 usage: $0 [option] -o file|-k

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]
  -k        : indicate to use socket to relay updates
  -o file   : file to which to output periodic CPU usage info
  -s        : be silent, no period info to stdout

EOF
  exit;
}

sub cpuTime () {
  my ($user, $nice, $sys, $idle, $iowait);

  local(*FIN);
  open(FIN, "</proc/stat");
  while (<FIN>) {
    chomp;
    if (/^cpu\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)(\s+(\d+))?/o) {
      ($user, $nice, $sys, $idle, $iowait) = ($1, $2, $3, $4, $6);
      if (!defined($iowait)) {
        $iowait = 0;
      }
      last;
    }
  }
  close(FIN);
  return ($user, $nice, $sys, $idle, $iowait);
}

# get options
my %opts;
getopts('hkso:d:', \%opts) or usage();
usage() if ($opts{h} || (!$opts{o} && !$opts{k}));

my($fout, $sock);
if ($opts{o}) {
  $fout = openFile($opts{o});
}
if ($opts{k}) {
  $sock = openSocket();
}

my $delay = $DELAY;
if ($opts{d}) {
  $delay = $opts{d};
}
$delay *= 1000;

my $doPrint = 1;
if ($opts{s}) {
  $doPrint = 0;
}

# save current cpu ticks
my($userSv, $niceSv, $sysSv, $idleSv, $iowaitSv) = cpuTime();

register($ALIAS, $NAME, location(), [$sock,$fout]);

while (!shouldTerminate($sock)) {
  usleep($delay);
  # retrieve new cpu ticks
  my ($userN, $niceN, $sysN, $idleN, $iowaitN) = cpuTime();
  # compute delta cpu ticks
  my ($user, $nice, $sys, $idle, $iowait) =
    ($userN - $userSv, $niceN - $niceSv, $sysN - $sysSv, $idleN - $idleSv, $iowaitN - $iowaitSv);
  # store the new saved ticks
  ($userSv, $niceSv, $sysSv, $idleSv, $iowaitSv) = ($userN, $niceN, $sysN, $idleN, $iowaitN);
  my $total = $user + $nice + $sys + $idle + $iowait;
  my $rpt = sprintf("[%s] %9.7f %9.7f %9.7f %9.7f %9.7f\n", "".localtime(),
		    $user/$total, $nice/$total, $sys/$total, $idle/$total, $iowait/$total);
  print $rpt if ($doPrint);
  announce($ALIAS, $rpt, [$sock,$fout]);
}

deregister($ALIAS, [$sock,$fout]);
cleanup($sock, [$fout]);
