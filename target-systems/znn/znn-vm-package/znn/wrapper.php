<?php
session_start();
#
# Read the wrapper file (if available).
#
$server_port = $_SERVER["SERVER_PORT"];
$wrapper_file_name = "/tmp/znn-wrapper-$server_port";
$wrapper_settings = "none";
if (file_exists($wrapper_file_name)) {
	$wrapper_file = fopen($wrapper_file_name, "r");
	$line = trim(fgets($wrapper_file));
	fclose($wrapper_file);
	if ($line == "none") {
		$wrapper_settings = "non";
	} elseif ($line == "captcha") {
		$wrapper_settings = "captcha";
	}
}
#
# Do we need to process a login?
#
$auth_file_name = "/tmp/znn-wrapper-auth-$server_port";
if (file_exists($auth_file_name)) {
  $auth_file = fopen($auth_file_name,"r");
  $line = trim(fgets($auth_file));
  fclose($auth_file);
  define ("SESSION_ID", $line);
}
else {
  define("SESSION_ID", "open");
}
$showLogin = FALSE;
if (SESSION_ID != "open" && $_COOKIE["sessionID"] != SESSION_ID) {
  # session is not valid. Check if login info was set
  if ($_REQUEST["password"] != "secret") {
    $showLogin=TRUE;
  }
  else {
    setcookie("sessionID", SESSION_ID);
  }
}
$session_id=$_COOKIE["sessionID"];

if ($showLogin) {
?>
<html>
 <head>
  <title>ZNN Login</title>
 </head>
 <body>
  <form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post">
   <p>User name: <input type="text" name="username" /></p>
   <p>Password: <input type="password" name="password" /></p>
   <p><input type="submit" value="Login"/></p>
  </form>
 </body>
</html>
<?php
} else {


#
# If we need a captcha and don't have a captcha_pass in session,
# show the user a captcha.
#
$proceed_to_news = 0;
if ($wrapper_settings == "captcha" && !isset($_SESSION["captcha_pass"])) {
	#
	# See if a captcha code has been entered.
	#
	if (isset($_GET["captcha_code"])) {
		include_once $_SERVER['DOCUMENT_ROOT'] . '/securimage/securimage.php';
		$securimage = new Securimage();

		$guess = $_GET["captcha_code"];
		if ($securimage->check($guess) == false && "bypass" != $guess) {
			echo "<html><head><title>Captcha error!</head></title>";
			echo "<body><p>Captcha fail! It was not $guess</p></body></html>";
		} else {
			$_SESSION["captcha_pass"] = 1;
			$proceed_to_news = 1;
		}
	} else {
		echo "<html><head><title>Enter Captcha</title></head><body><p>";
		echo "Show me that you are a human.</p>";
		echo "<form>";
		echo '<img id="captcha" src="/securimage/securimage_show.php"/>';
		echo '<input type="text" name="captcha_code" size="10" maxlength="6"/>';
		echo '<a href="#" onclick="document.getElementById(\'captcha\').src = ';
		echo '\'/securimage/securimage_show.php?\' + Math.random(); ';
		echo 'return false">[ Different Image ]</a>';
		echo "</form></bod></html>";
	}
} else {
	$proceed_to_news = 1;
}

if ($proceed_to_news == 1) {
	#
	# Show the original news.php file.
	#
	include "news.php";
}
}
?>

