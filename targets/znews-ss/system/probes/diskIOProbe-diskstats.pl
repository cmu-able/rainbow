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
my $DELAY = 4000;
#############################

# Determine diskstats format
my $statfile;
my $uname = `uname -a`;
if ($uname =~ /Linux\s+(.+?)\s+(\d+[.]\d+)/) {
  if ($2 eq "2.6") {
    $statfile = "/proc/diskstats";
#  } elsif ($2 <= 2.5) {
#    $statfile = "/proc/partitions";  # granularity of info more coarse
  }
} else {
  print "Linux version unsupported for diskstat access! Unable to continue probing\n";
  exit -1;
}


sub usage() {
  print STDERR << "EOF";

This program dumps current accumulated Disk IO to the Probe Relay file or socket

 usage: $0 [option] -o file|-k

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]
  -k        : indicate to use socket to relay updates
  -o file   : file to which to output periodic Disk IO info
  -s        : be silent, no period info to stdout

EOF
  exit;
}

sub diskUsage () {
  my ($MINOR, $FIRST, $TMIO) = (2, 4, 14);  # index to stat entry
  my (@accumStat) = (0,0,0,0,0,0,0,0,0,0,0);
  my (@dstat);  #$rcmp,$rmrg,$rsect,$rms,$wcmp,$wmrg,$wsect,$wms,$curio,$curms,$tmio

  open(FIN, "< $statfile");
  while (<FIN>) {
    chomp;
    @dstat = split(/\s+/);  # blank zeroth element due to space(s) in beginning
    if ($dstat[$MINOR] == 0 && $dstat[$TMIO] > 0) {  # accum reading from device
      for my $i ($FIRST..$#dstat) {
        $accumStat[$i-$FIRST] += $dstat[$i];
      }
      #print "Accumulated @accumStat\n";
    }
  }
  close(FIN);
  return @accumStat;
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

# zero out disk IO stats
my($rcmpS,$rmrgS,$rsectS,$rmsS,$wcmpS,$wmrgS,$wsectS,$wmsS,$curioS,$curmsS,$tmioS)
  = (0,0,0,0,0,0,0,0,0,0,0);

register($ALIAS, $NAME, location(), [$sock,$fout]);

while (!shouldTerminate($sock)) {
  # retrieve new disk IO stats
  my ($rcmpN,$rmrgN,$rsectN,$rmsN,$wcmpN,$wmrgN,$wsectN,$wmsN,$curioN,$curmsN,$tmioN) = diskUsage();
  # compute delta stats for all except 9th and 10th
  my ($rcmp,$rmrg,$rsect,$rms,$wcmp,$wmrg,$wsect,$wms,$curio,$curms,$tmio) =
    ($rcmpN-$rcmpS, $rmrgN-$rmrgS, $rsectN-$rsectS, $rmsN-$rmsS, $wcmpN-$wcmpS,
     $wmrgN-$wmrgS, $wsectN-$wsectS, $wmsN-$wmsS, $curioN, $curmsN, $tmioN-$tmioS);
  # store the new saved ticks
  ($rcmpS,$rmrgS,$rsectS,$rmsS,$wcmpS,$wmrgS,$wsectS,$wmsS,$curioS,$curmsS,$tmioS) =
    ($rcmpN,$rmrgN,$rsectN,$rmsN,$wcmpN,$wmrgN,$wsectN,$wmsN,$curioN,$curmsN,$tmioN);
  # each sector is 512 bytes as of kernel 2.4, so divide 2 to get kb
  #print "D: $rcmp,$rmrg,$rsect,$rms,$wcmp,$wmrg,$wsect,$wms,$curio,$curms,$tmio\n" if ($doPrint);
  my $kbRead = $rsect/2.0;
  my $kbRps = ($rms == 0) ? 0 : ($kbRead / $rms) * 1000;  # sec = 1000 ms
  my $kbWritten = $wsect/2.0;
  my $kbWps = ($wms == 0) ? 0 : ($kbWritten / $wms) * 1000;
  my $tps = ($rms+$wms == 0) ? 0 : ($rcmp+$wcmp)/($rms+$wms) * 1000;
  my $rpt = sprintf("[%s] %9.2f %9.2f %9.2f %9d %9d %9d %9d %9d\n", "".localtime(),
		    $tps, $kbRps, $kbWps, $kbRead, $kbWritten, $curio, $curms, $tmio);
  print $rpt if ($doPrint);
  announce($ALIAS, $rpt, [$sock,$fout]);
  usleep($delay);
}

deregister($ALIAS, [$sock,$fout]);
cleanup($sock, [$fout]);
