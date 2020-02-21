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
my $ALIAS = "apachetop";
my $NAME = "ApacheTopProbe";
my $DELAY = 10;
my $APACHE_LOG = "/cygdrive/c/server/Apache2/logs/access.log";
my $APACHETOP_LOG = "apachetop.log";
my $APACHETOP_EXEC = "./apachetop -f $APACHE_LOG -o $APACHETOP_LOG";
#############################


sub usage() {
  print STDERR << "EOF";

This program dumps apachetop output to the Probe Relay file or socket

 usage: $0 [option] -o file|-k

  -h        : this (help) message
  -d delay  : in milliseconds, the delay between each update [$DELAY]
  -k        : indicate to use socket to relay updates
  -o file   : file to which to output apachetop info
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

# fork to the apachetop program
my $pid = fork();
if ($pid == 0) {  ### child process
  if (! -e $APACHETOP_LOG) {
    open(FOUT, ">> $APACHETOP_LOG");
    close(FOUT);
  }
  exec($APACHETOP_EXEC)
    || die "exec $APACHETOP_EXEC failed: $! $?";
  exit(0);
}

### parent process

register($ALIAS, $NAME, location(), [$sock,$fout]);

# obtain apachetop's log file
open(FIN, "<$APACHETOP_LOG");
seek(FIN, 0, 2);
my $curpos = tell(FIN);
print "Seek to end of file $curpos\n" if ($doPrint);
seek(FIN, $curpos, 0);
my($line);
while (!shouldTerminate($sock)) {
  $line = <FIN>;
  if (defined($line)) {
    $curpos = tell(FIN);
#    print "Updated position $curpos\n" if ($doPrint);
    announce($ALIAS, $line, [$sock,$fout]);
  }
  seek(FIN, $curpos, 0);
  usleep($delay);
}

deregister($ALIAS, [$sock,$fout]);
cleanup($sock, [$fout]);
