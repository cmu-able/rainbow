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
my $ALIAS = "fidelity";
my $NAME = "FidelityProbe";
my $DELAY = 1000;
my $REPEATEVERY = 5;
my $APACHE_CONF_DIR = "/cygdrive/c/server/Apache2/conf";
my $APACHE_CONF_FILE = "httpd.conf";
#############################


sub usage() {
  print STDERR << "EOF";

This program dumps current server fidelity (1,3,5) to Probe Relay file/socket

 usage: $0 [option] -o file|-k

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]
  -k        : indicate to use socket to relay updates
  -o file   : file to which to output periodic server fidelity info
  -s        : be silent, no period info to stdout

EOF
  exit;
}

sub determineFidelity () {
  my $fid = 0;

  open(FIN, "$APACHE_CONF_DIR/$APACHE_CONF_FILE");
  my $firstLine = <FIN>;  # read the first line
  if ($firstLine =~ /[\#]{3} Fidelity-(\d+)/) {
    $fid = $1;
  }
  close(FIN);

  return $fid;
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

my $repeatCnt = $REPEATEVERY;
my $savedFidelity = 0;

register($ALIAS, $NAME, location(), [$sock,$fout]);

while (!shouldTerminate($sock)) {
  usleep($delay);
  # compute new fidelity status
  my $fidelity = determineFidelity();
  if ($fidelity != $savedFidelity || $repeatCnt == 0) {
    $savedFidelity = $fidelity;  #store new fidelity
    my $rpt = sprintf("[%s] %1d\n", "".localtime(), $fidelity);
    print $rpt if ($doPrint);
    announce($ALIAS, $rpt, [$sock,$fout]);

    if ($repeatCnt == 0) {
      $repeatCnt = $REPEATEVERY;
    }
  } else {
    $repeatCnt--;
  }
}

deregister($ALIAS, [$sock,$fout]);
cleanup($sock, [$fout]);
