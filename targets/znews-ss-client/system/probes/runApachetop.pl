#!/usr/bin/perl -w

use strict;

#############################
my $APACHE_LOG = "/var/log/apache2/access-delegate.log";
my $APACHETOP_LOG = "../../log/apachetop.log";
my $APACHETOP_EXEC = "./apachetop -f $APACHE_LOG -o $APACHETOP_LOG";
#############################

if (! -e $APACHETOP_LOG) {
  open(FOUT, ">> $APACHETOP_LOG");
  close(FOUT);
}
exec($APACHETOP_EXEC)
  || die "exec $APACHETOP_EXEC failed: $! $?";
