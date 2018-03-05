#!/usr/bin/perl -w

package Rainbow::Probe;

use strict;
use Time::HiRes qw(usleep);
use IO::Handle;
use IO::Socket::INET;

BEGIN {
    use Exporter ();
    use vars     qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS);

    # set the version for version checking (not really sure what I'm doing)
    @ISA         = qw(Exporter);
    @EXPORT      = qw();
    %EXPORT_TAGS = ();     # eg: TAG => [ qw!name1 name2! ],

    # your exported package globals go here,
    # as well as any optionally exported functions
    @EXPORT_OK   = qw($SOCKET_PORT
                      &location &openFile &openSocket &cleanup &shouldTerminate
                      &register &announce &deregister);

    autoflush STDOUT 1;
    autoflush STDERR 1;
}
use vars @EXPORT_OK;

# non-exported package globals here
use vars qw($RAINBOW_SERVICE_CMD $LOCATION $evalLocation
            $KILL_SIGNAL $TERM_SIG_FILE $KILL_ACK);

# initialize package globals, first exported ones
$SOCKET_PORT = 9210;

# then the others (still accessible via $myCGI::var)
$RAINBOW_SERVICE_CMD = "CMD relay";
$LOCATION = "127.0.0.1";
$KILL_SIGNAL = ".KILL.";
$TERM_SIG_FILE = "kill";
$KILL_ACK = "ACK kill";

sub location {
  if (!defined($evalLocation)) {
    $evalLocation = `hostname`;
    chomp($evalLocation);
    if ($evalLocation =~ /\r$/) {  # grrr, windows!
      chop($evalLocation);
    }
    if ($evalLocation eq "") {
      $evalLocation = $LOCATION;
    }
  }
  return $evalLocation;
}

sub openFile {
  my($file) = @_;

  local(*FOUT);
  open(FOUT, ">>$file");
  FOUT->autoflush(1);

  return *FOUT{IO};
}

sub openSocket {
  my $sock = IO::Socket::INET->new(PeerAddr => "$LOCATION",
				   PeerPort => "$SOCKET_PORT",
				   Proto     => "tcp")
    or die "Can't create socket: $!";
  print "Created socket " . $sock->sockhost().":".$sock->sockport() . "\n";
  if ($sock->connected()) {
    $sock->autoflush(1);
    print $sock "$RAINBOW_SERVICE_CMD\n";
    usleep(1000000);  #sleep half a sec to ensure connection
    print "  - socket connected\n";
    $sock->blocking(0);
  } else {
    print STDERR "  - socket unconnected!";
    exit(-1);
  }

  return $sock;
}

sub cleanup {
  my($sock, $outs) = @_;

  $sock->shutdown(2) if (defined($sock));
  foreach my $out (@{$outs}) {
    close($out) if (defined($out));
  }
}

sub shouldTerminate {
  my($sock) = @_;
  my($line, $killed);
  $killed = 0;

  if (defined($sock)) {
    # check if there's output from socket
    if (! $sock->connected()) {  # disconnectd
      print "Socket disconnected!\n";
      $killed = 1;
    } elsif (read($sock,$line,6)) {
      if ($line =~ /$KILL_SIGNAL/) {  # a kill message
        print "Received KILL on socket...\n";
        $killed = 1;
        print $sock "$KILL_ACK\n";
      }
    }
  }

  return -e $TERM_SIG_FILE || $killed;
}

sub register {
  my($alias, $name, $location, $outs) = @_;

  my $msg = "\$\$+$alias> $name\@$location\n";
  print "Registering $name\@$location:$alias...\n";
  foreach my $out (@{$outs}) {
    print $out $msg if (defined($out));
  }
}

sub announce {
  my($alias, $txt, $outs) = @_;

  my $msg = "\$\$*$alias> $txt";  # newline supplied with $txt
  foreach my $out (@{$outs}) {
    print $out $msg if (defined($out));
  }
}

sub deregister {
  my($alias, $outs) = @_;

  my $msg = "\$\$-$alias>\n";
  foreach my $out (@{$outs}) {
    print $out $msg if (defined($out));
  }
}
