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
my $ALIAS = "diskio";
my $NAME = "DiskIOProbe";
my $DELAY = 5000;
#############################

# Determine diskstats format
my $IOSTAT = "/usr/bin/iostat";
if (! -e $IOSTAT) {
  print "No iostat found, please install the sysstat package to continue!\n";
  exit -1;
}
my $IOSTAT_ARGS = "-k " . ($DELAY/1000);  # period, with indefinite ouput

sub usage() {
  print STDERR << "EOF";

This program dumps periodic Disk IO status to the Probe Relay file or socket

 usage: $0 [option] -o file|-k

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]
  -k        : indicate to use socket to relay updates
  -o file   : file to which to output periodic Disk IO info
  -s        : be silent, no period info to stdout

EOF
  exit;
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

# init disk IO stats vars
my($dev, $tps, $rdKBps, $wrKBps, $rdKB, $wrKB);
my($isInitial, $cntCycle) = (1, 0);
open(FIN, "$IOSTAT $IOSTAT_ARGS |")
  || die "Can't fork $IOSTAT: $!";

register($ALIAS, $NAME, location(), [$sock,$fout]);

while (!shouldTerminate($sock)) {
  # retrieve new disk IO stats
  while (<FIN>) {
    chomp;
    if (/^Device:/i) {
      ++$cntCycle;
      if ($isInitial && $cntCycle > 1) {  # no longer initial
        print "Filtered initial round!\n" if ($doPrint);
        $isInitial = 0;
      }
    }
    if (!$isInitial &&
        /^(\w+)\s+([0-9\.]+)\s+([0-9\.]+)\s+([0-9\.]+)\s+([0-9\.]+)\s+([0-9\.]+)/) {
      ($dev, $tps, $rdKBps, $wrKBps, $rdKB, $wrKB) = ($1, $2, $3, $4, $5, $6);
      last;
    }
  }
  my $rpt = sprintf("[%s] %s %.2f %.2f %.2f %d %d\n", "".localtime(),
		    $dev, $tps, $rdKBps, $wrKBps, $rdKB, $wrKB);
  print $rpt if ($doPrint);
  announce($ALIAS, $rpt, [$sock,$fout]);
  usleep($delay);
}

deregister($ALIAS, [$sock,$fout]);
close(FIN) || die "Bad $IOSTAT: $! $?";
cleanup($sock, [$fout]);
