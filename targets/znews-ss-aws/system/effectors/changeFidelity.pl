#!/usr/bin/perl -w

use strict;
use Getopt::Std;

my $isCygwin = 0;
my $uname;
if ( -e "/bin/uname" ) {
  $uname = `/bin/uname`;
} elsif ( -e "/usr/bin/uname" ) {
  $uname = `/usr/bin/uname`;
} else {
  $uname = "UNKNOWN";
}
if ($uname =~ /CYGWIN/i) {
  $isCygwin = 1;
}

chomp(my $SCRIPT_DIR = `/usr/bin/dirname "$0"`);
my $APACHE_CONF_DIR = ($isCygwin) ?
  "/cygdrive/c/server/Apache2/conf" :
  "/etc/apache2";
my $APACHE_CONF_FILE = ($isCygwin) ?
  "httpd.conf" :
  "apache2.conf";
my $APACHE_SITE_DIR = "$APACHE_CONF_DIR/sites-available";
my $APACHE_SITE_FILE = "z.com";
my $SOURCE_CONF_DIR = ($isCygwin) ?
  "$SCRIPT_DIR/fidelity.conf/cygwin" :
  "$SCRIPT_DIR/fidelity.conf/oracle";

sub usage() {
  print STDERR << "EOF";

This effector program changes the webserver fidelity level (1,3,5).

 usage: $0 [option] -l level

  -h        : this (help) message
  -l level  : the new fidelity setting, should be 1, 3, or 5 (0 for original)

EOF
  exit;
}

# get options
my %opts;
getopts('hl:', \%opts) or usage();
usage() if ($opts{h} || !defined($opts{l}));

my $level = $opts{l};
usage() if ($level != 0 && $level != 1 && $level != 3 && $level != 5);

my $fExt = "-f";
if ($level == 0) {
  if ($isCygwin) {
    $fExt = ".default";
  } else {
    $fExt .= "5";
  }
} else {
  $fExt .= $level;
}

if ($isCygwin) {

  print "Copying level $level $APACHE_CONF_FILE in $APACHE_CONF_DIR...";
  print `/usr/bin/cp -f $SOURCE_CONF_DIR/$APACHE_CONF_FILE$fExt $APACHE_CONF_DIR/$APACHE_CONF_FILE`;
  print "\nGracefully restarting Apache...";
  print `/cygdrive/c/server/Apache2/bin/Apache.exe -w -n "Apache2" -k restart`;
  print "\nDone.";

} else {

  print "Copying level $level $APACHE_CONF_FILE in $APACHE_CONF_DIR...";
  print `/bin/cp -f $SOURCE_CONF_DIR/$APACHE_CONF_FILE$fExt $APACHE_CONF_DIR/$APACHE_CONF_FILE`;
  print "\nSwapping site to level $level...";
  print `/bin/cp -f $SOURCE_CONF_DIR/$APACHE_SITE_FILE$fExt $APACHE_SITE_DIR/$APACHE_SITE_FILE`;
  print "\nGracefully restarting Apache...";
  print `/etc/init.d/apache2 reload`;
  print "\nDone.";

}
