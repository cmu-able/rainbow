#!/bin/bash
START_DIR="$(pwd)"
echo "Loading property and function definitions"
ZNN_BIN_DIR="$( cd "$( dirname "$0" )" && pwd)"
[ -a "$ZNN_BIN_DIR/../znn-config" ] \
  && source "$ZNN_BIN_DIR/../znn-config" || {
    echo "Failed to configure the environment"
    echo "  Expected to find znn-config in '$ZNN_BIN_DIR/..'"
    exit 1;
}
[ -a "$ZNN_BIN_DIR/functions.sh" ] && source "$ZNN_BIN_DIR/functions.sh" || {
  echo "Failed to load general functions"
  echo "  Expected to find functions.sh in '$ZNN_BIN_DIR/'"
  exit 1;
}
[ -a "$ZNN_BIN_DIR/znn-functions.sh" ] && source "$ZNN_BIN_DIR/znn-functions.sh" || {
  echo "Failed to load znn setup functions"
  echo "  Expected to find znn-functions.sh in '$ZNN_BIN_DIR'"
  exit 1;
}

DO_CONFIGURE="no"
[ "$#" == "1" -a "$1" ] && {
  DO_CONFIGURE="yes"
  ZNN_PROP_FILE=$1
}
echo "  Configuration set to $DO_CONFIGURE"

SERVICE=lb
ZNN_PCRE=$ZNN_HOME/sw-bin/pcre-$SERVICE-$ZNN_PCRE_VERSION
ZNN_HTTPD=$ZNN_HOME/sw-bin/httpd-$SERVICE-$ZNN_HTTPD_VERSION

echo "Installing the load balancer"
echo "  Installing pcre..."
echo "    Checking that pcre is not already in '$ZNN_HOME/sw-bin'"
if [ ! -d "$ZNN_HOME/sw-bin/pcre-$SERVICE-$ZNN_PCRE_VERSION" \
  -a ! -d "$ZNN_HOME/sw-exp/pcre-$SERVICE-$ZNN_PCRE_VERSION" ]; then
  echo "    Uncompressing pcre source"
  cd $ZNN_HOME/sw-exp
  tar zxf "../sw-src/pcre-$ZNN_PCRE_VERSION.tar.gz"
  mv "pcre-$ZNN_PCRE_VERSION" "pcre-$SERVICE-$ZNN_PCRE_VERSION"
  cd "pcre-$SERVICE-$ZNN_PCRE_VERSION"
  echo "    Compiling pcre"
  ./configure --prefix=$ZNN_PCRE
  make
  make install
  [ -d $ZNN_PCRE ] || {
    echo "  pcre failed to install successfully"
    exit 1
  }
  echo "  pcre installed!"
else
  echo "    pcre is already installed"
fi

echo "  Installing httpd"
echo "    Checking that httpd is not already in '$ZNN_HOME/sw-bin'"
if [ ! -d "$ZNN_HOME/sw-bin/httpd-$SERVICE-$ZNN_HTTPD_VERSION" \
  -a ! -d "$ZNN_HOME/sw-exp/httpd-$SERVICE-$ZNN_HTTPD_VERSION" ]; then
  echo "    Uncompressing httpd and apr source"
  cd $ZNN_HOME/sw-exp
  tar zxf "../sw-src/httpd-$ZNN_HTTPD_VERSION.tar.gz"
  mv "httpd-$ZNN_HTTPD_VERSION" "httpd-$SERVICE-$ZNN_HTTPD_VERSION"
  cd httpd-$SERVICE-$ZNN_HTTPD_VERSION/srclib
  tar zxf ../../../sw-src/apr-$ZNN_APR_VERSION.tar.gz
  mv apr-$ZNN_APR_VERSION apr
  tar zxf ../../../sw-src/apr-util-$ZNN_APR_UTIL_VERSION.tar.gz
  mv apr-util-$ZNN_APR_UTIL_VERSION apr-util

  echo "  Compiling httpd"
  cd ..
  ./configure --prefix=$ZNN_HTTPD --with-included-apr --with-pcre=$ZNN_PCRE
  make
  make install
  [ -d $ZNN_HTTPD ] || {
    echo "httpd failed to install successfully"
    exit 1
  }
  echo "httpd installed!"
else
  echo "    httpd is already installed"
fi
echo "  Installing mod_security"
echo "    Uncompressing mod_security"
cd $ZNN_HOME/sw-exp
tar xfz "../sw-src/modsecurity-apache_$ZNN_MODSECURITY_VERSION.tar.gz"
echo "    Compiling mod_security"
cd "modsecurity-apache_$ZNN_MODSECURITY_VERSION"
./configure --with-pcre=$ZNN_PCRE/bin/pcre-config --with-apxs=$ZNN_HTTPD/bin/apxs --with-apr=$ZNN_HOME/sw-exp/httpd-$SERVICE-$ZNN_HTTPD_VERSION/srclib/apr/apr-1-config --with-apu=$ZNN_HOME/sw-exp/httpd-lb-$ZNN_HTTPD_VERSION/srclib/apr-util/apu-1-config --disable-mlogc --prefix=$ZNN_MODSECURITY
make
make install
[ -f "$ZNN_HTTPD/modules/mod_security2.so" ] || {
  echo "mod_security failed to install properly in '$ZNN_HTTPD/modules/mod_security2.so'"
  exit 1
}
#thought I would need to copy into httpd/modules directory but seems to be there
#just change permissions to be consistent with other modules
chmod 755 $ZNN_HTTPD/modules/mod_security2.so

echo "  Configuring load balancer balance and security files"
cp $ZNN_HOME/znn-conf/$SERVICE/security.conf $ZNN_HTTPD/conf/
cp $ZNN_HOME/znn-conf/$SERVICE/balanced.conf $ZNN_HTTPD/conf/
cp $ZNN_HOME/znn-conf/$SERVICE/blocked.conf $ZNN_HTTPD/conf/
CONF_FILE="$ZNN_HTTPD/conf/httpd.conf"
find_first_line MODULE_LINE "$CONF_FILE" 1 \
  '^LoadModule .*'
MODULE_LINE=$(($MODULE_LINE-1))
insert_line "$CONF_FILE" $MODULE_LINE \
  "LoadModule security2_module modules/mod_security2.so"
insert_line "$CONF_FILE" $MODULE_LINE \
    "LoadFile $ZNN_LIBXML2_SO"
replace_line_regex "$CONF_FILE" \
    "#LoadModule unique_id.*" \
    "LoadModule unique_id_module modules/mod_unique_id.so"
replace_line_regex "$CONF_FILE" \
    "#LoadModule proxy_module.*" \
    "LoadModule proxy_module modules/mod_proxy.so"
replace_line_regex "$CONF_FILE" \
    "#LoadModule proxy_http.*" \
    "LoadModule proxy_http_module modules/mod_proxy_http.so"
replace_line_regex "$CONF_FILE" \
    "#LoadModule proxy_balancer.*" \
    "LoadModule proxy_balancer_module modules/mod_proxy_balancer.so"
replace_line_regex "$CONF_FILE" \
    "#LoadModule slotmem.*" \
    "LoadModule slotmem_shm_module modules/mod_slotmem_shm.so"
replace_line_regex "$CONF_FILE" \
    "#LoadModule lbmethod_byreq.*" \
    "LoadModule lbmethod_byrequests_module modules/mod_lbmethod_byrequests.so"
find_line MODULE_LINE "$CONF_FILE" \
    "^DocumentRoot.*"
find_first_line MODULE_LINE "$CONF_FILE" $MODULE_LINE \
    "^ *Require all granted"
insert_line "$CONF_FILE" $MODULE_LINE \
    "Include conf/blocked.conf"
insert_line "$CONF_FILE" $MODULE_LINE \
    "Allow from all"
insert_line "$CONF_FILE" $MODULE_LINE \
    "Order allow,deny"
cat $ZNN_HOME/znn-conf/$SERVICE/httpd.conf-append >> $CONF_FILE

echo "Configuring effectors"
for i in sw-src/effectors-templates/*; do   
  echo "processing " $(basename $i)
  sed 's|APACHE_INSTALL|'$ZNN_HTTPD'|' < $i > effectors/$(basename $i)
done

cd $START_DIR
if [ "$DO_CONFIGURE" == "yes" ]; then
  echo "Configuring load balancer based on configurations"
  echo "Setting balancer files to point to the right ips and ports"
  setup_znn_properties $ZNN_PROP_FILE
  setup_balancer_file $ZNN_HTTPD/conf/balanced.conf
  lbPort="customize_system_target_lb_httpPort"
  lbp=${!lbPort}
  lbp=`echo $lbp | sed -e 's/^ *//g' -e 's/ *$//g'`
  echo "Configuring http.conf"
  cp $ZNN_HTTPD/conf/httpd.conf{,orig}
  replace_line_regex "$CONF_FILE" \
    "^Listen.*" \
    "Listen $lbp" 

fi
echo "Load balancer successfully installed."
if [ "$DO_CONFIGURE" == "no" ]; then
echo "To complete configuration, enter this IP as the customize.system.target.$SERVICE"
echo "  property in the rainbow.properties file you will use to configure rainbow."
echo "  This will set up the load balancer to point to the appropriate web servers"
echo "  when you use the configure-znn.sh setup script in the machine that will"
echo "  run the Rainbow master."
echo "Once configured, start the server by issuing the command:"
echo "  $ZNN_HTTPD/bin/httpd start"
else
echo "Start the server by issuing the command:"
echo "  $ZNN_HTTPD/bin/httpd -k start"
fi
