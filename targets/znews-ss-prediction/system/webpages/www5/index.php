<?php

# Read a file to determine if we need to randomly reject request
$filename = "reject.mod";
$handle = fopen($filename, "r");
$content = fread($handle, filesize($filename));
fclose($handle);
if ($content > 1 && time()%$content == 0) {
  echo <<<REJECT
<html>
<head>
<title>Please come back...</title>
</head>
<body>
I (the server) am <em>swamped</em> right now!!<br>
Please kindly come back in a couple of minutes.<br>
Sorry for the inconvenience, and thanks for understanding!
</body>
</html>
REJECT;
  exit();
}

/*
$mysql_url = '';
$mysql_user = '';
$mysql_password = '';
$mysql_db = '';

$link = mysql_connect($mysql_url, $mysql_user, $mysql_password)
  or die('Could not connect: ' . mysql_error());

$mydb = mysql_select_db($mysql_db, $link);
if(!$mydb) {
 die('Could not select Test database: ' .  mysql_error());
}

// Performing SQL query
$query = "SELECT COUNT(*) FROM cur";
$result = mysql_query($query, $link)
  or die('Query COUNT failed: ' . mysql_error());
$row = mysql_fetch_row($result);
if ($row != false) {
  $numItems = $row[0];
}

$idx = rand(1, $numItems);
$query = "SELECT cur_text FROM cur LIMIT $idx,1";
$result = mysql_query($query, $link)
  or die('Query item $idx failed: ' . mysql_error());
$row = mysql_fetch_row($result);
if($row != false) {
  $text = $row[0];
}
*/
$idx = 0;
$text = "";

echo <<<END
<html>
<head>
<title>Somewhere Over the Rainbow</title>
<style>
pre {
    border: 1pt dashed black;
    white-space: pre;
    font-size: 8pt;
    overflow: auto;
    padding: 1em 0;
}
</style>
</head>
<body>
<h2>Z.com News - Hello World! (High-fidelity Settings)</h2>
This file contains a 1024x768 pixel image.  The total size of this file is approximately 205K.
<!--p>You had $numItems test items in the database, randomly fetched $idx:</p>
<pre>$text
</pre-->
<img src="large.jpg">
</body>
</html>
END

?>
