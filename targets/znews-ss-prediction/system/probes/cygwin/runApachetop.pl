#!/usr/bin/perl -w

use strict;

#############################
my $APACHE_LOG = "/cygdrive/c/server/Apache2/logs/access.log";
my $APACHETOP_LOG = "/cygdrive/c/server/rainbow/oracle/targets/znews1/log/apachetop.log";
my $APACHETOP_EXEC = "./apachetop -f $APACHE_LOG -o $APACHETOP_LOG";
#############################

if (! -e $APACHETOP_LOG) {
  open(FOUT, ">> $APACHETOP_LOG");
  close(FOUT);
}
exec($APACHETOP_EXEC)
  || die "exec $APACHETOP_EXEC failed: $! $?";
