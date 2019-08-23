#!/bin/bash
echo "Loading properties..."
ZNN_BIN_DIR="$( cd "$( dirname "$0" )" && pwd )"
[ -a "$ZNN_BIN_DIR/../znn-config" ] \
  && source "$ZNN_BIN_DIR/../znn-config" || {
    echo "Failed to configure the environment"
    echo "  Expected to find znn-config in '$ZNN_BIN_DIR/..'"
    exit 1;
}
echo "Installing mysql..."
echo "  Checking that mysql is not already installed"
[ ! -d "$ZNN_HOME/sw-bin/mysql-$ZNN_MYSQL_VERSION" -a ! -d "$ZNN_HOME/sw-exp/mysql-$ZNN_MYSQL_VERSION" ] || {
  echo "    Mysql is already installed"
  exit 1
}
echo "    mysql is not installed"
echo "  Decompressing mysql source code"
cd $ZNN_HOME/sw-exp
tar xfz "../sw-src/mysql-$ZNN_MYSQL_VERSION.tar.gz"
cd "mysql-$ZNN_MYSQL_VERSION"

echo "  Compiling mysql"
cmake "-DCMAKE_INSTALL_PREFIX=$ZNN_MYSQL" \
  "-DMYSQL_DATADIR=$ZNN_MYSQL/data" \
  "-DSYSCONFDIR=$ZNN_MYSQL/etc" \
  "-DMYSQL_UNIX_ADDR=$ZNN_MYSQL/mysql.sock" \
  "-DMYSQL_TCP_PORT=$ZNN_MYSQL_LOCAL_PORT" \
  . || {
  echo "Failed to configure MySQL."
  exit 1;
}

make
make install

echo "Creating intial mysql database"
cd "$ZNN_MYSQL"
./scripts/mysql_install_db --user=$USER --basedir=. \
  --datadir=./data


echo "Configuring mysql..."
sudo cp $ZNN_MYSQL/support-files/my-small.cnf /etc/my.cnf
./bin/mysqld_safe --user=$USER&
sleep 10
cd bin
./mysql --user=$ZNN_MYSQL_ROOT_USERNAME \
  --socket=$ZNN_MYSQL/mysql.sock <<EOF
create user $ZNN_MYSQL_USERNAME@localhost identified by '$ZNN_MYSQL_PASSWORD';
create user $ZNN_MYSQL_USERNAME identified by '$ZNN_MYSQL_PASSWORD';
grant all on $ZNN_MYSQL_DB_NAME.* to $ZNN_MYSQL_USERNAME;
create database $ZNN_MYSQL_DB_NAME;
set password = password('$ZNN_MYSQL_ROOT_PASSWORD');
EOF

echo "Creating initial znn database"
./mysql --user=$ZNN_MYSQL_USERNAME --socket=$ZNN_MYSQL/mysql.sock \
  --password=$ZNN_MYSQL_PASSWORD $ZNN_MYSQL_DB_NAME <<EOF
create table news (
  news_id integer not null primary key,
  news_title varchar(100),
  news_text text,
  news_img_cnt integer
);

create table img (
  img_id integer not null primary key,
  news_id integer,
  img_high_res varchar(200),
  img_low_res varchar(200)
);
EOF

echo "Loading znn database data"
cd $ZNN_HOME/news-src
./build-ld
./gen-sql
$ZNN_MYSQL/bin/mysql --user=$ZNN_MYSQL_USERNAME \
  --password=$ZNN_MYSQL_PASSWORD --socket=$ZNN_MYSQL/mysql.sock \
  $ZNN_MYSQL_DB_NAME < load-news.sql 

echo "Shutting down mysql."
killall mysqld
echo "To start the mysql server, execute the command: $ZNN_MYSQL/bin/mysqld&"
