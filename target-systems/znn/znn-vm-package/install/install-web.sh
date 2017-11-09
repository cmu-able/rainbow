#!/bin/bash
START_DIR="$(pwd)"
echo "Loading property definitions"
ZNN_BIN_DIR="$( cd "$( dirname "$0" )" && pwd)"
[ -a "$ZNN_BIN_DIR/../znn-config" ] \
  & source "$ZNN_BIN_DIR/../znn-config" || {
  echo "Failed to configure the environment"
  echo "  Expected to find znn-config in '$ZNN_BIN_DIR/..'"
  exit 1
}
[ -a "$ZNN_BIN_DIR/functions.sh" ] && source "$ZNN_BIN_DIR/functions.sh" || {
  echo "Failed to load general functions"
  echo "  Expected to find 'functions.sh' in '$ZNN_BIN_DIR'";
  exit 1
}
[ -a "$ZNN_BIN_DIR/znn-functions.sh" ] && source "$ZNN_BIN_DIR/znn-functions.sh" || {
  echo "Failed to load znn setup functions"
  echo "  Expected to find 'znn-functions.sh' in '$ZNN_BIN_DIR'"
  exit 1;
}
[ "$#" -le "2" -a "${1:0:3}" == "web" ] && WEB_SERVER=$1 || {
  echo "Usage: $0 web<n> [properties]"
  exit 1
}


DO_CONFIGURE="no"
[ "$#" == "2" -a "$2" ] && {
  DO_CONFIGURE="yes"
  ZNN_PROP_FILE=$2
}

echo "  Configuration set to $DO_CONFIGURE"


SERVICE=$1
ZNN_PCRE=$ZNN_HOME/sw-bin/pcre-$SERVICE-$ZNN_PCRE_VERSION
ZNN_HTTPD=$ZNN_HOME/sw-bin/httpd-$SERVICE-$ZNN_HTTPD_VERSION
ZNN_PHP=$ZNN_HOME/sw-bin/php-$SERVICE-$ZNN_PHP_VERSION

echo "Installing the web server"
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
  [ -d "$ZNN_PCRE" ] || {
    echo "pcre did not install successfully"
    exit 1;
  }
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
    echo "httpd did not install successfully"
    exit 1
  }
else 
  echo "    httpd is already installed"
fi

echo "  Installing freetype"
if [ ! -d "$ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION" \
    -a ! -d "$ZNN_HOME/sw-exp/freetype-$SERVICE-$ZNN_FREETYPE_VERSION" ]; then 
  echo "    Uncompressing freetype"
  cd "$ZNN_HOME/sw-exp"
  tar xfz "../sw-src/freetype-$ZNN_FREETYPE_VERSION.tar.gz"
  mv "freetype-$ZNN_FREETYPE_VERSION" "freetype-$SERVICE-$ZNN_FREETYPE_VERSION"
  echo "    Configuring and compiling freetype"
  cd "freetype-$SERVICE-$ZNN_FREETYPE_VERSION"
  ./configure --prefix=$ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION
  make
  make install
  [ -d "$ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION" ] || {
    echo "freetype failed to install successfully"
    exit 1
  } 
  ln -s $ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION/lib/libfreetype.so $ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION/lib/freetype.so
else 
  echo "    freetype is already installed"
fi

echo "  Installing libpng"
if [ ! -d "$ZNN_HOME/sw-bin/libpng-$SERVICE-$ZNN_LIBPNG_VERSION" \
    -a ! -d "$ZNN_HOME/sw-exp/libpng-$SERVICE-$ZNN_LIBPNG_VERSION" ]; then 
  cd "$ZNN_HOME/sw-exp"
  tar zxf "../sw-src/libpng-$ZNN_LIBPNG_VERSION.tar.gz"
  mv "libpng-$ZNN_LIBPNG_VERSION" "libpng-$SERVICE-$ZNN_LIBPNG_VERSION"
  echo "    Configuring and compiling libpng"
  cd "libpng-$SERVICE-$ZNN_LIBPNG_VERSION"
  ./configure --prefix=$ZNN_HOME/sw-bin/libpng-$SERVICE-$ZNN_LIBPNG_VERSION
  make
  make install
  [ -d "$ZNN_HOME/sw-bin/libpng-$SERVICE-$ZNN_LIBPNG_VERSION" ] || {
    echo "libpng failed to install successfully"
    exit 1
  }
else 
  echo "    libpng is already installed"
fi

echo "  Installing libgd"
if [ ! -d "$ZNN_HOME/sw-bin/libgd-$SERVICE-$ZNN_LIBGD_VERSION" \
    -a ! -d "$ZNN_HOME/sw-exp/libgd-$SERVICE-$ZNN_LIBGD_VERSION" ]; then 
  cd "$ZNN_HOME/sw-exp"
  tar zxf "../sw-src/libgd-$ZNN_LIBGD_VERSION.tar.gz"
  mv "libgd-$ZNN_LIBGD_VERSION" "libgd-$SERVICE-$ZNN_LIBGD_VERSION"
  echo "    Configuring and compiling libgd"
  cd "libgd-$SERVICE-$ZNN_LIBGD_VERSION"
  ./configure --with-png=$ZNN_HOME/sw-bin/libpng-$SERVICE-$ZNN_LIBPNG_VERSION \
    --prefix=$ZNN_HOME/sw-bin/libgd-$SERVICE-$ZNN_LIBGD_VERSION
  make
  make install
  [ -d "$ZNN_HOME/sw-bin/libgd-$SERVICE-$ZNN_LIBGD_VERSION" ] || {
    echo "libgd failed to install successfully"
    exit 1
  }
else 
  echo "    libgd is already installed"
fi

echo "  Installing PHP"
echo "    Checking that php is not already installed in '$ZNN_HOME/sw-bin'"
if [ ! -d "$ZNN_HOME/sw-bin/php-$SERVICE-$ZNN_PHP_VERSION" \
    -a ! -d "$ZNN_HOME/sw-exp/php-$SERVICE-$ZNN_PHP_VERSION" ]; then 

  echo "    Uncompressing php source directory"
  cd "$ZNN_HOME/sw-exp"
  tar zxf "../sw-src/php-$ZNN_PHP_VERSION.tar.gz"
  mv "php-$ZNN_PHP_VERSION" "php-$SERVICE-$ZNN_PHP_VERSION"
  echo "    Configuring and compiling php"
  cd "php-$SERVICE-$ZNN_PHP_VERSION"
  ./configure --prefix=$ZNN_PHP --with-apxs2=$ZNN_HTTPD/bin/apxs \
    --with-mysqli --with-config-file-path=$ZNN_PHP/config \
    --with-pdo-mysql --with-png-dir=$ZNN_HOME/sw-bin/libpng-$SERVICE-$ZNN_LIBPNG_VERSION --with-gd --with-freetype-dir=$ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION

  make
  make install

  [ -d "$ZNN_PHP" ] || {
    echo "PHP failed to install successfully"
    exit 1
  }
  echo "    Creating php config"
  cd $ZNN_PHP
  mkdir config
  cp $ZNN_HOME/sw-exp/php-$SERVICE-$ZNN_PHP_VERSION/php.ini-production config/php.ini
else 
  echo "    PHP is already installed"
fi

echo "  Installing znn..."
cd $ZNN_HOME/news-src
./build-ld
./rebuild-image-dir

echo "  Configuring web server"
#cp $ZNN_HOME/znn-conf/web/httpd.conf $ZNN_HTTPD/conf/
cp $ZNN_HTTPD/conf/httpd.conf{,-orig}
echo "    Updating http.conf to add php support"
find_line MODULE_LINE "$ZNN_HTTPD/conf/httpd.conf" \
    '^#LoadModule rewrite.*$'
echo "     line to put php5_module is $MODULE_LINE"
insert_line "$ZNN_HTTPD/conf/httpd.conf" $MODULE_LINE \
    "LoadModule php5_module modules/libphp5.so"
MODULE_LINE=$(($MODULE_LINE+1))
#find_line MODULE_LINE "$ZNN_HTTPD/conf/httpd.conf" \
#    '^LoadModule php5_module.*$'
#echo "     line where php5 module is loaded is $MODULE_LINE"
insert_line "$ZNN_HTTPD/conf/httpd.conf" $MODULE_LINE \
    "AddType application/x-httpd-php .php"
MODULE_LINE=$(($MODULE_LINE+1))
insert_line "$ZNN_HTTPD/conf/httpd.conf" $MODULE_LINE \
    "LoadFile $ZNN_HOME/sw-bin/libpng-$SERVICE-$ZNN_LIBPNG_VERSION/lib/libpng.so"
MODULE_LINE=$(($MODULE_LINE+1))
insert_line "$ZNN_HTTPD/conf/httpd.conf" $MODULE_LINE \
    "LoadFile $ZNN_HOME/sw-bin/freetype-$SERVICE-$ZNN_FREETYPE_VERSION/lib/freetype.so"
echo "    Updating http.conf to point to znn"
replace_line_regex "$ZNN_HTTPD/conf/httpd.conf" \
    "^DocumentRoot.*sw-bin/httpd-$SERVICE-$ZNN_HTTPD_VERSION/htdocs.*\$" \
    "DocumentRoot \"$ZNN_HOME/znn\""
replace_line_regex "$ZNN_HTTPD/conf/httpd.conf" \
    "<Directory .*sw-bin/httpd-$SERVICE-$ZNN_HTTPD_VERSION/htdocs.*\$" \
    "<Directory \"$ZNN_HOME/znn\">"

echo "    Updating znn to add database information"
replace_line_regex "$ZNN_HOME/znn/news.php" \
    "^.db_name = .*" \
    "\$db_name = \"$ZNN_MYSQL_DB_NAME\";"
replace_line_regex "$ZNN_HOME/znn/news.php" \
    "^.db_username = .*" \
    "\$db_username = \"$ZNN_MYSQL_USERNAME\";"
replace_line_regex "$ZNN_HOME/znn/news.php" \
    "^.db_password = .*" \
    "\$db_password = \"$ZNN_MYSQL_PASSWORD\";"
replace_line_regex "$ZNN_HOME/znn/news.php" \
    "^.db_port =.*" \
    "\$db_port = \"$ZNN_MYSQL_LOCAL_PORT\";"
	
echo "Configuring effectors"
for i in sw-src/znn-effectors-templates/*; do   
  echo "processing " $(basename $i)
  sed 's/APACHE_INSTALL/'$ZNN_HTTPD'/bin/httpd -k restart/' < $i > effectors/$(basename $i)
done
	
cd $START_DIR
if [ "$DO_CONFIGURE" == "yes" ]; then
  echo "Configuring ZNN to point to the database"
  setup_znn_properties "$ZNN_PROP_FILE"
  db=`echo $customize_system_target_db | sed -e 's/^ *//g' -e 's/ *$//g'`
  replace_line_regex "$ZNN_HOME/znn/news.php" \
    "^.db_host =.*" \
    "\$db_host = \"$db\";"
  server="customize_system_target_$WEB_SERVER"
  serverPort="customize_system_target_${WEB_SERVER}_httpPort"
  lbp=${!serverPort} 
  echo "server=$server, serverPort=$serverPort, lbp=$lbp, cstw0httpPort=$customize_system_target_web0_httpPort"
  lbp=`echo $lbp | sed -e 's/^ *//g' -e 's/ *$//g'`
  echo "Making load balancer listen on port $lbp"
  replace_line_regex "$ZNN_HTTPD/conf/httpd.conf" \
    "^Listen.*" \
    "Listen $lbp"
fi
echo "Web server and znn successfully installed."
if [ "$DO_CONFIGURE" == "no" ]; then
echo "CONFIGURURATION STILL NEEDED."
echo "To configure znn and the web server, enter this IP as a"
echo "  customize.system.target.web<n> property in rainbow.properties"
echo "  Then use configure-znn.sh in the machine that will run"
echo "  the Rainbow master."
echo "Once configured, start the web server by issuing the command:"
echo "  $ZNN_HTTPD/bin/httpd start"
else
echo "Start the web server by issuing the command:"
echo "  $ZNN_HTTPD/bin/httpd -k start"
fi

